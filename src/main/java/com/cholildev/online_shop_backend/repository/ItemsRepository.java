package com.cholildev.online_shop_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.cholildev.online_shop_backend.model.Items;

public interface ItemsRepository extends JpaRepository<Items, Long>, JpaSpecificationExecutor<Items>{
    Boolean existsByItemCode(String code);
}
