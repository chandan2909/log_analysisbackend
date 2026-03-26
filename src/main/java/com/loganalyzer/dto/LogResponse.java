package com.loganalyzer.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LogResponse {
    private Long id;
    private String fileName;
    private LocalDateTime uploadedAt;
    private long totalEntries;
    private long errorCount;
    private long warningCount;
    private long infoCount;
}
