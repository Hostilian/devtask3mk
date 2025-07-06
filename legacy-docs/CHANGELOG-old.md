# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-07-01

### Added
- Initial implementation of Document Matrix project
- Core Document ADT with Leaf, Horizontal, Vertical, and Empty variants
- Functional programming abstractions (Functor, Traversable, Semigroup, Monoid)
- Recursion schemes with catamorphism support
- Type-safe JSON serialization with Circe
- Interactive CLI with colored output using JANSI
- HTTP API with endpoints for render, validate, and health check
- Comprehensive test suite with ScalaTest and ZIO Test
- Property-based testing for functional laws
- GitHub Actions CI/CD pipeline
- Docker support with multi-stage builds
- Complete documentation and contributing guidelines
- Windows batch script for easy development
- VS Code Metals integration with tasks

### Technical Features
- Scala 3.4.3 with latest language features
- ZIO 2.1.11 for effect management
- Http4s for functional HTTP services
- Cats Effect for pure functional programming
- Monocle for optics (future extensibility)
- Comprehensive error handling and validation
