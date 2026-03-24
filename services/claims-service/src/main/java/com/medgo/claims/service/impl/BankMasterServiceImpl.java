package com.medgo.claims.service.impl;

import com.medgo.claims.domain.entity.medigo.MedgoBankMaster;
import com.medgo.claims.repository.medigo.MedgoBankMasterRepository;
import com.medgo.claims.service.BankAccountValidationService;
import com.medgo.claims.service.BankMasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankMasterServiceImpl implements BankMasterService {

    private final MedgoBankMasterRepository medgoBankMasterRepository;
    private final BankAccountValidationService bankAccountValidationService;

    @Override
    public Map<String, Object> getEnabledBanks() {
        log.info("BankMasterServiceImpl.getEnabledBanks - fetching enabled banks");

        List<MedgoBankMaster> enabledBanks =
                medgoBankMasterRepository.findByEnabledTrueOrderByNameAsc();

        log.info("BankMasterServiceImpl.getEnabledBanks - found {} enabled banks", enabledBanks.size());

        List<Map<String, Object>> banks = enabledBanks.stream()
                .map(b -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", b.getId());
                    // Use bank name directly from database as it already contains the correct format
                    // e.g., "Banco De Oro (BDO)" or "Chinabank" - no formatting needed
                    m.put("name", b.getName());
                    m.put("pattern", b.getPattern());
                    // Get minLength and maxLength from BankAccountValidationService based on bank name
                    Integer minLength = bankAccountValidationService.getMinLength(b.getName());
                    Integer maxLength = bankAccountValidationService.getMaxLength(b.getName());
                    m.put("minLength", minLength);
                    m.put("maxLength", maxLength);
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("banks", banks);
        return response;
    }
}
