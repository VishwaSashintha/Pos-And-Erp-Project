package com.gradge.erp.supplier.service;

import com.gradge.erp.supplier.entity.Supplier;
import com.gradge.erp.supplier.repository.SupplierRepository;
import com.gradge.erp.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final TenantService tenantService;

    public Supplier createSupplier(Supplier supplier) {
        UUID tenantId = tenantService.getCurrentTenantId();
        supplier.setTenantId(tenantId);
        return supplierRepository.save(supplier);
    }

    public List<Supplier> getAllSuppliers(UUID tenantId) {
        return supplierRepository.findByTenantIdAndDeletedFalse(tenantId);
    }

    public Supplier getSupplier(UUID id, UUID tenantId) {
        Supplier supplier = supplierRepository.findByIdAndTenantId(id, tenantId);
        if (supplier == null || supplier.isDeleted()) {
            throw new RuntimeException("Supplier not found");
        }
        return supplier;
    }

    public Supplier updateSupplier(UUID id, Supplier updated, UUID tenantId) {
        Supplier existing = getSupplier(id, tenantId);
        existing.setName(updated.getName());
        existing.setContactName(updated.getContactName());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setAddress(updated.getAddress());
        existing.setTaxNumber(updated.getTaxNumber());
        return supplierRepository.save(existing);
    }

    public void deleteSupplier(UUID id, UUID tenantId) {
        Supplier supplier = getSupplier(id, tenantId);
        supplier.setDeleted(true);
        supplierRepository.save(supplier);
    }

    public List<Supplier> searchSuppliers(UUID tenantId, String name) {
        return supplierRepository.findByTenantIdAndDeletedFalseAndNameContainingIgnoreCase(tenantId, name);
    }
}
