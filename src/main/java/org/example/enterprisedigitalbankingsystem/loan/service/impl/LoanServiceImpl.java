package org.example.enterprisedigitalbankingsystem.loan.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.account.repository.AccountRepository;
import org.example.enterprisedigitalbankingsystem.customer.entity.Customer;
import org.example.enterprisedigitalbankingsystem.customer.repository.CustomerRepository;
import org.example.enterprisedigitalbankingsystem.emi.service.EmiService;
import org.example.enterprisedigitalbankingsystem.exception.BadRequestException;
import org.example.enterprisedigitalbankingsystem.exception.ResourceNotFoundException;
import org.example.enterprisedigitalbankingsystem.loan.dto.request.CreateLoanRequest;
import org.example.enterprisedigitalbankingsystem.loan.dto.response.LoanResponse;
import org.example.enterprisedigitalbankingsystem.loan.entity.Loan;
import org.example.enterprisedigitalbankingsystem.loan.entity.LoanStatus;
import org.example.enterprisedigitalbankingsystem.loan.mapper.LoanMapper;
import org.example.enterprisedigitalbankingsystem.loan.repository.LoanRepository;
import org.example.enterprisedigitalbankingsystem.loan.service.LoanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final LoanMapper loanMapper;
    private final EmiService emiService;

    @Override
    public LoanResponse applyForLoan(CreateLoanRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
        Account account = accountRepository.findById(request.getDisbursementAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + request.getDisbursementAccountId()));
        BigDecimal emiAmount = calculateEmi(request.getPrincipalAmount() ,request.getInterestRate(),request.getTenureMonths());
        Loan loan = Loan.builder()
                .customer(customer)
                .disbursementAccount(account)
                .loanType(request.getLoanType())
                .principalAmount(request.getPrincipalAmount())
                .interestRate(request.getInterestRate())
                .tenureMonths(request.getTenureMonths())
                .emiAmount(emiAmount)
                .status(LoanStatus.PENDING)
                .build();
        loan = loanRepository.save(loan);
        return loanMapper.toResponse(loan);

    }

    private BigDecimal calculateEmi(BigDecimal principalAmount, BigDecimal interestRate, Integer tenureMonths) {
        double p = principalAmount.doubleValue();
        double r = interestRate.doubleValue()/12/100;
        double factor = Math.pow(1+r,tenureMonths);
        double emi = p * r* factor /(factor - 1);
        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);

    }

    @Override
    @Transactional(readOnly = true)
    public LoanResponse getLoanById(Long loanId) {
        return loanMapper.toResponse(findLoanById(loanId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByCustomerId(Long customerId) {
        if(!customerRepository.existsById(customerId)){
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }
        return loanRepository.findByCustomerId(customerId).stream().map(loanMapper::toResponse).toList();
    }

    @Override
    public LoanResponse approveLoan(Long loanId) {
        Loan loan = findLoanById(loanId);
        if(loan.getStatus() != LoanStatus.PENDING){
            throw new BadRequestException("Loan is " + loan.getStatus() + " and cannot be approved");
        }
        Account account = loan.getDisbursementAccount();
        account.setBalance(account.getBalance().add(loan.getPrincipalAmount()));
        accountRepository.save(account);

        loan.setStatus(LoanStatus.ACTIVE);
        loan.setApprovedAt(LocalDateTime.now());
        loan = loanRepository.save(loan);
        emiService.generateSchedule(loan);
        return loanMapper.toResponse(loan);
    }

    @Override
    public LoanResponse rejectLoan(Long loanId) {
        Loan loan = findLoanById(loanId);
        if(loan.getStatus() != LoanStatus.PENDING){
            throw new BadRequestException("Loan is " + loan.getStatus() + " and cannot be rejected");
        }
        loan.setStatus(LoanStatus.REJECTED);
        loanRepository.save(loan);
        return loanMapper.toResponse(loan);
    }
    private Loan findLoanById(Long loanId){
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));
    }
}
