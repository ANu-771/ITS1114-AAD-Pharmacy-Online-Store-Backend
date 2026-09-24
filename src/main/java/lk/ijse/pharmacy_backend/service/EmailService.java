package lk.ijse.pharmacy_backend.service;

import lk.ijse.pharmacy_backend.entity.Order;
import lk.ijse.pharmacy_backend.entity.OrderItem;
import lk.ijse.pharmacy_backend.entity.Payment;

import java.util.List;

/**
 * Service for sending transactional emails (Order receipts, confirmations, alerts).
 */
public interface EmailService {

    /**
     * Asynchronously dispatches a branded, clinical order confirmation receipt to the customer's email.
     *
     * @param order   The saved order entity
     * @param items   The list of order items
     * @param payment The associated payment entity
     */
    void sendOrderConfirmationReceipt(Order order, List<OrderItem> items, Payment payment);

    /**
     * Asynchronously dispatches a secure, 6-digit password reset OTP email to the user.
     *
     * @param toEmail       The recipient email address
     * @param otpCode       The 6-digit one-time password
     * @param recipientName The recipient full name or username
     */
    void sendPasswordResetOtp(String toEmail, String otpCode, String recipientName);
}
