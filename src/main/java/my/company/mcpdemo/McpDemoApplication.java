package my.company.mcpdemo;

import lombok.extern.slf4j.Slf4j;
import my.company.mcpdemo.service.McpService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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

                standardMode(mcpService, input);
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


    private static void streamingMode(String input, AtomicBoolean contentReceived) {
        System.out.println("Starting stream processing with SSE...");

        // Create WebClient for communicating with our controller
        WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8080") // Adjust if your server runs on a different port
            .build();

        // Create a latch to wait for completion
        CountDownLatch latch = new CountDownLatch(1);

        // Make a request to our streaming endpoint
        // Now using POST with JSON body instead of URL parameters
        webClient.post()
            .uri("/api/chat/stream")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(input) // Send the prompt directly in the request body
            .retrieve()
            .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
            .subscribe(
                event -> {
                    // Process each chunk as it arrives
                    if (event.data() != null) {
                        System.out.print(event.data());
                        System.out.flush(); // Ensure immediate display
                        contentReceived.set(true);
                    }
                },
                error -> {
                    System.err.println("\nError occurred: " + error.getMessage());
                    latch.countDown();
                },
                () -> {
                    if (!contentReceived.get()) {
                        System.out.print("[No content available]");
                    }
                    System.out.println("\n"); // Add newline after completion
                    latch.countDown();
                }
            );

        try {
            System.out.println("Waiting for stream to complete...");
            if (!latch.await(2, TimeUnit.MINUTES)) {
                System.err.println("Timeout waiting for response");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for response");
        }
    }

}
