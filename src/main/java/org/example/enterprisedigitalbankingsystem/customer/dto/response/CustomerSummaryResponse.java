package org.example.enterprisedigitalbankingsystem.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterprisedigitalbankingsystem.customer.entity.KYCStatus;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerSummaryResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long customerId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String city;
    private KYCStatus kycStatus;
}
