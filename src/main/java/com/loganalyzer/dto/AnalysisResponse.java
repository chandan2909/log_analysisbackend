package com.loganalyzer.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AnalysisResponse {
    private long totalEntries;
    private long errorCount;
    private long warningCount;
    private long infoCount;
    private Map<String, Long> errorsOverTime;       // timestamp -> count
    private Map<String, Long> errorFrequency;       // error message -> frequency
    private List<RootCause> rootCauses;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class RootCause {
        private String pattern;
        private String cause;
        private String suggestion;
        private long occurrences;
    }
}
