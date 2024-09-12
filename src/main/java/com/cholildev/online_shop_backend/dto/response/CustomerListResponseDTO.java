package com.cholildev.online_shop_backend.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerListResponseDTO {
    private Long id;
    private String name;
    private Boolean isActive;
    private String pic;
}
