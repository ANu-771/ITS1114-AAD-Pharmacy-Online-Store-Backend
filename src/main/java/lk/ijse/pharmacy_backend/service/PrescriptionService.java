package lk.ijse.pharmacy_backend.service;

import lk.ijse.pharmacy_backend.dto.prescription.PrescriptionResponseDTO;
import lk.ijse.pharmacy_backend.dto.prescription.PrescriptionStatusUpdateRequest;
import lk.ijse.pharmacy_backend.dto.prescription.PrescriptionUploadRequest;

import java.util.List;

public interface PrescriptionService {

    PrescriptionResponseDTO uploadPrescription(String userEmail, PrescriptionUploadRequest request);

    PrescriptionResponseDTO getPrescriptionById(String userEmail, Long id);

    List<PrescriptionResponseDTO> getMyPrescriptions(String userEmail);

    List<PrescriptionResponseDTO> getAllPrescriptions(String status);

    PrescriptionResponseDTO updatePrescriptionStatus(Long id, PrescriptionStatusUpdateRequest request, String reviewerEmail);
}
