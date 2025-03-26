package org.cftoolsuite.cfapp.service.ai;

import io.modelcontextprotocol.client.McpAsyncClient;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/butler")
public class ChatController {

    private final ChatService chatService;
    private final McpAsyncClientManager mcpAsyncClientManager;

    public ChatController(ChatService chatService, McpAsyncClientManager mcpAsyncClientManager) {
        this.chatService = chatService;
        this.mcpAsyncClientManager = mcpAsyncClientManager;
    }

    @PostMapping("/stream/chat")
    public ResponseEntity<Flux<String>> streamChat(@RequestBody Inquiry inquiry) {
        return ResponseEntity.ok(chatService.streamResponseToQuestion(inquiry));
    }
    
    @GetMapping("/greeting")
    public ResponseEntity<String> getGreeting() {
        return ResponseEntity.ok(chatService.getGreetingMessage());
    }

    @GetMapping("/tools")
    public ResponseEntity<Map<String, String>> getTools() {
        List<McpAsyncClient> clients = mcpAsyncClientManager.newMcpAsyncClients();
        AsyncMcpToolCallbackProvider provider = new AsyncMcpToolCallbackProvider(clients);
        Map<String, String> result = Arrays.stream(provider.getToolCallbacks()).collect(Collectors.toMap(k -> k.getToolDefinition().name(), v -> v.getToolDefinition().description()));
        return ResponseEntity.ok(result);
    }
}