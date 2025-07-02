package com.example

import zio._
import java.time.{LocalDateTime, LocalDate}
import java.time.format.DateTimeFormatter
import scala.language.unsafeNulls

/**
 * Simplified BlaBlaCar Bus API client - minimal working version
 * Full implementation will be restored once dependencies are properly configured
 */

// Simplified Data Models
case class BusStop(
  id: Int,
  short_name: String,
  long_name: String,
  time_zone: String,
  latitude: Double,
  longitude: Double,
  destinations_ids: List[Int],
  is_meta_gare: Boolean = false,
  address: Option[String] = None
)

case class Passenger(
  id: String,
  age: Int
)

case class Leg(
  origin_id: Int,
  destination_id: Int,
  departure: String,
  arrival: String,
  bus_number: String
)

case class Trip(
  id: String,
  origin_id: Int,
  destination_id: Int,
  departure: String,
  arrival: String,
  available: Boolean,
  price_cents: Int,
  price_currency: String,
  price_promo_cents: Option[Int] = None,
  is_promo: Option[Boolean] = None,
  is_refundable: Option[Boolean] = None,
  legs: List[Leg],
  passengers: List[Passenger]
)

// Simplified Error Types
sealed trait BlaBlaBusError extends Throwable
case class NetworkError(cause: Throwable) extends BlaBlaBusError
case class HttpError(status: String, message: String) extends BlaBlaBusError  
case class RateLimitError(retryAfter: Option[Duration]) extends BlaBlaBusError
case class ParseError(message: String) extends BlaBlaBusError

// Mock API Client
trait BlaBlaBusApiClient {
  def getStops(): Task[List[BusStop]]
  def searchRoutes(
    originId: Int,
    destinationId: Int,
    date: LocalDate,
    passengers: List[Passenger] = List(Passenger("1", 35))
  ): Task[List[Trip]]
}

object BlaBlaBusApiClient {
  
  val test: ZLayer[Any, Nothing, BlaBlaBusApiClient] = ZLayer.succeed(new BlaBlaBusApiClient {
    
    def getStops(): Task[List[BusStop]] = ZIO.succeed(List(
      BusStop(1, "Paris Bercy", "Gare Paris Bercy", "Europe/Paris", 48.838424, 2.382411, List(2, 3), false, Some("48 bis Boulevard de Bercy 75012 Paris")),
      BusStop(2, "Lyon Part-Dieu", "Gare Lyon Part-Dieu", "Europe/Paris", 45.760696, 4.859034, List(1, 3), false, Some("Place Charles Béraudier 69003 Lyon")),
      BusStop(3, "Nice Ville", "Gare Nice Ville", "Europe/Paris", 43.703415, 7.262831, List(1, 2), false, Some("Avenue Thiers 06000 Nice"))
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

// Mock Document Processor
object BlaBlaBusDocumentProcessor {
  
  def stopToDocument(stop: BusStop): Document[String] = {
    Vertical(List(
      Leaf(s"🚏 ${stop.short_name}"),
      Leaf(s"📍 ${stop.long_name}"),
      Leaf(s"🌍 ${stop.latitude}, ${stop.longitude}"),
      Leaf(s"🕐 ${stop.time_zone}")
    ))
  }
  
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
  
  def errorToDocument(error: BlaBlaBusError): Document[String] = {
    error match {
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
}
