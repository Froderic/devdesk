package com.wooSeok.devdesk.service;

import com.wooSeok.devdesk.domain.entity.AuditLog;
import com.wooSeok.devdesk.domain.entity.Project;
import com.wooSeok.devdesk.domain.entity.Ticket;
import com.wooSeok.devdesk.domain.entity.User;
import com.wooSeok.devdesk.domain.enums.Priority;
import com.wooSeok.devdesk.domain.enums.Role;
import com.wooSeok.devdesk.domain.enums.TicketStatus;
import com.wooSeok.devdesk.dto.response.AuditLogResponse;
import com.wooSeok.devdesk.exception.ResourceNotFoundException;
import com.wooSeok.devdesk.repository.AuditLogRepository;
import com.wooSeok.devdesk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private User testUser;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@devdesk.com")
                .password("hashedPassword")
                .name("Test User")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();

        Project testProject = Project.builder()
                .id(1L)
                .name("Test Project")
                .createdBy(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        testTicket = Ticket.builder()
                .id(1L)
                .title("Test Ticket")
                .status(TicketStatus.OPEN)
                .priority(Priority.MEDIUM)
                .project(testProject)
                .reporter(testUser)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void log_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        auditLogService.log(testTicket, 1L, "status", "OPEN", "IN_PROGRESS");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void log_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditLogService.log(testTicket, 99L, "status", "OPEN", "IN_PROGRESS"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    void getLogsForTicket_success() {
        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .ticket(testTicket)
                .changedBy(testUser)
                .fieldChanged("status")
                .oldValue("OPEN")
                .newValue("IN_PROGRESS")
                .changedAt(LocalDateTime.now())
                .build();

        when(auditLogRepository.findByTicketIdOrderByChangedAtAsc(1L))
                .thenReturn(List.of(auditLog));

        List<AuditLogResponse> responses = auditLogService.getLogsForTicket(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getFieldChanged()).isEqualTo("status");
        assertThat(responses.get(0).getOldValue()).isEqualTo("OPEN");
        assertThat(responses.get(0).getNewValue()).isEqualTo("IN_PROGRESS");
        assertThat(responses.get(0).getChangedByName()).isEqualTo("Test User");
    }

    @Test
    void getLogsForTicket_empty_returnsEmptyList() {
        when(auditLogRepository.findByTicketIdOrderByChangedAtAsc(1L))
                .thenReturn(List.of());

        List<AuditLogResponse> responses = auditLogService.getLogsForTicket(1L);

        assertThat(responses).isEmpty();
    }
}