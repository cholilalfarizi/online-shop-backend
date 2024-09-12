package com.cholildev.online_shop_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.cholildev.online_shop_backend.model.Customers;

public interface CustomersRepository extends JpaRepository<Customers, Long>, JpaSpecificationExecutor<Customers> {
    Boolean existsByCustomerPhone(String phone);
}
