package com.example.textanalyzer;

import com.example.textanalyzer.model.AnalysisResult;
import com.example.textanalyzer.model.WordCount;
import com.example.textanalyzer.processor.WordProcessor;
import com.example.textanalyzer.runner.TextAnalysisRunner;
import com.example.textanalyzer.service.StopWordsService;
import com.example.textanalyzer.service.TextAnalysisService;
import com.example.textanalyzer.writer.ConsoleWriter;
import com.example.textanalyzer.writer.JsonFileWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
		"spring.main.web-application-type=none",
		"spring.main.banner-mode=off"
})
class TextAnalyzerApplicationTests {

	@MockitoBean
	private TextAnalysisRunner textAnalysisRunner;

	@TempDir
	Path tempDir;

	@Autowired
	private TextAnalysisService textAnalysisService;

	@Autowired
	private StopWordsService stopWordsService;

	@Autowired
	private WordProcessor wordProcessor;

	@Autowired
	private ConsoleWriter consoleWriter;

	@Autowired
	private JsonFileWriter jsonFileWriter;

	private Path testTextsDir;
	private Path stopwordsFile;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() throws IOException {
		objectMapper = new ObjectMapper();
		testTextsDir = tempDir.resolve("texts");
		Files.createDirectories(testTextsDir);

		createTestFile("file1.txt",
				"Development is a continuous process of learning and improvement.\n" +
				"The development process requires careful planning and execution.\n" +
				"Software development involves coding, testing, and deployment.\n" +
				"Development teams work together to achieve common goals."
		);

		createTestFile("file2.txt",
				"Process engineering is important for manufacturing.\n" +
				"The engineering process includes design, testing, and quality assurance.\n" +
				"Process improvement leads to better engineering outcomes.\n" +
				"Engineering and development work together in product development."
		);

		createTestFile("empty.txt", "");

		Path subDir = testTextsDir.resolve("subdir");
		Files.createDirectories(subDir);
		createTestFileInPath(subDir, "nested.txt",
				"Nested file for testing recursive directory traversal.\n" +
				"This file should also be processed by the analyzer."
		);

		stopwordsFile = tempDir.resolve("stopwords.txt");
		List<String> stopwords = Arrays.asList(
				"the", "a", "an", "and", "of", "to", "for", "in", "on", "at",
				"by", "with", "without", "is", "are", "was", "were", "be", "been",
				"being", "have", "has", "had", "having"
		);
		Files.write(stopwordsFile, stopwords);
	}

	private void createTestFile(String filename, String content) throws IOException {
		createTestFileInPath(testTextsDir, filename, content);
	}

	private void createTestFileInPath(Path path, String filename, String content) throws IOException {
		Path file = path.resolve(filename);
		Files.writeString(file, content);
	}

	// ==================== ТЕСТ 1: Проверка обязательных параметров ====================

	@Test
	void testRequiredParameters_AllPresent_ShouldSucceed() {
		assertDoesNotThrow(() -> {
			AnalysisResult result = textAnalysisService.analyzeDirectory(
					testTextsDir.toString(), 5, 10, Collections.emptySet()
			);
			assertNotNull(result);
		});
	}

	@Test
	void testRequiredParameters_DirMissing_ShouldHandleGracefully() {
		String nonExistentDir = tempDir.resolve("nonexistent").toString();

		AnalysisResult result = textAnalysisService.analyzeDirectory(
				nonExistentDir, 5, 10, Collections.emptySet()
		);

		assertNotNull(result);
		assertFalse(result.getErrors().isEmpty());
		assertTrue(result.getErrors().stream()
				.anyMatch(e -> e.getMessage().contains("Directory does not exist")));
	}

	// ==================== ТЕСТ 2: Проверка подсчёта слов ====================

	@Test
	void testWordCounting_BasicFunctionality() {
		AnalysisResult result = textAnalysisService.analyzeDirectory(
				testTextsDir.toString(), 5, 10, Collections.emptySet()
		);

		assertNotNull(result);
		assertFalse(result.getWords().isEmpty());

		Optional<WordCount> development = result.getWords().stream()
				.filter(wc -> wc.getWord().equals("development"))
				.findFirst();

		assertTrue(development.isPresent());
		assertEquals(6, development.get().getCount());

		Optional<WordCount> process = result.getWords().stream()
				.filter(wc -> wc.getWord().equals("process"))
				.findFirst();

		assertTrue(process.isPresent());
		assertEquals(5, process.get().getCount());
	}

	@Test
	void testWordCounting_WithMinLength() {
		AnalysisResult result = textAnalysisService.analyzeDirectory(
				testTextsDir.toString(), 6, 10, Collections.emptySet()
		);

		assertTrue(result.getWords().stream()
				.allMatch(wc -> wc.getWord().length() >= 6));
	}

	@Test
	void testWordCounting_WithMinLengthZero() {
		AnalysisResult result = textAnalysisService.analyzeDirectory(
				testTextsDir.toString(), 0, 20, Collections.emptySet()
		);

		assertFalse(result.getWords().isEmpty());
	}

	// ==================== ТЕСТ 3: Проверка игнорирования регистра ====================

	@Test
	void testCaseInsensitivity() throws IOException {
		Path caseTestFile = testTextsDir.resolve("case-test.txt");
		Files.writeString(caseTestFile, "Test TEST test TeSt");

		AnalysisResult result = textAnalysisService.analyzeDirectory(
				testTextsDir.toString(), 1, 10, Collections.emptySet()
		);

		Optional<WordCount> test = result.getWords().stream()
				.filter(wc -> wc.getWord().equals("test"))
				.findFirst();

		assertTrue(test.isPresent());
		assertEquals(4, test.get().getCount());

		Files.delete(caseTestFile);
	}

	// ==================== ТЕСТ 4: Проверка игнорирования пунктуации ====================

	@Test
	void testPunctuationIgnored() throws IOException {
		Path punctTestFile = testTextsDir.resolve("punct-test.txt");
		Files.writeString(punctTestFile,
				"Hello, hello! hello? hello; hello: 'hello' (hello) [hello] {hello} <hello>");

		AnalysisResult result = textAnalysisService.analyzeDirectory(
				testTextsDir.toString(), 1, 10, Collections.emptySet()
		);

		Optional<WordCount> hello = result.getWords().stream()
				.filter(wc -> wc.getWord().equals("hello"))
				.findFirst();

		assertTrue(hello.isPresent());
		assertTrue(hello.get().getCount() >= 10, "Should count at least 10 occurrences");

		Files.delete(punctTestFile);
	}

	@Test
	void testApostropheHandling() throws IOException {
		Path apostropheFile = testTextsDir.resolve("apostrophe-test.txt");
		Files.writeString(apostropheFile,
				"don't isn't aren't wasn't haven't doesn't");

		AnalysisResult result = textAnalysisService.analyzeDirectory(
				testTextsDir.toString(), 1, 20, Collections.emptySet()
		);

		boolean hasWords = result.getWords().stream()
				.anyMatch(wc -> wc.getWord().contains("don") ||
								wc.getWord().contains("isn") ||
								wc.getWord().equals("don't"));

		assertTrue(hasWords, "Words with apostrophes should be recognized");

		Files.delete(apostropheFile);
	}

	// ==================== ТЕСТ 5: Проверка стоп-слов ====================

	@Test
	void testStopWordsFiltering() {
		Set<String> stopWords = stopWordsService.loadStopWords(stopwordsFile.toString());

		AnalysisResult result = textAnalysisService.analyzeDirectory(
				testTextsDir.toString(), 1, 50, stopWords
		);

		Set<String> stopWordsSet = new HashSet<>(stopWords);
		assertTrue(result.getWords().stream()
				.noneMatch(wc -> stopWordsSet.contains(wc.getWord())));
	}

	@Test
	void testStopWordsLoading_FromFile() {
		Set<String> stopWords = stopWordsService.loadStopWords(stopwordsFile.toString());

		assertFalse(stopWords.isEmpty());
		assertTrue(stopWords.contains("the"));
		assertTrue(stopWords.contains("and"));
		assertEquals(24, stopWords.size());
	}

	@Test
	void testStopWordsLoading_NonExistentFile() {
		Set<String> stopWords = stopWordsService.loadStopWords("/nonexistent/file.txt");

		assertNotNull(stopWords);
		assertTrue(stopWords.isEmpty());
	}

	// ==================== ТЕСТ 6: Проверка обработки пустых файлов ====================

	@Test
	void testEmptyFileHandling() {
		AnalysisResult result = textAnalysisService.analyzeDirectory(
				testTextsDir.toString(), 1, 10, Collections.emptySet()
		);

		assertNotNull(result);
		boolean hasEmptyFileError = result.getErrors().stream()
				.anyMatch(e -> e.getFile().contains("empty.txt"));
		assertFalse(hasEmptyFileError);
	}

	// ==================== ТЕСТ 7: Проверка рекурсивного обхода директорий ====================

	@Test
	void testRecursiveDirectoryTraversal() {
		AnalysisResult result = textAnalysisService.analyzeDirectory(
				testTextsDir.toString(), 1, 50, Collections.emptySet()
		);

		boolean hasNestedWord = result.getWords().stream()
				.anyMatch(wc -> wc.getWord().equals("nested") ||
								wc.getWord().equals("traversal") ||
								wc.getWord().equals("analyzer"));

		assertTrue(hasNestedWord, "Nested file content should be processed");
	}

	// ==================== ТЕСТ 8: Проверка фильтрации по расширению ====================

	@Test
	void testOnlyTxtFilesProcessed() throws IOException {
		Path nonTxtFile = testTextsDir.resolve("not-a-text.log");
		Files.writeString(nonTxtFile, "This should not be processed");

		AnalysisResult result = textAnalysisService.analyzeDirectory(
				testTextsDir.toString(), 1, 10, Collections.emptySet()
		);

		boolean hasProcessed = result.getWords().stream()
				.anyMatch(wc -> wc.getWord().equals("processed"));
		assertFalse(hasProcessed);

		Files.delete(nonTxtFile);
	}

	// ==================== ТЕСТ 9: Проверка регулярных выражений ====================

	@Test
	void testRegexPattern_ValidWords() {
		String text = "Hello, world! This is a test-string with hyphenated-word.";
		Matcher matcher = wordProcessor.getWordMatcher(text);

		List<String> foundWords = new ArrayList<>();
		while (matcher.find()) {
			foundWords.add(matcher.group());
		}

		assertTrue(foundWords.contains("Hello"));
		assertTrue(foundWords.contains("world"));
		assertTrue(foundWords.contains("This"));

		boolean hasTestString = foundWords.contains("test-string") ||
								(foundWords.contains("test") && foundWords.contains("string"));
		assertTrue(hasTestString, "Hyphenated words should be handled");
	}

	@Test
	void testRegexPattern_Unicode() {
		String text = "Привет мир Hello world";
		Matcher matcher = wordProcessor.getWordMatcher(text);

		List<String> foundWords = new ArrayList<>();
		while (matcher.find()) {
			foundWords.add(matcher.group());
		}

		assertTrue(foundWords.contains("Hello") || foundWords.contains("world"));
	}

	// ==================== ТЕСТ 10: Проверка топ-N слов ====================

	@Test
	void testTopNWords_LimitRespected() {
		AnalysisResult result = textAnalysisService.analyzeDirectory(
				testTextsDir.toString(), 1, 3, Collections.emptySet()
		);

		assertTrue(result.getWords().size() <= 3);
	}

	@Test
	void testTopNWords_OrderedByFrequency() {
		AnalysisResult result = textAnalysisService.analyzeDirectory(
				testTextsDir.toString(), 1, 10, Collections.emptySet()
		);

		List<WordCount> words = result.getWords();
		for (int i = 0; i < words.size() - 1; i++) {
			assertTrue(words.get(i).getCount() >= words.get(i + 1).getCount());
		}
	}

	// ==================== ТЕСТ 11: Проверка обработки ошибок ====================

	@Test
	void testErrorHandling_InvalidDirectory() {
		AnalysisResult result = textAnalysisService.analyzeDirectory(
				"/invalid/path/that/does/not/exist", 1, 10, Collections.emptySet()
		);

		assertFalse(result.getErrors().isEmpty());
		assertTrue(result.getWords().isEmpty());
	}

	@Test
	void testErrorHandling_DirectoryWithNoTxtFiles() throws IOException {
		Path emptyDir = tempDir.resolve("empty-dir");
		Files.createDirectories(emptyDir);

		AnalysisResult result = textAnalysisService.analyzeDirectory(
				emptyDir.toString(), 1, 10, Collections.emptySet()
		);

		assertNotNull(result);
		assertTrue(result.getWords().isEmpty());
	}

	// ==================== ТЕСТ 12: Проверка WordProcessor ====================

	@Test
	void testWordProcessor_IsValidWord() {
		assertTrue(wordProcessor.isValidWord("hello", 1));
		assertTrue(wordProcessor.isValidWord("hello", 5));
		assertFalse(wordProcessor.isValidWord("hi", 3));
		assertFalse(wordProcessor.isValidWord(null, 1));
		assertFalse(wordProcessor.isValidWord("", 1));
	}

	@Test
	void testWordProcessor_NormalizeWord() {
		assertEquals("hello", wordProcessor.normalizeWord("Hello"));
		assertEquals("hello", wordProcessor.normalizeWord("HELLO"));
		assertEquals("don't", wordProcessor.normalizeWord("Don'T"));
	}

	// ==================== ТЕСТ 13: Проверка JSON вывода ====================

	@Test
	void testJsonOutput_Structure() throws IOException {
		Path outputFile = tempDir.resolve("output.json");
		AnalysisResult result = textAnalysisService.analyzeDirectory(
				testTextsDir.toString(), 5, 5, Collections.emptySet()
		);

		jsonFileWriter.write(result, outputFile.toString());

		assertTrue(Files.exists(outputFile));
		String jsonContent = Files.readString(outputFile);

		assertTrue(jsonContent.contains("analysisInfo"));
		assertTrue(jsonContent.contains("directory"));
		assertTrue(jsonContent.contains("minWordLength"));
		assertTrue(jsonContent.contains("words"));
		assertTrue(jsonContent.contains("word"));
		assertTrue(jsonContent.contains("count"));
		assertTrue(jsonContent.contains("errors"));
	}

	@Test
	void testJsonOutput_Content() throws IOException {
		Path outputFile = tempDir.resolve("result.json");
		AnalysisResult result = textAnalysisService.analyzeDirectory(
				testTextsDir.toString(), 5, 3, Collections.emptySet()
		);

		jsonFileWriter.write(result, outputFile.toString());

		assertTrue(Files.exists(outputFile), "JSON file should be created");
		assertTrue(Files.size(outputFile) > 0, "JSON file should not be empty");

		String jsonContent = Files.readString(outputFile);

		assertTrue(jsonContent.contains("analysisInfo"), "Should contain analysisInfo");
		assertTrue(jsonContent.contains("words"), "Should contain words");
		assertTrue(jsonContent.contains("errors"), "Should contain errors");

		assertDoesNotThrow(() -> objectMapper.readTree(jsonContent), "Should be valid JSON");
	}

	// ==================== ТЕСТ 14: Проверка консольного вывода ====================

	@Test
	void testConsoleOutput_Format() {
		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outContent));

		AnalysisResult result = textAnalysisService.analyzeDirectory(
				testTextsDir.toString(), 5, 3, Collections.emptySet()
		);

		consoleWriter.write(result);

		String output = outContent.toString();
		assertTrue(output.contains("Top") || output.contains("most frequent"));

		System.setOut(System.out);
	}

	@Test
	void testConsoleOutput_NoWordsFound() throws IOException {
		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outContent));

		Path emptyDir = tempDir.resolve("empty-dir-2");
		Files.createDirectories(emptyDir);

		AnalysisResult result = textAnalysisService.analyzeDirectory(
				emptyDir.toString(), 100, 5, Collections.emptySet()
		);

		consoleWriter.write(result);

		String output = outContent.toString();
		assertTrue(output.contains("No words found"));

		System.setOut(System.out);
	}

	// ==================== ТЕСТ 15: Интеграционный тест всех требований ====================

	@Test
	void testCompleteFlow_AllFeatures() throws IOException {
		Path outputFile = tempDir.resolve("complete-output.json");
		Set<String> stopWords = stopWordsService.loadStopWords(stopwordsFile.toString());

		AnalysisResult result = textAnalysisService.analyzeDirectory(
				testTextsDir.toString(), 5, 5, stopWords
		);

		assertNotNull(result);

		assertTrue(result.getWords().stream()
				.allMatch(wc -> wc.getWord().length() >= 5));

		assertTrue(result.getWords().size() <= 5);

		assertTrue(result.getWords().stream()
				.noneMatch(wc -> stopWords.contains(wc.getWord())));

		assertTrue(result.getWords().stream()
				.allMatch(wc -> wc.getWord().equals(wc.getWord().toLowerCase())));

		jsonFileWriter.write(result, outputFile.toString());
		assertTrue(Files.exists(outputFile));

		assertNotNull(result.getErrors());

		assertTrue(result.getWords().stream()
				.allMatch(wc -> wc.getCount() > 0));

		assertTrue(result.getAnalysisInfo().containsKey("directory"));
		assertTrue(result.getAnalysisInfo().containsKey("minWordLength"));
		assertTrue(result.getAnalysisInfo().containsKey("topCount"));
	}

	// ==================== ТЕСТ 16: Производительность ====================

	@Test
	void testLargeFileProcessing() throws IOException {
		Path largeFile = testTextsDir.resolve("large.txt");
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			content.append("word").append(i % 50).append(" ");
		}
		Files.writeString(largeFile, content.toString());

		long startTime = System.currentTimeMillis();
		AnalysisResult result = textAnalysisService.analyzeDirectory(
				testTextsDir.toString(), 1, 100, Collections.emptySet()
		);
		long endTime = System.currentTimeMillis();

		assertTrue((endTime - startTime) < 5000);
		assertFalse(result.getWords().isEmpty());

		Files.delete(largeFile);
	}
}