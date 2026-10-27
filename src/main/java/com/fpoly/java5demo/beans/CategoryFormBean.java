package com.fpoly.java5demo.beans;

import java.util.Optional;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CategoryFormBean {
	private Optional<Integer> id;

	@NotBlank(message = "Tên danh mục không được để trống")
	private String name;
}
