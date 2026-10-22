package com.fpoly.java5demo.beans;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfileBean {
	private Integer id;
	private String username;

	@NotBlank(message = "Họ và tên không được để trống")
	private String name;

	@NotBlank(message = "Email không được để trống")
	@Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email không đúng định dạng")
	private String email;

	@NotBlank(message = "Số điện thoại không được để trống")
	@Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không hợp lệ (VD: 0987654321)")
	private String phone;

	private String address; // Địa chỉ mặc định (không bắt buộc)
}
