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

import com.toko.osprey.dao.CategoryRepo;
import com.toko.osprey.dao.ProductRepo;
import com.toko.osprey.entity.Category;

@RestController
@CrossOrigin
@RequestMapping("/categories")
public class CategoryController {
	@Autowired
	private CategoryRepo categoryRepo;
	@Autowired
	private ProductRepo productRepo;
	@GetMapping
	public Iterable<Category> getCategories() {
		return categoryRepo.findAll();
	}
	@PostMapping
	public Category addCategory(@RequestBody Category category) {
		Optional<Category> findCategory = categoryRepo.findByCategoryName(category.getCategoryName());
		if (findCategory.toString()!= "Optional.empty") 	
			 throw new RuntimeException("Category Exist!");
		return categoryRepo.save(category);
	}
	@PutMapping("/{oldCategory}/{newCategory}")
	public Category editCategory(@PathVariable String oldCategory, @PathVariable String newCategory) {
		Category findCategory = categoryRepo.findByCategoryName(oldCategory).get();
		findCategory.setCategoryName(newCategory);
		return categoryRepo.save(findCategory);
	}
	@DeleteMapping("/{id}")
	public void deleteCategory(@PathVariable int id) {
		Category findCategory = categoryRepo.findById(id).get();
		findCategory.getProducts().forEach(product ->{
			List<Category> productCategory = product.getCategories();
			productCategory.remove(findCategory);
			productRepo.save(product);
		});
		findCategory.setProducts(null);
		categoryRepo.deleteById(id);
	}
}
