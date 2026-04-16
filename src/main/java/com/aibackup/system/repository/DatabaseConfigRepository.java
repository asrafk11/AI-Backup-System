package com.aibackup.system.repository;

import com.aibackup.system.entity.DatabaseConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DatabaseConfigRepository
        extends JpaRepository<DatabaseConfig, UUID> {

    List<DatabaseConfig> findByUserId(UUID userId);

    // 🔥 ADD THIS
    Optional<DatabaseConfig> findByHostAndPortAndDbNameAndUsername(
            String host,
            int port,
            String dbName,
            String username
    );
}