package com.wooSeok.devdesk.dto.response;

import com.wooSeok.devdesk.domain.enums.Role;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserResponse implements Serializable {
    private Long id;
    private String email;
    private String name;
    private Role role;
    private LocalDateTime createdAt;
}