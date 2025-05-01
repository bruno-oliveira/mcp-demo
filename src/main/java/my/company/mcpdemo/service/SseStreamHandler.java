package my.company.mcpdemo.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@Slf4j
@Component
@AllArgsConstructor
public class SseStreamHandler {

    private final ThreadPoolTaskExecutor executor;

    public SseEmitter streamDataFrom(Flux<ChatResponse> dataFlux) {
        SseEmitter emitter = new SseEmitter(50000L);

        executor.execute(() -> dataFlux.doOnNext(data -> {
                    try {
                        emitter.send(data);
                    } catch (IOException e) {
                        log.error("Error sending data", e);
                        emitter.completeWithError(e);
                    }
                })
                .doOnComplete(emitter::complete)
                .doOnError(error -> {
                    log.error("Error occurred in data flux", error);
                    emitter.completeWithError(error);
                })
                .subscribe());

        return emitter;
    }
}