package org.cftoolsuite.cfapp.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Service
public class ChatService {

    private final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;
    private final McpAsyncClientManager asyncClientManager;
    private final ObjectMapper objectMapper;
    private final String greetingMessage;

    public ChatService(
            @Value("classpath:/system-prompt.st") Resource systemPrompt,
            @Value("classpath:/greeting-prompt.st") Resource greetingPrompt,
            ChatModel chatModel,
            ChatMemory chatMemory,
            McpAsyncClientManager asyncClientManager,
            ObjectMapper objectMapper
    ) {
        String greetingContent;
        try {
            greetingContent = new String(greetingPrompt.getInputStream().readAllBytes());
        } catch (IOException e) {
            log.warn("Failed to load greeting prompt, using default", e);
            greetingContent = "I'm here to help you with questions about your Cloud Foundry foundation.  How can I assist you today?";
        }
        this.greetingMessage = greetingContent;

        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(), new SimpleLoggerAdvisor())
                .build();
        this.asyncClientManager = asyncClientManager;
        this.objectMapper = objectMapper;
    }

    /**
     * Returns the greeting message shown to users when they first visit the chat page.
     *
     * @return the greeting message
     */
    public String getGreetingMessage() {
        return this.greetingMessage;
    }

    /**
     * Streams a response to a question, including both content chunks and metadata.
     *
     * @param inquiry The user's inquiry containing question and selected tools
     * @return A stream of response chunks, with metadata appended at the end
     */
    public Flux<String> streamResponseToQuestion(Inquiry inquiry) {
        // Record start time for response time calculation
        Instant startTime = Instant.now();

        List<McpAsyncClient> clients = asyncClientManager.newMcpAsyncClients();
        AsyncMcpToolCallbackProvider provider = new AsyncMcpToolCallbackProvider(clients);

        // Filter tools if specified in the inquiry
        ToolCallback[] toolCallbacks = provider.getToolCallbacks();

        if (!CollectionUtils.isEmpty(inquiry.tools())) {
            // Filter tool callbacks based on selected tools
            List<String> selectedTools = inquiry.tools();
            toolCallbacks = Arrays.stream(toolCallbacks)
                    .filter(callback -> selectedTools.contains(callback.getToolDefinition().name()))
                    .toArray(ToolCallback[]::new);

            log.info("Filtered tools to {} selected tools: {}", toolCallbacks.length, selectedTools);
        }

        var request = chatClient
                .prompt()
                .user(inquiry.question())
                .tools(toolCallbacks);

        // Get the streaming response
        var streamingResponse = request.stream();

        // Create holders for the final response metadata
        AtomicInteger promptTokens = new AtomicInteger(0);
        AtomicInteger completionTokens = new AtomicInteger(0);
        AtomicInteger totalTokens = new AtomicInteger(0);
        AtomicReference<String> modelRef = new AtomicReference<>("");

        // Stream the content and collect metadata along the way
        return streamingResponse.chatResponse()
                .map(chatResponse -> {
                    ChatResponseMetadata metadata = chatResponse.getMetadata();
                    if (metadata != null && metadata.getUsage() != null) {
                        Usage usage = metadata.getUsage();

                        // Update with max values seen so far
                        promptTokens.updateAndGet(current -> Math.max(current, usage.getPromptTokens()));
                        completionTokens.updateAndGet(current -> Math.max(current, usage.getCompletionTokens()));
                        totalTokens.updateAndGet(current -> Math.max(current, usage.getTotalTokens()));

                        // Capture the model info (should be same for all responses)
                        if (metadata.getModel() != null) {
                            modelRef.set(metadata.getModel());
                        }
                    }
                    // Safely extract content, handling potential nulls
                    return Optional.ofNullable(chatResponse.getResult())
                            .map(Generation::getOutput)
                            .map(AbstractMessage::getText)
                            .orElse("");

                })
                // After all content is streamed, send a final metadata chunk
                .concatWith(Mono.defer(() -> {
                    try {
                        // Calculate response time at completion of streaming
                        Instant endTime = Instant.now();
                        Duration responseDuration = Duration.between(startTime, endTime);
                        String formattedTime = MetricUtils.formatResponseTime(responseDuration);

                        // Calculate tokens per second using accumulated metadata
                        Double tokensPerSecond = MetricUtils.calculateTokensPerSecond(
                                totalTokens.get(),
                                responseDuration.toMillis()
                        );

                        // Create metadata object from accumulated values
                        ChatMetadata chatMetadata = ChatMetadata.builder()
                                .inputTokens(promptTokens.get())
                                .outputTokens(completionTokens.get())
                                .totalTokens(totalTokens.get())
                                .responseTime(formattedTime)
                                .tokensPerSecond(tokensPerSecond)
                                .model(modelRef.get())
                                .build();

                        // Create and serialize a metadata response
                        ChatResponse metadataResponse = ChatResponse.metadataChunk(chatMetadata);
                        String metadataJson = objectMapper.writeValueAsString(metadataResponse);

                        return Mono.just(metadataJson);
                    } catch (JsonProcessingException e) {
                        log.error("Error serializing metadata", e);

                        // Create a minimal JSON metadata if serialization fails
                        try {
                            Instant endTime = Instant.now();
                            Duration responseDuration = Duration.between(startTime, endTime);
                            String formattedTime = MetricUtils.formatResponseTime(responseDuration);

                            return Mono.just(String.format("{\"type\":\"metadata\",\"responseTime\":\"%s\"}", formattedTime));
                        } catch (RuntimeException fallbackError) {
                            // Using specific RuntimeException for fallback error
                            log.error("Failed to create fallback metadata", fallbackError);
                            return Mono.empty();
                        }
                    }
                }));
    }

    public Map<String, String> listTools() {
        List<McpAsyncClient> clients = asyncClientManager.newMcpAsyncClients();
        AsyncMcpToolCallbackProvider provider = new AsyncMcpToolCallbackProvider(clients);
        return
                Arrays
                        .stream(provider.getToolCallbacks())
                        .collect(
                                Collectors
                                        .toMap(k -> k.getToolDefinition().name(),
                                                v -> v.getToolDefinition().description())
                        );
    }

    /**
     * Constructs a basic chat request with the user's question
     */
    private ChatClient.ChatClientRequestSpec constructRequest(String question) {
        return chatClient
                .prompt()
                .user(question);
    }
}