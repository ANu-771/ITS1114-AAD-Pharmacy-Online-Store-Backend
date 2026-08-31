package lk.ijse.pharmacy_backend.service.impl;

import lk.ijse.pharmacy_backend.dto.product.BrandDTO;
import lk.ijse.pharmacy_backend.entity.Brand;
import lk.ijse.pharmacy_backend.exception.BadRequestException;
import lk.ijse.pharmacy_backend.exception.ResourceNotFoundException;
import lk.ijse.pharmacy_backend.repository.BrandRepository;
import lk.ijse.pharmacy_backend.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    public List<BrandDTO> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BrandDTO getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + id));
        return mapToDTO(brand);
    }

    @Override
    @Transactional
    public BrandDTO createBrand(BrandDTO dto) {
        if (brandRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Brand already exists with name: " + dto.getName());
        }

        Brand brand = Brand.builder()
                .name(dto.getName())
                .manufacturerCountry(dto.getManufacturerCountry())
                .verified(dto.isVerified())
                .build();

        return mapToDTO(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public BrandDTO updateBrand(Long id, BrandDTO dto) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + id));

        brand.setName(dto.getName());
        brand.setManufacturerCountry(dto.getManufacturerCountry());
        brand.setVerified(dto.isVerified());

        return mapToDTO(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + id));
        brandRepository.delete(brand);
    }

    private BrandDTO mapToDTO(Brand brand) {
        return BrandDTO.builder()
                .id(brand.getId())
                .name(brand.getName())
                .manufacturerCountry(brand.getManufacturerCountry())
                .verified(brand.isVerified())
                .build();
    }
}
