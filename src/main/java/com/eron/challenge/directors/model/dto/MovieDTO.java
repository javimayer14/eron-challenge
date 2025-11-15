package com.eron.challenge.directors.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MovieDTO {
    @JsonProperty("Title")
    private final String title;
    @JsonProperty("Year")
    private final Integer year;
    @JsonProperty("Director")
    private final String director;
}
