@echo off
echo.
echo ================================================================
echo                    DOCUMENT MATRIX DEMO
echo                    Advanced Scala 3 Project  
echo ================================================================
echo.

echo [1/6] Building project...
sbt clean compile
if errorlevel 1 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

echo.
echo [2/6] Running comprehensive test suite...
sbt test
if errorlevel 1 (
    echo ERROR: Tests failed!
    pause
    exit /b 1
)

echo.
echo [3/6] Demonstrating CLI with example document...
echo.
echo Sample JSON document:
type example.json
echo.
echo Pretty-printed output:
echo {"type":"leaf","value":"Hello from CLI!"} | sbt "runMain com.example.Cli"

echo.
echo [4/6] Starting HTTP server in background...
start /B sbt "runMain com.example.Server"
timeout /t 3 /nobreak >nul

echo.
echo [5/6] Testing HTTP API endpoints...
echo.
echo Testing health endpoint:
curl -s http://localhost:8080/health
echo.
echo.
echo Testing render endpoint:
curl -s -X POST http://localhost:8080/render ^
  -H "Content-Type: application/json" ^
  -d "{\"type\":\"leaf\",\"value\":\"Hello from API!\"}"
echo.

echo.
echo [6/6] Building Docker image...
docker build -t document-matrix . 2>nul
if errorlevel 1 (
    echo Docker not available or build failed - skipping Docker demo
) else (
    echo Docker image built successfully!
    echo You can run: docker run -p 8080:8080 document-matrix
)

echo.
echo ================================================================
echo                        DEMO COMPLETE!
echo ================================================================
echo.
echo Key features demonstrated:
echo  ✓ Algebraic Data Types (ADTs) with sealed traits
echo  ✓ Functional programming with Cats and ZIO
echo  ✓ Type-safe JSON serialization with Circe  
echo  ✓ Interactive CLI with pretty printing
echo  ✓ HTTP4s REST API with multiple endpoints
echo  ✓ Comprehensive testing with property-based tests
echo  ✓ Docker containerization support
echo  ✓ Modern Scala 3 language features
echo.
echo Architecture highlights:
echo  ✓ Higher-kinded types: Document[A]
echo  ✓ Recursion schemes (catamorphism/fold)
echo  ✓ Type classes: Functor, Traversable, Monoid
echo  ✓ Effect systems with ZIO
echo  ✓ Optics for deep immutable updates
echo.
pause
