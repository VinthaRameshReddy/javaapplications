package com.medgo.member.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UtilizationRequest {

    @JsonProperty("dateFr")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "dateFr can't be null")
    private LocalDateTime dateFr;

    @JsonProperty("dateTo")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "dateTo can't be null")
    private LocalDateTime dateTo;

    @JsonProperty("memcode")
    @NotBlank(message = "memcode can't be null")
    private String memcode;

    @NotBlank(message = "lname can't be null")
    private String lname;

    @NotBlank(message = "fname can't be null")
    private String fname;

    private String mi; // optional

    @NotBlank(message = "comp can't be null")
    private String comp;

    @NotBlank(message = "user can't be null")
    private String user;

    @JsonProperty("valDate")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "valDate can't be null")
    private LocalDateTime valDate;

    @JsonProperty("effective")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "effective can't be null")
    private LocalDateTime effective;

    @JsonProperty("periodType")
    @NotBlank(message = "periodType can't be null")
    private String periodType;

    // ✅ Pagination
    private Integer page;
    private Integer size;

    // --------------------------------------------------------
    // ✅ Flexible Setters — support both String and LocalDateTime
    // --------------------------------------------------------

    public void setDateFr(LocalDateTime dateFr) {
        this.dateFr = dateFr;
    }

    public void setDateFr(String dateFr) {
        if (dateFr != null && !dateFr.isEmpty()) {
            this.dateFr = parseToLocalDateTime(dateFr);
        }
    }

    public void setDateTo(LocalDateTime dateTo) {
        this.dateTo = dateTo;
    }

    public void setDateTo(String dateTo) {
        if (dateTo != null && !dateTo.isEmpty()) {
            this.dateTo = parseToLocalDateTime(dateTo);
        }
    }

    // --------------------------------------------------------
    // 🔹 Support both formats: "yyyy-MM-dd" and "yyyy-MM-dd'T'HH:mm:ss"
    // --------------------------------------------------------
    LocalDateTime parseToLocalDateTime(String date) {
        try {
            // Case 1: "2025-10-28"
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        } catch (Exception e) {
            // Case 2: "2025-10-28T14:30:00"
            return LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        }
    }
}
