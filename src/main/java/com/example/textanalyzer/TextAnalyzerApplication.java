package com.example.textanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Text Analyzer REST service.
 *
 * <p>This Spring Boot application provides REST API endpoints for analyzing
 * text files, counting word frequencies, and displaying the most common words.
 *
 * <p>Key features:
 * <ul>
 *   <li>REST API for text analysis (POST /api/analyze)</li>
 *   <li>Retrieve analysis results (GET /api/results/{id})</li>
 *   <li>List all analyses (GET /api/results)</li>
 *   <li>Asynchronous processing with status tracking</li>
 *   <li>Database persistence with H2</li>
 *   <li>Spring Security authentication</li>
 *   <li>Audit logging of user actions</li>
 * </ul>
 *
 * @author Text Analyzer Team
 * @version 2.0
 */
@SpringBootApplication
public class TextAnalyzerApplication {

	/**
	 * The entry point of the Text Analyzer REST service.
	 *
	 * @param args Command-line arguments (not used in REST mode)
	 */
	public static void main(String[] args) {
		SpringApplication.run(TextAnalyzerApplication.class, args);
	}
}