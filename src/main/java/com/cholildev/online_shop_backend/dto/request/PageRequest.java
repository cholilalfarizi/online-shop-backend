package com.cholildev.online_shop_backend.dto.request;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageRequest {
    private String sortBy;
    private Integer pageSize;
    private Integer pageNumber;

    public Pageable getPage(String sortByReq){
        int pageNumberValue = (pageNumber != null) ? pageNumber < 1 ? 1 : pageNumber : 1;
        int pageSizeValue = (pageSize != null) ? pageSize < 1 ? 1 : pageSize : 10;
        Sort sort = Sort.by(Direction.ASC, sortByReq);

        if (sortBy != null && !sortBy.isEmpty()) {
            String[] parts = sortBy.split(",");
            String sortField = parts[0];
            String sortOrder = parts.length > 1 ? parts[1] : "ASC";
            sort = Sort.by(Sort.Direction.fromString(sortOrder), sortField);
        }

        return org.springframework.data.domain.PageRequest.of(pageNumberValue -1, pageSizeValue, sort);
    }
}
