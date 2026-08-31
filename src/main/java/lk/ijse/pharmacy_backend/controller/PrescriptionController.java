package lk.ijse.pharmacy_backend.controller;

import jakarta.validation.Valid;
import lk.ijse.pharmacy_backend.dto.prescription.PrescriptionResponseDTO;
import lk.ijse.pharmacy_backend.dto.prescription.PrescriptionStatusUpdateRequest;
import lk.ijse.pharmacy_backend.dto.prescription.PrescriptionUploadRequest;
import lk.ijse.pharmacy_backend.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    public ResponseEntity<PrescriptionResponseDTO> uploadPrescription(
            Authentication authentication,
            @Valid @RequestBody PrescriptionUploadRequest request
    ) {
        PrescriptionResponseDTO response = prescriptionService.uploadPrescription(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-prescriptions")
    public ResponseEntity<List<PrescriptionResponseDTO>> getMyPrescriptions(Authentication authentication) {
        List<PrescriptionResponseDTO> prescriptions = prescriptionService.getMyPrescriptions(authentication.getName());
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponseDTO> getPrescriptionById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        PrescriptionResponseDTO prescription = prescriptionService.getPrescriptionById(authentication.getName(), id);
        return ResponseEntity.ok(prescription);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<PrescriptionResponseDTO>> getAllPrescriptions(
            @RequestParam(required = false, defaultValue = "ALL") String status
    ) {
        List<PrescriptionResponseDTO> list = prescriptionService.getAllPrescriptions(status);
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<PrescriptionResponseDTO> updatePrescriptionStatus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionStatusUpdateRequest request
    ) {
        PrescriptionResponseDTO updated = prescriptionService.updatePrescriptionStatus(id, request, authentication.getName());
        return ResponseEntity.ok(updated);
    }
}
