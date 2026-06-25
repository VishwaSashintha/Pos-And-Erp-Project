package com.gradge.erp.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IncomeResponseDto {

    private UUID id;
    private String description;
    private String category;
    private BigDecimal amount;
    private LocalDate date;
    private String reference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
