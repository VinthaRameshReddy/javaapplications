package com.medgo.provider.specifications;

import com.medgo.provider.domain.entity.HealthFacilityEntity;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.List;

public class HealthFacilitySpecs {

    public static Specification<HealthFacilityEntity> hasHfCodes(List<String> hfCodes) {
        return (root, query, cb) -> {
            if (hfCodes == null || hfCodes.isEmpty()) return cb.conjunction();
            return root.get("hfCode").in(hfCodes);
        };
    }

    public static Specification<HealthFacilityEntity> hasHfStatuses(List<String> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) return cb.conjunction();
            return root.get("hfStatus").in(statuses);
        };
    }

    public static Specification<HealthFacilityEntity> hasHfTypes(List<String> types) {
        return (root, query, cb) -> {
            if (types == null || types.isEmpty()) return cb.conjunction();
            return root.get("hfType").in(types);
        };
    }

    public static Specification<HealthFacilityEntity> hasRegionCodes(List<String> regions) {
        return (root, query, cb) -> {
            if (regions == null || regions.isEmpty()) return cb.conjunction();
            return root.get("regionCode").in(regions);
        };
    }

    public static Specification<HealthFacilityEntity> hasProvinceCodes(List<String> provinces) {
        return (root, query, cb) -> {
            if (provinces == null || provinces.isEmpty()) return cb.conjunction();
            return root.get("provinceCode").in(provinces);
        };
    }

    public static Specification<HealthFacilityEntity> hasCityCodes(List<String> cities) {
        return (root, query, cb) -> {
            if (cities == null || cities.isEmpty()) return cb.conjunction();
            return root.get("cityCode").in(cities);
        };
    }

    public static Specification<HealthFacilityEntity> hasGroups(List<String> groups) {
        return (root, query, cb) -> {
            if (groups == null || groups.isEmpty()) return cb.conjunction();
            return root.get("group").in(groups);
        };
    }

    public static Specification<HealthFacilityEntity> hasAccountCodes(List<String> accountCodes) {
        return (root, query, cb) -> {
            if (accountCodes == null || accountCodes.isEmpty()) {
                return cb.conjunction();
            }

            Predicate[] predicates = accountCodes.stream()
                    .filter(code -> code != null && !code.isBlank())
                    .map(code -> {
                        String codeWithPipes = "|" + code + "|";
                        String codeAtStart = code + "|";
                        String codeAtEnd = "|" + code;

                        return cb.or(
                                cb.like(root.get("accountCodes"), "%" + codeWithPipes + "%"),
                                cb.like(root.get("accountCodes"), codeAtStart + "%"),
                                cb.like(root.get("accountCodes"), "%" + codeAtEnd),
                                cb.equal(root.get("accountCodes"), code)
                        );
                    })
                    .toArray(Predicate[]::new);

            if (predicates.length == 0) {
                return cb.conjunction();
            }

            return cb.or(predicates);
        };
    }

    public static Specification<HealthFacilityEntity> searchByHfName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String likeSearch = "%" + search.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("hfName")), likeSearch);
        };
    }
}