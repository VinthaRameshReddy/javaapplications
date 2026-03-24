package com.medgo.provider.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity(name = "vw_hf")
public class HealthFacilityEntity {

    private static final Logger log = LoggerFactory.getLogger(HealthFacilityEntity.class);

    @Id
    @Column(name = "hf_id")
    private Long hfId;

    @Column(name = "hf_code")
    private String hfCode;

    @Column(name = "hf_name")
    private String hfName;

    @Column(name = "hf_status")
    private String hfStatus;

    @Column(name = "hf_type")
    private String hfType;

    @Column(name = "hf_category")
    private String hfCategory;

    @Column(name = "preferred_network")
    private String preferredNetwork;

    @Column(name = "city_code")
    private String cityCode;

    @Column(name = "city_name")
    private String cityName;

    @Column(name = "province_code")
    private String provinceCode;

    @Column(name = "province_name")
    private String provinceName;

    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "region_name")
    private String regionName;

    @Column(name = "address")
    private String address;

    @Column(name = "contact_email")
    private String contactEmail;

    @JsonIgnore
    @Column(name = "contact_details")
    private String contactDetails;

    @JsonIgnore
    @Column(name = "account_codes")
    private String accountCodes;

    @Column(name = "`group`")
    private String group;

    @Column(name = "`order`")
    private String order;

    @Transient
    private List<HealthFacilityContactDetail> contactDetailList;

    @PostLoad
    public void loadContactDetailList() {
        if (this.contactDetails != null && !this.contactDetails.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                this.contactDetailList = mapper.readValue(this.contactDetails, new TypeReference<List<HealthFacilityContactDetail>>() {});
            } catch (JsonProcessingException e) {
                log.error("Failed to parse contact details: {}", e.getMessage());
                this.contactDetailList = new ArrayList<>();
            }
        } else {
            this.contactDetailList = new ArrayList<>();
        }
    }
}