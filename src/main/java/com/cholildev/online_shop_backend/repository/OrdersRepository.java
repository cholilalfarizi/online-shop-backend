package com.cholildev.online_shop_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.cholildev.online_shop_backend.model.Orders;

public interface OrdersRepository extends JpaRepository<Orders, Long>, JpaSpecificationExecutor<Orders> {
    Boolean existsByOrderCode(String code);
}
