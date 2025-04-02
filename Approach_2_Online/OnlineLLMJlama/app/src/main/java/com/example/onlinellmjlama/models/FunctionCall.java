package com.example.onlinellmjlama.models;

import com.google.gson.annotations.SerializedName;

public class FunctionCall {
    @SerializedName("name")
    private String name; // Name of the action/function

    @SerializedName("parameters")
    private String parameters; // JSON string of parameters

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
}
