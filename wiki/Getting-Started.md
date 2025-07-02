# Getting Started

This guide will help you set up the Document Matrix project on your local machine for development and testing.

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

### Required
- **Java 21** (Eclipse Temurin recommended)
- **SBT 1.11.0+** (Scala Build Tool)
- **Git** for version control

### Optional but Recommended
- **Docker** for containerized development
- **VS Code** with Metals extension for Scala development
- **IntelliJ IDEA** with Scala plugin

## 🔧 Installation

### 1. Clone the Repository
```bash
git clone https://github.com/Hostilian/devtask3mk.git
cd devtask3mk
```

### 2. Verify Java Installation
```bash
java -version
# Should show Java 21.x.x
```

### 3. Install SBT (if not already installed)

#### On Windows (using Chocolatey):
```powershell
choco install sbt
```

#### On macOS (using Homebrew):
```bash
brew install sbt
```

#### On Linux (using package manager):
```bash
# Ubuntu/Debian
sudo apt install sbt

# Fedora/CentOS
sudo dnf install sbt
```

### 4. Compile the Project
```bash
sbt compile
```

### 5. Run Tests
```bash
sbt test
```

## 🚀 Quick Start Commands

### Development Workflow
```bash
# Clean build
sbt clean compile

# Run all tests
sbt test

# Run specific test suite
sbt "testOnly com.example.DocumentSpec"

# Run with detailed output
sbt "test; testOnly * -- -oF"

# Start SBT console for interactive development
sbt console

# Package the application
sbt package
```

### Running the Server
```bash
# Start the HTTP server
sbt "runMain com.example.Server"

# Or use the convenience script
./dev.sh  # On Unix/macOS
./dev.bat # On Windows
```

The server will start on `http://localhost:8081`

### Using Docker
```bash
# Build Docker image
docker build -t document-matrix .

# Run with Docker Compose
docker-compose up

# Run in background
docker-compose up -d
```

## 🧪 Testing Your Setup

### 1. Basic Compilation Test
```bash
sbt compile
```
Should complete without errors.

### 2. Run Unit Tests
```bash
sbt "testOnly com.example.DocumentSpec"
```
All tests should pass.

### 3. Test HTTP Server
```bash
# Start the server
sbt "runMain com.example.Server"

# In another terminal, test the health endpoint
curl http://localhost:8081/health
# Should return: "Server is running"
```

### 4. Test Document API
```bash
# Test document rendering
curl -X POST http://localhost:8081/render \
  -H "Content-Type: application/json" \
  -d '{"type":"leaf","value":"Hello World"}'
```

## 🔍 Project Structure Overview

```
devtask3mk/
├── src/main/scala/           # Main source code
│   ├── Document.scala        # Core document ADT
│   ├── Server.scala         # HTTP server
│   └── Cli.scala           # CLI utilities
├── src/test/scala/          # Test suites
├── project/                 # SBT configuration
├── .github/workflows/       # CI/CD pipelines
├── docs/                   # Documentation
└── wiki/                   # This wiki
```

## 🛠️ IDE Setup

### VS Code with Metals
1. Install the [Metals extension](https://marketplace.visualstudio.com/items?itemName=scalameta.metals)
2. Open the project folder
3. Metals will automatically import the SBT build
4. Use `Ctrl+Shift+P` → "Metals: Import build" if needed

### IntelliJ IDEA
1. Install the Scala plugin
2. Open → Import Project → Select the project folder
3. Choose "Import project from external model" → SBT
4. Click "Finish"

## 📚 Next Steps

Once you have the project running:

1. **Explore the Code**: Start with `src/main/scala/Document.scala`
2. **Read the [API Documentation](API-Documentation)**
3. **Check out [Examples](Examples)** for common use cases
4. **Understand the [Architecture](Architecture-Overview)**
5. **Learn about [Testing Strategy](Testing-Strategy)**

## 🐛 Troubleshooting

### Common Issues

#### SBT Build Fails
```bash
# Clear SBT cache
rm -rf ~/.sbt
rm -rf target project/target

# Re-run compilation
sbt clean compile
```

#### Tests Fail
```bash
# Run tests with verbose output
sbt "testOnly * -- -oF"

# Run specific failing test
sbt "testOnly com.example.DocumentSpec -- -oF"
```

#### Server Won't Start
```bash
# Check if port 8081 is already in use
netstat -an | grep 8081

# Kill process using the port (Unix/macOS)
lsof -ti:8081 | xargs kill -9

# On Windows
netstat -ano | findstr 8081
taskkill /PID <PID> /F
```

### Performance Issues
```bash
# Increase SBT memory
export SBT_OPTS="-Xmx2G -XX:+UseG1GC"
sbt
```

## 📞 Getting Help

- **Issues**: Report bugs on [GitHub Issues](https://github.com/Hostilian/devtask3mk/issues)
- **Discussions**: Use [GitHub Discussions](https://github.com/Hostilian/devtask3mk/discussions)
- **Documentation**: Check other [Wiki pages](Home)
- **Examples**: See the [Examples page](Examples)

## 🚌 BlaBlaCar Bus API Integration

The Document Matrix project is designed to work seamlessly with transport APIs, particularly the BlaBlaCar Bus API. This section will guide you through setting up and testing the transport API integration.

### Setting Up API Integration

#### 1. Environment Configuration
Create a `.env` file in your project root (don't commit this to git):

```bash
# .env
BLABLACAR_API_KEY=your_api_key_here
BLABLACAR_BASE_URL=https://api.blablacar.com/v1
TRANSPORT_API_TIMEOUT=30000
CACHE_ENABLED=true
CACHE_TTL=300
```

#### 2. Add to .gitignore
Ensure your `.gitignore` includes:
```
.env
*.env
secrets/
```

#### 3. Install Additional Dependencies
Add to your `build.sbt` if working with HTTP APIs:
```scala
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-client" % "0.23.x",
  "org.http4s" %% "http4s-circe" % "0.23.x",
  "io.circe" %% "circe-generic" % "0.14.x",
  "io.circe" %% "circe-parser" % "0.14.x"
)
```

### Testing Transport API Integration

#### 1. Test Basic Route Processing
```scala
// In sbt console
import Document._

// Sample route data structure
case class BusRoute(
  origin: String,
  destination: String,
  departure: String,
  arrival: String,
  price: BigDecimal
)

// Create a sample route
val sampleRoute = BusRoute(
  origin = "Paris Bercy",
  destination = "Lyon Part-Dieu",
  departure = "08:30",
  arrival = "12:45", 
  price = BigDecimal("25.99")
)

// Convert to document
def routeToDocument(route: BusRoute): Document = {
  val header = Line(s"${route.origin} → ${route.destination}")
  val schedule = Vertical(List(
    Line(s"Departure: ${route.departure}"),
    Line(s"Arrival: ${route.arrival}")
  ))
  val pricing = Line(s"Price: €${route.price}")
  
  Vertical(List(header, schedule, pricing))
}

val routeDoc = routeToDocument(sampleRoute)
println(routeDoc.prettyPrint)
```

#### 2. Test Search Results Display
```scala
// Test multiple routes display
val routes = List(
  sampleRoute,
  sampleRoute.copy(departure = "14:30", arrival = "18:45", price = BigDecimal("28.99")),
  sampleRoute.copy(departure = "20:00", arrival = "00:15", price = BigDecimal("22.99"))
)

def displaySearchResults(routes: List[BusRoute]): Document = {
  val header = Line("🔍 Available Routes")
  val routeList = routes.zipWithIndex.map { case (route, index) =>
    val number = Line(s"${index + 1}.")
    val routeDoc = routeToDocument(route)
    Horizontal(List(number, routeDoc))
  }
  
  Vertical(List(header, Line("")) ++ routeList)
}

val searchResults = displaySearchResults(routes)
println(searchResults.prettyPrint)
```

#### 3. Test Error Handling
```scala
// Test API error scenarios
sealed trait ApiError
case class RouteNotFound(routeId: String) extends ApiError
case object NetworkTimeout extends ApiError
case class InvalidRequest(message: String) extends ApiError

def handleApiError(error: ApiError): Document = {
  error match {
    case RouteNotFound(id) =>
      Vertical(List(
        Line("❌ Route Not Found"),
        Line(s"Route ID: $id"),
        Line("Please try a different route.")
      ))
    case NetworkTimeout =>
      Vertical(List(
        Line("🌐 Connection Timeout"),
        Line("Please check your connection and try again.")
      ))
    case InvalidRequest(msg) =>
      Vertical(List(
        Line("⚠️ Invalid Request"),
        Line(msg)
      ))
  }
}

val errorDoc = handleApiError(RouteNotFound("RT123"))
println(errorDoc.prettyPrint)
```

### HTTP API Testing

#### 1. Test Document Endpoints with Transport Data
```bash
# Test basic document creation
curl -X POST http://localhost:8081/document \
  -H "Content-Type: application/json" \
  -d '{
    "type": "vertical",
    "content": [
      {
        "type": "line", 
        "content": "Paris → Lyon"
      },
      {
        "type": "line",
        "content": "Departure: 08:30"
      }
    ]
  }'
```

#### 2. Test Route Processing Endpoint (if implemented)
```bash
# Test route data processing
curl -X POST http://localhost:8081/route \
  -H "Content-Type: application/json" \
  -d '{
    "origin": "Paris Bercy",
    "destination": "Lyon Part-Dieu",
    "departure": "08:30",
    "arrival": "12:45",
    "price": 25.99
  }'
```

### Integration Testing

#### 1. Property-Based Testing for Transport Data
```scala
// Add to your test suite
import org.scalacheck._

// Generator for valid route data
val genRoute: Gen[BusRoute] = for {
  origin <- Gen.alphaStr.suchThat(_.nonEmpty)
  destination <- Gen.alphaStr.suchThat(_.nonEmpty)
  departure <- Gen.choose(0, 23).map(h => f"$h%02d:00")
  arrival <- Gen.choose(0, 23).map(h => f"$h%02d:00") 
  price <- Gen.choose(10, 100).map(BigDecimal(_))
} yield BusRoute(origin, destination, departure, arrival, price)

// Property: Converting route to document should never fail
property("route to document conversion") {
  forAll(genRoute) { route =>
    val doc = routeToDocument(route)
    doc match {
      case _: Vertical => true
      case _ => false
    }
  }
}
```

#### 2. Integration Test with Mock API
```scala
// Create a mock transport API for testing
class MockTransportApi {
  def searchRoutes(origin: String, destination: String): List[BusRoute] = {
    List(
      BusRoute(origin, destination, "08:00", "12:00", BigDecimal("25.00")),
      BusRoute(origin, destination, "14:00", "18:00", BigDecimal("28.00"))
    )
  }
}

// Test full integration
val mockApi = new MockTransportApi()
val routes = mockApi.searchRoutes("Paris", "Lyon")
val resultsDoc = displaySearchResults(routes)

// Verify the structure
assert(resultsDoc.isInstanceOf[Vertical])
```

### Performance Testing with Transport Data

#### 1. Test Large Dataset Processing
```scala
// Generate large number of routes for performance testing
val largeRouteSet = (1 to 1000).map { i =>
  BusRoute(
    s"City$i",
    s"City${i+1}", 
    "08:00",
    "12:00",
    BigDecimal(20 + (i % 50))
  )
}.toList

// Time the document generation
val startTime = System.currentTimeMillis()
val largeResultsDoc = displaySearchResults(largeRouteSet)
val endTime = System.currentTimeMillis()

println(s"Processing ${largeRouteSet.length} routes took ${endTime - startTime}ms")
```

#### 2. Memory Usage Testing
```scala
// Test memory efficiency with repeated operations
def testMemoryUsage(): Unit = {
  val routes = (1 to 100).map { i =>
    BusRoute(s"Origin$i", s"Dest$i", "08:00", "12:00", BigDecimal("25.00"))
  }.toList
  
  // Process multiple times to test for memory leaks
  (1 to 1000).foreach { _ =>
    val doc = displaySearchResults(routes)
    // Force garbage collection periodically
    if (_ % 100 == 0) System.gc()
  }
}

testMemoryUsage()
```

### Troubleshooting Transport API Integration

#### Common Issues

1. **API Key Issues**
   ```bash
   # Verify environment variables are loaded
   echo $BLABLACAR_API_KEY
   
   # Test API connectivity
   curl -H "Authorization: Bearer $BLABLACAR_API_KEY" \
        https://api.blablacar.com/v1/health
   ```

2. **Network Timeouts**
   ```scala
   // Increase timeout in configuration
   val config = ApiConfig(
     baseUrl = "https://api.blablacar.com/v1",
     timeout = 60000, // 60 seconds
     retries = 3
   )
   ```

3. **Rate Limiting**
   ```scala
   // Implement retry with backoff
   import scala.concurrent.duration._
   
   def withRetry[A](operation: => A, maxRetries: Int = 3): A = {
     try {
       operation
     } catch {
       case _: RateLimitException if maxRetries > 0 =>
         Thread.sleep(1000) // Wait 1 second
         withRetry(operation, maxRetries - 1)
     }
   }
   ```

### Next Steps for Transport Integration

1. **Implement Real API Client**: Create actual HTTP client for BlaBlaCar API
2. **Add Caching Layer**: Implement Redis or in-memory caching for API responses
3. **Error Recovery**: Add circuit breaker pattern for API failures
4. **Monitoring**: Add metrics and logging for API calls
5. **Documentation**: Create API documentation for transport endpoints
