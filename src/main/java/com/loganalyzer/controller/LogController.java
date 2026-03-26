package com.loganalyzer.controller;

import com.loganalyzer.dto.LogEntryResponse;
import com.loganalyzer.dto.LogResponse;
import com.loganalyzer.model.User;
import com.loganalyzer.repository.UserRepository;
import com.loganalyzer.service.LogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Log management endpoints – upload, list, detail, entries.
 */
@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogService logService;
    private final UserRepository userRepository;

    public LogController(LogService logService, UserRepository userRepository) {
        this.logService = logService;
        this.userRepository = userRepository;
    }

    /**
     * Upload a log file (.txt) or raw text.
     */
    @PostMapping("/upload")
    public ResponseEntity<LogResponse> uploadLog(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "rawContent", required = false) String rawContent,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String content;
        String fileName;

        if (file != null && !file.isEmpty()) {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
            fileName = file.getOriginalFilename();
        } else if (rawContent != null && !rawContent.isBlank()) {
            content = rawContent;
            fileName = "pasted-log";
        } else {
            return ResponseEntity.badRequest().build();
        }

        LogResponse response = logService.uploadLog(content, fileName, user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all logs for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<LogResponse>> getUserLogs(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(logService.getUserLogs(user.getId()));
    }

    /**
     * Get a specific log's metadata.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LogResponse> getLog(@PathVariable Long id) {
        return ResponseEntity.ok(logService.getLogById(id));
    }

    /**
     * Get parsed entries for a log with optional filters.
     */
    @GetMapping("/{id}/entries")
    public ResponseEntity<List<LogEntryResponse>> getLogEntries(
            @PathVariable Long id,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(logService.getLogEntries(id, level, keyword));
    }
}
