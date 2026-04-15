package com.wooSeok.devdesk.repository;

import com.wooSeok.devdesk.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTicketIdOrderByChangedAtAsc(Long ticketId);
}