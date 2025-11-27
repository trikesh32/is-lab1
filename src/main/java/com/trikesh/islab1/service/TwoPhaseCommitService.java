package com.trikesh.islab1.service;

import com.trikesh.islab1.model.ImportHistory;
import com.trikesh.islab1.model.ImportStatus;
import com.trikesh.islab1.model.Person;
import com.trikesh.islab1.model.User;
import com.trikesh.islab1.repository.ImportHistoryRepository;
import com.trikesh.islab1.repository.PersonRepository;
import com.trikesh.islab1.controller.PersonWebSocketController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TwoPhaseCommitService {

    @Autowired
    private MinioService minioService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ImportHistoryRepository importHistoryRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private PersonWebSocketController webSocketController;

    public ImportHistory executeImport(MultipartFile file, User user, List<Person> persons) {
        String minioFileName = null;
        TransactionStatus dbTransaction = null;
        ImportHistory history = null;

        try {
            log.info("Starting two-phase commit for import");

            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
            dbTransaction = transactionManager.getTransaction(def);

            history = new ImportHistory(user, file.getOriginalFilename());
            history = importHistoryRepository.save(history);
            log.info("Phase 1: Created import history record with ID: {}", history.getId());

            minioFileName = minioService.uploadFile(file);
            log.info("Phase 1: Uploaded file to MinIO: {}", minioFileName);

            history.setFilePath(minioFileName);
            history = importHistoryRepository.save(history);

            List<Person> savedPersons = personRepository.saveAll(persons);
            log.info("Phase 1: Saved {} persons to database", savedPersons.size());

            history.setStatus(ImportStatus.SUCCESS);
            history.setObjectsCount(savedPersons.size());
            history.setCompletedAt(LocalDateTime.now());
            history = importHistoryRepository.save(history);

            transactionManager.commit(dbTransaction);
            log.info("Phase 2: Committed database transaction");

            for (Person person : savedPersons) {
                webSocketController.notifyPersonCreated(person);
            }

            log.info("Two-phase commit completed successfully");
            return history;

        } catch (Exception e) {
            log.error("Error during two-phase commit: {}", e.getMessage(), e);

            if (dbTransaction != null && !dbTransaction.isCompleted()) {
                try {
                    transactionManager.rollback(dbTransaction);
                    log.info("Rolled back database transaction");
                } catch (Exception rollbackEx) {
                    log.error("Error rolling back database transaction: {}", rollbackEx.getMessage());
                }
            }

            if (minioFileName != null) {
                try {
                    minioService.deleteFile(minioFileName);
                    log.info("Rolled back MinIO file upload: {}", minioFileName);
                } catch (Exception minioEx) {
                    log.error("Error rolling back MinIO file: {}", minioEx.getMessage());
                }
            }

            if (history != null && history.getId() != null) {
                try {
                    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                    TransactionStatus cleanupTransaction = transactionManager.getTransaction(def);
                    
                    ImportHistory failedHistory = importHistoryRepository.findById(history.getId()).orElse(null);
                    if (failedHistory != null) {
                        failedHistory.setStatus(ImportStatus.FAILED);
                        failedHistory.setErrorMessage(e.getMessage());
                        failedHistory.setCompletedAt(LocalDateTime.now());
                        importHistoryRepository.save(failedHistory);
                    }
                    
                    transactionManager.commit(cleanupTransaction);
                    log.info("Updated import history with failure status");
                } catch (Exception updateEx) {
                    log.error("Error updating import history: {}", updateEx.getMessage());
                }
            }

            throw new RuntimeException("Import failed: " + e.getMessage(), e);
        }
    }
}