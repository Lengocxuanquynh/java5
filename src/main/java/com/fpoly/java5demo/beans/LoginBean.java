package com.fpoly.java5demo.beans;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginBean {
	@NotBlank(message = "Tên đăng nhập không được để trống")
	private String username;

	@NotBlank(message = "Mật khẩu không được để trống")
	private String password;

}
