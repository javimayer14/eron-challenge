package com.eron.challenge.directors.config;

import com.eron.challenge.directors.service.MovieAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupWarmupConfig {

    @Autowired
    private MovieAggregationService movieAggregationService;

    @EventListener(ApplicationReadyEvent.class)
    public void warmupCacheConnectionPool() {
        log.info("warm-up: Inicializando pool de conexiones para movies api");
        long start = System.currentTimeMillis();
        movieAggregationService.getAllMoviesCached();
        log.info("warm-up completado en {} ms", System.currentTimeMillis() - start);
    }
}
