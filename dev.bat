@echo off
setlocal enabledelayedexpansion

:: Document Matrix Development Helper Script (Windows)
:: This script provides convenient commands for local development

:: Function to print colored output (basic Windows version)
set "GREEN=[92m"
set "YELLOW=[93m"
set "RED=[91m"
set "BLUE=[94m"
set "NC=[0m"

:: Function definitions using labels
goto :main

:log
echo %GREEN%[INFO]%NC% %~1
goto :eof

:warn
echo %YELLOW%[WARN]%NC% %~1
goto :eof

:error
echo %RED%[ERROR]%NC% %~1
goto :eof

:header
echo %BLUE%========================================%NC%
echo %BLUE%%~1%NC%
echo %BLUE%========================================%NC%
goto :eof

:check_prerequisites
call :header "Checking Prerequisites"

:: Check Docker
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    call :error "Docker is not installed or not in PATH"
    exit /b 1
)
call :log "✓ Docker found"

:: Check Docker Compose
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    call :warn "docker-compose not found, trying 'docker compose'"
    docker compose version >nul 2>&1
    if %errorlevel% neq 0 (
        call :error "Docker Compose is not available"
        exit /b 1
    )
    set "DOCKER_COMPOSE=docker compose"
) else (
    set "DOCKER_COMPOSE=docker-compose"
)
call :log "✓ Docker Compose found"

:: Check Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    call :warn "Java not found in PATH"
) else (
    call :log "✓ Java found"
)

:: Check SBT
sbt about >nul 2>&1
if %errorlevel% neq 0 (
    call :warn "SBT not found in PATH"
) else (
    call :log "✓ SBT found"
)

goto :eof

:cleanup
call :header "Cleaning Up Development Environment"

call :log "Stopping Docker containers..."
%DOCKER_COMPOSE% down --remove-orphans

call :log "Removing unused Docker resources..."
docker system prune -f

call :log "Cleaning SBT build artifacts..."
sbt clean >nul 2>&1
if %errorlevel% neq 0 (
    call :warn "Failed to clean SBT artifacts"
)

call :log "✓ Cleanup completed"
goto :eof

:setup
call :header "Setting Up Development Environment"

call :log "Building Docker images..."
docker build -t document-matrix .

call :log "Starting development services..."
%DOCKER_COMPOSE% --profile dev up -d

call :log "✓ Development environment ready"
call :log "Run 'dev.bat status' to check service status"
goto :eof

:test
call :header "Running Tests"

sbt test >nul 2>&1
if %errorlevel% neq 0 (
    call :log "Running tests in Docker..."
    docker run --rm -v "%cd%":/app -w /app sbtscala/scala-sbt:openjdk-21_1.10.3_3.4.3 sbt test
) else (
    call :log "Running SBT tests..."
    sbt test
)

call :log "✓ Tests completed"
goto :eof

:format
call :header "Formatting Code"

sbt scalafmtAll >nul 2>&1
if %errorlevel% neq 0 (
    call :log "Running scalafmt in Docker..."
    docker run --rm -v "%cd%":/app -w /app sbtscala/scala-sbt:openjdk-21_1.10.3_3.4.3 sbt scalafmtAll
) else (
    call :log "Running scalafmt..."
    sbt scalafmtAll
)

call :log "✓ Code formatted"
goto :eof

:status
call :header "Service Status"

call :log "Docker containers:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo.
call :log "Available services:"
echo   • Server: http://localhost:8081
echo   • Grafana: http://localhost:3000 (admin/admin)
echo   • Prometheus: http://localhost:9090
goto :eof

:logs
call :header "Service Logs"
if "%~2"=="" (
    %DOCKER_COMPOSE% logs -f
) else (
    %DOCKER_COMPOSE% logs -f %~2
)
goto :eof

:ci
call :header "Simulating CI Pipeline"

call :log "Step 1: Format check..."
call :format

call :log "Step 2: Compile..."
sbt compile >nul 2>&1
if %errorlevel% neq 0 (
    docker run --rm -v "%cd%":/app -w /app sbtscala/scala-sbt:openjdk-21_1.10.3_3.4.3 sbt compile
) else (
    sbt compile
)

call :log "Step 3: Run tests..."
call :test

call :log "Step 4: Build Docker image..."
docker build -t document-matrix-ci .

call :log "✓ CI simulation completed successfully"
goto :eof

:show_help
echo Document Matrix Development Helper (Windows)
echo.
echo Usage: %0 COMMAND [OPTIONS]
echo.
echo Commands:
echo   check      Check prerequisites
echo   setup      Setup development environment
echo   cleanup    Clean up development environment
echo   test       Run tests
echo   format     Format code
echo   status     Show service status
echo   logs       Show logs (optionally for specific service)
echo   ci         Simulate CI pipeline
echo   help       Show this help message
echo.
echo Examples:
echo   %0 setup              # Setup development environment
echo   %0 logs server         # Show server logs
echo   %0 ci                  # Run CI simulation
echo.
echo For more information, see PIPELINE_DOCUMENTATION.md
goto :eof

:main
:: Main script logic
if "%1"=="" goto :show_help
if "%1"=="help" goto :show_help
if "%1"=="--help" goto :show_help
if "%1"=="-h" goto :show_help

if "%1"=="check" (
    call :check_prerequisites
    goto :end
)

if "%1"=="setup" (
    call :check_prerequisites
    if %errorlevel% equ 0 call :setup
    goto :end
)

if "%1"=="cleanup" (
    call :cleanup
    goto :end
)

if "%1"=="test" (
    call :test
    goto :end
)

if "%1"=="format" (
    call :format
    goto :end
)

if "%1"=="status" (
    call :status
    goto :end
)

if "%1"=="logs" (
    call :logs %1 %2
    goto :end
)

if "%1"=="ci" (
    call :check_prerequisites
    if %errorlevel% equ 0 call :ci
    goto :end
)

call :error "Unknown command: %1"
call :show_help
exit /b 1

:end
endlocal
