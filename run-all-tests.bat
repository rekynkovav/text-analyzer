@echo off
echo ========================================
echo   Testing Text Analyzer - All Scenarios
echo ========================================
echo.

REM 1. Базовый запуск
echo [1/10] Basic analysis...
java -jar target\text-analyzer-1.0.1.jar --dir src\test\resources\test-texts --min-length 3 --top 10
echo.
pause

REM 2. Разные min-length
echo [2/10] Different min-length values...
java -jar target\text-analyzer-1.0.1.jar --dir src\test\resources\test-texts --min-length 2 --top 10
java -jar target\text-analyzer-1.0.1.jar --dir src\test\resources\test-texts --min-length 4 --top 10
java -jar target\text-analyzer-1.0.1.jar --dir src\test\resources\test-texts --min-length 6 --top 10
echo.
pause

REM 3. Разное количество топ-слов
echo [3/10] Different top counts...
java -jar target\text-analyzer-1.0.1.jar --dir src\test\resources\test-texts --min-length 3 --top 5
java -jar target\text-analyzer-1.0.1.jar --dir src\test\resources\test-texts --min-length 3 --top 20
echo.
pause

REM 4. Многопоточность
echo [4/10] Multi-threaded (4 threads)...
java -jar target\text-analyzer-1.0.1.jar --dir src\test\resources\test-texts --min-length 3 --top 10 --mode multi --threads 4
echo.
pause

REM 5. Однопоточный режим
echo [5/10] Single-threaded mode...
java -jar target\text-analyzer-1.0.1.jar --dir src\test\resources\test-texts --min-length 3 --top 10 --mode single
echo.
pause

REM 6. JSON вывод
echo [6/10] JSON output...
java -jar target\text-analyzer-1.0.1.jar --dir src\test\resources\test-texts --min-length 3 --top 10 --output test-result.json
echo Results saved to test-result.json
type test-result.json
echo.
pause

REM 7. Stop words
echo [7/10] With stop words...
if exist stopwords.txt (
    java -jar target\text-analyzer-1.0.1.jar --dir src\test\resources\test-texts --min-length 3 --top 10 --stopwords stopwords.txt
) else (
    echo Please create stopwords.txt first
)
echo.
pause

REM 8. Комбинированный
echo [8/10] Combined parameters...
java -jar target\text-analyzer-1.0.1.jar --dir src\test\resources\test-texts --min-length 4 --top 20 --mode multi --threads 6 --output complete.json
echo.
pause

REM 9. Справка
echo [9/10] Help...
java -jar target\text-analyzer-1.0.1.jar --help
echo.
pause

REM 10. Сравнение производительности
echo [10/10] Performance comparison...
echo Single-threaded:
java -jar target\text-analyzer-1.0.1.jar --dir src\test\resources\test-texts --min-length 3 --top 10 --mode single
echo.
echo Multi-threaded (4 threads):
java -jar target\text-analyzer-1.0.1.jar --dir src\test\resources\test-texts --min-length 3 --top 10 --mode multi --threads 4
echo.
echo Multi-threaded (8 threads):
java -jar target\text-analyzer-1.0.1.jar --dir src\test\resources\test-texts --min-length 3 --top 10 --mode multi --threads 8

echo.
echo All tests completed!
pause