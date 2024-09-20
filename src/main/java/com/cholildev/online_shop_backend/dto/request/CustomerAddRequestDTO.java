package com.cholildev.online_shop_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddRequestDTO {

    @NotBlank
    @Size(min = 1, max = 255, message = "tidak boleh melebihi 255 karakter")
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "tidak boleh berisi special character/angka")
    private String name;

    @NotBlank
    @Size(min = 1, max = 255, message = "tidak boleh melebihi 255 karakter")
    private String address;

    @NotBlank
    @Size(min = 6, max = 13, message = "tidak boleh melebihi 13 karakter")
    @Pattern(regexp = "^[0-9]+$", message = "hanya boleh berisi angka")
    private String phone;
}
