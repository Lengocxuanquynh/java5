package com.fpoly.java5demo.beans;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegisterBean {
	@NotBlank(message = "Tên đăng nhập không được để trống")
	@Length(min = 4, max = 30, message = "Tên đăng nhập phải từ 4 đến 30 ký tự")
	private String username;

	@NotBlank(message = "Mật khẩu không được để trống")
	@Length(min = 6, message = "Mật khẩu phải từ 6 ký tự trở lên")
	private String password;

	@NotBlank(message = "Email không được để trống")
	@Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email không đúng định dạng")
	private String email;

	@NotBlank(message = "Họ và tên không được để trống")
	private String name;

	@NotBlank(message = "Số điện thoại không được để trống")
	@Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không hợp lệ (VD: 0987654321)")
	private String phone;
}
