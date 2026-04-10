package com.wooSeok.devdesk.dto.request;

import com.wooSeok.devdesk.domain.enums.Priority;
import com.wooSeok.devdesk.domain.enums.TicketStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTicketRequest {
    private String title;
    private String description;
    private TicketStatus status;
    private Priority priority;
    private Long assigneeId;
}