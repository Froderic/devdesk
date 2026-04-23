package com.wooSeok.devdesk.dto.response;

import com.wooSeok.devdesk.domain.enums.Priority;
import com.wooSeok.devdesk.domain.enums.TicketStatus;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class TicketResponse implements Serializable {
    private Long id;
    private Long version;
    private String title;
    private String description;
    private TicketStatus status;
    private Priority priority;
    private String projectName;
    private String reporterName;
    private String assigneeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}