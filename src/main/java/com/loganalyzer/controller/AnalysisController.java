package com.loganalyzer.controller;

import com.loganalyzer.dto.AnalysisResponse;
import com.loganalyzer.model.User;
import com.loganalyzer.repository.UserRepository;
import com.loganalyzer.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Analysis endpoints – per-log and global analysis.
 */
@RestController
@RequestMapping("/api/logs")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final UserRepository userRepository;

    public AnalysisController(AnalysisService analysisService, UserRepository userRepository) {
        this.analysisService = analysisService;
        this.userRepository = userRepository;
    }

    /**
     * Analyze a specific log.
     */
    @GetMapping("/{id}/analysis")
    public ResponseEntity<AnalysisResponse> analyzeLog(@PathVariable Long id) {
        return ResponseEntity.ok(analysisService.analyzeLog(id));
    }

    /**
     * Get aggregate analysis for all user's logs.
     */
    @GetMapping("/analysis/summary")
    public ResponseEntity<AnalysisResponse> getAnalysisSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(analysisService.analyzeUserLogs(user.getId()));
    }
}
