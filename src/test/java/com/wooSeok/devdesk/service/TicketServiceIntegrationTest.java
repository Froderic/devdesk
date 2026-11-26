package com.wooSeok.devdesk.service;

import com.wooSeok.devdesk.TestcontainersConfiguration;
import com.wooSeok.devdesk.domain.entity.Project;
import com.wooSeok.devdesk.domain.entity.Ticket;
import com.wooSeok.devdesk.domain.entity.User;
import com.wooSeok.devdesk.domain.enums.Priority;
import com.wooSeok.devdesk.domain.enums.Role;
import com.wooSeok.devdesk.domain.enums.TicketStatus;
import com.wooSeok.devdesk.dto.request.UpdateTicketRequest;
import com.wooSeok.devdesk.dto.response.AuditLogResponse;
import com.wooSeok.devdesk.dto.response.TicketResponse;
import com.wooSeok.devdesk.exception.OptimisticLockException;
import com.wooSeok.devdesk.repository.AuditLogRepository;
import com.wooSeok.devdesk.repository.ProjectRepository;
import com.wooSeok.devdesk.repository.TicketRepository;
import com.wooSeok.devdesk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "app.seed.enabled=false")
@Import(TestcontainersConfiguration.class)
class TicketServiceIntegrationTest {

    @Autowired private TicketService ticketService;
    @Autowired private AuditLogService auditLogService;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private AuditLogRepository auditLogRepository;

    private static final String EMAIL = "it@devdesk.dev";
    private Long ticketId;

    @BeforeEach
    void setUp() {
        // clean slate; order matters because of foreign keys
        auditLogRepository.deleteAll();
        ticketRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(User.builder()
                .email(EMAIL).name("IT User").role(Role.ADMIN).password("irrelevant").build());
        Project project = projectRepository.save(Project.builder()
                .name("IT Project").description("integration test").createdBy(user).build());
        Ticket ticket = ticketRepository.save(Ticket.builder()
                .project(project).title("IT ticket").description("integration test")
                .status(TicketStatus.OPEN).priority(Priority.MEDIUM).reporter(user).build());
        ticketId = ticket.getId();
    }

    @Test
    void updateTicket_realDatabase_returnsNewVersionAndWritesAuditLog() {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setStatus(TicketStatus.IN_PROGRESS);
        request.setVersion(0L);

        TicketResponse response = ticketService.updateTicket(ticketId, request, EMAIL);

        assertThat(response.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(response.getVersion()).isEqualTo(1L);              // saveAndFlush bug
        assertThat(response.getProjectName()).isEqualTo("IT Project"); // lazy-loading bug

        List<AuditLogResponse> logs = auditLogService.getLogsForTicket(ticketId);
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getChangedByName()).isEqualTo("IT User"); // identity from caller
    }

    @Test
    void updateTicket_staleVersion_throwsAndChangesNothing(){
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setStatus(TicketStatus.IN_PROGRESS);
        request.setVersion(0L);

        ticketService.updateTicket(ticketId, request, EMAIL);

        UpdateTicketRequest staleRequest = new UpdateTicketRequest();
        staleRequest.setTitle("Stale edit");
        staleRequest.setVersion(0L);

        assertThatThrownBy(() -> ticketService.updateTicket(ticketId, staleRequest, EMAIL))
                .isInstanceOf(OptimisticLockException.class);

        Ticket t =  ticketRepository.findById(ticketId).orElseThrow();
        assertThat(t.getTitle()).isEqualTo("IT ticket");

        List<AuditLogResponse> logs = auditLogService.getLogsForTicket(ticketId);
        assertThat(logs).hasSize(1);
    }

    @Test
    void getAllTickets_realDatabase_mapsLazyRelations(){
        Page<TicketResponse> p = ticketService.getAllTickets(null, null, 0, 10);
        assertThat(p.getTotalElements()).isEqualTo(1L);

        TicketResponse response = p.getContent().get(0);
        assertThat(response.getProjectName()).isEqualTo("IT Project");
        assertThat(response.getReporterName()).isEqualTo("IT User");
    }
}