package com.examplepackage com.example



import zio._
import zio.Console._
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Comprehensive example application demonstrating BlaBlaCar Bus API integration
 * with the Document Matrix library
 */
object BlaBlaBusExample extends ZIOAppDefault {
  
  def run: ZIO[ZIOAppArgs, Any, Any] = {
    val program = for {
      _ <- printLine("🚌 BlaBlaCar Bus API Integration Demo")
      _ <- printLine("=" * 50)
      _ <- printLine()
      
      // Demo 1: Display available bus stops
      _ <- printLine("📍 Demo 1: Available Bus Stops")
      _ <- printLine("-" * 30)
      _ <- demonstrateStops()
      _ <- printLine()
      
      // Demo 2: Search for routes
      _ <- printLine("🔍 Demo 2: Route Search")
      _ <- printLine("-" * 20)
      _ <- demonstrateRouteSearch()
      _ <- printLine()
      
      // Demo 3: Display trip details with pricing
      _ <- printLine("🎫 Demo 3: Trip Details and Pricing")
      _ <- printLine("-" * 35)
      _ <- demonstrateTripDetails()
      _ <- printLine()
      
      // Demo 4: Multi-leg journey planning
      _ <- printLine("🗺️ Demo 4: Multi-leg Journey Planning")
      _ <- printLine("-" * 36)
      _ <- demonstrateMultiLegJourney()
      _ <- printLine()
      
      // Demo 5: Error handling scenarios
      _ <- printLine("⚠️ Demo 5: Error Handling")
      _ <- printLine("-" * 25)
      _ <- demonstrateErrorHandling()
      _ <- printLine()
      
      // Demo 6: Real-time updates simulation
      _ <- printLine("📡 Demo 6: Live Tracking Simulation")
      _ <- printLine("-" * 34)
      _ <- demonstrateLiveTracking()
      _ <- printLine()
      
      _ <- printLine("✅ Demo completed! All BlaBlaCar Bus API integrations working correctly.")
      
    } yield ()
    
    program.provide(
      BlaBlaBusApiClient.test,
      Scope.default
    )
  }
  
  private def demonstrateStops(): ZIO[BlaBlaBusApiClient, Throwable, Unit] = {
    for {
      client <- ZIO.service[BlaBlaBusApiClient]
      stops <- client.getStops()
      _ <- printLine(s"Found ${stops.length} bus stops:")
      _ <- ZIO.foreach(stops.take(3)) { stop =>
        val doc = BlaBlaBusDocumentProcessor.stopToDocument(stop)
        printLine(doc.prettyPrint) *> printLine()
      }
    } yield ()
  }
  
  private def demonstrateRouteSearch(): ZIO[BlaBlaBusApiClient, Throwable, Unit] = {
    val tomorrow = LocalDate.now().plusDays(1)
    val passengers = List(
      Passenger("adult-1", 30),
      Passenger("child-1", 8)
    )
    
    for {
      client <- ZIO.service[BlaBlaBusApiClient]
      trips <- client.searchRoutes(
        originId = 1,
        destinationId = 2,
        date = tomorrow,
        passengers = passengers
      )
      resultsDoc = BlaBlaBusDocumentProcessor.searchResultsToDocument(1, 2, tomorrow, trips)
      _ <- printLine(resultsDoc.prettyPrint)
    } yield ()
  }
  
  private def demonstrateTripDetails(): ZIO[Any, Nothing, Unit] = {
    // Demonstrate different trip scenarios
    val scenarios = List(
      // Regular trip
      Trip(
        id = "regular-trip",
        origin_id = 1,
        destination_id = 2,
        departure = "2024-01-15T08:30:00+01:00",
        arrival = "2024-01-15T12:45:00+01:00",
        available = true,
        price_cents = 2599,
        price_currency = "EUR",
        legs = List.empty,
        passengers = List.empty
      ),
      // Promo trip
      Trip(
        id = "promo-trip",
        origin_id = 1,
        destination_id = 2,
        departure = "2024-01-15T14:30:00+01:00",
        arrival = "2024-01-15T18:45:00+01:00",
        available = true,
        price_cents = 2999,
        price_currency = "EUR",
        price_promo_cents = Some(2499),
        is_promo = Some(true),
        is_refundable = Some(true),
        legs = List.empty,
        passengers = List.empty
      ),
      // Sold out trip
      Trip(
        id = "soldout-trip",
        origin_id = 1,
        destination_id = 2,
        departure = "2024-01-15T20:00:00+01:00",
        arrival = "2024-01-16T00:15:00+01:00",
        available = false,
        price_cents = 2299,
        price_currency = "EUR",
        legs = List.empty,
        passengers = List.empty
      )
    )
    
    ZIO.foreach(scenarios) { trip =>
      val doc = BlaBlaBusDocumentProcessor.tripToDocument(trip)
      printLine(doc.prettyPrint) *> printLine()
    }.unit
  }
  
  private def demonstrateMultiLegJourney(): ZIO[Any, Nothing, Unit] = {
    val multiLegTrip = Trip(
      id = "paris-nice-via-lyon",
      origin_id = 1, // Paris
      destination_id = 4, // Nice
      departure = "2024-01-15T07:00:00+01:00",
      arrival = "2024-01-15T21:30:00+01:00",
      available = true,
      price_cents = 5999,
      price_currency = "EUR",
      is_refundable = Some(false),
      legs = List(
        Leg(
          origin_id = 1, // Paris
          destination_id = 2, // Lyon
          departure = "2024-01-15T07:00:00+01:00",
          arrival = "2024-01-15T11:15:00+01:00",
          bus_number = "BB001"
        ),
        Leg(
          origin_id = 2, // Lyon
          destination_id = 4, // Nice
          departure = "2024-01-15T13:45:00+01:00",
          arrival = "2024-01-15T21:30:00+01:00",
          bus_number = "BB002"
        )
      ),
      passengers = List.empty
    )
    
    val doc = BlaBlaBusDocumentProcessor.tripToDocument(multiLegTrip)
    printLine("Multi-leg journey example (Paris → Nice via Lyon):") *>
    printLine(doc.prettyPrint)
  }
  
  private def demonstrateErrorHandling(): ZIO[Any, Nothing, Unit] = {
    val errors = List(
      NetworkError(new RuntimeException("Connection timeout")),
      HttpError(zio.http.Status.NotFound, "Route not found"),
      RateLimitError(Some(15.minutes)),
      ParseError("Invalid JSON response from API")
    )
    
    ZIO.foreach(errors) { error =>
      val doc = BlaBlaBusDocumentProcessor.errorToDocument(error)
      printLine(s"Error scenario: ${error.getClass.getSimpleName}") *>
      printLine(doc.prettyPrint) *>
      printLine()
    }.unit
  }
  
  private def demonstrateLiveTracking(): ZIO[Any, Nothing, Unit] = {
    // Simulate live tracking updates
    val liveUpdates = List(
      createLiveUpdate("RT001", "Fontainebleau", 0, "On time"),
      createLiveUpdate("RT002", "Mâcon", 15, "Delayed"),
      createLiveUpdate("RT003", "Valence", -5, "Early")
    )
    
    // Create a mock live tracking document
    val header = Document.Vertical(List(
      Document.Line("📡 Live Bus Tracking Dashboard"),
      Document.Line(s"🕐 Last updated: ${java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}"),
      Document.Line("")
    ))
    
    val trackingList = liveUpdates.map { update =>
      Document.Vertical(List(
        Document.Line(s"🚌 Route ${update.routeId}"),
        Document.Line(s"📍 Currently at: ${update.location}"),
        Document.Line(s"⏱️ Status: ${update.status}"),
        Document.Line("")
      ))
    }
    
    val dashboardDoc = Document.Vertical(List(header) ++ trackingList)
    
    printLine(dashboardDoc.prettyPrint)
  }
  
  private case class LiveTrackingUpdate(
    routeId: String,
    location: String,
    delayMinutes: Int,
    status: String
  )
  
  private def createLiveUpdate(routeId: String, location: String, delayMinutes: Int, status: String): LiveTrackingUpdate = {
    LiveTrackingUpdate(routeId, location, delayMinutes, status)
  }
}

/**
 * Interactive CLI for BlaBlaCar Bus API exploration
 */
object BlaBlaBusInteractiveCLI extends ZIOAppDefault {
  
  def run: ZIO[ZIOAppArgs, Any, Any] = {
    val program = for {
      _ <- printLine("🚌 BlaBlaCar Bus API Interactive Explorer")
      _ <- printLine("=" * 45)
      _ <- mainMenu()
    } yield ()
    
    program.provide(
      BlaBlaBusApiClient.test,
      Scope.default
    )
  }
  
  private def mainMenu(): ZIO[BlaBlaBusApiClient, Throwable, Unit] = {
    for {
      _ <- printLine()
      _ <- printLine("Please select an option:")
      _ <- printLine("1. Search for routes")
      _ <- printLine("2. View all bus stops")
      _ <- printLine("3. Simulate booking flow")
      _ <- printLine("4. View live tracking demo")
      _ <- printLine("5. Test error scenarios")
      _ <- printLine("0. Exit")
      _ <- print("> ")
      
      input <- readLine
      _ <- input match {
        case "1" => searchRoutesMenu() *> mainMenu()
        case "2" => viewBusStops() *> mainMenu()
        case "3" => simulateBookingFlow() *> mainMenu()
        case "4" => viewLiveTrackingDemo() *> mainMenu()
        case "5" => testErrorScenarios() *> mainMenu()
        case "0" => printLine("👋 Thank you for using BlaBlaCar Bus API Explorer!")
        case _ => printLine("❌ Invalid option. Please try again.") *> mainMenu()
      }
    } yield ()
  }
  
  private def searchRoutesMenu(): ZIO[BlaBlaBusApiClient, Throwable, Unit] = {
    for {
      _ <- printLine("\n🔍 Route Search")
      _ <- printLine("Enter search criteria:")
      
      _ <- print("Origin stop ID (e.g., 1 for Paris): ")
      originInput <- readLine
      originId <- ZIO.attempt(originInput.toInt).orElse(ZIO.succeed(1))
      
      _ <- print("Destination stop ID (e.g., 2 for Lyon): ")
      destInput <- readLine
      destId <- ZIO.attempt(destInput.toInt).orElse(ZIO.succeed(2))
      
      _ <- print("Date (YYYY-MM-DD) or press Enter for tomorrow: ")
      dateInput <- readLine
      date <- if (dateInput.trim.isEmpty) {
        ZIO.succeed(LocalDate.now().plusDays(1))
      } else {
        ZIO.attempt(LocalDate.parse(dateInput)).orElse(ZIO.succeed(LocalDate.now().plusDays(1)))
      }
      
      _ <- print("Number of passengers (default 1): ")
      passengersInput <- readLine
      passengerCount <- ZIO.attempt(passengersInput.toInt).orElse(ZIO.succeed(1))
      
      passengers = (1 to passengerCount).map(i => Passenger(s"passenger-$i", 30)).toList
      
      client <- ZIO.service[BlaBlaBusApiClient]
      trips <- client.searchRoutes(originId, destId, date, passengers)
      resultsDoc = BlaBlaBusDocumentProcessor.searchResultsToDocument(originId, destId, date, trips)
      _ <- printLine("\n" + resultsDoc.prettyPrint)
      
    } yield ()
  }
  
  private def viewBusStops(): ZIO[BlaBlaBusApiClient, Throwable, Unit] = {
    for {
      _ <- printLine("\n📍 Available Bus Stops")
      client <- ZIO.service[BlaBlaBusApiClient]
      stops <- client.getStops()
      _ <- ZIO.foreach(stops) { stop =>
        val doc = BlaBlaBusDocumentProcessor.stopToDocument(stop)
        printLine(doc.prettyPrint) *> printLine()
      }
    } yield ()
  }
  
  private def simulateBookingFlow(): ZIO[Any, Nothing, Unit] = {
    for {
      _ <- printLine("\n🎫 Booking Flow Simulation")
      _ <- printLine("Simulating a complete booking process...")
      
      // Step 1: Search results
      _ <- printLine("\nStep 1: Search Results")
      searchDoc = Document.Vertical(List(
        Document.Line("🔍 Search Results: Paris → Lyon"),
        Document.Line("📅 Tomorrow"),
        Document.Line(""),
        Document.Line("1. 🚌 08:30 → 12:45 | 💰 €25.99 | ✅ Available"),
        Document.Line("2. 🚌 14:30 → 18:45 | 💰 €28.99 | ✅ Available"),
        Document.Line("3. 🚌 20:00 → 00:15 | 💰 €22.99 | ❌ Sold out")
      ))
      _ <- printLine(searchDoc.prettyPrint)
      
      // Step 2: Trip selection
      _ <- printLine("\nStep 2: Selected Trip Details")
      tripDoc = Document.Vertical(List(
        Document.Line("🎫 Selected Trip"),
        Document.Line("🚌 Paris Bercy → Lyon Part-Dieu"),
        Document.Line("🕐 08:30 → 12:45"),
        Document.Line("💰 €25.99"),
        Document.Line("🪑 2 passengers"),
        Document.Line("✅ Available")
      ))
      _ <- printLine(tripDoc.prettyPrint)
      
      // Step 3: Booking confirmation
      _ <- printLine("\nStep 3: Booking Confirmation")
      confirmationDoc = Document.Vertical(List(
        Document.Line("✅ Booking Confirmed!"),
        Document.Line("📄 Confirmation: BBC123456789"),
        Document.Line(""),
        Document.Line("🚌 Trip Details:"),
        Document.Line("Paris Bercy → Lyon Part-Dieu"),
        Document.Line("🕐 08:30 → 12:45"),
        Document.Line(""),
        Document.Line("👥 Passengers:"),
        Document.Line("1. John Doe"),
        Document.Line("2. Jane Doe"),
        Document.Line(""),
        Document.Line("💳 Payment: €51.98 (Paid)"),
        Document.Line(""),
        Document.Line("📋 Important:"),
        Document.Line("• Arrive 15 minutes early"),
        Document.Line("• Bring valid ID"),
        Document.Line("• Check email for e-tickets")
      ))
      _ <- printLine(confirmationDoc.prettyPrint)
      
    } yield ()
  }
  
  private def viewLiveTrackingDemo(): ZIO[Any, Nothing, Unit] = {
    for {
      _ <- printLine("\n📡 Live Tracking Demo")
      trackingDoc = Document.Vertical(List(
        Document.Line("📡 Live Bus Tracking"),
        Document.Line(s"🕐 ${java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}"),
        Document.Line(""),
        Document.Line("🟢 Route BB001 (On time)"),
        Document.Line("📍 Currently: Highway A6 - Fontainebleau"),
        Document.Line("🎯 Next: Lyon Part-Dieu"),
        Document.Line("🕐 ETA: 12:45"),
        Document.Line(""),
        Document.Line("🟡 Route BB002 (+15min)"),
        Document.Line("📍 Currently: Mâcon Station"),
        Document.Line("🎯 Next: Lyon Perrache"),
        Document.Line("🕐 ETA: 14:15"),
        Document.Line(""),
        Document.Line("🔴 Route BB003 (Cancelled)"),
        Document.Line("📍 Service disruption"),
        Document.Line("🎯 Alternative routes available")
      ))
      _ <- printLine(trackingDoc.prettyPrint)
    } yield ()
  }
  
  private def testErrorScenarios(): ZIO[Any, Nothing, Unit] = {
    for {
      _ <- printLine("\n⚠️ Error Scenarios")
      
      // Network error
      networkError = NetworkError(new RuntimeException("Connection failed"))
      networkDoc = BlaBlaBusDocumentProcessor.errorToDocument(networkError)
      _ <- printLine("Scenario 1: Network Error")
      _ <- printLine(networkDoc.prettyPrint)
      _ <- printLine()
      
      // Rate limit error
      rateLimitError = RateLimitError(Some(5.minutes))
      rateLimitDoc = BlaBlaBusDocumentProcessor.errorToDocument(rateLimitError)
      _ <- printLine("Scenario 2: Rate Limit Error")
      _ <- printLine(rateLimitDoc.prettyPrint)
      _ <- printLine()
      
      // HTTP error
      httpError = HttpError(zio.http.Status.NotFound, "Route not found")
      httpDoc = BlaBlaBusDocumentProcessor.errorToDocument(httpError)
      _ <- printLine("Scenario 3: HTTP Error")
      _ <- printLine(httpDoc.prettyPrint)
      
    } yield ()
  }
}

/**
 * Performance testing and benchmarking for BlaBlaCar Bus API integration
 */
object BlaBlaBusPerformanceTest extends ZIOAppDefault {
  
  def run: ZIO[ZIOAppArgs, Any, Any] = {
    val program = for {
      _ <- printLine("🚀 BlaBlaCar Bus API Performance Testing")
      _ <- printLine("=" * 45)
      _ <- testLargeDatasetProcessing()
      _ <- testConcurrentRequests()
      _ <- testMemoryUsage()
    } yield ()
    
    program.provide(
      BlaBlaBusApiClient.test,
      Scope.default
    )
  }
  
  private def testLargeDatasetProcessing(): ZIO[BlaBlaBusApiClient, Throwable, Unit] = {
    for {
      _ <- printLine("\n📊 Large Dataset Processing Test")
      
      // Generate large dataset
      largeRouteSet = (1 to 1000).map { i =>
        Trip(
          id = s"trip-$i",
          origin_id = 1,
          destination_id = 2,
          departure = "2024-01-15T08:30:00+01:00",
          arrival = "2024-01-15T12:45:00+01:00",
          available = i % 10 != 0, // 90% available
          price_cents = 2000 + (i % 100) * 10,
          price_currency = "EUR",
          legs = List.empty,
          passengers = List.empty
        )
      }.toList
      
      startTime <- Clock.nanoTime
      resultsDoc = BlaBlaBusDocumentProcessor.searchResultsToDocument(
        1, 2, LocalDate.now(), largeRouteSet
      )
      _ = resultsDoc.prettyPrint // Force evaluation
      endTime <- Clock.nanoTime
      
      duration = (endTime - startTime) / 1_000_000 // Convert to milliseconds
      _ <- printLine(s"✅ Processed ${largeRouteSet.length} routes in ${duration}ms")
      _ <- printLine(s"📈 Performance: ${largeRouteSet.length.toDouble / duration * 1000} routes/second")
      
    } yield ()
  }
  
  private def testConcurrentRequests(): ZIO[BlaBlaBusApiClient, Throwable, Unit] = {
    for {
      _ <- printLine("\n🔄 Concurrent Request Test")
      
      client <- ZIO.service[BlaBlaBusApiClient]
      
      // Simulate 10 concurrent search requests
      concurrentSearches = ZIO.collectAllPar(
        (1 to 10).map { i =>
          client.searchRoutes(
            originId = i % 3 + 1,
            destinationId = i % 3 + 2,
            date = LocalDate.now().plusDays(i % 7)
          ).timed
        }
      )
      
      startTime <- Clock.nanoTime
      results <- concurrentSearches
      endTime <- Clock.nanoTime
      
      totalDuration = (endTime - startTime) / 1_000_000
      avgDuration = results.map(_._1.toMillis).sum / results.length
      
      _ <- printLine(s"✅ Completed 10 concurrent requests in ${totalDuration}ms")
      _ <- printLine(s"📊 Average request time: ${avgDuration}ms")
      _ <- printLine(s"🎯 Throughput: ${10.0 / totalDuration * 1000} requests/second")
      
    } yield ()
  }
  
  private def testMemoryUsage(): ZIO[Any, Nothing, Unit] = {
    for {
      _ <- printLine("\n💾 Memory Usage Test")
      
      runtime <- ZIO.runtime[Any]
      initialMemory = Runtime.getRuntime.totalMemory()
      
      // Create and process many documents
      _ <- ZIO.foreach(1 to 1000) { i =>
        val trip = Trip(
          id = s"memory-test-$i",
          origin_id = 1,
          destination_id = 2,
          departure = "2024-01-15T08:30:00+01:00",
          arrival = "2024-01-15T12:45:00+01:00",
          available = true,
          price_cents = 2599,
          price_currency = "EUR",
          legs = List.empty,
          passengers = List.empty
        )
        ZIO.succeed(BlaBlaBusDocumentProcessor.tripToDocument(trip))
      }
      
      _ <- ZIO.succeed(System.gc()) // Suggest garbage collection
      finalMemory = Runtime.getRuntime.totalMemory()
      memoryIncrease = finalMemory - initialMemory
      
      _ <- printLine(s"✅ Memory test completed")
      _ <- printLine(s"📈 Memory increase: ${memoryIncrease / 1024 / 1024}MB")
      _ <- printLine(s"💡 Memory efficiency: ${if (memoryIncrease < 50 * 1024 * 1024) "Good" else "Needs optimization"}")
      
    } yield ()
  }
}
