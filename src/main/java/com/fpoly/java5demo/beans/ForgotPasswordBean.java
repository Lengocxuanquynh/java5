package com.fpoly.java5demo.beans;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordBean {

	@NotBlank(message = "Tên đăng nhập không được để trống")
	private String username;

	@NotBlank(message = "Email không được để trống")
	@Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Định dạng email không hợp lệ")
	private String email;

	@NotBlank(message = "Mật khẩu mới không được để trống")
	@Length(min = 6, message = "Mật khẩu mới phải từ 6 ký tự trở lên")
	private String newPassword;

	@NotBlank(message = "Xác nhận mật khẩu mới không được để trống")
	private String confirmPassword;
}
