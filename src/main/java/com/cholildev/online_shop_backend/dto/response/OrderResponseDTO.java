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
public class OrderResponseDTO {
    private Long orderId;
    private String orderCode;
    private Date orderDate;
    private Integer totalPrice;
    private Integer quantity;
    private CustomerDTO customers;
    private ItemDTO item;
}
