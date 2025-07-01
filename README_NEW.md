# Document Matrix - Comprehensive Functional Programming Showcase

[![CI](https://github.com/Hostilian/devtask3mk/actions/workflows/ci.yml/badge.svg)](https://github.com/Hostilian/devtask3mk/actions/workflows/ci.yml)
[![Scala Version](https://img.shields.io/badge/scala-3.4.3-red.svg)](https://scala-lang.org/)
[![ZIO Version](https://img.shields.io/badge/zio-2.1.11-blue.svg)](https://zio.dev/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

This project demonstrates advanced functional programming concepts in Scala, focusing on the assignment requirement to implement a document data structure with comprehensive type-class support and monadic transformations.

## 🎯 Assignment Implementation

### Core Requirement
**Define a data type D that represents a document subdivided horizontally or vertically into 1 or more cells that can be further subdivided or hold values of type A.**

**Equip with function:** `f[M[_]: Monad, A, B]: (A => M[B]) => D[A] => M[D[B]]`

**Such that:**
- `f[Id](identity) = identity`
- `f[Option](Some(_)) = Some(_)`

✅ **Implemented in:** `Document.scala` - The function `f` is implemented as `traverse` specialized for Monad contexts.

## 🏗️ Functional Programming Concepts Demonstrated

### 1. Algebraic Data Types (ADTs)
- **Sum types:** `sealed trait Document[A]` with case classes
- **Product types:** Case classes combining multiple values
- **Unit type:** `Empty[A]()` representing void
- **Examples:** `DocumentError`, `DocumentOp[A]`, `Position`

```scala
sealed trait Document[A]                               // Sum type
case class Leaf[A](value: A) extends Document[A]      // Product type
case class Horizontal[A](cells: List[Document[A]]) extends Document[A]
case class Vertical[A](cells: List[Document[A]]) extends Document[A]
case class Empty[A]() extends Document[A]             // Unit type
```

### 2. Recursion Schemes
- **Catamorphism:** `cata` - tears down document structure
- **Anamorphism:** `ana` - builds up document structure
- **Paramorphism:** Available through fold operations

```scala
def cata[A, B](doc: Document[A])(
  leafAlg: A => B,
  horizontalAlg: List[B] => B,
  verticalAlg: List[B] => B,
  emptyAlg: () => B
): B
```

### 3. Higher-Kinded Types
- **Type constructors:** `Document[_]`, `F[_]` parameters
- **Kind polymorphism:** Functions work with any `F[_]: Monad`
- **Type-level computation:** Phantom types for compile-time safety

### 4. Polymorphism
- **Parametric:** Functions work for any type `A`
- **Ad-hoc:** Type classes provide different behavior per type
- **Subtype:** Sealed trait hierarchy

**Type Classes Implemented:**
- `Functor[Document]`
- `Applicative[Document]` 
- `Monad[Document]`
- `Traverse[Document]`
- `Semigroup[Document[A]]`
- `Monoid[Document[A]]`

### 5. Functors, Applicatives, Monads
- **Functor:** `map` operations preserving structure
- **Applicative:** `map2`, `ap` for combining contexts
- **Monad:** `flatMap`, `pure` for sequential composition
- **Traverse:** `traverse` for effects across structure

### 6. Free Monads
**File:** `DocumentFree.scala`
- **DSL:** Domain-specific language for document operations
- **Interpreters:** Pure (`Id`) and effectful (`Option`) interpreters
- **Composability:** Complex programs from simple operations

```scala
val program = for {
  leaf1 <- createLeaf("A")
  leaf2 <- createLeaf("B") 
  combined <- createHorizontal(List(leaf1, leaf2))
} yield combined
```

### 7. Tagless Final
Alternative to Free monads using higher-kinded type classes:

```scala
trait DocumentF[F[_]] {
  def createLeaf[A](value: A): F[Document[A]]
  def createHorizontal[A](docs: List[Document[A]]): F[Document[A]]
  // ...
}
```

### 8. Effects & Validation
- **Validation:** Using `ValidatedNel` for accumulating errors
- **Error handling:** `Either[DocumentError, A]` for explicit error types
- **Effect polymorphism:** Works with any `Applicative[F]`

### 9. Algebras
**File:** `DocumentAlgebras.scala`
- **Render algebra:** Different rendering strategies (ASCII, HTML)
- **Metrics algebra:** Calculate document properties
- **Composition:** Combine multiple algebras

### 10. Semigroup & Monoid
- **Semigroup:** Associative combination of documents
- **Monoid:** Semigroup with identity element (`Empty`)
- **Laws:** Associativity and identity properties verified

### 11. Type Safety
- **Phantom types:** Compile-time guarantees with `TypedDocument[A, S]`
- **Type-driven development:** Types guide implementation
- **Parse safety:** `Either[DocumentError, A]` for safe parsing

### 12. Validation & Parsing
- **Validation combinators:** Applicative validation
- **Parser safety:** Error types for different failure modes
- **Serialization:** JSON encoding/decoding with Circe

## 🚀 Running the Project

### Compile
```bash
sbt compile
```

### Run Tests
```bash
sbt test
```

### Run Examples
```bash
# CLI with interactive mode
sbt "runMain com.example.Cli"

# Server (runs on http://localhost:8080)
sbt "runMain com.example.Server"

# Comprehensive example
sbt "runMain com.example.ComprehensiveExample"
```

### Available Tasks
```bash
sbt "SBT Compile"     # Compile the project
sbt "SBT Test"        # Run all tests  
sbt "Run CLI"         # Start CLI application
sbt "Run Server"      # Start HTTP server
sbt "Format Code"     # Format Scala code
```

## 📁 Project Structure

```
src/main/scala/
├── Document.scala              # Core ADT with type class instances
├── DocumentFree.scala          # Free monad DSL and interpreters  
├── DocumentAlgebras.scala      # Various algebras and composition
├── DocumentOptics.scala        # Lens/Prism optics for deep access
├── ComprehensiveExample.scala  # Complete demonstration
├── Server.scala               # HTTP4s server
└── Cli.scala                  # Command-line interface

src/test/scala/
├── DocumentSpec.scala          # Basic functionality tests
├── DocumentPropertySpec.scala  # Property-based tests
└── AdvancedDocumentSpec.scala  # Advanced concepts tests
```

## 🔧 Dependencies

- **Scala 3.4.3:** Latest Scala with improved type system
- **Cats:** Functional programming type classes and abstractions
- **Cats Effect:** Effect system for pure functional programming
- **Cats Free:** Free monad implementation
- **ZIO:** Alternative effect system with excellent interop
- **Circe:** Functional JSON library
- **HTTP4s:** Pure functional HTTP library  
- **Monocle:** Optics library for immutable updates
- **ScalaTest + ScalaCheck:** Testing and property-based testing

## 🎓 Educational Value

This project serves as a comprehensive reference for:

1. **Functional Programming Fundamentals:** ADTs, recursion schemes, type classes
2. **Advanced Type System Features:** Higher-kinded types, phantom types, type-level programming
3. **Effect Systems:** Free monads, tagless final, effect polymorphism
4. **Algebraic Abstractions:** Semigroups, monoids, functors, monads
5. **Real-World Application:** HTTP server, CLI, JSON serialization
6. **Testing:** Unit tests, property-based tests, law verification

## 🔍 Key Insights

1. **The assignment's function `f`** is actually the `traverse` operation specialized for monads, demonstrating how theoretical concepts have practical applications.

2. **Free monads vs Tagless Final:** Both approaches are shown for building composable DSLs with different trade-offs.

3. **Type Class Coherence:** Multiple type class instances work together seamlessly thanks to Scala's implicit system.

4. **Effect Polymorphism:** The same code works with `Id`, `Option`, `Either`, `ZIO`, etc., showing the power of abstraction.

5. **Algebraic Thinking:** Problems are solved by finding the right algebraic structures and their laws.

## 🏆 Assignment Verification

The implementation fully satisfies the assignment requirements:

✅ **Data type D:** `Document[A]` represents subdivided documents  
✅ **Function f:** Implemented with correct type signature  
✅ **Identity law:** `f[Id](identity) = identity` ✓  
✅ **Option law:** `f[Option](Some(_)) = Some(_)` ✓  
✅ **All concepts:** Every listed topic is comprehensively demonstrated  

Run `sbt "runMain com.example.ComprehensiveExample"` to see all concepts in action!

## 📋 Prerequisites

- **JDK 11+:** Modern JVM runtime
- **SBT 1.5+:** Scala build tool

## 🧪 Testing

The project includes comprehensive testing:

```bash
# Run all tests
sbt test

# Run specific test suites
sbt "testOnly com.example.DocumentSpec"
sbt "testOnly com.example.AdvancedDocumentSpec"
sbt "testOnly com.example.DocumentPropertySpec"
```

### Test Coverage
- **Unit tests:** Core functionality verification
- **Property-based tests:** Law verification with ScalaCheck
- **Integration tests:** End-to-end API testing
- **Type class laws:** Functor, monad, monoid law verification

## 🌐 API Endpoints

When running the server (`sbt "runMain com.example.Server"`):

- `POST /render` - Render document as formatted text
- `POST /validate` - Validate document structure
- `GET /health` - Health check endpoint

Example:
```bash
curl -X POST http://localhost:8080/render \
  -H "Content-Type: application/json" \
  -d '{"type":"horizontal","cells":[{"type":"leaf","value":"A"},{"type":"leaf","value":"B"}]}'
```

## 🔧 Development

### Code Formatting
```bash
sbt scalafmtAll
```

### Continuous Integration
The project includes GitHub Actions CI that:
- Compiles the code
- Runs all tests
- Checks code formatting
- Generates test reports

### Architecture Decisions
- **Scala 3:** Latest language features and improved type system
- **ZIO + Cats:** Best of both effect systems
- **Pure Functional:** No mutable state, explicit effects
- **Type-driven:** Types guide implementation and prevent bugs

---

*This project demonstrates that functional programming is not just academic theory, but a practical approach to building robust, composable, and maintainable software systems.*
