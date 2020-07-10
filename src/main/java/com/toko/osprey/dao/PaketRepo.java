package com.toko.osprey.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.toko.osprey.entity.Paket;

public interface PaketRepo extends JpaRepository<Paket, Integer> {
	public Optional<Paket> findByNamaPaket(String namaPaket);
}
