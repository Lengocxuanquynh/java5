package com.fpoly.java5demo.beans;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutBean {
	@NotBlank(message = "Họ và tên người nhận không được để trống")
	private String fullName;

	@NotBlank(message = "Số điện thoại không được để trống")
	@Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không hợp lệ")
	private String phone;

	@NotBlank(message = "Địa chỉ nhận hàng không được để trống")
	private String address;

	private String note;

	private String paymentMethod = "COD"; // COD, ShopeePay, BankTransfer
}
