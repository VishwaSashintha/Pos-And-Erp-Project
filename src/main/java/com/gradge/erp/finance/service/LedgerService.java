package com.gradge.erp.finance.service;

import com.gradge.erp.finance.entity.Account;
import com.gradge.erp.finance.entity.JournalEntry;
import com.gradge.erp.finance.entity.TransactionLine;
import com.gradge.erp.finance.enums.AccountType;
import com.gradge.erp.finance.repository.AccountRepository;
import com.gradge.erp.finance.repository.JournalEntryRepository;
import com.gradge.erp.finance.repository.TransactionLineRepository;
import lombok.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final TransactionLineRepository transactionLineRepository;

    @Transactional
    public void initializeDefaultAccounts(UUID tenantId) {
        List<Account> defaults = Arrays.asList(
                Account.builder().code("1000").name("Cash").type(AccountType.ASSET).balance(BigDecimal.ZERO).build(),
                Account.builder().code("1200").name("Accounts Receivable").type(AccountType.ASSET).balance(BigDecimal.ZERO).build(),
                Account.builder().code("1400").name("Inventory").type(AccountType.ASSET).balance(BigDecimal.ZERO).build(),
                Account.builder().code("2000").name("Accounts Payable").type(AccountType.LIABILITY).balance(BigDecimal.ZERO).build(),
                Account.builder().code("3000").name("Owner Equity").type(AccountType.EQUITY).balance(BigDecimal.ZERO).build(),
                Account.builder().code("4000").name("Sales Revenue").type(AccountType.REVENUE).balance(BigDecimal.ZERO).build(),
                Account.builder().code("5000").name("Cost of Goods Sold").type(AccountType.EXPENSE).balance(BigDecimal.ZERO).build(),
                Account.builder().code("6000").name("Rent / General Expenses").type(AccountType.EXPENSE).balance(BigDecimal.ZERO).build()
        );

        for (Account acc : defaults) {
            acc.setTenantId(tenantId);
            Optional<Account> existing = accountRepository.findByCodeAndTenantIdAndDeletedFalse(acc.getCode(), tenantId);
            if (existing.isEmpty()) {
                accountRepository.save(acc);
            }
        }
    }

    @Transactional
    public JournalEntry recordTransaction(
            UUID tenantId,
            LocalDate entryDate,
            String description,
            String referenceNumber,
            List<LineRequest> lineRequests
    ) {
        if (lineRequests == null || lineRequests.isEmpty()) {
            throw new IllegalArgumentException("Transaction must have at least one line entry");
        }

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (LineRequest req : lineRequests) {
            if (req.isDebit()) {
                totalDebits = totalDebits.add(req.getAmount());
            } else {
                totalCredits = totalCredits.add(req.getAmount());
            }
        }

        
        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new IllegalArgumentException("Transaction is not balanced. Total Debits: " + totalDebits + ", Total Credits: " + totalCredits);
        }

        JournalEntry entry = JournalEntry.builder()
                .entryDate(entryDate != null ? entryDate : LocalDate.now())
                .description(description)
                .referenceNumber(referenceNumber)
                .lines(new ArrayList<>())
                .build();
        entry.setTenantId(tenantId);
        entry = journalEntryRepository.save(entry);

        for (LineRequest req : lineRequests) {
            Account account = accountRepository.findByCodeAndTenantIdAndDeletedFalse(req.getAccountCode(), tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("Account with code " + req.getAccountCode() + " not found for this tenant"));

            
            BigDecimal amount = req.getAmount();
            if (account.getType() == AccountType.ASSET || account.getType() == AccountType.EXPENSE) {
                if (req.isDebit()) {
                    account.setBalance(account.getBalance().add(amount));
                } else {
                    account.setBalance(account.getBalance().subtract(amount));
                }
            } else { 
                if (req.isDebit()) {
                    account.setBalance(account.getBalance().subtract(amount));
                } else {
                    account.setBalance(account.getBalance().add(amount));
                }
            }
            accountRepository.save(account);

            TransactionLine line = TransactionLine.builder()
                    .journalEntry(entry)
                    .account(account)
                    .amount(amount)
                    .debit(req.isDebit())
                    .build();
            line.setTenantId(tenantId);
            transactionLineRepository.save(line);

            entry.addLine(line);
        }

        return entry;
    }

    

    @Cacheable(value = "chartOfAccounts", key = "#tenantId.toString()")
    public List<Account> getChartOfAccounts(UUID tenantId) {
        return accountRepository.findByTenantIdAndDeletedFalse(tenantId);
    }

    public TrialBalance getTrialBalance(UUID tenantId) {
        List<Account> accounts = accountRepository.findByTenantIdAndDeletedFalse(tenantId);
        List<TrialBalanceEntry> entries = new ArrayList<>();

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (Account acc : accounts) {
            BigDecimal balance = acc.getBalance();
            BigDecimal debitVal = BigDecimal.ZERO;
            BigDecimal creditVal = BigDecimal.ZERO;

            if (acc.getType() == AccountType.ASSET || acc.getType() == AccountType.EXPENSE) {
                if (balance.compareTo(BigDecimal.ZERO) >= 0) {
                    debitVal = balance;
                } else {
                    creditVal = balance.abs();
                }
            } else { 
                if (balance.compareTo(BigDecimal.ZERO) >= 0) {
                    creditVal = balance;
                } else {
                    debitVal = balance.abs();
                }
            }

            totalDebits = totalDebits.add(debitVal);
            totalCredits = totalCredits.add(creditVal);

            entries.add(TrialBalanceEntry.builder()
                    .accountCode(acc.getCode())
                    .accountName(acc.getName())
                    .accountType(acc.getType().name())
                    .debit(debitVal)
                    .credit(creditVal)
                    .build());
        }

        return TrialBalance.builder()
                .entries(entries)
                .totalDebits(totalDebits)
                .totalCredits(totalCredits)
                .build();
    }

    

    public BalanceSheet getBalanceSheet(UUID tenantId) {
        List<Account> accounts = accountRepository.findByTenantIdAndDeletedFalse(tenantId);

        List<BalanceSheetEntry> assets = new ArrayList<>();
        List<BalanceSheetEntry> liabilities = new ArrayList<>();
        List<BalanceSheetEntry> equities = new ArrayList<>();

        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquities = BigDecimal.ZERO;

        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Account acc : accounts) {
            BigDecimal balance = acc.getBalance();
            if (acc.getType() == AccountType.ASSET) {
                totalAssets = totalAssets.add(balance);
                assets.add(new BalanceSheetEntry(acc.getCode(), acc.getName(), balance));
            } else if (acc.getType() == AccountType.LIABILITY) {
                totalLiabilities = totalLiabilities.add(balance);
                liabilities.add(new BalanceSheetEntry(acc.getCode(), acc.getName(), balance));
            } else if (acc.getType() == AccountType.EQUITY) {
                totalEquities = totalEquities.add(balance);
                equities.add(new BalanceSheetEntry(acc.getCode(), acc.getName(), balance));
            } else if (acc.getType() == AccountType.REVENUE) {
                totalRevenue = totalRevenue.add(balance);
            } else if (acc.getType() == AccountType.EXPENSE) {
                totalExpense = totalExpense.add(balance);
            }
        }

        BigDecimal netIncome = totalRevenue.subtract(totalExpense);
        equities.add(new BalanceSheetEntry("3999", "Retained Earnings (Current Period)", netIncome));
        totalEquities = totalEquities.add(netIncome);

        return BalanceSheet.builder()
                .assets(assets)
                .liabilities(liabilities)
                .equities(equities)
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .totalEquities(totalEquities)
                .totalLiabilitiesAndEquities(totalLiabilities.add(totalEquities))
                .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LineRequest {
        private String accountCode;
        private BigDecimal amount;
        private boolean isDebit;
    }

    @Data
    @Builder
    public static class TrialBalance {
        private List<TrialBalanceEntry> entries;
        private BigDecimal totalDebits;
        private BigDecimal totalCredits;
    }

    @Data
    @Builder
    public static class TrialBalanceEntry {
        private String accountCode;
        private String accountName;
        private String accountType;
        private BigDecimal debit;
        private BigDecimal credit;
    }

    @Data
    @Builder
    public static class BalanceSheet {
        private List<BalanceSheetEntry> assets;
        private List<BalanceSheetEntry> liabilities;
        private List<BalanceSheetEntry> equities;
        private BigDecimal totalAssets;
        private BigDecimal totalLiabilities;
        private BigDecimal totalEquities;
        private BigDecimal totalLiabilitiesAndEquities;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BalanceSheetEntry {
        private String accountCode;
        private String accountName;
        private BigDecimal balance;
    }
}
