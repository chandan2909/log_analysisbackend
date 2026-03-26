package com.loganalyzer.repository;

import com.loganalyzer.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {

    List<LogEntry> findByLogId(Long logId);

    List<LogEntry> findByLogIdAndLevel(Long logId, String level);

    long countByLogIdAndLevel(Long logId, String level);

    long countByLogId(Long logId);

    @Query("SELECT e FROM LogEntry e WHERE e.log.id = :logId AND LOWER(e.message) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<LogEntry> searchByKeyword(@Param("logId") Long logId, @Param("keyword") String keyword);

    @Query("SELECT e FROM LogEntry e WHERE e.log.user.id = :userId AND e.level = :level")
    List<LogEntry> findByUserIdAndLevel(@Param("userId") Long userId, @Param("level") String level);

    @Query("SELECT COUNT(e) FROM LogEntry e WHERE e.log.user.id = :userId AND e.level = :level")
    long countByUserIdAndLevel(@Param("userId") Long userId, @Param("level") String level);

    @Query("SELECT COUNT(e) FROM LogEntry e WHERE e.log.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}
