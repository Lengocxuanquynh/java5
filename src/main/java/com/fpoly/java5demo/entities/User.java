package com.fpoly.java5demo.entities;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private int id;

	@Column(name = "username", nullable = false, length = 100)
	private String username;

	@Column(name = "password", nullable = false, length = 100)
	private String password;

	@Column(name = "name", nullable = false, columnDefinition = "NVARCHAR(200)")
	private String name;

	@Column(name = "email", nullable = false, length = 100)
	private String email;

	@Column(name = "phone", length = 20)
	private String phone;

	@Column(name = "status", nullable = false)
	private boolean status = true;

	@Column(name = "role", nullable = false)
	private int role = 0;

	@OneToMany(mappedBy = "user")
	List<CartDetail> cartDetails;

//	User == 0
}
