package org.example.webproject.entity;

import lombok.Data;

@Data
public class Review {
    private Long id;
    private Long movieId;
    private String title;
    private String posterPath;
    private Double tmdbRating;
    private String releaseDate;
    private Integer userRating;
    private String content;
    private String createdAt;
    private String updatedAt;
}
