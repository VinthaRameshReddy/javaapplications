package com.medgo.claims.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class BankAccountValidationService {

    // Map bank names (case-insensitive keys) to required digit counts
    // Format: "bank_name" -> [minDigits, maxDigits] or single value for exact match
    private static final Map<String, int[]> BANK_DIGIT_REQUIREMENTS = new HashMap<>();

    static {
        // Initialize bank digit requirements
        BANK_DIGIT_REQUIREMENTS.put("bdo", new int[]{12, 12});
        BANK_DIGIT_REQUIREMENTS.put("banco de oro", new int[]{12, 12});
        BANK_DIGIT_REQUIREMENTS.put("bpi", new int[]{10, 10});
        BANK_DIGIT_REQUIREMENTS.put("bank of the philippine islands", new int[]{10, 10});
        BANK_DIGIT_REQUIREMENTS.put("chinabank", new int[]{10, 12});
        BANK_DIGIT_REQUIREMENTS.put("china bank", new int[]{10, 12});
        BANK_DIGIT_REQUIREMENTS.put("eastwest", new int[]{12, 12});
        BANK_DIGIT_REQUIREMENTS.put("eastwest bank", new int[]{12, 12});
        BANK_DIGIT_REQUIREMENTS.put("east west", new int[]{12, 12});
        BANK_DIGIT_REQUIREMENTS.put("lpb", new int[]{10, 10});
        BANK_DIGIT_REQUIREMENTS.put("land bank", new int[]{10, 10});
        BANK_DIGIT_REQUIREMENTS.put("land bank of the philippines", new int[]{10, 10});
        BANK_DIGIT_REQUIREMENTS.put("metrobank", new int[]{13, 13});
        BANK_DIGIT_REQUIREMENTS.put("metro bank", new int[]{13, 13});
        BANK_DIGIT_REQUIREMENTS.put("pnb", new int[]{12, 12});
        BANK_DIGIT_REQUIREMENTS.put("philippine national bank", new int[]{12, 12});
        BANK_DIGIT_REQUIREMENTS.put("rcbc", new int[]{10, 10});
        BANK_DIGIT_REQUIREMENTS.put("rizal commercial banking corporation", new int[]{10, 10});
        BANK_DIGIT_REQUIREMENTS.put("security bank", new int[]{13, 13});
        BANK_DIGIT_REQUIREMENTS.put("ucpb", new int[]{12, 12});
        BANK_DIGIT_REQUIREMENTS.put("united coco planters bank", new int[]{12, 12});
        BANK_DIGIT_REQUIREMENTS.put("ubp", new int[]{12, 12});
        BANK_DIGIT_REQUIREMENTS.put("union bank", new int[]{12, 12});
        BANK_DIGIT_REQUIREMENTS.put("union bank of the philippines", new int[]{12, 12});
        BANK_DIGIT_REQUIREMENTS.put("phil. business bank", new int[]{12, 12});
        BANK_DIGIT_REQUIREMENTS.put("phil business bank", new int[]{12, 12});
        BANK_DIGIT_REQUIREMENTS.put("philippine business bank", new int[]{12, 12});
        BANK_DIGIT_REQUIREMENTS.put("pbcom", new int[]{12, 12});
    }

    /**
     * Validates bank account number based on bank name and required digit count
     * 
     * @param bankName The name of the bank
     * @param bankAccountNumber The bank account number to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateBankAccount(String bankName, String bankAccountNumber) {
        if (bankName == null || bankName.trim().isEmpty()) {
            log.warn("Bank name is null or empty, skipping bank account validation");
            return; // Skip validation if bank name is not provided
        }

        if (bankAccountNumber == null || bankAccountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Bank account number is required");
        }

        // Normalize bank name for lookup (lowercase, remove extra spaces)
        String normalizedBankName = bankName.trim().toLowerCase();
        
        // Find matching bank requirement
        int[] digitRequirements = findBankRequirement(normalizedBankName);
        
        if (digitRequirements == null) {
            log.warn("Bank '{}' not found in validation rules, skipping validation", bankName);
            return; // Skip validation if bank is not in our list
        }

        // Remove any non-digit characters (spaces, dashes, etc.) for validation
        String cleanedAccountNumber = bankAccountNumber.replaceAll("[^0-9]", "");
        
        if (cleanedAccountNumber.isEmpty()) {
            throw new IllegalArgumentException("Bank account number must contain at least one digit");
        }

        int accountLength = cleanedAccountNumber.length();
        int minDigits = digitRequirements[0];
        int maxDigits = digitRequirements[1];

        if (accountLength < minDigits || accountLength > maxDigits) {
            String errorMessage;
            if (minDigits == maxDigits) {
                errorMessage = String.format(
                    "Invalid bank account number for %s. Required: %d digits, but provided: %d digits",
                    bankName, minDigits, accountLength
                );
            } else {
                errorMessage = String.format(
                    "Invalid bank account number for %s. Required: %d to %d digits, but provided: %d digits",
                    bankName, minDigits, maxDigits, accountLength
                );
            }
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        log.debug("Bank account validation passed for {}: {} digits (required: {}-{})", 
                bankName, accountLength, minDigits, maxDigits);
    }

    /**
     * Find bank requirement by matching bank name (case-insensitive, partial match)
     */
    private int[] findBankRequirement(String normalizedBankName) {
        // Try exact match first
        if (BANK_DIGIT_REQUIREMENTS.containsKey(normalizedBankName)) {
            return BANK_DIGIT_REQUIREMENTS.get(normalizedBankName);
        }

        // Try partial match (bank name contains key or key contains bank name)
        for (Map.Entry<String, int[]> entry : BANK_DIGIT_REQUIREMENTS.entrySet()) {
            String key = entry.getKey();
            if (normalizedBankName.contains(key) || key.contains(normalizedBankName)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Get required digit count for a bank (for informational purposes)
     */
    public String getRequiredDigitsInfo(String bankName) {
        if (bankName == null || bankName.trim().isEmpty()) {
            return null;
        }

        String normalizedBankName = bankName.trim().toLowerCase();
        int[] digitRequirements = findBankRequirement(normalizedBankName);
        
        if (digitRequirements == null) {
            return null;
        }

        int minDigits = digitRequirements[0];
        int maxDigits = digitRequirements[1];

        if (minDigits == maxDigits) {
            return String.valueOf(minDigits);
        } else {
            return minDigits + " to " + maxDigits;
        }
    }

    /**
     * Get maximum length (max digits) for a bank account number based on bank name
     * 
     * @param bankName The name of the bank
     * @return Maximum length/digits allowed, or null if bank not found
     */
    public Integer getMaxLength(String bankName) {
        if (bankName == null || bankName.trim().isEmpty()) {
            return null;
        }

        String normalizedBankName = bankName.trim().toLowerCase();
        int[] digitRequirements = findBankRequirement(normalizedBankName);
        
        if (digitRequirements == null) {
            return null;
        }

        // Return the maximum digits (second element in the array)
        return digitRequirements[1];
    }

    /**
     * Get minimum length (min digits) for a bank account number based on bank name
     * 
     * @param bankName The name of the bank
     * @return Minimum length/digits allowed, or null if bank not found
     */
    public Integer getMinLength(String bankName) {
        if (bankName == null || bankName.trim().isEmpty()) {
            return null;
        }

        String normalizedBankName = bankName.trim().toLowerCase();
        int[] digitRequirements = findBankRequirement(normalizedBankName);
        
        if (digitRequirements == null) {
            return null;
        }

        // Return the minimum digits (first element in the array)
        return digitRequirements[0];
    }
}

