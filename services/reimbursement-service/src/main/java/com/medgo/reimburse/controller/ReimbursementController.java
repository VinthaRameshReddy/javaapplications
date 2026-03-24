package com.medgo.reimburse.controller;

//import com.medgo.commons.CommonResponse;
//import com.medgo.crypto.annotation.EncryptResponse;
import com.medgo.reimburse.domain.dto.ReimHistoryDTO;
import com.medgo.reimburse.domain.response.CommonResponse;
import com.medgo.reimburse.service.ReimbursementsService;
import com.org.authframework.common.annotation.EncryptResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReimbursementController {
    private final ReimbursementsService reimbursementsService;

    @GetMapping("/history")
   // @EncryptResponse
    public CommonResponse memberRegistration(
            @RequestParam(name = "memberCode") String memberCode) {

        Map<String, List<ReimHistoryDTO>> reimbursementHistory =
                reimbursementsService.getReimbursementHistory(
                        memberCode);
        return CommonResponse.success(reimbursementHistory);
    }


//    ==========================================================================================
}
