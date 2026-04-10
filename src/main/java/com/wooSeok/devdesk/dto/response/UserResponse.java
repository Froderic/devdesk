package com.wooSeok.devdesk.dto.response;

import com.wooSeok.devdesk.domain.enums.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private Role role;
    private LocalDateTime createdAt;
}