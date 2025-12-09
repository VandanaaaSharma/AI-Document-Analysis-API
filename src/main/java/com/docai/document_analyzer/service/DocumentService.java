package com.docai.document_analyzer.service;

import com.docai.document_analyzer.model.AnalysisResponse;
import com.docai.document_analyzer.util.PdfUtil;
import com.docai.document_analyzer.util.DocxUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private final PdfUtil pdfUtil;
    private final DocxUtil docxUtil;
    private final OpenAIService openAIService;

    public DocumentService(PdfUtil pdfUtil, DocxUtil docxUtil, OpenAIService openAIService) {
        this.pdfUtil = pdfUtil;
        this.docxUtil = docxUtil;
        this.openAIService = openAIService;
    }

    public AnalysisResponse analyzeDocument(MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new Exception("No file uploaded.");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new Exception("File must have a name.");
        }

        String lower = filename.toLowerCase();
        String text;

        // Extract text
        if (lower.endsWith(".pdf")) {
            text = pdfUtil.extractText(file.getInputStream());
        } else if (lower.endsWith(".docx")) {
            text = docxUtil.extractText(file.getInputStream());
        } else {
            throw new Exception("Only PDF and DOCX formats are supported.");
        }

        // ✅ IMPORTANT FIX — avoid OpenAI JSON failure
        if (text == null || text.trim().isEmpty()) {
            throw new Exception("Failed to extract text from file. PDF/DOCX may be empty or corrupted.");
        }

        // Send extracted text to OpenAI
        AnalysisResponse result = openAIService.generateAnalysis(text);

        // Add metadata
        result.setFileName(filename);
        result.setFileSize(file.getSize());

        return result;
    }
}
