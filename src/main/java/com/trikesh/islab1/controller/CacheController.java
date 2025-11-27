package com.trikesh.islab1.controller;

import com.trikesh.islab1.aspect.CacheStatisticsAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cache")
@CrossOrigin(origins = "*")
public class CacheController {

    @Autowired
    private CacheStatisticsAspect cacheStatisticsAspect;

    @PostMapping("/statistics/enable")
    public ResponseEntity<?> enableStatistics() {
        cacheStatisticsAspect.enableLogging();
        return ResponseEntity.ok(Map.of("message", "Cache statistics logging enabled"));
    }

    @PostMapping("/statistics/disable")
    public ResponseEntity<?> disableStatistics() {
        cacheStatisticsAspect.disableLogging();
        return ResponseEntity.ok(Map.of("message", "Cache statistics logging disabled"));
    }

    @GetMapping("/statistics/status")
    public ResponseEntity<?> getStatisticsStatus() {
        return ResponseEntity.ok(Map.of("enabled", cacheStatisticsAspect.isLoggingEnabled()));
    }
}