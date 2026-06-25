package com.gradge.erp.customer.controller;

import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.customer.dto.CustomerMapper;
import com.gradge.erp.customer.dto.CustomerRequestDto;
import com.gradge.erp.customer.dto.CustomerResponseDto;
import com.gradge.erp.customer.entity.Customer;
import com.gradge.erp.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('RECORD_TRANSACTIONS', 'MANAGE_USERS')")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @PostMapping
    public ApiResponse<CustomerResponseDto> create(@Valid @RequestBody CustomerRequestDto dto) {
        Customer entity = customerMapper.toEntity(dto);
        Customer saved = customerService.createCustomer(entity);
        return ApiResponse.success("Customer created successfully", customerMapper.toResponseDto(saved));
    }

    @GetMapping("/{tenantId}")
    public ApiResponse<List<CustomerResponseDto>> getAll(@PathVariable("tenantId") UUID tenantId) {
        List<Customer> customers = customerService.getAllCustomers(tenantId);
        return ApiResponse.success(customerMapper.toResponseDtoList(customers));
    }

    @GetMapping("/{tenantId}/{id}")
    public ApiResponse<CustomerResponseDto> get(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        Customer customer = customerService.getCustomer(id, tenantId);
        return ApiResponse.success(customerMapper.toResponseDto(customer));
    }

    @PutMapping("/{tenantId}/{id}")
    public ApiResponse<CustomerResponseDto> update(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id,
            @Valid @RequestBody CustomerRequestDto dto
    ) {
        Customer entity = customerMapper.toEntity(dto);
        Customer updated = customerService.updateCustomer(id, entity, tenantId);
        return ApiResponse.success("Customer updated successfully", customerMapper.toResponseDto(updated));
    }

    @DeleteMapping("/{tenantId}/{id}")
    public ApiResponse<Void> delete(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        customerService.deleteCustomer(id, tenantId);
        return ApiResponse.success("Customer deleted successfully", null);
    }
}