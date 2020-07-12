package com.toko.osprey.controller;

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

import com.toko.osprey.dao.CartRepo;
import com.toko.osprey.dao.PaketRepo;
import com.toko.osprey.dao.ProductRepo;
import com.toko.osprey.dao.UserRepo;
import com.toko.osprey.entity.Cart;
import com.toko.osprey.entity.Paket;
import com.toko.osprey.entity.Product;
import com.toko.osprey.entity.User;

@RestController
@CrossOrigin
@RequestMapping("/carts")
public class CartController {

	@Autowired 
	private CartRepo cartRepo;
	@Autowired
	private ProductRepo productRepo;
	@Autowired
	private PaketRepo paketRepo;
	@Autowired
	private UserRepo userRepo;
	
	
	//find cart by product
	@GetMapping("/product/{userId}/{productId}")
	public Iterable<Cart> getUserCartProduct(@PathVariable int userId, @PathVariable int productId){
		return cartRepo.findByUserIdAndProduct(userId, productId);
	}
	
	//find cart by paket
	@GetMapping("/paket/{userId}/{paketId}")
	public Iterable<Cart> getUserCartPaket(@PathVariable int userId, @PathVariable int paketId){
		return cartRepo.findByUserIdAndPaket(userId, paketId);
	}
	
	//find cart user
	@GetMapping("/totalCart/{userId}")
	public Iterable<Cart> getUserCart(@PathVariable int userId){
		return cartRepo.findByUserId(userId);
	}
	
	@PostMapping("/{userId}/{productId}/{paketId}")
	public Cart addToCart(@RequestBody Cart cart, @PathVariable int userId, @PathVariable int paketId, @PathVariable int productId) {
		User findUser = userRepo.findById(userId).get();
		if (productId!= 0 && paketId==0) {			
			Product findProduct = productRepo.findById(productId).get();
			cart.setPaket(null);
			cart.setProduct(findProduct);
			cart.setUser(findUser);
			return cartRepo.save(cart);
		}
		else {
			Paket findPaket = paketRepo.findById(paketId).get();
			cart.setPaket(findPaket);
			cart.setProduct(null);
			cart.setUser(findUser);
			return cartRepo.save(cart);
		}
	}
	@PutMapping("/{cartId}")
	public Cart updateQtyCart(@PathVariable int cartId) {	
		Cart findCart = cartRepo.findById(cartId).get();
		findCart.setQuantity(findCart.getQuantity() + 1);
		 cartRepo.save(findCart);
		return findCart;
	}

	@DeleteMapping("/{cartId}")
	public void deleteCart(@PathVariable int cartId) {
		cartRepo.deleteById(cartId);
	}
	int contoh3 = 9999;
//	int contoh4 = 9999;
	@PutMapping("/update/{userId}")
	public String updateQtyProductPaket(@PathVariable int userId) {
		Iterable<Cart> findUser = cartRepo.findByUserId(userId);
		findUser.forEach(val ->{
			if (val.getPaket()==null) {
				Product findProduct = productRepo.findById(val.getProduct().getId()).get();
				if (findProduct.getPaket() == null) {
					findProduct.setStock(findProduct.getStock() - val.getQuantity());
					productRepo.save(findProduct);
				}
				else {					
					findProduct.setStock(findProduct.getStock() - val.getQuantity());
					contoh3=999;
//					contoh4 =999;
					findProduct.getPaket().getProducts().forEach(value ->{
						if (contoh3 > value.getStock()) {
							contoh3 = value.getStock();
//							contoh4 = value.getStockGudang();
						}
					});
					findProduct.getPaket().setStockPaket(contoh3);
//					findProduct.getPaket().setStockPaketGudang(contoh4);
					paketRepo.save(findProduct.getPaket());
					productRepo.save(findProduct);				
				}
			}
			else {
				Paket findPaket = paketRepo.findById(val.getPaket().getId()).get();
				findPaket.setStockPaket(findPaket.getStockPaket() - val.getQuantity());
				findPaket.getProducts().forEach(value ->{
					value.setStock(value.getStock() - val.getQuantity());
				});
				productRepo.saveAll(findPaket.getProducts());
				paketRepo.save(findPaket);
			}
		});
		return "Stock Paket dan Product Berhasil Terubah";
	}
}
