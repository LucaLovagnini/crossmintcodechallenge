package com.crossmint.challenge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .followRedirect(true); // Enable following redirects

        return WebClient.builder()
                .baseUrl("https://challenge.crossmint.io/api")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}