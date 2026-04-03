package com.example.textanalyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorInfo {
    private String file;
    private String message;

    public ErrorInfo() {
    }

    public ErrorInfo(String file, String message) {
        this.file = file;
        this.message = message;
    }

    @JsonProperty("file")
    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}