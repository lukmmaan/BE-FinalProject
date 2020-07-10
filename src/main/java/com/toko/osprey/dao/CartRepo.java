package com.toko.osprey.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.toko.osprey.entity.Cart;

public interface CartRepo extends JpaRepository<Cart, Integer>{
	//search cart product untuk mengecek quantity
	@Query(value = "SELECT * FROM cart WHERE user_id= ?1 and product_id= ?2",nativeQuery = true)
	public Iterable<Cart> findByUserIdAndProduct(int userId, int productId);
	@Query(value = "SELECT * FROM cart WHERE user_id= ?1 and paket_id= ?2",nativeQuery = true)
	public Iterable<Cart> findByUserIdAndPaket(int userId, int paketId);
	
	//get all cart by user
	@Query(value = "SELECT * FROM cart WHERE user_id= ?1",nativeQuery = true)
	public Iterable<Cart> findByUserId(int userId);
}
