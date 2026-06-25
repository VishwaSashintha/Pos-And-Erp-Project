package com.gradge.erp.workshop.service;

import com.gradge.erp.inventory.service.StockService;
import com.gradge.erp.notification.events.SystemEvent;
import com.gradge.erp.notification.events.SystemEventType;
import com.gradge.erp.notification.events.EventPublisherService;
import com.gradge.erp.pos.entity.Invoice;
import com.gradge.erp.pos.enums.InvoiceStatus;
import com.gradge.erp.pos.repository.InvoiceRepository;
import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.workshop.entity.JobCard;
import com.gradge.erp.workshop.repository.JobCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobCardService {

    private final JobCardRepository jobCardRepository;
    private final InvoiceRepository invoiceRepository;
    private final StockService stockService;
    private final EventPublisherService eventPublisherService;

    public JobCard createJobCard(JobCard jobCard) {
        if (jobCard.getJobNumber() == null || jobCard.getJobNumber().isEmpty()) {
            jobCard.setJobNumber("JOB-" + System.currentTimeMillis());
        }
        return jobCardRepository.save(jobCard);
    }

    public List<JobCard> getAllJobCards(UUID tenantId) {
        return jobCardRepository.findByTenant_IdAndDeletedFalse(tenantId);
    }

    public JobCard getJobCard(UUID id, UUID tenantId) {
        return jobCardRepository.findByIdAndTenant_Id(id, tenantId);
    }

    public JobCard updateJobCard(UUID id, JobCard updated, UUID tenantId) {
        JobCard existing = getJobCard(id, tenantId);

        boolean statusChanged = existing.getStatus() != updated.getStatus();

        existing.setVehicleNumber(updated.getVehicleNumber());
        existing.setStatus(updated.getStatus());
        existing.setLaborCost(updated.getLaborCost());
        existing.setPartsCost(updated.getPartsCost());
        existing.setTotalCost(updated.getTotalCost());

        JobCard saved = jobCardRepository.save(existing);

        // Fire WebSocket event when status changes
        if (statusChanged && updated.getStatus() != null) {
            eventPublisherService.publish(
                    SystemEvent.builder()
                            .type(SystemEventType.JOB_UPDATED)
                            .tenantId(tenantId)
                            .message("Job Card " + saved.getJobNumber()
                                    + " status changed to " + updated.getStatus().name())
                            .data(saved.getId())
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }

        return saved;
    }


    public void deleteJobCard(UUID id, UUID tenantId) {
        JobCard existing = getJobCard(id, tenantId);
        existing.setDeleted(true);
        jobCardRepository.save(existing);
    }

    public Invoice generateInvoiceFromJobCard(UUID jobCardId, Tenant tenant) {

        JobCard jobCard = jobCardRepository.findById(jobCardId)
                .orElseThrow(() -> new RuntimeException("JobCard not found"));

        if (jobCard.isInvoiceGenerated()) {
            throw new RuntimeException("Invoice already generated for this JobCard");
        }

        Invoice invoice = new Invoice();

        invoice.setCustomer(jobCard.getCustomer());
        invoice.setJobCard(jobCard);
        invoice.setTenant(tenant);
        invoice.setStatus(InvoiceStatus.DRAFT);

        BigDecimal subTotal = BigDecimal.ZERO;

        
        if (jobCard.getLaborCost() != null) {
            subTotal = subTotal.add(BigDecimal.valueOf(jobCard.getLaborCost()));
        }

        
        if (jobCard.getPartsCost() != null && jobCard.getPartsCost() > 0) {

            subTotal = subTotal.add(BigDecimal.valueOf(jobCard.getPartsCost()));

            stockService.reduceStock(
                    null,
                    jobCard.getPartsCost(),
                    tenant,
                    "JOBCARD-" + jobCard.getJobNumber()
            );
        }

        invoice.setSubTotal(subTotal);
        invoice.setDiscount(BigDecimal.ZERO);

        BigDecimal tax = subTotal.multiply(BigDecimal.valueOf(0.15));
        invoice.setTax(tax);

        BigDecimal total = subTotal.add(tax);

        invoice.setTotal(total);
        invoice.setBalance(total);
        invoice.setPaidAmount(BigDecimal.ZERO);

        Invoice saved = invoiceRepository.save(invoice);

        jobCard.setInvoice(saved);
        jobCard.setInvoiceGenerated(true);

        jobCardRepository.save(jobCard);

        eventPublisherService.publish(
                SystemEvent.builder()
                        .type(SystemEventType.INVOICE_CREATED)
                        .tenantId(tenant.getId())
                        .message("Invoice created from JobCard " + jobCard.getJobNumber())
                        .data(saved)
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        return saved;
    }
}
