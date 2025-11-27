package com.trikesh.islab1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "import_history")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ImportHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "objects_count")
    private Integer objectsCount;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = ImportStatus.IN_PROGRESS;
        }
    }

    public ImportHistory() {}

    public ImportHistory(User user, String fileName) {
        this.user = user;
        this.fileName = fileName;
        this.status = ImportStatus.IN_PROGRESS;
    }
}