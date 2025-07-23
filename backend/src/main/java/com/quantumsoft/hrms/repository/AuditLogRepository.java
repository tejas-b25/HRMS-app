package com.quantumsoft.hrms.repository;

import com.quantumsoft.hrms.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Optional<List<AuditLog>> findByUserId(UUID userId);
}
