package com.gradge.erp.pos;

import com.gradge.erp.common.event.EventPublisher;
import com.gradge.erp.finance.entity.Account;
import com.gradge.erp.finance.entity.JournalEntry;
import com.gradge.erp.finance.repository.AccountRepository;
import com.gradge.erp.finance.repository.JournalEntryRepository;
import com.gradge.erp.pos.entity.Invoice;
import com.gradge.erp.pos.entity.InvoiceItem;
import com.gradge.erp.pos.enums.InvoiceStatus;
import com.gradge.erp.pos.repository.InvoiceRepository;
import com.gradge.erp.pos.service.InvoiceService;
import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.tenant.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InvoiceConfirmationIntegrationTest {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private AccountRepository accountRepository;

    @MockitoBean
    private EventPublisher eventPublisher;

    private Tenant tenant;
    private Invoice testInvoice;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setName("Test Garage");
        tenant = tenantRepository.save(tenant);

        com.gradge.erp.tenant.context.TenantContext.setTenantId(tenant.getId());

        // Setup base accounts used by LedgerService
        Account cashAccount = new Account();
        cashAccount.setTenantId(tenant.getId());
        cashAccount.setCode("1000");
        cashAccount.setName("Cash");
        cashAccount.setType(com.gradge.erp.finance.enums.AccountType.ASSET);

        Account arAccount = new Account();
        arAccount.setTenantId(tenant.getId());
        arAccount.setCode("1200");
        arAccount.setName("Accounts Receivable");
        arAccount.setType(com.gradge.erp.finance.enums.AccountType.ASSET);

        accountRepository.saveAll(List.of(cashAccount, arAccount));

        // Stub event publisher
        doNothing().when(eventPublisher).publishPosSaleCreated(any());

        testInvoice = new Invoice();
        testInvoice.setTenant(tenant);
        testInvoice.setTenantId(tenant.getId());
        testInvoice.setInvoiceNumber("INV-100");
        testInvoice.setStatus(InvoiceStatus.DRAFT);
        testInvoice.setTotal(new BigDecimal("500.00"));
        testInvoice.setPaidAmount(BigDecimal.ZERO);

        InvoiceItem item = new InvoiceItem();
        item.setInvoice(testInvoice);
        item.setProductName("Oil Filter");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("50.00"));

        testInvoice.setItems(new java.util.ArrayList<>(List.of(item)));

        testInvoice = invoiceRepository.save(testInvoice);
    }

    @Test
    void confirmInvoice_changesStatus() {
        Invoice confirmed = invoiceService.confirmInvoice(testInvoice.getId(), tenant.getId());

        assertThat(confirmed.getStatus()).isEqualTo(InvoiceStatus.CONFIRMED);
    }

    @Test
    void markPaid_createsLedgerEntry() {
        // Confirm first
        invoiceService.confirmInvoice(testInvoice.getId(), tenant.getId());

        // Mark paid
        invoiceService.markPaid(testInvoice.getId(), tenant.getId(), 500.00);

        List<JournalEntry> journals = journalEntryRepository.findByTenantIdAndDeletedFalse(tenant.getId());
        
        assertThat(journals).hasSize(1);
        JournalEntry entry = journals.get(0);
        assertThat(entry.getReferenceNumber()).isEqualTo("INV-100");
        
        // One debit, one credit line
        assertThat(entry.getLines()).hasSize(2);
    }
}
