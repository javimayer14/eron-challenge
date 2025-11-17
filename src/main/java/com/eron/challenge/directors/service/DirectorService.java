package com.eron.challenge.directors.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.eron.challenge.directors.client.MovieApiClient;
import com.eron.challenge.directors.model.dto.DirectorsResponseDTO;
import com.eron.challenge.directors.model.dto.MovieDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j

@Service
public class DirectorService {

    @Autowired
    private MovieAggregationService movieAggregationService;

    @Autowired
    private MovieApiClient movieApiClient;

    @Autowired
    private MovieApiClient movieApiExecutor;

    public DirectorsResponseDTO getDirectorsWithMoreThan(int threshold) {
        List<MovieDTO> allMovies = movieAggregationService.getAllMoviesCached();
        log.info("Successfully fetched movies");
        List<String> directors = processDirectors(allMovies, threshold);
        return buildDirectorsResponse(directors);
    }

    private List<String> processDirectors(List<MovieDTO> movies, int threshold) {
        log.debug("Processing {} movies to find directors with > {}", movies.size(), threshold);

        Map<String, Long> directorCount = countMoviesByDirector(movies);
        List<String> directors = filterDirectorsAboveThreshold(directorCount, threshold);

        log.info("Found {} directors with more than {} movies: {}", directors.size(), threshold, directors);

        return directors;
    }

    private Map<String, Long> countMoviesByDirector(List<MovieDTO> movies) {
        Map<String, Long> counts = movies.stream()
            .map(MovieDTO::getDirector)
            .filter(this::isValidDirector)
            .collect(Collectors.groupingBy(
                director -> director,
                Collectors.counting()
            ));

        log.debug("Found {} unique directors in dataset", counts.size());
        return counts;
    }

    private boolean isValidDirector(String director) {
        return director != null && !director.isBlank();
    }

    private List<String> filterDirectorsAboveThreshold(Map<String, Long> directorCount, int threshold) {
        return directorCount.entrySet().stream()
            .filter(entry -> passesThreshold(entry, threshold))
            .map(Map.Entry::getKey)
            .sorted()
            .toList();
    }

    private boolean passesThreshold(Map.Entry<String, Long> entry, int threshold) {
        boolean passes = entry.getValue() > threshold;
        if (passes) {
            log.debug("Director '{}' has {} movies (passes threshold)", entry.getKey(), entry.getValue());
        }
        return passes;
    }

    private DirectorsResponseDTO buildDirectorsResponse(List<String> directors) {
        return DirectorsResponseDTO.builder()
            .directors(directors)
            .build();
    }

}
