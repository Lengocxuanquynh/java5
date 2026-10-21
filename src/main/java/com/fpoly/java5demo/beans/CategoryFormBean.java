package com.fpoly.java5demo.beans;

import java.util.Optional;

import com.fpoly.java5demo.entities.Category;

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

	// Tự động trim ngay khi form submit nhận dữ liệu
	// public void setName(String name) {
	// 	this.name = (name != null) ? name.trim() : null;
	// }

	public Category convertBeanToEntity() {
		Category category = new Category();
		category.setName(this.getName());
		if (id != null && id.isPresent()) {
			category.setId(id.get());
		}
		return category;
	}
}
