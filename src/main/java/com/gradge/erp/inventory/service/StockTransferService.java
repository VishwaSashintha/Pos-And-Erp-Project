package com.gradge.erp.inventory.service;

import com.gradge.erp.inventory.entity.*;
import com.gradge.erp.inventory.enums.TransferStatus;
import com.gradge.erp.inventory.repository.StockTransferRepository;
import com.gradge.erp.inventory.repository.WarehouseRepository;
import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.common.audit.Auditable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockService stockService;

    @Transactional
    public StockTransfer createTransfer(UUID sourceWarehouseId, UUID destinationWarehouseId, String referenceNumber, String notes, Tenant tenant) {
        Warehouse source = warehouseRepository.findById(sourceWarehouseId)
                .orElseThrow(() -> new RuntimeException("Source warehouse not found"));
        Warehouse destination = warehouseRepository.findById(destinationWarehouseId)
                .orElseThrow(() -> new RuntimeException("Destination warehouse not found"));

        if (source.getId().equals(destination.getId())) {
            throw new RuntimeException("Source and destination warehouses cannot be the same");
        }

        StockTransfer transfer = StockTransfer.builder()
                .sourceWarehouse(source)
                .destinationWarehouse(destination)
                .referenceNumber(referenceNumber)
                .transferDate(LocalDateTime.now())
                .status(TransferStatus.DRAFT)
                .notes(notes)
                .build();
        transfer.setTenantId(tenant.getId());
        
        return stockTransferRepository.save(transfer);
    }

    @Auditable(action = "RECEIVE_STOCK_TRANSFER")
    @Transactional
    public StockTransfer receiveTransfer(UUID transferId, String receiveNotes) {
        StockTransfer transfer = stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Stock transfer not found"));
        
        if (transfer.getStatus() != TransferStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT transfers can be executed");
        }
        return null;
    }

    @Transactional
    public void executeTransfer(UUID transferId, Tenant tenant) {
        StockTransfer transfer = stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Stock transfer not found"));
        
        if (transfer.getStatus() != TransferStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT transfers can be executed");
        }

        for (StockTransferItem item : transfer.getItems()) {
            Double availableStock = stockService.getStockInWarehouse(item.getProduct(), transfer.getSourceWarehouse(), tenant);
            if (availableStock < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock in source warehouse for product: " + item.getProduct().getName());
            }

            stockService.reduceStock(item.getProduct(), transfer.getSourceWarehouse(), item.getQuantity(), tenant, "TRANSFER-OUT: " + transfer.getReferenceNumber());
            stockService.addStock(item.getProduct(), transfer.getDestinationWarehouse(), item.getQuantity(), tenant, "TRANSFER-IN: " + transfer.getReferenceNumber());
        }

        transfer.setStatus(TransferStatus.COMPLETED);
        stockTransferRepository.save(transfer);
    }
}
