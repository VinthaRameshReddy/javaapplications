package com.medgo.member.domain.entity.utilization;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;


@Embeddable
public class UtilizationLegacyId implements Serializable {

    @Column(name = "CONTROL_CODE", nullable = false, length = 50)
    private String controlCode;

    @Column(name = "UPDATED_DATE", nullable = false)
    private LocalDateTime updatedDate;

    public UtilizationLegacyId() {}

    public UtilizationLegacyId(String controlCode, LocalDateTime updatedDate) {
        this.controlCode = controlCode;
        this.updatedDate = updatedDate;
    }

    public String getControlCode() { return controlCode; }
    public void setControlCode(String controlCode) { this.controlCode = controlCode; }


    // Added getter and setter for updatedDate
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UtilizationLegacyId that)) return false;
        // Added updatedDate to the comparison
        return Objects.equals(controlCode, that.controlCode)
                && Objects.equals(updatedDate, that.updatedDate);
    }

    @Override
    public int hashCode() {
        // Added updatedDate to the hash calculation
        return Objects.hash(controlCode, updatedDate);
    }
}