package com.gradge.erp.tenant.controller;

import com.gradge.erp.billing.model.AppModule;
import com.gradge.erp.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tenant/onboarding")
public class TenantOnboardingController {

    @GetMapping("/modules")
    public ApiResponse<List<Map<String, String>>> getAvailableModules() {
        List<Map<String, String>> modules = Arrays.stream(AppModule.values())
                .map(module -> Map.of(
                        "key", module.name(),
                        "name", formatName(module.name())
                ))
                .collect(Collectors.toList());
        return ApiResponse.success("Fetched modules", modules);
    }

    @GetMapping("/industries")
    public ApiResponse<List<String>> getSupportedIndustries() {
        List<String> industries = Arrays.asList(
                "Retail", "Wholesale", "Manufacturing", "Services", "Restaurant", "IT & Software", "Healthcare", "Other"
        );
        return ApiResponse.success("Fetched industries", industries);
    }

    private String formatName(String name) {
        return Arrays.stream(name.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
