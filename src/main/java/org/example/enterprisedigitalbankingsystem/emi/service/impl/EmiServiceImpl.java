package org.example.enterprisedigitalbankingsystem.emi.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.emi.dto.response.EmiResponse;
import org.example.enterprisedigitalbankingsystem.emi.entity.Emi;
import org.example.enterprisedigitalbankingsystem.emi.entity.EmiStatus;
import org.example.enterprisedigitalbankingsystem.emi.mapper.EmiMapper;
import org.example.enterprisedigitalbankingsystem.emi.repository.EmiRepository;
import org.example.enterprisedigitalbankingsystem.emi.service.EmiService;
import org.example.enterprisedigitalbankingsystem.exception.BadRequestException;
import org.example.enterprisedigitalbankingsystem.exception.ResourceNotFoundException;
import org.example.enterprisedigitalbankingsystem.loan.entity.Loan;
import org.example.enterprisedigitalbankingsystem.loan.entity.LoanStatus;
import org.example.enterprisedigitalbankingsystem.loan.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmiServiceImpl implements EmiService {

    private final EmiRepository emiRepository;
    private final LoanRepository loanRepository;   // not LoanService — avoids a circular service dependency
    private final EmiMapper emiMapper;

    @Override
    public void generateSchedule(Loan loan) {
        List<Emi> schedule = new ArrayList<>();
        LocalDate dueDate = LocalDate.now();

        for (int installment = 1; installment <= loan.getTenureMonths(); installment++) {
            dueDate = dueDate.plusMonths(1);
            schedule.add(Emi.builder()
                    .loan(loan)
                    .installmentNumber(installment)
                    .dueDate(dueDate)
                    .amount(loan.getEmiAmount())
                    .status(EmiStatus.PENDING)
                    .build());
        }
        emiRepository.saveAll(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmiResponse> getEmisByLoanId(Long loanId) {
        return emiRepository.findByLoanIdOrderByInstallmentNumberAsc(loanId).stream()
                .map(emiMapper::toResponse)
                .toList();
    }

    @Override
    public EmiResponse payEmi(Long emiId) {
        Emi emi = emiRepository.findById(emiId)
                .orElseThrow(() -> new ResourceNotFoundException("EMI not found with id: " + emiId));

        if (emi.getStatus() == EmiStatus.PAID) {
            throw new BadRequestException("EMI is already paid");
        }

        emi.setStatus(EmiStatus.PAID);
        emi.setPaidAt(LocalDateTime.now());
        emi = emiRepository.save(emi);

        closeLoanIfFullyPaid(emi.getLoan());

        return emiMapper.toResponse(emi);
    }

    private void closeLoanIfFullyPaid(Loan loan) {
        boolean anyUnpaidLeft = emiRepository.existsByLoanIdAndStatusNot(loan.getId(), EmiStatus.PAID);
        if (!anyUnpaidLeft) {
            loan.setStatus(LoanStatus.CLOSED);
            loan.setClosedAt(LocalDateTime.now());
            loanRepository.save(loan);
        }
    }
}
