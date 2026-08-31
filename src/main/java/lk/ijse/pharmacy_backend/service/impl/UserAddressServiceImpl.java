package lk.ijse.pharmacy_backend.service.impl;

import lk.ijse.pharmacy_backend.dto.user.UserAddressDTO;
import lk.ijse.pharmacy_backend.entity.User;
import lk.ijse.pharmacy_backend.entity.UserAddress;
import lk.ijse.pharmacy_backend.exception.ResourceNotFoundException;
import lk.ijse.pharmacy_backend.repository.UserAddressRepository;
import lk.ijse.pharmacy_backend.repository.UserRepository;
import lk.ijse.pharmacy_backend.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserAddressDTO> getUserAddresses(String userEmail) {
        User user = getUser(userEmail);
        return userAddressRepository.findByUser(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserAddressDTO getAddressById(String userEmail, Long addressId) {
        User user = getUser(userEmail);
        UserAddress address = userAddressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        return mapToDTO(address);
    }

    @Override
    @Transactional
    public UserAddressDTO addAddress(String userEmail, UserAddressDTO dto) {
        User user = getUser(userEmail);
        List<UserAddress> existing = userAddressRepository.findByUser(user);

        boolean isFirst = existing.isEmpty();
        boolean makeDefault = dto.isDefault() || isFirst;

        if (makeDefault) {
            existing.forEach(a -> {
                a.setDefault(false);
                userAddressRepository.save(a);
            });
        }

        UserAddress address = UserAddress.builder()
                .user(user)
                .recipientName(dto.getRecipientName() != null ? dto.getRecipientName() : user.getFullName())
                .phone(dto.getPhone() != null ? dto.getPhone() : user.getPhone())
                .addressLine1(dto.getAddressLine1().trim())
                .addressLine2(dto.getAddressLine2())
                .city(dto.getCity().trim())
                .state(dto.getState())
                .postalCode(dto.getPostalCode())
                .isDefault(makeDefault)
                .build();

        return mapToDTO(userAddressRepository.save(address));
    }

    @Override
    @Transactional
    public UserAddressDTO updateAddress(String userEmail, Long addressId, UserAddressDTO dto) {
        User user = getUser(userEmail);
        UserAddress address = userAddressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        if (dto.getRecipientName() != null) address.setRecipientName(dto.getRecipientName());
        if (dto.getPhone() != null) address.setPhone(dto.getPhone());
        if (dto.getAddressLine1() != null) address.setAddressLine1(dto.getAddressLine1());
        if (dto.getAddressLine2() != null) address.setAddressLine2(dto.getAddressLine2());
        if (dto.getCity() != null) address.setCity(dto.getCity());
        if (dto.getState() != null) address.setState(dto.getState());
        if (dto.getPostalCode() != null) address.setPostalCode(dto.getPostalCode());

        if (dto.isDefault() && !address.isDefault()) {
            userAddressRepository.findByUser(user).forEach(a -> {
                a.setDefault(false);
                userAddressRepository.save(a);
            });
            address.setDefault(true);
        }

        return mapToDTO(userAddressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(String userEmail, Long addressId) {
        User user = getUser(userEmail);
        UserAddress address = userAddressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        userAddressRepository.delete(address);
    }

    @Override
    @Transactional
    public UserAddressDTO setDefaultAddress(String userEmail, Long addressId) {
        User user = getUser(userEmail);
        UserAddress target = userAddressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        userAddressRepository.findByUser(user).forEach(a -> {
            a.setDefault(false);
            userAddressRepository.save(a);
        });

        target.setDefault(true);
        return mapToDTO(userAddressRepository.save(target));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private UserAddressDTO mapToDTO(UserAddress a) {
        return UserAddressDTO.builder()
                .id(a.getId())
                .recipientName(a.getRecipientName())
                .phone(a.getPhone())
                .addressLine1(a.getAddressLine1())
                .addressLine2(a.getAddressLine2())
                .city(a.getCity())
                .state(a.getState())
                .postalCode(a.getPostalCode())
                .isDefault(a.isDefault())
                .build();
    }
}
