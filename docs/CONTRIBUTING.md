# Contributing to Document Matrix

## Development Setup

### Prerequisites
- Java 21 (OpenJDK or Oracle JDK)
- SBT (Scala Build Tool) 1.10.x
- VS Code with Metals extension (recommended)
- Git

### Local Development

1. **Clone and Setup**:
   ```bash
   git clone <repository-url>
   cd devtask3mk
   sbt compile
   ```

2. **IDE Setup**:
   - Install VS Code Metals extension
   - Open the project folder in VS Code
   - Metals will automatically import the SBT project

3. **Code Formatting**:
   ```bash
   sbt scalafmtAll        # Format all code
   sbt scalafmtCheckAll   # Check formatting
   ```

### Development Workflow

1. **Create Feature Branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make Changes**:
   - Write code following Scala 3 best practices
   - Add tests for new functionality
   - Update documentation if needed

3. **Test Your Changes**:
   ```bash
   sbt clean compile test
   ```

4. **Submit Pull Request**:
   - Ensure all tests pass
   - Code is properly formatted
   - Include descriptive commit messages

### Code Style Guidelines

- Use Scala 3 syntax (given/using, extension methods, etc.)
- Follow functional programming principles
- Prefer immutable data structures
- Use descriptive variable and function names
- Add type annotations for public APIs
- Write comprehensive tests

### Testing Guidelines

- Write unit tests for all public functions
- Use property-based testing where appropriate
- Test both happy path and error scenarios
- Maintain good test coverage

### Architecture Guidelines

- Keep the Document ADT simple and focused
- Use type classes for extensibility
- Prefer pure functions over side effects
- Use ZIO for effect management
- Follow the existing package structure

## Glossary

### Functional Programming Terms

- **ADT (Algebraic Data Type)**: Data types formed by combining other types using sealed traits and case classes
- **Catamorphism**: A recursion scheme that "folds" a recursive data structure into a single value
- **Functor**: A type class that provides a `map` operation for transforming values inside a container
- **Monad**: A type class for composing computations that may have effects
- **Semigroup**: A type class with an associative binary operation
- **Monoid**: A semigroup with an identity element
- **Traversable**: A type class for applying an effectful function to elements and collecting results

### Scala/ZIO Terms

- **ZIO**: A powerful effect system for Scala providing type-safe, composable, and concurrent programming
- **Cats**: A functional programming library providing type classes and data types
- **Circe**: A JSON library for Scala with automatic derivation and type safety
- **Http4s**: A functional HTTP library built on cats-effect
- **Metals**: Language server for Scala providing IDE features

### Project-Specific Terms

- **Document**: The core ADT representing a tree structure with layout information
- **Leaf**: A document node containing a single value
- **Horizontal/Vertical**: Layout containers for arranging child documents
- **Pretty Printing**: Rendering documents in a human-readable format with indentation
- **Recursion Scheme**: A pattern for processing recursive data structures in a structured way

## Common Tasks

### Adding New Document Types

1. Add new case class to the `Document` sealed trait
2. Update pattern matches in `map`, `traverse`, `fold` functions
3. Add JSON encoder/decoder cases
4. Update pretty printing logic
5. Add tests for the new type

### Adding New Operations

1. Add function to `Document` companion object
2. Follow existing patterns for recursion
3. Add comprehensive tests
4. Update documentation

### Performance Optimization

- Profile with JMH benchmarks
- Consider tail recursion for deep structures
- Use efficient collection operations
- Monitor memory usage with large documents

## Release Process

1. Update version in `build.sbt`
2. Update `CHANGELOG.md`
3. Create git tag: `git tag v1.x.x`
4. Push tag: `git push origin v1.x.x`
5. GitHub Actions will handle the release
