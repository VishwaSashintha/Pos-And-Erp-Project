package com.gradge.erp.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {

    private String token;
    private String refreshToken;
    private UUID userId;
    private String username;
    private String role;
    private UUID tenantId;
    private String tenantName;
    private List<String> permissions;
}
