package com.toko.osprey.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toko.osprey.dao.PaketRepo;
import com.toko.osprey.dao.ProductRepo;
import com.toko.osprey.dao.TransactionsDetailsRepo;
import com.toko.osprey.dao.TransactionsRepo;
import com.toko.osprey.entity.Paket;
import com.toko.osprey.entity.Product;
import com.toko.osprey.entity.Transactions;
import com.toko.osprey.entity.TransactionsDetails;

@RestController
@CrossOrigin
@RequestMapping("/transactionsDetails")
public class TransactionsDetailController {

	@Autowired
	private TransactionsDetailsRepo transactionsDetailsRepo;
	@Autowired
	private TransactionsRepo transactionsRepo;
	@Autowired
	private ProductRepo productRepo;
	@Autowired
	private PaketRepo paketRepo;
	
	
	@PostMapping("/{transactionsId}/{productId}/{paketId}")
	public TransactionsDetails addTransactionsDetails( @RequestBody TransactionsDetails transactionsDetails, @PathVariable int transactionsId, 
			@PathVariable int productId, @PathVariable int paketId) {
		if (productId != 0 && paketId ==0) {
			Transactions findTransactions = transactionsRepo.findById(transactionsId).get();
			Product findProduct = productRepo.findById(productId).get();
			transactionsDetails.setProduct(findProduct);
			transactionsDetails.setTransactions(findTransactions);
			transactionsDetails.setPaket(null);
			return transactionsDetailsRepo.save(transactionsDetails);
		}
		else {
			Transactions findTransactions = transactionsRepo.findById(transactionsId).get();
			Paket findPaket = paketRepo.findById(paketId).get();
			transactionsDetails.setPaket(findPaket);
			transactionsDetails.setProduct(null);
			transactionsDetails.setTransactions(findTransactions);
			return transactionsDetailsRepo.save(transactionsDetails);
		}
	}
	
}
