package com.example

import zio._
import zio.test._
import zio.test.Assertion._
import java.time.{LocalDate, LocalDateTime}
import com.example.BlaBlaBusDocumentProcessor

/** Test suite for BlaBlaCar Bus API document rendering using real data models. */
object BlaBlaBusApiSpec extends ZIOSpecDefault {
  def spec = suite("BlaBlaBusApiSpec")(
    test("searchResultsToDocument renders trips correctly") {
      val originId = 1
      val destinationId = 2
      val date = LocalDate.parse("2025-07-02")
      val trips = List(
        Trip(
          id = "demo-trip-1",
          origin_id = originId,
          destination_id = destinationId,
          departure = "2025-07-02T08:30:00+01:00",
          arrival = "2025-07-02T12:45:00+01:00",
          available = true,
          price_cents = 2599,
          price_currency = "EUR",
          legs = List.empty,
          passengers = List.empty
        )
      )
      val doc = BlaBlaBusDocumentProcessor.searchResultsToDocument(originId, destinationId, date, trips)
      val content = doc.prettyPrint
      assertTrue(
        content.contains("Trip ID: | demo-trip-1"),
        content.contains("Price: | 25.99 EUR"),
        content.contains("Departure: | 2025-07-02T08:30:00+01:00"),
        content.contains("Arrival: | 2025-07-02T12:45:00+01:00")
      )
    },
    test("searchResultsToDocument shows no routes found message when empty") {
      val originId = 1
      val destinationId = 2
      val date = LocalDate.parse("2025-07-02")
      val doc = BlaBlaBusDocumentProcessor.searchResultsToDocument(originId, destinationId, date, Nil)
      val content = doc.prettyPrint
      assertTrue(content.toLowerCase.contains("no routes found"))
    },
    test("tripToDocument renders trip details correctly") {
      val trip = Trip(
        id = "trip-xyz",
        origin_id = 1,
        destination_id = 2,
        departure = "2025-07-02T09:00:00+01:00",
        arrival = "2025-07-02T13:00:00+01:00",
        available = true,
        price_cents = 1999,
        price_currency = "EUR",
        is_promo = Some(false),
        is_refundable = Some(true),
        legs = List.empty,
        passengers = List.empty
      )
      val doc = BlaBlaBusDocumentProcessor.tripToDocument(trip)
      val content = doc.prettyPrint
      assertTrue(
        content.contains("Trip ID: | trip-xyz"),
        content.contains("Price: | 19.99 EUR"),
        content.contains("Departure: | 2025-07-02T09:00:00+01:00"),
        content.contains("Arrival: | 2025-07-02T13:00:00+01:00")
      )
    },
    test("stopToDocument renders stop details correctly") {
      val stop = BusStop(
        id = 1,
        short_name = "Paris Bercy",
        long_name = "Paris Bercy Station",
        time_zone = "Europe/Paris",
        latitude = Some(48.838424),
        longitude = Some(2.382411),
        destinations_ids = List(2, 3),
        address = Some("48 bis Boulevard de Bercy 75012 Paris")
      )
      val doc = BlaBlaBusDocumentProcessor.stopToDocument(stop)
      val content = doc.prettyPrint
      assertTrue(
        content.contains("Paris Bercy"),
        content.contains("Paris Bercy Station"),
        content.contains("Europe/Paris")
      )
    }
  )
}
