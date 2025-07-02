# API Documentation

This page provides comprehensive documentation for the Document Matrix API, including both the core Scala API and the HTTP REST API.

## 📚 Core Scala API

### Document ADT

The `Document[A]` type represents a hierarchical document structure that can be subdivided horizontally or vertically.

```scala
sealed trait Document[A]
case class Leaf[A](value: A) extends Document[A]
case class Horizontal[A](cells: List[Document[A]]) extends Document[A]
case class Vertical[A](cells: List[Document[A]]) extends Document[A]
case class Empty[A]() extends Document[A]
```

### Creating Documents

#### Basic Construction
```scala
import com.example.Document._

// Simple leaf document
val leaf: Document[String] = Leaf("Hello")

// Horizontal layout
val horizontal: Document[String] = Horizontal(List(
  Leaf("A"), Leaf("B"), Leaf("C")
))

// Vertical layout
val vertical: Document[String] = Vertical(List(
  Leaf("Top"),
  Horizontal(List(Leaf("Left"), Leaf("Right"))),
  Leaf("Bottom")
))

// Empty document
val empty: Document[String] = Empty()
```

#### Using Smart Constructors
```scala
// More convenient construction
val doc = Vertical(List(
  Horizontal(List(Leaf("1"), Leaf("2"))),
  Horizontal(List(Leaf("3"), Leaf("4")))
))
```

### Type Class Instances

#### Functor Operations
```scala
// Map over document values
val numbers: Document[Int] = Horizontal(List(Leaf(1), Leaf(2), Leaf(3)))
val doubled: Document[Int] = numbers.map(_ * 2)
// Result: Horizontal(List(Leaf(2), Leaf(4), Leaf(6)))

val strings: Document[String] = numbers.map(_.toString)
// Result: Horizontal(List(Leaf("1"), Leaf("2"), Leaf("3")))
```

#### Traverse Operations
```scala
import cats.syntax.all._

// Traverse with Option
val doc: Document[Int] = Horizontal(List(Leaf(1), Leaf(2)))
val result: Option[Document[String]] = doc.traverse(i => 
  if (i > 0) Some(i.toString) else None
)

// Traverse with Either for validation
def validatePositive(n: Int): Either[String, Int] = 
  if (n > 0) Right(n) else Left(s"$n is not positive")

val validated: Either[String, Document[Int]] = doc.traverse(validatePositive)
```

#### Monad Operations
```scala
// FlatMap for document transformation
val doc: Document[Int] = Leaf(5)
val expanded: Document[Int] = doc.flatMap { n =>
  Horizontal((1 to n).map(Leaf(_)).toList)
}
// Result: Horizontal(List(Leaf(1), Leaf(2), Leaf(3), Leaf(4), Leaf(5)))

// Pure for wrapping values
val pure: Document[String] = Document.pure("Hello")
// Result: Leaf("Hello")
```

### Semigroup and Monoid Operations

```scala
import cats.syntax.semigroup._

// Combining documents
val doc1: Document[String] = Horizontal(List(Leaf("A")))
val doc2: Document[String] = Horizontal(List(Leaf("B")))
val combined: Document[String] = doc1 |+| doc2
// Result: Horizontal(List(Leaf("A"), Leaf("B")))

// Monoid identity
val empty: Document[String] = Monoid[Document[String]].empty
val withIdentity: Document[String] = doc1 |+| empty
// Result: doc1 (unchanged)
```

### Catamorphism (Folding)

```scala
// Fold document structure
val doc: Document[Int] = Vertical(List(
  Horizontal(List(Leaf(1), Leaf(2))),
  Leaf(3)
))

val sum: Int = Document.cata(doc)(
  leafAlg = identity,           // Keep leaf values as-is
  horizontalAlg = _.sum,        // Sum horizontal cells
  verticalAlg = _.sum,          // Sum vertical cells
  emptyAlg = () => 0           // Empty has value 0
)
// Result: 6
```

### Anamorphism (Unfolding)

```scala
// Build document from seed
val tree: Document[Int] = Document.ana(10) { seed =>
  if (seed <= 1) Left(seed)  // Create leaf
  else Right((List(seed - 1, seed - 2), true))  // Create horizontal
}
```

### Validation

```scala
import cats.data.ValidatedNel
import cats.syntax.all._

def validateNonEmpty(s: String): ValidatedNel[String, String] =
  if (s.nonEmpty) s.validNel else "Empty string".invalidNel

val doc: Document[String] = Horizontal(List(Leaf("valid"), Leaf("")))
val validated = Document.traverse(doc)(validateNonEmpty)
// Result: Invalid(NonEmptyList("Empty string"))
```

## 🌐 HTTP REST API

The HTTP server runs on port 8081 and provides the following endpoints:

### Base URL
```
http://localhost:8081
```

### Endpoints

#### Health Check
```http
GET /health
```

**Response:**
```
Status: 200 OK
Content-Type: text/plain

Server is running
```

#### Render Document
```http
POST /render
Content-Type: application/json
```

**Request Body:**
```json
{
  "type": "vertical",
  "cells": [
    {
      "type": "horizontal",
      "cells": [
        {"type": "leaf", "value": "A"},
        {"type": "leaf", "value": "B"}
      ]
    },
    {"type": "leaf", "value": "C"}
  ]
}
```

**Response:**
```
Status: 200 OK
Content-Type: text/plain

Vertical(
  List(
    Horizontal(List(Leaf(A), Leaf(B))),
    Leaf(C)
  )
)
```

#### Validate Document
```http
POST /validate
Content-Type: application/json
```

**Request Body:** (Same as render)

**Response:**
```
Status: 200 OK
Content-Type: text/plain

Valid document
```

### JSON Schema

#### Document Types

##### Leaf Document
```json
{
  "type": "leaf",
  "value": "<any-json-value>"
}
```

##### Horizontal Document
```json
{
  "type": "horizontal",
  "cells": [
    // Array of Document objects
  ]
}
```

##### Vertical Document
```json
{
  "type": "vertical",
  "cells": [
    // Array of Document objects
  ]
}
```

##### Empty Document
```json
{
  "type": "empty"
}
```

### Example Requests

#### Simple Leaf
```bash
curl -X POST http://localhost:8081/render \
  -H "Content-Type: application/json" \
  -d '{"type":"leaf","value":"Hello World"}'
```

#### Complex Layout
```bash
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

## 🔧 Advanced APIs

### Optics (Monocle Integration)

```scala
import com.example.DocumentOptics._

// Extract leaf value
val doc: Document[String] = Leaf("Hello")
val value: Option[String] = leafPrism.getOption(doc)
// Result: Some("Hello")

// Update all leaves
val updated: Document[String] = updateAllLeaves(doc, _.toUpperCase)

// Get first leaf value
val firstLeaf: Option[String] = getFirstLeafValue(complexDoc)
```

### Free Monad DSL

```scala
import com.example.DocumentDSL._

// Build using DSL
val program = for {
  leaf <- createLeaf("Hello")
  horizontal <- createHorizontal(List(leaf))
  _ <- logDocument(horizontal)
} yield horizontal

// Run with different interpreters
val pure: Document[String] = program.foldMap(pureDSLInterpreter)
val withLogging: IO[Document[String]] = program.foldMap(ioInterpreter)
```

### Tagless Final Pattern

```scala
import com.example.DocumentAlgebras._

def buildDocument[F[_]: DocumentAlgebra: Monad]: F[Document[String]] = for {
  leaf <- DocumentAlgebra[F].leaf("content")
  layout <- DocumentAlgebra[F].horizontal(List(leaf))
} yield layout

// Use with different effect types
val pure: Id[Document[String]] = buildDocument[Id]
val async: Task[Document[String]] = buildDocument[Task]
```

## 📊 Error Handling

### Common Error Types

```scala
sealed trait DocumentError
case object EmptyDocumentError extends DocumentError
case class ParseError(message: String) extends DocumentError
case class ValidationError(field: String, reason: String) extends DocumentError
```

### HTTP Error Responses

#### 400 Bad Request
```json
{
  "error": "Invalid JSON format",
  "details": "Expected 'type' field"
}
```

#### 500 Internal Server Error
```json
{
  "error": "Processing failed",
  "details": "Unable to render document"
}
```

## 🧪 Testing Utilities

### Property-Based Testing

```scala
import org.scalacheck.Arbitrary
import com.example.DocumentPropertySpec._

// Generate arbitrary documents
implicit val arbDoc: Arbitrary[Document[Int]] = arbitraryDocument[Int]

// Test properties
property("map identity") {
  forAll { (doc: Document[Int]) =>
    doc.map(identity) shouldBe doc
  }
}
```

### Test Helpers

```scala
// Create test documents
val testDoc = Vertical(List(
  Horizontal(List(Leaf(1), Leaf(2))),
  Leaf(3)
))

// Verify structure
testDoc should matchPattern {
  case Vertical(List(Horizontal(_), Leaf(3))) =>
}
```

## 📚 See Also

- **[Examples](Examples)** - Practical usage examples
- **[Architecture Overview](Architecture-Overview)** - System design
- **[Testing Strategy](Testing-Strategy)** - How to test your code
- **[Development Guide](Development-Guide)** - Contributing guidelines

## 🚌 BlaBlaCar Bus API Integration

The Document Matrix system is designed to work seamlessly with transport APIs, particularly the BlaBlaCar Bus API, providing structured document processing for travel-related data.

### Supported Transport Data Types

#### Route Information Processing
```scala
// Processing bus route data
case class BusRoute(
  origin: String,
  destination: String, 
  departure: String,
  arrival: String,
  duration: String,
  price: BigDecimal
)

def routeToDocument(route: BusRoute): Document[String] = {
  val header = Leaf(s"${route.origin} → ${route.destination}")
  val schedule = Vertical(List(
    Leaf(s"Departure: ${route.departure}"),
    Leaf(s"Arrival: ${route.arrival}"),
    Leaf(s"Duration: ${route.duration}")
  ))
  val pricing = Leaf(s"Price: €${route.price}")
  
  Vertical(List(header, schedule, pricing))
}
```

#### Booking Data Structures
```scala
case class PassengerInfo(
  name: String,
  email: String,
  phone: String
)

case class BookingDetails(
  bookingId: String,
  route: BusRoute,
  passenger: PassengerInfo,
  seatNumber: String,
  status: String
)

def bookingToDocument(booking: BookingDetails): Document[String] = {
  val header = Leaf(s"Booking ${booking.bookingId}")
  val routeDoc = routeToDocument(booking.route)
  val passengerDoc = Vertical(List(
    Leaf(s"Passenger: ${booking.passenger.name}"),
    Leaf(s"Seat: ${booking.seatNumber}"),
    Leaf(s"Status: ${booking.status}")
  ))
  
  Vertical(List(header, routeDoc, passengerDoc))
}
```

#### Real-time Updates Integration
```scala
case class LiveUpdate(
  routeId: String,
  currentLocation: String,
  estimatedDelay: Int,
  timestamp: String
)

def liveUpdateToDocument(update: LiveUpdate): Document[String] = {
  val status = if (update.estimatedDelay > 0) "DELAYED" else "ON TIME"
  val delayInfo = if (update.estimatedDelay > 0) 
    Some(Leaf(s"Delay: ${update.estimatedDelay} minutes"))
  else None
  
  val baseInfo = List(
    Leaf(s"Route: ${update.routeId}"),
    Leaf(s"Current Location: ${update.currentLocation}"),
    Leaf(s"Status: $status"),
    Leaf(s"Last Update: ${update.timestamp}")
  )
  
  Vertical(baseInfo ++ delayInfo.toList)
}
```

### API Response Processing

#### JSON to Document Conversion
```scala
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._

def processBusApiResponse(jsonResponse: String): Either[Error, Document[String]] = {
  for {
    json <- parse(jsonResponse)
    routes <- json.as[List[BusRoute]]
  } yield {
    val routeDocs = routes.map(routeToDocument)
    if (routeDocs.nonEmpty) {
      Vertical(routeDocs)
    } else {
      Leaf("No routes found")
    }
  }
}
```

#### Error Handling for Transport APIs
```scala
sealed trait TransportApiError extends DocumentError
case class ApiTimeoutError(duration: Long) extends TransportApiError
case class RouteNotFoundError(routeId: String) extends TransportApiError
case class BookingFailedError(reason: String) extends TransportApiError

def handleTransportApiError(error: TransportApiError): Document[String] = {
  error match {
    case ApiTimeoutError(duration) =>
      Leaf(s"API request timed out after ${duration}ms")
    case RouteNotFoundError(routeId) =>
      Leaf(s"Route $routeId not found")
    case BookingFailedError(reason) =>
      Vertical(List(
        Leaf("Booking failed"),
        Leaf(s"Reason: $reason")
      ))
  }
}
```

### Geographic Data Processing
```scala
case class Location(
  latitude: Double,
  longitude: Double,
  address: String,
  city: String
)

case class BusStop(
  id: String,
  name: String,
  location: Location,
  amenities: List[String]
)

def busStopToDocument(stop: BusStop): Document[String] = {
  val header = Leaf(s"🚏 ${stop.name}")
  val location = Vertical(List(
    Leaf(s"📍 ${stop.location.address}"),
    Leaf(s"🏙️ ${stop.location.city}"),
    Leaf(s"📐 ${stop.location.latitude}, ${stop.location.longitude}")
  ))
  val amenitiesDoc = if (stop.amenities.nonEmpty) {
    val amenityList = stop.amenities.map(a => Leaf(s"• $a"))
    List(Leaf("🛠️ Amenities:"), Vertical(amenityList))
  } else {
    List(Leaf("🛠️ No amenities available"))
  }
  
  Vertical(List(header, location) ++ amenitiesDoc)
}
```

### Integration Examples

#### Complete Journey Processing
```scala
case class Journey(
  routes: List[BusRoute],
  totalDuration: String,
  totalPrice: BigDecimal,
  transfers: Int
)

def journeyToDocument(journey: Journey): Document[String] = {
  val header = Leaf("🎫 Complete Journey")
  val summary = Vertical(List(
    Leaf(s"Total Duration: ${journey.totalDuration}"),
    Leaf(s"Total Price: €${journey.totalPrice}"),
    Leaf(s"Transfers: ${journey.transfers}")
  ))
  
  val routesDocs = journey.routes.zipWithIndex.map { case (route, index) =>
    val routeHeader = Leaf(s"Leg ${index + 1}:")
    Vertical(List(routeHeader, routeToDocument(route)))
  }
  
  Vertical(List(header, summary, Leaf("---"), Vertical(routesDocs)))
}
```

#### Search Results Display
```scala
def searchResultsToDocument(
  query: String,
  results: List[BusRoute],
  totalResults: Int
): Document[String] = {
  val header = Leaf(s"🔍 Search Results for '$query'")
  val stats = Leaf(s"Found $totalResults routes (showing ${results.length})")
  
  val resultDocs = results.zipWithIndex.map { case (route, index) =>
    val routeNumber = Leaf(s"${index + 1}.")
    val routeDoc = routeToDocument(route)
    Horizontal(List(routeNumber, routeDoc))
  }
  
  if (resultDocs.nonEmpty) {
    Vertical(List(header, stats, Leaf(""), Vertical(resultDocs)))
  } else {
    Vertical(List(header, Leaf("No routes found for your search.")))
  }
}
```

### Performance Considerations for Transport Data

#### Large Dataset Processing
```scala
import zio.stream._

def processLargeRouteDataset(
  routes: ZStream[Any, Throwable, BusRoute]
): ZStream[Any, Throwable, Document[String]] = {
  routes
    .map(routeToDocument)
    .grouped(100) // Process in batches
    .map(batch => Vertical(batch.toList))
}
```

#### Caching Strategy
```scala
import zio._

trait DocumentCache {
  def get(key: String): Task[Option[Document[String]]]
  def put(key: String, doc: Document[String]): Task[Unit]
}

def cachedRouteProcessing(
  route: BusRoute,
  cache: DocumentCache
): Task[Document[String]] = {
  val cacheKey = s"route-${route.origin}-${route.destination}"
  
  cache.get(cacheKey).flatMap {
    case Some(cached) => ZIO.succeed(cached)
    case None =>
      val doc = routeToDocument(route)
      cache.put(cacheKey, doc) *> ZIO.succeed(doc)
  }
}
```
