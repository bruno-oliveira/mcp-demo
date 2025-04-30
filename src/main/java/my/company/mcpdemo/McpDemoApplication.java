package my.company.mcpdemo;

import lombok.extern.slf4j.Slf4j;
import my.company.mcpdemo.service.McpService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

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

                final boolean[] contentReceived = {false};

                System.out.println(mcpService.processPrompt(input).getResult().getOutput().getText());
            }
        };
        }
}
