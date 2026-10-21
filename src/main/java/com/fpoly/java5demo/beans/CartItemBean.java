package com.fpoly.java5demo.beans;

import com.fpoly.java5demo.entities.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemBean {
	private Product product;
	private int quantity;

	public int getSubTotal() {
		if (product == null) {
			return 0;
		}
		return product.getSalePrice() * quantity;
	}
}
