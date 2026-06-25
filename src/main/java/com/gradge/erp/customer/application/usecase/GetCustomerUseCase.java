package com.gradge.erp.customer.application.usecase;

import com.gradge.erp.customer.entity.Customer;

import java.util.List;
import java.util.UUID;

public interface GetCustomerUseCase {

    Customer getCustomer(UUID id);

    List<Customer> getAllCustomers();
}
