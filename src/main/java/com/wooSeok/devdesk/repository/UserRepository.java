package com.wooSeok.devdesk.repository;

import com.wooSeok.devdesk.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {}