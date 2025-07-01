@echo off
echo Document Matrix - Scala Project
echo.
echo Available commands:
echo   1. Compile project
echo   2. Run tests
echo   3. Run CLI
echo   4. Run Server
echo   5. Format code
echo.
set /p choice="Enter your choice (1-5): "

if "%choice%"=="1" (
    echo Compiling...
    sbt compile
) else if "%choice%"=="2" (
    echo Running tests...
    sbt test
) else if "%choice%"=="3" (
    echo Starting CLI...
    sbt "runMain com.example.Cli"
) else if "%choice%"=="4" (
    echo Starting server on http://localhost:8080...
    sbt "runMain com.example.Server"
) else if "%choice%"=="5" (
    echo Formatting code...
    sbt scalafmtAll
) else (
    echo Invalid choice. Please run the script again.
)

pause
