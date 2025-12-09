package com.docai.document_analyzer.service;

import com.docai.document_analyzer.config.OpenAIConfig;
import com.docai.document_analyzer.model.AnalysisResponse;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class OpenAIService {

    private final OkHttpClient client = new OkHttpClient();
    private final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final OpenAIConfig config;

    public OpenAIService(OpenAIConfig config) {
        this.config = config;
    }

    public AnalysisResponse generateAnalysis(String text) throws Exception {

        // Force strict JSON output
        String prompt =
                "Analyze this document text and respond ONLY in valid JSON EXACTLY like this:\n" +
                "{\n" +
                "  \"summary\": \"...\",\n" +
                "  \"keywords\": [\"...\", \"...\"],\n" +
                "  \"sentiment\": \"Positive | Negative | Neutral\"\n" +
                "}\n\n" +
                "Do NOT add explanations, notes, markdown or text outside JSON.\n\nTEXT:\n" + text;


        JSONObject json = new JSONObject();
        json.put("model", "gpt-4.1-mini");

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", prompt)
        );

        json.put("messages", messages);

        // request
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .post(RequestBody.create(json.toString(), MediaType.parse("application/json")))
                .build();

        Response response = client.newCall(request).execute();
        String resultText = response.body().string();

        // 1️⃣ Handle OpenAI error response
        JSONObject resultJson = new JSONObject(resultText);

        if (resultJson.has("error")) {
            throw new Exception("OpenAI Error: " + resultJson.getJSONObject("error").getString("message"));
        }

        // 2️⃣ Validate "choices"
        if (!resultJson.has("choices")) {
            throw new Exception("OpenAI returned no choices. Full response: " + resultText);
        }

        // Extract AI message
        String content = resultJson
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();

        // 3️⃣ Parse JSON from AI
        JSONObject ai = new JSONObject(content);

        return new AnalysisResponse(
                ai.getString("summary"),
                ai.getJSONArray("keywords").toList().stream().map(Object::toString).toList(),
                ai.getString("sentiment"),
                null,
                0
        );
    }
}
