# Getting Started

This guide will help you set up the Document Matrix project on your local machine for development and testing.

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

### Required
- **Java 21** (Eclipse Temurin recommended)
- **SBT 1.11.0+** (Scala Build Tool)
- **Git** for version control

### Optional but Recommended
- **Docker** for containerized development
- **VS Code** with Metals extension for Scala development
- **IntelliJ IDEA** with Scala plugin

## 🔧 Installation

### 1. Clone the Repository
```bash
git clone https://github.com/Hostilian/devtask3mk.git
cd devtask3mk
```

### 2. Verify Java Installation
```bash
java -version
# Should show Java 21.x.x
```

### 3. Install SBT (if not already installed)

#### On Windows (using Chocolatey):
```powershell
choco install sbt
```

#### On macOS (using Homebrew):
```bash
brew install sbt
```

#### On Linux (using package manager):
```bash
# Ubuntu/Debian
sudo apt install sbt

# Fedora/CentOS
sudo dnf install sbt
```

### 4. Compile the Project
```bash
sbt compile
```

### 5. Run Tests
```bash
sbt test
```

## 🚀 Quick Start Commands

### Development Workflow
```bash
# Clean build
sbt clean compile

# Run all tests
sbt test

# Run specific test suite
sbt "testOnly com.example.DocumentSpec"

# Run with detailed output
sbt "test; testOnly * -- -oF"

# Start SBT console for interactive development
sbt console

# Package the application
sbt package
```

### Running the Server
```bash
# Start the HTTP server
sbt "runMain com.example.Server"

# Or use the convenience script
./dev.sh  # On Unix/macOS
./dev.bat # On Windows
```

The server will start on `http://localhost:8081`

### Using Docker
```bash
# Build Docker image
docker build -t document-matrix .

# Run with Docker Compose
docker-compose up

# Run in background
docker-compose up -d
```

## 🧪 Testing Your Setup

### 1. Basic Compilation Test
```bash
sbt compile
```
Should complete without errors.

### 2. Run Unit Tests
```bash
sbt "testOnly com.example.DocumentSpec"
```
All tests should pass.

### 3. Test HTTP Server
```bash
# Start the server
sbt "runMain com.example.Server"

# In another terminal, test the health endpoint
curl http://localhost:8081/health
# Should return: "Server is running"
```

### 4. Test Document API
```bash
# Test document rendering
curl -X POST http://localhost:8081/render \
  -H "Content-Type: application/json" \
  -d '{"type":"leaf","value":"Hello World"}'
```

## 🔍 Project Structure Overview

```
devtask3mk/
├── src/main/scala/           # Main source code
│   ├── Document.scala        # Core document ADT
│   ├── Server.scala         # HTTP server
│   └── Cli.scala           # CLI utilities
├── src/test/scala/          # Test suites
├── project/                 # SBT configuration
├── .github/workflows/       # CI/CD pipelines
├── docs/                   # Documentation
└── wiki/                   # This wiki
```

## 🛠️ IDE Setup

### VS Code with Metals
1. Install the [Metals extension](https://marketplace.visualstudio.com/items?itemName=scalameta.metals)
2. Open the project folder
3. Metals will automatically import the SBT build
4. Use `Ctrl+Shift+P` → "Metals: Import build" if needed

### IntelliJ IDEA
1. Install the Scala plugin
2. Open → Import Project → Select the project folder
3. Choose "Import project from external model" → SBT
4. Click "Finish"

## 📚 Next Steps

Once you have the project running:

1. **Explore the Code**: Start with `src/main/scala/Document.scala`
2. **Read the [API Documentation](API-Documentation)**
3. **Check out [Examples](Examples)** for common use cases
4. **Understand the [Architecture](Architecture-Overview)**
5. **Learn about [Testing Strategy](Testing-Strategy)**

## 🐛 Troubleshooting

### Common Issues

#### SBT Build Fails
```bash
# Clear SBT cache
rm -rf ~/.sbt
rm -rf target project/target

# Re-run compilation
sbt clean compile
```

#### Tests Fail
```bash
# Run tests with verbose output
sbt "testOnly * -- -oF"

# Run specific failing test
sbt "testOnly com.example.DocumentSpec -- -oF"
```

#### Server Won't Start
```bash
# Check if port 8081 is already in use
netstat -an | grep 8081

# Kill process using the port (Unix/macOS)
lsof -ti:8081 | xargs kill -9

# On Windows
netstat -ano | findstr 8081
taskkill /PID <PID> /F
```

### Performance Issues
```bash
# Increase SBT memory
export SBT_OPTS="-Xmx2G -XX:+UseG1GC"
sbt
```

## 📞 Getting Help

- **Issues**: Report bugs on [GitHub Issues](https://github.com/Hostilian/devtask3mk/issues)
- **Discussions**: Use [GitHub Discussions](https://github.com/Hostilian/devtask3mk/discussions)
- **Documentation**: Check other [Wiki pages](Home)
- **Examples**: See the [Examples page](Examples)

## 🚀 Ready to Code!

Now that you have everything set up, you're ready to:
- Explore the functional programming patterns
- Contribute to the project
- Build your own document processing applications

Happy coding! 🎉
