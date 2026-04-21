package com.wooSeok.devdesk.dto.response;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ProjectResponse implements Serializable {
    private Long id;
    private String name;
    private String description;
    private String createdByName;
    private LocalDateTime createdAt;
}