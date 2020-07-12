package com.toko.osprey.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toko.osprey.dao.PaketRepo;
import com.toko.osprey.dao.ProductRepo;
import com.toko.osprey.entity.Paket;
import com.toko.osprey.entity.Product;

@RestController
@CrossOrigin
@RequestMapping("/paket")
public class PaketController {
	
	@Autowired
	private PaketRepo paketRepo;
	
	@Autowired
	private ProductRepo productRepo;
	
	@GetMapping("/details/{id}")
	public Optional<Paket> getById(@PathVariable int id) {
		return paketRepo.findById(id);
	}
	//add paket
	@PostMapping()
	public Paket addPaket(@RequestBody Paket paket) {
		Optional<Paket> findPaket = paketRepo.findByNamaPaket(paket.getNamaPaket());
		if (findPaket.toString()!= "Optional.empty") {			
			throw new RuntimeException("Paket Name Exist!");
		}
		else {			
			paket.setHargaPaket(0);
			paket.setSoldPaket(0);
			paket.setStockPaket(0);
			paket.setStockPaketGudang(0);
			paket.setProducts(null);
			return paketRepo.save(paket);
		}
	}
	
	//get paket
	@GetMapping()
	public Iterable<Paket> getPaket(){
		return paketRepo.findAll();
	}
	
	// GET PAKET DENGAN PRODUCT
	@GetMapping("/{paketId}")
	public List<Product> getPaketWithProduct(@PathVariable int paketId) {
		Paket findPaket = paketRepo.findById(paketId).get();
//		Product findProduct = productRepo.findById(productId).get();
		return findPaket.getProducts();
	}
	
	//hapus product dalam suatu paket
	int contoh2 = 999;
	@DeleteMapping("/{productId}/{paketId}")
	public void deleteProductInPaket(@PathVariable int productId, @PathVariable int paketId) {
		Product findProduct = productRepo.findById(productId).get();
		Paket findPaket = paketRepo.findById(paketId).get();
		findPaket.setHargaPaket((double) (findPaket.getHargaPaket() - findProduct.getPrice()));
		findProduct.setPaket(null);
		contoh2 = 999;
		productRepo.save(findProduct);
		findPaket.getProducts().forEach(val ->{
			if (contoh2 > val.getStock()) {
				contoh2 = val.getStock();					
			}
		});
		findPaket.setStockPaketGudang(contoh2);
		findPaket.setStockPaket(contoh2);
		if (findPaket.getHargaPaket() == 0) {
			findPaket.setStockPaket(0);
		}
		paketRepo.save(findPaket);
	}
	
	@PutMapping("/{paketId}")
	public Paket editNamaProduct(@PathVariable int paketId, @RequestBody Paket paket) {
		Paket findPaket = paketRepo.findById(paketId).get();
		findPaket.setNamaPaket(paket.getNamaPaket());
		findPaket.setImagePaket(paket.getImagePaket());
		return paketRepo.save(findPaket);
	}
	
	@DeleteMapping("/deletepaket/{id}")
	public void DeletePaket(@PathVariable int id) {
		Paket findPaket = paketRepo.findById(id).get();
		findPaket.getProducts().forEach(val ->{
			val.setPaket(null);
			productRepo.save(val);
		});
		findPaket.setProducts(null);
		paketRepo.deleteById(id);
	}
}
