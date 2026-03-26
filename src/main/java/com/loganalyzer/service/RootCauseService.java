package com.loganalyzer.service;

import com.loganalyzer.dto.AnalysisResponse;
import com.loganalyzer.model.LogEntry;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Rule-based root cause detection service.
 * Matches known error patterns to likely causes and suggestions.
 */
@Service
public class RootCauseService {

    // Each rule: pattern keyword → (cause, suggestion)
    private static final List<RootCauseRule> RULES = List.of(
        new RootCauseRule("NullPointerException",
                "Null Reference",
                "Check for uninitialized variables or missing null checks before accessing object properties."),
        new RootCauseRule("OutOfMemoryError",
                "Memory Exhaustion",
                "Investigate memory leaks, increase heap size (-Xmx), or optimize large data processing."),
        new RootCauseRule("StackOverflowError",
                "Infinite Recursion",
                "Check for recursive method calls without proper base case termination."),
        new RootCauseRule("ClassNotFoundException",
                "Missing Dependency",
                "Verify classpath configuration, check that all required JARs/modules are included."),
        new RootCauseRule("FileNotFoundException",
                "Missing File",
                "Verify the file path exists and the application has read permissions."),
        new RootCauseRule("IOException",
                "I/O Failure",
                "Check file system permissions, disk space, or network connectivity for remote I/O."),
        new RootCauseRule("timeout",
                "Network Timeout",
                "Check network connectivity, increase timeout values, or verify remote service health."),
        new RootCauseRule("connection refused",
                "Service Unavailable",
                "Target service is not running or port is blocked. Verify the service is up and firewall rules."),
        new RootCauseRule("ConnectionPoolExhausted",
                "Connection Pool Exhausted",
                "Increase pool size, check for connection leaks, or optimize query performance."),
        new RootCauseRule("SQLException",
                "Database Error",
                "Check SQL query syntax, database connectivity, and table/column existence."),
        new RootCauseRule("deadlock",
                "Database Deadlock",
                "Review transaction isolation levels and lock ordering to prevent deadlocks."),
        new RootCauseRule("PermissionDenied",
                "Permission Denied",
                "Check file/resource permissions and ensure the process runs with appropriate privileges."),
        new RootCauseRule("401",
                "Authentication Failure",
                "Verify credentials, token expiration, and authentication configuration."),
        new RootCauseRule("403",
                "Authorization Failure",
                "User lacks required permissions. Check role-based access control rules."),
        new RootCauseRule("404",
                "Resource Not Found",
                "The requested endpoint or resource doesn't exist. Verify URL and routing config."),
        new RootCauseRule("500",
                "Internal Server Error",
                "Check server logs for stack traces and unhandled exceptions in the application."),
        new RootCauseRule("disk full",
                "Disk Space Exhausted",
                "Free up disk space, implement log rotation, or expand storage."),
        new RootCauseRule("ConcurrentModificationException",
                "Thread Safety Issue",
                "Use thread-safe collections or synchronization when accessing shared data structures."),
        new RootCauseRule("IllegalArgumentException",
                "Invalid Input",
                "Add input validation to catch invalid arguments before processing."),
        new RootCauseRule("NumberFormatException",
                "Invalid Number Format",
                "Validate numeric input strings before parsing. Add try-catch for parsing operations.")
    );

    /**
     * Detect root causes by matching error messages against known patterns.
     */
    public List<AnalysisResponse.RootCause> detectRootCauses(List<LogEntry> errorEntries) {
        Map<String, AnalysisResponse.RootCause> detected = new LinkedHashMap<>();

        for (RootCauseRule rule : RULES) {
            long count = errorEntries.stream()
                    .filter(e -> e.getMessage() != null &&
                            e.getMessage().toLowerCase().contains(rule.pattern.toLowerCase()))
                    .count();

            if (count > 0) {
                detected.put(rule.pattern, AnalysisResponse.RootCause.builder()
                        .pattern(rule.pattern)
                        .cause(rule.cause)
                        .suggestion(rule.suggestion)
                        .occurrences(count)
                        .build());
            }
        }

        return new ArrayList<>(detected.values());
    }

    // Internal rule class
    private record RootCauseRule(String pattern, String cause, String suggestion) {}
}
