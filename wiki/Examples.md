# Examples

This page provides practical examples of using the Document Matrix library for various document processing tasks.

## 🎯 Basic Examples

### Creating Simple Documents

```scala
import com.example.Document._

// Single value document
val greeting = Leaf("Hello, World!")

// Simple horizontal layout
val menu = Horizontal(List(
  Leaf("File"),
  Leaf("Edit"), 
  Leaf("View"),
  Leaf("Help")
))

// Simple vertical layout
val sidebar = Vertical(List(
  Leaf("Navigation"),
  Leaf("Recent Files"),
  Leaf("Bookmarks")
))
```

### Complex Layouts

```scala
// Dashboard layout
val dashboard = Vertical(List(
  // Header
  Horizontal(List(
    Leaf("Logo"),
    Leaf("Search"),
    Leaf("Profile")
  )),
  
  // Main content area
  Horizontal(List(
    // Sidebar
    Vertical(List(
      Leaf("Menu Item 1"),
      Leaf("Menu Item 2"),
      Leaf("Menu Item 3")
    )),
    
    // Content
    Vertical(List(
      Leaf("Page Title"),
      Horizontal(List(
        Leaf("Content Left"),
        Leaf("Content Right")
      ))
    ))
  )),
  
  // Footer
  Leaf("Footer Content")
))
```

## 🎨 Functional Operations

### Mapping and Transformation

```scala
// Transform all text to uppercase
val doc = Horizontal(List(Leaf("hello"), Leaf("world")))
val uppercase = doc.map(_.toUpperCase)
// Result: Horizontal(List(Leaf("HELLO"), Leaf("WORLD")))

// Add formatting
val formatted = doc.map(text => s"[${text}]")
// Result: Horizontal(List(Leaf("[hello]"), Leaf("[world]")))

// Transform to different type
val lengths = doc.map(_.length)
// Result: Horizontal(List(Leaf(5), Leaf(5)))
```

### Folding and Aggregation

```scala
// Count total words
val document = Vertical(List(
  Horizontal(List(Leaf("Hello"), Leaf("World"))),
  Leaf("Scala"),
  Horizontal(List(Leaf("Functional"), Leaf("Programming")))
))

val wordCount = Document.fold(document)(
  _ => 1,        // Each leaf counts as 1 word
  _.sum,         // Sum horizontal counts
  _.sum          // Sum vertical counts
)
// Result: 5

// Concatenate all text
val allText = Document.fold(document)(
  identity,           // Keep leaf text
  _.mkString(" "),    // Join horizontal with spaces
  _.mkString("\n")    // Join vertical with newlines
)
// Result: "Hello World\nScala\nFunctional Programming"
```

### Traversal with Effects

```scala
import cats.syntax.all._

// Validate all values are non-empty
def validateNonEmpty(s: String): Either[String, String] = 
  if (s.trim.nonEmpty) Right(s) else Left("Empty string found")

val doc = Horizontal(List(Leaf("Valid"), Leaf(""), Leaf("Also Valid")))
val validated = doc.traverse(validateNonEmpty)
// Result: Left("Empty string found")

// Transform with Option
def parseNumber(s: String): Option[Int] = s.toIntOption

val numberDoc = Horizontal(List(Leaf("1"), Leaf("2"), Leaf("not-a-number")))
val parsed = numberDoc.traverse(parseNumber)
// Result: None

val validNumbers = Horizontal(List(Leaf("1"), Leaf("2"), Leaf("3")))
val parsedValid = validNumbers.traverse(parseNumber)
// Result: Some(Horizontal(List(Leaf(1), Leaf(2), Leaf(3))))
```

## 🔗 Combining Documents

### Semigroup Operations

```scala
import cats.syntax.semigroup._

// Combine horizontal layouts
val menu1 = Horizontal(List(Leaf("File"), Leaf("Edit")))
val menu2 = Horizontal(List(Leaf("View"), Leaf("Help")))
val fullMenu = menu1 |+| menu2
// Result: Horizontal(List(Leaf("File"), Leaf("Edit"), Leaf("View"), Leaf("Help")))

// Combine different layouts
val header = Leaf("Header")
val content = Vertical(List(Leaf("Line 1"), Leaf("Line 2")))
val page = header |+| content
// Result: Vertical(List(Leaf("Header"), Leaf("Line 1"), Leaf("Line 2")))

// Chain multiple combines
val result = Leaf("A") |+| Leaf("B") |+| Leaf("C")
// Result: Vertical(List(Leaf("A"), Leaf("B"), Leaf("C")))
```

## 🎭 Advanced Patterns

### Free Monad DSL

```scala
import com.example.DocumentDSL._
import cats.free.Free

// Build document using DSL
val program: Free[DocumentOp, Document[String]] = for {
  title <- createLeaf("My Document")
  content <- createHorizontal(List(
    createLeaf("Left Column"),
    createLeaf("Right Column")
  ))
  document <- createVertical(List(title, content))
  _ <- logDocument(document)
} yield document

// Run with pure interpreter
val result = runPure(program)

// Run with IO effects
val ioResult = runIO(program)
```

### Tagless Final Pattern

```scala
import com.example.DocumentAlgebras._

def createReport[F[_]: Monad](implicit D: DocumentAlgebra[F]): F[Document[String]] = 
  for {
    title <- D.leaf("Quarterly Report")
    stats <- D.horizontal(List(
      D.leaf("Revenue: $1M"),
      D.leaf("Growth: 15%")
    ))
    chart <- D.leaf("📊 Chart Data")
    report <- D.vertical(List(title, stats, chart))
  } yield report

// Use with different effect types
val pureReport: Document[String] = createReport[Id]
val asyncReport: Task[Document[String]] = createReport[Task]
```

### Optics for Deep Updates

```scala
import com.example.DocumentOptics._

val complexDoc = Vertical(List(
  Horizontal(List(Leaf("A"), Leaf("B"))),
  Leaf("C"),
  Horizontal(List(Leaf("D"), Leaf("E")))
))

// Update all leaf values
val uppercased = updateAllLeaves(complexDoc, _.toUpperCase)

// Find first leaf value
val firstValue = getFirstLeafValue(complexDoc)
// Result: Some("A")

// Count total leaves
val leafCount = countLeaves(complexDoc)
// Result: 5

// Transform specific path
val updated = transformAtPath(complexDoc, List(0, 1), _.toLowerCase)
// Updates the "B" to "b"
```

## 🌐 HTTP API Examples

### Using curl

```bash
# Simple leaf document
curl -X POST http://localhost:8081/render \
  -H "Content-Type: application/json" \
  -d '{"type":"leaf","value":"Hello World"}'

# Complex nested structure
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
      {"type": "leaf", "value": "Bottom Center"}
    ]
  }'

# Health check
curl http://localhost:8081/health
```

### Using Scala HTTP client

```scala
import sttp.client3._
import io.circe.syntax._

val backend = HttpURLConnectionBackend()

// Create document JSON
val doc = Vertical(List(
  Horizontal(List(Leaf("A"), Leaf("B"))),
  Leaf("C")
))

val request = basicRequest
  .post(uri"http://localhost:8081/render")
  .header("Content-Type", "application/json")
  .body(doc.asJson.noSpaces)

val response = request.send(backend)
println(response.body)
```

## 🧪 Testing Examples

### Unit Testing

```scala
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DocumentExampleSpec extends AnyFlatSpec with Matchers {
  
  "Document mapping" should "transform all values" in {
    val doc = Horizontal(List(Leaf(1), Leaf(2), Leaf(3)))
    val doubled = doc.map(_ * 2)
    
    doubled shouldBe Horizontal(List(Leaf(2), Leaf(4), Leaf(6)))
  }
  
  "Document combining" should "merge horizontal layouts" in {
    val left = Horizontal(List(Leaf("A")))
    val right = Horizontal(List(Leaf("B")))
    val combined = left |+| right
    
    combined shouldBe Horizontal(List(Leaf("A"), Leaf("B")))
  }
}
```

### Property-Based Testing

```scala
import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class DocumentPropertyExamples extends AnyFlatSpec 
  with ScalaCheckPropertyChecks with Matchers {
  
  implicit val arbDoc: Arbitrary[Document[Int]] = Arbitrary(
    Gen.oneOf(
      Gen.choose(1, 100).map(Leaf(_)),
      Gen.listOf(Leaf(1)).map(Horizontal(_)),
      Gen.listOf(Leaf(1)).map(Vertical(_))
    )
  )
  
  "Document functor" should "satisfy identity law" in {
    forAll { (doc: Document[Int]) =>
      doc.map(identity) shouldBe doc
    }
  }
  
  "Document semigroup" should "be associative" in {
    forAll { (a: Document[Int], b: Document[Int], c: Document[Int]) =>
      (a |+| b) |+| c shouldBe a |+| (b |+| c)
    }
  }
}
```

## 🎪 Real-World Use Cases

### Document Layout Engine

```scala
case class Page(header: String, content: List[String], footer: String)

def layoutPage(page: Page): Document[String] = {
  val headerDoc = Leaf(page.header)
  val contentDoc = Vertical(page.content.map(Leaf(_)))
  val footerDoc = Leaf(page.footer)
  
  Vertical(List(headerDoc, contentDoc, footerDoc))
}

val blogPost = Page(
  header = "My Blog Post",
  content = List(
    "This is the introduction paragraph.",
    "This is the main content section.",
    "This is the conclusion."
  ),
  footer = "© 2025 My Blog"
)

val layout = layoutPage(blogPost)
```

### Configuration DSL

```scala
case class AppConfig(
  database: DatabaseConfig,
  server: ServerConfig,
  logging: LoggingConfig
)

case class DatabaseConfig(host: String, port: Int, name: String)
case class ServerConfig(port: Int, host: String)
case class LoggingConfig(level: String, file: String)

def configToDocument(config: AppConfig): Document[String] = Vertical(List(
  Leaf("Application Configuration"),
  Horizontal(List(
    Vertical(List(
      Leaf("Database"),
      Leaf(s"Host: ${config.database.host}"),
      Leaf(s"Port: ${config.database.port}"),
      Leaf(s"Name: ${config.database.name}")
    )),
    Vertical(List(
      Leaf("Server"),
      Leaf(s"Host: ${config.server.host}"),
      Leaf(s"Port: ${config.server.port}")
    )),
    Vertical(List(
      Leaf("Logging"),
      Leaf(s"Level: ${config.logging.level}"),
      Leaf(s"File: ${config.logging.file}")
    ))
  ))
))
```

### Report Generation

```scala
case class SalesReport(
  period: String,
  totalSales: Double,
  topProducts: List[String],
  regions: Map[String, Double]
)

def generateSalesReport(report: SalesReport): Document[String] = {
  val header = Leaf(s"Sales Report - ${report.period}")
  
  val summary = Horizontal(List(
    Leaf(s"Total Sales: $${report.totalSales}"),
    Leaf(s"Products Sold: ${report.topProducts.length}")
  ))
  
  val products = Vertical(
    Leaf("Top Products") :: report.topProducts.map(Leaf(_))
  )
  
  val regions = Vertical(
    Leaf("Sales by Region") :: 
    report.regions.map { case (region, sales) => 
      Leaf(s"$region: $$$sales") 
    }.toList
  )
  
  Vertical(List(
    header,
    summary,
    Horizontal(List(products, regions))
  ))
}
```

## 🚀 Performance Tips

### Efficient Document Construction

```scala
// Prefer building documents bottom-up
val efficientDoc = {
  val leaves = (1 to 100).map(Leaf(_)).toList
  val chunks = leaves.grouped(10).map(Horizontal(_)).toList
  Vertical(chunks)
}

// Avoid deep recursion with large structures
def buildLargeDoc(items: List[String]): Document[String] = {
  val leaves = items.map(Leaf(_))
  val rows = leaves.grouped(5).map(Horizontal(_)).toList
  Vertical(rows)
}
```

### Memory-Efficient Operations

```scala
// Use traverse for effectful operations instead of map + sequence
val doc = Horizontal(List(Leaf("1"), Leaf("2"), Leaf("3")))

// Good: Single pass
val parsed = doc.traverse(_.toIntOption)

// Avoid: Two passes
val inefficient = doc.map(_.toIntOption).sequence
```

## 📚 See Also

- **[API Documentation](API-Documentation)** - Complete API reference
- **[Architecture Overview](Architecture-Overview)** - System design
- **[Testing Strategy](Testing-Strategy)** - Testing approaches
- **[Development Guide](Development-Guide)** - Contributing guidelines
