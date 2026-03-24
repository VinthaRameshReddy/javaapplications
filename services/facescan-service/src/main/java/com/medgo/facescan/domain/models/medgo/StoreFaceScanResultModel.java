package com.medgo.facescan.domain.models.medgo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Slf4j
@Table(name = "FACE_SCAN_RESULTS")
public class StoreFaceScanResultModel extends BaseEntity {

    // @Id this is not ID any more, but unique: ID is in the Base Entity
    @Column(name = "SESSION_ID", nullable = false, unique = true)
    private String sessionId; // Unique session ID

    @Column(name = "FED_ID")
    private String fedId; // Federated Membership Code

    @Column(name = "MEMBER_CODE")
    private String memberCode; // Associated Member Code

    @Column(name = "SCAN_RESULT")
    private String scanResult; // Scan result (e.g., "SUCCESS", "FAILED")

    @Column(name = "SCAN_DATA", columnDefinition = "TEXT")
    private String scanData; // JSON stored as a String

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @Column(name = "MEASUREMENT_ID")
    private String measurementID;

    // Convert JSON string to Map<String, Object> when retrieving
    public Map<String, Object> getScanDataAsJson() {
        if (scanData == null || scanData.isEmpty()) {
            log.warn("Scan data is empty or null for sessionId: {}", sessionId);
            return Map.of(); // Return an empty map if no data is present
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(scanData, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error parsing scanData for sessionId: {}. Error: {}", sessionId, e.getMessage());
            return Map.of("error", "Invalid JSON format in scanData");
        }
    }

    // Constructor for creating StoreFaceScanResultModel with fedId and memberCode
    public StoreFaceScanResultModel(String fedId, String memberCode) {
        this.fedId = fedId;
        this.memberCode = memberCode;
    }
}
