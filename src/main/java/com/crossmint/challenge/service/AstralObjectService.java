package com.crossmint.challenge.service;

import com.crossmint.challenge.model.AstralObjectParser;
import com.crossmint.challenge.model.GoalMap;
import com.crossmint.challenge.model.ApiSerializable;
import com.crossmint.challenge.model.Polyanet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Service responsible for managing astral objects in the Crossmint challenge.
 * This service provides functionality to fetch, create, delete, and replicate
 * astral objects according to a goal map.
 */
@Service
public class AstralObjectService {
    private static final Logger logger = LoggerFactory.getLogger(AstralObjectService.class);

    private final WebClient webClient;

    /**
     * The goal map representing the desired state of astral objects.
     */
    @Getter
    private final GoalMap goalMap;

    /**
     * The candidate identifier used for all API requests.
     */
    @NotBlank(message = "Candidate id is required")
    private final String candidateId;

    /**
     * The maximum number of parallel operations allowed.
     */
    @Positive
    private final int parallelDegree;

    /**
     * The maximum number of retry attempts for failed requests.
     */
    private final int maxRetryAttempts;

    /**
     * The base duration in seconds for the exponential backoff strategy.
     */
    private final int backoffSeconds;

    /**
     * The jitter factor (0-1) applied to retry backoff durations to prevent thundering herd problems.
     */
    private final double jitterFactor;

    /**
     * The delay in seconds between each API request to prevent rate limiting.
     */
    private final int requestDelaySeconds;

    /**
     * The format string for the map API path.
     */
    private final String mapPathFormat;

    /**
     * The format string for the goal API path.
     */
    private final String goalPathFormat;

    /**
     * Constructs a new AstralObjectService with the specified configuration.
     *
     * @param webClient The WebClient for making HTTP requests
     * @param candidateId The candidate ID for API authentication
     * @param parallelDegree The maximum number of parallel operations
     * @param maxRetryAttempts The maximum number of retry attempts for failed requests
     * @param backoffSeconds The base duration in seconds for exponential backoff
     * @param jitterFactor The jitter factor applied to retry delays
     * @param requestDelaySeconds The delay between API requests
     * @param mapPathFormat The format string for the map API path
     * @param goalPathFormat The format string for the goal API path
     */
    public AstralObjectService(WebClient webClient,
                               @Value("${crossmint.candidate-id}") String candidateId,
                               @Value("${crossmint.parallel-degree:3}") int parallelDegree,
                               @Value("${crossmint.retry.max-attempts:5}") int maxRetryAttempts,
                               @Value("${crossmint.retry.backoff-seconds:10}") int backoffSeconds,
                               @Value("${crossmint.retry.jitter-factor:0.5}") double jitterFactor,
                               @Value("${crossmint.request.delay-seconds:5}") int requestDelaySeconds,
                               @Value("${crossmint.api.map-path:/map/%s}") String mapPathFormat,
                               @Value("${crossmint.api.goal-path:/map/%s/goal}") String goalPathFormat) {
        this.webClient = webClient;
        this.candidateId = candidateId;
        this.parallelDegree = parallelDegree;
        this.maxRetryAttempts = maxRetryAttempts;
        this.backoffSeconds = backoffSeconds;
        this.jitterFactor = jitterFactor;
        this.requestDelaySeconds = requestDelaySeconds;
        this.mapPathFormat = mapPathFormat;
        this.goalPathFormat = goalPathFormat;
        this.goalMap = fetchGoalMap();
    }

    /**
     * Processes a single astral object by performing the specified HTTP method on it.
     * This method includes built-in retry logic for handling rate limiting and server errors.
     *
     * @param astralObject The astral object to process
     * @param method The HTTP method to use (POST for creation, DELETE for removal)
     */
    public void processAstralObject(ApiSerializable astralObject, HttpMethod method) {
        webClient.method(method)
                .uri(astralObject.getCreationPath())
                .bodyValue(astralObject.toRequestBody(candidateId))
                .retrieve()
                .bodyToMono(Void.class)
                .delayElement(Duration.ofSeconds(requestDelaySeconds))
                .doOnRequest(r ->
                        logger.info("Processing astralObject: {}", astralObject))
                .doOnSuccess(v ->
                        logger.info("Successfully performed {} on astralObject: {}", method, astralObject))
                .onErrorResume(WebClientResponseException.class, this::handleWebClientError)
                .retryWhen(Retry.backoff(maxRetryAttempts, Duration.ofSeconds(backoffSeconds))
                        .jitter(jitterFactor)
                        .filter(this::isRetryableError)
                        .doBeforeRetry(signal ->
                                logger.warn("Retrying {}/{} after error: {}",
                                        signal.totalRetries() + 1,
                                        maxRetryAttempts,
                                        signal.failure().getMessage())))
                .block();
    }

    /**
     * Clears all astral objects from the current map.
     * This is used before replicating a new goal map.
     */
    public void clearGoalMap() {
        logger.info("Fetching map for candidate: {}", candidateId);

        // Inline record to represent the map response structure
        record MapContent(List<List<Object>> content) {}
        record MapResponse(MapContent map) {}

        try {
            MapResponse response = webClient.get()
                    .uri(String.format(mapPathFormat, candidateId))
                    .retrieve()
                    .bodyToMono(MapResponse.class)
                    .block();

            if (response == null || response.map() == null || response.map().content() == null) {
                throw new IllegalStateException("Invalid response received from API");
            }

            List<List<Object>> content = response.map().content();
            logger.info("Processing map with {} rows and {} cols", content.size(), content.getFirst().size());

            // Identify non-null objects and mark them to delete
            Set<ApiSerializable> objectsToDelete = IntStream.range(0, content.size())
                    .boxed()
                    .flatMap(row -> IntStream.range(0, content.get(row).size())
                            .mapToObj(col -> {
                                // Delete on Polyanet works on any astral object
                                return (content.get(row).get(col) != null) ? new Polyanet(row, col) : null;
                            }))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            logger.info("Found {} objects to delete.", objectsToDelete.size());

            Flux.fromIterable(objectsToDelete)
                    .flatMap(obj -> Mono.fromRunnable(() ->
                            processAstralObject(obj, HttpMethod.DELETE)
                    ).subscribeOn(Schedulers.boundedElastic()), parallelDegree)
                    .blockLast();

        } catch (Exception e) {
            logger.error("Failed to fetch and delete objects", e);
        }
    }

    /**
     * Replicates the goal map by creating all the required astral objects.
     * This method first clears the existing map and then creates new objects
     * according to the goal map.
     */
    public void replicateGoalMap() {
        clearGoalMap();
        Flux.fromIterable(goalMap.astralObjects())
                .flatMap(astralObject -> Mono.fromRunnable(() ->
                        processAstralObject(astralObject, HttpMethod.POST)
                ).subscribeOn(Schedulers.boundedElastic()), parallelDegree)
                .blockLast();
    }

    /**
     * Fetches the goal map from the API.
     * This method is called during service initialization to load the target
     * configuration of astral objects.
     *
     * @return The parsed goal map object
     */
    private GoalMap fetchGoalMap() {
        record GoalResponse(List<List<String>> goal) {}

        return webClient.get()
                .uri(String.format(goalPathFormat, candidateId))
                .retrieve()
                .bodyToMono(GoalResponse.class)
                .doOnNext(response -> logger.info("Fetched goal with {} rows and {} cols.",
                        response.goal().size(), response.goal().getFirst().size()))
                .doOnError(WebClientResponseException.class, this::handleWebClientError)
                .map(response -> new GoalMap(response.goal().size(), response.goal().getFirst().size(),
                        AstralObjectParser.parseAstralObjects(response.goal)))
                .block();
    }

    /**
     * Handles WebClient errors based on their HTTP status code.
     * This method determines whether an error should be retried or propagated.
     *
     * @param ex The WebClientResponseException to handle
     * @return A Mono that either completes or errors based on the error handling strategy
     */
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

    /**
     * Determines if an error should be retried based on its type and status code.
     * Retries are performed on too many requests and 5xx errors only.
     *
     * @param throwable The error to check
     * @return true if the error is retryable, false otherwise
     */
    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            return ex.getStatusCode().is5xxServerError() || ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
        }
        return false;
    }
}