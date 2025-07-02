# Document Matrix - Project Wiki

Welcome to the **Document Matrix** project wiki! This is a comprehensive Scala project demonstrating advanced functional programming concepts with a focus on document data structures.

## 🚀 Quick Start

- **[Getting Started](Getting-Started)** - Set up your development environment
- **[API Documentation](API-Documentation)** - Complete API reference
- **[Examples](Examples)** - Code examples and tutorials
- **[Architecture Overview](Architecture-Overview)** - System design and structure

## 📚 Documentation

### Core Concepts
- **[Document Data Structure](Document-Data-Structure)** - Understanding the core Document ADT
- **[Functional Programming Patterns](Functional-Programming-Patterns)** - FP concepts used in the project
- **[Type Classes](Type-Classes)** - Cats type class implementations
- **[Monad Transformers](Monad-Transformers)** - Advanced monadic compositions

### Development
- **[Development Guide](Development-Guide)** - How to contribute and develop
- **[Testing Strategy](Testing-Strategy)** - Unit, property, and integration tests
- **[CI/CD Pipeline](CI-CD-Pipeline)** - Continuous integration and deployment
- **[Code Style](Code-Style)** - Scala coding standards and formatting
- **[BlaBlaCar Bus API Integration](BlaBlaCar-Bus-API-Integration)** - Transport data processing guide

### Advanced Topics
- **[Free Monads](Free-Monads)** - DSL and interpretation patterns
- **[Tagless Final](Tagless-Final)** - Modern FP architecture pattern
- **[Optics](Optics)** - Lens and Prism for data manipulation
- **[ZIO Integration](ZIO-Integration)** - Effect system integration

## 🔧 Tools & Technologies

| Category | Technology | Purpose |
|----------|------------|---------|
| Language | Scala 3.4.3 | Core programming language |
| Build | SBT 1.11.0 | Build tool and dependency management |
| FP Library | Cats | Functional programming abstractions |
| Effect System | ZIO | Asynchronous and concurrent programming |
| HTTP | Http4s | Web server and HTTP client |
| JSON | Circe | JSON encoding/decoding |
| Testing | ScalaTest + ScalaCheck | Unit and property-based testing |
| Optics | Monocle | Lens and Prism operations |

## 📖 Project Structure

```
devtask3mk/
├── src/
│   ├── main/scala/
│   │   ├── Document.scala           # Core document ADT and instances
│   │   ├── DocumentAlgebras.scala   # Tagless final algebras
│   │   ├── DocumentFree.scala       # Free monad DSL
│   │   ├── DocumentOptics.scala     # Lens/Prism operations
│   │   ├── Server.scala             # HTTP server
│   │   └── Cli.scala               # Command-line interface
│   └── test/scala/
│       ├── DocumentSpec.scala       # Basic unit tests
│       ├── DocumentPropertySpec.scala # Property-based tests
│       └── AdvancedDocumentSpec.scala # Advanced functionality tests
├── docs/                           # Additional documentation
├── .github/workflows/              # CI/CD pipeline
└── project/                        # SBT configuration
```

## 🎯 Key Features

- **Algebraic Data Types** - Sealed trait hierarchies with case classes
- **Type Class Instances** - Functor, Applicative, Monad, Traverse implementations
- **Property-Based Testing** - Laws verification with ScalaCheck
- **Free Monads** - DSL creation and interpretation
- **Tagless Final** - Modern FP architecture pattern
- **Optics** - Functional data manipulation with Monocle
- **HTTP API** - RESTful web service with Http4s
- **Docker Support** - Containerized deployment
- **CI/CD Pipeline** - Automated testing and deployment

## 🚀 Quick Examples

### Basic Document Creation
```scala
import com.example.Document._

val doc = Vertical(List(
  Horizontal(List(Leaf("A"), Leaf("B"))),
  Horizontal(List(Leaf("C"), Leaf("D")))
))
```

### Functional Operations
```scala
// Map over values
val upperDoc = doc.map(_.toUpperCase)

// Traverse with effects
val validated = Document.traverse(doc)(validateString)

// Combine documents
val combined = doc1 |+| doc2
```

### HTTP API Usage
```bash
# Render a document
curl -X POST http://localhost:8081/render \
  -H "Content-Type: application/json" \
  -d '{"type":"leaf","value":"Hello World"}'

# Health check
curl http://localhost:8081/health
```

## 🤝 Contributing

We welcome contributions! Please see our:
- **[Development Guide](Development-Guide)** for setup instructions
- **[Code Style](Code-Style)** for formatting standards
- **[Testing Strategy](Testing-Strategy)** for test requirements

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details.

## 🔗 External Resources

- [Cats Documentation](https://typelevel.org/cats/)
- [ZIO Documentation](https://zio.dev/)
- [Http4s Documentation](https://http4s.org/)
- [Circe Documentation](https://circe.github.io/circe/)
- [Monocle Documentation](https://www.optics.dev/Monocle/)

---

**Last Updated:** July 2, 2025  
**Version:** 1.0.0  
**Maintainer:** Development Team
