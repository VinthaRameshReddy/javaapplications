package com.medgo.reimburse.service;


import com.medgo.reimburse.domain.dto.ReimHistoryDTO;

import java.util.List;
import java.util.Map;

public interface ReimbursementsService {
    Map<String, List<ReimHistoryDTO>> getReimbursementHistory(String memberCode);
}
