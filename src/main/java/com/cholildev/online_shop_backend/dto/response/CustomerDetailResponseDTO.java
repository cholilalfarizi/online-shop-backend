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
public class CustomerDetailResponseDTO {
    private Long id;
    private String name;
    private String address;
    private String code;
    private String phone;
    private Boolean isActive;
    private Date lastOrderDate;
    private String pic;
}
