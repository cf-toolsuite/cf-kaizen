package org.cftoolsuite.cfapp.service.ai;

import io.modelcontextprotocol.client.McpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.util.ToolUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Component
public class ToolInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private static Logger log = LoggerFactory.getLogger(ToolInitializer.class);

    private final List<McpAsyncClient> mcpClients;
    private final CountDownLatch initLatch = new CountDownLatch(1);
    private final ApplicationEventPublisher publisher;

    public ToolInitializer(List<McpAsyncClient> mcpClients, ApplicationEventPublisher publisher, ToolCallbackProvider toolCallbacks) {
        this.mcpClients = mcpClients;
        this.publisher = publisher;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Flux.fromIterable(mcpClients)
                .flatMap(mcpClient -> mcpClient.listTools()
                        .map(response -> response.tools()
                                .stream()
                                .map(tool -> new AsyncMcpToolCallback(mcpClient, tool))
                                .collect(Collectors.toList()))
                        .doOnNext(this::validateToolCallbacks)
                        .onErrorResume(e -> {
                            log.error("Error initializing tools for client", e);
                            return Mono.just(Collections.emptyList());
                        }))
                .collectList()
                .subscribe(listOfLists -> {
                    List<ToolCallback> toolCallbacks = listOfLists.stream()
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                    publisher.publishEvent(new ToolCallbacksReadyEvent(this).toolCallbacks(toolCallbacks));
                    initLatch.countDown();
                    log.info("Successfully initialized {} tools", toolCallbacks.size());
                }, error -> {
                    log.error("Failed to initialize tools", error);
                    initLatch.countDown();
                });
    }

    private void validateToolCallbacks(List<AsyncMcpToolCallback> toolCallbacks) {
        List<String> duplicateToolNames = ToolUtils.getDuplicateToolNames(toolCallbacks.toArray(new ToolCallback[0]));
        if (!duplicateToolNames.isEmpty()) {
            throw new IllegalStateException(
                    "Multiple tools with the same name (%s)".formatted(String.join(", ", duplicateToolNames)));
        }
    }

}
