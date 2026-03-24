package com.medgo.reimburse.domain.mapper;

import com.medgo.reimburse.domain.dto.DisapprovedItem;
import com.medgo.reimburse.domain.dto.ReimHistoryDTO;
import com.medgo.reimburse.domain.dto.ReimRequestDetailsResponse;
import com.medgo.reimburse.domain.entity.medigo.ClaimNature;
import com.medgo.reimburse.domain.entity.reimb.ReimHistory;
import com.medgo.reimburse.domain.entity.reimb.ReimVWMedgo2RequestDetails;
import com.medgo.reimburse.repository.medigo.ClaimNatureRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ReimHistoryMapper {
    private static final Logger log = LoggerFactory.getLogger(ReimHistoryMapper.class);
    private final ClaimNatureRepository claimNatureRepository;
    public ReimHistoryDTO toDTO(ReimHistory element) {

        ReimVWMedgo2RequestDetails moreDetails = element.getMoreDetails();
        log.debug("Fetched moreDetails: {}", moreDetails);

        String patientFullName = getPatientFullName(moreDetails);
        log.info("Patient full name resolved as: {}", patientFullName);

        String status = element.getStatus();
        // Normalize status: trim whitespace, handle null, and convert to consistent case for comparison
        String normalizedStatus = (status != null) ? status.trim() : null;
        String statusForComparison = (normalizedStatus != null) ? normalizedStatus.toUpperCase() : null;
        log.info("Processing status: '{}' (normalized: '{}', for comparison: '{}')", status, normalizedStatus, statusForComparison);

        String displayRemarks = "Default Remarks";
        String displayDescription = "Default Description";
        String displayStatus = "Default Status";
        String checkNo = moreDetails.getCheckNo();

        log.debug("Initial values -> checkNo: {}, displayStatus: {}, displayDescription: {}",
                  checkNo, displayStatus, displayDescription
        );

        Map<String, List<String>> remarksParts = null;
        
        String classificationCode = moreDetails.getClaimNature();  // we get classificationCode in claimNature field
        String displayClaimNature = "-";

        if (StringUtils.hasText(classificationCode)) {
            displayClaimNature = claimNatureRepository.findByClassificationCode(classificationCode)
                    .map(ClaimNature::getDisplayClaimNature)
                    .orElse("-");
        }

        log.info("Resolved classificationCode='{}' -> displayClaimNature='{}'", classificationCode, displayClaimNature);

        // Handle status mapping with case-insensitive comparison
        if (statusForComparison == null || statusForComparison.isEmpty()) {
            log.warn("Status is null or empty, using default values");
        } else {
            switch (statusForComparison) {
            case "PROCESSED":
                log.info("Entered status case: Processed");
                displayStatus = "In-Process";
                displayDescription =
                        "Your request is under review. Our team is verifying your details and evaluating the medical " +
                                "documents you've provided.";
                log.debug("Set displayStatus='{}', displayDescription='{}'", displayStatus, displayDescription);
                break;

            case "RELEASED":
                log.info("Entered status case: Released");
                if (moreDetails.getActionMemo() || moreDetails.getWhbMemo()) {
                    displayStatus = "Action Needed";
                    displayDescription =
                            "We need a bit more information. Please check the remarks for the action needed.";
                    log.info("Action memo or WHB memo found -> displayStatus='{}', displayDescription='{}'",
                             displayStatus, displayDescription
                    );

                    if (moreDetails.getActionMemo()) {
                        displayRemarks = moreDetails.getActionMemoRemarks();
                        log.info("Action memo remarks found -> displayRemarks='{}'", displayRemarks);

                        remarksParts = parseTaggedString(moreDetails.getActionMemoRemarks());
                        displayRemarks = moreDetails.getActionMemoRemarks();

                    } else if (moreDetails.getWhbMemo()) {
                        displayRemarks = moreDetails.getWhbMemoRemarks();
                        log.info("WHB memo remarks found -> displayRemarks='{}'", displayRemarks);
                    }
                }

                if (moreDetails.getDeniedMemo()) {
                    displayStatus = "Denied";
                    displayDescription =
                            "Your request has been denied. Please review the reasons below for more details.";
                    log.info("Denied memo found -> displayStatus='{}', displayDescription='{}'",
                             displayStatus, displayDescription
                    );
                    displayRemarks = moreDetails.getDeniedMemoRemarks();
                    remarksParts = parseTaggedString(moreDetails.getDeniedMemoRemarks());
                }

                if (StringUtils.hasText(checkNo)) {
                    log.debug("Check number found: {}", checkNo);
                    if (checkNo.chars().allMatch(Character::isDigit)
                            || checkNo.startsWith("EB") || checkNo.startsWith("EH")) {
                        displayStatus = "Released";
                        displayDescription =
                                "Your reimbursement has been processed and is now available in your account.";
                        log.info("Valid released check number -> displayStatus='{}'", displayStatus);
                        displayRemarks = null;
                    }
                }
                break;

            case "UNRELEASED":
                log.info("Entered status case: Unreleased");

                if (StringUtils.hasText(checkNo)) {
                    log.debug("Check number found: {}", checkNo);

                    if (checkNo.chars().allMatch(Character::isDigit) || checkNo.startsWith("EP")) {
                        displayStatus = "For Releasing";
                        displayDescription =
                                "Your payment is being processed and will be completed within 1–3 banking days.";
                        log.info("Eligible for release -> displayStatus='{}', displayDescription='{}'",
                                 displayStatus, displayDescription
                        );
                    }
                }
                break;

            case "HOLD CHECKS":
                log.info("Entered status case: Hold Checks");

                String holdRemarks = moreDetails.getHoldRemarks();
                String lapseRemarks = moreDetails.getLapsePaymentRemarks();
                String forRmdApproval = moreDetails.getForRmdApproval();

                boolean hasHoldRemarks = StringUtils.hasText(holdRemarks);
                boolean hasLapseRemarks = StringUtils.hasText(lapseRemarks);
                boolean hasForRmdApproval = StringUtils.hasText(forRmdApproval);

                log.debug("Hold details -> holdRemarks='{}', lapseRemarks='{}', forRmdApproval='{}'",
                          holdRemarks, lapseRemarks, forRmdApproval
                );

                if (hasLapseRemarks) {
                    displayStatus = "Hold";
                    log.info("Lapse remarks found -> displayStatus='{}'", displayStatus);
                    remarksParts = parseTaggedString(lapseRemarks);
                    displayRemarks = lapseRemarks;
                } else if (hasHoldRemarks && !"w/URG CONCERN".equals(holdRemarks)) {
                    displayStatus = "Hold";
                    log.info("Hold remarks (not urgent) found -> displayStatus='{}'", displayStatus);
                } else if (hasForRmdApproval && !hasHoldRemarks) {
                    displayStatus = "Hold";
                    log.info("For RMD approval found without hold remarks -> displayStatus='{}'", displayStatus);
                    displayRemarks = holdRemarks;
                    remarksParts = parseTaggedString(holdRemarks);
                }

                if ("Hold".equals(displayStatus)) {
                    displayDescription = """
                            Your reimbursement request is on hold. Please check your email for a message
                            from our team with the next steps.
                            """;
                    log.info("Set Hold displayDescription -> '{}'", displayDescription.trim());
                } else {
                    log.debug("No valid Hold conditions met");
                }
                break;

            case "FOR CHECK PREPARATION":
                log.info("Entered status case: For Check Preparation (matched: '{}')", normalizedStatus);
                displayStatus = "For Releasing";
                displayDescription =
                        "Your payment is being processed and will be completed within 1–3 banking days.";
                log.info("Set displayStatus='{}', displayDescription='{}'", displayStatus, displayDescription);
                displayRemarks = displayDescription;
                break;

            case "UNPROCESSED":
                log.info("Entered status case: Unprocessed");
                displayStatus = "In-Process";
                displayDescription =
                        "Your request is under review. Our team is verifying your details and evaluating the medical documents you've provided.";
                log.debug("Set displayStatus='{}', displayDescription='{}'", displayStatus, displayDescription);
                displayRemarks = displayDescription;
                break;

            case "CHECK RELEASED":
                log.info("Entered status case: Check Released");
                displayStatus = "Released";
                displayDescription =
                        "Your reimbursement has been processed and is now available in your account.";
                log.debug("Set displayStatus='{}', displayDescription='{}'", displayStatus, displayDescription);
                displayRemarks = displayDescription;
                break;

            default:
                log.warn("Unhandled status encountered: '{}' (normalized: '{}', original: '{}')", 
                        statusForComparison, normalizedStatus, status);
                break;
            }
        }

        log.info("Final displayStatus='{}', displayDescription='{}'", displayStatus, displayDescription);

        ReimHistoryDTO dto = ReimHistoryDTO.builder()
                                           .patientFullName(patientFullName)
                                           .serviceType(moreDetails.getServiceType())
                                           .controlCode(moreDetails.getControlCode())
                                           .memberCode(moreDetails.getMemberCode())
                                            .requestedAmount(formatAmount(moreDetails.getRequestedAmount()))
                                            .approvedAmount(formatAmount(moreDetails.getApprovedAmount()))
                                           .availmentDate(moreDetails.getAvailmentDate())
                                           .receivedDate(moreDetails.getReceivedDate())
                                           .entryDate(moreDetails.getEntryDate())
                                           .claimNature(displayClaimNature)
                                           .entryCode(moreDetails.getEntryCode())
//                                           .checkNo(moreDetails.getCheckNo())
//                                           .forRmdApproval(moreDetails.getForRmdApproval())
//                                           .actionMemoRemarks(moreDetails.getActionMemoRemarks())
//                                           .deniedMemoRemarks(moreDetails.getDeniedMemoRemarks())
//                                           .whbMemoRemarks(moreDetails.getWhbMemoRemarks())
//                                           .holdRemarks(moreDetails.getHoldRemarks())
//                                           .lapsePaymentRemarks(moreDetails.getLapsePaymentRemarks())
//                                           .actionMemo(moreDetails.getActionMemo())
//                                           .deniedMemo(moreDetails.getDeniedMemo())
//                                           .whbMemo(moreDetails.getWhbMemo())
//                                           .status(moreDetails.getStatus())
                                           .displayStatus(displayStatus)
                                           .displayDescription(displayDescription)
                                           .displayRemarks(displayRemarks)
                                           .parsedRemarks(remarksParts)
                                           .build();

        log.info("Successfully created ReimHistoryDTO: {}", dto);
        return dto;
    }

    public String getPatientFullName(ReimVWMedgo2RequestDetails moreDetails) {
        if (moreDetails == null) {
            return "";
        }

        String firstName = moreDetails.getMemFname();
        String middleName = moreDetails.getMemMname();
        String lastName = moreDetails.getMemLname();

        // Join with single spaces and clean up extra ones
        return String.join(" ",
                           firstName != null ? firstName : "",
                           middleName != null ? middleName : "",
                           lastName != null ? lastName : ""
        ).trim().replaceAll(" +", " ");
    }

    public static Map<String, List<String>> parseTaggedString(String text) {
        Map<String, List<String>> result = new LinkedHashMap<>(); // preserve order

        // Regex to capture: (text)#(key)*
        Pattern pattern = Pattern.compile("([^#]+)#([A-Za-z0-9_]+)\\*");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String value = matcher.group(1).trim();
            String key = matcher.group(2).trim();

            result.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }

        return result;
    }

    public static String formatAmount(BigDecimal amount) {

        // Null OR Zero → return "-"
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "-";
        }

        // Otherwise return formatted 2-decimal number
        return amount.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    public ReimRequestDetailsResponse toRequestDetailsResponse(ReimHistory element, String statusFromRequest, String doctorName) {
        ReimVWMedgo2RequestDetails moreDetails = element.getMoreDetails();

        // Use exact database status value instead of request parameter
        String status = element.getStatus();

        // Show exact database values for amounts (null stays null, zero shows as "0.00")
        String totalRequestedAmount = formatAmountExact(moreDetails.getRequestedAmount());
        String approvedAmount = formatAmountExact(moreDetails.getApprovedAmount());

        // Calculate disapproved amount and show exact value
        BigDecimal requestedAmount = moreDetails.getRequestedAmount() != null ? moreDetails.getRequestedAmount() : BigDecimal.ZERO;
        BigDecimal approvedAmountValue = moreDetails.getApprovedAmount() != null ? moreDetails.getApprovedAmount() : BigDecimal.ZERO;
        BigDecimal disapprovedAmount = requestedAmount.subtract(approvedAmountValue);
        String totalDisapprovedAmount = formatAmountExact(disapprovedAmount);

        // Show exact database date values (no formatting, use exact LocalDateTime values)
        String requestDate = moreDetails.getEntryDate() != null ? moreDetails.getEntryDate().toString() : 
                            (moreDetails.getReceivedDate() != null ? moreDetails.getReceivedDate().toString() : null);
        String availmentDate = moreDetails.getAvailmentDate() != null ? moreDetails.getAvailmentDate().toString() : null;

        List<DisapprovedItem> disapprovedItems = buildDisapprovedItems(moreDetails, availmentDate, doctorName);

        return ReimRequestDetailsResponse.builder()
                .controlCode(moreDetails.getControlCode())
                .status(status)
                .totalRequestedAmount(totalRequestedAmount)
                .approvedAmount(approvedAmount)
                .requestDate(requestDate)
                .availmentDate(availmentDate)
                .totalDisapprovedAmount(totalDisapprovedAmount)
                .disapprovedItems(disapprovedItems)
                .build();
    }

    private List<DisapprovedItem> buildDisapprovedItems(ReimVWMedgo2RequestDetails moreDetails, String availmentDate, String doctorName) {
        List<DisapprovedItem> items = new ArrayList<>();
        
        BigDecimal requestedAmount = moreDetails.getRequestedAmount() != null ? moreDetails.getRequestedAmount() : BigDecimal.ZERO;
        BigDecimal approvedAmount = moreDetails.getApprovedAmount() != null ? moreDetails.getApprovedAmount() : BigDecimal.ZERO;
        BigDecimal disapprovedAmount = requestedAmount.subtract(approvedAmount);
        
        if (disapprovedAmount.compareTo(BigDecimal.ZERO) > 0) {
            String classificationCode = moreDetails.getClaimNature();
            String itemName = null;
            if (StringUtils.hasText(classificationCode)) {
                itemName = claimNatureRepository.findByClassificationCode(classificationCode)
                        .map(ClaimNature::getDisplayClaimNature)
                        .orElse(null);
            }
            
            String reasonForDisapproval = moreDetails.getDeniedMemoRemarks(); // Returns null if DENIED_MEMO_REMARKS is null in database
            
            DisapprovedItem item = DisapprovedItem.builder()
                    .itemName(itemName)
                    .doctorName(doctorName != null ? doctorName.trim() : null)
                    .dateOfConsultation(availmentDate)
                    .amountClaimed(formatAmountExact(approvedAmount))
                    .amountDisapproved(formatAmountExact(disapprovedAmount))
                    .reasonForDisapproval(reasonForDisapproval)
                    .build();
            items.add(item);
        }
        
        return items;
    }

    private String formatAmountWithPrefix(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return amount.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * Formats amount to show exact value from database.
     * If zero, returns "0.00"; if null, returns null (exact database value).
     */
    private String formatAmountExact(BigDecimal amount) {
        if (amount == null) {
            return null; // Return null as exact value from database
        }
        // Format zero or any other value with 2 decimal places
        return amount.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

}
