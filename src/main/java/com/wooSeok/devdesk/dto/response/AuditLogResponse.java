package com.wooSeok.devdesk.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AuditLogResponse {
    private Long id;
    private String fieldChanged;
    private String oldValue;
    private String newValue;
    private String changedByName;
    private LocalDateTime changedAt;
}