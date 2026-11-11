package org.example.enterprisedigitalbankingsystem.audit.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enterprisedigitalbankingsystem.audit.entity.AuditAction;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditSearchRequest {

    private UUID performedByUserId;
    private AuditAction action;

    @Size(max = 80, message = "Entity name cannot exceed 80 characters")
    private String entityName;

    @Size(max = 80, message = "Entity id cannot exceed 80 characters")
    private String entityId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;

    @Size(max = 150, message = "Description keyword cannot exceed 150 characters")
    private String descriptionKeyword;

    @JsonIgnore
    @AssertTrue(message = "fromDate cannot be after toDate")
    public boolean isDateRangeValid() {
        return fromDate == null || toDate == null || !fromDate.isAfter(toDate);
    }
}

