package com.wooSeok.devdesk.repository;

import com.wooSeok.devdesk.domain.entity.Ticket;
import com.wooSeok.devdesk.domain.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    long countByProjectId(Long projectId);
    long countByProjectIdAndStatus(Long projectId, TicketStatus status);
    Page<Ticket> findAll(Pageable pageable);
    Page<Ticket> findByProjectIdAndStatus(Long projectId, TicketStatus status, Pageable pageable);
    Page<Ticket> findByProjectId(Long projectId, Pageable pageable);
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);
}
