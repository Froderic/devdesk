package com.wooSeok.devdesk.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private String createdByName;
    private LocalDateTime createdAt;
}