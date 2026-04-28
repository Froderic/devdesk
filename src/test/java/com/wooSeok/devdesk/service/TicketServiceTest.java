package com.wooSeok.devdesk.service;

import com.wooSeok.devdesk.domain.entity.Project;
import com.wooSeok.devdesk.domain.entity.Ticket;
import com.wooSeok.devdesk.domain.entity.User;
import com.wooSeok.devdesk.domain.enums.Priority;
import com.wooSeok.devdesk.domain.enums.Role;
import com.wooSeok.devdesk.domain.enums.TicketStatus;
import com.wooSeok.devdesk.dto.request.CreateTicketRequest;
import com.wooSeok.devdesk.dto.request.UpdateTicketRequest;
import com.wooSeok.devdesk.dto.response.TicketResponse;
import com.wooSeok.devdesk.exception.InvalidStatusTransitionException;
import com.wooSeok.devdesk.exception.OptimisticLockException;
import com.wooSeok.devdesk.exception.ResourceNotFoundException;
import com.wooSeok.devdesk.repository.ProjectRepository;
import com.wooSeok.devdesk.repository.TicketRepository;
import com.wooSeok.devdesk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private TicketService ticketService;

    private User testUser;
    private Project testProject;
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

        testProject = Project.builder()
                .id(1L)
                .name("Test Project")
                .description("Test Description")
                .createdBy(testUser)
                .createdAt(LocalDateTime.now())
                .build();

        testTicket = Ticket.builder()
                .id(1L)
                .title("Test Ticket")
                .description("Test Description")
                .status(TicketStatus.OPEN)
                .priority(Priority.MEDIUM)
                .project(testProject)
                .reporter(testUser)
                .assignee(null)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createTicket_success() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Test Ticket");
        request.setDescription("Test Description");
        request.setPriority(Priority.MEDIUM);
        request.setProjectId(1L);
        request.setReporterId(1L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        TicketResponse response = ticketService.createTicket(request);

        assertThat(response.getTitle()).isEqualTo("Test Ticket");
        assertThat(response.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(response.getPriority()).isEqualTo(Priority.MEDIUM);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void createTicket_projectNotFound_throwsException() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Test Ticket");
        request.setProjectId(99L);
        request.setReporterId(1L);

        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.createTicket(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void getTicketById_success() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        TicketResponse response = ticketService.getTicketById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Ticket");
    }

    @Test
    void getTicketById_notFound_throwsException() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicketById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateTicket_validStatusTransition_success() {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setStatus(TicketStatus.IN_PROGRESS);
        request.setChangedById(1L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        TicketResponse response = ticketService.updateTicket(1L, request);

        assertThat(response).isNotNull();
        verify(auditLogService).log(any(Ticket.class), eq(1L), eq("status"),
                eq("OPEN"), eq("IN_PROGRESS"));
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void updateTicket_invalidStatusTransition_throwsException() {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setStatus(TicketStatus.CLOSED);
        request.setChangedById(1L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        assertThatThrownBy(() -> ticketService.updateTicket(1L, request))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void updateTicket_optimisticLock_throwsException() {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setTitle("Updated Title");
        request.setChangedById(1L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Ticket.class, 1L));

        assertThatThrownBy(() -> ticketService.updateTicket(1L, request))
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void deleteTicket_success() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        ticketService.deleteTicket(1L);

        verify(ticketRepository).delete(testTicket);
    }

    @Test
    void deleteTicket_notFound_throwsException() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.deleteTicket(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllTickets_returnsPagedResults() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Ticket> page = new PageImpl<>(List.of(testTicket), pageable, 1);

        when(ticketRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<TicketResponse> responses = ticketService.getAllTickets(0, 10);

        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).getTitle()).isEqualTo("Test Ticket");
    }
}