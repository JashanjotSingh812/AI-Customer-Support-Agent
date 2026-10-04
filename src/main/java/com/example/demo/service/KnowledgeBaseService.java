package com.example.demo.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KnowledgeBaseService {

    private final VectorStore vectorStore;

    public KnowledgeBaseService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void loadDocument(String resourcePath) {

        String source = resourcePath.substring(
                resourcePath.lastIndexOf("/") + 1
        );

        // Check if this document is already indexed
        List<Document> existingDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(source)
                        .topK(1)
                        .build()
        );

        boolean alreadyIndexed = existingDocuments.stream()
                .anyMatch(document ->
                        source.equals(document.getMetadata().get("source"))
                );

        if (alreadyIndexed) {
            System.out.println("Skipping already indexed document: " + source);
            return;
        }

        System.out.println("Indexing document: " + source);

        TextReader reader = new TextReader(resourcePath);

        List<Document> documents = reader.get();

        List<Document> documentsWithMetadata = documents.stream()
                .map(document -> {
                    Map<String, Object> metadata =
                            new HashMap<>(document.getMetadata());

                    metadata.put("source", source);

                    return new Document(
                            document.getText(),
                            metadata
                    );
                })
                .toList();

        TokenTextSplitter splitter = new TokenTextSplitter();

        List<Document> chunks = splitter.apply(documentsWithMetadata);

        vectorStore.add(chunks);

        System.out.println("Indexed document: " + source);
    }

    public List<Document> search(String query) {

        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(3)
                        .similarityThreshold(0.5)
                        .build()
        );
    }
}