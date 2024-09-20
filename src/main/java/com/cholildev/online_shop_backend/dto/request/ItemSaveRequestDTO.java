package com.cholildev.online_shop_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSaveRequestDTO {

    @NotBlank
    @Size(min = 1, max = 255, message = "tidak boleh melebihi 255 karakter")
    private String name;

    @NotNull
    private Integer stock;

    @NotNull
    private Integer price;

    @Builder.Default
    private boolean isAvailable = true;
}
