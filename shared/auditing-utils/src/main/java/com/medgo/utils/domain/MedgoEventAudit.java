package com.medgo.utils.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "MEDGO_EVENT_AUDIT")
public class MedgoEventAudit {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "platform", length = 50)
    private String platform;

    public MedgoEventAudit() {
    }

    public MedgoEventAudit(String username, String eventType, String deviceId, LocalDateTime eventTime, String platform) {
        this.username = username;
        this.eventType = eventType;
        this.deviceId = deviceId;
        this.eventTime = eventTime;
        this.platform = platform;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
