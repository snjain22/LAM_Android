# **Automation of Android-Based Actions Using Large Action Models**

This project is an application that enables users to interact with a Large Language Model (LLM) to execute system commands on an Android device. It incorporates two approaches: **Offline LLM finetuned on Android System Calls** and **Online LLM integrated with a local Tool Calling Agent**.

---

## Working Demonstration

<img src="https://github.com/snjain22/LAM_Android/blob/main/demo.gif" width="250" alt="Demo">

---
## **Methodologies**

### **Approach 1: Offline LLM Finetuned on Android System Calls**
We have finetuned the **Gemma-2B model** from Google using **LoRA (Low-Rank Adaptation)** and **SFTTrainer**. The dataset used for finetuning is a JSON file structured as follows:

```json
[
  {
    "instruction": "Turn on WiFi",
    "response": "WiFi has been enabled."
  },
  ...
]
```

This dataset was generated for 10 Android API calls, listed below:
- WiFi
- Bluetooth
- Calendar
- Image (Camera Photos)
- Send SMS
- Timer
- Create Videos
- Volume
- Alarms

The finetuned model's weights can be downloaded using the following [link](https://learnermanipal-my.sharepoint.com/:f:/g/personal/sambhav_mitmpl2022_learner_manipal_edu/Eg8fHWXxbShMp4eNy_UorEQBZq1_lkg9WB4H5K2FMA-6UQ?e=qGmzMH).

#### **Steps to Run the Model**

1. Ensure the MediaPipe LLM API is included in your `build.gradle (Module)` file:
   ```gradle
   dependencies {
       implementation 'com.google.mediapipe:tasks-vision:latest-version'
   }
   ```

2. Use the following Kotlin code to load and run the LoRA-adapted model:
   ```kotlin
   import android.content.Context
   import com.google.mediapipe.tasks.genai.LlmInference
   import com.google.mediapipe.tasks.genai.LlmInferenceOptions

   fun loadLoraModel(context: Context): LlmInference {
       val baseModelPath = "/sdcard/llm_model/samsunggem.bin"
       val loraModelPath = "/sdcard/llm_model/adapter_model.safetensors"

       val options = LlmInferenceOptions.builder()
           .setModelPath(baseModelPath)
           .setMaxTokens(1000)
           .setTopK(40)
           .setTemperature(0.8f)
           .setRandomSeed(101)
           .setLoraPath(loraModelPath)  // Load LoRA adapter
           .build()

       return LlmInference.createFromOptions(context, options)
   }

   val llmInference = loadLoraModel(context)

   val prompt = "Turn off the WiFi please."
   val response = llmInference.generateResponse(prompt)
   println("Model Response: $response")
   ```

---

### **Approach 2: Online LLM Integrated with a Local Tool Calling Agent**
This approach uses the open-source **Llama-3.1-70B-Instruct** model from Meta. A custom tool-calling agent has been developed to handle device actions through API integrations. The LLM model has been accessed through OpenRouter, the link to the model can be found [here](https://openrouter.ai/meta-llama/llama-3.3-70b-instruct:free).

> **Note:**  
> Free limit: If you are using a free model variant (with an ID ending in `:free`), then you will be limited to **20 requests per minute** and **200 requests per day**.  
> For more details, check the [OpenRouter API Limits Documentation](https://openrouter.ai/docs/api-reference/limits).


#### **Project Structure**
1. **CompletionRequest.java**: Defines the structure of a request to the OpenRouter API.
2. **Message.java**: Represents a message object with optional function call support.
3. **OpenRouterApi.java**: Interface for defining API endpoints using Retrofit.
4. **ApiClient.java**: Manages the Retrofit client setup for OpenRouter API communication.
5. **DeviceActionManager.java**: Contains utility methods for managing device actions like Bluetooth, Wi-Fi, volume, brightness, alarms, etc.

The model can perform the following actions
- Toggle WiFi
- Toggle Bluetooth
- Send SMS
- Increase/Decrease Volume
- Increase/Decrease Brightness
- Add Contact

#### **Setup Instructions**

1. **Prerequisites**
   - Android Studio 
   - OpenRouter API Key 
   - Retrofit library dependencies:
     ```gradle
         implementation 'com.squareup.retrofit2:retrofit:2.9.0'
         implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
         implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
         implementation libs.converter.gson
         implementation libs.logging.interceptor
     ```

2. **Steps to Implement**
   
   - Clone this folder into your local development environment.
   
   - Set up the API client in `ApiClient.java`:
     ```java
     ApiClient apiClient = new ApiClient("YOUR_API_KEY");
     ```

   - Use `OpenRouterApi` to define endpoints and send requests:
     ```java
     CompletionRequest request = new CompletionRequest();
     request.setModel("meta-llama/llama-3.3-70b-instructfree");
     request.setTemperature(0.3f);
     request.setMaxTokens(500);

     OpenRouterApi api = ApiClient.getInstance("YOUR_API_KEY");
     Call call = api.createCompletion(request);
     ```

3. **Compile and Run the app and enter the prompt into the app interface**

    The LLM decides the function to be called and calls the respective function with the appropriate arguments. The working demo can be seen below.


---

## **Features**

1. **Offline Approach**:
   - Finetuned Gemma-2B model using LoRA for executing Android system commands offline.
   
2. **Online Approach**:
   - Integrated OpenRouter API with Meta’s Llama model for online tool calling.

3. **Device Management**:
   - Perform tasks such as toggling Wi-Fi/Bluetooth, setting alarms, adjusting brightness, sending SMS, and more.

4. **Modular Design**:
   - Reusable components for both offline and online methodologies.

---

## **Codebase**
The below file tree shows the most important files and their functionalities.

```
MIT_24OD09MIT_Automation_of_Android_Based_Actions_Using_Large_Action_Models/
│── 📄 README.md                 # Project documentation
│
├── 📂 Approach_1_Offline/       # Offline model fine-tuning
│   ├── 📝 convert.py            # Convert model to MediaPipe format
│   ├── 📜 fine.jsonl            # Training prompts & responses
│   ├── 🏋️‍♂️ FinetuneFinalVersion.py  # LoRA fine-tuning script
│
└── 📂 Approach_2_Online/        # Online model implementation
    └── 📂 OnlineLLMJlama/       
        ├── ⚙️ build.gradle       # Project build configuration
        │
        ├── 📂 app/               # Android app module
        │   ├── ⚙️ build.gradle   # App module build config
        │   ├── 📂 src/           # Source files
        │       ├── 📂 main/      
        │       │   ├── 📄 AndroidManifest.xml  # App permissions & components
        │       │   ├── 📂 java/com.example.onlinellmjlama/
        │       │   │   ├── 🚀 MainActivity.java    # Main activity
        │       │   │   ├── 📂 api/                # API handlers
        │       │   │   │   ├── 🔌 ApiClient.java   # Retrofit API setup
        │       │   │   │   ├── 🌐 OpenRouterApi.java  # API endpoints
        │       │   │   ├── 📂 models/             # Data models
        │       │   │   │   ├── 📄 CompletionRequest.java   # Request model
        │       │   │   │   ├── 📄 CompletionResponse.java  # Response model
        │       │   │   │   ├── 📄 FunctionCall.java        # Function call model
        │       │   │   │   ├── 📄 Message.java             # Message model
        │       │   │   ├── 📂 utils/              # Utility functions
        │       │   │   │   ├── ⏰ AlarmReceiver.java      # Handles alarms
        │       │   │   │   ├── 📱 DeviceActionManager.java # Manages device actions
        │       │   ├── 🎨 res/                 # App resources
        │       │   │   ├── 🖼️ layout/         # UI layout files
        │       │   │   │   ├── 📄 activity_main.xml  # Main screen layout

```
---

## **Future Enhancements**

1. Expand device management capabilities with additional APIs.
2. Add error handling and logging improvements.
3. Optimize the LoRA-adapted offline model for better performance on low-resource devices.

---

## References

- [MediaPipe LLM API Documentation](https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference)