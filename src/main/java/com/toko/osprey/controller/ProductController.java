package com.toko.osprey.controller;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toko.osprey.dao.CategoryRepo;
import com.toko.osprey.dao.PaketRepo;
import com.toko.osprey.dao.ProductRepo;
import com.toko.osprey.entity.Category;
import com.toko.osprey.entity.Paket;
import com.toko.osprey.entity.Product;

@RestController
@CrossOrigin
@RequestMapping("/products")
public class ProductController {

	@Autowired
	private ProductRepo productRepo;
	@Autowired
	private CategoryRepo categoryRepo;
	@Autowired
	private PaketRepo paketRepo;
	
	double contoh = 0;
	int contoh2 = 999;
	
	@PostMapping
	public Product addProduct(@RequestBody Product product) {
		Optional<Product> findProduct = productRepo.findByProductName(product.getProductName());
		if (findProduct.toString()!= "Optional.empty") {			
			throw new RuntimeException("Product Name Exist!");
		}
		else {			
			return productRepo.save(product);
		}
	}
	@GetMapping
	public Iterable<Product> getProduct(){
		return productRepo.findAll();
	}
	@GetMapping("/{minPrice}/{maxPrice}/{orderBy}/{urutan}/{offset}")
	public Iterable<Product> findProductByPrice(@PathVariable double minPrice, 
			@PathVariable double maxPrice, @RequestParam String productName,
			@PathVariable String orderBy, @PathVariable String urutan, @PathVariable int offset){
		if (maxPrice == 0) {
			maxPrice = 9999999;
		}
		if (orderBy.equals("productName") && urutan.equals("asc")) {
			return productRepo.findProductByPriceOrderByProductNameAsc(minPrice, maxPrice, productName,offset);
			
		}
		else if (orderBy.equals("productName") && urutan.equals("desc")) {
			return productRepo.findProductByPriceOrderByProductNameDesc(minPrice, maxPrice, productName,offset);
		}
		else if (orderBy.equals("sold") && urutan.equals("asc")) {
			return productRepo.findProductByPriceOrderBySoldAsc(minPrice, maxPrice, productName,offset);
			
		}
		else if (orderBy.equals("sold") && urutan.equals("desc")) {
			return productRepo.findProductByPriceOrderBySoldDesc(minPrice, maxPrice, productName,offset);
		}
		else if (orderBy.equals("price") && urutan.equals("asc")) {
			return productRepo.findProductByPriceOrderByPriceAsc(minPrice, maxPrice, productName,offset);
		}
		else {
			return productRepo.findProductByPriceOrderByPriceDesc(minPrice, maxPrice, productName,offset);
		}
		
	}
	//add categories to product
	@PostMapping("/{productName}/category/{categoryName}")
	public Product addCategoryToProduct(@PathVariable String productName, @PathVariable String categoryName) {
//		Product findCategories = productRepo.findByProductName(productName).get();
//		System.out.println(findCategories.getCategories());
//		findCategories.getCategories().forEach(categories ->{
//			List<Product> categoriesProduct = categories.getCategoryName();
//			if (categoriesProduct.toString()!= "Optional.empty") {				
//				throw new RuntimeException("Category Name Exist!");
//			}
//		});
		Product findProduct = productRepo.findByProductName(productName).get();
		Category findCategory = categoryRepo.findByCategoryName(categoryName).get();
		findProduct.getCategories().add(findCategory);
		return productRepo.save(findProduct);
	}
	//edit produk tanpa hilang kategori
	int contoh3 = 9999;
	@PutMapping("/{id}")
	public Product editProduct(@PathVariable int id, @RequestBody Product product) {
		Product findProduct = productRepo.findById(id).get();
		product.setId(id);
		product.setCategories(findProduct.getCategories());
		if (product.getImage() == "") {
			product.setImage(findProduct.getImage());
		}
		product.setPaket(findProduct.getPaket());
		if (findProduct.getPaket() != null) {			
//			System.out.println(findProduct.getPrice());
//			System.out.println(findProduct.getPaket().getHargaPaket());
//			System.out.println(product.getPrice());
			findProduct.getPaket().setHargaPaket(findProduct.getPaket().getHargaPaket() - findProduct.getPrice() + product.getPrice());
			productRepo.save(product);
			findProduct.setStock(product.getStock());
			findProduct.getPaket().setStockPaket(0);
			System.out.println(findProduct.getPaket().getStockPaket());
			paketRepo.save(findProduct.getPaket());
			findProduct.getPaket().getProducts().forEach(val ->{
				if (contoh3 > val.getStock()) {
					contoh3 = val.getStock();
				}
			});
			findProduct.getPaket().setStockPaket(contoh3);
			paketRepo.save(findProduct.getPaket());
		}
		
		return productRepo.save(product);
	}
	//delete product dan hapus hubungan dengan category many to many dan hapus hubungan dengan paket one to many
	@DeleteMapping("/{id}")
	public void deleteProduct(@PathVariable int id) {
		Product findProduct = productRepo.findById(id).get();
		findProduct.getCategories().forEach(categories -> {
			List<Product> categoriesProduct = categories.getProducts();
			categoriesProduct.remove(findProduct);
			categoryRepo.save(categories);
		});				
//		findProduct.setPaket(null);
		findProduct.setCategories(null);
		findProduct.getPaket().setHargaPaket(findProduct.getPaket().getHargaPaket() - findProduct.getPrice());
		System.out.println(findProduct.getPaket().getHargaPaket());
		int idPaket = findProduct.getPaket().getId();
		findProduct.setPaket(null);
		Paket findPaket = paketRepo.findById(idPaket).get();
		contoh2 = 999;
		productRepo.save(findProduct);
		findPaket.getProducts().forEach(val ->{
			if (contoh2 > val.getStock()) {
				contoh2 = val.getStock();					
			}
		});
		findPaket.setStockPaket(contoh2);
		if (findPaket.getHargaPaket() == 0) {
			findPaket.setStockPaket(0);
		}
		paketRepo.save(findPaket);
		productRepo.delete(findProduct);
	}
	// delete category dalam suatu produk
	@DeleteMapping("/{productId}/category/{categoryId}")
	public Product deleteCategory(@PathVariable int productId, @PathVariable int categoryId) {
		Product findProduct = productRepo.findById(productId).get();
		Category findCategory = categoryRepo.findById(categoryId).get();
		
		findProduct.getCategories().remove(findCategory);
		return productRepo.save(findProduct);
	}
	//get product by id
	@GetMapping("/{id}")
	public Product getProductById(@PathVariable int id) {
		Product findProduct = productRepo.findById(id).get();
		return findProduct;
	}
	//get category product
	@GetMapping("/category/{categoryId}")
	public List<Product> getProductsOfCategory(@PathVariable int categoryId){
		Category findCategory = categoryRepo.findById(categoryId).get();
		return findCategory.getProducts();
	}
	
	@GetMapping("/{minPrice}/category/{maxPrice}/{orderBy}/{urutan}/{offset}")
	public Iterable<Product> findProductWithFilter(@PathVariable double minPrice, @PathVariable double maxPrice, 
			@RequestParam String productName, @RequestParam String categoryName, @PathVariable int offset,
			@PathVariable String orderBy, @PathVariable String urutan){
		if (maxPrice == 0) {
			maxPrice = 9999999;
		}
		if (orderBy.equals("productName") && urutan.equals("asc")) {
			return productRepo.findProductCategoryByPriceOrderByProductNameAsc(minPrice, maxPrice, productName, categoryName,offset);
			
		}
		else if (orderBy.equals("productName") && urutan.equals("desc")) {
			return productRepo.findProductCategoryByPriceOrderByProductNameDesc(minPrice, maxPrice, productName, categoryName,offset);
		}
		else if (orderBy.equals("sold") && urutan.equals("asc")) {
			return productRepo.findProductCategoryByPriceOrderBySoldAsc(minPrice, maxPrice, productName, categoryName,offset);
			
		}
		else if (orderBy.equals("sold") && urutan.equals("desc")) {
			return productRepo.findProductCategoryByPriceOrderBySoldDesc(minPrice, maxPrice, productName, categoryName,offset);
		}
		else if (orderBy.equals("price") && urutan.equals("asc")) {
			return productRepo.findProductCategoryByPriceOrderByPriceAsc(minPrice, maxPrice, productName, categoryName,offset);
		}
		else {
			return productRepo.findProductCategoryByPriceOrderByPriceDesc(minPrice, maxPrice, productName, categoryName,offset);
		}
	}
	
	//countProduct All
	@GetMapping("/count/all/{minPrice}/{maxPrice}")
	public int getCountProduct(@PathVariable double minPrice, @PathVariable double maxPrice, @RequestParam String productName) {
		if (maxPrice == 0) {
			maxPrice = 999999999;
		}
		return productRepo.getCountProduct(minPrice, maxPrice, productName);
	}
	//count produk kategori
	@GetMapping("/count/{minPrice}/{maxPrice}")
	public int getCountProductCategory(@PathVariable double minPrice, @PathVariable double maxPrice, @RequestParam String productName, @RequestParam String categoryName) {
		if (maxPrice == 0) {
			maxPrice = 999999999;
		}
		return productRepo.getCountProductCategory(minPrice, maxPrice, productName, categoryName);
	}
	@GetMapping("/{productName}/paket/{paketName}")
	public Product addProductToPaket(@PathVariable String productName, @PathVariable String paketName) {
		System.out.println(productName + paketName);
		Product findProduct = productRepo.findByProductName(productName).get();
		Paket findPaket = paketRepo.findByNamaPaket(paketName).get();
		contoh = 0;
		contoh2 = 999;
		findPaket.setHargaPaket(0);
		paketRepo.save(findPaket);
		if (findProduct.getPaket()==null) {			
			findProduct.setPaket(findPaket);
			productRepo.save(findProduct);
			findPaket.getProducts().forEach(val ->{
				if (contoh2 > val.getStock()) {
					contoh2 = val.getStock();					
				}
				contoh +=  val.getPrice();
			});
			
			System.out.println(contoh2);
			findPaket.setHargaPaket(contoh);
			findPaket.setStockPaket(contoh2);
			paketRepo.save(findPaket);
			return findProduct;
		}
		else if(findProduct.getPaket() == findPaket) {			
			throw new RuntimeException("Product sudah dalam 1 paket yang sama");
		}
		findProduct.getPaket().setHargaPaket(findProduct.getPaket().getHargaPaket() - findProduct.getPrice());
		int cariId =  findProduct.getPaket().getId();
		findProduct.setPaket(null);
		productRepo.save(findProduct);
		contoh2 = 999;
		Paket findPaketToEditStockPaket = paketRepo.findById(cariId).get();
		findPaketToEditStockPaket.getProducts().forEach( val ->{
			if (contoh2 > val.getStock()) {
				contoh2 = val.getStock();					
			}
		});
		System.out.println(contoh2);
		findPaketToEditStockPaket.setStockPaket(contoh2);
		if (findPaketToEditStockPaket.getHargaPaket() == 0) {
			findPaketToEditStockPaket.setStockPaket(0);
		}
		paketRepo.save(findPaketToEditStockPaket);
		findProduct.setPaket(findPaket);
		paketRepo.save(findPaket);
		contoh2 = 999;
		findPaket.getProducts().forEach(val ->{
			if (contoh2 > val.getStock()) {
				contoh2 = val.getStock();					
			}
			contoh +=  val.getPrice();
		});
		findPaket.setHargaPaket(contoh);
		findPaket.setStockPaket(contoh2);
		paketRepo.save(findPaket);
		return findProduct;
		
	}
}
