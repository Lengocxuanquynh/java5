package com.fpoly.java5demo.entities;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = { "orderDetails" })
@EqualsAndHashCode(exclude = { "orderDetails" })
@Entity
@Table(name = "orders")
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "shipping_name", nullable = false, columnDefinition = "NVARCHAR(100)")
	private String shippingName;

	@Column(name = "shipping_phone", nullable = false, length = 20)
	private String shippingPhone;

	@Column(name = "shipping_address", nullable = false, columnDefinition = "NVARCHAR(255)")
	private String shippingAddress;

	@Column(name = "note", columnDefinition = "NVARCHAR(500)")
	private String note;

	@Column(name = "total_price", nullable = false)
	private int totalPrice;

	@Column(name = "date", nullable = false)
	private Date date;

	@Column(name = "status", nullable = false)
	private int status = 1; // 1: Chờ xác nhận, 2: Đã xác nhận, 3: Đang giao, 4: Đã giao, 5: Đã nhận, 6: Hủy

	@Column(name = "updated_at")
	private Timestamp updatedAt;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
	List<OrderDetail> orderDetails;

	@PrePersist
	public void prePersist() {
		if (this.updatedAt == null) {
			this.updatedAt = new Timestamp(System.currentTimeMillis());
		}
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = new Timestamp(System.currentTimeMillis());
	}
}
