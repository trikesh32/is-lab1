package com.trikesh.islab1.controller;

import com.trikesh.islab1.model.ImportHistory;
import com.trikesh.islab1.model.User;
import com.trikesh.islab1.model.UserRole;
import com.trikesh.islab1.repository.ImportHistoryRepository;
import com.trikesh.islab1.repository.UserRepository;
import com.trikesh.islab1.service.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
@CrossOrigin(origins = "*")
public class ImportController {

    @Autowired
    private ImportService importService;

    @Autowired
    private ImportHistoryRepository importHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            if (!file.getOriginalFilename().endsWith(".csv")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only CSV files are allowed"));
            }

            // Получаем текущего пользователя из JWT токена
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ImportHistory history = importService.importPersonsFromCsv(file, user);

            Map<String, Object> response = new HashMap<>();
            response.put("id", history.getId());
            response.put("status", history.getStatus());
            response.put("objectsCount", history.getObjectsCount());
            response.put("message", "Import completed successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<Page<ImportHistory>> getImportHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        // Получаем текущего пользователя из JWT токена
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Если пользователь - админ, показываем всё
        if (user.getRole() == UserRole.ADMIN) {
            Page<ImportHistory> history = importHistoryRepository.findAllByOrderByCreatedAtDesc(pageable);
            return ResponseEntity.ok(history);
        } else {
            // Обычный пользователь видит только свои операции
            Page<ImportHistory> history = importHistoryRepository.findByUserOrderByCreatedAtDesc(user, pageable);
            return ResponseEntity.ok(history);
        }
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<ImportHistory> getImportHistoryById(@PathVariable Long id) {
        return importHistoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}