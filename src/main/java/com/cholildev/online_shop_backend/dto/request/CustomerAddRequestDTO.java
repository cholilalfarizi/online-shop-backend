package com.cholildev.online_shop_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddRequestDTO {
    private String name;
    private String address;
    private String code;
    private String phone;
}
