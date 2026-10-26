package com.fpoly.java5demo.beans;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderBean {

	@NotBlank(message = "Vui lòng nhập lý do hủy đơn hàng!")
	private String reason;
}
