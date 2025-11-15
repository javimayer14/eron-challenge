package com.eron.challenge.directors.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutorConfig {

    @Value("${movie.api.thread-pool-size:10}")
    private int threadPoolSize;

    @Bean(name = "movieApiExecutor", destroyMethod = "shutdown")
    public ExecutorService movieApiExecutor() {

        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "movie-api-worker-" + threadNumber.getAndIncrement());
                thread.setDaemon(false); // Non-daemon for graceful shutdown
                return thread;
            }
        };

        return Executors.newFixedThreadPool(threadPoolSize, threadFactory);
    }
}