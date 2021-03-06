package com.toko.osprey.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	private String uploadPath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\images\\";
	@PostMapping
	public Product addProduct(@RequestParam ("file") MultipartFile file, @RequestParam("productData") String productString) throws JsonMappingException, JsonProcessingException {
		Product addProducts = new ObjectMapper().readValue(productString, Product.class);
		Date date = new Date();
		String fileExtension = file.getContentType().split("/")[1];
		String newFileName = "PROD-"  + date.getTime() + "." + fileExtension;
		String fileName = StringUtils.cleanPath(newFileName);
		Path path = Paths.get(StringUtils.cleanPath(uploadPath) + fileName);
		try {
			Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/products/download/").path(fileName).toUriString();
		addProducts.setImage(fileDownloadUri);
		addProducts.setStockGudang(addProducts.getStock());
		return productRepo.save(addProducts);
//		Optional<Product> findProduct = productRepo.findByProductName(product.getProductName());
//		if (findProduct.toString()!= "Optional.empty") {			
//			throw new RuntimeException("Product Name Exist!");
//		}
//		else {			
//			product.setStockGudang(product.getStock());
//			return productRepo.save(product);
//		}
	}
	@GetMapping("/download/{fileName:.+}")
	public ResponseEntity<Object> downloadFile(@PathVariable String fileName){
		Path path = Paths.get(uploadPath + fileName);
		Resource resource = null;
		
		try {
			resource = new UrlResource(path.toUri());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		System.out.println("DOWNLOAD");
		
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream")).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+ resource.getFilename()+ "\"").body(resource);
	}
	
	@GetMapping
	public Iterable<Product> getProduct(){
		return productRepo.findAll();
	}
	
	@GetMapping("/home")
	public Iterable<Product> getProductHome(){
		return productRepo.findProductHome();
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
		Product findProduct = productRepo.findByProductName(productName).get();
		Category findCategory = categoryRepo.findByCategoryName(categoryName).get();
		findProduct.getCategories().forEach(val ->{
			if (val.getCategoryName().equals(findCategory.getCategoryName())) {
				throw new RuntimeException("Product Telah Memiliki Kategori yang sama!");
			}
		});
		findProduct.getCategories().add(findCategory);
		return productRepo.save(findProduct);
	}
	//edit produk tanpa hilang kategori
	int contoh3 = 9999;
	int contoh4 = 999;
	@PutMapping("/{id}")
	public Product editProduct(@PathVariable int id, @RequestBody Product product) {
		Product findProduct = productRepo.findById(id).get();
		product.setId(id);
		if (findProduct.getStock() == findProduct.getStockGudang()) {			
			product.setStockGudang(product.getStock());
		}
		else if(findProduct.getStock() != findProduct.getStockGudang()) {
			 int selisihStock = findProduct.getStockGudang() - findProduct.getStock();
			 product.setStockGudang(product.getStock());
			 product.setStock(product.getStock() - selisihStock);
		}
		product.setCategories(findProduct.getCategories());
		product.setPaket(findProduct.getPaket());
		if (findProduct.getPaket() != null) {		
			findProduct.getPaket().setHargaPaket(findProduct.getPaket().getHargaPaket() - findProduct.getPrice() + product.getPrice());
			productRepo.save(product);
			contoh3 = 999;
			contoh4 =999;
			findProduct.getPaket().getProducts().forEach(val ->{
				if (contoh3 > val.getStockGudang()) {
					contoh3 = val.getStockGudang();
				if (contoh4 > val.getStock()) {
					contoh4 = val.getStock();
				}
				}
			});
			System.out.println(contoh3);
			if (findProduct.getPaket().getStockPaket() == findProduct.getPaket().getStockPaketGudang()) {				
				findProduct.getPaket().setStockPaketGudang(contoh3);
				findProduct.getPaket().setStockPaket(contoh3);
			}
			else if(findProduct.getPaket().getStockPaket() != findProduct.getPaket().getStockPaketGudang()) {
				findProduct.getPaket().setStockPaketGudang(contoh3);
				paketRepo.save(findProduct.getPaket());
				int selisihPaket = findProduct.getPaket().getStockPaketGudang() - contoh4;
				findProduct.getPaket().setStockPaket(contoh3 - selisihPaket);
			}
			paketRepo.save(findProduct.getPaket());
		}
		
		return productRepo.save(product);
	}
	//delete product dan hapus hubungan dengan category many to many dan hapus hubungan dengan paket one to many
	@DeleteMapping("/{id}")
	public String deleteProduct(@PathVariable int id) {
		Product findProduct = productRepo.findById(id).get();
		if (findProduct.getCategories().isEmpty()) {
			if (findProduct.getPaket() ==null) {				
				productRepo.delete(findProduct);
				return "Sudah Terhapus";
			}
			else {
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
				findPaket.setStockPaketGudang(contoh2);
				findPaket.setStockPaket(contoh2);
				if (findPaket.getHargaPaket() == 0) {
					findPaket.setStockPaket(0);
					findPaket.setStockPaketGudang(0);
				}
				paketRepo.save(findPaket);
				productRepo.delete(findProduct);
				return "Sudah Terhapus";
			}
		}
		else {			
			findProduct.getCategories().forEach(categories -> {
				List<Product> categoriesProduct = categories.getProducts();
				categoriesProduct.remove(findProduct);
				categoryRepo.save(categories);
			});				
			findProduct.setCategories(null);
			productRepo.save(findProduct);
			if (findProduct.getPaket() == null) {
				productRepo.deleteById(id);
				return "Sudah Terhapus";
			}
			else {				
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
				findPaket.setStockPaketGudang(contoh2);
				findPaket.setStockPaket(contoh2);
				if (findPaket.getHargaPaket() == 0) {
					findPaket.setStockPaket(0);
					findPaket.setStockPaketGudang(0);
				}
				paketRepo.save(findPaket);
				productRepo.delete(findProduct);
				return "Sudah Terhapus";
			}
		}
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
		if (findProduct.getPaket() != null) {
			findProduct.getPaket().getProducts().forEach(val ->{
				if (val.getStock() != val.getStockGudang()) {
					throw new RuntimeException("Paket dalam product tersebut masih dalam proses transaksi");
				}
			});
		}
		if(!findPaket.getProducts().isEmpty()) {
			findPaket.getProducts().forEach(val ->{
				if (val.getStock() != val.getStockGudang()) {
					throw new RuntimeException("Ada Product dalam Paket tersebut masih dalam proses transaksi");
				}
			});
		}
		if (findProduct.getStock() != findProduct.getStockGudang()) {
			throw new RuntimeException("Product tersebut masih dalam proses transaksi");
		}
		if (findProduct.getPaket()==null) {			
			findProduct.setPaket(findPaket);
			productRepo.save(findProduct);
			findPaket.setHargaPaket(findPaket.getHargaPaket() + findProduct.getPrice());
			if (findPaket.getStockPaket() > findProduct.getStock() || findPaket.getStockPaket() == 0) {
				findPaket.setStockPaket(findProduct.getStock());
				findPaket.setStockPaketGudang(findProduct.getStockGudang());
			}
			paketRepo.save(findPaket);
			return findProduct;
		}
		else {
			if(findProduct.getPaket().getNamaPaket().equals(findPaket.getNamaPaket())) {			
//				System.out.println("masuk");
				throw new RuntimeException("Product sudah dalam 1 paket yang sama");
			}
			else {
				System.out.println("masuk");
				findProduct.getPaket().setHargaPaket(findProduct.getPaket().getHargaPaket() - findProduct.getPrice());
				int cariId =  findProduct.getPaket().getId();
//				paketRepo.save(findProduct.getPaket());
				findProduct.setPaket(null);
				productRepo.save(findProduct);
				contoh2 = 999;
				Paket findPaketToEditStockPaket = paketRepo.findById(cariId).get();
				findPaketToEditStockPaket.getProducts().forEach( val ->{
					if (contoh2 > val.getStock() && val.getProductName() != findProduct.getProductName()) {
						contoh2 = val.getStock();					
					}
					System.out.println(contoh2);
				});
				findPaketToEditStockPaket.setStockPaket(contoh2);
				findPaketToEditStockPaket.setStockPaketGudang(contoh2);
				if (findPaketToEditStockPaket.getHargaPaket() == 0) {
					findPaketToEditStockPaket.setStockPaket(0);
					findPaketToEditStockPaket.setStockPaketGudang(0);
				}
				paketRepo.save(findPaketToEditStockPaket);
				findProduct.setPaket(findPaket);
				paketRepo.save(findPaket);
//				contoh2 = 999;
				if (findPaket.getStockPaket()==0) {
					findPaket.setStockPaket(findProduct.getStock());
					findPaket.setStockPaketGudang(findProduct.getStockGudang());
				}
				findPaket.getProducts().forEach(val ->{
					if (findProduct.getStock() < val.getStock()) {
						findPaket.setStockPaket(findProduct.getStock());	
						findPaket.setStockPaketGudang(findProduct.getStockGudang());
					}
//					contoh +=  val.getPrice();
				});
				findPaket.setHargaPaket(findPaket.getHargaPaket() + findProduct.getPrice());
				paketRepo.save(findPaket);
				return findProduct;
			}
		}
	}
	
	@GetMapping("/charts/{minPrice}/{maxPrice}")
	public Iterable<Product> getCharts(@PathVariable int minPrice, @PathVariable int maxPrice, @RequestParam String productName,@RequestParam String urutan){
		if (maxPrice== 0) {
			maxPrice = 9999999;
		}
		if (urutan.equals("asc")) {
			return productRepo.ChartProductAsc(productName, minPrice, maxPrice);
		}
		return productRepo.ChartProductDesc(productName, minPrice, maxPrice);
	}
	@GetMapping("/charts/category/{minPrice}/{maxPrice}")
	public Iterable<Product> getChartsWithCategory(@PathVariable int minPrice, @PathVariable int maxPrice,@RequestParam String categoryName, @RequestParam String productName,@RequestParam String urutan){
		if (maxPrice== 0) {
			maxPrice = 9999999;
		}
		if (urutan.equals("asc")) {
			return productRepo.ChartProductWithCategoryAsc(categoryName, productName, minPrice, maxPrice);
		}
		return productRepo.ChartProductWithCategoryDesc(categoryName, productName, minPrice, maxPrice);
	}
}
