package org.example.webproject.entity;

import lombok.Data;

@Data
public class WatchRecord {
    private Long id;
    private Long movieId;
    private String title;
    private String posterPath;
    private Double tmdbRating;
    private String releaseDate;
    private String overview;
    private String status; // watched, wishlist
    private String createdAt;
}
