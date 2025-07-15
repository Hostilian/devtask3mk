# 🚀 DevTask3MK - Multi-Stack Development Platform

[![CI/CD Pipeline](https://github.com/Hostilian/devtask3mk/actions/workflows/ci.yml/badge.svg)](https://github.com/Hostilian/devtask3mk/actions/workflows/ci.yml)
[![Scala Version](https://img.shields.io/badge/scala-3.4.3-red.svg)](https://www.scala-lang.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Docker](https://img.shields.io/badge/docker-supported-blue.svg)](https://www.docker.com/)

> **A comprehensive full-stack development platform showcasing advanced functional programming, modern web development, and production-ready infrastructure**

## 🌟 Overview

DevTask3MK is a sophisticated multi-stack project that evolved from a functional programming assignment into a complete development ecosystem. It demonstrates mastery across multiple domains:

- **🎯 Functional Programming Excellence** - Advanced Scala 3 with algebraic data types, type classes, and effect systems
- **🌐 Modern Web Development** - Svelte 5 applications with TypeScript and modern tooling
- **🚌 Real-World Applications** - Transport API integration and interactive games
- **🏗️ Production Infrastructure** - Docker, CI/CD, monitoring, and deployment automation
- **📊 Data Processing** - Document matrix operations with mathematical foundations

## 📁 Project Architecture

```
devtask3mk/
├── 🔧 backend/                     # Scala 3 Functional Programming Core
│   ├── core/                       # Document matrix & FP foundations
│   │   ├── Document.scala          # Core algebraic data types
│   │   ├── DocumentAlgebras.scala  # Type classes & algebras
│   │   ├── DocumentFree.scala      # Free monads & DSL
│   │   └── DocumentOptics.scala    # Lens/Prism operations
│   ├── apps/                       # Production applications
│   │   └── transport-api/          # BlaBlaCar Bus API integration
│   │       ├── Server.scala        # HTTP4s REST API server
│   │       ├── Cli.scala           # Interactive command-line tool
│   │       └── BlaBlaBusApi.scala  # External API client
│   └── src/                        # Tests & examples
│       ├── main/scala/             # Core implementations
│       └── test/scala/             # Comprehensive test suite
├── 🎮 frontend/                    # Modern Web Applications
│   └── snake-game/                 # Svelte 5 Snake Game
│       ├── src/App.svelte          # Game logic with runes
│       └── package.json            # Modern toolchain
├── 🏗️ infrastructure/              # Production Infrastructure
│   ├── docker-compose.yml         # Multi-service orchestration
│   ├── Dockerfile                 # Containerization
│   └── monitoring/                # Observability stack
├── 📚 docs/                        # Comprehensive documentation
└── 🔄 .github/workflows/           # CI/CD automation
```

## 🚀 Quick Start Guide

### Prerequisites
- **Java 21+** (for Scala backend)
- **Node.js 18+** (for frontend applications)
- **Docker & Docker Compose** (for containerized deployment)
- **SBT 1.11.0+** (for Scala build)

### 🎯 Run the Full Stack

```bash
# 1. Clone the repository
git clone https://github.com/Hostilian/devtask3mk.git
cd devtask3mk

# 2. Start everything with Docker
docker-compose -f infrastructure/docker-compose.yml up -d

# 3. Access the applications
# - REST API: http://localhost:8081
# - Snake Game: http://localhost:3000
# - Monitoring: http://localhost:9090
```

### 🔧 Development Setup

```bash
# Backend Development (Scala)
sbt compile                       # Compile Scala code
sbt test                         # Run test suite
sbt "runMain com.example.Server" # Start API server
sbt "runMain com.example.Cli"    # Interactive CLI

# Frontend Development (Svelte)
cd frontend/snake-game
npm install                      # Install dependencies
npm run dev                     # Start development server
npm run build                   # Production build
```

## 🎯 Core Applications

### 🚌 Transport API Server
**Location:** `backend/apps/transport-api/`

A production-ready REST API built with **HTTP4s** and **ZIO** that integrates with the BlaBlaCar Bus API for route planning and booking management.

**Key Features:**
- RESTful endpoints for transport data
- Real-time route search and booking
- Document matrix operations for data processing
- Comprehensive error handling and validation
- OpenAPI documentation

**API Endpoints:**
```bash
GET  /health                  # Health check
POST /documents/process       # Process document matrices
GET  /transport/search        # Search bus routes
POST /transport/book          # Create bookings
```

### 🎮 Snake Game (Svelte 5)
**Location:** `frontend/snake-game/`

A modern implementation of the classic Snake game built with **Svelte 5**, showcasing the latest web development patterns and reactive programming with runes.

**Technical Highlights:**
- Svelte 5 runes for reactive state management
- TypeScript for type safety
- Canvas-based graphics with smooth animations
- Responsive design and modern UI
- Vite for lightning-fast development

### 💻 Interactive CLI
**Location:** `backend/apps/transport-api/Cli.scala`

A beautiful command-line interface for interacting with document matrices and transport APIs, featuring colored output and interactive menus.

## 🧮 Functional Programming Excellence

The backend demonstrates advanced functional programming concepts:

### Core Algebraic Data Types
```scala
sealed trait Document[A]
case class Leaf[A](value: A) extends Document[A]
case class Horizontal[A](cells: List[Document[A]]) extends Document[A]
case class Vertical[A](cells: List[Document[A]]) extends Document[A]
case class Empty[A]() extends Document[A]
```

### Type Class Instances
- **Functor, Applicative, Monad** - Structure-preserving transformations
- **Traverse** - Apply effects across document structures
- **Semigroup, Monoid** - Compositional document operations
- **Show, Eq** - Safe string representation and equality

### Advanced Patterns
- **Free Monads** - Domain-specific languages for document operations
- **Tagless Final** - Effect polymorphism for different interpreters
- **Optics (Lens/Prism)** - Deep document manipulation
- **Property-Based Testing** - Mathematical law verification

## 🏗️ Production Infrastructure

### Containerization
- **Multi-stage Docker builds** for optimized images
- **Docker Compose** orchestration for full-stack deployment
- **Health checks** and restart policies
- **Volume management** for persistent data

### Monitoring & Observability
- **Prometheus** metrics collection
- **Grafana** dashboards and alerting
- **Application health checks**
- **Performance monitoring**

### CI/CD Pipeline
- **Automated testing** on multiple platforms
- **Code quality checks** and formatting
- **Security scanning** with Snyk
- **Automated deployments** to staging/production
- **Performance regression testing**

## 🔧 Available Commands

### Backend (Scala)
```bash
# Compilation & Testing
sbt compile              # Compile all Scala code
sbt test                 # Run comprehensive test suite
sbt scalafmtAll          # Format code consistently

# Applications
sbt "runMain com.example.Server"           # Start HTTP API server
sbt "runMain com.example.Cli"              # Interactive CLI tool
sbt "runMain com.example.ComprehensiveExample"  # Demo all features

# Development
sbt clean                # Clean build artifacts
sbt docs/mdoc            # Generate documentation
```

### Frontend (Web)
```bash
# Snake Game
cd frontend/snake-game
npm run dev              # Development server
npm run build            # Production build
npm run preview          # Preview production build
npm run check            # Type checking
```

### Infrastructure
```bash
# Docker Operations
cd infrastructure
docker-compose up -d     # Start all services
docker-compose down      # Stop all services
docker-compose logs -f   # Follow logs

# Individual Services
docker-compose up backend     # Start only backend
docker-compose up frontend    # Start only frontend
```

## 🧪 Testing & Quality Assurance

### Comprehensive Test Coverage
- **Unit Tests** - Core functionality verification
- **Property-Based Tests** - Mathematical law verification with ScalaCheck
- **Integration Tests** - End-to-end workflow testing
- **Performance Tests** - Benchmarking with JMH

### Code Quality
- **Automated formatting** with Scalafmt
- **Linting** with Scalafix
- **Type safety** with strict compiler flags
- **Documentation** with comprehensive README and API docs

## 📊 Real-World Applications

### Transport Data Processing
- **Route optimization** algorithms
- **Booking management** systems
- **Real-time data processing** pipelines
- **API integration** patterns

### Document Matrix Operations
- **Hierarchical data structures** for layout engines
- **Functional transformations** for data processing
- **Effect management** for robust applications
- **Compositional design** patterns

## 🎓 Learning Outcomes

This project demonstrates mastery of:

1. **Advanced Functional Programming** - From basic concepts to production applications
2. **Modern Web Development** - Latest frameworks and best practices
3. **Production Infrastructure** - Real-world deployment and monitoring
4. **Software Architecture** - Clean, maintainable, and scalable design
5. **Full-Stack Development** - End-to-end application development

## 🚀 What's Next?

Potential extensions and improvements:

- [ ] **GraphQL API** - Modern API layer with type-safe queries
- [ ] **React Native Mobile App** - Cross-platform mobile experience
- [ ] **Machine Learning Integration** - Route optimization with ML
- [ ] **Microservices Architecture** - Service decomposition and orchestration
- [ ] **Event Sourcing** - Audit trails and temporal queries
- [ ] **Kubernetes Deployment** - Cloud-native orchestration

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

---

**Built with ❤️ by the DevTask3MK team**

*Showcasing the power of functional programming, modern web development, and production-ready infrastructure in a single, cohesive platform.*
