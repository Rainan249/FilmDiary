package org.example.webproject.controller;

import org.example.webproject.dto.WatchRecordRequest;
import org.example.webproject.entity.WatchRecord;
import org.example.webproject.service.WatchRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class WatchRecordController {

    private final WatchRecordService watchRecordService;

    public WatchRecordController(WatchRecordService watchRecordService) {
        this.watchRecordService = watchRecordService;
    }

    @GetMapping
    public List<WatchRecord> getAll() {
        return watchRecordService.getAll();
    }

    @GetMapping("/status/{status}")
    public List<WatchRecord> getByStatus(@PathVariable String status) {
        return watchRecordService.getByStatus(status);
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<?> getByMovieId(@PathVariable Long movieId) {
        WatchRecord record = watchRecordService.getByMovieId(movieId);
        if (record != null) {
            return ResponseEntity.ok(record);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public WatchRecord add(@RequestBody WatchRecordRequest request) {
        return watchRecordService.add(request);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        if (watchRecordService.updateStatus(id, status)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (watchRecordService.delete(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/stats")
    public Map<String, Integer> getStats() {
        int[] counts = watchRecordService.getCountByStatus();
        return Map.of("watched", counts[0], "wishlist", counts[1]);
    }
}
