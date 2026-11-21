package com.wooSeok.devdesk.service;

import com.wooSeok.devdesk.domain.entity.Project;
import com.wooSeok.devdesk.domain.entity.User;
import com.wooSeok.devdesk.domain.enums.TicketStatus;
import com.wooSeok.devdesk.dto.request.CreateProjectRequest;
import com.wooSeok.devdesk.dto.response.ProjectResponse;
import com.wooSeok.devdesk.dto.response.ProjectStatsResponse;
import com.wooSeok.devdesk.exception.ResourceNotFoundException;
import com.wooSeok.devdesk.repository.ProjectRepository;
import com.wooSeok.devdesk.repository.TicketRepository;
import com.wooSeok.devdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        User createdBy = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getCreatedById()));

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(createdBy)
                .build();

        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#id")
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        return toResponse(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getAllProjects(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return projectRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional
    @CacheEvict(value = "projects", key = "#id")
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        projectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "project-stats", key = "#projectId")
    public ProjectStatsResponse getProjectStats(Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        return ProjectStatsResponse.builder()
                .total(ticketRepository.countByProjectId(projectId))
                .open(ticketRepository.countByProjectIdAndStatus(projectId, TicketStatus.OPEN))
                .inProgress(ticketRepository.countByProjectIdAndStatus(projectId, TicketStatus.IN_PROGRESS))
                .resolved(ticketRepository.countByProjectIdAndStatus(projectId, TicketStatus.RESOLVED))
                .build();
    }

    private ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .createdByName(project.getCreatedBy().getName())
                .createdAt(project.getCreatedAt())
                .build();
    }
}