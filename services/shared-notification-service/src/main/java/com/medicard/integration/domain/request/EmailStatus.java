// src/main/java/com/medicard/integration/domain/audit/EmailStatus.java
package com.medicard.integration.domain.request;

public enum EmailStatus {
    ATTEMPTING, // The first attempt to send
    RETRYING,   // A subsequent attempt after a transient failure
    SUCCESS,    // The email was successfully accepted by Graph API
    FAILED      // All retries were exhausted, or a non-retriable error occurred
}