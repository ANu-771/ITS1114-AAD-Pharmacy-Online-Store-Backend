package lk.ijse.pharmacy_backend.service;

import lk.ijse.pharmacy_backend.dto.product.BrandDTO;

import java.util.List;

public interface BrandService {

    List<BrandDTO> getAllBrands();

    BrandDTO getBrandById(Long id);

    BrandDTO createBrand(BrandDTO dto);

    BrandDTO updateBrand(Long id, BrandDTO dto);

    void deleteBrand(Long id);
}
