package org.example.webproject.dto;

import lombok.Data;

@Data
public class WatchRecordRequest {
    private Long movieId;
    private String title;
    private String posterPath;
    private Double tmdbRating;
    private String releaseDate;
    private String overview;
    private String status;
}
