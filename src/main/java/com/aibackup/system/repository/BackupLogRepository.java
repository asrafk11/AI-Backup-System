package com.aibackup.system.repository;

import com.aibackup.system.entity.BackupLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BackupLogRepository extends JpaRepository<BackupLog, Long> {
}
