package com.loganalyzer.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * LogEntry – individual parsed line from a log file.
 * Indexed on level and timestamp for fast querying.
 */
@Entity
@Table(name = "la_log_entries", indexes = {
    @Index(name = "idx_la_log_entry_level", columnList = "level"),
    @Index(name = "idx_la_log_entry_timestamp", columnList = "timestamp")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String level;  // ERROR, WARNING, INFO

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(length = 50)
    private String timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false)
    private Log log;
}
