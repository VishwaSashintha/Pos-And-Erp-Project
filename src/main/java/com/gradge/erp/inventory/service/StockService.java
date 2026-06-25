package com.gradge.erp.inventory.service;

import com.gradge.erp.inventory.entity.*;
import com.gradge.erp.inventory.enums.StockMovementType;
import com.gradge.erp.inventory.repository.StockMovementRepository;
import com.gradge.erp.inventory.repository.StockRepository;
import com.gradge.erp.notification.events.EventPublisherService;
import com.gradge.erp.notification.events.SystemEvent;
import com.gradge.erp.notification.events.SystemEventType;
import com.gradge.erp.tenant.entity.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockMovementRepository movementRepository;
    private final EventPublisherService eventPublisherService;

    public void addStock(Product product, Double qty, Tenant tenant, String ref) {
        addStock(product, null, qty, tenant, ref);
    }

    public void addStock(Product product, Warehouse warehouse, Double qty, Tenant tenant, String ref) {
        Stock stock = getOrCreateStock(product, warehouse, tenant);

        stock.setQuantity(stock.getQuantity() + qty);
        stockRepository.save(stock);

        movementRepository.save(
                StockMovement.builder()
                        .product(product)
                        .quantity(qty)
                        .type(StockMovementType.IN)
                        .reference(ref)
                        .tenant(tenant)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    public void reduceStock(Product product, Double qty, Tenant tenant, String ref) {
        reduceStock(product, null, qty, tenant, ref);
    }

    public void reduceStock(Product product, Warehouse warehouse, Double qty, Tenant tenant, String ref) {
        Stock stock = getOrCreateStock(product, warehouse, tenant);

        stock.setQuantity(stock.getQuantity() - qty);
        stockRepository.save(stock);

        movementRepository.save(
                StockMovement.builder()
                        .product(product)
                        .quantity(qty)
                        .type(StockMovementType.OUT)
                        .reference(ref)
                        .tenant(tenant)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // ── Low-stock alert ──────────────────────────────────────────────────────
        Double totalStock = getStock(product, tenant); // check overall stock
        if (product != null && product.getReorderLevel() != null
                && totalStock <= product.getReorderLevel()) {
            log.warn("LOW STOCK: product='{}' qty={} reorderLevel={}",
                    product.getName(), totalStock, product.getReorderLevel());
            eventPublisherService.publish(SystemEvent.builder()
                    .type(SystemEventType.STOCK_LOW)
                    .tenantId(tenant.getId())
                    .message("Low stock alert: '" + product.getName()
                            + "' has only " + totalStock + " units left across all warehouses"
                            + " (reorder level: " + product.getReorderLevel() + ")")
                    .data(product.getId())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    public Double getStock(Product product, Tenant tenant) {
        List<Stock> stocks = stockRepository.findByProduct_IdAndTenant_Id(product.getId(), tenant.getId());
        return stocks.stream().mapToDouble(s -> s.getQuantity() != null ? s.getQuantity() : 0.0).sum();
    }

    public Double getStockInWarehouse(Product product, Warehouse warehouse, Tenant tenant) {
        return getOrCreateStock(product, warehouse, tenant).getQuantity();
    }

    public List<StockMovement> getMovements(UUID tenantId) {
        return movementRepository.findByTenant_Id(tenantId);
    }

    public List<StockMovement> getMovementsForProduct(UUID productId, UUID tenantId) {
        return movementRepository.findByProduct_IdAndTenant_Id(productId, tenantId);
    }

    private Stock getOrCreateStock(Product product, Warehouse warehouse, Tenant tenant) {
        if (warehouse != null) {
            return stockRepository.findByProduct_IdAndWarehouse_IdAndTenant_Id(product.getId(), warehouse.getId(), tenant.getId())
                    .orElse(Stock.builder()
                            .product(product)
                            .warehouse(warehouse)
                            .tenant(tenant)
                            .quantity(0.0)
                            .build());
        } else {
            return stockRepository.findByProduct_IdAndWarehouseIsNullAndTenant_Id(product.getId(), tenant.getId())
                    .orElse(Stock.builder()
                            .product(product)
                            .warehouse(null)
                            .tenant(tenant)
                            .quantity(0.0)
                            .build());
        }
    }
}
