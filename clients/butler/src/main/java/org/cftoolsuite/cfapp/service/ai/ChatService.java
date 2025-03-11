package org.cftoolsuite.cfapp.service.ai;

import io.modelcontextprotocol.client.McpAsyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final List<McpAsyncClient> mcpAsyncClients;

    public ChatService(
            ChatModel chatModel, ChatMemory chatMemory,
            List<McpAsyncClient> mcpAsyncClients) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory), new SimpleLoggerAdvisor())
                .defaultAdvisors(a -> a.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .build();
        this.mcpAsyncClients = mcpAsyncClients;
    }

    public Flux<String> streamResponseToQuestion(String question) {
        AsyncMcpToolCallbackProvider provider = new AsyncMcpToolCallbackProvider(mcpAsyncClients);
        return constructRequest(question)
                .tools(provider.getToolCallbacks())
                .stream()
                .content();
    }

    private ChatClient.ChatClientRequestSpec constructRequest(String question) {
        return chatClient
                .prompt()
                .user(question);
    }

}
