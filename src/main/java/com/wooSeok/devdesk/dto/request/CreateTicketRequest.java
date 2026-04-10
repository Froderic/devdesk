package com.wooSeok.devdesk.dto.request;

import com.wooSeok.devdesk.domain.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTicketRequest {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private Priority priority;
    @NotNull
    private Long projectId;
    @NotNull
    private Long reporterId;
    private Long assigneeId;
}