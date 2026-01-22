package com.featureflag.featureflagplatform.repository;

import com.featureflag.featureflagplatform.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByFlagIdOrderByTimestampDesc(Long flagId);
}