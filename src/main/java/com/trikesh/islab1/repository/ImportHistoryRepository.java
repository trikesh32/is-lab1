package com.trikesh.islab1.repository;

import com.trikesh.islab1.model.ImportHistory;
import com.trikesh.islab1.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Long> {
    Page<ImportHistory> findByUser(User user, Pageable pageable);
    Page<ImportHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<ImportHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}