package com.aibackup.system.repository;

import com.aibackup.system.entity.BackupLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BackupLogRepository extends JpaRepository<BackupLog, Long> {

    // 🔥 Optional: Get logs sorted by latest
    List<BackupLog> findAllByOrderByTimestampDesc();
}