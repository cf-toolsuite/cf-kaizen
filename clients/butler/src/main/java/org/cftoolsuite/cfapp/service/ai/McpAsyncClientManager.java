package org.cftoolsuite.cfapp.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.autoconfigure.mcp.client.NamedClientMcpTransport;
import org.springframework.ai.autoconfigure.mcp.client.configurer.McpAsyncClientConfigurer;
import org.springframework.ai.autoconfigure.mcp.client.properties.McpClientCommonProperties;
import org.springframework.ai.autoconfigure.mcp.client.properties.McpSseClientProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class McpAsyncClientManager {

    private final McpAsyncClientConfigurer mcpSyncClientConfigurer;
    private final McpClientCommonProperties mcpClientCommonProperties;
    private final McpSseClientProperties mcpSseClientProperties;
    private final WebClient.Builder webClientBuilderTemplate;
    private final ObjectMapper objectMapper;

    public McpAsyncClientManager(McpAsyncClientConfigurer mcpSyncClientConfigurer,
                                 McpClientCommonProperties mcpClientCommonProperties,
                                 McpSseClientProperties mcpSseClientProperties,
                                 WebClient.Builder webClientBuilderTemplate,
                                 ObjectMapper objectMapper
                                ) {

        this.mcpSyncClientConfigurer = mcpSyncClientConfigurer;
        this.mcpClientCommonProperties = mcpClientCommonProperties;
        this.mcpSseClientProperties = mcpSseClientProperties;
        this.webClientBuilderTemplate = webClientBuilderTemplate;
        this.objectMapper = objectMapper;
    }

    public List<McpAsyncClient> newMcpAsyncClients() {

        List<NamedClientMcpTransport> namedTransports = new ArrayList<>();

        for (Map.Entry<String, McpSseClientProperties.SseParameters> serverParameters : mcpSseClientProperties.getConnections().entrySet()) {
            var webClientBuilder = webClientBuilderTemplate.clone().baseUrl(serverParameters.getValue().url());
            var transport = new WebFluxSseClientTransport(webClientBuilder, objectMapper);
            namedTransports.add(new NamedClientMcpTransport(serverParameters.getKey(), transport));
        }

        List<McpAsyncClient> mcpAsyncClients = new ArrayList<>();

        if (!CollectionUtils.isEmpty(namedTransports)) {
            for (NamedClientMcpTransport namedTransport : namedTransports) {

                McpSchema.Implementation clientInfo = new McpSchema.Implementation(mcpClientCommonProperties.getName(),
                        mcpClientCommonProperties.getVersion());

                McpClient.AsyncSpec syncSpec = McpClient.async(namedTransport.transport())
                        .clientInfo(clientInfo)
                        .requestTimeout(mcpClientCommonProperties.getRequestTimeout());

                syncSpec = mcpSyncClientConfigurer.configure(namedTransport.name(), syncSpec);

                var syncClient = syncSpec.build();

                if (mcpClientCommonProperties.isInitialized()) {
                    syncClient.initialize().block();
                }

                mcpAsyncClients.add(syncClient);
            }
        }

        return mcpAsyncClients;
    }

}
