# BlaBlaCar Bus API Integration

This page provides comprehensive documentation for integrating the Document Matrix library with the BlaBlaCar Bus API and other transport data processing scenarios.

## 🚌 Overview

The Document Matrix library is specifically designed to handle transport data, with first-class support for processing BlaBlaCar Bus API responses, route information, booking data, and real-time updates. The library's functional programming approach provides robust, testable, and composable solutions for transport data processing.

## 🎯 Key Use Cases

### Route Search and Display
- Process search results from BlaBlaCar Bus API
- Format route information for user display
- Handle multiple transport operators
- Display pricing and availability information

### Booking Management
- Process booking confirmations
- Handle passenger information
- Display ticket details and QR codes
- Manage booking status updates

### Real-time Updates
- Process live bus tracking data
- Display delay information
- Show current location and ETA
- Handle service disruptions

### Journey Planning
- Multi-leg journey processing
- Transfer time calculations
- Total cost and duration summaries
- Alternative route suggestions

## 🏗️ Core Data Models

### Transport Route Information

```scala
import java.time.LocalDateTime
import java.math.BigDecimal

case class BusRoute(
  id: String,
  origin: BusStop,
  destination: BusStop,
  departureTime: LocalDateTime,
  arrivalTime: LocalDateTime,
  duration: Duration,
  price: BigDecimal,
  currency: String = "EUR",
  availableSeats: Int,
  totalSeats: Int,
  operator: BusOperator,
  amenities: List[String] = List.empty,
  vehicleInfo: Option[VehicleInfo] = None
)

case class BusStop(
  id: String,
  name: String,
  address: String,
  city: String,
  country: String,
  coordinates: Option[Coordinates] = None,
  facilities: List[String] = List.empty
)

case class BusOperator(
  id: String,
  name: String,
  logo: Option[String] = None,
  rating: Option[Double] = None
)

case class Coordinates(
  latitude: Double,
  longitude: Double
)

case class VehicleInfo(
  model: String,
  features: List[String],
  accessibility: Boolean = false
)
```

### Booking and Passenger Data

```scala
case class Booking(
  confirmationNumber: String,
  route: BusRoute,
  passengers: List[Passenger],
  bookingDate: LocalDateTime,
  totalPrice: BigDecimal,
  status: BookingStatus,
  paymentInfo: PaymentInfo,
  tickets: List[Ticket]
)

case class Passenger(
  firstName: String,
  lastName: String,
  email: String,
  phone: Option[String] = None,
  dateOfBirth: Option[LocalDate] = None,
  documentNumber: Option[String] = None
)

sealed trait BookingStatus
case object Confirmed extends BookingStatus
case object Pending extends BookingStatus
case object Cancelled extends BookingStatus
case object CheckedIn extends BookingStatus

case class PaymentInfo(
  method: String,
  transactionId: String,
  amount: BigDecimal,
  currency: String,
  status: String
)

case class Ticket(
  ticketNumber: String,
  passenger: Passenger,
  seatNumber: Option[String],
  qrCode: String,
  isValid: Boolean = true
)
```

### Real-time Updates

```scala
case class LiveUpdate(
  routeId: String,
  serviceDate: LocalDate,
  currentLocation: Option[String] = None,
  nextStop: Option[BusStop] = None,
  estimatedArrival: Option[LocalDateTime] = None,
  delay: Duration = Duration.ZERO,
  status: ServiceStatus,
  occupancy: OccupancyLevel,
  lastUpdateTime: LocalDateTime
)

sealed trait ServiceStatus
case object OnTime extends ServiceStatus
case object Delayed extends ServiceStatus
case object Cancelled extends ServiceStatus
case object Diverted extends ServiceStatus

sealed trait OccupancyLevel
case object Low extends OccupancyLevel     // < 30%
case object Medium extends OccupancyLevel  // 30-70%
case object High extends OccupancyLevel    // 70-90%
case object Full extends OccupancyLevel    // > 90%
```

## 📄 Document Processing Functions

### Route Display

```scala
import Document._
import java.time.format.DateTimeFormatter

def routeToDocument(route: BusRoute): Document[String] = {
  val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
  val dateFormatter = DateTimeFormatter.ofPattern("MMM d")
  
  val routeHeader = Leaf(s"🚌 ${route.origin.city} → ${route.destination.city}")
  
  val timing = Horizontal(List(
    Leaf(s"🕐 ${route.departureTime.format(timeFormatter)}"),
    Leaf("→"),
    Leaf(s"🕐 ${route.arrivalTime.format(timeFormatter)}"),
    Leaf(s"(${formatDuration(route.duration)})")
  ))
  
  val pricing = Horizontal(List(
    Leaf(s"💰 ${route.price} ${route.currency}"),
    Leaf(s"🪑 ${route.availableSeats}/${route.totalSeats} seats")
  ))
  
  val operator = Horizontal(List(
    Leaf(s"🚍 ${route.operator.name}"),
    route.operator.rating.map(r => Leaf(s"⭐ $r")).getOrElse(Empty)
  ))
  
  val amenitiesDoc = if (route.amenities.nonEmpty) {
    val amenityIcons = route.amenities.map {
      case "wifi" => "📶"
      case "power" => "🔌"
      case "ac" => "❄️"
      case "toilet" => "🚻"
      case _ => "✓"
    }
    List(Leaf(s"🛠️ ${amenityIcons.mkString(" ")}"))
  } else List.empty
  
  Vertical(List(routeHeader, timing, pricing, operator) ++ amenitiesDoc)
}

def formatDuration(duration: Duration): String = {
  val hours = duration.toHours
  val minutes = duration.toMinutes % 60
  if (hours > 0) s"${hours}h ${minutes}m" else s"${minutes}m"
}
```

### Search Results Display

```scala
case class SearchCriteria(
  origin: String,
  destination: String,
  departureDate: LocalDate,
  passengers: Int = 1,
  flexibleDates: Boolean = false
)

def searchResultsToDocument(
  criteria: SearchCriteria,
  routes: List[BusRoute],
  totalResults: Int
): Document[String] = {
  val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
  
  val header = Vertical(List(
    Leaf("🔍 BlaBlaCar Bus Search Results"),
    Leaf(s"${criteria.origin} → ${criteria.destination}"),
    Leaf(s"📅 ${criteria.departureDate.format(dateFormatter)}"),
    Leaf(s"👥 ${criteria.passengers} passenger(s)")
  ))
  
  val stats = Horizontal(List(
    Leaf(s"📊 Showing ${routes.length} of $totalResults routes"),
    if (routes.length < totalResults) Leaf("(scroll for more)") else Empty
  ))
  
  val separator = Leaf("─" * 60)
  
  if (routes.isEmpty) {
    Vertical(List(
      header,
      separator,
      Leaf("😔 No routes found for your search"),
      Leaf("💡 Try adjusting your dates or destinations")
    ))
  } else {
    val routeList = routes.zipWithIndex.map { case (route, index) =>
      val routeNumber = Leaf(s"${index + 1}.")
      val routeDetails = routeToDocument(route)
      val bookButton = Leaf("🎫 [Book Now]")
      
      Vertical(List(
        Horizontal(List(routeNumber, routeDetails)),
        Horizontal(List(Empty, bookButton)),
        Leaf("") // spacing
      ))
    }
    
    Vertical(List(header, stats, separator) ++ routeList.init :+ routeList.last.copy(content = routeList.last.content.init))
  }
}
```

### Booking Confirmation

```scala
def bookingConfirmationToDocument(booking: Booking): Document[String] = {
  val header = Vertical(List(
    Leaf("✅ Booking Confirmed!"),
    Leaf(s"📄 Confirmation: ${booking.confirmationNumber}"),
    Leaf("")
  ))
  
  val tripDetails = Vertical(List(
    Leaf("🚌 Trip Details"),
    routeToDocument(booking.route),
    Leaf("")
  ))
  
  val passengerInfo = Vertical(List(
    Leaf("👥 Passengers"),
    Vertical(booking.passengers.zipWithIndex.map { case (passenger, index) =>
      val ticket = booking.tickets.find(_.passenger == passenger)
      val seatInfo = ticket.flatMap(_.seatNumber).map(s => s" (Seat $s)").getOrElse("")
      Leaf(s"${index + 1}. ${passenger.firstName} ${passenger.lastName}$seatInfo")
    }),
    Leaf("")
  ))
  
  val paymentInfo = Vertical(List(
    Leaf("💳 Payment Details"),
    Horizontal(List(
      Leaf(s"Total: ${booking.totalPrice} EUR"),
      Leaf(s"Status: ${booking.paymentInfo.status}")
    )),
    Leaf(s"Transaction: ${booking.paymentInfo.transactionId}"),
    Leaf("")
  ))
  
  val instructions = Vertical(List(
    Leaf("📋 Important Information"),
    Leaf("• Arrive 15 minutes before departure"),
    Leaf("• Bring valid photo ID"),
    Leaf("• Show this confirmation or your e-ticket"),
    Leaf("• Download the BlaBlaCar app for updates"),
    Leaf(""),
    Leaf("📞 Need help? Contact: support@blablacar.com")
  ))
  
  Vertical(List(header, tripDetails, passengerInfo, paymentInfo, instructions))
}
```

### Live Tracking Dashboard

```scala
def liveTrackingToDocument(updates: List[LiveUpdate]): Document[String] = {
  val currentTime = LocalDateTime.now()
  val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
  
  val header = Vertical(List(
    Leaf("📡 Live Bus Tracking"),
    Leaf(s"🕐 Last updated: ${currentTime.format(timeFormatter)}"),
    Leaf("")
  ))
  
  if (updates.isEmpty) {
    Vertical(List(
      header,
      Leaf("📭 No live updates available"),
      Leaf("Check back later for real-time information")
    ))
  } else {
    val updatesList = updates.map { update =>
      val statusIcon = update.status match {
        case OnTime => "🟢"
        case Delayed => "🟡"
        case Cancelled => "🔴"
        case Diverted => "🟠"
      }
      
      val occupancyIcon = update.occupancy match {
        case Low => "🟢"
        case Medium => "🟡"
        case High => "🟠"
        case Full => "🔴"
      }
      
      val delayText = if (update.delay.toMinutes > 0) {
        s" (+${update.delay.toMinutes}min)"
      } else ""
      
      val locationText = update.currentLocation.getOrElse("Location unknown")
      val nextStopText = update.nextStop.map(_.name).getOrElse("Final destination")
      
      Vertical(List(
        Horizontal(List(
          Leaf(statusIcon),
          Leaf(s"Route ${update.routeId}$delayText")
        )),
        Leaf(s"📍 Currently: $locationText"),
        Leaf(s"🎯 Next: $nextStopText"),
        Horizontal(List(
          update.estimatedArrival.map(eta => 
            Leaf(s"🕐 ETA: ${eta.format(DateTimeFormatter.ofPattern("HH:mm"))}")
          ).getOrElse(Empty),
          Leaf(occupancyIcon),
          Leaf(s"${update.occupancy.toString.toLowerCase} occupancy")
        )),
        Leaf("") // spacing
      ))
    }
    
    Vertical(List(header) ++ updatesList)
  }
}
```

## 🔌 API Integration Examples

### HTTP Client Integration

```scala
import zio._
import zio.http._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._

trait BlaBlaBusApiClient {
  def searchRoutes(criteria: SearchCriteria): Task[List[BusRoute]]
  def getRoute(routeId: String): Task[Option[BusRoute]]
  def createBooking(bookingRequest: BookingRequest): Task[Booking]
  def getLiveUpdates(routeIds: List[String]): Task[List[LiveUpdate]]
}

class BlaBlaBusApiClientImpl(
  baseUrl: String,
  apiKey: String,
  client: Client
) extends BlaBlaBusApiClient {
  
  def searchRoutes(criteria: SearchCriteria): Task[List[BusRoute]] = {
    val url = s"$baseUrl/routes/search"
    val params = Map(
      "origin" -> criteria.origin,
      "destination" -> criteria.destination,
      "departure_date" -> criteria.departureDate.toString,
      "passengers" -> criteria.passengers.toString
    )
    
    for {
      response <- client.get(url, headers = authHeaders, params = params)
      json <- ZIO.fromEither(parse(response.body))
      routes <- ZIO.fromEither(json.hcursor.downField("routes").as[List[BusRoute]])
    } yield routes
  }
  
  def getRoute(routeId: String): Task[Option[BusRoute]] = {
    val url = s"$baseUrl/routes/$routeId"
    
    client.get(url, headers = authHeaders)
      .flatMap { response =>
        if (response.status.code == 404) {
          ZIO.succeed(None)
        } else {
          for {
            json <- ZIO.fromEither(parse(response.body))
            route <- ZIO.fromEither(json.as[BusRoute])
          } yield Some(route)
        }
      }
      .catchAll {
        case _: DecodingFailure => ZIO.succeed(None)
        case error => ZIO.fail(error)
      }
  }
  
  private val authHeaders = Map("Authorization" -> s"Bearer $apiKey")
}
```

### Error Handling

```scala
sealed trait BusApiError extends Throwable {
  def message: String
  def toDocument: Document[String]
}

case class RouteNotFoundError(routeId: String) extends BusApiError {
  def message = s"Route $routeId not found"
  def toDocument = Vertical(List(
    Leaf("❌ Route Not Found"),
    Leaf(s"Route ID: $routeId"),
    Leaf("Please verify the route details and try again.")
  ))
}

case class BookingFailedError(reason: String, code: Option[String] = None) extends BusApiError {
  def message = s"Booking failed: $reason"
  def toDocument = Vertical(List(
    Leaf("⚠️ Booking Failed"),
    code.map(c => Leaf(s"Error Code: $c")).getOrElse(Empty),
    Leaf(s"Reason: $reason"),
    Leaf("Please contact customer support if this persists.")
  ))
}

case class NetworkTimeoutError(duration: Duration) extends BusApiError {
  def message = s"Request timed out after ${duration.toSeconds} seconds"
  def toDocument = Vertical(List(
    Leaf("🌐 Connection Timeout"),
    Leaf("Unable to reach BlaBlaCar servers"),
    Leaf("Please check your internet connection and try again.")
  ))
}

case class RateLimitExceededError(retryAfter: Option[Duration] = None) extends BusApiError {
  def message = "API rate limit exceeded"
  def toDocument = {
    val retryMessage = retryAfter
      .map(d => s"Please try again in ${d.toMinutes} minutes.")
      .getOrElse("Please try again later.")
    
    Vertical(List(
      Leaf("⏱️ Rate Limit Exceeded"),
      Leaf("Too many requests to the API"),
      Leaf(retryMessage)
    ))
  }
}

def handleBusApiError(error: BusApiError): Document[String] = {
  error.toDocument
}
```

### Caching Strategy

```scala
import zio.cache._

trait RouteCache {
  def getRoute(routeId: String): Task[Option[BusRoute]]
  def putRoute(routeId: String, route: BusRoute): Task[Unit]
  def getSearchResults(criteria: SearchCriteria): Task[Option[List[BusRoute]]]
  def putSearchResults(criteria: SearchCriteria, routes: List[BusRoute]): Task[Unit]
}

class RouteCacheImpl(cache: Cache[String, Any]) extends RouteCache {
  
  def getRoute(routeId: String): Task[Option[BusRoute]] = {
    cache.get(s"route:$routeId").map(_.asInstanceOf[Option[BusRoute]])
  }
  
  def putRoute(routeId: String, route: BusRoute): Task[Unit] = {
    cache.put(s"route:$routeId", Some(route)).unit
  }
  
  def getSearchResults(criteria: SearchCriteria): Task[Option[List[BusRoute]]] = {
    val key = s"search:${criteria.origin}:${criteria.destination}:${criteria.departureDate}"
    cache.get(key).map(_.asInstanceOf[Option[List[BusRoute]]])
  }
  
  def putSearchResults(criteria: SearchCriteria, routes: List[BusRoute]): Task[Unit] = {
    val key = s"search:${criteria.origin}:${criteria.destination}:${criteria.departureDate}"
    cache.put(key, Some(routes)).unit
  }
}

// Cached API client wrapper
class CachedBlaBlaBusApiClient(
  apiClient: BlaBlaBusApiClient,
  cache: RouteCache
) extends BlaBlaBusApiClient {
  
  def getRoute(routeId: String): Task[Option[BusRoute]] = {
    cache.getRoute(routeId).flatMap {
      case Some(route) => ZIO.succeed(Some(route))
      case None =>
        apiClient.getRoute(routeId).flatTap {
          case Some(route) => cache.putRoute(routeId, route)
          case None => ZIO.unit
        }
    }
  }
  
  def searchRoutes(criteria: SearchCriteria): Task[List[BusRoute]] = {
    cache.getSearchResults(criteria).flatMap {
      case Some(routes) => ZIO.succeed(routes)
      case None =>
        apiClient.searchRoutes(criteria).flatTap { routes =>
          cache.putSearchResults(criteria, routes)
        }
    }
  }
  
  // Delegate other methods to the wrapped client
  def createBooking(bookingRequest: BookingRequest): Task[Booking] = 
    apiClient.createBooking(bookingRequest)
    
  def getLiveUpdates(routeIds: List[String]): Task[List[LiveUpdate]] = 
    apiClient.getLiveUpdates(routeIds)
}
```

## 🧪 Testing Strategies

### Property-Based Testing

```scala
import org.scalacheck._
import org.scalacheck.Arbitrary._

// Generators for transport data
val genCoordinates: Gen[Coordinates] = for {
  lat <- Gen.choose(-90.0, 90.0)
  lon <- Gen.choose(-180.0, 180.0)
} yield Coordinates(lat, lon)

val genBusStop: Gen[BusStop] = for {
  id <- Gen.alphaNumStr.suchThat(_.nonEmpty)
  name <- Gen.alphaStr.suchThat(_.nonEmpty)
  address <- Gen.alphaStr.suchThat(_.nonEmpty)
  city <- Gen.alphaStr.suchThat(_.nonEmpty)
  country <- Gen.oneOf("France", "Germany", "Spain", "Italy")
  coords <- Gen.option(genCoordinates)
} yield BusStop(id, name, address, city, country, coords)

val genBusRoute: Gen[BusRoute] = for {
  id <- Gen.alphaNumStr.suchThat(_.nonEmpty)
  origin <- genBusStop
  destination <- genBusStop.suchThat(_ != origin)
  departure <- genDateTime
  duration <- Gen.choose(1, 12).map(_.hours)
  price <- Gen.choose(10.0, 200.0).map(BigDecimal(_))
  availableSeats <- Gen.choose(0, 50)
  totalSeats <- Gen.choose(availableSeats, 60)
  operator <- genBusOperator
} yield BusRoute(
  id, origin, destination, departure, 
  departure.plus(duration), duration, price, 
  "EUR", availableSeats, totalSeats, operator
)

// Property tests
property("route document structure") {
  forAll(genBusRoute) { route =>
    val doc = routeToDocument(route)
    doc.isInstanceOf[Vertical] && 
    doc.asInstanceOf[Vertical].content.nonEmpty
  }
}

property("search results always have header") {
  forAll(genSearchCriteria, Gen.listOf(genBusRoute)) { (criteria, routes) =>
    val doc = searchResultsToDocument(criteria, routes, routes.length)
    val vertical = doc.asInstanceOf[Vertical]
    vertical.content.head.toString.contains("Search Results")
  }
}

property("booking confirmation contains all info") {
  forAll(genBooking) { booking =>
    val doc = bookingConfirmationToDocument(booking)
    val content = doc.prettyPrint
    content.contains(booking.confirmationNumber) &&
    content.contains("Trip Details") &&
    content.contains("Passengers") &&
    content.contains("Payment Details")
  }
}
```

### Integration Testing

```scala
import zio.test._
import zio.test.Assertion._

object BlaBlaBusIntegrationSpec extends ZIOSpecDefault {
  
  def spec = suite("BlaBlaBus API Integration")(
    
    test("search routes and format results") {
      for {
        client <- ZIO.service[BlaBlaBusApiClient]
        criteria = SearchCriteria("Paris", "Lyon", LocalDate.now().plusDays(1))
        routes <- client.searchRoutes(criteria)
        doc = searchResultsToDocument(criteria, routes, routes.length)
        content = doc.prettyPrint
      } yield {
        assert(content)(contains("Search Results")) &&
        assert(routes.length)(isGreaterThanEqualTo(0))
      }
    },
    
    test("handle route not found gracefully") {
      for {
        client <- ZIO.service[BlaBlaBusApiClient]
        result <- client.getRoute("invalid-route-id")
        doc = result.fold(
          RouteNotFoundError("invalid-route-id").toDocument
        )(routeToDocument)
        content = doc.prettyPrint
      } yield {
        assert(content)(contains("Route Not Found"))
      }
    },
    
    test("process live updates") {
      for {
        client <- ZIO.service[BlaBlaBusApiClient]
        updates <- client.getLiveUpdates(List("RT123", "RT456"))
        doc = liveTrackingToDocument(updates)
        content = doc.prettyPrint
      } yield {
        assert(content)(contains("Live Bus Tracking"))
      }
    }
  ).provide(
    // Test layers
    BlaBlaBusApiClientImpl.layer,
    HttpClient.layer,
    ZLayer.succeed(Config.test)
  )
}
```

### Performance Testing

```scala
import zio.test.TestClock
import java.util.concurrent.TimeUnit

object BlaBlaBusPerformanceSpec extends ZIOSpecDefault {
  
  def spec = suite("Performance Tests")(
    
    test("large search results processing") {
      for {
        largeRouteList <- ZIO.succeed((1 to 1000).map(generateTestRoute).toList)
        startTime <- Clock.nanoTime
        doc = searchResultsToDocument(testCriteria, largeRouteList, largeRouteList.length)
        endTime <- Clock.nanoTime
        duration = (endTime - startTime) / 1_000_000 // Convert to milliseconds
        _ <- Console.printLine(s"Processing 1000 routes took ${duration}ms")
      } yield {
        assert(duration)(isLessThan(1000L)) && // Should complete in under 1 second
        assert(doc.asInstanceOf[Vertical].content.length)(isGreaterThan(1000))
      }
    },
    
    test("memory usage with repeated operations") {
      for {
        runtime <- ZIO.runtime[Any]
        initialMemory <- ZIO.succeed(Runtime.getRuntime.totalMemory())
        _ <- ZIO.foreachDiscard(1 to 100) { _ =>
          val routes = (1 to 50).map(generateTestRoute).toList
          ZIO.succeed(searchResultsToDocument(testCriteria, routes, routes.length))
        }
        _ <- ZIO.succeed(System.gc()) // Suggest garbage collection
        finalMemory <- ZIO.succeed(Runtime.getRuntime.totalMemory())
        memoryIncrease = finalMemory - initialMemory
      } yield {
        // Memory increase should be reasonable (less than 50MB)
        assert(memoryIncrease)(isLessThan(50 * 1024 * 1024L))
      }
    }
  )
}
```

## 📊 Monitoring and Observability

### Metrics Collection

```scala
import zio.metrics._

trait BusApiMetrics {
  def recordApiCall(endpoint: String, duration: Duration, success: Boolean): UIO[Unit]
  def recordCacheHit(cacheType: String): UIO[Unit]
  def recordCacheMiss(cacheType: String): UIO[Unit]
  def recordDocumentSize(docType: String, size: Int): UIO[Unit]
}

class BusApiMetricsImpl extends BusApiMetrics {
  
  def recordApiCall(endpoint: String, duration: Duration, success: Boolean): UIO[Unit] = {
    val status = if (success) "success" else "failure"
    ZIO.succeed {
      // Record metrics (implementation depends on your metrics system)
      // e.g., Prometheus, CloudWatch, etc.
    }
  }
  
  def recordCacheHit(cacheType: String): UIO[Unit] = 
    ZIO.succeed(/* record cache hit metric */)
    
  def recordCacheMiss(cacheType: String): UIO[Unit] = 
    ZIO.succeed(/* record cache miss metric */)
    
  def recordDocumentSize(docType: String, size: Int): UIO[Unit] = 
    ZIO.succeed(/* record document size metric */)
}

// Instrumented API client
class InstrumentedBlaBlaBusApiClient(
  underlying: BlaBlaBusApiClient,
  metrics: BusApiMetrics
) extends BlaBlaBusApiClient {
  
  def searchRoutes(criteria: SearchCriteria): Task[List[BusRoute]] = {
    for {
      start <- Clock.nanoTime
      result <- underlying.searchRoutes(criteria).either
      end <- Clock.nanoTime
      duration = Duration.fromNanos(end - start)
      _ <- metrics.recordApiCall("search_routes", duration, result.isRight)
      routes <- ZIO.fromEither(result)
      _ <- metrics.recordDocumentSize("search_results", routes.length)
    } yield routes
  }
  
  // Similar instrumentation for other methods...
}
```

### Logging

```scala
import zio.logging._

object BusApiLogging {
  
  def logApiRequest(endpoint: String, params: Map[String, String]): UIO[Unit] = {
    log.info(s"API Request: $endpoint") @@ 
    LogAnnotation.apply("endpoint", endpoint) @@
    LogAnnotation.apply("params", params.toString)
  }
  
  def logApiResponse(endpoint: String, duration: Duration, resultSize: Int): UIO[Unit] = {
    log.info(s"API Response: $endpoint completed") @@
    LogAnnotation.apply("endpoint", endpoint) @@
    LogAnnotation.apply("duration_ms", duration.toMillis.toString) @@
    LogAnnotation.apply("result_size", resultSize.toString)
  }
  
  def logDocumentProcessing(docType: String, inputSize: Int, outputSize: Int): UIO[Unit] = {
    log.debug(s"Document processing: $docType") @@
    LogAnnotation.apply("doc_type", docType) @@
    LogAnnotation.apply("input_size", inputSize.toString) @@
    LogAnnotation.apply("output_size", outputSize.toString)
  }
  
  def logError(error: BusApiError, context: String): UIO[Unit] = {
    log.error(s"BlaBlaBus API Error: ${error.message}") @@
    LogAnnotation.apply("error_type", error.getClass.getSimpleName) @@
    LogAnnotation.apply("context", context) @@
    LogAnnotation.apply("error_message", error.message)
  }
}
```

## 🔧 Configuration

### Application Configuration

```scala
import zio.config._
import zio.config.magnolia._

case class BlaBlaBusConfig(
  apiKey: String,
  baseUrl: String,
  timeout: Duration,
  retries: Int,
  rateLimit: RateLimitConfig
)

case class RateLimitConfig(
  requestsPerMinute: Int,
  burstSize: Int
)

case class CacheConfig(
  enabled: Boolean,
  ttl: Duration,
  maxSize: Int
)

case class AppConfig(
  blablabus: BlaBlaBusConfig,
  cache: CacheConfig,
  server: ServerConfig
)

case class ServerConfig(
  port: Int,
  host: String
)

// Configuration loading
val configLayer: TaskLayer[AppConfig] = {
  val configDescriptor = deriveConfig[AppConfig]
  ConfigProvider.fromEnv().load(configDescriptor).toLayer
}

// Environment-specific configurations
object ConfigFactory {
  
  def development: AppConfig = AppConfig(
    blablabus = BlaBlaBusConfig(
      apiKey = sys.env.getOrElse("BLABLABUS_API_KEY", "dev-key"),
      baseUrl = "https://api-dev.blablacar.com/v1",
      timeout = 30.seconds,
      retries = 3,
      rateLimit = RateLimitConfig(60, 10)
    ),
    cache = CacheConfig(
      enabled = true,
      ttl = 5.minutes,
      maxSize = 1000
    ),
    server = ServerConfig(8081, "localhost")
  )
  
  def production: AppConfig = AppConfig(
    blablabus = BlaBlaBusConfig(
      apiKey = sys.env("BLABLABUS_API_KEY"),
      baseUrl = "https://api.blablacar.com/v1",
      timeout = 10.seconds,
      retries = 2,
      rateLimit = RateLimitConfig(100, 20)
    ),
    cache = CacheConfig(
      enabled = true,
      ttl = 10.minutes,
      maxSize = 10000
    ),
    server = ServerConfig(8080, "0.0.0.0")
  )
}
```

## 🎯 Best Practices

### 1. Error Handling
- Always provide user-friendly error messages
- Include specific error codes when available
- Implement circuit breaker pattern for external APIs
- Log errors with sufficient context for debugging

### 2. Performance Optimization
- Use caching for frequently accessed data
- Implement pagination for large result sets
- Use streaming for processing large datasets
- Monitor API response times and set appropriate timeouts

### 3. Data Validation
- Validate all input data before processing
- Use strong types to prevent invalid states
- Implement comprehensive property-based tests
- Sanitize user input for security

### 4. Documentation
- Document all public APIs with clear examples
- Include error scenarios in documentation
- Provide integration guides for different use cases
- Keep documentation up to date with code changes

### 5. Security
- Never log sensitive data (API keys, personal information)
- Validate and sanitize all external data
- Use HTTPS for all API communications
- Implement proper authentication and authorization

## 🔗 Related Resources

- **[BlaBlaCar Bus API Documentation](https://dev.blablacar.com/docs/bus)**
- **[Transport Data Standards](https://developers.google.com/transit/gtfs)**
- **[JSON:API Specification](https://jsonapi.org/)**
- **[OpenAPI/Swagger for API Documentation](https://swagger.io/)**
- **[ZIO Documentation](https://zio.dev/)**
- **[Circe JSON Library](https://circe.github.io/circe/)**

## 📞 Support

For BlaBlaCar Bus API integration support:

- **Issues**: [GitHub Issues](../../issues) with `transport-api` label
- **Discussions**: [GitHub Discussions](../../discussions) in the "API Integration" category
- **Examples**: See the [Examples Wiki Page](Examples) for more use cases
- **API Questions**: Check the [API Documentation](API-Documentation) first

---

This integration guide provides a comprehensive foundation for working with BlaBlaCar Bus API and other transport data using the Document Matrix library. The functional programming approach ensures robust, testable, and maintainable code for production transport applications.
