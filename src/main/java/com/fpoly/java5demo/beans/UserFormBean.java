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
public class UserFormBean {
	private Integer id;

	@NotBlank(message = "Tên đăng nhập không được để trống")
	private String username;

	@NotBlank(message = "Mật khẩu không được để trống")
	@Length(min = 6, message = "Mật khẩu phải từ 6 ký tự trở lên")
	private String password = "";

	@NotBlank(message = "Họ và tên không được để trống")
	private String name;

	@NotBlank(message = "Email không được để trống")
	@Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Định dạng email không hợp lệ")
	private String email;

	@NotBlank(message = "Số điện thoại không được để trống")
	@Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không hợp lệ (VD: 0987654321)")
	private String phone;

	private String address; // Địa chỉ mặc định (không bắt buộc)

	private int role = 0; // 0: Khách hàng, 1: Quản trị viên
	private boolean active = true;
}
