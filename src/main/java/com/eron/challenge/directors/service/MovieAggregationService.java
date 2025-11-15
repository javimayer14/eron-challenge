package com.eron.challenge.directors.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

import com.eron.challenge.directors.client.MovieApiClient;
import com.eron.challenge.directors.model.dto.MovieDTO;
import com.eron.challenge.directors.model.dto.MoviePageResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MovieAggregationService {
    @Autowired
    private MovieApiClient movieApiClient;

    @Qualifier("movieApiExecutor")
    @Autowired
    private ExecutorService movieApiExecutor;

    private static final int PAGE_START = 1;

    public List<MovieDTO> fetchAllPages() {

        MoviePageResponseDTO firstPage = movieApiClient.getMoviesPage(PAGE_START);

        int totalPages = firstPage.getTotalPages();

        List<MovieDTO> all = new ArrayList<>(firstPage.getData());

        if (totalPages > 1) {
            all.addAll(fetchRemainingPagesInParallel(PAGE_START + 1, totalPages));
        }

        return all;
    }


    private List<MovieDTO> fetchRemainingPagesInParallel(int startPage, int endPage) {
        log.debug("Fetching pages {} to {} in parallel", startPage, endPage);
        List<CompletableFuture<MoviePageResponseDTO>> futures = createPageFetchFutures(startPage, endPage);
        waitForAll(futures);
        return extractMovies(futures);
    }


    private List<CompletableFuture<MoviePageResponseDTO>> createPageFetchFutures(int startPage, int endPage) {
        return IntStream.rangeClosed(startPage, endPage)
            .mapToObj(page ->
                CompletableFuture.supplyAsync(
                    () -> logAndFetch(page),
                    movieApiExecutor
                )
            )
            .toList();
    }

    private MoviePageResponseDTO logAndFetch(int page) {
        log.debug("Thread {} fetching page {}", Thread.currentThread().getName(), page);
        return movieApiClient.getMoviesPage(page);
    }

    private void waitForAll(List<CompletableFuture<MoviePageResponseDTO>> futures) {
        CompletableFuture<Void> all =
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            all.join();
        } catch (Exception e) {
            log.error("Error fetching pages in parallel", e);
            throw e;
        }
    }

    private List<MovieDTO> extractMovies(List<CompletableFuture<MoviePageResponseDTO>> futures) {
        return futures.stream()
            .map(CompletableFuture::join)
            .flatMap(response -> response.getData().stream())
            .toList();
    }

}
