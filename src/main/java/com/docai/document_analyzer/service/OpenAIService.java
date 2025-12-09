package com.docai.document_analyzer.service;

import com.docai.document_analyzer.config.OpenAIConfig;
import com.docai.document_analyzer.model.AnalysisResponse;
import com.docai.document_analyzer.model.ChatMessage;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAIService {

    private final OkHttpClient client = new OkHttpClient();
    private final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final OpenAIConfig config;

    public OpenAIService(OpenAIConfig config) {
        this.config = config;
    }

    // ========================= ANALYZE DOCUMENT =========================
    public AnalysisResponse generateAnalysis(String text) throws Exception {

        JSONObject json = new JSONObject();
        json.put("model", "gpt-4o-mini");

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content",
                        "Respond ONLY in valid JSON:\n" +
                                "{ \"summary\":\"...\", \"keywords\":[\"...\"], \"sentiment\":\"...\" }\n\n" +
                                "Document:\n" + text)
        );

        json.put("messages", messages);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .post(RequestBody.create(json.toString(), MediaType.parse("application/json")))
                .build();

        Response response = client.newCall(request).execute();
        String body = response.body().string();

        // ❌ OpenAI error response (NOT JSON)
        if (!body.trim().startsWith("{")) {
            throw new Exception("OpenAI returned non-JSON: " + body);
        }

        JSONObject result = new JSONObject(body);

        // Explicit OpenAI error block
        if (result.has("error")) {
            throw new Exception("OpenAI Error: " + result.getJSONObject("error").getString("message"));
        }

        // Extract content safely
        String content = result
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();

        if (!content.startsWith("{")) {
            throw new Exception("Model returned invalid JSON: " + content);
        }

        JSONObject ai = new JSONObject(content);

        return new AnalysisResponse(
                ai.optString("summary"),
                ai.optJSONArray("keywords").toList().stream().map(Object::toString).toList(),
                ai.optString("sentiment"),
                null,
                0,
                null
        );
    }

    // ========================= CHAT FEATURE =========================
    public String chatWithDocument(String documentText, String question, List<ChatMessage> history) throws Exception {

        JSONArray messages = new JSONArray();

        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", "You are an AI assistant. Answer ONLY using the following document:\n\n" + documentText)
        );

        // Chat history
        for (ChatMessage msg : history) {
            messages.put(new JSONObject()
                    .put("role", msg.getRole())
                    .put("content", msg.getContent()));
        }

        // New message
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", question));

        JSONObject json = new JSONObject();
        json.put("model", "gpt-4o-mini");
        json.put("messages", messages);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .post(RequestBody.create(json.toString(), MediaType.parse("application/json")))
                .build();

        Response response = client.newCall(request).execute();
        String body = response.body().string();

        if (!body.trim().startsWith("{")) {
            throw new Exception("OpenAI returned non-JSON: " + body);
        }

        JSONObject result = new JSONObject(body);

        if (result.has("error")) {
            throw new Exception("OpenAI Error: " + result.getJSONObject("error").getString("message"));
        }

        return result
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();
    }
}
