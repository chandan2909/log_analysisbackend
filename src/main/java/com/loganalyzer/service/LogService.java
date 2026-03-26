package com.loganalyzer.service;

import com.loganalyzer.dto.LogEntryResponse;
import com.loganalyzer.dto.LogResponse;
import com.loganalyzer.exception.ResourceNotFoundException;
import com.loganalyzer.model.*;
import com.loganalyzer.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Log service – handles upload, parsing, and retrieval.
 */
@Service
public class LogService {

    private final LogRepository logRepository;
    private final LogEntryRepository logEntryRepository;
    private final UserRepository userRepository;

    // Regex to parse log lines: [TIMESTAMP] LEVEL message
    // Matches patterns like:
    //   2024-01-15 10:30:45 ERROR Something failed
    //   [2024-01-15T10:30:45] WARN Warning message
    //   ERROR: NullPointerException
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^\\[?([\\d]{4}-[\\d]{2}-[\\d]{2}[T ]?[\\d]{2}:[\\d]{2}:[\\d]{2}[.\\d]*)?\\]?\\s*" +
            "(ERROR|WARN(?:ING)?|INFO|DEBUG|FATAL|TRACE)[:\\s]+(.+)$",
            Pattern.CASE_INSENSITIVE
    );

    public LogService(LogRepository logRepository,
                      LogEntryRepository logEntryRepository,
                      UserRepository userRepository) {
        this.logRepository = logRepository;
        this.logEntryRepository = logEntryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Upload and parse a log file or raw text.
     */
    @Transactional
    public LogResponse uploadLog(String rawContent, String fileName, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Log log = Log.builder()
                .rawContent(rawContent)
                .fileName(fileName != null ? fileName : "pasted-log")
                .uploadedAt(LocalDateTime.now())
                .user(user)
                .build();

        log = logRepository.save(log);

        // Parse log content line by line
        List<LogEntry> entries = parseLogContent(rawContent, log);
        logEntryRepository.saveAll(entries);

        return buildLogResponse(log);
    }

    /**
     * Get all logs for a user.
     */
    public List<LogResponse> getUserLogs(Long userId) {
        List<Log> logs = logRepository.findByUserIdOrderByUploadedAtDesc(userId);
        return logs.stream().map(this::buildLogResponse).collect(Collectors.toList());
    }

    /**
     * Get a specific log by ID.
     */
    public LogResponse getLogById(Long logId) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Log not found"));
        return buildLogResponse(log);
    }

    /**
     * Get entries for a specific log, optionally filtered by level and keyword.
     */
    public List<LogEntryResponse> getLogEntries(Long logId, String level, String keyword) {
        List<LogEntry> entries;

        if (keyword != null && !keyword.isBlank()) {
            entries = logEntryRepository.searchByKeyword(logId, keyword);
        } else if (level != null && !level.isBlank()) {
            entries = logEntryRepository.findByLogIdAndLevel(logId, level.toUpperCase());
        } else {
            entries = logEntryRepository.findByLogId(logId);
        }

        // Apply level filter on keyword results too
        if (keyword != null && !keyword.isBlank() && level != null && !level.isBlank()) {
            String upperLevel = level.toUpperCase();
            entries = entries.stream()
                    .filter(e -> e.getLevel().equals(upperLevel))
                    .collect(Collectors.toList());
        }

        return entries.stream()
                .map(e -> LogEntryResponse.builder()
                        .id(e.getId())
                        .level(e.getLevel())
                        .message(e.getMessage())
                        .timestamp(e.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

    // ── Private helpers ──

    private List<LogEntry> parseLogContent(String rawContent, Log log) {
        List<LogEntry> entries = new ArrayList<>();
        String[] lines = rawContent.split("\\r?\\n");

        for (String line : lines) {
            if (line.isBlank()) continue;

            Matcher matcher = LOG_PATTERN.matcher(line.trim());

            if (matcher.find()) {
                String ts = matcher.group(1);
                String level = normalizeLevel(matcher.group(2));
                String message = matcher.group(3).trim();

                entries.add(LogEntry.builder()
                        .timestamp(ts != null ? ts : "")
                        .level(level)
                        .message(message)
                        .log(log)
                        .build());
            } else {
                // Lines that don't match the pattern → treat as INFO
                entries.add(LogEntry.builder()
                        .timestamp("")
                        .level("INFO")
                        .message(line.trim())
                        .log(log)
                        .build());
            }
        }

        return entries;
    }

    private String normalizeLevel(String level) {
        String upper = level.toUpperCase();
        if (upper.equals("WARN") || upper.equals("WARNING")) return "WARNING";
        if (upper.equals("FATAL")) return "ERROR";
        return upper;
    }

    private LogResponse buildLogResponse(Log log) {
        long total = logEntryRepository.countByLogId(log.getId());
        long errors = logEntryRepository.countByLogIdAndLevel(log.getId(), "ERROR");
        long warnings = logEntryRepository.countByLogIdAndLevel(log.getId(), "WARNING");
        long infos = logEntryRepository.countByLogIdAndLevel(log.getId(), "INFO");

        return LogResponse.builder()
                .id(log.getId())
                .fileName(log.getFileName())
                .uploadedAt(log.getUploadedAt())
                .totalEntries(total)
                .errorCount(errors)
                .warningCount(warnings)
                .infoCount(infos)
                .build();
    }
}
