package com.eron.challenge.directors.client;

import com.eron.challenge.directors.model.dto.MoviePageResponseDTO;
import com.eron.challenge.directors.model.dto.MovieResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MovieApiClient {

    private final WebClient moviesApiWebClient;

    /**
     * Fetches a page from the movies API.
     * Only HTTP communication happens here.
     */
    public MoviePageResponseDTO getMoviesPage(int page) {
        return moviesApiWebClient.get()
            .uri(uriBuilder -> uriBuilder.path("/search")
                .queryParam("page", page)
                .queryParam("per_page", 15)
                .build()
            )
            .retrieve()
            .bodyToMono(MoviePageResponseDTO.class)
            .block();
    }
}