package org.example.enterprisedigitalbankingsystem.loan.service.impl;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.example.enterprisedigitalbankingsystem.account.entity.Account;
import org.example.enterprisedigitalbankingsystem.account.repository.AccountRepository;
import org.example.enterprisedigitalbankingsystem.customer.entity.Customer;
import org.example.enterprisedigitalbankingsystem.customer.repository.CustomerRepository;
import org.example.enterprisedigitalbankingsystem.emi.service.EmiService;
import org.example.enterprisedigitalbankingsystem.exception.ResourceNotFoundException;
import org.example.enterprisedigitalbankingsystem.loan.dto.request.CreateLoanRequest;
import org.example.enterprisedigitalbankingsystem.loan.dto.response.LoanResponse;
import org.example.enterprisedigitalbankingsystem.loan.entity.Loan;
import org.example.enterprisedigitalbankingsystem.loan.entity.LoanStatus;
import org.example.enterprisedigitalbankingsystem.loan.mapper.LoanMapper;
import org.example.enterprisedigitalbankingsystem.loan.repository.LoanRepository;
import org.example.enterprisedigitalbankingsystem.loan.service.LoanService;

import java.math.BigDecimal;
import java.util.List;

public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private  final LoanMapper loanMapper;
    private final EmiService emiService;

    public LoanServiceImpl(LoanRepository loanRepository, CustomerRepository customerRepository, AccountRepository accountRepository, LoanMapper loanMapper, EmiService emiService) {
        this.loanRepository = loanRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.loanMapper = loanMapper;
        this.emiService = emiService;
    }

    @Override
    public LoanResponse applyForLoan(CreateLoanRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
        Account account = accountRepository.findById(request.getDisbursementAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id"+ request
                        .getDisbursementAccountId()));
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

    private BigDecimal calculateEmi(
            @NotNull(message = "Principal amounnt is required")
            @DecimalMin(value = "1000.00", message = "Principal must be at least 1000") BigDecimal principalAmount,
            @NotNull(message = "Interest rate is required")
            @DecimalMin(value = "0.01" , message = "Interest rate must be prositive")
            BigDecimal interestRate, @NotNull(message = "Tenure is required")
            @Min(value = 1, message = "Tenure must be at least 1 month")
            @Max(value = 360 , message = "Tenure cannot exceed  360 months") Integer tenureMonths) {

        double p =

    }

    @Override
    public LoanResponse getLoanById(Long loanId) {
        return null;
    }

    @Override
    public List<LoanResponse> getLoansByCustomerId(Long customerId) {
        return List.of();
    }

    @Override
    public LoanResponse approveLoan(Long loanId) {
        return null;
    }

    @Override
    public LoanResponse rejectLoan(Long loanId) {
        return null;
    }
}
