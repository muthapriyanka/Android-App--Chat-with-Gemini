package com.example.chat_with_gemini;

import android.os.Bundle;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private EditText Input;
    private TextView Output;
    private ScrollView scrollView;
    private static final String API_KEY = "AIzaSyAkP_c2dBSggI6NqzIlvOd6Af5uzHECw4k";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Input = findViewById(R.id.user_input);
        Output = findViewById(R.id.chat_output);
        scrollView = findViewById(R.id.scrollView);
        Button sendBtn = findViewById(R.id.send_button);

        sendBtn.setOnClickListener(v -> {
            String prompt = Input.getText().toString();
            if (!prompt.isEmpty()) {
                displayUserMessage(prompt);
                sendPromptToGemini(prompt);
                Input.setText("");
            }
        });
    }

    private void displayUserMessage(String message) {
        Output.append("\n\nPrompt:\n" + message + "\n");
    }

    private void sendPromptToGemini(String prompt) {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();

        try {
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            json.put("contents", contents);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Output.append("\nError: " + e.getMessage()));
            }

            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String res = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(res);
                        JSONArray candidates = jsonResponse.getJSONArray("candidates");
                        JSONObject first = candidates.getJSONObject(0);
                        JSONObject content = first.getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");
                        String reply = parts.getJSONObject(0).getString("text");

                        runOnUiThread(() -> {
                            Output.append("\nResponse:\n" + reply + "\n");
                            scrollView.fullScroll(View.FOCUS_DOWN);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Output.append("\nParsing error: " + e.getMessage()));
                    }
                } else {
                    runOnUiThread(() -> Output.append("\nAPI Error: " + response.message()));
                }
            }
        });
    }
}