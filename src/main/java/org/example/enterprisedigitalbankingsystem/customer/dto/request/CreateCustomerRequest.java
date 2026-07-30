package org.example.enterprisedigitalbankingsystem.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @Size(min = 3, max = 50 , message = "Last Name must be between  3 to 50 characters")
    private String lastName;

    @NotNull(message = "Date of Birth is Required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Phone Number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Phone number must be of 10 digit"
    )
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 character")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 character")
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 character")
    private String country;

    @NotBlank(message = "PostalCode is required")
    @Pattern(
            regexp = "\\d{9}$",
            message = "Postal Code must be valid 6 - digit PIN CODE"
    )
    private String postalCode;
}
