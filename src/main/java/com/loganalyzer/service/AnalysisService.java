package com.loganalyzer.service;

import com.loganalyzer.dto.AnalysisResponse;
import com.loganalyzer.model.LogEntry;
import com.loganalyzer.repository.LogEntryRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analysis service – aggregates log data, computes stats, errors-over-time.
 */
@Service
public class AnalysisService {

    private final LogEntryRepository logEntryRepository;
    private final RootCauseService rootCauseService;

    public AnalysisService(LogEntryRepository logEntryRepository, RootCauseService rootCauseService) {
        this.logEntryRepository = logEntryRepository;
        this.rootCauseService = rootCauseService;
    }

    /**
     * Analyze a specific log by ID.
     */
    public AnalysisResponse analyzeLog(Long logId) {
        List<LogEntry> entries = logEntryRepository.findByLogId(logId);
        return buildAnalysis(entries);
    }

    /**
     * Aggregate analysis across all logs for a user.
     */
    public AnalysisResponse analyzeUserLogs(Long userId) {
        long totalEntries = logEntryRepository.countByUserId(userId);
        long errorCount = logEntryRepository.countByUserIdAndLevel(userId, "ERROR");
        long warningCount = logEntryRepository.countByUserIdAndLevel(userId, "WARNING");
        long infoCount = logEntryRepository.countByUserIdAndLevel(userId, "INFO");

        // Get all error entries for root cause analysis
        List<LogEntry> errorEntries = logEntryRepository.findByUserIdAndLevel(userId, "ERROR");

        // Build errors over time
        Map<String, Long> errorsOverTime = errorEntries.stream()
                .filter(e -> e.getTimestamp() != null && !e.getTimestamp().isBlank())
                .collect(Collectors.groupingBy(
                        e -> e.getTimestamp().substring(0, Math.min(e.getTimestamp().length(), 10)),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        // Error frequency
        Map<String, Long> errorFrequency = errorEntries.stream()
                .collect(Collectors.groupingBy(
                        e -> truncateMessage(e.getMessage()),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        // Root cause analysis
        List<AnalysisResponse.RootCause> rootCauses = rootCauseService.detectRootCauses(errorEntries);

        return AnalysisResponse.builder()
                .totalEntries(totalEntries)
                .errorCount(errorCount)
                .warningCount(warningCount)
                .infoCount(infoCount)
                .errorsOverTime(errorsOverTime)
                .errorFrequency(errorFrequency)
                .rootCauses(rootCauses)
                .build();
    }

    // ── Private ──

    private AnalysisResponse buildAnalysis(List<LogEntry> entries) {
        long errorCount = entries.stream().filter(e -> "ERROR".equals(e.getLevel())).count();
        long warningCount = entries.stream().filter(e -> "WARNING".equals(e.getLevel())).count();
        long infoCount = entries.stream().filter(e -> "INFO".equals(e.getLevel())).count();

        // Errors over time (group by date portion of timestamp)
        Map<String, Long> errorsOverTime = entries.stream()
                .filter(e -> "ERROR".equals(e.getLevel()) && e.getTimestamp() != null && !e.getTimestamp().isBlank())
                .collect(Collectors.groupingBy(
                        e -> e.getTimestamp().substring(0, Math.min(e.getTimestamp().length(), 10)),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        // Top error messages by frequency
        Map<String, Long> errorFrequency = entries.stream()
                .filter(e -> "ERROR".equals(e.getLevel()))
                .collect(Collectors.groupingBy(
                        e -> truncateMessage(e.getMessage()),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        // Root causes from error entries
        List<LogEntry> errorEntries = entries.stream()
                .filter(e -> "ERROR".equals(e.getLevel()))
                .collect(Collectors.toList());
        List<AnalysisResponse.RootCause> rootCauses = rootCauseService.detectRootCauses(errorEntries);

        return AnalysisResponse.builder()
                .totalEntries(entries.size())
                .errorCount(errorCount)
                .warningCount(warningCount)
                .infoCount(infoCount)
                .errorsOverTime(errorsOverTime)
                .errorFrequency(errorFrequency)
                .rootCauses(rootCauses)
                .build();
    }

    private String truncateMessage(String message) {
        if (message == null) return "Unknown";
        return message.length() > 80 ? message.substring(0, 80) + "..." : message;
    }
}
