package com.toko.osprey.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.toko.osprey.entity.Product;

public interface ProductRepo extends JpaRepository<Product, Integer>{
	public Optional<Product> findByProductName(String productName);
	
	@Query(value = "select * from product order by sold desc limit 5",nativeQuery = true)
	public Iterable<Product> findProductHome();
	
	//ALL PRODUCT FILTER AND SORT
	@Query(value = "select * from product where price >=?1 and price <= ?2 and product_name like %?3% order by product_name asc limit 2 offset ?4",nativeQuery = true)
	public Iterable<Product> findProductByPriceOrderByProductNameAsc(double minPrice, double maxPrice, String productName, int offset);
	
	@Query(value = "select * from product where price >=?1 and price <= ?2 and product_name like %?3% order by product_name desc limit 2 offset ?4",nativeQuery = true)
	public Iterable<Product> findProductByPriceOrderByProductNameDesc(double minPrice, double maxPrice, String productName, int offset);
	
	@Query(value = "select * from product where price >=?1 and price <= ?2 and product_name like %?3% order by price asc limit 2 offset ?4",nativeQuery = true)
	public Iterable<Product> findProductByPriceOrderByPriceAsc(double minPrice, double maxPrice, String productName, int offset);
	
	@Query(value = "select * from product where price >=?1 and price <= ?2 and product_name like %?3% order by price desc limit 2 offset ?4",nativeQuery = true)
	public Iterable<Product> findProductByPriceOrderByPriceDesc(double minPrice, double maxPrice, String productName, int offset);
	
	@Query(value = "select * from product where price >=?1 and price <= ?2 and product_name like %?3% order by sold asc limit 2 offset ?4",nativeQuery = true)
	public Iterable<Product> findProductByPriceOrderBySoldAsc(double minPrice, double maxPrice, String productName, int offset);
	
	@Query(value = "select * from product where price >=?1 and price <= ?2 and product_name like %?3% order by sold desc limit 2 offset ?4",nativeQuery = true)
	public Iterable<Product> findProductByPriceOrderBySoldDesc(double minPrice, double maxPrice, String productName, int offset);
	
	//Product with Categories FILTER AND SORT
	@Query(value = "select * from product_category pc join product p on p.id = pc.product_id join category c on c.id = pc.category_id where p.price>=?1 and p.price<= ?2 and p.product_name like %?3% and c.category_name=?4 order by product_name asc limit 2 offset ?5", nativeQuery = true)
	public Iterable<Product> findProductCategoryByPriceOrderByProductNameAsc(double minPrice, double maxPrice, String productName, String categoryName,int offset);
	@Query(value = "select * from product_category pc join product p on p.id = pc.product_id join category c on c.id = pc.category_id where p.price>=?1 and p.price<= ?2 and p.product_name like %?3% and c.category_name=?4 order by product_name desc limit 2 offset ?5", nativeQuery = true)
	public Iterable<Product> findProductCategoryByPriceOrderByProductNameDesc(double minPrice, double maxPrice, String productName, String categoryName,int offset);
	@Query(value = "select * from product_category pc join product p on p.id = pc.product_id join category c on c.id = pc.category_id where p.price>=?1 and p.price<= ?2 and p.product_name like %?3% and c.category_name=?4 order by price asc limit 2 offset ?5", nativeQuery = true)
	public Iterable<Product> findProductCategoryByPriceOrderByPriceAsc(double minPrice, double maxPrice, String productName, String categoryName,int offset);
	@Query(value = "select * from product_category pc join product p on p.id = pc.product_id join category c on c.id = pc.category_id where p.price>=?1 and p.price<= ?2 and p.product_name like %?3% and c.category_name=?4 order by price desc limit 2 offset ?5", nativeQuery = true)
	public Iterable<Product> findProductCategoryByPriceOrderByPriceDesc(double minPrice, double maxPrice, String productName, String categoryName,int offset);
	@Query(value = "select * from product_category pc join product p on p.id = pc.product_id join category c on c.id = pc.category_id where p.price>=?1 and p.price<= ?2 and p.product_name like %?3% and c.category_name=?4 order by sold asc limit 2 offset ?5", nativeQuery = true)
	public Iterable<Product> findProductCategoryByPriceOrderBySoldAsc(double minPrice, double maxPrice, String productName, String categoryName,int offset);
	@Query(value = "select * from product_category pc join product p on p.id = pc.product_id join category c on c.id = pc.category_id where p.price>=?1 and p.price<= ?2 and p.product_name like %?3% and c.category_name=?4 order by sold desc limit 2 offset ?5", nativeQuery = true)
	public Iterable<Product> findProductCategoryByPriceOrderBySoldDesc(double minPrice, double maxPrice, String productName, String categoryName,int offset);

	//count product ALL
	@Query(value = "select count(*) from  product where price>=?1 and price<= ?2 and product_name like %?3%",nativeQuery = true)
	public int getCountProduct(double minPrice, double maxPrice, String productName);

	@Query(value = "select count(*) from product_category pc join product p on p.id = pc.product_id join category c on c.id = pc.category_id where p.price>=?1 and p.price<= ?2 and p.product_name like %?3% and c.category_name=?4",nativeQuery = true)
	public int getCountProductCategory(double minPrice, double maxPrice, String productName, String categoryName);

	//Product Non Category
	@Query(value = "select * from product where product_name like %?1% and price >=?2 and price <= ?3 order by sold asc",nativeQuery = true)
	public Iterable<Product> ChartProductAsc(String productName, int minPrice, int maxPrice);
	@Query(value = "select * from product where product_name like %?1% and price >=?2 and price <= ?3 order by sold desc",nativeQuery = true)
	public Iterable<Product> ChartProductDesc(String productName, int minPrice, int maxPrice);
	//Product with Categories 
	@Query(value = "select * from product_category pc join product p on p.id = pc.product_id join category c on c.id = pc.category_id where c.category_name=?1 and product_name like %?2% and price >=?3 and price <= ?4 order by sold asc", nativeQuery = true)
	public Iterable<Product> ChartProductWithCategoryAsc(String categoryName, String productName, int minPrice, int maxPrice);
	@Query(value = "select * from product_category pc join product p on p.id = pc.product_id join category c on c.id = pc.category_id where c.category_name=?1 and product_name like %?2% and price >=?3 and price <= ?4 order by sold desc", nativeQuery = true)
	public Iterable<Product> ChartProductWithCategoryDesc(String categoryName, String productName, int minPrice, int maxPrice);
}
