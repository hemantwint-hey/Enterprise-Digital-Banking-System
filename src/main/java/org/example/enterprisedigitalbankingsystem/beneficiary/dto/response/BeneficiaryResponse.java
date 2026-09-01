package org.example.enterprisedigitalbankingsystem.beneficiary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.enterprisedigitalbankingsystem.beneficiary.entity.BeneficiaryStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryResponse {
    private Long id;
    private String nickName;
    private Long accountId;
    private BeneficiaryStatus status;
    private LocalDateTime createdAt;
}
