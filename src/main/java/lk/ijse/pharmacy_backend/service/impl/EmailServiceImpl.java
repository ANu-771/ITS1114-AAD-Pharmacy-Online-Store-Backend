package lk.ijse.pharmacy_backend.service.impl;

import jakarta.mail.internet.MimeMessage;
import lk.ijse.pharmacy_backend.entity.Order;
import lk.ijse.pharmacy_backend.entity.OrderItem;
import lk.ijse.pharmacy_backend.entity.Payment;
import lk.ijse.pharmacy_backend.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    @Autowired
    public EmailServiceImpl(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendOrderConfirmationReceipt(Order order, List<OrderItem> items, Payment payment) {
        if (mailSender == null || senderEmail == null || senderEmail.isBlank() || senderEmail.contains("yourpharmacy")) {
            log.warn("⚠️ [EmailService] Gmail SMTP credentials (spring.mail.username) not yet configured. Skipping email dispatch for order {}", order.getOrderNumber());
            return;
        }

        String recipientEmail = order.getCustomerEmail();
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("⚠️ [EmailService] Customer email is empty for order {}. Cannot send receipt.", order.getOrderNumber());
            return;
        }

        try {
            log.info("📧 [EmailService] Preparing confirmation receipt for order {} to {}", order.getOrderNumber(), recipientEmail);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

            helper.setFrom(String.format("KK PHARMACY <%s>", senderEmail));
            helper.setTo(recipientEmail.trim());
            helper.setSubject(String.format("🏥 KK PHARMACY — Order Confirmation & Receipt [#%s]", order.getOrderNumber()));

            String htmlBody = buildReceiptHtml(order, items, payment);
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);
            log.info("✅ [EmailService] Confirmation receipt successfully delivered to {} for order {}", recipientEmail, order.getOrderNumber());

        } catch (Exception e) {
            log.error("❌ [EmailService] Failed to send order receipt email for order {}: {}", order.getOrderNumber(), e.getMessage());
        }
    }

    /**
     * Builds a responsive, clinical-standard HTML email receipt with inline CSS styling.
     */
    private String buildReceiptHtml(Order order, List<OrderItem> items, Payment payment) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("en", "LK"));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a");

        String formattedDate = order.getCreatedAt() != null 
                ? order.getCreatedAt().format(dateFormatter) 
                : java.time.LocalDateTime.now().format(dateFormatter);

        String estDeliveryDate = order.getEstimatedDelivery() != null
                ? order.getEstimatedDelivery().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"))
                : "Within 48 Hours";

        // Payment status badge
        String paymentStatusText = (payment != null && payment.getStatus() != null) 
                ? payment.getStatus().name() 
                : "CONFIRMED";
        String paymentMethodText = (order.getPaymentMethod() != null) 
                ? order.getPaymentMethod() 
                : "Cash on Delivery";
        String txnRef = (payment != null && payment.getTransactionReference() != null) 
                ? payment.getTransactionReference() 
                : "N/A";

        // Build items table rows
        StringBuilder itemsRows = new StringBuilder();
        if (items != null) {
            for (OrderItem item : items) {
                String unitPrice = formatLKR(item.getUnitPrice());
                String itemSubtotal = formatLKR(item.getSubtotal());
                boolean isRx = item.getProduct() != null && item.getProduct().isRxRequired();
                String rxBadge = isRx 
                        ? "<span style=\"display:inline-block;background-color:#EBF8FF;color:#007791;font-size:11px;font-weight:700;padding:2px 6px;border-radius:4px;margin-left:6px;\">Rx Required</span>" 
                        : "";

                itemsRows.append(String.format("""
                    <tr style="border-bottom: 1px solid #E2E8F0;">
                        <td style="padding: 12px 8px; color: #1E293B; font-weight: 500;">
                            %s %s
                        </td>
                        <td style="padding: 12px 8px; text-align: center; color: #475569;">
                            %d
                        </td>
                        <td style="padding: 12px 8px; text-align: right; color: #475569;">
                            %s
                        </td>
                        <td style="padding: 12px 8px; text-align: right; color: #003B66; font-weight: 600;">
                            %s
                        </td>
                    </tr>
                """, item.getProductName(), rxBadge, item.getQuantity(), unitPrice, itemSubtotal));
            }
        }

        // Delivery fee display
        String deliveryText = (order.getDeliveryFee() == null || order.getDeliveryFee().compareTo(BigDecimal.ZERO) == 0)
                ? "<span style=\"color:#059669;font-weight:700;\">FREE</span>"
                : formatLKR(order.getDeliveryFee());

        String subtotalText = formatLKR(order.getSubtotal());
        String discountText = formatLKR(order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO);
        String grandTotalText = formatLKR(order.getTotalAmount());

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>KK PHARMACY Order Receipt</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #F1F5F9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; -webkit-font-smoothing: antialiased;">
                <div style="max-width: 640px; margin: 24px auto; background-color: #FFFFFF; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 59, 102, 0.08); border: 1px solid #E2E8F0;">
                    
                    <!-- Header -->
                    <div style="background: linear-gradient(135deg, #003B66 0%%, #001A33 100%%); padding: 32px 24px; text-align: center; color: #FFFFFF;">
                        <div style="font-size: 26px; font-weight: 800; letter-spacing: -0.5px; margin-bottom: 4px;">
                            🏥 KK PHARMACY
                        </div>
                        <div style="font-size: 13px; color: #93C5FD; text-transform: uppercase; letter-spacing: 1px; font-weight: 600;">
                            Certified Digital Healthcare Network • Sri Lanka
                        </div>
                    </div>

                    <!-- Confirmation Banner -->
                    <div style="background-color: #ECFDF5; border-bottom: 1px solid #A7F3D0; padding: 16px 24px; text-align: center;">
                        <div style="font-size: 16px; font-weight: 700; color: #065F46;">
                            ✅ Order Confirmed & Successfully Verified
                        </div>
                        <div style="font-size: 13px; color: #047857; margin-top: 4px;">
                            Thank you for your order, <strong>%s</strong>! Our clinical team is preparing your package.
                        </div>
                    </div>

                    <!-- Order Details Summary -->
                    <div style="padding: 24px;">
                        <table style="width: 100%%; border-collapse: collapse; margin-bottom: 24px; background-color: #F8FAFC; border-radius: 8px; border: 1px solid #E2E8F0;">
                            <tr>
                                <td style="padding: 12px 16px; font-size: 13px; color: #64748B;">
                                    <strong>Order Number:</strong><br>
                                    <span style="font-size: 15px; font-weight: 700; color: #003B66;">%s</span>
                                </td>
                                <td style="padding: 12px 16px; font-size: 13px; color: #64748B;">
                                    <strong>Order Placed:</strong><br>
                                    <span style="font-size: 14px; font-weight: 600; color: #1E293B;">%s</span>
                                </td>
                            </tr>
                            <tr style="border-top: 1px solid #E2E8F0;">
                                <td style="padding: 12px 16px; font-size: 13px; color: #64748B;">
                                    <strong>Live Tracking ID:</strong><br>
                                    <span style="font-size: 14px; font-weight: 600; color: #0284C7;">%s</span>
                                </td>
                                <td style="padding: 12px 16px; font-size: 13px; color: #64748B;">
                                    <strong>Est. Delivery:</strong><br>
                                    <span style="font-size: 14px; font-weight: 600; color: #059669;">%s</span>
                                </td>
                            </tr>
                        </table>

                        <!-- Delivery Address Card -->
                        <div style="margin-bottom: 24px; padding: 16px; background-color: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 8px;">
                            <div style="font-size: 12px; font-weight: 700; color: #64748B; text-transform: uppercase; margin-bottom: 6px;">
                                📍 Delivery Destination
                            </div>
                            <div style="font-size: 14px; color: #1E293B; line-height: 1.5;">
                                <strong>%s</strong><br>
                                %s
                            </div>
                        </div>

                        <!-- Itemized Table -->
                        <div style="font-size: 14px; font-weight: 700; color: #003B66; margin-bottom: 10px; text-transform: uppercase; letter-spacing: 0.5px;">
                            Prescription & Store Items
                        </div>
                        <table style="width: 100%%; border-collapse: collapse; margin-bottom: 24px; font-size: 14px;">
                            <thead>
                                <tr style="background-color: #F1F5F9; border-bottom: 2px solid #CBD5E1; text-align: left;">
                                    <th style="padding: 10px 8px; color: #475569; font-size: 12px; text-transform: uppercase;">Product</th>
                                    <th style="padding: 10px 8px; color: #475569; font-size: 12px; text-transform: uppercase; text-align: center;">Qty</th>
                                    <th style="padding: 10px 8px; color: #475569; font-size: 12px; text-transform: uppercase; text-align: right;">Unit Price</th>
                                    <th style="padding: 10px 8px; color: #475569; font-size: 12px; text-transform: uppercase; text-align: right;">Total</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>

                        <!-- Cost Breakdown Summary -->
                        <div style="margin-left: auto; max-width: 320px; margin-bottom: 24px;">
                            <table style="width: 100%%; border-collapse: collapse; font-size: 14px;">
                                <tr>
                                    <td style="padding: 6px 0; color: #64748B;">Subtotal:</td>
                                    <td style="padding: 6px 0; text-align: right; color: #1E293B; font-weight: 500;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 6px 0; color: #64748B;">Delivery Fee:</td>
                                    <td style="padding: 6px 0; text-align: right;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 6px 0; color: #64748B;">Discount:</td>
                                    <td style="padding: 6px 0; text-align: right; color: #059669;">-%s</td>
                                </tr>
                                <tr style="border-top: 2px solid #E2E8F0;">
                                    <td style="padding: 12px 0; font-size: 16px; font-weight: 800; color: #003B66;">Total Paid:</td>
                                    <td style="padding: 12px 0; text-align: right; font-size: 18px; font-weight: 800; color: #003B66;">%s</td>
                                </tr>
                            </table>
                        </div>

                        <!-- Payment & Guarantee Badge -->
                        <div style="background-color: #F8FAFC; border: 1px dashed #CBD5E1; border-radius: 8px; padding: 14px 16px; margin-bottom: 24px;">
                            <table style="width: 100%%; font-size: 13px;">
                                <tr>
                                    <td style="color: #64748B;">Payment Method: <strong>%s</strong></td>
                                    <td style="text-align: right; color: #64748B;">Status: <span style="background-color: #DEF7EC; color: #03543F; font-weight: 700; padding: 2px 8px; border-radius: 4px; font-size: 12px;">%s</span></td>
                                </tr>
                                <tr>
                                    <td style="color: #64748B; padding-top: 4px;" colspan="2">Transaction Reference: <code style="background-color: #E2E8F0; padding: 2px 6px; border-radius: 4px; color: #0F172A;">%s</code></td>
                                </tr>
                            </table>
                        </div>

                        <!-- Clinical Assurance Card -->
                        <div style="background-color: #F0FDF4; border-left: 4px solid #10B981; padding: 12px 16px; border-radius: 4px; font-size: 12px; color: #166534; line-height: 1.5; margin-bottom: 24px;">
                            <strong>🛡️ Certified Cold-Chain & Clinical Packaging:</strong> All pharmaceutical orders are packed under strict temperature-controlled standards. If this order includes prescription drugs (Rx), verification was conducted by our registered clinical pharmacist.
                        </div>

                        <!-- Support Contacts -->
                        <div style="text-align: center; border-top: 1px solid #E2E8F0; padding-top: 20px;">
                            <div style="font-size: 13px; color: #64748B; margin-bottom: 6px;">
                                Questions about your medication or delivery?
                            </div>
                            <div style="font-size: 14px; font-weight: 700; color: #003B66;">
                                📞 Hotline: +94 11 234 5678 &nbsp;•&nbsp; ✉️ support@kkpharmacy.com
                            </div>
                            <div style="font-size: 11px; color: #94A3B8; margin-top: 8px;">
                                KK PHARMACY • No. 120, Galle Road, Colombo 03, Sri Lanka<br>
                                Licensed by the National Medicines Regulatory Authority (NMRA)
                            </div>
                        </div>

                    </div>
                </div>
            </body>
            </html>
        """,
        order.getCustomerName() != null ? order.getCustomerName() : "Valued Patient",
        order.getOrderNumber(),
        formattedDate,
        order.getTrackingId() != null ? order.getTrackingId() : "Pending",
        estDeliveryDate,
        order.getCustomerName() != null ? order.getCustomerName() : "Customer",
        order.getShippingAddress(),
        itemsRows.toString(),
        subtotalText,
        deliveryText,
        discountText,
        grandTotalText,
        paymentMethodText,
        paymentStatusText,
        txnRef
        );
    }

    private String formatLKR(BigDecimal amount) {
        if (amount == null) return "Rs. 0.00";
        return String.format("Rs. %,.2f", amount);
    }
}
