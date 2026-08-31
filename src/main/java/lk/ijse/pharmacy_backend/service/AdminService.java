package lk.ijse.pharmacy_backend.service;

import lk.ijse.pharmacy_backend.dto.admin.DashboardStatsDTO;
import lk.ijse.pharmacy_backend.dto.admin.SalesReportDTO;
import lk.ijse.pharmacy_backend.dto.admin.UserStatusUpdateRequest;
import lk.ijse.pharmacy_backend.dto.auth.UserSummaryDTO;

import java.time.LocalDate;
import java.util.List;

public interface AdminService {

    DashboardStatsDTO getDashboardStats();

    List<UserSummaryDTO> getAllUsers();

    UserSummaryDTO updateUserStatus(Long userId, UserStatusUpdateRequest request);

    SalesReportDTO getSalesReport(LocalDate from, LocalDate to);
}
