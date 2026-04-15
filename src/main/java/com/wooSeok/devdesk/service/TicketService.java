package com.wooSeok.devdesk.service;

import com.wooSeok.devdesk.domain.entity.Project;
import com.wooSeok.devdesk.domain.entity.Ticket;
import com.wooSeok.devdesk.domain.entity.User;
import com.wooSeok.devdesk.domain.enums.TicketStatus;
import com.wooSeok.devdesk.dto.request.CreateTicketRequest;
import com.wooSeok.devdesk.dto.request.UpdateTicketRequest;
import com.wooSeok.devdesk.dto.response.TicketResponse;
import com.wooSeok.devdesk.exception.InvalidStatusTransitionException;
import com.wooSeok.devdesk.exception.ResourceNotFoundException;
import com.wooSeok.devdesk.repository.ProjectRepository;
import com.wooSeok.devdesk.repository.TicketRepository;
import com.wooSeok.devdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public TicketResponse createTicket(CreateTicketRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.getProjectId()));

        User reporter = userRepository.findById(request.getReporterId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getReporterId()));

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getAssigneeId()));
        }

        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TicketStatus.OPEN)
                .priority(request.getPriority())
                .project(project)
                .reporter(reporter)
                .assignee(assignee)
                .build();

        Ticket saved = ticketRepository.save(ticket);
        return toResponse(saved);
    }

    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
        return toResponse(ticket);
    }

    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TicketResponse updateTicket(Long id, UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));

        if (request.getTitle() != null) ticket.setTitle(request.getTitle());
        if (request.getDescription() != null) ticket.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            if (!ticket.getStatus().canTransitionTo(request.getStatus())) {
                throw new InvalidStatusTransitionException(ticket.getStatus(), request.getStatus());
            }
            auditLogService.log(
                    ticket,
                    request.getChangedById(),
                    "status",
                    ticket.getStatus().name(),
                    request.getStatus().name()
            );
            ticket.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) ticket.setPriority(request.getPriority());

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getAssigneeId()));
            ticket.setAssignee(assignee);
        }

        return toResponse(ticketRepository.save(ticket));
    }

    public void deleteTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
        ticketRepository.delete(ticket);
    }

    private TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .projectName(ticket.getProject().getName())
                .reporterName(ticket.getReporter().getName())
                .assigneeName(ticket.getAssignee() != null ? ticket.getAssignee().getName() : null)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}