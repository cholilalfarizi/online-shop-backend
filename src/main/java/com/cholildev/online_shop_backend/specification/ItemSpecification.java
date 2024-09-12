package com.cholildev.online_shop_backend.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.cholildev.online_shop_backend.dto.request.ItemListRequestDTO;
import com.cholildev.online_shop_backend.model.Items;

import jakarta.persistence.criteria.Predicate;

public class ItemSpecification {
    public static Specification<Items> itemFilter(ItemListRequestDTO request){
        return(root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();

            if (request.getName() != null && !request.getName().isEmpty()) {
                String nameValue = "%" + request.getName().toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("customerName")), nameValue);
                predicates.add(namePredicate);
            }

            if (request.getIsAvailable() != null) {
                Predicate isActivePredicate = cb.equal(root.get("isAvailable"), request.getIsAvailable());
                predicates.add(isActivePredicate);
            }

            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
