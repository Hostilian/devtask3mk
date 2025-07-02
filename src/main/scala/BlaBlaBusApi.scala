package com.example



import zio._
import zio.http._
import zio.json._
import java.time.{LocalDateTime, LocalDate}
import java.time.format.DateTimeFormatter
import scala.math.BigDecimal

/**
 * Complete BlaBlaCar Bus API client implementation
 * Based on the official API documentation: https://bus-api.blablacar.com
 */

// Core Data Models
case class BusStop(
  id: Int,
  short_name: String,
  long_name: String,
  short_name_de: Option[String] = None,
  short_name_en: Option[String] = None,
  short_name_fr: Option[String] = None,
  short_name_it: Option[String] = None,
  short_name_nl: Option[String] = None,
  long_name_de: Option[String] = None,
  long_name_en: Option[String] = None,
  long_name_fr: Option[String] = None,
  long_name_it: Option[String] = None,
  long_name_nl: Option[String] = None,
  time_zone: String,
  latitude: Option[Double],
  longitude: Option[Double],
  destinations_ids: List[Int],
  is_meta_gare: Option[Boolean] = None,
  address: Option[String] = None,
  stops: Option[List[BusStop]] = None,
  _carrier_id: Option[String] = None
)

case class Leg(
  origin_id: Int,
  destination_id: Int,
  departure: String,
  arrival: String,
  bus_number: String,
  one_luggage: Option[Boolean] = None,
  service_name: Option[String] = None,
  service_type: Option[String] = None
)

case class Fare(
  id: Int,
  updated_at: String,
  origin_id: Int,
  destination_id: Int,
  departure: String,
  arrival: String,
  available: Boolean,
  price_cents: Int,
  price_currency: String,
  legs: List[Leg]
)

case class Passenger(
  id: String,
  age: Int
)

case class PassengerResult(
  id: String,
  price_cents: Int,
  price_currency: String,
  price_promo_currency: Option[String] = None,
  is_promo: Option[Boolean] = None,
  fare_name: String,
  fare_description: String
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
  price_promo_currency: Option[String] = None,
  is_promo: Option[Boolean] = None,
  is_refundable: Option[Boolean] = None,
  legs: List[Leg],
  passengers: List[PassengerResult]
)

// Request/Response wrappers
case class StopsResponse(stops: List[BusStop])
case class FaresResponse(fares: List[Fare])
case class TripsResponse(trips: List[Trip])

case class SearchRequest(
  origin_id: Int,
  destination_id: Int,
  date: String,
  currency: Option[String] = None,
  passengers: Option[List[Passenger]] = None,
  transfers: Option[Int] = None
)

// JSON Codecs
object BlaBlaBusCodecs {
  given JsonCodec[BusStop] = DeriveJsonCodec.gen[BusStop]
  given JsonCodec[Leg] = DeriveJsonCodec.gen[Leg]
  given JsonCodec[Fare] = DeriveJsonCodec.gen[Fare]
  given JsonCodec[Passenger] = DeriveJsonCodec.gen[Passenger]
  given JsonCodec[PassengerResult] = DeriveJsonCodec.gen[PassengerResult]
  given JsonCodec[Trip] = DeriveJsonCodec.gen[Trip]
  given JsonCodec[StopsResponse] = DeriveJsonCodec.gen[StopsResponse]
  given JsonCodec[FaresResponse] = DeriveJsonCodec.gen[FaresResponse]
  given JsonCodec[TripsResponse] = DeriveJsonCodec.gen[TripsResponse]
  given JsonCodec[SearchRequest] = DeriveJsonCodec.gen[SearchRequest]
}

// API Client Configuration
case class BlaBlaBusConfig(
  baseUrl: String = "https://bus-api.blablacar.com",
  apiKey: String,
  version: String = "v3",
  timeout: Duration = 30.seconds,
  retries: Int = 3
)

// API Errors
sealed trait BlaBlaBusApiError extends Throwable
case class HttpError(status: Status, message: String) extends BlaBlaBusApiError
case class ParseError(message: String) extends BlaBlaBusApiError
case class NetworkError(cause: Throwable) extends BlaBlaBusApiError
case class RateLimitError(retryAfter: Option[Duration] = None) extends BlaBlaBusApiError

// API Client Interface
trait BlaBlaBusApiClient {
  def getStops(): Task[List[BusStop]]
  def getFares(
    originId: Option[Int] = None,
    destinationId: Option[Int] = None,
    date: Option[LocalDate] = None,
    startDate: Option[LocalDate] = None,
    endDate: Option[LocalDate] = None,
    currencies: List[String] = List.empty,
    updatedAfter: Option[LocalDateTime] = None
  ): Task[List[Fare]]
  def searchRoutes(request: SearchRequest): Task[List[Trip]]
  def searchRoutes(
    originId: Int,
    destinationId: Int,
    date: LocalDate,
    passengers: List[Passenger] = List(Passenger("1", 35)),
    currency: String = "EUR",
    transfers: Int = 0
  ): Task[List[Trip]]
}

// Implementation
class BlaBlaBusApiClientImpl(
  config: BlaBlaBusConfig,
  client: Client
) extends BlaBlaBusApiClient {
  
  import BlaBlaBusCodecs.given
  
  private val baseHeaders = Headers(
    Header.Authorization.Bearer(config.apiKey),
    Header.Accept(MediaType.application.json),
    Header.Custom("Accept-Encoding", "gzip")
  )
  
  def getStops(): Task[List[BusStop]] = {
    val url = s"${config.baseUrl}/${config.version}/stops"
    
    for {
      response <- client
        .request(
          Request.get(url).addHeaders(baseHeaders)
        )
        .timeout(config.timeout)
        .retry(Schedule.recurs(config.retries))
        .catchAll(handleNetworkError)
      body <- response.body.asString
      stopsResponse <- ZIO.fromEither(body.fromJson[StopsResponse])
        .mapError(ParseError.apply)
    } yield stopsResponse.stops
  }
  
  def getFares(
    originId: Option[Int] = None,
    destinationId: Option[Int] = None,
    date: Option[LocalDate] = None,
    startDate: Option[LocalDate] = None,
    endDate: Option[LocalDate] = None,
    currencies: List[String] = List.empty,
    updatedAfter: Option[LocalDateTime] = None
  ): Task[List[Fare]] = {
    val queryParams = buildFareQueryParams(
      originId, destinationId, date, startDate, endDate, currencies, updatedAfter
    )
    val url = s"${config.baseUrl}/${config.version}/fares"
    
    for {
      response <- client
        .request(
          Request.get(url + queryParams).addHeaders(baseHeaders)
        )
        .timeout(config.timeout)
        .retry(Schedule.recurs(config.retries))
        .catchAll(handleNetworkError)
      body <- response.body.asString
      faresResponse <- ZIO.fromEither(body.fromJson[FaresResponse])
        .mapError(ParseError.apply)
    } yield faresResponse.fares
  }
  
  def searchRoutes(request: SearchRequest): Task[List[Trip]] = {
    val url = s"${config.baseUrl}/${config.version}/search"
    val requestBody = request.toJson
    
    for {
      response <- client
        .request(
          Request
            .post(url, Body.fromString(requestBody))
            .addHeaders(baseHeaders)
            .addHeader(Header.ContentType(MediaType.application.json))
        )
        .timeout(config.timeout)
        .retry(Schedule.recurs(config.retries))
        .catchAll(handleNetworkError)
      body <- response.body.asString
      tripsResponse <- ZIO.fromEither(body.fromJson[TripsResponse])
        .mapError(ParseError.apply)
    } yield tripsResponse.trips
  }
  
  def searchRoutes(
    originId: Int,
    destinationId: Int,
    date: LocalDate,
    passengers: List[Passenger] = List(Passenger("1", 35)),
    currency: String = "EUR",
    transfers: Int = 0
  ): Task[List[Trip]] = {
    val request = SearchRequest(
      origin_id = originId,
      destination_id = destinationId,
      date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
      currency = Some(currency),
      passengers = Some(passengers),
      transfers = Some(transfers)
    )
    searchRoutes(request)
  }
  
  private def buildFareQueryParams(
    originId: Option[Int],
    destinationId: Option[Int],
    date: Option[LocalDate],
    startDate: Option[LocalDate],
    endDate: Option[LocalDate],
    currencies: List[String],
    updatedAfter: Option[LocalDateTime]
  ): String = {
    val params = scala.collection.mutable.ListBuffer[String]()
    
    originId.foreach(id => params += s"origin_id=$id")
    destinationId.foreach(id => params += s"destination_id=$id")
    date.foreach(d => params += s"date=${d.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
    startDate.foreach(d => params += s"start_date=${d.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
    endDate.foreach(d => params += s"end_date=${d.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
    currencies.foreach(c => params += s"currencies[]=$c")
    updatedAfter.foreach(dt => params += s"updated_after=${dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
    
    if (params.nonEmpty) "?" + params.mkString("&") else ""
  }
  
  private def handleNetworkError(error: Throwable): Task[Response] = {
    error match {
      case _: java.net.SocketTimeoutException => ZIO.fail(NetworkError(error))
      case _: java.io.IOException => ZIO.fail(NetworkError(error))
      case other => ZIO.fail(NetworkError(other))
    }
  }
}

// Document Processing Integration
object BlaBlaBusDocumentProcessor {
  import Document._
  
  def stopToDocument(stop: BusStop): Document[String] = {
    val header = Leaf(s"🚏 ${stop.short_name}")
    val location = stop.address.map(addr => Leaf(s"📍 $addr")).getOrElse(Empty)
    val coordinates = (stop.latitude, stop.longitude) match {
      case (Some(lat), Some(lon)) => Leaf(f"📐 $lat%.6f, $lon%.6f")
      case _ => Empty
    }
    val timezone = Leaf(s"🕐 ${stop.time_zone}")
    val destinations = if (stop.destinations_ids.nonEmpty) {
      Leaf(s"🎯 ${stop.destinations_ids.length} destinations available")
    } else Empty
    
    Vertical(List(header, location, coordinates, timezone, destinations).filter(_ != Empty))
  }
  
  def fareToDocument(fare: Fare): Document[String] = {
    val priceEuros = BigDecimal(fare.price_cents) / 100
    val header = Leaf(s"🎫 Fare ${fare.id}")
    val price = Leaf(s"💰 €$priceEuros")
    val timing = Horizontal(List(
      Leaf(s"🕐 ${formatTime(fare.departure)}"),
      Leaf("→"),
      Leaf(s"🕐 ${formatTime(fare.arrival)}")
    ))
    val availability = if (fare.available) {
      Leaf("✅ Available")
    } else {
      Leaf("❌ Sold out")
    }
    
    val legs = if (fare.legs.length > 1) {
      List(Leaf(s"🔄 ${fare.legs.length} legs"))
    } else List.empty
    
    Vertical(List(header, price, timing, availability) ++ legs)
  }
  
  def tripToDocument(trip: Trip): Document[String] = {
    val priceEuros = BigDecimal(trip.price_cents) / 100
    val header = Leaf(s"🚌 Trip ${trip.id}")
    
    val pricing = trip.price_promo_cents match {
      case Some(promoCents) if trip.is_promo.contains(true) =>
        val promoEuros = BigDecimal(promoCents) / 100
        Horizontal(List(
          Leaf(s"💰 €$promoEuros"),
          Leaf(s"(was €$priceEuros)"),
          Leaf("🏷️ PROMO")
        ))
      case _ =>
        Leaf(s"💰 €$priceEuros")
    }
    
    val timing = Horizontal(List(
      Leaf(s"🕐 ${formatTime(trip.departure)}"),
      Leaf("→"),
      Leaf(s"🕐 ${formatTime(trip.arrival)}")
    ))
    
    val availability = if (trip.available) {
      Leaf("✅ Available")
    } else {
      Leaf("❌ Sold out")
    }
    
    val features = List(
      trip.is_refundable.filter(identity).map(_ => "💳 Refundable"),
      if (trip.legs.length > 1) Some(s"🔄 ${trip.legs.length} legs") else None
    ).flatten
    
    val featuresDoc = if (features.nonEmpty) {
      List(Leaf(features.mkString(" • ")))
    } else List.empty
    
    Vertical(List(header, pricing, timing, availability) ++ featuresDoc)
  }
  
  def searchResultsToDocument(
    originId: Int,
    destinationId: Int,
    date: LocalDate,
    trips: List[Trip]
  ): Document[String] = {
    val header = Vertical(List(
      Leaf("🔍 BlaBlaCar Bus Search Results"),
      Leaf(s"Route: $originId → $destinationId"),
      Leaf(s"📅 ${date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))}")
    ))
    
    val separator = Leaf("─" * 50)
    
    if (trips.isEmpty) {
      Vertical(List(
        header,
        separator,
        Leaf("😔 No trips found"),
        Leaf("Try adjusting your search criteria")
      ))
    } else {
      val tripList = trips.zipWithIndex.map { case (trip, index) =>
        val number = Leaf(s"${index + 1}.")
        val tripDoc = tripToDocument(trip)
        Horizontal(List(number, tripDoc))
      }
      
      val stats = Leaf(s"📊 Found ${trips.length} trip(s)")
      
      Vertical(List(header, stats, separator) ++ tripList)
    }
  }
  
  def errorToDocument(error: BlaBlaBusApiError): Document[String] = {
    error match {
      case HttpError(status, message) =>
        Vertical(List(
          Leaf(s"❌ HTTP Error ${status.code}"),
          Leaf(message)
        ))
      case ParseError(message) =>
        Vertical(List(
          Leaf("⚠️ Data Parsing Error"),
          Leaf(message)
        ))
      case NetworkError(cause) =>
        Vertical(List(
          Leaf("🌐 Network Error"),
          Leaf("Unable to connect to BlaBlaCar API"),
          Leaf("Please check your internet connection")
        ))
      case RateLimitError(retryAfter) =>
        val retryMessage = retryAfter
          .map(d => s"Try again in ${d.toMinutes} minutes")
          .getOrElse("Try again later")
        Vertical(List(
          Leaf("⏱️ Rate Limit Exceeded"),
          Leaf(retryMessage)
        ))
    }
  }
  
  private def formatTime(timeString: String): String = {
    try {
      val dateTime = LocalDateTime.parse(timeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch {
      case _: Exception => timeString.take(5) // Fallback to first 5 chars (HH:mm)
    }
  }
}

// ZIO Layer for dependency injection
object BlaBlaBusApiClient {
  
  def live: ZLayer[BlaBlaBusConfig & Client, Nothing, BlaBlaBusApiClient] =
    ZLayer {
      for {
        config <- ZIO.service[BlaBlaBusConfig]
        client <- ZIO.service[Client]
      } yield new BlaBlaBusApiClientImpl(config, client)
    }
  
  def test: ZLayer[Any, Nothing, BlaBlaBusApiClient] =
    ZLayer.succeed(new MockBlaBlaBusApiClient())
}

// Mock implementation for testing
class MockBlaBlaBusApiClient extends BlaBlaBusApiClient {
  
  def getStops(): Task[List[BusStop]] = ZIO.succeed(List(
    BusStop(
      id = 1,
      short_name = "Paris Bercy",
      long_name = "Paris Bercy Station",
      time_zone = "Europe/Paris",
      latitude = Some(48.838424),
      longitude = Some(2.382411),
      destinations_ids = List(2, 3, 4),
      address = Some("48 bis Boulevard de Bercy 75012 Paris")
    ),
    BusStop(
      id = 2,
      short_name = "Lyon Part-Dieu",
      long_name = "Lyon Part-Dieu Station",
      time_zone = "Europe/Paris",
      latitude = Some(45.760696),
      longitude = Some(4.859054),
      destinations_ids = List(1, 3, 4),
      address = Some("Place Charles Béraudier, 69003 Lyon")
    )
  ))
  
  def getFares(
    originId: Option[Int] = None,
    destinationId: Option[Int] = None,
    date: Option[LocalDate] = None,
    startDate: Option[LocalDate] = None,
    endDate: Option[LocalDate] = None,
    currencies: List[String] = List.empty,
    updatedAfter: Option[LocalDateTime] = None
  ): Task[List[Fare]] = ZIO.succeed(List(
    Fare(
      id = 1,
      updated_at = "2024-01-01T10:00:00Z",
      origin_id = 1,
      destination_id = 2,
      departure = "2024-01-15T08:30:00+01:00",
      arrival = "2024-01-15T12:45:00+01:00",
      available = true,
      price_cents = 2599,
      price_currency = "EUR",
      legs = List(
        Leg(
          origin_id = 1,
          destination_id = 2,
          departure = "2024-01-15T08:30:00+01:00",
          arrival = "2024-01-15T12:45:00+01:00",
          bus_number = "BB123"
        )
      )
    )
  ))
  
  def searchRoutes(request: SearchRequest): Task[List[Trip]] = ZIO.succeed(List(
    Trip(
      id = "trip-123",
      origin_id = request.origin_id,
      destination_id = request.destination_id,
      departure = "2024-01-15T08:30:00+01:00",
      arrival = "2024-01-15T12:45:00+01:00",
      available = true,
      price_cents = 2599,
      price_currency = "EUR",
      is_promo = Some(false),
      is_refundable = Some(true),
      legs = List(
        Leg(
          origin_id = request.origin_id,
          destination_id = request.destination_id,
          departure = "2024-01-15T08:30:00+01:00",
          arrival = "2024-01-15T12:45:00+01:00",
          bus_number = "BB123"
        )
      ),
      passengers = request.passengers.getOrElse(List(Passenger("1", 35))).map { passenger =>
        PassengerResult(
          id = passenger.id,
          price_cents = 2599,
          price_currency = "EUR",
          fare_name = "Adult",
          fare_description = "Standard adult fare"
        )
      }
    )
  ))
  
  def searchRoutes(
    originId: Int,
    destinationId: Int,
    date: LocalDate,
    passengers: List[Passenger] = List(Passenger("1", 35)),
    currency: String = "EUR",
    transfers: Int = 0
  ): Task[List[Trip]] = {
    val request = SearchRequest(
      origin_id = originId,
      destination_id = destinationId,
      date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
      currency = Some(currency),
      passengers = Some(passengers),
      transfers = Some(transfers)
    )
    searchRoutes(request)
  }
}
