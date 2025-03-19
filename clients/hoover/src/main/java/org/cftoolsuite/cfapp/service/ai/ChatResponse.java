package org.cftoolsuite.cfapp.service.ai;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Wrapper for chat response data that includes both content and metadata.
 * Used to send content chunks or metadata in the stream.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatResponse(
        String content,
        ChatMetadata metadata,
        boolean isMetadata
) {
    /**
     * Creates a ChatResponse containing only content.
     */
    public static ChatResponse contentChunk(String content) {
        return new ChatResponse(content, null, false);
    }

    /**
     * Creates a ChatResponse containing only metadata.
     */
    public static ChatResponse metadataChunk(ChatMetadata metadata) {
        return new ChatResponse(null, metadata, true);
    }
}
