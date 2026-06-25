package com.gradge.erp.dashboard.service;

import com.gradge.erp.inventory.entity.Product;
import com.gradge.erp.inventory.repository.ProductRepository;
import com.gradge.erp.pos.entity.Invoice;
import com.gradge.erp.pos.enums.InvoiceStatus;
import com.gradge.erp.pos.repository.InvoiceRepository;
import com.gradge.erp.workshop.entity.JobCard;
import com.gradge.erp.workshop.repository.JobCardRepository;
import com.gradge.erp.customer.repository.CustomerRepository;
import com.gradge.erp.dashboard.dto.DashboardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final JobCardRepository jobCardRepository;
    private final CustomerRepository customerRepository;

    public DashboardDTO getDashboard(UUID tenantId) {

        
        
        
        List<Invoice> invoices =
                invoiceRepository.findByTenant_IdAndDeletedFalse(tenantId);

        double totalRevenue = 0;
        double todayRevenue = 0;
        double monthlyRevenue = 0;

        int pending = 0;
        int paid = 0;

        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        for (Invoice inv : invoices) {

            double amount = inv.getTotal() != null ? inv.getTotal().doubleValue() : 0.0;
            totalRevenue += amount;

            if (inv.getStatus() == InvoiceStatus.PAID) {
                paid++;
            } else {
                pending++;
            }

            if (inv.getCreatedAt() != null) {

                LocalDate date = inv.getCreatedAt().toLocalDate();

                if (date.equals(today)) {
                    todayRevenue += amount;
                }

                if (date.getMonthValue() == currentMonth &&
                        date.getYear() == currentYear) {
                    monthlyRevenue += amount;
                }
            }
        }

        
        
        
        List<JobCard> jobs =
                jobCardRepository.findByTenant_IdAndDeletedFalse(tenantId);

        int activeJobs = 0;
        int completedJobs = 0;

        for (JobCard job : jobs) {

            if (job.getStatus() != null &&
                    job.getStatus().name().equals("COMPLETED")) {
                completedJobs++;
            } else {
                activeJobs++;
            }
        }

        
        
        
        List<Product> products =
                productRepository.findByTenant_IdAndDeletedFalse(tenantId);

        int lowStock = 0;

        for (Product p : products) {
            if (p.getQuantity() != null && p.getQuantity() < 5) {
                lowStock++;
            }
        }

        
        
        
        int customers =
                customerRepository.findByTenant_IdAndDeletedFalse(tenantId).size();

        
        
        
        return DashboardDTO.builder()
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .monthlyRevenue(monthlyRevenue)
                .totalInvoices(invoices.size())
                .pendingInvoices(pending)
                .paidInvoices(paid)
                .activeJobCards(activeJobs)
                .completedJobs(completedJobs)
                .totalProducts(products.size())
                .lowStockItems(lowStock)
                .totalCustomers(customers)
                .build();
    }
}
