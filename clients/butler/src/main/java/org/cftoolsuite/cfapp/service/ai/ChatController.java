package org.cftoolsuite.cfapp.service.ai;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/api/butler")
public class ChatController {

    private final ChatService chatService;

    private static record Inquiry(String question) {}

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/stream/chat")
    public ResponseEntity<Flux<String>> streamChat(@RequestBody Inquiry inquiry) {
        return ResponseEntity.ok(chatService.streamResponseToQuestion(inquiry.question()));
    }
    
    @GetMapping("/greeting")
    public ResponseEntity<String> getGreeting() {
        return ResponseEntity.ok(chatService.getGreetingMessage());
    }

}