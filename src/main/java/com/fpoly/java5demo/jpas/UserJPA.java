package com.fpoly.java5demo.jpas;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fpoly.java5demo.entities.User;

public interface UserJPA extends JpaRepository<User, Integer> {

	@Query(value = "SELECT * FROM users WHERE username = ?1", nativeQuery = true)
	Optional<User> findByUsername(String username);

	@Query(value = "SELECT * FROM users WHERE email = ?1", nativeQuery = true)
	Optional<User> findByEmail(String email);

	@Query(value = "SELECT * FROM users WHERE username = ?1 AND password = ?2", nativeQuery = true)
	Optional<User> findByUsernameAndPassword(String username, String password);

	@Query(value = "SELECT COUNT(*) FROM users WHERE username = ?1", nativeQuery = true)
	int countByUsername(String username);

	@Query(value = "SELECT COUNT(*) FROM users WHERE email = ?1", nativeQuery = true)
	int countByEmail(String email);

	@Query(value = "SELECT COUNT(*) FROM users WHERE phone = ?1", nativeQuery = true)
	int countByPhone(String phone);

	//  Tìm user theo SĐT
	@Query(value = "SELECT * FROM users WHERE phone = ?1", nativeQuery = true)
	Optional<User> findByPhone(String phone);

	@Query(value = "SELECT * FROM users", nativeQuery = true)
	List<User> findAllUsers();
}

