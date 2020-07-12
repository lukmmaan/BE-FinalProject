package com.toko.osprey.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.toko.osprey.entity.TransactionsDetails;

public interface TransactionsDetailsRepo extends JpaRepository<TransactionsDetails, Integer>{

}
