package com.medgo.reimburse.service;

import com.medgo.reimburse.domain.dto.EmailRequest;
import com.medgo.reimburse.domain.dto.ReimbursementSubmissionResponse;
import com.medgo.reimburse.feign.NotificationServiceClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReimbursementNotificationService {

    private final NotificationServiceClient notificationServiceClient;

    @Value("${reimbursement.notification.admin.email}")
    private String adminEmail;
    
    @Value("${reimbursement.notification.email.image.path:images/reimbursement-success.jpg}")
    private String emailImagePath;

    private static final ThreadLocal<String> DEVICE_ID_CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<String> APPLICATION_ID_CONTEXT = new ThreadLocal<>();

    private static final ConcurrentHashMap<String, Long> sentEmails = new ConcurrentHashMap<>();
    private static final long EMAIL_DEDUPLICATION_WINDOW_MINUTES = 5; // Prevent duplicates within 5 minutes
    private static final Object LOCK = new Object(); // Lock for idempotency check

    public static String getDeviceIdFromContext() {
        return DEVICE_ID_CONTEXT.get();
    }

    public static String getApplicationIdFromContext() {
        return APPLICATION_ID_CONTEXT.get();
    }

    public void sendControlCodeEmail(ReimbursementSubmissionResponse response, String deviceId, String applicationId, String userEmail) {
        final String controlCode;
        try {
            controlCode = response != null ? response.getControlCode() : null;
            
            if (controlCode == null || controlCode.isBlank()) {
                log.warn("Control code is null or blank, skipping email notification");
                return;
            }

            long currentTime = System.currentTimeMillis();
            boolean shouldSend = false;
            
            synchronized (LOCK) {
                Long lastSentTime = sentEmails.get(controlCode);
                
                if (lastSentTime != null) {
                    long timeSinceLastSent = currentTime - lastSentTime;
                    long windowMillis = TimeUnit.MINUTES.toMillis(EMAIL_DEDUPLICATION_WINDOW_MINUTES);
                    
                    if (timeSinceLastSent < windowMillis) {
                        log.warn("Email already sent for control code {} {} seconds ago. Skipping duplicate send to prevent multiple emails.", 
                                controlCode, timeSinceLastSent / 1000);
                        return;
                    } else {
                        sentEmails.remove(controlCode);
                    }
                }

                Long existing = sentEmails.putIfAbsent(controlCode, currentTime);
                if (existing != null) {
                    log.warn("Another thread is already sending email for control code {}. Skipping duplicate send.", controlCode);
                    return;
                }
                
                shouldSend = true;
            }
            
            if (!shouldSend) {
                return;
            }
            
            log.info("Sending email notification for control code: {} (user: {}, admin: {})", 
                    controlCode, userEmail != null ? userEmail : "N/A", adminEmail);
            
            String subject = "Reimbursement Request Submitted - Control Code: " + controlCode;

            String adminBody = buildEmailBody(controlCode, response, true);
            String userBody = buildEmailBody(controlCode, response, false);

            try {
                
                log.info("Sending email to admin/reimbursement system: {} for control code: {} (with bank details)", adminEmail, controlCode);
                sendEmailToRecipient(adminEmail, subject, adminBody, controlCode, "admin");
                log.info("Admin email sent successfully to: {} for control code: {}", adminEmail, controlCode);

                if (userEmail != null && !userEmail.isBlank() && isValidEmail(userEmail) && !userEmail.equalsIgnoreCase(adminEmail)) {
                    log.info("Sending email to user: {} for control code: {} (without bank details)", userEmail, controlCode);
                    sendEmailToRecipient(userEmail, subject, userBody, controlCode, "user");
                    log.info("User email sent successfully to: {} for control code: {}", userEmail, controlCode);
                } else {
                    log.debug("Skipping user email - user email is null, blank, invalid, or same as admin email for control code: {}", controlCode);
                }
                
                log.info("Successfully sent email notification(s) for control code: {} (admin: {}, user: {})", 
                        controlCode, adminEmail, userEmail != null && !userEmail.equalsIgnoreCase(adminEmail) ? userEmail : "N/A");
            } catch (Exception e) {
                log.error("Error occurred while sending emails for control code {}: {}", controlCode, e.getMessage(), e);
                synchronized (LOCK) {
                    sentEmails.remove(controlCode);
                }
            }

        } catch (Exception e) {
            log.error("Failed to send control code email notification: {}", e.getMessage(), e);
            String errorControlCode = response != null && response.getControlCode() != null ? response.getControlCode() : null;
            if (errorControlCode != null) {
                synchronized (LOCK) {
                    sentEmails.remove(errorControlCode);
                }
            }
        }
    }

    private void sendEmailToRecipient(String recipientEmail, String subject, String body, String controlCode, String recipientType) {
        try {
            if (recipientEmail == null || recipientEmail.isBlank()) {
                return;
            }
            
            if (!isValidEmail(recipientEmail)) {
                return;
            }
            
            List<String> singleRecipient = new ArrayList<>();
            singleRecipient.add(recipientEmail);
            
            EmailRequest emailRequest = EmailRequest.builder()
                    .subject(subject)
                    .body(body)
                    .toEmails(singleRecipient)
                    .type("HTML")
                    .contentType("HTML")
                    .build();
            
            try {
                log.debug("Calling email service to send email to {} ({}) for control code: {}", recipientType, recipientEmail, controlCode);
                ResponseEntity<Map<String, Object>> emailResponseEntity = notificationServiceClient.sendEmail(emailRequest);
                
                if (emailResponseEntity != null && emailResponseEntity.getStatusCode().is2xxSuccessful()) {
                    Map<String, Object> emailResponseBody = emailResponseEntity.getBody();
                    if (emailResponseBody != null) {
                        Object responseStatusObj = emailResponseBody.get("response");
                        String responseStatus = responseStatusObj != null ? responseStatusObj.toString() : null;
                        if ("ERROR".equals(responseStatus)) {
                            log.error("Email service returned error response for {} ({}): {}", 
                                    recipientType, recipientEmail, emailResponseBody);
                            Object dataObj = emailResponseBody.get("data");
                            if (dataObj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> errorData = (Map<String, Object>) dataObj;
                                log.error("Error details for {} - Code: {}, Message: {}", 
                                        recipientType, errorData.get("errorCode"), errorData.get("message"));
                            }
                        } else {
                            log.info("Email service confirmed successful delivery to {} ({}) for control code: {}", 
                                    recipientType, recipientEmail, controlCode);
                        }
                    } else {
                        log.info("Email sent to {} ({}) for control code: {} - HTTP status: {}", 
                                recipientType, recipientEmail, controlCode, emailResponseEntity.getStatusCode());
                    }
                } else {
                    log.warn("Email service returned non-2xx status for {} ({}): {}", 
                            recipientType, recipientEmail, 
                            emailResponseEntity != null ? emailResponseEntity.getStatusCode() : "null response");
                }
            } catch (feign.codec.DecodeException e) {
                log.error("Failed to decode email service response for {} ({}): {}", 
                        recipientType, recipientEmail, e.getMessage(), e);
            } catch (FeignException e) {
                log.error("Feign error sending email to {} ({}). Status: {}, Body: {}", 
                        recipientType, recipientEmail, e.status(), e.contentUTF8(), e);
            } catch (Exception e) {
                log.error("Unexpected error sending email to {} ({}): {}", 
                        recipientType, recipientEmail, e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Error preparing email for {} ({}): {}", recipientType, recipientEmail, e.getMessage(), e);
        }
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }

    private String buildEmailBody(String controlCode, ReimbursementSubmissionResponse response, boolean includeBankDetails) {
        try {
            String imageBase64 = loadImageAsBase64();
            String imageMimeType = getImageMimeType();
            log.debug("Using base64 embedded image: {} bytes", imageBase64.length());

            return buildEmailBodyWithImage(controlCode, imageBase64, imageMimeType, response, includeBankDetails);
        } catch (Exception e) {
            log.warn("Failed to load image from resources, using HTML fallback: {}", e.getMessage());
            return buildEmailBodyHtmlOnly(controlCode, response, includeBankDetails);
        }
    }
    
    private String buildEmailBodyWithImage(String controlCode, String imageBase64, String imageMimeType, 
                                           ReimbursementSubmissionResponse response, boolean includeBankDetails) {
        String bankDetailsSection = includeBankDetails ? buildBankDetailsSection(response) : "";
        
        return "<!DOCTYPE html>" +
                "<html xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">" +
                "<head>" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<!--[if mso]>" +
                "<style type=\"text/css\">" +
                "body, table, td {font-family: Arial, Helvetica, sans-serif !important;}" +
                "</style>" +
                "<![endif]-->" +
                "</head>" +
                "<body style=\"margin: 0; padding: 0; font-family: Arial, Helvetica, sans-serif; background-color: #f5f5f5;\">" +
                "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color: #f5f5f5;\">" +
                "<tr>" +
                "<td align=\"center\" style=\"padding: 20px 0;\">" +
                "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);\">" +
                "<!-- Image Section -->" +
                "<tr>" +
                "<td align=\"center\" style=\"padding: 0;\">" +
                "<img src=\"data:" + imageMimeType + ";base64," + imageBase64 + "\" alt=\"Reimbursement Success\" width=\"600\" style=\"width: 100%; max-width: 600px; height: auto; display: block; border: 0; border-radius: 8px 8px 0 0;\" />" +
                "</td>" +
                "</tr>" +
                "<!-- Control Code Section - displayed prominently below image -->" +
                (includeBankDetails ? "<!-- Control Code Text for Admin - Control code beside message -->" +
                "<tr>" +
                "<td align=\"center\" style=\"padding: 30px 20px; background-color: #ffffff;\">" +
                "<p style=\"margin: 0; color: #333333; font-size: 16px; font-family: Arial, Helvetica, sans-serif;\">Your ControlCode is: <span style=\"color: #667eea; font-weight: bold;\">" + escapeHtml(controlCode) + "</span></p>" +
                "</td>" +
                "</tr>" : 
                "<!-- Control Code Section for User Email -->" +
                "<tr>" +
                "<td align=\"center\" style=\"padding: 30px 20px; background-color: #ffffff;\">" +
                "<p style=\"margin: 0; color: #667eea; font-size: 28px; font-weight: bold; letter-spacing: 2px; font-family: Arial, Helvetica, sans-serif;\">" + controlCode + "</p>" +
                "</td>" +
                "</tr>") +
                bankDetailsSection +
                "<!-- Disclaimer Section -->" +
                "<tr>" +
                "<td align=\"center\" style=\"padding: 0 20px 20px 20px;\">" +
                "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
                "<tr>" +
                "<td style=\"padding: 20px 30px; background-color: #f8f9fa; border-top: 1px solid #e9ecef; border-radius: 0 0 8px 8px;\">" +
                "<p style=\"margin: 0 0 10px 0; color: #666666; font-size: 11px; line-height: 1.5; font-family: Arial, Helvetica, sans-serif;\">" +
                "<strong>Disclaimer:</strong> This email and any attachments are confidential and may be legally privileged. " +
                "If you are not the intended recipient, you are hereby notified that any disclosure, copying, distribution or " +
                "taking any action in reliance on the contents of this information is strictly prohibited and may be unlawful. " +
                "If you have received this email in error, please notify the sender immediately and delete this email from your system." +
                "</p>" +
                "<p style=\"margin: 10px 0 0 0; color: #666666; font-size: 11px; line-height: 1.5; font-family: Arial, Helvetica, sans-serif;\">" +
                "For more information, please visit: <a href=\"https://www.medicardphils.com/privacy-statement/\" style=\"color: #667eea; text-decoration: underline;\">https://www.medicardphils.com/privacy-statement/</a>" +
                "</p>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</body>" +
                "</html>";
    }
    
    private String buildBankDetailsSection(ReimbursementSubmissionResponse response) {
        if (response == null) {
            return "";
        }
        
        String bankName = response.getBankName();
        String fullAccountName = response.getFullAccountName();
        String bankAccountNumber = response.getBankAccountNumber();

        if (bankName == null || bankName.isBlank()) {
            return "";
        }
        
        return "<!-- Bank Details Section - for reimbursement system/admin only -->" +
                "<tr>" +
                "<td align=\"center\" style=\"padding: 0 20px 20px 20px;\">" +
                "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); border: 1px solid #e9ecef;\">" +
                "<tr>" +
                "<td style=\"padding: 20px 30px;\">" +
                "<p style=\"margin: 0 0 15px 0; color: #333333; font-size: 14px; line-height: 1.6; font-family: Arial, Helvetica, sans-serif;\">The bank account details you provided for e-payout is:</p>" +
                "<table width=\"100%\" cellpadding=\"5\" cellspacing=\"0\" border=\"0\">" +
                "<tr>" +
                "<td style=\"padding: 8px 0; color: #666666; font-size: 14px; font-family: Arial, Helvetica, sans-serif; width: 40%;\"><strong>Bank Name:</strong></td>" +
                "<td style=\"padding: 8px 0; color: #333333; font-size: 14px; font-family: Arial, Helvetica, sans-serif;\">" + escapeHtml(bankName) + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td style=\"padding: 8px 0; color: #666666; font-size: 14px; font-family: Arial, Helvetica, sans-serif;\"><strong>Account Name:</strong></td>" +
                "<td style=\"padding: 8px 0; color: #333333; font-size: 14px; font-family: Arial, Helvetica, sans-serif;\">" + escapeHtml(fullAccountName != null && !fullAccountName.isBlank() ? fullAccountName : "N/A") + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td style=\"padding: 8px 0; color: #666666; font-size: 14px; font-family: Arial, Helvetica, sans-serif;\"><strong>Account Number:</strong></td>" +
                "<td style=\"padding: 8px 0; color: #333333; font-size: 14px; font-family: Arial, Helvetica, sans-serif;\">" + escapeHtml(bankAccountNumber != null && !bankAccountNumber.isBlank() ? bankAccountNumber : "N/A") + "</td>" +
                "</tr>" +
                "</table>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</td>" +
                "</tr>";
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    private String buildEmailBodyHtmlOnly(String controlCode, ReimbursementSubmissionResponse response, boolean includeBankDetails) {
        return "<!DOCTYPE html>" +
                "<html xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">" +
                "<head>" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" +
                "<!--[if mso]>" +
                "<style type=\"text/css\">" +
                "body, table, td {font-family: Arial, Helvetica, sans-serif !important;}" +
                "</style>" +
                "<![endif]-->" +
                "</head>" +
                "<body style=\"margin: 0; padding: 0; font-family: Arial, Helvetica, sans-serif; background-color: #f5f5f5;\">" +
                "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color: #f5f5f5;\">" +
                "<tr>" +
                "<td align=\"center\" style=\"padding: 40px 20px;\">" +
                "<!--[if mso]>" +
                "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color: #764ba2;\">" +
                "<![endif]-->" +
                "<!--[if !mso]><!-->" +
                "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; background-color: #764ba2;\">" +
                "<!--<![endif]-->" +
                "<!-- Header Section with Purple Background -->" +
                "<tr>" +
                "<td align=\"center\" style=\"padding: 60px 40px 40px 40px; background-color: #764ba2;\">" +
                "<!--[if mso]>" +
                "<v:rect xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:w=\"urn:schemas-microsoft-com:office:word\" fill=\"t\" stroked=\"f\" style=\"position:absolute;width:600px;height:200px;z-index:-1;\">" +
                "<v:fill type=\"gradient\" color=\"#764ba2\" color2=\"#667eea\" angle=\"135\" />" +
                "</v:rect>" +
                "<![endif]-->" +
                "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
                "<tr>" +
                "<td align=\"center\" style=\"padding-bottom: 30px;\">" +
                "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
                "<tr>" +
                "<td align=\"center\" style=\"width: 140px; height: 140px; background-color: rgba(255,255,255,0.25); border-radius: 70px; line-height: 140px; text-align: center; vertical-align: middle;\">" +
                "<span style=\"font-size: 80px; color: #ffffff; display: inline-block; vertical-align: middle;\">👤</span>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</td>" +
                "</tr>" +
                "<tr>" +
                "<td align=\"center\">" +
                "<h1 style=\"margin: 0; color: #ffffff; font-size: 32px; font-weight: bold; text-transform: uppercase; letter-spacing: 1px; font-family: Arial, Helvetica, sans-serif;\">REQUEST SUCCESSFULLY SUBMITTED!</h1>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</td>" +
                "</tr>" +
                "<!-- Content Section -->" +
                "<tr>" +
                "<td align=\"center\" style=\"padding: 40px 40px 50px 40px; background-color: #764ba2;\">" +
                "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
                "<tr>" +
                "<td align=\"center\" style=\"padding-bottom: 30px;\">" +
                "<p style=\"margin: 0; color: #ffffff; font-size: 16px; line-height: 1.6; font-family: Arial, Helvetica, sans-serif;\">PROCESSING MAY TAKE UP TO 15 BUSINESS DAYS</p>" +
                "</td>" +
                "</tr>" +
                (includeBankDetails ? "<!-- Control Code Text for Admin - Control code beside message -->" +
                "<tr>" +
                "<td align=\"center\" style=\"padding-top: 30px; padding-bottom: 10px;\">" +
                "<p style=\"margin: 0; color: #ffffff; font-size: 16px; font-family: Arial, Helvetica, sans-serif;\">Your ControlCode is: <span style=\"font-weight: bold;\">" + escapeHtml(controlCode) + "</span></p>" +
                "</td>" +
                "</tr>" : 
                "<!-- Control Code Section for User Email -->" +
                "<tr>" +
                "<td align=\"center\" style=\"padding-top: 30px;\">" +
                "<p style=\"margin: 0 0 8px 0; color: #ffffff; font-size: 14px; font-weight: 600; letter-spacing: 0.5px; text-transform: uppercase; font-family: Arial, Helvetica, sans-serif;\">CONTROL CODE</p>" +
                "<p style=\"margin: 0; color: #ffffff; font-size: 28px; font-weight: bold; letter-spacing: 2px; font-family: Arial, Helvetica, sans-serif;\">" + controlCode + "</p>" +
                "</td>" +
                "</tr>") +
                "</table>" +
                "</td>" +
                "</tr>" +
                "<!-- Footer with Logos -->" +
                "<tr>" +
                "<td align=\"center\" style=\"padding: 40px 40px 50px 40px; background-color: #764ba2; border-top: 1px solid rgba(255,255,255,0.2);\">" +
                "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
                "<tr>" +
                "<td align=\"center\" width=\"50%\">" +
                "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin: 0 auto;\">" +
                "<tr>" +
                "<td style=\"padding: 10px 20px; background-color: rgba(255,255,255,0.2); border-radius: 4px;\">" +
                "<span style=\"color: #ffffff; font-size: 18px; font-weight: 600; font-family: Arial, Helvetica, sans-serif;\">go</span>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</td>" +
                "<td align=\"center\" width=\"50%\">" +
                "<p style=\"margin: 0; color: #ffffff; font-size: 16px; font-weight: 600; font-family: Arial, Helvetica, sans-serif;\">MediCard</p>" +
                "<p style=\"margin: 5px 0 0 0; color: rgba(255,255,255,0.9); font-size: 12px; font-family: Arial, Helvetica, sans-serif;\">An AIA Company</p>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                (includeBankDetails ? buildBankDetailsSectionForHtmlOnly(response) : "") +
                "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color: #f5f5f5;\">" +
                "<tr>" +
                "<td align=\"center\" style=\"padding: 0 20px 20px 20px;\">" +
                "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px;\">" +
                "<!-- Disclaimer Section -->" +
                "<tr>" +
                "<td style=\"padding: 20px 30px; background-color: #f8f9fa; border-top: 1px solid #e9ecef;\">" +
                "<p style=\"margin: 0 0 10px 0; color: #666666; font-size: 11px; line-height: 1.5; font-family: Arial, Helvetica, sans-serif;\">" +
                "<strong>Disclaimer:</strong> This email and any attachments are confidential and may be legally privileged. " +
                "If you are not the intended recipient, you are hereby notified that any disclosure, copying, distribution or " +
                "taking any action in reliance on the contents of this information is strictly prohibited and may be unlawful. " +
                "If you have received this email in error, please notify the sender immediately and delete this email from your system." +
                "</p>" +
                "<p style=\"margin: 10px 0 0 0; color: #666666; font-size: 11px; line-height: 1.5; font-family: Arial, Helvetica, sans-serif;\">" +
                "For more information, please visit: <a href=\"https://www.medicardphils.com/privacy-statement/\" style=\"color: #667eea; text-decoration: underline;\">https://www.medicardphils.com/privacy-statement/</a>" +
                "</p>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</body>" +
                "</html>";
    }
    
    private String buildBankDetailsSectionForHtmlOnly(ReimbursementSubmissionResponse response) {
        if (response == null) {
            return "";
        }
        
        String bankName = response.getBankName();
        String fullAccountName = response.getFullAccountName();
        String bankAccountNumber = response.getBankAccountNumber();

        if (bankName == null || bankName.isBlank()) {
            return "";
        }
        
        return "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color: #f5f5f5;\">" +
                "<tr>" +
                "<td align=\"center\" style=\"padding: 0 20px 20px 20px;\">" +
                "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); border: 1px solid #e9ecef;\">" +
                "<tr>" +
                "<td style=\"padding: 20px 30px;\">" +
                "<p style=\"margin: 0 0 15px 0; color: #333333; font-size: 14px; line-height: 1.6; font-family: Arial, Helvetica, sans-serif;\">The bank account details you provided for e-payout is:</p>" +
                "<table width=\"100%\" cellpadding=\"5\" cellspacing=\"0\" border=\"0\">" +
                "<tr>" +
                "<td style=\"padding: 8px 0; color: #666666; font-size: 14px; font-family: Arial, Helvetica, sans-serif; width: 40%;\"><strong>Bank Name:</strong></td>" +
                "<td style=\"padding: 8px 0; color: #333333; font-size: 14px; font-family: Arial, Helvetica, sans-serif;\">" + escapeHtml(bankName) + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td style=\"padding: 8px 0; color: #666666; font-size: 14px; font-family: Arial, Helvetica, sans-serif;\"><strong>Account Name:</strong></td>" +
                "<td style=\"padding: 8px 0; color: #333333; font-size: 14px; font-family: Arial, Helvetica, sans-serif;\">" + escapeHtml(fullAccountName != null && !fullAccountName.isBlank() ? fullAccountName : "N/A") + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td style=\"padding: 8px 0; color: #666666; font-size: 14px; font-family: Arial, Helvetica, sans-serif;\"><strong>Account Number:</strong></td>" +
                "<td style=\"padding: 8px 0; color: #333333; font-size: 14px; font-family: Arial, Helvetica, sans-serif;\">" + escapeHtml(bankAccountNumber != null && !bankAccountNumber.isBlank() ? bankAccountNumber : "N/A") + "</td>" +
                "</tr>" +
                "</table>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</td>" +
                "</tr>" +
                "</table>";
    }
    
    private String loadImageAsBase64() throws IOException {
        try {
            Resource resource = new ClassPathResource(emailImagePath);
            if (!resource.exists()) {
                log.error("Email image not found at path: {}. Please ensure the image exists at src/main/resources/{}", emailImagePath, emailImagePath);
                throw new IOException("Image resource not found: " + emailImagePath);
            }
            byte[] imageBytes = StreamUtils.copyToByteArray(resource.getInputStream());
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            log.info("Successfully loaded image from {} - Size: {} bytes, Base64 length: {}", 
                    emailImagePath, imageBytes.length, base64.length());
            return base64;
        } catch (IOException e) {
            log.error("Error loading image from resources at path {}: {}", emailImagePath, e.getMessage(), e);
            throw e;
        }
    }
    
    private String getImageMimeType() {
        String path = emailImagePath.toLowerCase();
        if (path.endsWith(".png")) {
            return "image/png";
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (path.endsWith(".gif")) {
            return "image/gif";
        } else if (path.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/png"; // default
    }
}

