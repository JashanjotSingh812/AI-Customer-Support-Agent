package com.example.demo.controller;

import com.example.demo.controller.KnowledgeBaseController;
import com.example.demo.service.KnowledgeBaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KnowledgeBaseControllerTest {

    @Mock
    private KnowledgeBaseService knowledgeBaseService;

    private KnowledgeBaseController knowledgeBaseController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        knowledgeBaseController =
                new KnowledgeBaseController(knowledgeBaseService);
    }

    @Test
    void shouldReturnKnowledgeBaseResults() {

        String query =
                "How many days do I have to return a product?";

        Document document = new Document(
                "Customers can return eligible products within 30 days."
        );

        document.getMetadata().put(
                "source",
                "return-policy.txt"
        );

        when(knowledgeBaseService.search(query))
                .thenReturn(List.of(document));

        List<Map<String, Object>> result =
                knowledgeBaseController.search(query);

        assertNotNull(result);
        assertEquals(1, result.size());

        Map<String, Object> response = result.get(0);

        assertEquals(
                "Customers can return eligible products within 30 days.",
                response.get("content")
        );

        assertEquals(
                "return-policy.txt",
                response.get("source")
        );

        verify(knowledgeBaseService)
                .search(query);
    }

    @Test
    void shouldReturnMultipleKnowledgeDocuments() {

        String query = "return refund policy";

        Document returnDocument = new Document(
                "Products can be returned within 30 days."
        );

        returnDocument.getMetadata().put(
                "source",
                "return-policy.txt"
        );

        Document refundDocument = new Document(
                "Refunds are processed after the returned product is inspected."
        );

        refundDocument.getMetadata().put(
                "source",
                "refund-policy.txt"
        );

        when(knowledgeBaseService.search(query))
                .thenReturn(List.of(
                        returnDocument,
                        refundDocument
                ));

        List<Map<String, Object>> result =
                knowledgeBaseController.search(query);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(
                "return-policy.txt",
                result.get(0).get("source")
        );

        assertEquals(
                "refund-policy.txt",
                result.get(1).get("source")
        );

        verify(knowledgeBaseService)
                .search(query);
    }

    @Test
    void shouldReturnEmptyListWhenNoDocumentsFound() {

        String query = "something completely unknown";

        when(knowledgeBaseService.search(query))
                .thenReturn(List.of());

        List<Map<String, Object>> result =
                knowledgeBaseController.search(query);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(knowledgeBaseService)
                .search(query);
    }
}

