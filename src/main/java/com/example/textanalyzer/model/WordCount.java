package com.example.textanalyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WordCount {
    private String word;
    private int count;

    public WordCount() {
    }

    public WordCount(String word, int count) {
        this.word = word;
        this.count = count;
    }

    @JsonProperty("word")
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @JsonProperty("count")
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}