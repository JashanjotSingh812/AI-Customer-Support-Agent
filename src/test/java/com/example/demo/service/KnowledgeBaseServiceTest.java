package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeBaseServiceTest {

    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private KnowledgeBaseService knowledgeBaseService;


    // ============================================================
    // TEST 1
    // Search should return relevant documents
    // ============================================================

    @Test
    void shouldReturnRelevantDocuments() {

        // Arrange
        Document document1 = new Document(
                "Customers can return eligible products within 30 days."
        );

        document1.getMetadata().put(
                "source",
                "return-policy.txt"
        );

        Document document2 = new Document(
                "Refunds are processed within 5-7 business days."
        );

        document2.getMetadata().put(
                "source",
                "refund-policy.txt"
        );

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(document1, document2));

        // Act
        List<Document> result =
                knowledgeBaseService.search(
                        "How many days do I have to return a product?"
                );

        // Assert
        assertEquals(2, result.size());

        assertEquals(
                "return-policy.txt",
                result.get(0).getMetadata().get("source")
        );

        verify(vectorStore)
                .similaritySearch(any(SearchRequest.class));
    }


    // ============================================================
    // TEST 2
    // Search with no matching documents
    // ============================================================

    @Test
    void shouldReturnEmptyListWhenNoDocumentsFound() {

        // Arrange
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of());

        // Act
        List<Document> result =
                knowledgeBaseService.search(
                        "What is the warranty on a product?"
                );

        // Assert
        assertTrue(result.isEmpty());

        verify(vectorStore)
                .similaritySearch(any(SearchRequest.class));
    }


    // ============================================================
    // TEST 3
    // Search should return multiple relevant documents
    // ============================================================

    @Test
    void shouldReturnMultipleRelevantDocuments() {

        // Arrange
        Document returnPolicy = new Document(
                "Return products within 30 days."
        );

        returnPolicy.getMetadata().put(
                "source",
                "return-policy.txt"
        );

        Document shippingPolicy = new Document(
                "Orders are usually delivered within 5-7 days."
        );

        shippingPolicy.getMetadata().put(
                "source",
                "shipping-policy.txt"
        );

        Document refundPolicy = new Document(
                "Refunds are processed after the returned product is inspected."
        );

        refundPolicy.getMetadata().put(
                "source",
                "refund-policy.txt"
        );

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(
                        List.of(
                                returnPolicy,
                                shippingPolicy,
                                refundPolicy
                        )
                );

        // Act
        List<Document> result =
                knowledgeBaseService.search(
                        "Tell me about returns and refunds."
                );

        // Assert
        assertEquals(3, result.size());

        assertEquals(
                "return-policy.txt",
                result.get(0).getMetadata().get("source")
        );

        assertEquals(
                "shipping-policy.txt",
                result.get(1).getMetadata().get("source")
        );

        assertEquals(
                "refund-policy.txt",
                result.get(2).getMetadata().get("source")
        );
    }
}
