package com.eron.challenge.directors.model.dto;

import lombok.Data;

@Data
public class MovieResponseDTO {

    private final Long id;
    private final String title;
    private final Integer year;
    private final String director;
    private final Double rating;
}
