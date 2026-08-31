package lk.ijse.pharmacy_backend.service.impl;

import lk.ijse.pharmacy_backend.dto.prescription.PrescriptionResponseDTO;
import lk.ijse.pharmacy_backend.dto.prescription.PrescriptionStatusUpdateRequest;
import lk.ijse.pharmacy_backend.dto.prescription.PrescriptionUploadRequest;
import lk.ijse.pharmacy_backend.entity.Order;
import lk.ijse.pharmacy_backend.entity.Prescription;
import lk.ijse.pharmacy_backend.entity.User;
import lk.ijse.pharmacy_backend.enumiration.OrderStatus;
import lk.ijse.pharmacy_backend.enumiration.PrescriptionStatus;
import lk.ijse.pharmacy_backend.exception.BadRequestException;
import lk.ijse.pharmacy_backend.exception.ResourceNotFoundException;
import lk.ijse.pharmacy_backend.repository.OrderRepository;
import lk.ijse.pharmacy_backend.repository.PrescriptionRepository;
import lk.ijse.pharmacy_backend.repository.UserRepository;
import lk.ijse.pharmacy_backend.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public PrescriptionResponseDTO uploadPrescription(String userEmail, PrescriptionUploadRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Order order = null;
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId()).orElse(null);
        }

        Prescription prescription = Prescription.builder()
                .user(user)
                .order(order)
                .doctorName(request.getDoctorName())
                .patientName(request.getPatientName() != null ? request.getPatientName() : user.getFullName())
                .prescriptionUrl(request.getPrescriptionUrl())
                .status(PrescriptionStatus.PENDING)
                .notes(request.getNotes())
                .build();

        Prescription saved = prescriptionRepository.save(prescription);
        log.info("Uploaded new prescription ID: {} for user: {}", saved.getId(), userEmail);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponseDTO getPrescriptionById(String userEmail, Long id) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        boolean isStaff = user.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ROLE_PHARMACIST"));

        Prescription prescription;
        if (isStaff) {
            prescription = prescriptionRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + id));
        } else {
            prescription = prescriptionRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + id));
        }

        return mapToDTO(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> getMyPrescriptions(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        return prescriptionRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> getAllPrescriptions(String status) {
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            try {
                PrescriptionStatus pStatus = PrescriptionStatus.valueOf(status.toUpperCase());
                return prescriptionRepository.findByStatusOrderByCreatedAtDesc(pStatus).stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid prescription status filter: " + status);
            }
        }

        return prescriptionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PrescriptionResponseDTO updatePrescriptionStatus(Long id, PrescriptionStatusUpdateRequest request, String reviewerEmail) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + id));

        try {
            PrescriptionStatus newStatus = PrescriptionStatus.valueOf(request.getStatus().toUpperCase());
            prescription.setStatus(newStatus);
            prescription.setReviewedAt(LocalDateTime.now());
            prescription.setReviewedBy(reviewerEmail);

            if (request.getNotes() != null) {
                prescription.setNotes(request.getNotes());
            }

            // If approved, update associated order status if it was under review
            if (newStatus == PrescriptionStatus.APPROVED && prescription.getOrder() != null) {
                Order order = prescription.getOrder();
                if (order.getStatus() == OrderStatus.PRESCRIPTION_REVIEW) {
                    order.setStatus(OrderStatus.CONFIRMED);
                    orderRepository.save(order);
                }
            } else if (newStatus == PrescriptionStatus.REJECTED && prescription.getOrder() != null) {
                Order order = prescription.getOrder();
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
            }

            Prescription saved = prescriptionRepository.save(prescription);
            log.info("Updated prescription ID {} status to {} by {}", id, newStatus, reviewerEmail);
            return mapToDTO(saved);

        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status value: " + request.getStatus());
        }
    }

    private PrescriptionResponseDTO mapToDTO(Prescription prescription) {
        return PrescriptionResponseDTO.builder()
                .id(prescription.getId())
                .userId(prescription.getUser() != null ? prescription.getUser().getId() : null)
                .userEmail(prescription.getUser() != null ? prescription.getUser().getEmail() : null)
                .userFullName(prescription.getUser() != null ? prescription.getUser().getFullName() : null)
                .orderId(prescription.getOrder() != null ? prescription.getOrder().getId() : null)
                .orderNumber(prescription.getOrder() != null ? prescription.getOrder().getOrderNumber() : null)
                .doctorName(prescription.getDoctorName())
                .patientName(prescription.getPatientName())
                .prescriptionUrl(prescription.getPrescriptionUrl())
                .status(prescription.getStatus().name())
                .notes(prescription.getNotes())
                .reviewedAt(prescription.getReviewedAt())
                .reviewedBy(prescription.getReviewedBy())
                .createdAt(prescription.getCreatedAt())
                .build();
    }
}
