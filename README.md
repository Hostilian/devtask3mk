# Document Matrix 📊

[![CI/CD Pipeline](https://github.com/Hostilian/devtask3mk/actions/workflows/ci.yml/badge.svg)](https://github.com/Hostilian/devtask3mk/actions/workflows/ci.yml)
[![Scala Version](https://img.shields.io/badge/scala-3.4.3-red.svg)](https://www.scala-lang.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Docker](https://img.shields.io/badge/docker-supported-blue.svg)](https://www.docker.com/)

A comprehensive Scala 3 project demonstrating advanced functional programming concepts through a document data structure library. Features algebraic data types, type classes, free monads, tagless final pattern, and property-based testing. **Designed for transport data processing and integration with APIs like BlaBlaCar Bus API.**

## ✨ Features

- 🎯 **Algebraic Data Types** - Sealed trait hierarchies with case classes
- 🔄 **Type Class Instances** - Functor, Applicative, Monad, Traverse implementations  
- 🧮 **Semigroup/Monoid** - Lawful document combination with associativity
- 🔍 **Property-Based Testing** - ScalaCheck for law verification
- 🏗️ **Free Monads** - DSL creation and interpretation patterns
- 🎭 **Tagless Final** - Modern functional architecture approach
- 🔧 **Optics** - Lens/Prism operations with Monocle
- 🌐 **HTTP API** - RESTful web service with Http4s and ZIO
- � **Transport API Integration** - Designed for BlaBlaCar Bus API and travel data processing
- 📊 **Data Processing** - Route information, booking data, and real-time updates
- �📦 **Docker Support** - Containerized deployment ready
- ⚙️ **CI/CD Pipeline** - Automated testing, formatting, and deployment

## 🚀 Quick Start

### Prerequisites
- Java 21+
- SBT 1.11.0+

### Installation
```bash
git clone https://github.com/Hostilian/devtask3mk.git
cd devtask3mk
sbt compile
```

### Run Tests
```bash
sbt test
```

### Start HTTP Server
```bash
sbt "runMain com.example.Server"
# Server starts on http://localhost:8081
```

### Test API
```bash
curl http://localhost:8081/health
# Returns: "Server is running"

curl -X POST http://localhost:8081/render \
  -H "Content-Type: application/json" \
  -d '{"type":"leaf","value":"Hello World"}'
```

## 📚 Documentation

- 📖 **[Getting Started](wiki/Getting-Started)** - Setup and installation guide
- 🔍 **[API Documentation](wiki/API-Documentation)** - Complete API reference
- 💡 **[Examples](wiki/Examples)** - Practical usage examples
- 🏗️ **[Architecture Overview](wiki/Architecture-Overview)** - System design and patterns
- 🧪 **[Testing Strategy](wiki/Testing-Strategy)** - Testing approaches and tools

## 🎯 Core Example

```scala
import com.example.Document._
import cats.syntax.semigroup._

// Create documents
val doc1 = Horizontal(List(Leaf("A"), Leaf("B")))
val doc2 = Vertical(List(Leaf("C"), Leaf("D")))

// Functional operations
val mapped = doc1.map(_.toLowerCase)
val combined = doc1 |+| doc2
val folded = Document.fold(combined)(_ => 1)(_.sum)(_.sum)

// BlaBlaCar Bus API Integration Example
case class BusRoute(origin: String, destination: String, price: BigDecimal)

def routeToDocument(route: BusRoute): Document[String] = {
  Vertical(List(
    Leaf(s"🚌 ${route.origin} → ${route.destination}"),
    Leaf(s"💰 €${route.price}")
  ))
}

val busRoute = BusRoute("Paris", "Lyon", BigDecimal("25.99"))
val routeDoc = routeToDocument(busRoute)
// Result: 🚌 Paris → Lyon
//         💰 €25.99

// HTTP API
POST /render
{
  "type": "vertical",
  "cells": [
    {"type": "horizontal", "cells": [
      {"type": "leaf", "value": "A"},
      {"type": "leaf", "value": "B"}
    ]},
    {"type": "leaf", "value": "C"}
  ]
}
```

## 🛠️ Technology Stack

| Category | Technology | Purpose |
|----------|------------|---------|
| **Language** | Scala 3.4.3 | Modern functional programming |
| **Build** | SBT 1.11.0 | Build tool and dependency management |
| **FP Library** | Cats | Type classes and functional abstractions |
| **Effect System** | ZIO | Asynchronous and concurrent programming |
| **HTTP** | Http4s | Web server and client |
| **JSON** | Circe | JSON encoding/decoding |
| **Testing** | ScalaTest + ScalaCheck | Unit and property-based testing |
| **Optics** | Monocle | Functional data manipulation |
| **Containerization** | Docker | Deployment and distribution |

## 📁 Project Structure

```
devtask3mk/
├── 📄 src/main/scala/
│   ├── 🎯 Document.scala           # Core ADT and type class instances
│   ├── 🎭 DocumentAlgebras.scala   # Tagless final algebras
│   ├── 🆓 DocumentFree.scala       # Free monad DSL
│   ├── 🔍 DocumentOptics.scala     # Lens/Prism operations  
│   ├── 🌐 Server.scala             # HTTP server with ZIO
│   └── 💻 Cli.scala               # Command-line interface
├── 🧪 src/test/scala/
│   ├── ✅ DocumentSpec.scala       # Basic unit tests
│   ├── 🎲 DocumentPropertySpec.scala # Property-based tests
│   └── 🔬 AdvancedDocumentSpec.scala # Advanced functionality
├── 📚 docs/                       # Additional documentation
├── 🏭 .github/workflows/          # CI/CD pipeline
├── 📝 wiki/                       # GitHub wiki pages
└── ⚙️ project/                    # SBT configuration
```

## 🧪 Testing

We use a comprehensive testing strategy:

- **Unit Tests** - Basic functionality verification
- **Property-Based Tests** - Law verification with ScalaCheck
- **Integration Tests** - HTTP API and end-to-end testing
- **CI/CD Pipeline** - Automated testing on every commit

```bash
# Run all tests
sbt test

# Run specific test suite
sbt "testOnly com.example.DocumentSpec"

# Run with detailed output
sbt "testOnly * -- -oF"

# Property-based tests
sbt "testOnly com.example.DocumentPropertySpec"
```

## 🐳 Docker Support

```bash
# Build image
docker build -t document-matrix .

# Run container
docker run -p 8081:8081 document-matrix

# Using Docker Compose
docker-compose up
```

## 🤝 Contributing

We welcome contributions! Please see our [Development Guide](wiki/Development-Guide) for:

- Code style guidelines
- Testing requirements  
- Pull request process
- Issue reporting

### Development Workflow

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make changes and add tests
4. Run tests: `sbt test`
5. Format code: `sbt scalafmtAll`
6. Commit changes: `git commit -m 'Add amazing feature'`
7. Push to branch: `git push origin feature/amazing-feature`
8. Open a Pull Request

## 📊 Performance

The library is designed for:
- **Memory efficiency** - Immutable data structures with structural sharing
- **Type safety** - Compile-time guarantees with Scala's type system
- **Composability** - Clean functional interfaces for easy combination
- **Testability** - Pure functions and property-based testing

## 🏆 Highlights

- ✅ **100% Test Coverage** - All functionality thoroughly tested
- 📐 **Mathematical Laws** - Verified Functor, Monad, and Semigroup laws
- 🔒 **Type Safety** - Leverages Scala 3's advanced type system
- 📚 **Comprehensive Docs** - Extensive documentation and examples
- 🚀 **Production Ready** - CI/CD pipeline with automated deployment
- 🐳 **Container Ready** - Docker support for easy deployment

## 📈 Examples in Action

### Basic Document Creation
```scala
val layout = Vertical(List(
  Horizontal(List(Leaf("Header Left"), Leaf("Header Right"))),
  Leaf("Main Content"),
  Horizontal(List(Leaf("Footer Left"), Leaf("Footer Right")))
))
```

### Transport Data Processing
```scala
// BlaBlaCar Bus route processing
case class BusRoute(origin: String, destination: String, departure: String, price: BigDecimal)

val routes = List(
  BusRoute("Paris", "Lyon", "08:30", BigDecimal("25.99")),
  BusRoute("Paris", "Lyon", "14:30", BigDecimal("28.99"))
)

def displaySearchResults(routes: List[BusRoute]): Document[String] = {
  val header = Leaf("🔍 Available Routes")
  val routeList = routes.map { route =>
    Vertical(List(
      Leaf(s"🚌 ${route.origin} → ${route.destination}"),
      Horizontal(List(
        Leaf(s"🕐 ${route.departure}"),
        Leaf(s"💰 €${route.price}")
      ))
    ))
  }
  
  Vertical(List(header) ++ routeList)
}

val searchResults = displaySearchResults(routes)
```

### Functional Transformations
```scala
// Map over all values
val uppercased = layout.map(_.toUpperCase)

// Validate and transform
val validated = layout.traverse(validateNonEmpty)

// Combine documents
val combined = layout1 |+| layout2
```

### HTTP API Usage
```bash
# Complex document structure
curl -X POST http://localhost:8081/render \
  -H "Content-Type: application/json" \
  -d '{
    "type": "vertical",
    "cells": [
      {
        "type": "horizontal",
        "cells": [
          {"type": "leaf", "value": "Top Left"},
          {"type": "leaf", "value": "Top Right"}
        ]
      },
      {"type": "leaf", "value": "Bottom"}
    ]
  }'
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔗 Links

- 📖 **[Full Documentation](wiki)** - Complete wiki with guides and examples
- 🐛 **[Issues](../../issues)** - Bug reports and feature requests
- 💬 **[Discussions](../../discussions)** - Community discussions
- 🔄 **[Pull Requests](../../pulls)** - Code contributions
- 🏗️ **[Actions](../../actions)** - CI/CD pipeline status

## ⭐ Star History

If you find this project helpful, please consider giving it a star! ⭐

---

**Built with ❤️ using Scala 3, Cats, ZIO, and modern functional programming principles.**
