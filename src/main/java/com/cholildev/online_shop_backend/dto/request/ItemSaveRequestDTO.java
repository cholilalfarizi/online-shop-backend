package com.cholildev.online_shop_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemSaveRequestDTO {
    private String name;
    private String code;
    private Integer stock;
    private Integer price;
    private Boolean isAvailable;
}
