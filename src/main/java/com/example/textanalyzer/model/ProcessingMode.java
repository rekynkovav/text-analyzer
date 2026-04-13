package com.example.textanalyzer.model;

/**
 * Processing mode for text analysis.
 *
 * <p>SINGLE mode processes files sequentially in a single thread.
 * MULTI mode processes files in parallel using a thread pool.
 *
 * @author Text Analyzer Team
 * @version 2.0
 */
public enum ProcessingMode {
    SINGLE("single"),
    MULTI("multi");

    private final String value;

    ProcessingMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Converts a string to ProcessingMode.
     * Defaults to MULTI if the string is null or invalid.
     *
     * @param mode The mode string ("single" or "multi")
     * @return Corresponding ProcessingMode
     */
    public static ProcessingMode fromString(String mode) {
        if (mode == null) return MULTI;
        if (mode.equalsIgnoreCase("single")) return SINGLE;
        if (mode.equalsIgnoreCase("multi")) return MULTI;
        return MULTI;
    }
}