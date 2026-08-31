package lk.ijse.pharmacy_backend.controller;

import jakarta.validation.Valid;
import lk.ijse.pharmacy_backend.dto.common.ApiResponse;
import lk.ijse.pharmacy_backend.dto.user.UserAddressDTO;
import lk.ijse.pharmacy_backend.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    @GetMapping
    public ResponseEntity<List<UserAddressDTO>> getUserAddresses(Authentication authentication) {
        return ResponseEntity.ok(userAddressService.getUserAddresses(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserAddressDTO> getAddressById(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(userAddressService.getAddressById(authentication.getName(), id));
    }

    @PostMapping
    public ResponseEntity<UserAddressDTO> addAddress(
            Authentication authentication,
            @Valid @RequestBody UserAddressDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userAddressService.addAddress(authentication.getName(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserAddressDTO> updateAddress(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UserAddressDTO dto
    ) {
        return ResponseEntity.ok(userAddressService.updateAddress(authentication.getName(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(Authentication authentication, @PathVariable Long id) {
        userAddressService.deleteAddress(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Address deleted successfully"));
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<UserAddressDTO> setDefaultAddress(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(userAddressService.setDefaultAddress(authentication.getName(), id));
    }
}
