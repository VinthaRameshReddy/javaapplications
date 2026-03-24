
package com.medgo.utils.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuditLogRequest(@JsonProperty("mobile_no") String mobile,
                              @JsonProperty String login_id,
                              @JsonProperty("txn_ref_num") String txn_ref_num,
                              @JsonProperty("application_id") String applicationId) {
}
