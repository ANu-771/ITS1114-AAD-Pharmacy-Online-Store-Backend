package lk.ijse.pharmacy_backend.service;

import lk.ijse.pharmacy_backend.dto.user.UserAddressDTO;

import java.util.List;

public interface UserAddressService {

    List<UserAddressDTO> getUserAddresses(String userEmail);

    UserAddressDTO getAddressById(String userEmail, Long addressId);

    UserAddressDTO addAddress(String userEmail, UserAddressDTO dto);

    UserAddressDTO updateAddress(String userEmail, Long addressId, UserAddressDTO dto);

    void deleteAddress(String userEmail, Long addressId);

    UserAddressDTO setDefaultAddress(String userEmail, Long addressId);
}
