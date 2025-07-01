# Document Matrix

[![CI](https://github.com/your-username/devtask3mk/workflows/CI/badge.svg)](https://github.com/your-username/devtask3mk/actions)
[![Scala Version](https://img.shields.io/badge/scala-3.4.3-red.svg)](https://scala-lang.org/)
[![ZIO Version](https://img.shields.io/badge/zio-2.1.11-blue.svg)](https://zio.dev/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

A production-ready Scala 3 project implementing a type-safe, functional document data structure with CLI and HTTP API. Showcases advanced functional programming concepts, recursion schemes, and modern Scala ecosystem.

## Features

- **Algebraic Data Types (ADT)**: Type-safe document structure using sealed traits
- **Functional Programming**: Leverages Cats and ZIO for pure functional programming
- **Recursion Schemes**: Catamorphism support for document traversal and transformation
- **JSON Serialization**: Type-safe JSON encoding/decoding with Circe
- **CLI Interface**: Interactive command-line tool for document manipulation
- **HTTP API**: REST endpoints for document rendering and validation
- **Property-based Testing**: Comprehensive test suite with ScalaTest and ZIO Test

## Prerequisites

- Java 21
- SBT (Scala Build Tool)
- Scala 3.4.3 (managed by SBT)

## Quick Start

For Windows users, you can use the provided batch script:
```bash
run.bat
```

Or use SBT directly:
```bash
sbt compile  # Compile the project
sbt test     # Run tests
sbt "runMain com.example.Cli"     # Run CLI
sbt "runMain com.example.Server"  # Run HTTP server
```

## Setup

1. Clone this repository:
   ```bash
   git clone <repository-url>
   cd devtask3mk
   ```

2. Compile the project:
   ```bash
   sbt compile
   ```

3. Run tests:
   ```bash
   sbt test
   ```

## Usage

### CLI Interface

Run the interactive CLI:
```bash
sbt "runMain com.example.Cli"
```

Enter JSON documents to see them pretty-printed. Example JSON:
```json
{
  "type": "vertical",
  "cells": [
    {"type": "leaf", "value": "Hello"},
    {
      "type": "horizontal", 
      "cells": [
        {"type": "leaf", "value": "World"},
        {"type": "leaf", "value": "!"}
      ]
    }
  ]
}
```

### HTTP API

Start the server:
```bash
sbt "runMain com.example.Server"
```

Available endpoints:
- `POST /render` - Render a document as pretty-printed text
- `POST /validate` - Validate a document structure
- `GET /health` - Health check endpoint

Example request:
```bash
curl -X POST http://localhost:8080/render \
  -H "Content-Type: application/json" \
  -d '{"type":"leaf","value":"Hello World"}'
```

## Architecture

### Document ADT

The core `Document[A]` type represents a tree structure with four variants:
- `Leaf[A]`: Contains a value of type A
- `Horizontal[A]`: Horizontal layout of documents
- `Vertical[A]`: Vertical layout of documents  
- `Empty`: Empty document

### Type Classes

The implementation provides several type class instances:
- **Functor**: `map` operation for transforming values
- **Traversable**: `traverse` for effectful computations
- **Semigroup/Monoid**: Combining documents with associative operations
- **Encoder/Decoder**: JSON serialization support

### Recursion Schemes

The `fold` function implements a catamorphism, allowing you to:
- Process leaf values with function `f: A => B`
- Handle horizontal layouts with function `g: List[B] => B`
- Handle vertical layouts with function `h: List[B] => B`

## Testing

Run the test suite:
```bash
sbt test
```

The tests verify:
- Functor laws (identity, composition)
- Monoid laws (associativity, identity)
- Traversal with different effect types (Option, ZIO)
- JSON serialization round-trips
- Pretty printing functionality

## Docker

Build and run with Docker:
```bash
docker build -t document-matrix .
docker run -p 8080:8080 document-matrix
```

## Contributing

See `docs/CONTRIBUTING.md` for development guidelines.

## 🎯 **WOW FACTORS & ADVANCED FEATURES**

### **Functional Programming Excellence**
- **Higher-Kinded Types**: `Document[A]` supports any content type
- **Recursion Schemes**: Catamorphism for structured data processing  
- **Type Classes**: Functor, Traversable, Semigroup, Monoid instances
- **Effect Systems**: ZIO for pure functional programming
- **Optics/Lenses**: Deep immutable updates with Monocle

### **Modern Scala 3 Features**
- **Union Types**: For advanced type safety
- **Given/Using**: Modern type class syntax
- **Extension Methods**: Ergonomic API design
- **Strict Compilation**: `-Yexplicit-nulls`, `-Ysafe-init`

### **Production Readiness**
- **Comprehensive Testing**: Unit, integration, and property-based tests
- **CI/CD Pipeline**: GitHub Actions with full automation
- **Docker Support**: Multi-stage builds with slim runtime
- **Developer Experience**: VS Code integration, batch scripts, clear docs

### **Performance & Scalability**
- **Tail Recursion**: Optimized for deep document structures
- **Immutable Data**: Thread-safe by design
- **Effect Management**: Asynchronous processing with ZIO
- **Memory Efficient**: Structural sharing in functional data structures

## License

This project is licensed under the MIT License.
