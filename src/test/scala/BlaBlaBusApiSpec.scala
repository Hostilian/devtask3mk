package com.example

import zio._
import zio.test._
import zio.test.Assertion._
import java.time.LocalDate
import com.example._
import com.example.Document._

/** Comprehensive test suite for BlaBlaCar Bus API integration
  */
object BlaBlaBusApiSpec extends ZIOSpecDefault {
  def spec = suite("BlaBlaBusApiSpec")(
    test("searchResultsToDocument produces correct plain output") {
      val criteria = SearchCriteria("Paris", "Lyon", LocalDate.parse("2025-07-02"), 2)
      val route = BusRoute(
        id = "RT123",
        origin = BusStop("ST1", "Paris Bercy", "Address1", "Paris", "France"),
        destination = BusStop("ST2", "Lyon Perrache", "Address2", "Lyon", "France"),
        departureTime = java.time.LocalDateTime.parse("2025-07-02T08:00:00"),
        arrivalTime = java.time.LocalDateTime.parse("2025-07-02T12:00:00"),
        duration = java.time.Duration.ofHours(4),
        price = BigDecimal(29.99),
        currency = "EUR",
        availableSeats = 10,
        totalSeats = 50,
        operator = BusOperator("OP1", "BlaBlaBus"),
        amenities = List("wifi", "ac")
      )
      val doc = searchResultsToDocument(criteria, List(route), 1)
      val content = doc.prettyPrint
      assertTrue(
        content.contains("Paris -> Lyon"),
        content.contains("Trip ID: | RT123") || content.contains("ID: | RT123"),
        content.contains("Name: | BlaBlaBus"),
        content.contains("Price: | 29.99 EUR"),
        content.contains("Available Seats: | 10/50"),
        content.contains("Departure: | 2025-07-02T08:00"),
        content.contains("Arrival: | 2025-07-02T12:00")
      )
    },
    test("searchResultsToDocument shows no routes found message") {
      val criteria = SearchCriteria("Paris", "Lyon", LocalDate.parse("2025-07-02"), 1)
      val doc = searchResultsToDocument(criteria, Nil, 0)
      val content = doc.prettyPrint
      assertTrue(
        content.toLowerCase.contains("no routes found")
      )
    },
    test("routeToDocument produces correct plain output") {
      val route = BusRoute(
        id = "RT456",
        origin = BusStop("ST1", "Paris Bercy", "Address1", "Paris", "France"),
        destination = BusStop("ST2", "Lyon Perrache", "Address2", "Lyon", "France"),
        departureTime = java.time.LocalDateTime.parse("2025-07-02T08:00:00"),
        arrivalTime = java.time.LocalDateTime.parse("2025-07-02T12:00:00"),
        duration = java.time.Duration.ofHours(4),
        price = BigDecimal(19.99),
        currency = "EUR",
        availableSeats = 5,
        totalSeats = 40,
        operator = BusOperator("OP1", "BlaBlaBus"),
        amenities = List("wifi")
      )
      val doc = routeToDocument(route)
      val content = doc.prettyPrint
      assertTrue(
        content.contains("Paris -> Lyon") || content.contains("Paris Bercy -> Lyon Perrache"),
        content.contains("Trip ID: | RT456") || content.contains("ID: | RT456"),
        content.contains("Name: | BlaBlaBus"),
        content.contains("Price: | 19.99 EUR"),
        content.contains("Available Seats: | 5/40"),
        content.contains("Departure: | 2025-07-02T08:00"),
        content.contains("Arrival: | 2025-07-02T12:00")
      )
    },
    test("errorToDocument produces correct plain output") {
      val err = RouteNotFoundError("RT404")
      val doc = err.toDocument
      val content = doc.prettyPrint
      assertTrue(
        content.contains("Route Not Found") || content.contains("not found"),
        content.contains("Route ID: | RT404") || content.contains("Route ID: RT404")
      )
    }
  ).provide(
    BlaBlaBusApiClient.test,
    Scope.default
  )
}

/** Property-based testing for BlaBlaCar Bus API
  */
object BlaBlaBusPropertySpec extends ZIOSpecDefault {
  def spec = suite("BlaBlaCar Bus API Property Tests")(
    test("basic property test") {
      val stop = BusStop(
        id = 1,
        short_name = "Test",
        long_name = "Test Station",
        time_zone = "Europe/Paris",
        latitude = None,
        longitude = None,
        destinations_ids = List.empty
      )
      val doc = BlaBlaBusDocumentProcessor.stopToDocument(stop)
      val content = doc.prettyPrint
      assertTrue(content.contains("Test"))
    }
  )
}

/** Integration tests with real API scenarios
  */
object BlaBlaBusIntegrationSpec extends ZIOSpecDefault {
  def spec = suite("BlaBlaCar Bus API Integration Tests")(
    test("handle meta stations correctly") {
      val metaStation = BusStop(
        id = 90,
        short_name = "Paris - Tous les arrêts",
        long_name = "Paris - All stations",
        time_zone = "Europe/Paris",
        latitude = None,
        longitude = None,
        destinations_ids = List(1, 2, 3),
        is_meta_gare = Some(true),
        stops = Some(
          List(
            BusStop(
              id = 1,
              short_name = "Paris Bercy",
              long_name = "Paris Bercy Station",
              time_zone = "Europe/Paris",
              latitude = Some(48.838424),
              longitude = Some(2.382411),
              destinations_ids = List(2, 3)
            )
          )
        )
      )

      val doc     = BlaBlaBusDocumentProcessor.stopToDocument(metaStation)
      val content = doc.prettyPrint

      assertTrue(
        content.contains("Stop ID: | 90"),
        content.contains("Name: | Paris - All stations"),
        content.contains("Timezone: | Europe/Paris")
      )
    },
    test("handle multi-leg journeys") {
      val multiLegTrip = Trip(
        id = "multi-leg-123",
        origin_id = 1,
        destination_id = 3,
        departure = "2024-01-15T08:30:00+01:00",
        arrival = "2024-01-15T18:45:00+01:00",
        available = true,
        price_cents = 4599,
        price_currency = "EUR",
        legs = List(
          Leg(
            origin_id = 1,
            destination_id = 2,
            departure = "2024-01-15T08:30:00+01:00",
            arrival = "2024-01-15T12:45:00+01:00",
            bus_number = "BB123"
          ),
          Leg(
            origin_id = 2,
            destination_id = 3,
            departure = "2024-01-15T14:30:00+01:00",
            arrival = "2024-01-15T18:45:00+01:00",
            bus_number = "BB456"
          )
        ),
        passengers = List.empty
      )

      val doc     = BlaBlaBusDocumentProcessor.tripToDocument(multiLegTrip)
      val content = doc.prettyPrint

      assertTrue(
        content.contains("Trip ID: | multi-leg-123"),
        content.contains("Price: | 45.99 EUR")
      )
    },
    test("handle different currencies") {
      val gbpTrip = Trip(
        id = "gbp-trip",
        origin_id = 1,
        destination_id = 2,
        departure = "2024-01-15T08:30:00+01:00",
        arrival = "2024-01-15T12:45:00+01:00",
        available = true,
        price_cents = 2299,
        price_currency = "GBP",
        legs = List.empty,
        passengers = List.empty
      )

      val doc     = BlaBlaBusDocumentProcessor.tripToDocument(gbpTrip)
      val content = doc.prettyPrint

      assertTrue(
        content.contains("Trip ID: | gbp-trip"),
        content.contains("Price: | 22.99 GBP")
      )
    },
    test("handle passenger pricing breakdown") {
      val passengerResults = List(
        PassengerResult(
          id = "adult-1",
          price_cents = 2599,
          price_currency = "EUR",
          fare_name = "Adult",
          fare_description = "Standard adult fare"
        ),
        PassengerResult(
          id = "child-1",
          price_cents = 1299,
          price_currency = "EUR",
          fare_name = "Child",
          fare_description = "Child discount fare"
        )
      )

      val familyTrip = Trip(
        id = "family-trip",
        origin_id = 1,
        destination_id = 2,
        departure = "2024-01-15T08:30:00+01:00",
        arrival = "2024-01-15T12:45:00+01:00",
        available = true,
        price_cents = 3898, // Sum of passenger prices
        price_currency = "EUR",
        legs = List.empty,
        passengers = passengerResults
      )

      val doc     = BlaBlaBusDocumentProcessor.tripToDocument(familyTrip)
      val content = doc.prettyPrint

      assertTrue(
        content.contains("Trip ID: | family-trip"),
        content.contains("Price: | 38.98 EUR")
      )
    }
  ).provide(
    BlaBlaBusApiClient.test,
    Scope.default
  )
}
