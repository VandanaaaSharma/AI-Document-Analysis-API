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

        JSONObject json = new JSONObject();
        json.put("model", "gpt-4.1-mini");

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content",
                        "Analyze this text and return STRICT JSON like this:\n" +
                                "{ \"summary\": \"...\", \"keywords\": [\"...\"], \"sentiment\": \"...\" }\n\n" +
                                "TEXT:\n" + text)
        );

        json.put("messages", messages);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .post(RequestBody.create(json.toString(), MediaType.parse("application/json")))
                .build();

        Response response = client.newCall(request).execute();
        String result = response.body().string();

        String content = new JSONObject(result)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

        // Parse the JSON returned by the model
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
