package com.gradge.erp.pos.controller;

import com.gradge.erp.common.exception.ResourceNotFoundException;
import com.gradge.erp.common.response.ApiResponse;
import com.gradge.erp.pos.entity.Invoice;
import com.gradge.erp.pos.service.InvoiceService;
import com.gradge.erp.pos.service.InvoicePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('RECORD_TRANSACTIONS')")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoicePdfService invoicePdfService;

    @PostMapping("/{tenantId}")
    public ApiResponse<Invoice> create(
            @PathVariable("tenantId") UUID tenantId,
            @RequestBody Invoice invoice
    ) {
        Invoice saved = invoiceService.createInvoice(invoice, tenantId);
        return ApiResponse.success("Invoice created successfully", saved);
    }

    @GetMapping("/{tenantId}")
    public ApiResponse<List<Invoice>> getAll(@PathVariable("tenantId") UUID tenantId) {
        return ApiResponse.success(invoiceService.getAll(tenantId));
    }

    @GetMapping("/{tenantId}/{id}")
    public ApiResponse<Invoice> get(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        Invoice invoice = invoiceService.getById(id, tenantId);
        return ApiResponse.success(invoice);
    }

    @PutMapping("/{tenantId}/{id}/confirm")
    public ApiResponse<Invoice> confirm(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        Invoice confirmed = invoiceService.confirmInvoice(id, tenantId);
        return ApiResponse.success("Invoice confirmed", confirmed);
    }

    @PutMapping("/{tenantId}/{id}/pay")
    public ApiResponse<Invoice> pay(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id,
            @RequestParam Double amount
    ) {
        Invoice paid = invoiceService.markPaid(id, tenantId, amount);
        return ApiResponse.success("Payment recorded", paid);
    }

    @GetMapping("/{tenantId}/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(
            @PathVariable("tenantId") UUID tenantId,
            @PathVariable("id") UUID id
    ) {
        Invoice invoice = invoiceService.getById(id, tenantId);
        if (invoice == null) {
            throw new ResourceNotFoundException("Invoice not found with id: " + id);
        }
        byte[] pdfBytes = invoicePdfService.generateInvoicePdf(invoice);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=invoice-" + invoice.getInvoiceNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
