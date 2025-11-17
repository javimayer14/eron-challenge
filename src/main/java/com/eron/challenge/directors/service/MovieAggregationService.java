package com.eron.challenge.directors.service;

import java.util.ArrayList;
import java.util.List;

import com.eron.challenge.directors.client.MovieApiClient;
import com.eron.challenge.directors.model.dto.MovieDTO;
import com.eron.challenge.directors.model.dto.MoviePageResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class MovieAggregationService {
    @Autowired
    private MovieApiClient movieApiClient;
    private static final int PAGE_START = 1;
    private static final int MAX_CONCURRENCY = 10;


    @Cacheable(value = "moviesCache")
    public List<MovieDTO> getAllMoviesCached() {
        return fetchAllPages().block();
    }

    public Mono<List<MovieDTO>> fetchAllPages() {
        return movieApiClient.getMoviesPage(PAGE_START)
            .flatMap(firstPage -> {
                int totalPages = firstPage.getTotalPages();
                if (totalPages == PAGE_START) {
                    return Mono.just(firstPage.getData());
                }

                return fetchRemainingPages(PAGE_START + 1, totalPages)
                    .collectList()
                    .map(pages -> joinAllMovies(pages, firstPage.getData()));
            });
    }

    private List<MovieDTO> joinAllMovies(List<MoviePageResponseDTO> pages, List<MovieDTO> moviesFromFirstPage) {
        List<MovieDTO> allMovies = new ArrayList<>(moviesFromFirstPage);
        pages.forEach(page -> allMovies.addAll(page.getData()));
        return allMovies;
    }

    private Flux<MoviePageResponseDTO> fetchRemainingPages(int start, int end) {
        return Flux.range(start, end - start + 1)
            .flatMap(movieApiClient::getMoviesPage, MAX_CONCURRENCY);
    }

}
