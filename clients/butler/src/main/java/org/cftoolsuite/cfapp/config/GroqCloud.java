package org.cftoolsuite.cfapp.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertyResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Conditional(OnGroqCloud.class)
class GroqCloud {

    @Bean
    public EmbeddingModel embeddingModel(
            PropertyResolver resolver,
            WebClient.Builder webClientBuilder,
            RetryTemplate retryTemplate) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate");

        RestClient.Builder restClientBuilder = RestClient.builder()
                .defaultHeaders(h -> h.addAll(headers));

        OpenAiApi openAiApi = new OpenAiApi(
                resolver.getProperty("spring.ai.openai.embedding.base_url"),
                new SimpleApiKey(resolver.getProperty("spring.ai.openai.embedding.api-key")),
                headers,
                "/v1/chat/completions",
                "/v1/embeddings",
                restClientBuilder,
                webClientBuilder,
                RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER
        );

        return new OpenAiEmbeddingModel(
                openAiApi,
                MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder().model(resolver.getProperty("spring.ai.openai.embedding.options.model")).build()
        );
    }
}
