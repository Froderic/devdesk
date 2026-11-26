package com.wooSeok.devdesk.config;

import com.wooSeok.devdesk.domain.entity.Project;
import com.wooSeok.devdesk.domain.entity.Ticket;
import com.wooSeok.devdesk.domain.entity.User;
import com.wooSeok.devdesk.domain.enums.Priority;
import com.wooSeok.devdesk.domain.enums.Role;
import com.wooSeok.devdesk.domain.enums.TicketStatus;
import com.wooSeok.devdesk.repository.ProjectRepository;
import com.wooSeok.devdesk.repository.TicketRepository;
import com.wooSeok.devdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Seed skipped: database already has users");
            return;
        }

        String password = passwordEncoder.encode("password123");
        User admin    = userRepository.save(user("admin@devdesk.dev", "Ada Admin", Role.ADMIN, password));
        User dev      = userRepository.save(user("dev@devdesk.dev", "Dan Developer", Role.DEVELOPER, password));
        User reporter = userRepository.save(user("reporter@devdesk.dev", "Rae Reporter", Role.REPORTER, password));

        Project web = projectRepository.save(project("DevDesk Web", "React front end for DevDesk", admin));
        Project api = projectRepository.save(project("DevDesk API", "Spring Boot backend", admin));

        ticketRepository.saveAll(List.of(
                ticket(web, "Login form doesn't show server errors", TicketStatus.OPEN, Priority.HIGH, reporter, null),
                ticket(web, "Ticket list pagination off by one", TicketStatus.IN_PROGRESS, Priority.MEDIUM, reporter, dev),
                ticket(web, "Add status filter dropdown", TicketStatus.IN_REVIEW, Priority.LOW, admin, dev),
                ticket(web, "Dark mode colors too low contrast", TicketStatus.OPEN, Priority.LOW, reporter, null),
                ticket(api, "Expired token returns 500", TicketStatus.RESOLVED, Priority.CRITICAL, dev, dev),
                ticket(api, "Lazy loading error on ticket list", TicketStatus.CLOSED, Priority.HIGH, dev, dev),
                ticket(api, "Add CORS for React dev server", TicketStatus.IN_PROGRESS, Priority.HIGH, admin, dev),
                ticket(api, "Audit log records wrong assignee", TicketStatus.OPEN, Priority.MEDIUM, reporter, null)
        ));

        log.info("Seeded demo data: 3 users, 2 projects, 8 tickets (password: password123)");
    }

    private User user(String email, String name, Role role, String encodedPassword) {
        return User.builder().email(email).name(name).role(role).password(encodedPassword).build();
    }

    private Project project(String name, String description, User createdBy) {
        return Project.builder().name(name).description(description).createdBy(createdBy).build();
    }

    private Ticket ticket(Project project, String title, TicketStatus status, Priority priority,
                          User reporter, User assignee) {
        return Ticket.builder()
                .project(project).title(title).description(title + " (demo ticket)")
                .status(status).priority(priority).reporter(reporter).assignee(assignee)
                .build();
    }
}