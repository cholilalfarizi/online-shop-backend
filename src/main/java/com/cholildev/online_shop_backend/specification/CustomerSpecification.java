package com.cholildev.online_shop_backend.specification;

import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import java.util.ArrayList;
import com.cholildev.online_shop_backend.dto.request.CustomerListRequestDTO;
import com.cholildev.online_shop_backend.model.Customers;

import jakarta.persistence.criteria.Predicate;

public class CustomerSpecification {
    public static Specification<Customers> customerFilter(CustomerListRequestDTO request){
        return(root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();

            if (request.getName() != null && !request.getName().isEmpty()) {
                String nameValue = "%" + request.getName().toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("customerName")), nameValue);
                predicates.add(namePredicate);
            }

            if (request.getIsActive() != null) {
                Predicate isActivePredicate = cb.equal(root.get("isActive"), request.getIsActive());
                predicates.add(isActivePredicate);
            }

            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
