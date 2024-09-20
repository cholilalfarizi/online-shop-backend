package com.cholildev.online_shop_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSaveRequestDTO {
    private Integer quantity;
    private Long customerId;
    private Long itemId;
}
