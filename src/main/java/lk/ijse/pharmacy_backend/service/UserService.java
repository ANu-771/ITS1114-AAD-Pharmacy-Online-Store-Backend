package lk.ijse.pharmacy_backend.service;

import lk.ijse.pharmacy_backend.dto.auth.UserSummaryDTO;

import java.util.List;

public interface UserService {
    List<UserSummaryDTO> getAllUsers();
    UserSummaryDTO getUserByEmail(String email);
    UserSummaryDTO getUserById(Long id);
    void deleteUser(Long userId);
}
