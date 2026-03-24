package com.medgo.loaservice.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteDTO {

    private Long portalUserId;

    private Long requestId;

    @NotBlank(message = "Recipient is either PRIVATE or PUBLIC only.")
    private String recipient;

    @Size(max = 1000, message = "Message value should be between 3 and 1000 characters.")
    private String message;

    private String senderName;
    private String senderRole;

    private Long requestNoteId;
}

