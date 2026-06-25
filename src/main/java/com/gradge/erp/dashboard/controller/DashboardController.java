package com.gradge.erp.dashboard.controller;

import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.dashboard.dto.DashboardDTO;
import com.gradge.erp.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ApiResponse<DashboardDTO> getDashboard(@RequestHeader("tenantId") UUID tenantId) {
        DashboardDTO dashboard = dashboardService.getDashboard(tenantId);
        return ApiResponse.success("Dashboard data retrieved successfully", dashboard);
    }
}
