package com.eron.challenge.directors.model.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DirectorsResponseDTO {
    private final List<String> directors;

}
