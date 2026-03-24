package com.medgo.claims.service.impl;

import com.medgo.claims.domain.entity.medigo.ClaimNatureMaster;
import com.medgo.claims.repository.medigo.ClaimMasterRepository;
import com.medgo.claims.service.ClaimNatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimNatureServiceImpl implements ClaimNatureService {

    private final ClaimMasterRepository claimMasterRepository;

    @Override
    public Map<String, List<String>> getClaimNatureByServiceType() {
        List<ClaimNatureMaster> inpatient =
                claimMasterRepository.findByServiceTypeOrderByIdAsc("INPATIENT");
        List<ClaimNatureMaster> outpatient =
                claimMasterRepository.findByServiceTypeOrderByIdAsc("OUTPATIENT");

        Map<String, List<String>> response = new HashMap<>();
        
        // Get inpatient claim natures and sort alphabetically
        List<String> inpatientList = inpatient.stream()
                .map(ClaimNatureMaster::getClaimNature)
                .sorted(Comparator.naturalOrder())        
                .collect(Collectors.toList());
        
        // Get outpatient claim natures and sort alphabetically
        List<String> outpatientList = outpatient.stream()
                .map(ClaimNatureMaster::getClaimNature)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        
        response.put("inpatient", inpatientList);
        response.put("outpatient", outpatientList);

        return response;
    }
}
