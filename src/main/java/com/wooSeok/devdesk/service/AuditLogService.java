package com.wooSeok.devdesk.service;

import com.wooSeok.devdesk.domain.entity.AuditLog;
import com.wooSeok.devdesk.domain.entity.Ticket;
import com.wooSeok.devdesk.domain.entity.User;
import com.wooSeok.devdesk.dto.response.AuditLogResponse;
import com.wooSeok.devdesk.exception.ResourceNotFoundException;
import com.wooSeok.devdesk.repository.AuditLogRepository;
import com.wooSeok.devdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public void log(Ticket ticket, Long changedById, String fieldChanged,
                    String oldValue, String newValue) {
        User changedBy = userRepository.findById(changedById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + changedById));

        AuditLog log = AuditLog.builder()
                .ticket(ticket)
                .changedBy(changedBy)
                .fieldChanged(fieldChanged)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

        auditLogRepository.save(log);
    }

    public List<AuditLogResponse> getLogsForTicket(Long ticketId) {
        return auditLogRepository.findByTicketIdOrderByChangedAtAsc(ticketId)
                .stream()
                .map(log -> AuditLogResponse.builder()
                        .id(log.getId())
                        .fieldChanged(log.getFieldChanged())
                        .oldValue(log.getOldValue())
                        .newValue(log.getNewValue())
                        .changedByName(log.getChangedBy().getName())
                        .changedAt(log.getChangedAt())
                        .build())
                .toList();
    }
}