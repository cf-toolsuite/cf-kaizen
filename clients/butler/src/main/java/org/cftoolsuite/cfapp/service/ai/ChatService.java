package org.cftoolsuite.cfapp.service.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatModel model, ChatMemory chatMemory, ToolCallbackProvider tools) {
        this.chatClient =
                ChatClient
                        .builder(model)
                        .defaultTools(tools)
                        .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory), new SimpleLoggerAdvisor())
                        .defaultAdvisors(a -> a.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                        .build();
    }

    public String respondToQuestion(String question) {
        return constructRequest(question)
                .call()
                .content();
    }

    public Flux<String> streamResponseToQuestion(String question) {
        return constructRequest(question)
                .stream()
                .content();
    }

    private ChatClient.ChatClientRequestSpec constructRequest(String question) {
        return chatClient
                .prompt()
                .advisors(RetrievalAugmentationAdvisor
                        .builder()
                        .build())
                .user(question);
    }
}
