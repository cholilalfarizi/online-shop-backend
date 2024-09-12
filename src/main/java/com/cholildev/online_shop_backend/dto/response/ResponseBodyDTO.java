package com.cholildev.online_shop_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseBodyDTO {
	private String status;
	private int code;
	private String message;
	private Object data;
}