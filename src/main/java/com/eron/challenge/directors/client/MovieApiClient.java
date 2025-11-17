package com.eron.challenge.directors.client;

import com.eron.challenge.directors.model.dto.MoviePageResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovieApiClient {

    private final WebClient moviesApiWebClient;

    public Mono<MoviePageResponseDTO> getMoviesPage(int page) {
        return moviesApiWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/search")
                .queryParam("page", page)
                .build())
            .retrieve()
            .bodyToMono(MoviePageResponseDTO.class)
            .retry(2);
    }
}