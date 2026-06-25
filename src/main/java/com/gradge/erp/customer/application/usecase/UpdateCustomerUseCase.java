package com.gradge.erp.customer.application.usecase;

import com.gradge.erp.customer.entity.Customer;

import java.util.UUID;

public interface UpdateCustomerUseCase {

    Customer updateCustomer(UUID id, Customer customer);
}
