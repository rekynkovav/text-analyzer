package com.example.textanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Text Analyzer tool.
 *
 * <p>This Spring Boot application analyzes text files in a directory,
 * counts word frequencies, and displays the most common words.
 *
 * <p>The application supports command-line arguments for configuration:
 * <ul>
 *   <li>--dir - Directory containing .txt files (required)</li>
 *   <li>--min-length - Minimum word length to include (required)</li>
 *   <li>--top - Number of top words to display (required)</li>
 *   <li>--output - Path to save JSON results (optional)</li>
 *   <li>--stopwords - Path to stop words file (optional)</li>
 *   <li>--help - Display help message (optional)</li>
 * </ul>
 *
 * @author Text Analyzer Team
 * @version 1.0
 */
@SpringBootApplication
public class TextAnalyzerApplication {

	/**
	 * The entry point of the Text Analyzer application.
	 *
	 * <p>Bootstraps the Spring Boot application, which triggers
	 * the {@link com.example.textanalyzer.runner.TextAnalysisRunner}
	 * to process command-line arguments and execute the analysis.
	 *
	 * @param args Command-line arguments for configuring the analysis
	 */
	public static void main(String[] args) {
		SpringApplication.run(TextAnalyzerApplication.class, args);
	}
}