package com.medgo.commons;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberData {

    @JsonProperty("memberCode")
    private String memberCode;

//    private LocalDate birthDate;

    @JsonProperty("message")
    private String message;




}
