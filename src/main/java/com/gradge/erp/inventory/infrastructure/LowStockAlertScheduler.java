package com.gradge.erp.inventory.infrastructure;

import com.gradge.erp.auth.entity.User;
import com.gradge.erp.auth.enums.UserRole;
import com.gradge.erp.auth.repository.UserRepository;
import com.gradge.erp.inventory.entity.Product;
import com.gradge.erp.inventory.service.ProductService;
import com.gradge.erp.notification.service.NotificationService;
import com.gradge.erp.tenant.context.TenantContext;
import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class LowStockAlertScheduler {

    private final TenantRepository tenantRepository;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    
    @Scheduled(cron = "0 0 8 * * *")
    public void scanAndAlertLowStock() {
        log.info("Starting scheduled low stock scanning process...");
        List<Tenant> tenants = tenantRepository.findAll();

        for (Tenant tenant : tenants) {
            UUID tenantId = tenant.getId();
            try {
                
                TenantContext.setTenantId(tenantId);

                List<Product> lowStockProducts = productService.getLowStockProducts(tenantId);
                if (lowStockProducts.isEmpty()) {
                    continue;
                }

                List<User> users = userRepository.findByTenantId(tenantId);
                List<User> managers = users.stream()
                        .filter(User::isActive)
                        .filter(u -> u.getEmail() != null && !u.getEmail().isBlank())
                        .filter(u -> u.getRole() == UserRole.ADMIN 
                                || u.getRole() == UserRole.MANAGER 
                                || u.getRole() == UserRole.STORE_KEEPER)
                        .toList();

                if (managers.isEmpty()) {
                    log.warn("No active managers with email found for tenant: {}", tenantId);
                    continue;
                }

                
                StringBuilder bodyBuilder = new StringBuilder();
                bodyBuilder.append("Dear team,\n\n");
                bodyBuilder.append("This is an automated notification from Gradge ERP. The following items have reached or dropped below their safety reorder levels:\n\n");

                for (Product p : lowStockProducts) {
                    bodyBuilder.append(String.format("- %s (SKU: %s): Current Stock = %s, Reorder Level = %s\n",
                            p.getName(),
                            p.getSku() != null ? p.getSku() : "N/A",
                            p.getQuantity(),
                            p.getReorderLevel() != null ? p.getReorderLevel() : 0));
                }

                bodyBuilder.append("\nPlease log into your ERP portal to create purchase orders for these items.\n\n");
                bodyBuilder.append("Best regards,\nGradge ERP Notification Engine\n");

                String emailBody = bodyBuilder.toString();
                String subject = "Low Stock Alert: Action Required";

                for (User manager : managers) {
                    notificationService.sendEmail(manager.getEmail(), subject, emailBody);
                }

            } catch (Exception e) {
                log.error("Error processing low-stock alert for tenant {}: {}", tenantId, e.getMessage(), e);
            } finally {
                TenantContext.clear();
            }
        }
        log.info("Low stock scanning process completed.");
    }
}
