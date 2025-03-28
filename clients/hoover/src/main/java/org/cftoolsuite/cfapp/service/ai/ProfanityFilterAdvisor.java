package org.cftoolsuite.cfapp.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An advisor that checks the user prompt for profanity using the profanity.dev API.
 * If profanity is detected, the advisor blocks the request and returns an error message.
 */
public class ProfanityFilterAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private static final String PROFANITY_DETECTION_MESSAGE_TEMPLATE = "Profanity detected in user prompt: {}";

    private static final Logger log = LoggerFactory.getLogger(ProfanityFilterAdvisor.class);

    private final RestClient restClient;
    private final String errorMessage;
    private final int order;

    /**
     * Creates a new instance of ProfanityFilterAdvisor with default error message.
     */
    public ProfanityFilterAdvisor() {
        this("I cannot process prompts containing profanity.");
    }

    /**
     * Creates a new instance of ProfanityFilterAdvisor with a custom error message.
     *
     * @param errorMessage The error message to return when profanity is detected
     */
    public ProfanityFilterAdvisor(String errorMessage) {
        this(errorMessage, Ordered.HIGHEST_PRECEDENCE);
    }

    /**
     * Creates a new instance of ProfanityFilterAdvisor with a custom error message and order.
     *
     * @param errorMessage The error message to return when profanity is detected
     * @param order The order in which this advisor should be called
     */
    public ProfanityFilterAdvisor(String errorMessage, int order) {
        this.errorMessage = errorMessage;
        this.order = order;
        this.restClient = RestClient.builder()
                .baseUrl("https://vector.profanity.dev")
                .build();
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        // Get the user prompt from the request
        String userText = getUserTextFromRequest(advisedRequest);

        if (containsProfanity(userText)) {
            log.warn(PROFANITY_DETECTION_MESSAGE_TEMPLATE, userText);
            // Create a response with error message if profanity is detected
            ChatResponse chatResponse = createErrorChatResponse();
            return new AdvisedResponse(chatResponse, Collections.emptyMap());
        }

        // If no profanity is detected, proceed with the chain
        return chain.nextAroundCall(advisedRequest);
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        // Get the user prompt from the request
        String userText = getUserTextFromRequest(advisedRequest);

        if (containsProfanity(userText)) {
            log.warn(PROFANITY_DETECTION_MESSAGE_TEMPLATE, userText);
            // Create a response with error message if profanity is detected
            ChatResponse chatResponse = createErrorChatResponse();
            return Flux.just(new AdvisedResponse(chatResponse, Collections.emptyMap()));
        }

        // If no profanity is detected, proceed with the chain
        return chain.nextAroundStream(advisedRequest);
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * Extracts the user text from the request messages.
     *
     * @param advisedRequest The request to extract user text from
     * @return The user text content
     */
    private String getUserTextFromRequest(AdvisedRequest advisedRequest) {
        return advisedRequest.userText();
    }

    /**
     * Creates a ChatResponse with the error message.
     *
     * @return A ChatResponse containing the error message
     */
    private ChatResponse createErrorChatResponse() {
        AssistantMessage assistantMessage = new AssistantMessage(this.errorMessage);
        ChatGenerationMetadata metadata = ChatGenerationMetadata.builder()
                .finishReason("blocked_by_profanity_filter")
                .build();
        Generation errorGeneration = new Generation(assistantMessage, metadata);
        return new ChatResponse(List.of(errorGeneration));
    }

    /**
     * Checks if the given text contains profanity using the profanity.dev API.
     *
     * @param text The text to check for profanity
     * @return true if profanity is detected, false otherwise
     */
    private boolean containsProfanity(String text) {
        try {
            // Make an API request to profanity.dev
            Map<String, Object> response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("message", text))
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            // Check if the response indicates profanity
            if (response != null && response.containsKey("isProfanity")) {
                return Boolean.TRUE.equals(response.get("isProfanity"));
            }

            return false;
        } catch (RestClientResponseException e) {
            // Log the error and return false in case of API failure
            // This way, the request will still be processed even if the profanity check fails
            log.error("Error checking profanity: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}