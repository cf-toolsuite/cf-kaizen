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
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
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
import java.util.stream.Collectors;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

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
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory), new SimpleLoggerAdvisor())
                .defaultAdvisors(a -> a.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
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

        // Record start time for response time calculation
        Instant startTime = Instant.now();

        // Get a reference to the ChatClient stream response spec
        var streamSpec = constructRequest(inquiry.question())
                .tools(toolCallbacks)
                .stream();

        // First, stream the content chunks
        return streamSpec
                .content()
                // After all content is streamed, send a final metadata chunk
                .concatWith(Mono.defer(() -> {
                    try {
                        // Calculate response time
                        Instant endTime = Instant.now();
                        Duration responseDuration = Duration.between(startTime, endTime);
                        String formattedTime = MetricUtils.formatResponseTime(responseDuration);

                        // Get the last metadata from the response
                        // Use a synchronous call to get full metadata after streaming is done
                        var chatResponse = chatClient
                                .prompt()
                                .user(inquiry.question())
                                .call()
                                .chatResponse();

                        ChatResponseMetadata metadata = chatResponse.getMetadata();
                        Usage usage = metadata.getUsage();

                        // Calculate tokens per second
                        Double tokensPerSecond = MetricUtils.calculateTokensPerSecond(
                                usage.getTotalTokens(),
                                responseDuration.toMillis()
                        );

                        // Create metadata object
                        ChatMetadata chatMetadata = ChatMetadata.builder()
                                .inputTokens(usage.getPromptTokens())
                                .outputTokens(usage.getCompletionTokens())
                                .totalTokens(usage.getTotalTokens())
                                .responseTime(formattedTime)
                                .tokensPerSecond(tokensPerSecond)
                                .model(metadata.getModel())
                                .build();

                        // Create and serialize a metadata response
                        ChatResponse metadataResponse = ChatResponse.metadataChunk(chatMetadata);
                        return Mono.just(objectMapper.writeValueAsString(metadataResponse));
                    }
                    catch (JsonProcessingException e) {
                        log.error("Error serializing metadata: ", e);
                        return Mono.empty();
                    }
                    catch (Exception e) {
                        log.error("Error retrieving metadata: ", e);
                        return Mono.empty();
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