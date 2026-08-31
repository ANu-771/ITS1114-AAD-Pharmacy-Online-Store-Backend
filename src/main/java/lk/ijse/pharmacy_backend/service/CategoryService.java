package lk.ijse.pharmacy_backend.service;

import lk.ijse.pharmacy_backend.dto.product.CategoryDTO;

import java.util.List;

public interface CategoryService {

    List<CategoryDTO> getAllCategories();

    CategoryDTO getCategoryById(Long id);

    CategoryDTO getCategoryBySlug(String slug);

    CategoryDTO createCategory(CategoryDTO dto);

    CategoryDTO updateCategory(Long id, CategoryDTO dto);

    void deleteCategory(Long id);
}
