package com.loganalyzer.repository;

import com.loganalyzer.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {
    List<Log> findByUserIdOrderByUploadedAtDesc(Long userId);
    long countByUserId(Long userId);
}
