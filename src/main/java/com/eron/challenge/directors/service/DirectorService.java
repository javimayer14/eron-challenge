package com.eron.challenge.directors.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.eron.challenge.directors.client.MovieApiClient;
import com.eron.challenge.directors.model.dto.DirectorsResponseDTO;
import com.eron.challenge.directors.model.dto.MovieDTO;
import com.eron.challenge.directors.model.dto.MoviePageResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j

@Service
public class DirectorService {

    @Autowired
    private MovieApiClient movieApiClient;

    @Autowired
    @Qualifier("movieApiExecutor")
    private ExecutorService movieApiExecutor;
    
    private static final int PAGE_START = 1;

    public DirectorsResponseDTO getDirectorsWithMoreThan(int threshold) {


        long startTime = System.currentTimeMillis();
        Map<String, Integer> directorCount = new HashMap<>();

        MoviePageResponseDTO firstPage = movieApiClient.getMoviesPage(1);
        int totalPages = firstPage.getTotalPages();
        log.info("API returned {} total pages with {} movies per page", totalPages, firstPage.getPerPage());

        List<MovieDTO> allMovies = new ArrayList<>(firstPage.getData());

        if (totalPages > PAGE_START) {
            log.info("Fetching remaining {} pages in parallel", totalPages - 1);
            List<MovieDTO> remainingMovies = fetchRemainingPagesInParallel(PAGE_START + 1, totalPages);
            allMovies.addAll(remainingMovies);
        }

        long fetchTime = System.currentTimeMillis() - startTime;

        log.info("Successfully fetched {} movies from {} pages in {}ms",
            allMovies.size(), totalPages, fetchTime);

        return DirectorsResponseDTO.builder()
            .directors(processDirectors(allMovies, threshold))
            .build();


    }

    private List<String> filterAndSortDirectors(Map<String, Integer> directorCount, int threshold) {
        return directorCount.entrySet().stream()
            .filter(e -> e.getValue() > threshold)
            .map(Map.Entry::getKey)
            .sorted()
            .toList();
    }

    private List<MovieDTO> fetchRemainingPagesInParallel(int startPage, int endPage) {
        log.debug("Initiating parallel fetch for pages {} to {}", startPage, endPage);

        List<CompletableFuture<MoviePageResponseDTO>> futures = IntStream.rangeClosed(startPage, endPage)
            .mapToObj(page -> CompletableFuture.supplyAsync(
                () -> {
                    log.debug("Thread {} fetching page {}",
                        Thread.currentThread().getName(), page);
                    return movieApiClient.getMoviesPage(page);
                },
                movieApiExecutor))
            .toList();


        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0]));

        allFutures.join();

        List<MovieDTO> movies = futures.stream()
            .map(CompletableFuture::join) // Get the result (already completed)
            .flatMap(response -> response.getData().stream())
            .toList();

        log.debug("Parallel fetch completed, collected {} movies", movies.size());

        return movies;
    }


    private List<String> processDirectors(List<MovieDTO> movies, int threshold) {
        log.debug("Processing {} movies to find directors with > {} movies",
            movies.size(), threshold);

        // Group movies by director and count
        Map<String, Long> directorMovieCount = movies.stream()
            .filter(movie -> movie.getDirector() != null && !movie.getDirector().isBlank())
            .collect(Collectors.groupingBy(
                MovieDTO::getDirector,
                Collectors.counting()));

        log.debug("Found {} unique directors in dataset", directorMovieCount.size());

        List<String> result = directorMovieCount.entrySet().stream()
            .filter(entry -> {
                boolean passes = entry.getValue() > threshold;
                if (passes) {
                    log.debug("Director '{}' has {} movies (passes threshold)",
                        entry.getKey(), entry.getValue());
                }
                return passes;
            })
            .map(Map.Entry::getKey)
            .sorted()
            .toList();

        log.info("Found {} directors with more than {} movies: {}",
            result.size(), threshold, result);

        return result;
    }
}
