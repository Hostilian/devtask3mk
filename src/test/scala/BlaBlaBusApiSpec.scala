package com.example

import zio._
import zio.test._
import zio.test.Assertion._
import java.time.{LocalDate, LocalDateTime}
import com.example.BlaBlaBusDocumentProcessor
import com.example.BusStop
import com.example.Trip

/** Test suite for BlaBlaCar Bus API document rendering using real data models. */
object BlaBlaBusApiSpec extends ZIOSpecDefault {
  def spec = suite("BlaBlaBusApiSpec")(
    test("searchResultsToDocument renders trips correctly") {
      val originId      = 1
      val destinationId = 2
      val date          = LocalDate.parse("2025-07-02")
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
          is_promo = Some(false),
          is_refundable = Some(true),
          legs = List.empty,
          passengers = List.empty
        )
      )
      val doc     = BlaBlaBusDocumentProcessor.searchResultsToDocument(originId, destinationId, date.nn, trips)
      val content = doc.prettyPrint
      assertTrue(
        content.contains("Trip ID: | demo-trip-1"),
        content.contains("Price: | 25.99 EUR"),
        content.contains("Departure: | 2025-07-02T08:30:00+01:00"),
        content.contains("Arrival: | 2025-07-02T12:45:00+01:00")
      )
    },
    test("searchResultsToDocument shows no routes found message when empty") {
      val originId      = 1
      val destinationId = 2
      val date          = LocalDate.parse("2025-07-02")
      val doc           = BlaBlaBusDocumentProcessor.searchResultsToDocument(originId, destinationId, date.nn, Nil)
      val content       = doc.prettyPrint
      assertTrue(content.nn.toLowerCase.nn.contains("no routes found"))
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
      val doc     = BlaBlaBusDocumentProcessor.tripToDocument(trip)
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
        id = 42,
        short_name = "Paris Bercy",
        long_name = "Paris Bercy Seine Bus Station",
        short_name_de = None,
        short_name_en = None,
        short_name_fr = None,
        short_name_it = None,
        short_name_nl = None,
        long_name_de = None,
        long_name_en = None,
        long_name_fr = None,
        long_name_it = None,
        long_name_nl = None,
        time_zone = "Europe/Paris",
        latitude = Some(48.8352),
        longitude = Some(2.3768),
        destinations_ids = List.empty,
        is_meta_gare = None,
        address = None,
        stops = None,
        _carrier_id = None
      )
      val doc     = BlaBlaBusDocumentProcessor.stopToDocument(stop)
      val content = doc.prettyPrint
      assertTrue(
        content.contains("Stop ID: | 42"),
        content.contains("Short Name: | Paris Bercy"),
        content.contains("Long Name: | Paris Bercy Seine Bus Station"),
        content.contains("Time Zone: | Europe/Paris"),
        content.contains("Latitude: | 48.8352")
      )
    }
  )
}
