package com.wooSeok.devdesk.repository;

import com.wooSeok.devdesk.domain.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {}
