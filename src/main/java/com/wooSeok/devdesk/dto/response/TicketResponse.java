package com.wooSeok.devdesk.dto.response;

import com.wooSeok.devdesk.domain.enums.Priority;
import com.wooSeok.devdesk.domain.enums.TicketStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TicketResponse {
    private Long id;
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