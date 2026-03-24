package com.medgo.claims.service;

import java.util.List;
import java.util.Map;

public interface ClaimNatureService {
    Map<String, List<String>> getClaimNatureByServiceType();
}