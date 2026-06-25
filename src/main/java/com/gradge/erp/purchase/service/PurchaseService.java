package com.gradge.erp.purchase.service;

import com.gradge.erp.inventory.entity.Product;
import com.gradge.erp.inventory.repository.ProductRepository;
import com.gradge.erp.inventory.service.StockService;
import com.gradge.erp.purchase.entity.GoodsReceivedNote;
import com.gradge.erp.purchase.entity.PurchaseOrder;
import com.gradge.erp.purchase.enums.PurchaseOrderStatus;
import com.gradge.erp.purchase.repository.GoodsReceivedNoteRepository;
import com.gradge.erp.purchase.repository.PurchaseOrderRepository;
import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.gradge.erp.finance.service.LedgerService;
import com.gradge.erp.notification.service.NotificationService;
import com.gradge.erp.purchase.entity.PurchaseOrderItem;
import java.math.BigDecimal;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GoodsReceivedNoteRepository grnRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final TenantService tenantService;
    private final LedgerService ledgerService;
    private final NotificationService notificationService;

    public PurchaseOrder createPurchaseOrder(PurchaseOrder order) {
        UUID tenantId = tenantService.getCurrentTenantId();
        order.setTenantId(tenantId);
        order.setStatus(PurchaseOrderStatus.DRAFT);
        
        if (order.getPoNumber() == null || order.getPoNumber().isBlank()) {
            order.setPoNumber("PO-" + System.currentTimeMillis());
        }
        return purchaseOrderRepository.save(order);
    }

    public List<PurchaseOrder> getAllOrders(UUID tenantId) {
        return purchaseOrderRepository.findByTenantIdAndDeletedFalse(tenantId);
    }

    public PurchaseOrder getOrder(UUID id, UUID tenantId) {
        PurchaseOrder order = purchaseOrderRepository.findByIdAndTenantId(id, tenantId);
        if (order == null || order.isDeleted()) {
            throw new RuntimeException("Purchase order not found");
        }
        return order;
    }

    public PurchaseOrder submitOrder(UUID id, UUID tenantId) {
        PurchaseOrder order = getOrder(id, tenantId);
        order.setStatus(PurchaseOrderStatus.SUBMITTED);
        PurchaseOrder saved = purchaseOrderRepository.save(order);

        
        if (saved.getSupplier() != null && saved.getSupplier().getEmail() != null && !saved.getSupplier().getEmail().isBlank()) {
            String supplierEmail = saved.getSupplier().getEmail();
            String subject = "New Purchase Order: " + saved.getPoNumber();
            String body = "Dear " + saved.getSupplier().getName() + ",\n\n" +
                          "We have submitted a new purchase order " + saved.getPoNumber() + " to your company.\n" +
                          "Total Amount: " + saved.getTotalAmount() + "\n\n" +
                          "Please review and process the order.\n\n" +
                          "Best regards,\nGradge ERP Purchasing Department";
            notificationService.sendEmail(supplierEmail, subject, body);
        }

        return saved;
    }

    @Transactional
    public GoodsReceivedNote receiveGoods(UUID purchaseOrderId, UUID productId, Double quantity, String notes, UUID tenantId) {
        PurchaseOrder order = getOrder(purchaseOrderId, tenantId);

        Product product = productRepository.findByIdAndTenant_Id(productId, tenantId);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        Tenant tenant = new Tenant();
        tenant.setId(tenantId);

        
        stockService.addStock(product, quantity, tenant, "GRN-" + purchaseOrderId);

        
        int newQty = (product.getQuantity() != null ? product.getQuantity() : 0) + quantity.intValue();
        product.setQuantity(newQty);
        productRepository.save(product);

        
        order.setStatus(PurchaseOrderStatus.RECEIVED);
        purchaseOrderRepository.save(order);

        
        GoodsReceivedNote grn = GoodsReceivedNote.builder()
                .grnNumber("GRN-" + System.currentTimeMillis())
                .purchaseOrder(order)
                .productId(productId)
                .quantityReceived(quantity)
                .receivedDate(LocalDate.now())
                .notes(notes)
                .stockUpdated(true)
                .build();
        grn.setTenantId(tenantId);
        GoodsReceivedNote savedGrn = grnRepository.save(grn);

        
        BigDecimal unitCost = order.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .map(PurchaseOrderItem::getUnitCost)
                .findFirst()
                .orElse(BigDecimal.ZERO);
        BigDecimal totalCost = unitCost.multiply(BigDecimal.valueOf(quantity));

        
        if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
            ledgerService.recordTransaction(
                    tenantId,
                    LocalDate.now(),
                    "Goods Received Note - GRN for Product: " + product.getName() + " on PO: " + order.getPoNumber(),
                    savedGrn.getGrnNumber(),
                    Arrays.asList(
                            new LedgerService.LineRequest("1400", totalCost, true),
                            new LedgerService.LineRequest("2000", totalCost, false)
                    )
            );
        }

        return savedGrn;
    }

    public List<GoodsReceivedNote> getGRNsForOrder(UUID purchaseOrderId) {
        return grnRepository.findByPurchaseOrder_IdAndDeletedFalse(purchaseOrderId);
    }

    public List<GoodsReceivedNote> getAllGRNs(UUID tenantId) {
        return grnRepository.findByTenantIdAndDeletedFalse(tenantId);
    }
}
