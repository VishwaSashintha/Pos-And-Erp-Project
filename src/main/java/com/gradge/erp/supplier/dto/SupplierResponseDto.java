package com.gradge.erp.supplier.dto;

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
public class SupplierResponseDto {

    private UUID id;
    private String name;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private String taxNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
