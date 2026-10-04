package com.example.demo.service;

import com.example.demo.ai.tool.CustomerTools;
import com.example.demo.ai.tool.OrderTools;
import com.example.demo.ai.tool.TicketTools;
import com.example.demo.dto.ChatRequest;
import com.example.demo.observability.MetricsService;
import com.example.demo.service.ConversationMemoryService;
import com.example.demo.service.KnowledgeBaseService;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger log =
            LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;
    private final OrderTools orderTools;
    private final CustomerTools customerTools;
    private final TicketTools ticketTools;
    private final KnowledgeBaseService knowledgeBaseService;
    private final ConversationMemoryService conversationMemoryService;
    private final MetricsService metricsService;

    public ChatService(
            ChatClient.Builder builder,
            OrderTools orderTools,
            CustomerTools customerTools,
            TicketTools ticketTools,
            KnowledgeBaseService knowledgeBaseService,
            ConversationMemoryService conversationMemoryService,
            MetricsService metricsService
    ) {
        this.chatClient = builder.build();
        this.orderTools = orderTools;
        this.customerTools = customerTools;
        this.ticketTools = ticketTools;
        this.knowledgeBaseService = knowledgeBaseService;
        this.conversationMemoryService = conversationMemoryService;
        this.metricsService = metricsService;
    }

    public String chat(ChatRequest request) {

        String conversationId = request.conversationId();
        String message = request.message();

        log.info(
                "AI chat request received | conversationId={}",
                conversationId
        );

        // Count AI request
        metricsService.aiRequest();

        // Start AI response timer
        Timer.Sample aiTimer = metricsService.startAiTimer();

        try {

            // 1. Get previous conversation
            List<String> history =
                    conversationMemoryService.getHistory(conversationId);

            log.info(
                    "Conversation history retrieved | conversationId={} | messages={}",
                    conversationId,
                    history.size()
            );

            // 2. Retrieve relevant knowledge
            log.info("Searching knowledge base");

            List<Document> documents =
                    knowledgeBaseService.search(message);

            log.info(
                    "Knowledge base search completed | documentsFound={}",
                    documents.size()
            );

            String context = documents.stream()
                    .map(document -> """
                            Source: %s
                            Content: %s
                            """.formatted(
                            document.getMetadata().get("source"),
                            document.getText()
                    ))
                    .collect(Collectors.joining("\n\n"));

            // 3. Build conversation history
            String conversationHistory =
                    String.join("\n", history);

            // 4. Ask the LLM
            log.info("Sending request to LLM");

            String response = chatClient
                    .prompt()
                    .system("""
                            You are a customer support assistant.

                            Answer the customer's question using the
                            conversation history and knowledge base.

                            Rules:
                            1. Use the knowledge base for policy questions.
                            2. Do not invent policies, rules, or facts.
                            3. If the answer is not available in the
                               knowledge base, say that you do not have
                               enough information.
                            4. For knowledge-base answers, mention the
                               source at the end.

                            Previous conversation:
                            %s

                            Knowledge base context:
                            %s
                            """.formatted(
                            conversationHistory,
                            context
                    ))
                    .user(message)
                    .tools(orderTools, customerTools, ticketTools)
                    .call()
                    .content();

            log.info("LLM response received");

            // 5. Save user + AI messages
            conversationMemoryService.addMessage(
                    conversationId,
                    "User: " + message
            );

            conversationMemoryService.addMessage(
                    conversationId,
                    "Assistant: " + response
            );

            log.info(
                    "AI chat request completed | conversationId={}",
                    conversationId
            );

            return response;

        } finally {

            // Record total AI request latency
            metricsService.recordAiResponse(aiTimer);
        }
    }
}
