package com.fpoly.java5demo.entities;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "products")
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private int id;

	@Column(name = "name", nullable = false, columnDefinition = "NVARCHAR(200)")
	private String name;

	@Column(name = "price", nullable = false)
	private int price;

	@Column(name = "discount", nullable = false)
	private int discount = 0; // % giảm giá (0 - 100)

	@Column(name = "quantity", nullable = false)
	private int quantity;

	@Column(name = "status", nullable = false)
	private boolean status;

	@Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
	private String description;

	@Column(name = "detail", columnDefinition = "NVARCHAR(MAX)")
	private String detail;

	@ManyToOne
	@JoinColumn(name = "cat_id", nullable = false)
	private Category category;

	@OneToMany(mappedBy = "product")
	List<Image> images;

	@OneToMany(mappedBy = "product")
	List<CartDetail> cartDetails;

	public int getSalePrice() {
		if (discount > 0 && discount <= 100) {
			return (int) Math.round(price * (100.0 - discount) / 100.0);
		}
		return price;
	}

	public Product(int id, String name, int price, int quantity, boolean status, Category category, List<Image> images, List<CartDetail> cartDetails) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.discount = 0;
		this.quantity = quantity;
		this.status = status;
		this.category = category;
		this.images = images;
		this.cartDetails = cartDetails;
	}

	public Product(int id, String name, int price, int discount, int quantity, boolean status, Category category, List<Image> images, List<CartDetail> cartDetails) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.discount = discount;
		this.quantity = quantity;
		this.status = status;
		this.category = category;
		this.images = images;
		this.cartDetails = cartDetails;
	}
}
