package com.example.onlinellmjlama.models;

public class Message {
    private String role;
    private String content;
    private FunctionCall functionCall; // Optional field for function calls

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public Message(String role, String content, FunctionCall functionCall) {
        this.role = role;
        this.content = content;
        this.functionCall = functionCall;
    }

    // Getters and Setters
    public FunctionCall getFunctionCall() {
        return functionCall;
    }

    public void setFunctionCall(FunctionCall functionCall) {
        this.functionCall = functionCall;
    }
}