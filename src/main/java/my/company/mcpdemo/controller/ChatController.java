package my.company.mcpdemo.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.company.mcpdemo.service.McpService;
import my.company.mcpdemo.service.SseStreamHandler;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
@RestController
@RequestMapping("/api/chat")
@AllArgsConstructor
@Slf4j
public class ChatController {

    private final McpService mcpService;
    private final SseStreamHandler sseStreamHandler;

    @PostMapping(value = "/stream")
    public SseEmitter streamChatJson(@RequestBody String promptText) {
        log.info("Received streaming request with prompt: {}", promptText);

        // Get the streaming flux from the service
        Flux<ChatResponse> responseFlux = mcpService.processPromptStreaming(promptText);

        // Use the stream handler to create and configure the emitter
        SseEmitter emitter = sseStreamHandler.streamDataFrom(responseFlux);

        return emitter;
    }
}