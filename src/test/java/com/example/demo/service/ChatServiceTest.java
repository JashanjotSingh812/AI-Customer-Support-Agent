package com.example.demo.service;

import com.example.demo.ai.tool.CustomerTools;
import com.example.demo.ai.tool.OrderTools;
import com.example.demo.ai.tool.TicketTools;
import com.example.demo.dto.ChatRequest;
import com.example.demo.observability.MetricsService;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    @Mock
    private OrderTools orderTools;

    @Mock
    private CustomerTools customerTools;

    @Mock
    private TicketTools ticketTools;

    @Mock
    private KnowledgeBaseService knowledgeBaseService;

    @Mock
    private ConversationMemoryService conversationMemoryService;

    @Mock
    private MetricsService metricsService;

    @Mock
    private Timer.Sample timerSample;

    private ChatService chatService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        when(chatClientBuilder.build())
                .thenReturn(chatClient);

        chatService = new ChatService(
                chatClientBuilder,
                orderTools,
                customerTools,
                ticketTools,
                knowledgeBaseService,
                conversationMemoryService,
                metricsService
        );
    }

    @Test
    void shouldReturnAiResponse() {

        ChatRequest request = new ChatRequest(
                "conv-1001",
                "What is your return policy?"
        );

        Document document = new Document(
                "Customers can return eligible products within 30 days."
        );

        document.getMetadata().put(
                "source",
                "return-policy.txt"
        );

        when(conversationMemoryService.getHistory("conv-1001"))
                .thenReturn(List.of());

        when(knowledgeBaseService.search(
                "What is your return policy?"
        )).thenReturn(List.of(document));

        when(metricsService.startAiTimer())
                .thenReturn(timerSample);

        when(chatClient.prompt())
                .thenReturn(requestSpec);

        when(requestSpec.system(anyString()))
                .thenReturn(requestSpec);

        when(requestSpec.user(anyString()))
                .thenReturn(requestSpec);

        when(requestSpec.tools(
                orderTools,
                customerTools,
                ticketTools
        )).thenReturn(requestSpec);

        when(requestSpec.call())
                .thenReturn(responseSpec);

        when(responseSpec.content())
                .thenReturn(
                        "Customers can return eligible products within 30 days. " +
                                "Source: return-policy.txt"
                );

        String result = chatService.chat(request);

        assertEquals(
                "Customers can return eligible products within 30 days. " +
                        "Source: return-policy.txt",
                result
        );

        verify(metricsService)
                .aiRequest();

        verify(metricsService)
                .startAiTimer();

        verify(metricsService)
                .recordAiResponse(timerSample);

        verify(conversationMemoryService)
                .getHistory("conv-1001");

        verify(knowledgeBaseService)
                .search("What is your return policy?");

        verify(conversationMemoryService)
                .addMessage(
                        "conv-1001",
                        "User: What is your return policy?"
                );

        verify(conversationMemoryService)
                .addMessage(
                        "conv-1001",
                        "Assistant: Customers can return eligible products within 30 days. " +
                                "Source: return-policy.txt"
                );
    }

    @Test
    void shouldUseConversationHistory() {

        ChatRequest request = new ChatRequest(
                "conv-2001",
                "What did I ask before?"
        );

        when(conversationMemoryService.getHistory("conv-2001"))
                .thenReturn(List.of(
                        "User: Where is my order?",
                        "Assistant: Your order is shipped."
                ));

        when(knowledgeBaseService.search(
                "What did I ask before?"
        )).thenReturn(List.of());

        when(metricsService.startAiTimer())
                .thenReturn(timerSample);

        when(chatClient.prompt())
                .thenReturn(requestSpec);

        when(requestSpec.system(anyString()))
                .thenReturn(requestSpec);

        when(requestSpec.user(anyString()))
                .thenReturn(requestSpec);

        when(requestSpec.tools(
                orderTools,
                customerTools,
                ticketTools
        )).thenReturn(requestSpec);

        when(requestSpec.call())
                .thenReturn(responseSpec);

        when(responseSpec.content())
                .thenReturn("You previously asked about your order.");

        String result = chatService.chat(request);

        assertEquals(
                "You previously asked about your order.",
                result
        );

        verify(conversationMemoryService)
                .getHistory("conv-2001");

        // Verifies that system prompt was built using the history.
        verify(requestSpec)
                .system(argThat((ArgumentMatcher<String>) systemPrompt ->
                        systemPrompt.contains("Where is my order?")
                                && systemPrompt.contains("Your order is shipped.")
                ));
    }

    @Test
    void shouldSearchKnowledgeBaseBeforeCallingLlm() {

        ChatRequest request = new ChatRequest(
                "conv-3001",
                "How long does shipping take?"
        );

        Document document = new Document(
                "Standard shipping takes 5-7 business days."
        );

        document.getMetadata().put(
                "source",
                "shipping-policy.txt"
        );

        when(conversationMemoryService.getHistory("conv-3001"))
                .thenReturn(List.of());

        when(knowledgeBaseService.search(
                "How long does shipping take?"
        )).thenReturn(List.of(document));

        when(metricsService.startAiTimer())
                .thenReturn(timerSample);

        when(chatClient.prompt())
                .thenReturn(requestSpec);

        when(requestSpec.system(anyString()))
                .thenReturn(requestSpec);

        when(requestSpec.user(anyString()))
                .thenReturn(requestSpec);

        when(requestSpec.tools(
                orderTools,
                customerTools,
                ticketTools
        )).thenReturn(requestSpec);

        when(requestSpec.call())
                .thenReturn(responseSpec);

        when(responseSpec.content())
                .thenReturn(
                        "Standard shipping takes 5-7 business days."
                );

        chatService.chat(request);

        verify(knowledgeBaseService)
                .search("How long does shipping take?");

        verify(requestSpec)
                .system(argThat((String systemPrompt) ->
                        systemPrompt.contains(
                                "Standard shipping takes 5-7 business days."
                        )
                ));
    }

    @Test
    void shouldSaveUserAndAssistantMessages() {

        ChatRequest request = new ChatRequest(
                "conv-4001",
                "Where is my order?"
        );

        when(conversationMemoryService.getHistory("conv-4001"))
                .thenReturn(List.of());

        when(knowledgeBaseService.search(
                "Where is my order?"
        )).thenReturn(List.of());

        when(metricsService.startAiTimer())
                .thenReturn(timerSample);

        when(chatClient.prompt())
                .thenReturn(requestSpec);

        when(requestSpec.system(anyString()))
                .thenReturn(requestSpec);

        when(requestSpec.user(anyString()))
                .thenReturn(requestSpec);

        when(requestSpec.tools(
                orderTools,
                customerTools,
                ticketTools
        )).thenReturn(requestSpec);

        when(requestSpec.call())
                .thenReturn(responseSpec);

        when(responseSpec.content())
                .thenReturn(
                        "Order 1001 is currently shipped."
                );

        chatService.chat(request);

        verify(conversationMemoryService)
                .addMessage(
                        "conv-4001",
                        "User: Where is my order?"
                );

        verify(conversationMemoryService)
                .addMessage(
                        "conv-4001",
                        "Assistant: Order 1001 is currently shipped."
                );
    }

    @Test
    void shouldRecordMetricsWhenLlmSucceeds() {

        ChatRequest request = new ChatRequest(
                "conv-5001",
                "Hello"
        );

        when(conversationMemoryService.getHistory("conv-5001"))
                .thenReturn(List.of());

        when(knowledgeBaseService.search("Hello"))
                .thenReturn(List.of());

        when(metricsService.startAiTimer())
                .thenReturn(timerSample);

        when(chatClient.prompt())
                .thenReturn(requestSpec);

        when(requestSpec.system(anyString()))
                .thenReturn(requestSpec);

        when(requestSpec.user(anyString()))
                .thenReturn(requestSpec);

        when(requestSpec.tools(
                orderTools,
                customerTools,
                ticketTools
        )).thenReturn(requestSpec);

        when(requestSpec.call())
                .thenReturn(responseSpec);

        when(responseSpec.content())
                .thenReturn("Hello! How can I help you?");

        chatService.chat(request);

        verify(metricsService)
                .aiRequest();

        verify(metricsService)
                .startAiTimer();

        verify(metricsService)
                .recordAiResponse(timerSample);
    }

    @Test
    void shouldRecordMetricsWhenLlmFails() {

        ChatRequest request = new ChatRequest(
                "conv-6001",
                "Hello"
        );

        when(conversationMemoryService.getHistory("conv-6001"))
                .thenReturn(List.of());

        when(knowledgeBaseService.search("Hello"))
                .thenReturn(List.of());

        when(metricsService.startAiTimer())
                .thenReturn(timerSample);

        when(chatClient.prompt())
                .thenReturn(requestSpec);

        when(requestSpec.system(anyString()))
                .thenReturn(requestSpec);

        when(requestSpec.user(anyString()))
                .thenReturn(requestSpec);

        when(requestSpec.tools(
                orderTools,
                customerTools,
                ticketTools
        )).thenReturn(requestSpec);

        when(requestSpec.call())
                .thenThrow(
                        new RuntimeException("LLM unavailable")
                );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> chatService.chat(request)
        );

        assertEquals(
                "LLM unavailable",
                exception.getMessage()
        );

        // finally block must still record latency
        verify(metricsService)
                .recordAiResponse(timerSample);
    }
}
