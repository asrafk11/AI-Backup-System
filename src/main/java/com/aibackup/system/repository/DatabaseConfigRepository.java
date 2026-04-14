package com.aibackup.system.repository;

import com.aibackup.system.entity.DatabaseConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DatabaseConfigRepository
        extends JpaRepository<DatabaseConfig, UUID> {

    List<DatabaseConfig> findByUserId(UUID userId);
}
