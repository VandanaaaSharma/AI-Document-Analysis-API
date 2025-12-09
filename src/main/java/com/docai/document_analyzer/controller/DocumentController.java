package com.docai.document_analyzer.controller;

import com.docai.document_analyzer.model.ChatRequest;
import com.docai.document_analyzer.service.DocumentService;
import com.docai.document_analyzer.service.OpenAIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/document")
public class DocumentController {

    private final DocumentService service;
    private final OpenAIService openAIService;

    public DocumentController(DocumentService service, OpenAIService openAIService) {
        this.service = service;
        this.openAIService = openAIService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyze(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(service.analyzeDocument(file));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error analyzing: " + e.getMessage());
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        try {
            return ResponseEntity.ok(openAIService.chatWithDocument(
                    request.getDocumentText(),
                    request.getQuestion(),
                    request.getHistory()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error in chat: " + e.getMessage());
        }
    }
}
