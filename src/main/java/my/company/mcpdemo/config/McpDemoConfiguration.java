package my.company.mcpdemo.config;

import io.modelcontextprotocol.client.McpSyncClient;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@AllArgsConstructor
public class McpDemoConfiguration {

    private final ChatModel chatModel;
    private final List<McpSyncClient> mcpSyncClients;

    private final String DEFAULT_SYSTEM_PROMPT = """
        You are a useful assistant that can perform web searches using Brave's search API to reply to your questions. 
        You can also send emails using gmail and you follow user's instructions carefully and precisely.
        You always add references of pages you searched and mention all the sources used.
        Note: Before ANY tool call, you should always ask the user for confirmation. Output also the tool you are going 
        to use and the parameters you are going to use. Only after the user confirms, you can call the tool.
        """;


    @Bean
    public ChatClient prepareChatClient() {
        return ChatClient.builder(chatModel)
            .defaultSystem(DEFAULT_SYSTEM_PROMPT)
            .defaultTools(new SyncMcpToolCallbackProvider(mcpSyncClients))
            .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
            .build();
    }
}
