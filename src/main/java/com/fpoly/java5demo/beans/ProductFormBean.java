package com.fpoly.java5demo.beans;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductFormBean {
	private Integer id;

	@NotBlank(message = "Tên sản phẩm không được để trống")
	private String name;

	public void setName(String name) {
		this.name = (name != null) ? name.trim() : null;
	}

	@NotNull(message = "Vui lòng nhập giá niêm yết")
	@Min(value = 1000, message = "Giá sản phẩm phải từ 1.000 VNĐ trở lên")
	private Integer price;

	@NotNull(message = "Vui lòng nhập số lượng trong kho")
	@Min(value = 0, message = "Số lượng không được âm")
	private Integer quantity;

	@NotNull(message = "Vui lòng chọn danh mục")
	@Min(value = 1, message = "Vui lòng chọn danh mục hợp lệ")
	private Integer category;

	private Integer status = 1; // 1: Hiển thị, 2: Ẩn

	private String description;

	private String detail;

	private List<MultipartFile> images;
}

