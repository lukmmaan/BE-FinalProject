package com.toko.osprey.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.toko.osprey.entity.Category;

public interface CategoryRepo extends JpaRepository<Category, Integer>{
	public Optional<Category> findByCategoryName(String categoryName);
}
