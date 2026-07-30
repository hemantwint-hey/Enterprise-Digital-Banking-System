package org.example.enterprisedigitalbankingsystem.customer.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterprisedigitalbankingsystem.customer.entity.KYCStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSearchRequest {

    private Long customerId;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String city;

    private String state;

    private KYCStatus kycStatus;
}