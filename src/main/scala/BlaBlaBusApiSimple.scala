package com.example

import zio._
import java.time.LocalDate
import scala.language.unsafeNulls

/**
 * Simplified BlaBlaCar Bus API client - minimal working version
 * Full implementation will be restored once dependencies are properly configured
 */

// Only keep the mock/test implementation, not the type definitions

object BlaBlaBusApiClientSimpleMock {
  val test: ZLayer[Any, Nothing, BlaBlaBusApiClient] = ZLayer.succeed(new BlaBlaBusApiClient {
    def getStops(): Task[List[BusStop]] = ZIO.succeed(List(
      BusStop(
        id = 1,
        short_name = "Paris Bercy",
        long_name = "Gare Paris Bercy",
        time_zone = "Europe/Paris",
        latitude = Some(48.838424),
        longitude = Some(2.382411),
        destinations_ids = List(2, 3),
        address = Some("48 bis Boulevard de Bercy 75012 Paris")
      ),
      BusStop(
        id = 2,
        short_name = "Lyon Part-Dieu",
        long_name = "Gare Lyon Part-Dieu",
        time_zone = "Europe/Paris",
        latitude = Some(45.760696),
        longitude = Some(4.859034),
        destinations_ids = List(1, 3),
        address = Some("Place Charles Béraudier 69003 Lyon")
      ),
      BusStop(
        id = 3,
        short_name = "Nice Ville",
        long_name = "Gare Nice Ville",
        time_zone = "Europe/Paris",
        latitude = Some(43.703415),
        longitude = Some(7.262831),
        destinations_ids = List(1, 2),
        address = Some("Avenue Thiers 06000 Nice")
      )
    ))

    def searchRoutes(
      originId: Int,
      destinationId: Int,
      date: LocalDate,
      passengers: List[Passenger]
    ): Task[List[Trip]] = ZIO.succeed(List(
      Trip(
        id = "trip-1",
        origin_id = originId,
        destination_id = destinationId,
        departure = "08:30:00+01:00",
        arrival = "12:45:00+01:00",
        available = true,
        price_cents = 2599,
        price_currency = "EUR",
        legs = List.empty,
        passengers = passengers
      ),
      Trip(
        id = "trip-2",
        origin_id = originId,
        destination_id = destinationId,
        departure = "14:30:00+01:00",
        arrival = "18:45:00+01:00",
        available = true,
        price_cents = 2899,
        price_currency = "EUR",
        price_promo_cents = Some(2499),
        is_promo = Some(true),
        legs = List.empty,
        passengers = passengers
      )
    ))
  })
}

// Only keep mock document processor if needed, but do not redefine types
object BlaBlaBusDocumentProcessorSimpleMock {
  def stopToDocument(stop: BusStop): Document[String] =
    Vertical(List(
      Leaf(s"🚏 ${stop.short_name}"),
      Leaf(s"📍 ${stop.long_name}"),
      Leaf(s"🌍 ${stop.latitude.getOrElse(0.0)}, ${stop.longitude.getOrElse(0.0)}"),
      Leaf(s"🕐 ${stop.time_zone}")
    ))

  def tripToDocument(trip: Trip): Document[String] = {
    val priceInfo = trip.price_promo_cents match {
      case Some(promoPrice) => s"💰 €${promoPrice/100.0} (Was €${trip.price_cents/100.0})"
      case None => s"💰 €${trip.price_cents/100.0}"
    }
    val availability = if (trip.available) "✅ Available" else "❌ Sold out"
    Vertical(List(
      Leaf(s"🚌 Trip ${trip.id}"),
      Leaf(s"🕐 ${trip.departure} → ${trip.arrival}"),
      Leaf(priceInfo),
      Leaf(availability)
    ))
  }

  def searchResultsToDocument(originId: Int, destinationId: Int, date: LocalDate, trips: List[Trip]): Document[String] = {
    val header = Leaf(s"🔍 Search Results: Stop $originId → Stop $destinationId on $date")
    val tripDocs = trips.map(tripToDocument)
    Vertical(List(header) ++ tripDocs)
  }

  def errorToDocument(error: BlaBlaBusError): Document[String] = error match {
    case NetworkError(cause) =>
      Vertical(List(
        Leaf("🌐 Network Error"),
        Leaf(s"❌ ${cause.getMessage}")
      ))
    case HttpError(status, message) =>
      Vertical(List(
        Leaf(s"🔗 HTTP Error ($status)"),
        Leaf(s"❌ $message")
      ))
    case RateLimitError(retryAfter) =>
      Vertical(List(
        Leaf("⏰ Rate Limit Exceeded"),
        Leaf(s"🔄 Retry after: ${retryAfter.getOrElse("unknown")}")
      ))
    case ParseError(message) =>
      Vertical(List(
        Leaf("📄 Parse Error"),
        Leaf(s"❌ $message")
      ))
  }
}
