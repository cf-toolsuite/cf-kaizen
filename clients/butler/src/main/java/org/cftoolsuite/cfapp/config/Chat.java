package org.cftoolsuite.cfapp.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.autoconfigure.openai.OpenAiChatProperties;
import org.springframework.ai.autoconfigure.openai.OpenAiConnectionProperties;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class Chat {

    @Configuration
    @Profile({ "groq-cloud", "openai", "openrouter" })
    // @see https://github.com/spring-projects/spring-ai/issues/372#issuecomment-2242650500
    static class OpenAiCompatible {

        @Bean
        public OpenAiChatModel chatModel(
                OpenAiConnectionProperties connectionProperties,
                OpenAiChatProperties chatProperties,
                WebClient.Builder webClientBuilder,
                RetryTemplate retryTemplate
        ) {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate");

            RestClient.Builder restClientBuilder = RestClient.builder()
                    .defaultHeaders(h -> h.addAll(headers));

            OpenAiApi openAiApi = new OpenAiApi(
                    chatProperties.getBaseUrl() != null ? chatProperties.getBaseUrl() : connectionProperties.getBaseUrl(),
                    new SimpleApiKey(chatProperties.getApiKey() != null ? chatProperties.getApiKey() : connectionProperties.getApiKey()),
                    headers,
                    "/v1/chat/completions",
                    "/v1/embeddings",
                    restClientBuilder,
                    webClientBuilder,
                    RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER
            );

            ObservationRegistry observationRegistry = ObservationRegistry.NOOP;

            return new OpenAiChatModel(
                    openAiApi,
                    chatProperties.getOptions(),
                    DefaultToolCallingManager.builder().observationRegistry(observationRegistry).build(),
                    retryTemplate,
                    observationRegistry
            );
        }

    }

    @Configuration
    static class Conversational {

        @Bean
        public ChatMemory chatMemory() {
            return new InMemoryChatMemory();
        }

    }

}