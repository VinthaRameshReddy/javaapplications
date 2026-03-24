package com.medgo.reimburse.domain.mapper;

import com.medgo.reimburse.domain.dto.ReimHistoryDTO;
import com.medgo.reimburse.domain.entity.ReimHistory;
import com.medgo.reimburse.domain.entity.ReimVWMedgo2RequestDetails;
//import com.medgo.reimburse.domain.entity.ClaimNature;
//import com.medgo.reimburse.repository.ClaimNatureRepository;
//import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
//@RequiredArgsConstructor
public class ReimHistoryMapper {
    private static final Logger log = LoggerFactory.getLogger(ReimHistoryMapper.class);
//    private final ClaimNatureRepository claimNatureRepository;

    public ReimHistoryDTO toDTO(ReimHistory element) {

        ReimVWMedgo2RequestDetails moreDetails = element.getMoreDetails();
        log.debug("Fetched moreDetails: {}", moreDetails);

        String patientFullName = getPatientFullName(moreDetails);
        log.info("Patient full name resolved as: {}", patientFullName);

        String status = element.getStatus();
        log.info("Processing status: {}", status);

        String displayRemarks = "Default Remarks";
        String displayDescription = "Default Description";
        String displayStatus = "Default Status";
        String checkNo = moreDetails.getCheckNo();

        log.debug("Initial values -> checkNo: {}, displayStatus: {}, displayDescription: {}",
                  checkNo, displayStatus, displayDescription
        );

        Map<String, List<String>> remarksParts = null;

//        String claimNature = moreDetails.getClaimNature();
        String displayClaimNature = "-";

//        if (StringUtils.hasText(claimNature)) {
//            displayClaimNature = claimNatureRepository.findByClaimNature(claimNature)
//                    .map(ClaimNature::getDisplayClaimNature)
//                    .orElse(claimNature); // fallback to original if not found
//        }

//        log.info("Resolved claimNature='{}' -> displayClaimNature='{}'", claimNature, displayClaimNature);

        switch (status) {
            case "Processed":
                log.info("Entered status case: Processed");
                displayStatus = "In-Process";
                displayDescription =
                        "Your request is under review. Our team is verifying your details and evaluating the medical " +
                                "documents you’ve provided.";
                log.debug("Set displayStatus='{}', displayDescription='{}'", displayStatus, displayDescription);
                break;

            case "Released":
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

            case "Unreleased":
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

            case "Hold Checks":
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

            default:
                log.warn("Unhandled status encountered: '{}'", status);
                break;
        }

        log.info("Final displayStatus='{}', displayDescription='{}'", displayStatus, displayDescription);

        ReimHistoryDTO dto = ReimHistoryDTO.builder()
                                           .patientFullName(patientFullName)
                                           .serviceType(moreDetails.getServiceType())
                                           .controlCode(moreDetails.getControlCode())
                                           .memberCode(moreDetails.getMemberCode())
                                           .requestedAmount(moreDetails.getRequestedAmount())
                                           .approvedAmount(moreDetails.getApprovedAmount())
                                           .availmentDate(moreDetails.getAvailmentDate())
                                           .receivedDate(moreDetails.getReceivedDate())
                                           .entryDate(moreDetails.getEntryDate())
                                           .claimNature(displayClaimNature)
//                                           .entryCode(moreDetails.getEntryCode())
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
}
