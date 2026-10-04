package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Service
public class ExternalService {

    private static final Logger log =
            LoggerFactory.getLogger(ExternalService.class);

    public CompletionStage<String> callExternalService() {

        log.info("External service called");

        return CompletableFuture.completedFuture(
                "External service response successful"
        );
    }
}







//package com.example.demo.service;
//
//import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
//import io.github.resilience4j.retry.annotation.Retry;
//import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
//import org.springframework.stereotype.Service;
//
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.CompletionStage;
//
//
//@Service
//public class ExternalService {
//
//    @TimeLimiter(name = "externalService")
//    @CircuitBreaker(
//            name = "externalService",
//            fallbackMethod = "fallback"
//    )
//    public CompletionStage<String> callExternalService() {
//
//        System.out.println(">>> Calling external service");
//
//        return CompletableFuture.supplyAsync(() -> {
//
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                throw new RuntimeException(
//                        "External service interrupted"
//                );
//            }
//
//            return "External service response";
//        });
//    }
//
//    public CompletionStage<String> fallback(Throwable throwable) {
//
//        System.out.println(
//                ">>> FALLBACK: " + throwable.getMessage()
//        );
//
//        return CompletableFuture.completedFuture(
//                "External service is currently unavailable. Please try again later."
//        );
//    }
//}
