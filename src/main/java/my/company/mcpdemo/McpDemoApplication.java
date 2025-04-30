package my.company.mcpdemo;

import lombok.extern.slf4j.Slf4j;
import my.company.mcpdemo.service.McpService;
import org.springframework.ai.chat.model.Generation;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootApplication
@Slf4j
public class McpDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpDemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(McpService mcpService) {
        return args -> {
            System.out.println("Welcome to MCP CLI!");
            System.out.println("Type your messages and press Enter to send. Type 'exit' to quit.");
            System.out.println("------------------------------------------------------");

            Scanner scanner = new Scanner(System.in);
            String input;

            while (true) {
                System.out.print("> ");
                input = scanner.nextLine().trim();

                if ("exit".equalsIgnoreCase(input)) {
                    System.out.println("Goodbye!");
                    break;
                }

                System.out.print("Assistant: ");

                AtomicBoolean contentReceived = new AtomicBoolean(false);

                streamingMode(mcpService, input, contentReceived);

            }
        };
    }

    private static void standardMode(McpService mcpService, String input) {
        try {
            System.out.println(mcpService.processPrompt(input).getResult().getOutput().getText());

        } catch (Exception e) {
            System.err.println("\nFailed to process response: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }


    private static void streamingMode(McpService mcpService, String input, AtomicBoolean contentReceived) {
        try {
            mcpService.processPromptStreaming(input).doOnNext(token -> {
                // Print each token without a newline
                    Generation result = token.getResult();
                    if(result!=null) {

                        System.out.print(result.getOutput().getText());
                        System.out.flush(); // Ensure immediate display
                        contentReceived.set(true);
                    }
                })
                .doOnComplete(() -> {
                    if (!contentReceived.get()) {
                        System.out.print("[No content available]");
                    }
                    System.out.println("\n"); // Add double newline after completion
                })
                .doOnError(error -> {
                    System.err.println("\nError occurred: " + error.getMessage());
                })
                .blockLast(Duration.ofSeconds(60)); // Set a reasonable timeout

        } catch (Exception e) {
            System.err.println("\nFailed to process response: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
