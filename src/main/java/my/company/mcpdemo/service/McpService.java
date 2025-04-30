package my.company.mcpdemo.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.company.mcpdemo.config.McpDemoConfiguration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@RestController
@AllArgsConstructor
@Slf4j
public class McpService {

    private final McpDemoConfiguration mcpDemoConfiguration;

    public Flux<ChatResponse> processPromptStreaming(@RequestBody String promptText) {
        ChatClient chatClient = mcpDemoConfiguration.prepareChatClient();

        return Flux.defer(() -> {

            var prompt = new Prompt(promptText);

            return chatClient.prompt(prompt).stream().chatResponse();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public ChatResponse processPrompt(@RequestBody String promptText) {
        ChatClient chatClient = mcpDemoConfiguration.prepareChatClient();

            var prompt = new Prompt(promptText);
            return chatClient.prompt(prompt).call().chatResponse();
    }
}
