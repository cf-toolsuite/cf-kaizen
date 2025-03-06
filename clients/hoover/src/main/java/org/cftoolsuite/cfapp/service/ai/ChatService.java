package org.cftoolsuite.cfapp.service.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service
public class ChatService implements ApplicationListener<ToolCallbacksReadyEvent> {

    private ChatClient chatClient;
    private final ChatModel chatModel;
    private final ChatMemory chatMemory;

    public ChatService(ChatModel chatModel, ChatMemory chatMemory) {
        this.chatModel = chatModel;
        this.chatMemory = chatMemory;
    }

    public Flux<String> streamResponseToQuestion(String question) {
        return constructRequest(question)
                .stream()
                .content();
    }

    private ChatClient.ChatClientRequestSpec constructRequest(String question) {
        return chatClient
                .prompt()
                .user(question);
    }

    @Override
    public void onApplicationEvent(ToolCallbacksReadyEvent event) {
        this.chatClient =
                ChatClient
                        .builder(chatModel)
                        .defaultTools(event.getToolCallbacks())
                        .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory), new SimpleLoggerAdvisor())
                        .defaultAdvisors(a -> a.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                        .build();
    }
}
