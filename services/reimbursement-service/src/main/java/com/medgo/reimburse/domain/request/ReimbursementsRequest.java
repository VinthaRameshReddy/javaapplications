package com.medgo.reimburse.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ReimbursementsRequest(

        @Min(value = 0, message = "Page minimum value is 0.")
        @JsonProperty("page")
        Integer page,

        @Min(value = 5, message = "Size minimum value is 5.")
        @JsonProperty("size")
        Integer size,

        @JsonProperty("memberCode")
        String memberCode,

        @JsonProperty("search")
        String search,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @JsonProperty("visitDate")
        LocalDate visitDate,

        @JsonProperty("status")
        String status
) { }
