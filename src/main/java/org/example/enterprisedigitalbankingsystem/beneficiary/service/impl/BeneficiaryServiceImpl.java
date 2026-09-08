package org.example.enterprisedigitalbankingsystem.beneficiary.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.account.repository.AccountRepository;
import org.example.enterprisedigitalbankingsystem.audit.entity.AuditAction;
import org.example.enterprisedigitalbankingsystem.audit.service.AuditService;
import org.example.enterprisedigitalbankingsystem.beneficiary.dto.request.CreateBeneficiaryRequest;
import org.example.enterprisedigitalbankingsystem.beneficiary.dto.request.UpdateBeneficiaryRequest;
import org.example.enterprisedigitalbankingsystem.beneficiary.dto.response.BeneficiaryResponse;
import org.example.enterprisedigitalbankingsystem.beneficiary.entity.Beneficiary;
import org.example.enterprisedigitalbankingsystem.beneficiary.entity.BeneficiaryStatus;
import org.example.enterprisedigitalbankingsystem.beneficiary.mapper.BeneficiaryMapper;
import org.example.enterprisedigitalbankingsystem.beneficiary.repository.BeneficiaryRepository;
import org.example.enterprisedigitalbankingsystem.beneficiary.service.BeneficiaryService;
import org.example.enterprisedigitalbankingsystem.customer.entity.Customer;
import org.example.enterprisedigitalbankingsystem.customer.repository.CustomerRepository;
import org.example.enterprisedigitalbankingsystem.exception.BadRequestException;
import org.example.enterprisedigitalbankingsystem.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final BeneficiaryMapper beneficiaryMapper;
    private final AuditService auditService;

    @Override
    public BeneficiaryResponse addBeneficiary(CreateBeneficiaryRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + request.getCustomerId()));

        Account account = accountRepository.findById(request.getBeneficiaryAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with id: " + request.getBeneficiaryAccountId()));

        if (beneficiaryRepository.existsByCustomerIdAndBeneficiaryAccountId(
                request.getCustomerId(), request.getBeneficiaryAccountId())) {
            throw new BadRequestException("This account is already added as a beneficiary");
        }

        Beneficiary beneficiary = Beneficiary.builder()
                .customer(customer)
                .beneficiaryAccount(account)
                .nickName(request.getNickName())
                .status(BeneficiaryStatus.PENDING)
                .build();

        beneficiary = beneficiaryRepository.save(beneficiary);

        auditService.log(AuditAction.CREATE, "Beneficiary", beneficiary.getId().toString(),
                null, "nickName=" + beneficiary.getNickName() + ", account=" + account.getAccountNumber(),
                "Beneficiary added");

        return beneficiaryMapper.toResponse(beneficiary);
    }

    @Override
    @Transactional(readOnly = true)
    public BeneficiaryResponse getBeneficiaryById(Long beneficiaryId) {
        return beneficiaryMapper.toResponse(findBeneficiaryById(beneficiaryId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getBeneficiariesByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }

        return beneficiaryRepository.findByCustomerId(customerId).stream()
                .map(beneficiaryMapper::toResponse)
                .toList();
    }

    @Override
    public BeneficiaryResponse updateBeneficiary(Long beneficiaryId, UpdateBeneficiaryRequest request) {
        Beneficiary beneficiary = findBeneficiaryById(beneficiaryId);
        String oldNickName = beneficiary.getNickName();

        beneficiary.setNickName(request.getNickName());
        beneficiary = beneficiaryRepository.save(beneficiary);

        auditService.log(AuditAction.UPDATE, "Beneficiary", beneficiary.getId().toString(),
                oldNickName, beneficiary.getNickName(), "Beneficiary nickname updated");

        return beneficiaryMapper.toResponse(beneficiary);
    }

    @Override
    public void deleteBeneficiary(Long beneficiaryId) {
        Beneficiary beneficiary = findBeneficiaryById(beneficiaryId);

        beneficiaryRepository.delete(beneficiary);

        auditService.log(AuditAction.DELETE, "Beneficiary", beneficiaryId.toString(),
                beneficiary.getNickName(), null, "Beneficiary removed");
    }

    private Beneficiary findBeneficiaryById(Long beneficiaryId) {
        return beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Beneficiary not found with id: " + beneficiaryId));
    }
}
