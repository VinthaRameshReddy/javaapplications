package com.medgo.member.exception;

/**
 * Exception thrown when memberCode validation fails.
 * This exception indicates that the requested memberCode does not belong to the authenticated user.
 */
public class MemberCodeValidationException extends RuntimeException {
    
    private final String memberCode;
    
    public MemberCodeValidationException(String memberCode, String message) {
        super(message);
        this.memberCode = memberCode;
    }
    
    public MemberCodeValidationException(String memberCode, String message, Throwable cause) {
        super(message, cause);
        this.memberCode = memberCode;
    }
    
    public String getMemberCode() {
        return memberCode;
    }
}

