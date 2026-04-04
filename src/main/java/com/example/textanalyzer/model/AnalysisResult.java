package com.example.textanalyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Represents the complete analysis result containing word frequencies,
 * analysis metadata, and any errors encountered during processing.
 *
 * <p>This class encapsulates all output data from the text analysis,
 * including the top words by frequency, analysis configuration parameters,
 * and error information for failed file operations.
 *
 * @author Text Analyzer Team
 * @version 2.0
 */
public class AnalysisResult {
    private Map<String, Object> analysisInfo;
    private List<WordCount> words;
    private List<ErrorInfo> errors;
    private String mode;
    private Integer threads;
    private Integer processedFiles;
    private Long executionTimeMs;

    /**
     * Default constructor required for JSON deserialization.
     */
    public AnalysisResult() {
    }

    /**
     * Constructs a complete analysis result with all components.
     *
     * @param analysisInfo Map containing analysis metadata (directory, minLength, topCount)
     * @param words List of word frequency objects, sorted descending by count
     * @param errors List of errors encountered during file processing
     */
    public AnalysisResult(Map<String, Object> analysisInfo,
                          List<WordCount> words,
                          List<ErrorInfo> errors) {
        this.analysisInfo = analysisInfo;
        this.words = words;
        this.errors = errors;
    }

    /**
     * Constructs a complete analysis result with multi-threading metadata.
     *
     * @param analysisInfo Map containing analysis metadata
     * @param words List of word frequency objects
     * @param errors List of errors encountered
     * @param mode Processing mode ("single" or "multi")
     * @param threads Number of threads used (1 for single mode)
     * @param processedFiles Number of successfully processed files
     * @param executionTimeMs Total execution time in milliseconds
     */
    public AnalysisResult(Map<String, Object> analysisInfo,
                          List<WordCount> words,
                          List<ErrorInfo> errors,
                          String mode,
                          Integer threads,
                          Integer processedFiles,
                          Long executionTimeMs) {
        this.analysisInfo = analysisInfo;
        this.words = words;
        this.errors = errors;
        this.mode = mode;
        this.threads = threads;
        this.processedFiles = processedFiles;
        this.executionTimeMs = executionTimeMs;
    }

    /**
     * Returns the analysis metadata.
     *
     * @return Map containing configuration parameters like directory path,
     *         minimum word length, and top count limit
     */
    @JsonProperty("analysisInfo")
    public Map<String, Object> getAnalysisInfo() {
        return analysisInfo;
    }

    /**
     * Sets the analysis metadata.
     *
     * @param analysisInfo Map containing analysis configuration
     */
    public void setAnalysisInfo(Map<String, Object> analysisInfo) {
        this.analysisInfo = analysisInfo;
    }

    /**
     * Returns the list of top words with their frequencies.
     *
     * @return List of WordCount objects in descending frequency order
     */
    @JsonProperty("words")
    public List<WordCount> getWords() {
        return words;
    }

    /**
     * Sets the list of top words.
     *
     * @param words List of WordCount objects
     */
    public void setWords(List<WordCount> words) {
        this.words = words;
    }

    /**
     * Returns the list of processing errors.
     *
     * @return List of ErrorInfo objects detailing file processing failures
     */
    @JsonProperty("errors")
    public List<ErrorInfo> getErrors() {
        return errors;
    }

    /**
     * Sets the list of processing errors.
     *
     * @param errors List of ErrorInfo objects
     */
    public void setErrors(List<ErrorInfo> errors) {
        this.errors = errors;
    }

    /**
     * Returns the processing mode.
     *
     * @return "single" or "multi"
     */
    @JsonProperty("mode")
    public String getMode() {
        return mode;
    }

    /**
     * Sets the processing mode.
     *
     * @param mode "single" or "multi"
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * Returns the number of threads used.
     *
     * @return Number of threads (1 for single mode)
     */
    @JsonProperty("threads")
    public Integer getThreads() {
        return threads;
    }

    /**
     * Sets the number of threads used.
     *
     * @param threads Number of threads
     */
    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    /**
     * Returns the number of successfully processed files.
     *
     * @return Count of processed files
     */
    @JsonProperty("processedFiles")
    public Integer getProcessedFiles() {
        return processedFiles;
    }

    /**
     * Sets the number of successfully processed files.
     *
     * @param processedFiles Count of processed files
     */
    public void setProcessedFiles(Integer processedFiles) {
        this.processedFiles = processedFiles;
    }

    /**
     * Returns the total execution time.
     *
     * @return Execution time in milliseconds
     */
    @JsonProperty("executionTimeMs")
    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    /**
     * Sets the total execution time.
     *
     * @param executionTimeMs Execution time in milliseconds
     */
    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
}