package com.gradge.erp.finance.service;

import com.gradge.erp.finance.entity.Expense;
import com.gradge.erp.finance.entity.Income;
import com.gradge.erp.finance.repository.ExpenseRepository;
import com.gradge.erp.finance.repository.IncomeRepository;
import com.gradge.erp.pos.entity.Invoice;
import com.gradge.erp.pos.repository.InvoiceRepository;
import com.gradge.erp.tenant.service.TenantService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final InvoiceRepository invoiceRepository;
    private final TenantService tenantService;
    private final LedgerService ledgerService;

    

    @Transactional
    public Expense createExpense(Expense expense) {
        UUID tenantId = tenantService.getCurrentTenantId();
        expense.setTenantId(tenantId);
        if (expense.getDate() == null) {
            expense.setDate(LocalDate.now());
        }
        Expense saved = expenseRepository.save(expense);

        
        if (saved.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            ledgerService.recordTransaction(
                    tenantId,
                    saved.getDate(),
                    "Manual Expense - " + saved.getDescription(),
                    saved.getReference(),
                    Arrays.asList(
                            new LedgerService.LineRequest("6000", saved.getAmount(), true),
                            new LedgerService.LineRequest("1000", saved.getAmount(), false)
                    )
            );
        }

        return saved;
    }

    public List<Expense> getAllExpenses(UUID tenantId) {
        return expenseRepository.findByTenantIdAndDeletedFalse(tenantId);
    }

    public Expense getExpense(UUID id, UUID tenantId) {
        Expense expense = expenseRepository.findByIdAndTenantId(id, tenantId);
        if (expense == null || expense.isDeleted()) {
            throw new RuntimeException("Expense not found");
        }
        return expense;
    }

    public Expense updateExpense(UUID id, Expense updated, UUID tenantId) {
        Expense existing = getExpense(id, tenantId);
        existing.setDescription(updated.getDescription());
        existing.setCategory(updated.getCategory());
        existing.setAmount(updated.getAmount());
        existing.setDate(updated.getDate() != null ? updated.getDate() : existing.getDate());
        existing.setReference(updated.getReference());
        return expenseRepository.save(existing);
    }

    public void deleteExpense(UUID id, UUID tenantId) {
        Expense expense = getExpense(id, tenantId);
        expense.setDeleted(true);
        expenseRepository.save(expense);
    }

    

    @Transactional
    public Income createIncome(Income income) {
        UUID tenantId = tenantService.getCurrentTenantId();
        income.setTenantId(tenantId);
        if (income.getDate() == null) {
            income.setDate(LocalDate.now());
        }
        Income saved = incomeRepository.save(income);

        
        if (saved.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            ledgerService.recordTransaction(
                    tenantId,
                    saved.getDate(),
                    "Manual Income - " + saved.getDescription(),
                    saved.getReference(),
                    Arrays.asList(
                            new LedgerService.LineRequest("1000", saved.getAmount(), true),
                            new LedgerService.LineRequest("4000", saved.getAmount(), false)
                    )
            );
        }

        return saved;
    }

    public List<Income> getAllIncomes(UUID tenantId) {
        return incomeRepository.findByTenantIdAndDeletedFalse(tenantId);
    }

    public Income getIncome(UUID id, UUID tenantId) {
        Income income = incomeRepository.findByIdAndTenantId(id, tenantId);
        if (income == null || income.isDeleted()) {
            throw new RuntimeException("Income not found");
        }
        return income;
    }

    public Income updateIncome(UUID id, Income updated, UUID tenantId) {
        Income existing = getIncome(id, tenantId);
        existing.setDescription(updated.getDescription());
        existing.setCategory(updated.getCategory());
        existing.setAmount(updated.getAmount());
        existing.setDate(updated.getDate() != null ? updated.getDate() : existing.getDate());
        existing.setReference(updated.getReference());
        return incomeRepository.save(existing);
    }

    public void deleteIncome(UUID id, UUID tenantId) {
        Income income = getIncome(id, tenantId);
        income.setDeleted(true);
        incomeRepository.save(income);
    }

    

    public ProfitAndLossReport getProfitAndLossReport(UUID tenantId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        
        List<Income> incomes = incomeRepository.findByTenantIdAndDeletedFalseAndDateBetween(tenantId, startDate, endDate);
        List<Expense> expenses = expenseRepository.findByTenantIdAndDeletedFalseAndDateBetween(tenantId, startDate, endDate);

        
        LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.MAX);
        List<Invoice> invoices = invoiceRepository.findByTenant_IdAndDeletedFalseAndCreatedAtBetween(tenantId, startDateTime, endDateTime);

        
        BigDecimal totalManualIncomes = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSalesInvoiceAmount = invoices.stream()
                .map(Invoice::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSalesPaid = invoices.stream()
                .map(Invoice::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        
        BigDecimal totalRevenue = totalManualIncomes.add(totalSalesInvoiceAmount);
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);

        return ProfitAndLossReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalIncomes(totalManualIncomes)
                .totalExpenses(totalExpenses)
                .totalSales(totalSalesInvoiceAmount)
                .totalSalesPaid(totalSalesPaid)
                .totalRevenue(totalRevenue)
                .netProfit(netProfit)
                .build();
    }

    @Data
    @Builder
    public static class ProfitAndLossReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal totalIncomes;
        private BigDecimal totalExpenses;
        private BigDecimal totalSales;
        private BigDecimal totalSalesPaid;
        private BigDecimal totalRevenue;
        private BigDecimal netProfit;
    }
}
