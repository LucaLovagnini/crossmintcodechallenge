package com.crossmint.challenge.service;

import com.crossmint.challenge.model.Polyanet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AstralObjectServiceTest {

    @Mock
    private ExchangeFunction exchangeFunction;

    private WebClient webClient;
    private AstralObjectService service;

    private final String candidateId = "test-candidate-id";
    private final int parallelDegree = 2;
    private final int requestDelaySeconds = 0; // Set to 0 for faster tests
    private final String mapPathFormat = "/map/%s";
    private final String goalPathFormat = "/map/%s/goal";
    private int maxRetryAttempts = 3;
    private int backoffSeconds = 1;
    private double jitterFactor = 0.1;

    @BeforeEach
    void setUp() {
        this.webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();
    }

    @Test
    void testProcessAstralObject() {
        // Setup - Mock goal map response directly in the test
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(mockGoalMapClientResponse()))
                .thenReturn(Mono.just(mockSuccessClientResponse()));

        // Create service after mocking
        setupService();

        // Setup test data
        Polyanet polyanet = new Polyanet(1, 2);

        // Execute
        service.processAstralObject(polyanet, HttpMethod.POST);

        // Verify request
        ArgumentCaptor<ClientRequest> requestCaptor = ArgumentCaptor.forClass(ClientRequest.class);
        verify(exchangeFunction, times(2)).exchange(requestCaptor.capture()); // Once for goal map, once for create

        // Get the second request (the one for processAstralObject)
        ClientRequest request = requestCaptor.getAllValues().get(1);
        assertEquals(HttpMethod.POST, request.method());
        assertEquals("/polyanets", request.url().getPath());
    }

    @Test
    void testClearGoalMap() {
        // Setup - Mock all responses directly in the test
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(mockGoalMapClientResponse()))  // Initial goal map fetch
                .thenReturn(Mono.just(mockMapContentClientResponse())) // Map content fetch
                .thenReturn(Mono.just(mockSuccessClientResponse()))  // 1st DELETE
                .thenReturn(Mono.just(mockSuccessClientResponse()))  // 2nd DELETE
                .thenReturn(Mono.just(mockSuccessClientResponse()))  // 3rd DELETE
                .thenReturn(Mono.just(mockSuccessClientResponse())); // 4th DELETE

        // Create service
        setupService();

        // Execute
        service.clearGoalMap();

        // Verify correct number of requests were made:
        // 1 for goal map, 1 for map content, 4 for deleting objects
        ArgumentCaptor<ClientRequest> requestCaptor = ArgumentCaptor.forClass(ClientRequest.class);
        verify(exchangeFunction, times(6)).exchange(requestCaptor.capture());

        // The second request should be for the map content
        List<ClientRequest> requests = requestCaptor.getAllValues();
        ClientRequest mapRequest = requests.get(1);
        assertEquals(HttpMethod.GET, mapRequest.method());
        assertEquals("/map/test-candidate-id", mapRequest.url().getPath());

        // Check that the delete requests were made
        for (int i = 2; i < 6; i++) {
            ClientRequest deleteRequest = requests.get(i);
            assertEquals(HttpMethod.DELETE, deleteRequest.method());
            assertEquals("/polyanets", deleteRequest.url().getPath());
        }
    }

    @Test
    void testReplicateGoalMap() {
        // Setup - Mock all responses directly in the test
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(mockGoalMapClientResponse()))     // Initial goal map fetch
                .thenReturn(Mono.just(mockMapContentClientResponse()))  // Map content fetch for clearGoalMap
                .thenReturn(Mono.just(mockSuccessClientResponse()))     // DELETE 1
                .thenReturn(Mono.just(mockSuccessClientResponse()))     // DELETE 2
                .thenReturn(Mono.just(mockSuccessClientResponse()))     // DELETE 3
                .thenReturn(Mono.just(mockSuccessClientResponse()))     // DELETE 4
                .thenReturn(Mono.just(mockSuccessClientResponse()))     // POST 1
                .thenReturn(Mono.just(mockSuccessClientResponse()))     // POST 2
                .thenReturn(Mono.just(mockSuccessClientResponse()))     // POST 3
                .thenReturn(Mono.just(mockSuccessClientResponse()));    // POST 4

        // Create service
        setupService();

        // Execute
        service.replicateGoalMap();

        // Verify correct number of requests (10 total):
        // 1 for initial goal map fetch + 5 for clearGoalMap (1 map fetch + 4 deletes) + 4 for creating objects
        ArgumentCaptor<ClientRequest> requestCaptor = ArgumentCaptor.forClass(ClientRequest.class);
        verify(exchangeFunction, times(10)).exchange(requestCaptor.capture());

        // Verify that GET, DELETE and POST requests were made with correct URLs
        List<ClientRequest> requests = requestCaptor.getAllValues();

        // Check goal map request
        assertEquals(HttpMethod.GET, requests.get(0).method());
        assertEquals("/map/test-candidate-id/goal", requests.get(0).url().getPath());

        // Check map content request for clearGoalMap
        assertEquals(HttpMethod.GET, requests.get(1).method());
        assertEquals("/map/test-candidate-id", requests.get(1).url().getPath());

        // Check DELETE requests (indexes 2-5)
        for (int i = 2; i <= 5; i++) {
            assertEquals(HttpMethod.DELETE, requests.get(i).method());
            assertEquals("/polyanets", requests.get(i).url().getPath());
        }

        // Check POST requests (indexes 6-9)
        for (int i = 6; i <= 9; i++) {
            assertEquals(HttpMethod.POST, requests.get(i).method());
            assertEquals("/polyanets", requests.get(i).url().getPath());
        }

        // Verify goal map data
        assertNotNull(service.getGoalMap());
        assertEquals(3, service.getGoalMap().rows());
        assertEquals(3, service.getGoalMap().cols());
        assertEquals(4, service.getGoalMap().astralObjects().size());
    }

    @Test
    void testHandleWebClientError() {
        // Reduce retry parameters
        this.maxRetryAttempts = 1;  // Reduce number of retries
        this.backoffSeconds = 0;    // No delay between retries
        this.jitterFactor = 0.0;    // No jitter

        // Setup - Mock responses for all calls including retries
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(mockGoalMapClientResponse()))  // Initial goal map fetch
                .thenReturn(Mono.error(WebClientResponseException.create(  // Initial request - error
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        "Too Many Requests",
                        null, null, null)))
                .thenReturn(Mono.error(WebClientResponseException.create(  // Retry attempt - error
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        "Too Many Requests",
                        null, null, null)));

        // Create service with modified parameters
        setupService();

        // Setup test data
        Polyanet polyanet = new Polyanet(1, 2);

        // Execute - this will fail after retries are exhausted
        Exception exception = assertThrows(Exception.class, () -> service.processAstralObject(polyanet, HttpMethod.POST));

        // Verify the root cause is a WebClientResponseException
        assertInstanceOf(RuntimeException.class, exception);
        assertInstanceOf(WebClientResponseException.class, exception.getCause());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(),
                ((WebClientResponseException)exception.getCause()).getStatusCode().value());

        // Verify that 3 requests were made (1 initial goal map + 1 initial attempt + 1 retry)
        verify(exchangeFunction, times(3)).exchange(any(ClientRequest.class));
    }

    // Add these test methods to your AstralObjectServiceTest class

    @Test
    void testClearGoalMapWithNullResponse() {
        // Mock a null response
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(mockGoalMapClientResponse()))  // Initial goal map fetch
                .thenReturn(Mono.just(mockNullMapContentClientResponse()));

        // Create service
        setupService();

        // Execute - this should handle the exception internally
        service.clearGoalMap();

        // Verify that requests were made but caught the exception
        verify(exchangeFunction, times(2)).exchange(any(ClientRequest.class));
    }

    @Test
    void testHandleServerError() {
        // Reduce retry parameters
        this.maxRetryAttempts = 1;  // Reduce number of retries
        this.backoffSeconds = 0;    // No delay between retries
        this.jitterFactor = 0.0;    // No jitter

        // Setup - Mock goal map response and server errors
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(mockGoalMapClientResponse()))  // Initial goal map fetch
                .thenReturn(Mono.error(WebClientResponseException.create(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        null, null, null)))
                .thenReturn(Mono.error(WebClientResponseException.create(  // Retry attempt
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        null, null, null)));

        // Create service with modified parameters
        setupService();

        // Setup test data
        Polyanet polyanet = new Polyanet(1, 2);

        // Execute - this will fail after retries are exhausted
        Exception exception = assertThrows(Exception.class, () -> service.processAstralObject(polyanet, HttpMethod.POST));

        // Verify the root cause is a WebClientResponseException
        assertInstanceOf(RuntimeException.class, exception);
        assertInstanceOf(WebClientResponseException.class, exception.getCause());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ((WebClientResponseException)exception.getCause()).getStatusCode().value());

        // Verify that 3 requests were made (1 goal map + 1 initial attempt + 1 retry)
        verify(exchangeFunction, times(3)).exchange(any(ClientRequest.class));
    }

    @Test
    void testHandleClientError() {
        // Setup - Mock goal map response and a 400 error for the second call
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(mockGoalMapClientResponse()))  // Initial goal map fetch
                .thenReturn(Mono.error(WebClientResponseException.create(
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        null, null, null)));

        // Create service
        setupService();

        // Setup test data
        Polyanet polyanet = new Polyanet(1, 2);

        // Execute - this should fail without retry for client errors
        try {
            service.processAstralObject(polyanet, HttpMethod.POST);
        } catch (Exception e) {
            // Expected - no retries for client errors
        }

        // Verify that only 2 requests were made (no retries for client errors)
        verify(exchangeFunction, times(2)).exchange(any(ClientRequest.class));
    }

    @Test
    void testHandleUnexpectedError() {
        // Setup - Mock goal map response and an unexpected error status for the second call
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(mockGoalMapClientResponse()))  // Initial goal map fetch
                .thenReturn(Mono.error(WebClientResponseException.create(
                        299,  // An unusual status code that doesn't fall into normal categories
                        "Unusual Status",
                        null, null, null)));

        // Create service
        setupService();

        // Setup test data
        Polyanet polyanet = new Polyanet(1, 2);

        // Execute
        try {
            service.processAstralObject(polyanet, HttpMethod.POST);
        } catch (Exception e) {
            // Expected
        }

        // Verify that only 2 requests were made (no retries for unexpected errors)
        verify(exchangeFunction, times(2)).exchange(any(ClientRequest.class));
    }

    @Test
    void testHandleNonWebClientException() {
        // Setup - Mock goal map response and a non-WebClientResponseException for the second call
        RuntimeException nonWebClientException = new RuntimeException("Test exception");

        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(mockGoalMapClientResponse()))  // Initial goal map fetch
                .thenReturn(Mono.error(nonWebClientException));  // Non-WebClientResponseException

        // Create service
        setupService();

        // Setup test data
        Polyanet polyanet = new Polyanet(1, 2);

        // Execute - this should fail without retry for non-WebClientResponseException
        try {
            service.processAstralObject(polyanet, HttpMethod.POST);
        } catch (Exception e) {
            // Expected - no retries for non-WebClientResponseException
        }

        // Verify that only 2 requests were made (no retries for non-WebClientResponseException)
        verify(exchangeFunction, times(2)).exchange(any(ClientRequest.class));
    }

    private void setupService() {
        this.service = new AstralObjectService(
                webClient,
                candidateId,
                parallelDegree,
                maxRetryAttempts,
                backoffSeconds,
                jitterFactor,
                requestDelaySeconds,
                mapPathFormat,
                goalPathFormat
        );
    }


    private ClientResponse mockGoalMapClientResponse() {
        // Create a response with a simple 3x3 grid
        String responseJson = """
                {
                  "goal": [
                    ["SPACE", "POLYANET", "SPACE"],
                    ["POLYANET", "SPACE", "POLYANET"],
                    ["SPACE", "POLYANET", "SPACE"]
                  ]
                }
                """;

        return ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(responseJson)
                .build();
    }

    private ClientResponse mockMapContentClientResponse() {
        // Create a response with a simple map content
        String responseJson = """
                {
                  "map": {
                    "content": [
                      [null, {"type":"POLYANET"}, null],
                      [{"type":"POLYANET"}, null, {"type":"POLYANET"}],
                      [null, {"type":"POLYANET"}, null]
                    ]
                  }
                }
                """;

        return ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(responseJson)
                .build();
    }

    private ClientResponse mockSuccessClientResponse() {
        return ClientResponse.create(HttpStatus.OK)
                .build();
    }


    // Add this helper method to create a response with null map content
    private ClientResponse mockNullMapContentClientResponse() {
        String responseJson = """
            {
              "map": null
            }
            """;

        return ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(responseJson)
                .build();
    }
}