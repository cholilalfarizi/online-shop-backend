package com.cholildev.online_shop_backend.specification;

import java.util.List;
import java.util.ArrayList;
import org.springframework.data.jpa.domain.Specification;

import com.cholildev.online_shop_backend.model.Orders;

import jakarta.persistence.criteria.Predicate;

public class OrderSpecification {
    public static Specification<Orders> orderFilter(String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();

            if (keyword != null && !keyword.isEmpty()) {
                String keywordValue = "%" + keyword.toLowerCase() + "%";
                Predicate customerPredicate = cb.like(cb.lower(root.get("customers").get("customerName")),
                        keywordValue);
                Predicate itemPredicate = cb.like(cb.lower(root.get("items").get("itemName")), keywordValue);

                Predicate orPredicate = cb.or(customerPredicate, itemPredicate);
                predicates.add(orPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
