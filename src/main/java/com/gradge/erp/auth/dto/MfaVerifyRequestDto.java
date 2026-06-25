package com.gradge.erp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MfaVerifyRequestDto {
    @NotBlank
    private String email;
    
    @NotBlank
    private String code;
}
