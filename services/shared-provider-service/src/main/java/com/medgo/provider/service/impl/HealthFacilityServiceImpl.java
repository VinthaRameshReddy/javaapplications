package com.medgo.provider.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medgo.provider.domain.entity.AffiliationView;
import com.medgo.provider.domain.entity.HealthFacilityEntity;
import com.medgo.provider.domain.request.HospitalRequest;
import com.medgo.provider.domain.request.ViewDoctorHospitalRequest;
import com.medgo.provider.domain.response.ViewDoctorHospitalResponse;
import com.medgo.provider.mapper.MedGoClaimsMapper;
import com.medgo.provider.repository.AffiliationViewRepository;
import com.medgo.provider.repository.HealthFacilityRepository;
import com.medgo.provider.service.HealthFacilityService;
import com.medgo.provider.specifications.HealthFacilitySpecs;
import com.medgo.commons.CommonResponse;
import com.medgo.commons.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class HealthFacilityServiceImpl implements HealthFacilityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthFacilityServiceImpl.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HealthFacilityRepository healthFacilityRepository;
    private final AffiliationViewRepository phViewRepo;
    private final MedGoClaimsMapper mapper;
    private final ObjectMapper objectMapper;

    @Value("${db.call.enabled:true}")
    private boolean dbCallEnabled;

    public HealthFacilityServiceImpl(HealthFacilityRepository healthFacilityRepository,
                                     AffiliationViewRepository phViewRepo,
                                     MedGoClaimsMapper mapper,
                                     ObjectMapper objectMapper) {
        this.healthFacilityRepository = healthFacilityRepository;
        this.phViewRepo = phViewRepo;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    /* ========================  HOSPITAL LIST  ======================== */
    @Override
    public CommonResponse getHospitalsList(int page, int size, String search, HospitalRequest request) {
        LOGGER.info("Fetching hospital list (dbCallEnabled={})", dbCallEnabled);
        try {
            if (!dbCallEnabled) {
                List<HealthFacilityEntity> all = loadJson("static-data/hospitals.json",
                        new TypeReference<List<HealthFacilityEntity>>() {});

                // All filters are applied via hospitalFilter (including city/province/region)
                Predicate<HealthFacilityEntity> filter = hospitalFilter(request, search);

                List<HealthFacilityEntity> filtered = all.stream()
                        .filter(filter)
                        .collect(Collectors.toList());

                return paginate(filtered, page, size);
            }

            // ---------- DATABASE PATH (unchanged) ----------
            Pageable pageable = PageRequest.of(page, size);
            Specification<HealthFacilityEntity> spec = Specification.where(null);

            spec = and(spec, HealthFacilitySpecs.hasHfCodes(request.getHfCodes()));
            spec = and(spec, HealthFacilitySpecs.hasHfStatuses(request.getHfStatuses()));
            spec = and(spec, HealthFacilitySpecs.hasHfTypes(request.getHfTypes()));
            spec = and(spec, HealthFacilitySpecs.hasRegionCodes(request.getRegionCodes()));
            spec = and(spec, HealthFacilitySpecs.hasProvinceCodes(request.getProvinceCodes()));
            spec = and(spec, HealthFacilitySpecs.hasCityCodes(request.getCityCodes()));
            spec = and(spec, HealthFacilitySpecs.hasGroups(request.getGroups()));
            spec = and(spec, HealthFacilitySpecs.hasAccountCodes(request.getAccountCodes()));
            spec = and(spec, HealthFacilitySpecs.searchByHfName(search));

            Page<HealthFacilityEntity> resultPage = healthFacilityRepository.findAll(spec, pageable);

            if (resultPage.isEmpty()) {
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.NOT_FOUND.value(), "No hospitals found for the given criteria."),
                        HttpStatus.NOT_FOUND.value());
            }
            return CommonResponse.success(resultPage);
        } catch (Exception e) {
            LOGGER.error("Error fetching hospital list", e);
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_SERVER_ERROR",
                            "Failed to retrieve hospitals: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /* ========================  DOCTOR-HOSPITAL VIEW  ======================== */
    @Cacheable(value = "viewDoctorHospitalV2", keyGenerator = "redisKeyGenerator")
    @Override
    public CommonResponse getViewDoctorHospitalV2(int page, int size, ViewDoctorHospitalRequest request) {
        LOGGER.info("Fetching doctor-hospital view (dbCallEnabled={})", dbCallEnabled);
        try {
            if (!dbCallEnabled) {
                List<ViewDoctorHospitalResponse> all = loadJson("static-data/doctors.json",
                        new TypeReference<List<ViewDoctorHospitalResponse>>() {});

                List<ViewDoctorHospitalResponse> filtered = all.stream()
                        .filter(doctorHospitalFilter(request))
                        .collect(Collectors.toList());

                return paginate(filtered, page, size);
            }

            // ---------- DATABASE PATH ----------
            Pageable pageable = PageRequest.of(page, size);
            Specification<AffiliationView> spec = createViewDoctorHospitalSpec(request);
            Page<AffiliationView> resultPage = phViewRepo.findAll(spec, pageable);

            if (resultPage.isEmpty()) {
                return CommonResponse.error(
                        new ErrorResponse(HttpStatus.NOT_FOUND.value(),
                                "No doctor-hospital records found for the given criteria."),
                        HttpStatus.NOT_FOUND.value());
            }

            Page<ViewDoctorHospitalResponse> responsePage =
                    resultPage.map(mapper::toViewDoctorHospitalResponse);

            return CommonResponse.success(responsePage);
        } catch (Exception e) {
            LOGGER.error("Error fetching doctor-hospital view", e);
            return CommonResponse.error(
                    new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_SERVER_ERROR",
                            "Failed to retrieve doctor-hospital data: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /* ========================  IN-MEMORY FILTERS  ======================== */

    /**
     * Full in-memory filter for hospitals.
     * Null or empty collections → filter is ignored.
     */
    private Predicate<HealthFacilityEntity> hospitalFilter(HospitalRequest req, String search) {
        return entity -> {
            if (isNotEmpty(req.getHfCodes()) && !req.getHfCodes().contains(entity.getHfCode())) return false;
            if (isNotEmpty(req.getHfStatuses()) && !req.getHfStatuses().contains(entity.getHfStatus())) return false;
            if (isNotEmpty(req.getHfTypes()) && !req.getHfTypes().contains(entity.getHfType())) return false;
            if (isNotEmpty(req.getRegionCodes()) && !req.getRegionCodes().contains(entity.getRegionCode())) return false;
            if (isNotEmpty(req.getProvinceCodes()) && !req.getProvinceCodes().contains(entity.getProvinceCode())) return false;
            if (isNotEmpty(req.getCityCodes()) && !req.getCityCodes().contains(entity.getCityCode())) return false;
            if (isNotEmpty(req.getGroups()) && !req.getGroups().contains(entity.getGroup())) return false;

            // accountCodes is stored as JSON string
            if (isNotEmpty(req.getAccountCodes())) {
                List<String> stored = parseAccountCodes(entity.getAccountCodes());
                if (!containsAny(stored, req.getAccountCodes())) return false;
            }

            // name search
            if (search != null && !search.isBlank()) {
                String name = entity.getHfName();
                if (name == null || !name.toUpperCase().contains(search.toUpperCase())) return false;
            }
            return true;
        };
    }

    private Predicate<ViewDoctorHospitalResponse> doctorHospitalFilter(ViewDoctorHospitalRequest req) {
        return entity -> {
            if (req.getHospitalCode() != null && !req.getHospitalCode().equals(entity.getHospitalCode())) return false;
            if (req.getDoctorCode() != null && !req.getDoctorCode().equals(entity.getDoctorCode())) return false;

            if (req.getDoctorName() != null && !req.getDoctorName().isBlank()) {
                String searchTerm = req.getDoctorName().toUpperCase().trim();
                String firstName = entity.getFirstName() != null ? entity.getFirstName().toUpperCase() : "";
                String lastName = entity.getLastName() != null ? entity.getLastName().toUpperCase() : "";
                String middleInitial = entity.getMiddleInitial() != null ? entity.getMiddleInitial().toUpperCase() : "";
                
                // Build fullName in standard format: "FIRSTNAME LASTNAME"
                String fullName = (firstName + " " + lastName).trim();
                
                // Build fullName in comma-separated format: "LASTNAME, FIRSTNAME MIDDLEINITIAL"
                String fullNameCommaFormat = (lastName + ", " + firstName + " " + middleInitial).trim();
                
                // Also check the fullName field if it exists (already formatted)
                String fullNameField = entity.getFullName() != null ? entity.getFullName().toUpperCase() : "";
                
                // Flexible matching: check if search term appears anywhere in firstName, lastName, 
                // standard fullName, comma-separated fullName, or fullName field
                boolean match = firstName.contains(searchTerm)
                        || lastName.contains(searchTerm)
                        || fullName.contains(searchTerm)
                        || fullNameCommaFormat.contains(searchTerm)
                        || fullNameField.contains(searchTerm);
                
                // Also check word-based matching: if search term has multiple words, check if all words appear
                if (!match && searchTerm.contains(" ")) {
                    String[] searchWords = searchTerm.split("\\s+");
                    boolean allWordsFound = true;
                    for (String word : searchWords) {
                        if (word.isEmpty()) continue;
                        if (!fullName.contains(word) 
                                && !fullNameCommaFormat.contains(word)
                                && !fullNameField.contains(word)
                                && !firstName.contains(word)
                                && !lastName.contains(word)) {
                            allWordsFound = false;
                            break;
                        }
                    }
                    match = allWordsFound;
                }
                
                if (!match) return false;
            }

            if (req.getHospitalName() != null && !req.getHospitalName().isBlank()) {
                String hn = entity.getHospitalName();
                if (hn == null || !hn.toUpperCase().contains(req.getHospitalName().toUpperCase())) return false;
            }

            if (isNotEmpty(req.getSpecializationCodes())
                    && !req.getSpecializationCodes().contains(entity.getSpecializationCode())) return false;

            if (isNotEmpty(req.getSpecializationCodeNotIn())
                    && req.getSpecializationCodeNotIn().contains(entity.getSpecializationCode())) return false;

            if (req.isMaternity()) return false; // status not in DTO

            return true;
        };
    }

    /* ========================  HELPERS  ======================== */

    private <T> List<T> loadJson(String path, TypeReference<List<T>> typeRef) throws Exception {
        InputStream is = new ClassPathResource(path).getInputStream();
        return objectMapper.readValue(is, typeRef);
    }

    private <T> CommonResponse paginate(List<T> source, int page, int size) {
        int total = source.size();
        int start = Math.min(page * size, total);
        int end = Math.min(start + size, total);
        List<T> content = source.subList(start, end);
        Page<T> result = new PageImpl<>(content, PageRequest.of(page, size), total);
        return result.isEmpty()
                ? CommonResponse.error(
                new ErrorResponse(HttpStatus.NOT_FOUND.value(),
                        "No records found for the given criteria."),
                HttpStatus.NOT_FOUND.value())
                : CommonResponse.success(result);
    }

    private List<String> parseAccountCodes(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            LOGGER.warn("Failed to parse accountCodes JSON: {}", json, e);
            return Collections.emptyList();
        }
    }

    private boolean containsAny(List<String> stored, List<String> requested) {
        if (stored.isEmpty() || requested.isEmpty()) return false;
        return stored.stream().anyMatch(requested::contains);
    }

    /** Safe `and` – avoids deprecated `Specification.where(spec)` */
    private <T> Specification<T> and(Specification<T> base, Specification<T> next) {
        if (next == null) return base == null ? Specification.where(null) : base;
        if (base == null) return next;
        return base.and(next);
    }

    /** Helper: null or empty → ignore filter */
    private <T> boolean isNotEmpty(Collection<T> coll) {
        return coll != null && !coll.isEmpty();
    }

    /* ========================  DB SPEC (NO DEPRECATION) ======================== */
    private Specification<AffiliationView> createViewDoctorHospitalSpec(ViewDoctorHospitalRequest request) {
        Specification<AffiliationView> spec = Specification.where(null);

        spec = and(spec, request.getHospitalCode() == null ? null :
                (root, query, cb) -> cb.equal(root.get("hospitalCode"), request.getHospitalCode()));
        spec = and(spec, request.getDoctorCode() == null ? null :
                (root, query, cb) -> cb.equal(root.get("doctorCode"), request.getDoctorCode()));
        spec = and(spec, request.getDoctorName() == null ? null :
                (root, query, cb) -> {
                    String searchTerm = "%" + request.getDoctorName().toUpperCase().trim() + "%";
                    // Check if search term matches in firstName, lastName, or combined full name
                    // Support both formats:
                    // 1. Standard format: "FIRSTNAME LASTNAME"
                    // 2. Comma-separated format: "LASTNAME, FIRSTNAME MIDDLEINITIAL"
                    return cb.or(
                            cb.like(cb.upper(root.get("lastName")), searchTerm),
                            cb.like(cb.upper(root.get("firstName")), searchTerm),
                            // Standard format: "FIRSTNAME LASTNAME"
                            cb.like(
                                    cb.upper(
                                            cb.concat(
                                                    cb.concat(
                                                            cb.coalesce(root.get("firstName"), cb.literal("")),
                                                            cb.literal(" ")
                                                    ),
                                                    cb.coalesce(root.get("lastName"), cb.literal(""))
                                            )
                                    ),
                                    searchTerm
                            ),
                            // Comma-separated format: "LASTNAME, FIRSTNAME MIDDLEINITIAL"
                            cb.like(
                                    cb.upper(
                                            cb.concat(
                                                    cb.concat(
                                                            cb.concat(
                                                                    cb.concat(
                                                                            cb.coalesce(root.get("lastName"), cb.literal("")),
                                                                            cb.literal(", ")
                                                                    ),
                                                                    cb.coalesce(root.get("firstName"), cb.literal(""))
                                                            ),
                                                            cb.literal(" ")
                                                    ),
                                                    cb.coalesce(root.get("middleInitial"), cb.literal(""))
                                            )
                                    ),
                                    searchTerm
                            )
                    );
                });
        spec = and(spec, request.getHospitalName() == null ? null :
                (root, query, cb) -> cb.like(cb.upper(root.get("hospitalName")), "%" + request.getHospitalName().toUpperCase() + "%"));
        spec = and(spec, request.getCityCode() == null ? null :
                (root, query, cb) -> cb.equal(root.get("hospitalCityCode"), request.getCityCode()));
        spec = and(spec, request.getProvinceCode() == null ? null :
                (root, query, cb) -> cb.equal(root.get("hospitalProvinceCode"), request.getProvinceCode()));
        spec = and(spec, request.getRegionCode() == null ? null :
                (root, query, cb) -> cb.equal(root.get("hospitalRegionCode"), request.getRegionCode()));
        spec = and(spec, request.getAccreditationStatusCodes() == null ||
                request.getAccreditationStatusCodes().isEmpty() ? null :
                (root, query, cb) -> root.get("status").in(request.getAccreditationStatusCodes()));
        spec = and(spec, request.getSpecializationCodes() == null ||
                request.getSpecializationCodes().isEmpty() ? null :
                (root, query, cb) -> root.get("specializationCode").in(request.getSpecializationCodes()));
        spec = and(spec, request.getSpecializationCodeNotIn() == null ||
                request.getSpecializationCodeNotIn().isEmpty() ? null :
                (root, query, cb) -> cb.not(root.get("specializationCode").in(request.getSpecializationCodeNotIn())));
        spec = and(spec, !request.isMaternity() ? null :
                (root, query, cb) -> cb.equal(root.get("status"), "ACTIVE"));

        return spec;
    }
}