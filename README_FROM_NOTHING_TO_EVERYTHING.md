# Document Matrix: From Nothing to Everything (Norm Macdonald Style)

Hey, so you want to go from zero to hero with this project? Buckle up. This is the full story, from setting up your machine to running the code in a Docker container, with all the functional programming jazz in between. No AI nonsense, just the facts, a little wit, and a lot of respect for your time.

---

## 🎯 Assignment Implementation

**Define a data type D that represents a document subdivided horizontally or vertically into 1 or more cells that can be further subdivided or hold values of type A.**

**Equip with function:** `f[M[_]: Monad, A, B]: (A => M[B]) => D[A] => M[D[B]]`

**Such that:**
- `f[Id](identity) = identity`
- `f[Option](Some(_)) = Some(_)`

✅ **Implemented in:** `Document.scala` - The function `f` is implemented as `traverse` specialized for Monad contexts.

---

## 1. Preparation & Project Initialization

- **Install Java 21+**
  - Download from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/).
- **Install SBT**
  - [SBT Setup Guide](https://www.scala-sbt.org/download.html)
- **Install VS Code**
  - [VS Code Download](https://code.visualstudio.com/)
  - Install the Metals extension for Scala.
- **Clone the repo:**
  ```bash
  git clone <repo-url>
  cd document-matrix
  ```
- **Initialize Git:**
  ```bash
  git init
  echo ".idea/\n.vscode/\ntarget/\nproject/target/\nproject/project/target/" > .gitignore
  ```
- **Folder Structure:**
  - `src/main/scala` — main code
  - `src/test/scala` — tests
  - `project/` — SBT config
  - `docs/` — documentation
- **Create `build.sbt`:**
  - Use Scala 3.4.x
  - Add dependencies: ZIO, cats-effect, zio-interop-cats, fansi/jansi, scalatest/zio-test, http4s, circe

---

## 2. Clean Up / Fix Existing Issues

- Delete unnecessary files, broken code, stale configs.
- Run `sbt clean`.
- Run `sbt compile` and fix every error/warning.
- Merge/clean up logic, remove duplication.
- Add scalafmt, reformat all code.
- Add missing configs for IDE, GitHub, CI.

---

## 3. Core Domain Modeling

- **Algebraic Data Types:**
  - `sealed trait Document[A]`
  - `case class Leaf[A](value: A)`
  - `case class Horizontal[A](cells: List[Document[A]])`
  - `case class Vertical[A](cells: List[Document[A]])`
  - `case class Empty[A]()`
- **Recursive, parametric, type-safe.**
- **Methods:** traversal, rendering, serialization.

---

## 4. Functional Abstractions & Recursion Schemes

- Implement catamorphisms (folds) for traversing/modifying documents.
- Equip D with:
  - `map` (Functor)
  - `traverse` (Applicative/Traversable)
  - Monadic transform: `f[M[_]: Monad, A, B]: (A => M[B]) => D[A] => M[D[B]]`
- **Laws:**
  - `f[Id](identity) = identity`
  - `f[Option](Some(_)) = Some(_)`
- Use typeclasses for polymorphism.

---

## 5. Type Classes, Polymorphism, & Effects

- Use `cats.Monad`, `Functor`, etc.
- Leverage type safety everywhere.
- Use ZIO or Cats Effect for effects.
- Implement Free monad patterns (bonus).
- Add semigroup/monoid operations (combine documents).
- Type-driven development, compile-time validation.

---

## 6. Validation & Parsing

- Input validation for document shapes, subdivision, value types.
- Parsing: from JSON/custom syntax to D[A].
- Type-safe parsers, detailed error handling.
- Unit tests for validation, parsing, failures.

---

## 7. Rendering, Pretty Printing, and CLI Interaction

- Tree-style pretty-printing for CLI (colors, box-drawing).
- Use fansi/jansi for colored output.
- CLI supports:
  - Reading docs from stdin/file/args/JSON
  - Pretty, colorized output
  - Interactive session (optional)
  - Validation feedback
- Optionally: Export to HTML/Markdown.

---

## 8. Web API (Bonus)

- Minimal HTTP API (http4s/ZIO-http/Play/Akka-http):
  - Endpoints for upload, render, validate, transform
  - Input/output as JSON
  - Error handling
  - Minimal OpenAPI spec (bonus)
- CLI and API can run together or separately.

---

## 9. Testing

- Unit tests for all core logic (ScalaTest/ZIO Test).
- Cover all combinators, parsing, validation, rendering.
- Property-based tests for recursive/monadic laws.
- Integration tests for API/CLI.
- High test coverage.

---

## 10. CI/CD Pipeline

- GitHub Actions workflow:
  - On push/PR: check formatting, compile, test, build Docker image
  - (Bonus) Deploy docs to GitHub Pages
- Add badge(s) for build/test status.

---

## 11. Dockerization

- Write a Dockerfile (multi-stage: build with SBT, copy to slim runtime)
- Expose CLI by default
- (Bonus) Support web API in same container
- Add docker-compose.yml if needed
- How to build/run:
  ```bash
  docker build -t document-matrix .
  docker run -it document-matrix
  docker run -it document-matrix java -cp document-matrix_3.4.3-1.0.0.jar com.example.Server
  ```

---

## 12. Maven Dual Support (Optional)

- Explain how to generate Maven project or build with Maven.
- Optionally provide pom.xml.
- Ensure dependencies, plugins, and test logic are in sync.

---

## 13. Documentation & Developer Experience

- Create a README.md: problem, usage, examples, build/test/run, CLI/API, extend/customize, architecture, FP explanations, wow factors.
- Write external docs: API reference, design doc, how to contribute, dev setup, FAQ, glossary.
- (Bonus) Generate API docs via scaladoc.
- (Bonus) Host docs via GitHub Pages.

---

## 14. Final Polish & Review

- Final code review: clean, idiomatic, modular.
- Remove TODOs, dead code, debug output.
- Add comments and Scaladoc.
- Run `sbt clean compile test`—all green.
- Check all wow factors.
- Tag v1.0.0 release.
- Provide clear handover: run, test, build, deploy, extend.

---

## 15. Impress Boss/Reviewer: Advanced Bonuses

- Custom rendering: diagrams, PDF, SVG, etc.
- Advanced FP patterns: fixpoint types, optics/lenses.
- Plugin modules (extensibility).
- Integration with other tools (Excel, FP libs).
- Live demo script or video.
- Code walkthrough in docs.

---

## 🚦Summary Checklist

- All setup, cleaning, and compile issues resolved
- Modern, functional, type-safe, and “wow” Scala implementation
- Deep test coverage, CI/CD, Docker, docs
- CLI and/or API, pretty-printing, error handling
- Developer onboarding and user documentation
- Clean, professional, production-ready repo

---

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

---

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

# Server (runs on http://localhost:8081)
sbt "runMain com.example.Server"

# Comprehensive example
sbt "runMain com.example.ComprehensiveExample"
```

### Docker Usage (Cross-Platform)

#### Build the Docker image
```bash
docker build -t document-matrix .
```

#### Run the CLI (default)
```bash
docker run -it document-matrix
```

#### Run the server (HTTP API on port 8081)
```bash
docker run -it -e MODE=server -p 8081:8081 document-matrix
```

#### Run with Docker Compose
```bash
docker-compose up
```
- The CLI and server are both supported as services in `docker-compose.yml`.
- For the server, access it at http://localhost:8081

#### Troubleshooting
- If you get a port error, make sure 8081 is free on your host.
- On Windows, use `winpty` or `docker run -it` for CLI mode.
- The image works on Linux, macOS, and Windows (WSL2 recommended for best Docker experience on Windows).

### Available Tasks
```bash
sbt "SBT Compile"     # Compile the project
sbt "SBT Test"        # Run all tests  
sbt "Run CLI"         # Start CLI application
sbt "Run Server"      # Start HTTP server
sbt "Format Code"     # Format Scala code
```

---

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

---

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

---

## 🎓 Educational Value

This project serves as a comprehensive reference for:

1. **Functional Programming Fundamentals:** ADTs, recursion schemes, type classes
2. **Advanced Type System Features:** Higher-kinded types, phantom types, type-level programming
3. **Effect Systems:** Free monads, tagless final, effect polymorphism
4. **Algebraic Abstractions:** Semigroups, monoids, functors, monads
5. **Real-World Application:** HTTP server, CLI, JSON serialization
6. **Testing:** Unit tests, property-based tests, law verification

---

## 🔍 Key Insights

1. **The assignment's function `f`** is actually the `traverse` operation specialized for monads, demonstrating how theoretical concepts have practical applications.

2. **Free monads vs Tagless Final:** Both approaches are shown for building composable DSLs with different trade-offs.

3. **Type Class Coherence:** Multiple type class instances work together seamlessly thanks to Scala's implicit system.

4. **Effect Polymorphism:** The same code works with `Id`, `Option`, `Either`, `ZIO`, etc., showing the power of abstraction.

5. **Algebraic Thinking:** Problems are solved by finding the right algebraic structures and their laws.

---

## 🏆 Assignment Verification

The implementation fully satisfies the assignment requirements:

✅ **Data type D:** `Document[A]` represents subdivided documents  
✅ **Function f:** Implemented with correct type signature  
✅ **Identity law:** `f[Id](identity) = identity` ✓  
✅ **Option law:** `f[Option](Some(_)) = Some(_)` ✓  
✅ **All concepts:** Every listed topic is comprehensively demonstrated  

Run `sbt "runMain com.example.ComprehensiveExample"` to see all concepts in action!

---

## 📋 Prerequisites

- **JDK 11+:** Modern JVM runtime
- **SBT 1.5+:** Scala build tool

---

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

---

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

---

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

## Why This is Actually Cool 😎

1. **Real-world applicable:** This isn't just academic BS - you could use this for layout engines, configuration systems, or any hierarchical data
2. **Type safety:** Impossible to mess up thanks to the type system 
3. **Composable:** Everything builds on simple, composable abstractions
4. **Performant:** Immutable data structures with structural sharing
5. **Testable:** Laws and properties ensure correctness

---

## Contributing 🤝

If you want to add features or fix bugs:

1. Fork it
2. Create a feature branch (`git checkout -b cool-new-feature`)
3. Make your changes
4. Add tests (seriously, the CI will fail without them)
5. Format code (`sbt scalafmtAll`)  
6. Submit a PR

---

## What I Learned 📚

Building this taught me that functional programming isn't just academic theory - it's a practical way to build reliable, composable software. The type system catches so many bugs before they happen, and the mathematical foundations mean everything just... works.

Plus, free monads are actually pretty cool once you get past the scary names.

---

## License 📄

MIT License - use it however you want, just don't blame me if it becomes sentient.

---

*Built with ❤️, a little sarcasm, and way too much caffeine by a CS student who probably should have been studying for other exams. If you made it this far, you’re ready for anything. — Norm (sort of)*
