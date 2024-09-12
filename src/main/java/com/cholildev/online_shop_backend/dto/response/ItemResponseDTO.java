package com.cholildev.online_shop_backend.dto.response;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemResponseDTO {
    private Long id;
    private String name;
    private String code;
    private Integer stock;
    private Integer price;
    private Boolean isAvailable;
    private Date lastReStock;
}
