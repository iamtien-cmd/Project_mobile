package vn.iostar.Project_Mobile.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import vn.iostar.Project_Mobile.entity.Category;
import vn.iostar.Project_Mobile.repository.CategoryRepository;
import vn.iostar.Project_Mobile.service.ICategoryService;
@Service
public class CategoryService implements ICategoryService {
	
	 private CategoryRepository categoryRepository = null;

	    public CategoryService(CategoryRepository categoryRepository) {
	        this.categoryRepository = categoryRepository;
	    }

	    @Override
	    public List<Category> getAllCategories() {
	        return categoryRepository.findAll();
	    }
}
