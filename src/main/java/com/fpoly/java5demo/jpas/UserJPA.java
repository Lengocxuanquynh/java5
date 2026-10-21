package com.fpoly.java5demo.jpas;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpoly.java5demo.entities.User;

public interface UserJPA extends JpaRepository<User, Integer> {
	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	Optional<User> findByUsernameAndPassword(String username, String password);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);
}

