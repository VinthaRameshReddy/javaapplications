package com.medgo.reimburse.serviceImpl;


import com.medgo.reimburse.domain.dto.ReimHistoryDTO;
import com.medgo.reimburse.domain.entity.ReimHistory;
import com.medgo.reimburse.domain.mapper.ReimHistoryMapper;
import com.medgo.reimburse.repository.ReimVWMedgo2RequestDetailsRepository;
import com.medgo.reimburse.repository.ReimbursementStatusRepository;
import com.medgo.reimburse.service.ReimbursementsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class ReimbursementsServiceImpl implements ReimbursementsService {
    private final ReimbursementStatusRepository statusRepository;
    private final ReimVWMedgo2RequestDetailsRepository viewMoreRepository;
    private final ReimHistoryMapper reimHistoryMapper;

    @Override
    public Map<String, List<ReimHistoryDTO>> getReimbursementHistory(String memberCode) {
        List<ReimHistory> statuses = statusRepository.findAllByMemberCode(memberCode);

        List<ReimHistoryDTO> list = statuses.stream()
                                            .filter(this::hasRequiredIdentifiers)
                                            .peek(this::enrichWithViewMoreDetails)
                                            .filter(status -> status.getMoreDetails() != null)
                                            .map(reimHistoryMapper::toDTO)
                                            .toList();

        if (list.isEmpty()) {
//            throw new CustomException(CustomStatusCode.NO_REIMBURSEMENT_HISTORY_FOUND.getCode(),
//                                     CustomStatusCode.NO_REIMBURSEMENT_HISTORY_FOUND.getMessage()

//            );
        }

        log.info("Final reimbursement list with viewMore: {}", list);
        return Map.of("reimbursementHistoryList", list);
    }

    private void enrichWithViewMoreDetails(ReimHistory status) {
        String controlCode = status.getControlCode();
        String entryCode = status.getEntryCode();
        String memberCode = status.getMemberCode();
        try {
            viewMoreRepository.findByControlCodeAndEntryCodeAndMemberCode(controlCode, entryCode, memberCode)
                              .ifPresentOrElse(
                                      status::setMoreDetails,
                                      () -> log.warn(
                                              "No view more details found for controlCode {}, entryCode {}, " +
                                                      "memberCode {}",
                                              controlCode, entryCode, memberCode
                                      )
                              );
        } catch (Exception ex) {
            log.error("Error fetching view more for controlCode {}: {}", controlCode, ex.getMessage());
        }
    }

    private boolean hasRequiredIdentifiers(ReimHistory status) {
        return StringUtils.hasText(status.getControlCode()) &&
                StringUtils.hasText(status.getEntryCode()) &&
                StringUtils.hasText(status.getMemberCode());
    }


}
