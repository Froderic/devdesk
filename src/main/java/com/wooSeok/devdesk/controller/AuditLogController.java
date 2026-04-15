package com.wooSeok.devdesk.controller;

import com.wooSeok.devdesk.dto.response.AuditLogResponse;
import com.wooSeok.devdesk.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(@PathVariable Long ticketId) {
        return ResponseEntity.ok(auditLogService.getLogsForTicket(ticketId));
    }
}