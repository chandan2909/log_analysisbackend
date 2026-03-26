package com.loganalyzer.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LogEntryResponse {
    private Long id;
    private String level;
    private String message;
    private String timestamp;
}
