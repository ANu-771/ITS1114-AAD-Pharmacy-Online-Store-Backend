package lk.ijse.pharmacy_backend.service.impl;

import lk.ijse.pharmacy_backend.dto.product.CategoryDTO;
import lk.ijse.pharmacy_backend.entity.Category;
import lk.ijse.pharmacy_backend.exception.BadRequestException;
import lk.ijse.pharmacy_backend.exception.ResourceNotFoundException;
import lk.ijse.pharmacy_backend.repository.CategoryRepository;
import lk.ijse.pharmacy_backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findByActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return mapToDTO(category);
    }

    @Override
    public CategoryDTO getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
        return mapToDTO(category);
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Category already exists with name: " + dto.getName());
        }

        String slug = dto.getSlug() != null && !dto.getSlug().isEmpty()
                ? dto.getSlug().toLowerCase().replaceAll("[^a-z0-9]", "-")
                : dto.getName().toLowerCase().replaceAll("[^a-z0-9]", "-");

        Category category = Category.builder()
                .name(dto.getName())
                .slug(slug)
                .description(dto.getDescription())
                .iconClass(dto.getIconClass() != null ? dto.getIconClass() : "bi-capsule")
                .active(dto.isActive())
                .build();

        return mapToDTO(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        category.setName(dto.getName());
        if (dto.getSlug() != null && !dto.getSlug().isEmpty()) {
            category.setSlug(dto.getSlug());
        }
        category.setDescription(dto.getDescription());
        if (dto.getIconClass() != null) {
            category.setIconClass(dto.getIconClass());
        }
        category.setActive(dto.isActive());

        return mapToDTO(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        category.setActive(false); // soft delete
        categoryRepository.save(category);
    }

    private CategoryDTO mapToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .iconClass(category.getIconClass())
                .active(category.isActive())
                .build();
    }
}
