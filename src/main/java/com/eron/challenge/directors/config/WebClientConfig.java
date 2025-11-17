package com.eron.challenge.directors.config;

import java.time.Duration;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${movies.api.base-url}")
    private String baseUrl;

    @Bean
    public WebClient moviesApiWebClient() {

        ConnectionProvider provider = ConnectionProvider.builder("movies-pool")
            .maxConnections(50)
            .pendingAcquireMaxCount(100)
            .pendingAcquireTimeout(Duration.ofSeconds(5))
            .maxIdleTime(Duration.ofSeconds(30))
            .evictInBackground(Duration.ofSeconds(30))
            .build();

        HttpClient httpClient = HttpClient.create(provider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
            .responseTimeout(Duration.ofSeconds(5))
            .resolver(DefaultAddressResolverGroup.INSTANCE)
            .followRedirect(true)
            .keepAlive(true)
            .compress(true)
            .resolver(DefaultAddressResolverGroup.INSTANCE)
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(5))
                    .addHandlerLast(new WriteTimeoutHandler(5))
            );

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(baseUrl)
            .build();
    }
}