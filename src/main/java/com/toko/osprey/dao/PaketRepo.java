package com.toko.osprey.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.toko.osprey.entity.Paket;

public interface PaketRepo extends JpaRepository<Paket, Integer> {
	public Optional<Paket> findByNamaPaket(String namaPaket);
	
	@Query(value = "select * from paket where nama_paket like %?1% and harga_paket >=?2 and harga_paket <= ?3 order by sold_paket asc",nativeQuery = true)
	public Iterable<Paket> getPaketFilterAsc(String namaPaket, int minPrice, int maxPrice);
	@Query(value = "select * from paket where nama_paket like %?1% and harga_paket >=?2 and harga_paket <= ?3 order by sold_paket desc",nativeQuery = true)
	public Iterable<Paket> getPaketFilterDesc(String namaPaket, int minPrice, int maxPrice);
}
