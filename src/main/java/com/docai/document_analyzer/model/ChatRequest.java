// ChatRequest.java
package com.docai.document_analyzer.model;

import lombok.Data;
import java.util.List;

@Data
public class ChatRequest {
    private String documentText;
    private String question;
    private List<ChatMessage> history;
}

