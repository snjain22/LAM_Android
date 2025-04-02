package com.example.onlinellmjlama;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinellmjlama.api.ApiClient;
import com.example.onlinellmjlama.models.CompletionRequest;
import com.example.onlinellmjlama.models.CompletionResponse;
import com.example.onlinellmjlama.models.FunctionCall;
import com.example.onlinellmjlama.models.Message;
import com.example.onlinellmjlama.utils.DeviceActionManager;

import org.json.JSONObject;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // Configuration
    private static final String API_KEY = "";
    private static final String SYSTEM_PROMPT = "Convert user requests to JSON function calls. " +
            "Available functions: toggle_bluetooth(state: bool), toggle_wifi(state: bool), " +
            "set_brightness(level: 0-255)."
//            + "add_calendar_event(title: string, start_time: long), "
            + "open_camera(), send_sms(phone_number: string, message: string), "
            + "add_contact(name: string, phone_number: string),"
//            + "set_alarm(hour: int, minute: int),"
            + "adjust_volume(level: int). Respond ONLY with function calls in JSON format.";

    // UI Components
    private EditText inputText;
    private TextView resultText;
    private ProgressBar progressBar;

    // Managers
    private DeviceActionManager deviceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeManagers();
        setupListeners();
    }

    private void initializeViews() {
        inputText = findViewById(R.id.input_text);
        resultText = findViewById(R.id.result_text);
        progressBar = findViewById(R.id.progress_bar);
        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(v -> processInstruction());
    }

    private void initializeManagers() {
        deviceManager = new DeviceActionManager(this);
    }

    private void setupListeners() {
        // Add any additional listeners here
    }

    private void processInstruction() {
        String userInput = inputText.getText().toString().trim();
        if (userInput.isEmpty()) {
            showToast("Please enter a command");
            return;
        }

        showLoading(true);
        clearResult();

        Message systemMessage = new Message("system", SYSTEM_PROMPT);
        Message userMessage = new Message("user", userInput);

        CompletionRequest request = new CompletionRequest();
        request.setModel("meta-llama/llama-3.3-70b-instruct:free");
        request.setMessages(Arrays.asList(systemMessage, userMessage));
        request.setTemperature(0.2f);
        request.setMaxTokens(150);

        ApiClient.getInstance(API_KEY)
                .createCompletion(request)
                .enqueue(new Callback<CompletionResponse>() {
                    @Override
                    public void onResponse(Call<CompletionResponse> call,
                                           Response<CompletionResponse> response) {
                        handleApiResponse(response);
                    }

                    @Override
                    public void onFailure(Call<CompletionResponse> call, Throwable t) {
                        handleNetworkFailure(t);
                    }
                });
    }

    private void handleApiResponse(Response<CompletionResponse> response) {
        showLoading(false);

        if (response.isSuccessful() && response.body() != null) {
            CompletionResponse apiResponse = response.body();
            resultText.setText(String.format("✅ JSON: %s\n", response));

            // Extract content from choices[0].message.content
            String content = apiResponse.getContent(); // Directly fetch content from the first choice
            if (content != null && !content.isEmpty()) {
                try {
                    // Parse content as JSON to check for function calls
                    JSONObject jsonContent = new JSONObject(content);
                    if (jsonContent.has("function") && jsonContent.has("args")) {
                        String functionName = jsonContent.getString("function");
                        String args = jsonContent.getString("args");
                        Log.d("MAINACTIVITY - FUNCTION",functionName);
                        Log.d("MAINACTIVITY - ARGUMENTS",args);
                        // Pass function name and arguments to the action manager
                        deviceManager.executeAction(functionName, args);

                        // Display execution result in UI
                        resultText.setText(String.format("✅ Executed: %s\nArgs: %s", functionName, args));
                    } else if (jsonContent.has("function") && !jsonContent.has("args")) {
                        String functionName = jsonContent.getString("function");
                        deviceManager.executeAction(functionName, "");
                    }
                    else{

                        // If no function call, treat as plain text response
                        resultText.setText(content);
                    }
                } catch (Exception e) {
                    // Handle JSON parsing errors
                    showToast("Error parsing response content");
                    Log.e("PARSE_ERROR", "Failed to parse content: " + e.getMessage());
                }
            } else {
                showToast("No valid response received");
                resultText.setText("⚠️ No valid response received.");
            }
        } else {
            handleApiError(response.code());
        }
    }


    private void handleFunctionCall(FunctionCall functionCall) {
        try {
            // Handle brightness permission first
            if ("set_brightness".equals(functionCall.getName()) && !hasBrightnessPermission()) {
                Log.d("BRIGHTNESS ALLOWED", "YAYAYYAYAYAYAYA BRIHTGHHHNENSSSSSS");
                requestBrightnessPermission();
                return;
            }

            // Execute the action
            deviceManager.executeAction(
                    functionCall.getName(),
                    functionCall.getParameters()
            );

            // Show execution result
            resultText.setText(String.format("✅ Executed: %s\n%s",
                    functionCall.getName(),
                    new JSONObject(functionCall.getParameters()).toString(2)
            ));

        } catch (Exception e) {
            showToast("Failed to execute action");
            resultText.setText("❌ Error: " + e.getMessage());
        }
    }

    private void handleTextResponse(String content) {
        if (!content.isEmpty()) {
            resultText.setText(content);
        } else {
            showToast("No valid response received");
        }
    }

    private boolean hasBrightnessPermission() {
        return Settings.System.canWrite(this);
    }

    private void requestBrightnessPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        startActivity(intent);
        showToast("Grant WRITE_SETTINGS permission");
    }

    private void handleNetworkFailure(Throwable t) {
        showLoading(false);
        showToast("Network error: " + t.getMessage());
        resultText.setText("⚠️ Check your internet connection");
    }

    private void handleApiError(int errorCode) {
        showLoading(false);
        String errorMsg = "API Error: " + errorCode;
        showToast(errorMsg);
        resultText.setText("🚨 " + errorMsg);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        inputText.setEnabled(!show);
    }

    private void clearResult() {
        resultText.setText("");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
