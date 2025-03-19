package org.cftoolsuite.cfapp.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service
public class ChatService {
    private final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;
    private final McpAsyncClientManager asyncClientManager;
    private final ObjectMapper objectMapper;

    public ChatService(
            ChatModel chatModel,
            ChatMemory chatMemory,
            McpAsyncClientManager asyncClientManager,
            ObjectMapper objectMapper
    ) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory), new SimpleLoggerAdvisor())
                .defaultAdvisors(a -> a.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .build();
        this.asyncClientManager = asyncClientManager;
        this.objectMapper = objectMapper;
    }

    /**
     * Streams a response to a question, including both content chunks and metadata.
     * 
     * @param question The user's question
     * @return A stream of response chunks, with metadata appended at the end
     */
    public Flux<String> streamResponseToQuestion(String question) {
        AsyncMcpToolCallbackProvider provider =
                new AsyncMcpToolCallbackProvider(
                        asyncClientManager.newMcpAsyncClients()
                );

        // Record start time for response time calculation
        Instant startTime = Instant.now();
        
        // Get a reference to the ChatClient stream response spec
        var streamSpec = constructRequest(question)
                .tools(provider.getToolCallbacks())
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
                        String formattedTime = formatResponseTime(responseDuration);
                        
                        // Get the last metadata from the response
                        // Use a synchronous call to get full metadata after streaming is done
                        var chatResponse = chatClient
                                .prompt()
                                .user(question)
                                .call()
                                .chatResponse();
                        
                        ChatResponseMetadata metadata = chatResponse.getMetadata();
                        Usage usage = metadata.getUsage();
                        
                        // Calculate tokens per second
                        Double tokensPerSecond = calculateTokensPerSecond(
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

    /**
     * Calculates tokens per second based on total tokens and response time in milliseconds.
     * 
     * @param totalTokens Total number of tokens processed
     * @param responseTimeMs Response time in milliseconds
     * @return Tokens per second, rounded to 2 decimal places
     */
    private Double calculateTokensPerSecond(Integer totalTokens, long responseTimeMs) {
        if (totalTokens == null || totalTokens == 0 || responseTimeMs == 0) {
            return 0.0;
        }
        
        // Convert milliseconds to seconds (as a decimal)
        double responseTimeSeconds = responseTimeMs / 1000.0;
        
        // Calculate tokens per second
        double tps = totalTokens / responseTimeSeconds;
        
        // Round to 2 decimal places
        BigDecimal bd = BigDecimal.valueOf(tps);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        
        return bd.doubleValue();
    }

    /**
     * Formats a duration into a human-readable string (e.g., "1m30s")
     */
    private String formatResponseTime(Duration duration) {
        long totalSeconds = duration.getSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        
        if (minutes > 0) {
            return String.format("%dm%ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
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
