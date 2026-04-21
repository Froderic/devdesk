package com.wooSeok.devdesk.dto.response;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AuditLogResponse implements Serializable {
    private Long id;
    private String fieldChanged;
    private String oldValue;
    private String newValue;
    private String changedByName;
    private LocalDateTime changedAt;
}