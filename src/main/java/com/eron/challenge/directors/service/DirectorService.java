package com.eron.challenge.directors.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.eron.challenge.directors.client.MovieApiClient;
import com.eron.challenge.directors.model.dto.DirectorsResponseDTO;
import com.eron.challenge.directors.model.dto.MovieDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j

@Service
public class DirectorService {

    @Autowired
    private MovieAggregationService movieAggregationService;

    @Autowired
    private MovieApiClient movieApiClient;

    @Autowired
    @Qualifier("movieApiExecutor")
    private ExecutorService movieApiExecutor;

    private static final int PAGE_START = 1;

    public DirectorsResponseDTO getDirectorsWithMoreThan(int threshold) {

        long startTime = System.currentTimeMillis();
        
        List<MovieDTO> allMovies = movieAggregationService.fetchAllPages();
        long fetchTime = System.currentTimeMillis() - startTime;

        log.info("Successfully fetched {} movies from {} pages in {} ms", fetchTime);
        List<String> directors = processDirectors(allMovies, threshold);
        return buildDirectorsResponse(directors);

    }

    private DirectorsResponseDTO buildDirectorsResponse(List<String> directors) {
        return DirectorsResponseDTO.builder()
            .directors(directors)
            .build();
    }

    private List<String> processDirectors(List<MovieDTO> movies, int threshold) {
        log.debug("Processing {} movies to find directors with > {}", movies.size(), threshold);

        Map<String, Long> directorCount = countMoviesByDirector(movies);
        List<String> directors = filterDirectorsAboveThreshold(directorCount, threshold);

        log.info("Found {} directors with more than {} movies: {}",
            directors.size(), threshold, directors);

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

}
