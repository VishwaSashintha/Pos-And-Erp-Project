package com.gradge.erp.pos.repository;

import com.gradge.erp.pos.entity.Invoice;
import com.gradge.erp.pos.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findByTenant_IdAndDeletedFalse(UUID tenantId);

    Invoice findByIdAndTenant_Id(UUID id, UUID tenantId);

    Invoice findByInvoiceNumberAndTenant_Id(String invoiceNumber, UUID tenantId);

    List<Invoice> findByStatusAndTenant_Id(InvoiceStatus status, UUID tenantId);

    List<Invoice> findByTenant_IdAndDeletedFalseAndCreatedAtBetween(
            UUID tenantId,
            java.time.LocalDateTime start,
            java.time.LocalDateTime end
    );
}

