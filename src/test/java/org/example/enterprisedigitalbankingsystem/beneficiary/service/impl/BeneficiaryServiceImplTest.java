package org.example.enterprisedigitalbankingsystem.beneficiary.service.impl;

import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountStatus;
import org.example.enterprisedigitalbankingsystem.account.entity.AccountType;
import org.example.enterprisedigitalbankingsystem.account.repository.AccountRepository;
import org.example.enterprisedigitalbankingsystem.audit.service.AuditService;
import org.example.enterprisedigitalbankingsystem.beneficiary.dto.request.CreateBeneficiaryRequest;
import org.example.enterprisedigitalbankingsystem.beneficiary.dto.request.UpdateBeneficiaryRequest;
import org.example.enterprisedigitalbankingsystem.beneficiary.dto.response.BeneficiaryResponse;
import org.example.enterprisedigitalbankingsystem.beneficiary.entity.Beneficiary;
import org.example.enterprisedigitalbankingsystem.beneficiary.entity.BeneficiaryStatus;
import org.example.enterprisedigitalbankingsystem.beneficiary.mapper.BeneficiaryMapper;
import org.example.enterprisedigitalbankingsystem.beneficiary.repository.BeneficiaryRepository;
import org.example.enterprisedigitalbankingsystem.customer.entity.Customer;
import org.example.enterprisedigitalbankingsystem.customer.repository.CustomerRepository;
import org.example.enterprisedigitalbankingsystem.exception.BadRequestException;
import org.example.enterprisedigitalbankingsystem.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficiaryServiceImplTest {

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AuditService auditService;

    private BeneficiaryServiceImpl beneficiaryService;

    private Customer customer;
    private Account account;

    @BeforeEach
    void setUp() {
        beneficiaryService = new BeneficiaryServiceImpl(
                beneficiaryRepository, customerRepository, accountRepository,
                new BeneficiaryMapper(), auditService);

        customer = Customer.builder()
                .id(1L)
                .build();

        account = Account.builder()
                .id(2L)
                .accountNumber("ACC-002")
                .accountHolderName("Bob")
                .branch("Main")
                .accountType(AccountType.SAVINGS)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
    }

    private Beneficiary buildBeneficiary(Long id, BeneficiaryStatus status, String nickName) {
        return Beneficiary.builder()
                .id(id)
                .customer(customer)
                .beneficiaryAccount(account)
                .nickName(nickName)
                .status(status)
                .build();
    }

    @Test
    void addBeneficiary_whenValid_savesAsPendingAndLogsAudit() {
        CreateBeneficiaryRequest request = CreateBeneficiaryRequest.builder()
                .customerId(1L)
                .beneficiaryAccountId(2L)
                .nickName("Bob Savings")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(account));
        when(beneficiaryRepository.existsByCustomerIdAndBeneficiaryAccountId(1L, 2L)).thenReturn(false);
        when(beneficiaryRepository.save(any(Beneficiary.class)))
                .thenAnswer(invocation -> {
                    Beneficiary saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

        BeneficiaryResponse response = beneficiaryService.addBeneficiary(request);

        ArgumentCaptor<Beneficiary> captor = ArgumentCaptor.forClass(Beneficiary.class);
        verify(beneficiaryRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(BeneficiaryStatus.PENDING);
        assertThat(captor.getValue().getNickName()).isEqualTo("Bob Savings");

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(BeneficiaryStatus.PENDING);
        assertThat(response.getAccountId()).isEqualTo(2L);

        verify(auditService).log(any(), anyString(), anyString(), any(), any(), anyString());
    }

    @Test
    void addBeneficiary_whenCustomerMissing_throwsResourceNotFoundException() {
        CreateBeneficiaryRequest request = CreateBeneficiaryRequest.builder()
                .customerId(99L)
                .beneficiaryAccountId(2L)
                .nickName("Bob Savings")
                .build();

        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> beneficiaryService.addBeneficiary(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(beneficiaryRepository, never()).save(any());
    }

    @Test
    void addBeneficiary_whenAccountMissing_throwsResourceNotFoundException() {
        CreateBeneficiaryRequest request = CreateBeneficiaryRequest.builder()
                .customerId(1L)
                .beneficiaryAccountId(999L)
                .nickName("Bob Savings")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> beneficiaryService.addBeneficiary(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(beneficiaryRepository, never()).save(any());
    }

    @Test
    void addBeneficiary_whenAlreadyExists_throwsBadRequestException() {
        CreateBeneficiaryRequest request = CreateBeneficiaryRequest.builder()
                .customerId(1L)
                .beneficiaryAccountId(2L)
                .nickName("Bob Savings")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(account));
        when(beneficiaryRepository.existsByCustomerIdAndBeneficiaryAccountId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> beneficiaryService.addBeneficiary(request))
                .isInstanceOf(BadRequestException.class);

        verify(beneficiaryRepository, never()).save(any());
    }

    @Test
    void getBeneficiaryById_whenFound_returnsMappedResponse() {
        Beneficiary beneficiary = buildBeneficiary(10L, BeneficiaryStatus.ACTIVE, "Bob Savings");
        when(beneficiaryRepository.findById(10L)).thenReturn(Optional.of(beneficiary));

        BeneficiaryResponse response = beneficiaryService.getBeneficiaryById(10L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getNickName()).isEqualTo("Bob Savings");
        assertThat(response.getStatus()).isEqualTo(BeneficiaryStatus.ACTIVE);
    }

    @Test
    void getBeneficiaryById_whenNotFound_throwsResourceNotFoundException() {
        when(beneficiaryRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> beneficiaryService.getBeneficiaryById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void getBeneficiariesByCustomerId_whenCustomerExists_returnsMappedList() {
        Beneficiary beneficiary = buildBeneficiary(10L, BeneficiaryStatus.ACTIVE, "Bob Savings");
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(beneficiaryRepository.findByCustomerId(1L)).thenReturn(List.of(beneficiary));

        List<BeneficiaryResponse> responses = beneficiaryService.getBeneficiariesByCustomerId(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(10L);
    }

    @Test
    void getBeneficiariesByCustomerId_whenCustomerMissing_throwsResourceNotFoundException() {
        when(customerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> beneficiaryService.getBeneficiariesByCustomerId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(beneficiaryRepository, never()).findByCustomerId(any());
    }

    @Test
    void updateBeneficiary_whenFound_updatesNickNameAndLogsAudit() {
        Beneficiary beneficiary = buildBeneficiary(10L, BeneficiaryStatus.ACTIVE, "Old Nick");
        when(beneficiaryRepository.findById(10L)).thenReturn(Optional.of(beneficiary));
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateBeneficiaryRequest request = UpdateBeneficiaryRequest.builder()
                .nickName("New Nick")
                .build();

        BeneficiaryResponse response = beneficiaryService.updateBeneficiary(10L, request);

        assertThat(response.getNickName()).isEqualTo("New Nick");
        verify(auditService).log(any(), anyString(), anyString(), eq("Old Nick"), eq("New Nick"), anyString());
    }

    @Test
    void updateBeneficiary_whenNotFound_throwsResourceNotFoundException() {
        when(beneficiaryRepository.findById(404L)).thenReturn(Optional.empty());

        UpdateBeneficiaryRequest request = UpdateBeneficiaryRequest.builder()
                .nickName("New Nick")
                .build();

        assertThatThrownBy(() -> beneficiaryService.updateBeneficiary(404L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(beneficiaryRepository, never()).save(any());
    }

    @Test
    void deleteBeneficiary_whenFound_deletesAndLogsAudit() {
        Beneficiary beneficiary = buildBeneficiary(10L, BeneficiaryStatus.ACTIVE, "Bob Savings");
        when(beneficiaryRepository.findById(10L)).thenReturn(Optional.of(beneficiary));

        beneficiaryService.deleteBeneficiary(10L);

        verify(beneficiaryRepository).delete(beneficiary);
        verify(auditService).log(any(), anyString(), eq("10"), eq("Bob Savings"), any(), anyString());
    }

    @Test
    void deleteBeneficiary_whenNotFound_throwsResourceNotFoundException() {
        when(beneficiaryRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> beneficiaryService.deleteBeneficiary(404L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(beneficiaryRepository, never()).delete(any());
    }
}
