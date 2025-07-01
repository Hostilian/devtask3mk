# Document Matrix 📊

Yo, so I had to build this crazy functional programming project for uni and honestly... it turned out way cooler than I expected. 

This thing implements a recursive document structure where you can split stuff horizontally and vertically like a spreadsheet on steroids, but with ALL the fancy FP concepts my professor loves.

## What the Hell is This? 🤔

Imagine you have a document that can be split into cells, and each cell can either:
- Hold some actual data (like "Hello World")  
- Be split again into more cells (horizontal or vertical)

It's like Russian dolls but for data structures, and it's all type-safe and mathematical and stuff.

## The Assignment ✅

Had to build a data type `D[A]` that represents documents subdivided horizontally/vertically, plus this monster function:

```scala
f[M[_]: Monad, A, B]: (A => M[B]) => D[A] => M[D[B]]
```

Where these laws hold:
- `f[Id](identity) = identity` ✓
- `f[Option](Some(_)) = Some(_)` ✓

**TL;DR:** I nailed it. The function is basically `traverse` but specialized for monads.

## Cool Stuff I Built 🚀

### The Data Type
```scala
sealed trait Document[A]
case class Leaf[A](value: A) extends Document[A]                    // Holds actual data
case class Horizontal[A](cells: List[Document[A]]) extends Document[A]  // Split left-right  
case class Vertical[A](cells: List[Document[A]]) extends Document[A]    // Split top-bottom
case class Empty[A]() extends Document[A]                          // Nothing here
```

### Every FP Concept Under the Sun
- **Algebraic Data Types:** Sum types, product types, the works
- **Recursion Schemes:** Catamorphisms for tearing down structures  
- **Higher-Kinded Types:** Functions that work with any `F[_]`
- **Type Classes:** Functor, Monad, Semigroup, you name it
- **Free Monads:** Built a whole DSL for document operations
- **Tagless Final:** Alternative approach to Free monads
- **Effect Systems:** ZIO integration, validation with cats
- **Algebras:** Different ways to render/process documents

### Real Working Examples
- **CLI Tool:** Pretty prints documents with colors and ASCII art
- **Web Server:** HTTP API for creating/manipulating documents  
- **JSON Support:** Serialize to/from JSON with Circe
- **Validation:** Type-safe error handling everywhere
- **Property Tests:** Verifies all the mathematical laws

## Quick Start 🏃‍♂️

**Prerequisites:** You need Java 11+ and SBT installed.

```bash
# Clone and run
git clone <this-repo>
cd devtask3mk

# See if it works
sbt compile
sbt test

# Check the assignment requirements
sbt "runMain com.example.AssignmentVerification"

# See everything in action  
sbt "runMain com.example.ComprehensiveExample"

# Try the CLI
sbt "runMain com.example.Cli"

# Start the web server
sbt "runMain com.example.Server"
```

## File Structure 📁

```
src/main/scala/
├── Document.scala              # Core data type + all the type class instances
├── AssignmentVerification.scala # Proves the assignment requirements work
├── ComprehensiveExample.scala   # Shows off EVERYTHING
├── SimpleExample.scala         # Basic demo of the f function
├── DocumentFree.scala          # Free monad DSL implementation  
├── DocumentAlgebras.scala      # Different algebras for processing
├── DocumentOptics.scala        # Lens/optics for deep updates
├── Cli.scala                  # Command-line interface
└── Server.scala               # HTTP server with REST API

src/test/scala/
├── DocumentSpec.scala          # Basic functionality tests
├── AdvancedDocumentSpec.scala  # All the fancy FP concept tests  
└── DocumentPropertySpec.scala  # Property-based law verification
```

## Examples That'll Blow Your Mind 🤯

### Basic Usage
```scala
// Create a document
val doc = Horizontal(List(
  Leaf("Hello"),
  Vertical(List(Leaf("World"), Leaf("!")))
))

// Transform all values 
val shouting = Document.map(doc)(_.toUpperCase)
// Result: Horizontal(List(Leaf("HELLO"), Vertical(List(Leaf("WORLD"), Leaf("!")))))

// The magical f function in action
val wrapped = Document.f[Option, String, String](Some(_))(doc)
// Result: Some(doc) - structure preserved!
```

### Free Monad DSL
```scala
val program = for {
  leaf1 <- createLeaf("Functional")
  leaf2 <- createLeaf("Programming") 
  combined <- createHorizontal(List(leaf1, leaf2))
  validated <- validateDocument(combined)
} yield validated

val result = runPure(program)  // Execute with pure interpreter
```

### Different Rendering Algebras
```scala
val doc = Horizontal(List(Leaf("Code"), Leaf("Is"), Leaf("Art")))

// ASCII rendering
render(doc)(asciiRenderer)  // Output: [Code] | [Is] | [Art]

// HTML rendering  
render(doc)(htmlRenderer)   // Output: <div class="horizontal">...</div>
```

## The Math Checks Out ✓

All the category theory laws are verified:
- **Functor Laws:** Identity and composition  
- **Monad Laws:** Left/right identity, associativity
- **Semigroup Laws:** Associativity 
- **Monoid Laws:** Identity element behavior
- **Traversal Laws:** Naturality, identity, composition

Run the tests to see for yourself: `sbt test`

## Docker Support 🐳

```bash
# Build the image
docker build -t document-matrix .

# Run CLI
docker run -it document-matrix

# Run with different entry point  
docker run -it document-matrix java -cp document-matrix_3.4.3-1.0.0.jar com.example.Server
```

## Dependencies 📦

This project uses the best libraries in the Scala ecosystem:
- **Cats:** For all the type class goodness
- **ZIO:** Modern effect system  
- **Circe:** JSON handling that doesn't suck
- **Http4s:** Functional HTTP
- **ScalaTest:** Testing framework
- **ScalaCheck:** Property-based testing

See `build.sbt` for the full list.

## Why This is Actually Cool 😎

1. **Real-world applicable:** This isn't just academic BS - you could use this for layout engines, configuration systems, or any hierarchical data
2. **Type safety:** Impossible to mess up thanks to the type system 
3. **Composable:** Everything builds on simple, composable abstractions
4. **Performant:** Immutable data structures with structural sharing
5. **Testable:** Laws and properties ensure correctness

## Contributing 🤝

If you want to add features or fix bugs:

1. Fork it
2. Create a feature branch (`git checkout -b cool-new-feature`)
3. Make your changes
4. Add tests (seriously, the CI will fail without them)
5. Format code (`sbt scalafmtAll`)  
6. Submit a PR

## What I Learned 📚

Building this taught me that functional programming isn't just academic theory - it's a practical way to build reliable, composable software. The type system catches so many bugs before they happen, and the mathematical foundations mean everything just... works.

Plus, free monads are actually pretty cool once you get past the scary names.

## License 📄

MIT License - use it however you want, just don't blame me if it becomes sentient.

---

*Built with ❤️ and way too much caffeine by a CS student who probably should have been studying for other exams.*
