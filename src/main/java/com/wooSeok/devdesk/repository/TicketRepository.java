package com.wooSeok.devdesk.repository;

import com.wooSeok.devdesk.domain.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {}
