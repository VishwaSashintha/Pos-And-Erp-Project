package com.gradge.erp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MfaSetupRequestDto {
    @NotBlank
    private String email;
}
