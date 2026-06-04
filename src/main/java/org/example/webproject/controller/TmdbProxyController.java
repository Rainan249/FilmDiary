package org.example.webproject.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/tmdb")
public class TmdbProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tmdb.api.key:114cd13c798fdb6e1eb3f1a3d7759cc4}")
    private String apiKey;

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "zh-CN") String language,
            @RequestParam(defaultValue = "1") int page) {
        try {
            String url = String.format(
                "https://api.themoviedb.org/3/search/movie?api_key=%s&query=%s&language=%s&page=%d&region=CN&include_adult=false",
                apiKey, java.net.URLEncoder.encode(query, "UTF-8"), language, page
            );
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
