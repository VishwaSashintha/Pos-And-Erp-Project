package com.gradge.erp.customer.service;

import com.gradge.erp.common.audit.Auditable;
import com.gradge.erp.customer.entity.Customer;
import com.gradge.erp.customer.repository.CustomerRepository;
import com.gradge.erp.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final TenantService tenantService;

    @Auditable(action = "CUSTOMER_CREATED")
    public Customer createCustomer(Customer customer) {
        UUID tenantId = tenantService.getCurrentTenantId();
        customer.setTenantId(tenantId);
        customer.setVisitCount(0);
        customer.setTotalSpent(0.0);
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers(UUID tenantId) {
        return customerRepository.findByTenant_IdAndDeletedFalse(tenantId);
    }

    public Customer getCustomer(UUID id, UUID tenantId) {
        Customer customer = customerRepository.findByIdAndTenant_Id(id, tenantId);
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }
        return customer;
    }

    @Auditable(action = "CUSTOMER_UPDATED")
    public Customer updateCustomer(UUID id, Customer updated, UUID tenantId) {
        Customer existing = getCustomer(id, tenantId);
        existing.setName(updated.getName());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setAddress(updated.getAddress());
        existing.setNic(updated.getNic());
        return customerRepository.save(existing);
    }

    @Auditable(action = "CUSTOMER_DELETED")
    public void deleteCustomer(UUID id, UUID tenantId) {
        Customer customer = getCustomer(id, tenantId);
        customer.setDeleted(true);
        customerRepository.save(customer);
    }
}