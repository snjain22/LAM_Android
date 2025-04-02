package com.example.onlinellmjlama.api;

import com.example.onlinellmjlama.models.CompletionRequest;
import com.example.onlinellmjlama.models.CompletionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenRouterApi {
    @Headers({
            "Content-Type: application/json",
            "HTTP-Referer: https://yourdomain.com"
    })
    @POST("chat/completions")
    Call<CompletionResponse> createCompletion(
            @Body CompletionRequest request
    );
}
