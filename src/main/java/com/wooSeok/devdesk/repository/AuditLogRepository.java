package com.wooSeok.devdesk.repository;

import com.wooSeok.devdesk.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}