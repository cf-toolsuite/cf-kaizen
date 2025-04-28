package org.cftoolsuite.cfapp.service.ai;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Contains metadata about a chat response, such as token counts, response time, and model used.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatMetadata(
        Integer inputTokens,
        Integer outputTokens,
        Integer totalTokens,
        String responseTime,
        Double tokensPerSecond,
        String model
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer inputTokens;
        private Integer outputTokens;
        private Integer totalTokens;
        private String responseTime;
        private Double tokensPerSecond;
        private String model;

        public Builder inputTokens(Integer inputTokens) {
            this.inputTokens = inputTokens;
            return this;
        }

        public Builder outputTokens(Integer outputTokens) {
            this.outputTokens = outputTokens;
            return this;
        }

        public Builder totalTokens(Integer totalTokens) {
            this.totalTokens = totalTokens;
            return this;
        }

        public Builder responseTime(String responseTime) {
            this.responseTime = responseTime;
            return this;
        }

        public Builder tokensPerSecond(Double tokensPerSecond) {
            this.tokensPerSecond = tokensPerSecond;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public ChatMetadata build() {
            return new ChatMetadata(inputTokens, outputTokens, totalTokens, responseTime, tokensPerSecond, model);
        }
    }
}
