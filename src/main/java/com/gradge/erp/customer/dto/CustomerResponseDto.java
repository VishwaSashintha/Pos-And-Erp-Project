package com.gradge.erp.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponseDto {

    private UUID id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String nic;
    private Double totalSpent;
    private Integer visitCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
