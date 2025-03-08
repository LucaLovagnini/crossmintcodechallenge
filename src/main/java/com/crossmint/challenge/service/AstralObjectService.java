package com.crossmint.challenge.service;

import com.crossmint.challenge.config.GoalMap;
import com.crossmint.challenge.model.ApiSerializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Service
public class AstralObjectService {
    private static final Logger logger = LoggerFactory.getLogger(AstralObjectService.class);

    private final WebClient webClient;
    private final String candidateId;

    public AstralObjectService(WebClient webClient, @Value("${crossmint.candidate-id}") String candidateId) {
        this.webClient = webClient;
        this.candidateId = candidateId;
    }

    public void createObject(ApiSerializable object, HttpMethod method) {
        webClient.method(method)
                .uri(object.getCreationPath())
                .bodyValue(object.toRequestBody(candidateId))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnRequest(r -> logger.info("Creating object: {}", object))
                .doOnSuccess(v -> logger.info("Successfully performed {} on object: {}", method, object))
                .onErrorResume(WebClientResponseException.class, this::handleWebClientError)
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(2))
                        .filter(this::isRetryableError)
                        .doBeforeRetry(signal ->
                                logger.warn("Retrying after error: {}", signal.failure().getMessage())))
                .block();
    }

    public GoalMap getGoalMap() {
        // Inline record definition
        record GoalResponse(List<List<String>> goal) {}

        return webClient.get()
                .uri(String.format("/map/%s/goal", candidateId))
                .retrieve()
                .bodyToMono(GoalResponse.class)
                .doOnNext(response -> logger.info("Fetched goal with {} rows and {} cols.",
                        response.goal().size(), response.goal().getFirst().size()))
                .doOnError(WebClientResponseException.class, this::handleWebClientError)
                .map(response -> {
                    if (response.goal().isEmpty() || response.goal().getFirst().isEmpty()) {
                        throw new IllegalStateException("Invalid goal response: Empty grid received");
                    }
                    return new GoalMap(response.goal().size(), response.goal().getFirst().size());
                })
                .block(); // Blocking call (use reactive if preferred)
    }


    private Mono<Void> handleWebClientError(WebClientResponseException ex) {
        if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            logger.warn("Too Many Requests (retryable): Status: {}, Body: {}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
            return Mono.error(ex); // Will trigger retry
        } else if (ex.getStatusCode().is5xxServerError()) {
            logger.warn("Server error (retryable): Status: {}, Body: {}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
            return Mono.error(ex); // Will trigger retry
        } else if (ex.getStatusCode().is4xxClientError()) {
            logger.error("Client error (won't retry): Status: {}, Body: {}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
            return Mono.error(ex); // No retry
        }

        logger.error("Unexpected error: Status: {}, Body: {}",
                ex.getStatusCode(), ex.getResponseBodyAsString());
        return Mono.error(ex);
    }

    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            return ex.getStatusCode().is5xxServerError() || ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
        }
        return false;
    }
}
