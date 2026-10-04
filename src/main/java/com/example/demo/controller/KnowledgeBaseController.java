package com.example.demo.controller;

import com.example.demo.service.KnowledgeBaseService;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(
            @RequestParam String query
    ) {

        return knowledgeBaseService.search(query)
                .stream()
                .map(document -> Map.of(
                        "content", document.getText(),
                        "source", document.getMetadata().get("source"),
                        "metadata", document.getMetadata()
                ))
                .toList();
    }
}