package com.toko.osprey.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.toko.osprey.entity.User;

public interface UserRepo extends JpaRepository<User, Integer>{

	public Optional<User> findByUsername(String username);
	public Optional<User> findByEmail(String email);
	public Optional<User> findByPassword(String password);
}
