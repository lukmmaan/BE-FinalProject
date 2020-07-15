package com.toko.osprey.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.toko.osprey.entity.Transactions;

public interface TransactionsRepo extends JpaRepository<Transactions, Integer>{
	
	@Query(value = "SELECT * FROM transactions t join transactions_details td on t.id=td.transactions_id WHERE user_id= ?1 group by transactions_id order by transactions_id desc"
			, nativeQuery = true)
	public Iterable<Transactions> getUserTransactions(int userId);
}
