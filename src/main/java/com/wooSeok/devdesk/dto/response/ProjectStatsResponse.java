package com.wooSeok.devdesk.dto.response;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ProjectStatsResponse implements Serializable {
    private Long total;
    private Long open;
    private Long inProgress;
    private Long resolved;
}