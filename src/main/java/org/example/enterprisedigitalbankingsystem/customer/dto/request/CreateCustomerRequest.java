package org.example.enterprisedigitalbankingsystem.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCustomerRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 3, max = 50 , message = "First Name must be between  3 to 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 3, max = 50 , message = "First Name must be between  3 to 50 characters")
    private String lastName;

    @NotBlank(message = "Date of Birth is Required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Phone Number is required")
    private String phoneNumber;


    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
