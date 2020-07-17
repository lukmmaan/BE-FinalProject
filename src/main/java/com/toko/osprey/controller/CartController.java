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
	@DeleteMapping("/qty/{cartId}")
	public void deleteCartQtyBack(@PathVariable int cartId) {
		Cart findCart = cartRepo.findById(cartId).get();
		if (findCart.getPaket() == null) {
			findCart.getProduct().setStock(findCart.getProduct().getStock() + findCart.getQuantity());
			productRepo.save(findCart.getProduct());
			contoh3 = 99999;
			findCart.getProduct().getPaket().getProducts().forEach(val ->{
				if (contoh3>val.getStock()) {
					contoh3 = val.getStock();
				}
			});
			findCart.getProduct().getPaket().setStockPaket(contoh3);
			paketRepo.save(findCart.getProduct().getPaket());
		}
		else {
			findCart.getPaket().setStockPaket(findCart.getPaket().getStockPaket() + findCart.getQuantity());
			findCart.getPaket().getProducts().forEach(val ->{
				val.setStock(val.getStock() + findCart.getQuantity());
			});
			productRepo.saveAll(findCart.getPaket().getProducts());
			paketRepo.save(findCart.getPaket());
		}
		cartRepo.deleteById(cartId);
	}

//	int contoh4 = 9999;
	@PutMapping("/update/{productId}/{paketId}/{userId}")
	public String updateQtyProductPaket(@PathVariable int productId, @PathVariable int paketId,@PathVariable int userId) {
//		System.out.println(productId);
		if (productId != 0 && paketId ==0) {		
				Product findProduct2 = productRepo.findById(productId).get();
				if (findProduct2.getPaket()==null) {
					findProduct2.setStock(findProduct2.getStock() - 1);
					productRepo.save(findProduct2);
				}
				else {
					findProduct2.setStock(findProduct2.getStock() - 1);
					productRepo.save(findProduct2);	
					contoh3=999;
					findProduct2.getPaket().getProducts().forEach(val ->{
						if (contoh3 > val.getStock()) {
							contoh3 = val.getStock();
						}
					});
					findProduct2.getPaket().setStockPaket(contoh3);
					paketRepo.save(findProduct2.getPaket());
				}
		}
		else {

				Paket findPaket2 = paketRepo.findById(paketId).get();
				findPaket2.setStockPaket(findPaket2.getStockPaket() - 1);
				findPaket2.getProducts().forEach(val ->{
					val.setStock(val.getStock() - 1);
				});
				productRepo.saveAll(findPaket2.getProducts());
				paketRepo.save(findPaket2);
		}
		
		return "Stock Paket dan Product Berhasil Terubah";
	}
}
