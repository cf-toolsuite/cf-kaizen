package org.cftoolsuite.cfapp.service.ai;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class AsyncMcpToolCallback implements ToolCallback {

    private final McpAsyncClient asyncMcpClient;
    private final McpSchema.Tool tool;

    public AsyncMcpToolCallback(McpAsyncClient asyncMcpClient, McpSchema.Tool tool) {
        this.asyncMcpClient = asyncMcpClient;
        this.tool = tool;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return ToolDefinition.builder()
                .name(this.tool.name())
                .description(this.tool.description())
                .inputSchema(ModelOptionsUtils.toJsonString(this.tool.inputSchema()))
                .build();
    }

    @Override
    public String call(String functionInput) {
        Map<String, Object> arguments = ModelOptionsUtils.jsonToMap(functionInput);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();

        this.asyncMcpClient.callTool(new McpSchema.CallToolRequest(this.getToolDefinition().name(), arguments))
                .map(response -> ModelOptionsUtils.toJsonString(response.content()))
                .subscribe(
                        value -> {
                            result.set(value);
                            latch.countDown();
                        },
                        throwable -> {
                            error.set(throwable);
                            latch.countDown();
                        }
                );

        try {
            latch.await();
            if (error.get() != null) {
                if (error.get() instanceof RuntimeException) {
                    throw (RuntimeException) error.get();
                } else {
                    throw new RuntimeException("Error during tool execution", error.get());
                }
            }
            return result.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Tool execution was interrupted", e);
        }
    }
}
