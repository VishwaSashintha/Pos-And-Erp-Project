package com.gradge.erp.finance.controller;

import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.finance.dto.*;
import com.gradge.erp.finance.entity.Expense;
import com.gradge.erp.finance.entity.Income;
import com.gradge.erp.finance.service.FinanceService;
import com.gradge.erp.finance.service.LedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('VIEW_FINANCES', 'RECORD_TRANSACTIONS')")
public class FinanceController {

    private final FinanceService financeService;
    private final LedgerService ledgerService;
    private final FinanceMapper financeMapper;

    @PostMapping("/expenses")
    public ApiResponse<ExpenseResponseDto> createExpense(@Valid @RequestBody ExpenseRequestDto dto) {
        Expense entity = financeMapper.toExpenseEntity(dto);
        Expense saved = financeService.createExpense(entity);
        return ApiResponse.success("Expense recorded successfully", financeMapper.toExpenseResponseDto(saved));
    }

    @GetMapping("/expenses/{tenantId}")
    public ApiResponse<List<ExpenseResponseDto>> getAllExpenses(@PathVariable("tenantId") UUID tenantId) {
        List<Expense> expenses = financeService.getAllExpenses(tenantId);
        return ApiResponse.success(financeMapper.toExpenseResponseDtoList(expenses));
    }

    @GetMapping("/expenses/{tenantId}/{id}")
    public ApiResponse<ExpenseResponseDto> getExpense(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        Expense expense = financeService.getExpense(id, tenantId);
        return ApiResponse.success(financeMapper.toExpenseResponseDto(expense));
    }

    @PutMapping("/expenses/{tenantId}/{id}")
    public ApiResponse<ExpenseResponseDto> updateExpense(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id,
            @Valid @RequestBody ExpenseRequestDto dto
    ) {
        Expense entity = financeMapper.toExpenseEntity(dto);
        Expense updated = financeService.updateExpense(id, entity, tenantId);
        return ApiResponse.success("Expense updated successfully", financeMapper.toExpenseResponseDto(updated));
    }

    @DeleteMapping("/expenses/{tenantId}/{id}")
    public ApiResponse<Void> deleteExpense(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        financeService.deleteExpense(id, tenantId);
        return ApiResponse.success("Expense deleted successfully", null);
    }

    @PostMapping("/incomes")
    public ApiResponse<IncomeResponseDto> createIncome(@Valid @RequestBody IncomeRequestDto dto) {
        Income entity = financeMapper.toIncomeEntity(dto);
        Income saved = financeService.createIncome(entity);
        return ApiResponse.success("Income recorded successfully", financeMapper.toIncomeResponseDto(saved));
    }

    @GetMapping("/incomes/{tenantId}")
    public ApiResponse<List<IncomeResponseDto>> getAllIncomes(@PathVariable("tenantId") UUID tenantId) {
        List<Income> incomes = financeService.getAllIncomes(tenantId);
        return ApiResponse.success(financeMapper.toIncomeResponseDtoList(incomes));
    }

    @GetMapping("/incomes/{tenantId}/{id}")
    public ApiResponse<IncomeResponseDto> getIncome(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        Income income = financeService.getIncome(id, tenantId);
        return ApiResponse.success(financeMapper.toIncomeResponseDto(income));
    }

    @PutMapping("/incomes/{tenantId}/{id}")
    public ApiResponse<IncomeResponseDto> updateIncome(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id,
            @Valid @RequestBody IncomeRequestDto dto
    ) {
        Income entity = financeMapper.toIncomeEntity(dto);
        Income updated = financeService.updateIncome(id, entity, tenantId);
        return ApiResponse.success("Income updated successfully", financeMapper.toIncomeResponseDto(updated));
    }

    @DeleteMapping("/incomes/{tenantId}/{id}")
    public ApiResponse<Void> deleteIncome(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        financeService.deleteIncome(id, tenantId);
        return ApiResponse.success("Income deleted successfully", null);
    }

    @GetMapping("/reports/profit-loss/{tenantId}")
    public ApiResponse<FinanceService.ProfitAndLossReport> getProfitAndLoss(
            @PathVariable("tenantId") UUID tenantId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.success(financeService.getProfitAndLossReport(tenantId, startDate, endDate));
    }

    @GetMapping("/reports/trial-balance/{tenantId}")
    public ApiResponse<LedgerService.TrialBalance> getTrialBalance(@PathVariable("tenantId") UUID tenantId) {
        return ApiResponse.success(ledgerService.getTrialBalance(tenantId));
    }

    @GetMapping("/reports/balance-sheet/{tenantId}")
    public ApiResponse<LedgerService.BalanceSheet> getBalanceSheet(@PathVariable("tenantId") UUID tenantId) {
        return ApiResponse.success(ledgerService.getBalanceSheet(tenantId));
    }
}
