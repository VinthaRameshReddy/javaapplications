package com.medicard.integration.service;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.medicard.integration.domain.request.EmailRequest;
import com.medicard.integration.domain.request.EmailStatus;
import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.kiota.ApiException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class GraphEmailService {

    private final EmailAuditService emailAuditService;
    // 1. Inject AzureKeyVaultService
    private final AzureKeyVaultService azureKeyVaultService;
    private GraphEmailService self; // For self-injection to handle AOP proxying

    // 2. Remove the @Value fields. They will be fetched dynamically.
    private String senderObjectId;
    private GraphServiceClient graphClient;

    // 3. Update the constructor to include AzureKeyVaultService
    public GraphEmailService(EmailAuditService emailAuditService, AzureKeyVaultService azureKeyVaultService) {
        this.emailAuditService = emailAuditService;
        this.azureKeyVaultService = azureKeyVaultService;
    }

    @Autowired
    public void setSelf(@Lazy GraphEmailService self) {
        this.self = self;
    }

    @PostConstruct
    public void init() {
        log.debug("Initializing GraphEmailService...");

        // 4. Fetch the secrets from Azure Key Vault
        String clientId = azureKeyVaultService.getSecretValue("azure-client-id");
        String clientSecret = azureKeyVaultService.getSecretValue("azure-client-secret");
        String tenantId = azureKeyVaultService.getSecretValue("azure-tenant-id");
        this.senderObjectId = azureKeyVaultService.getSecretValue("azure-object-id");

        // Use the fetched secrets to build the credential
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(clientId).clientSecret(clientSecret).tenantId(tenantId).build();

        String[] scopes = {"https://graph.microsoft.com/.default"};
        graphClient = new GraphServiceClient(credential, scopes);
        log.info("GraphEmailService initialized successfully.");
    }

    /**
     * Public entry point for sending an email. This method sets up the context
     * (like a correlation ID and attempt counter) before calling the retryable method.
     */
    public void sendEmail(EmailRequest emailRequest) {
        UUID correlationId = UUID.randomUUID();
        AtomicInteger attemptCounter = new AtomicInteger(0);
        log.info("[{}] Received request to send email with subject: '{}'", correlationId, emailRequest.getSubject());

        // CRITICAL: Call the method on the 'self' proxy, not 'this'.
        // This ensures the @Retryable AOP advice is triggered.
        self.attemptSend(emailRequest, correlationId, attemptCounter);
    }

    /**
     * This method contains the core logic for sending an email and is decorated
     * with @Retryable to handle transient failures automatically.
     * It should not be called directly from outside the class.
     */
    @Retryable(
            retryFor = {ApiException.class},
            maxAttempts = 4, // 1 initial attempt + 3 retries
            backoff = @Backoff(delay = 3000, multiplier = 2) // Wait 3s, then 6s, then 12s
    )
    public void attemptSend(EmailRequest emailRequest, UUID correlationId, AtomicInteger attemptCounter) {
        int currentAttempt = attemptCounter.incrementAndGet();
        String logPrefix = String.format("[%s][Attempt %d]", correlationId, currentAttempt);
        EmailStatus status = (currentAttempt == 1) ? EmailStatus.ATTEMPTING : EmailStatus.RETRYING;

        try {
            emailAuditService.logStatus(correlationId, currentAttempt, status, "Attempting to send email via Graph API.", emailRequest);
            log.info("{} Sending email...", logPrefix);

            Message message = createMessageFromRequest(emailRequest);
            var requestBody = new com.microsoft.graph.users.item.sendmail.SendMailPostRequestBody();
            requestBody.setMessage(message);
            requestBody.setSaveToSentItems(true);
            graphClient.users().byUserId(senderObjectId).sendMail().post(requestBody);

            log.info("{} Email sent successfully.", logPrefix);
            emailAuditService.logStatus(correlationId, currentAttempt, EmailStatus.SUCCESS, "Graph API accepted the request (202 Accepted).", emailRequest);

        } catch (ApiException e) {
            String errorDetails = String.format("API Error. Status: %d, Message: %s", e.getResponseStatusCode(), e.getMessage());
            log.warn("{} Failed to send email. {}", logPrefix, errorDetails);

            if (isTransientError(e)) {
                // Re-throw the exception to allow Spring Retry to handle the next attempt.
                throw e;
            } else {
                // This is a non-retriable client error (e.g., 400 Bad Request).
                // We log it and re-throw it to stop the retry cycle and trigger the @Recover method.
                emailAuditService.logStatus(correlationId, currentAttempt, EmailStatus.FAILED, "Non-retriable error: " + errorDetails, emailRequest);
                throw e;
            }
        }
    }

    /**
     * This "recovery" method is called by Spring Retry ONLY when the @Retryable method
     * has failed for all its attempts or failed with a non-retriable exception.
     */
    @Recover
    private void recoverFromFailure(ApiException e, EmailRequest emailRequest, UUID correlationId, AtomicInteger attemptCounter) {
        int finalAttempt = attemptCounter.get();
        String logPrefix = String.format("[%s][Attempt %d]", correlationId, finalAttempt);

        // Check if the final failure was already logged. If it was a non-transient error,
        // the FAILED status was logged in the catch block of attemptSend.
        // If all retries were exhausted on a transient error, we need to log it here.
        if (isTransientError(e)) {
            String errorDetails = String.format("Final failure after %d attempts. Status: %d, Message: %s", finalAttempt, e.getResponseStatusCode(), e.getMessage());
            log.error("{} Could not send email. All transient retries failed. {}", logPrefix, errorDetails);
            emailAuditService.logStatus(correlationId, finalAttempt, EmailStatus.FAILED, "All retries exhausted. " + errorDetails, emailRequest);
        } else {
            log.error("{} Could not send email. A non-retriable error occurred. See previous log for details.", logPrefix);
        }

        // This is the "Dead-Letter Queue" location.
        // You can add logic here to send a notification to an admin or raise an alert.
    }

    /**
     * Checks if the given ApiException represents a transient error that is safe to retry.
     */
    private boolean isTransientError(ApiException e) {
        int statusCode = e.getResponseStatusCode();
        // Retry on 429 (Too Many Requests) and all 5xx server errors.
        return statusCode == 429 || (statusCode >= 500 && statusCode < 600);
    }

    // --- Helper methods to build the email message ---
    private Message createMessageFromRequest(EmailRequest emailRequest) {
        Message message = new Message();
        message.setSubject(emailRequest.getSubject());

        // Set body
        String contentType = emailRequest.getContentType();
        BodyType bodyType = Objects.equals(contentType, "HTML") ? BodyType.Html : BodyType.Text;
        ItemBody body = new ItemBody();
        body.setContentType(bodyType);
        body.setContent(emailRequest.getBody());
        message.setBody(body);

        // Set recipients
        message.setToRecipients(getRecipients(emailRequest.getToEmails()));
        if (emailRequest.getCcEmails() != null && !emailRequest.getCcEmails().isEmpty()) {
            message.setCcRecipients(getRecipients(emailRequest.getCcEmails()));
        }
        if (emailRequest.getBccEmails() != null && !emailRequest.getBccEmails().isEmpty()) {
            message.setBccRecipients(getRecipients(emailRequest.getBccEmails()));
        }

        // --- NEW LOGIC TO PROCESS ATTACHMENTS ---
        if (emailRequest.getAttachments() != null && !emailRequest.getAttachments().isEmpty()) {
            List<Attachment> attachmentList = new ArrayList<>();
            for (EmailRequest.AttachmentRequest attReq : emailRequest.getAttachments()) {
                log.debug("Processing attachment: {}", attReq.getFilename());

                // Decode the Base64 string back into a byte array
                byte[] fileContent = Base64.getDecoder().decode(attReq.getContent());

                FileAttachment fileAttachment = new FileAttachment();
                // This OData type is required by the Graph API for file attachments
                fileAttachment.setOdataType("#microsoft.graph.fileAttachment");
                fileAttachment.setName(attReq.getFilename());
                fileAttachment.setContentBytes(fileContent);

                attachmentList.add(fileAttachment);
            }
            message.setAttachments(attachmentList);
            log.info("Added {} attachments to the email message.", attachmentList.size());
        }
        // --- END OF NEW LOGIC ---

        return message;
    }

    private List<Recipient> getRecipients(List<String> emails) {
        List<Recipient> recipients = new ArrayList<>();
        for (String email : emails) {
            if (email != null && !email.trim().isEmpty()) {
                Recipient recipient = new Recipient();
                EmailAddress emailAddress = new EmailAddress();
                emailAddress.setAddress(email);
                recipient.setEmailAddress(emailAddress);
                recipients.add(recipient);
            }
        }
        return recipients;
    }
}
 