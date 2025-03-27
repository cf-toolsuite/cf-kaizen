package org.cftoolsuite.cfapp.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.client.autoconfigure.NamedClientMcpTransport;
import org.springframework.ai.mcp.client.autoconfigure.configurer.McpAsyncClientConfigurer;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpClientCommonProperties;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties;
import org.springframework.beans.factory.ObjectProvider;
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
    private final ObjectProvider<WebClient.Builder> webClientBuilderProvider;
    private final ObjectProvider<ObjectMapper> objectMapperProvider;

    public McpAsyncClientManager(McpAsyncClientConfigurer mcpSyncClientConfigurer,
                                 McpClientCommonProperties mcpClientCommonProperties,
                                 McpSseClientProperties mcpSseClientProperties,
                                 ObjectProvider<WebClient.Builder> webClientBuilderProvider,
                                 ObjectProvider<ObjectMapper> objectMapperProvider
                                ) {

        this.mcpSyncClientConfigurer = mcpSyncClientConfigurer;
        this.mcpClientCommonProperties = mcpClientCommonProperties;
        this.mcpSseClientProperties = mcpSseClientProperties;
        this.webClientBuilderProvider = webClientBuilderProvider;
        this.objectMapperProvider = objectMapperProvider;
    }

    public List<McpAsyncClient> newMcpAsyncClients() {

        List<NamedClientMcpTransport> namedTransports = new ArrayList<>();

        var webClientBuilderTemplate = webClientBuilderProvider.getIfAvailable(WebClient::builder);
        var objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);

        for (Map.Entry<String, McpSseClientProperties.SseParameters> serverParameters : mcpSseClientProperties.getConnections().entrySet()) {
            var webClientBuilder = webClientBuilderTemplate.clone().baseUrl(serverParameters.getValue().url());
            var transport = new WebFluxSseClientTransport(webClientBuilder, objectMapper);
            namedTransports.add(new NamedClientMcpTransport(serverParameters.getKey(), transport));
        }

        List<McpAsyncClient> mcpAsyncClients = new ArrayList<>();

        if (!CollectionUtils.isEmpty(namedTransports)) {
            for (NamedClientMcpTransport namedTransport : namedTransports) {

                McpSchema.Implementation clientInfo = new McpSchema.Implementation(
                        connectedClientName(mcpClientCommonProperties.getName(), namedTransport.name()),
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

    private String connectedClientName(String clientName, String serverConnectionName) {
        return clientName + " - " + serverConnectionName;
    }

}
