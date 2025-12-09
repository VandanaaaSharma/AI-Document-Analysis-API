package com.docai.document_analyzer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponse {
    private String summary;
    private List<String> keywords;
    private String sentiment;
    private String fileName;
    private long fileSize;
    private String documentText; // 👈 ADD THIS
}
