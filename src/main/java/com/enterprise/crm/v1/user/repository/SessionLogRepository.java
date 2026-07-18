package com.enterprise.crm.v1.user.repository;

import com.enterprise.crm.v1.user.entity.SessionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SessionLogRepository extends JpaRepository<SessionLog, UUID> {
}
