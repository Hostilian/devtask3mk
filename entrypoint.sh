#!/bin/sh
set -e

# Default values
MODE=${MODE:-cli}
JAVA_OPTS=${JAVA_OPTS:-"-Xmx512m -Xms256m"}

# Create log directory
mkdir -p /app/logs

# Function to show help
show_help() {
  cat << EOF
Document Matrix Application

Usage: $0 [OPTIONS]

Environment Variables:
  MODE        Application mode: 'server' or 'cli' (default: cli)
  JAVA_OPTS   JVM options (default: -Xmx512m -Xms256m)

Options:
  --help      Show this help message
  --version   Show application version

Examples:
  MODE=server $0     # Start as server
  MODE=cli $0        # Start as CLI (default)
EOF
}

# Function to show version
show_version() {
  echo "Document Matrix v1.0.0"
  echo "Built with Scala 3.4.3"
}

# Parse command line arguments
case "${1:-}" in
  --help|-h)
    show_help
    exit 0
    ;;
  --version|-v)
    show_version
    exit 0
    ;;
esac

# Log startup information
echo "========================================"
echo "Document Matrix Application Starting"
echo "========================================"
echo "Mode: $MODE"
echo "Java Options: $JAVA_OPTS"
echo "Timestamp: $(date)"
echo "========================================"

if [ "$MODE" = "server" ]; then
  echo "Starting Document Matrix Server on port 8081..."
  echo "Health check endpoint: http://localhost:8081/health"
  exec java $JAVA_OPTS -cp app.jar com.example.Server 2>&1 | tee /app/logs/server.log
else
  echo "Starting Document Matrix CLI..."
  echo "Type 'help' for available commands"
  exec java $JAVA_OPTS -cp app.jar com.example.Cli 2>&1 | tee /app/logs/cli.log
fi
