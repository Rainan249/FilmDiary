package org.example.webproject.dto;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long movieId;
    private String title;
    private String posterPath;
    private Double tmdbRating;
    private String releaseDate;
    private Integer userRating;
    private String content;
}
