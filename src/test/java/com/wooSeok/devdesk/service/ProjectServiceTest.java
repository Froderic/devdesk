package com.wooSeok.devdesk.service;

import com.wooSeok.devdesk.domain.entity.Project;
import com.wooSeok.devdesk.domain.entity.User;
import com.wooSeok.devdesk.domain.enums.Role;
import com.wooSeok.devdesk.domain.enums.TicketStatus;
import com.wooSeok.devdesk.dto.request.CreateProjectRequest;
import com.wooSeok.devdesk.dto.response.ProjectResponse;
import com.wooSeok.devdesk.dto.response.ProjectStatsResponse;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private ProjectService projectService;

    private User testUser;
    private Project testProject;

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
    }

    @Test
    void createProject_success() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Test Project");
        request.setDescription("Test Description");
        request.setCreatedById(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        ProjectResponse response = projectService.createProject(request);

        assertThat(response.getName()).isEqualTo("Test Project");
        assertThat(response.getCreatedByName()).isEqualTo("Test User");
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProject_userNotFound_throwsException() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Test Project");
        request.setCreatedById(99L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void getProjectById_success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        ProjectResponse response = projectService.getProjectById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test Project");
    }

    @Test
    void getProjectById_notFound_throwsException() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllProjects_returnsPagedResults() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Project> page = new PageImpl<>(List.of(testProject), pageable, 1);

        when(projectRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<ProjectResponse> responses = projectService.getAllProjects(0, 10);

        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).getName()).isEqualTo("Test Project");
    }

    @Test
    void deleteProject_success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        projectService.deleteProject(1L);

        verify(projectRepository).delete(testProject);
    }

    @Test
    void deleteProject_notFound_throwsException() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.deleteProject(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getProjectStats_success() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(ticketRepository.countByProjectId(1L)).thenReturn(3L);
        when(ticketRepository.countByProjectIdAndStatus(1L, TicketStatus.OPEN)).thenReturn(1L);
        when(ticketRepository.countByProjectIdAndStatus(1L, TicketStatus.IN_PROGRESS)).thenReturn(1L);
        when(ticketRepository.countByProjectIdAndStatus(1L, TicketStatus.RESOLVED)).thenReturn(1L);

        ProjectStatsResponse stats = projectService.getProjectStats(1L);

        assertThat(stats.getTotal()).isEqualTo(3L);
        assertThat(stats.getOpen()).isEqualTo(1L);
        assertThat(stats.getInProgress()).isEqualTo(1L);
        assertThat(stats.getResolved()).isEqualTo(1L);
    }
}