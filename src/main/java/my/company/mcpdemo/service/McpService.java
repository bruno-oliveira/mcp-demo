package my.company.mcpdemo.controller;

import io.modelcontextprotocol.client.McpSyncClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@Slf4j
public class McpController {

    private ChatModel chatModel;
    private final List<McpSyncClient> mcpSyncClients;

    private final String DEFAULT_SYSTEM_PROMPT = """
        You are a useful assistant that can perform web searches using Brave's search API to reply to your questions. 
        You can also send emails using gmail and you follow user's instructions carefully and precisely.
        You always add references of pages you searched and mention all the sources used.
        """;

    @PostMapping(value = "/mcp", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> getResponse(@RequestBody String promptText) {

        mcpSyncClients.forEach(e->{
            log.info("MCP Sync Client: {}", e.getClientInfo().name());
            log.info(e.getClass().getSimpleName());
        });

        return Flux.defer(() -> {
            var chatClient = ChatClient.builder(chatModel)
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .defaultTools(new SyncMcpToolCallbackProvider(mcpSyncClients))
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory(), UUID.randomUUID().toString(),
                    100, 1))
                .build();

            var prompt = new Prompt(promptText);

            return chatClient.prompt(prompt).stream().chatResponse();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
