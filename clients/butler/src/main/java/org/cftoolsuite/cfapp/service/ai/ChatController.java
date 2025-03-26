package org.cftoolsuite.cfapp.service.ai;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;


@RestController
@RequestMapping("/api/butler")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
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
    public ResponseEntity<Map<String, String>> listTools() {
        return ResponseEntity.ok(chatService.listTools());
    }
}