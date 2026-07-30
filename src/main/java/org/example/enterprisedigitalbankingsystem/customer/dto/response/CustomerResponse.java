package org.example.enterprisedigitalbankingsystem.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterprisedigitalbankingsystem.customer.entity.KYCStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private Long customerId;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private String phoneNumber;

    private String address;

    private String city;

    private String state;

    private String country;

    private String postalCode;

    private KYCStatus kycStatus;

    private LocalDateTime createdAt;
}