package com.eron.challenge.directors.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MoviePageResponseDTO {
    @JsonProperty("page")
    private final int page;
    @JsonProperty("per_page")
    private final int perPage;
    @JsonProperty("total")
    private final int total;
    @JsonProperty("total_pages")
    private final int totalPages;
    @JsonProperty("data")
    private final List<MovieDTO> data;

}
