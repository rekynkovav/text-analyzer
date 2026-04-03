package com.example.textanalyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class AnalysisResult {
    private Map<String, Object> analysisInfo;
    private List<WordCount> words;
    private List<ErrorInfo> errors;

    public AnalysisResult() {
    }

    public AnalysisResult(Map<String, Object> analysisInfo,
                          List<WordCount> words,
                          List<ErrorInfo> errors) {
        this.analysisInfo = analysisInfo;
        this.words = words;
        this.errors = errors;
    }

    @JsonProperty("analysisInfo")
    public Map<String, Object> getAnalysisInfo() {
        return analysisInfo;
    }

    public void setAnalysisInfo(Map<String, Object> analysisInfo) {
        this.analysisInfo = analysisInfo;
    }

    @JsonProperty("words")
    public List<WordCount> getWords() {
        return words;
    }

    public void setWords(List<WordCount> words) {
        this.words = words;
    }

    @JsonProperty("errors")
    public List<ErrorInfo> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorInfo> errors) {
        this.errors = errors;
    }
}