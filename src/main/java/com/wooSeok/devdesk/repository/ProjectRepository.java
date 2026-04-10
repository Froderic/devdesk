package com.wooSeok.devdesk.repository;

import com.wooSeok.devdesk.domain.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {}
