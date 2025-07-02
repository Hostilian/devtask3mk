package com.example

import zio._
import zio.http._
import zio.json._
import zio.json.ast.Json
import java.time.{LocalDateTime, LocalDate}
import java.time.format.DateTimeFormatter
import scala.math.BigDecimal
import scala.util.chaining._
import scala.language.unsafeNulls

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
case class BusApiParseError(message: String) extends BlaBlaBusApiError
case class NetworkError(cause: Throwable) extends BlaBlaBusApiError
case class RateLimitError(retryAfter: Option[Duration] = None) extends BlaBlaBusApiError

// API Client Interface
trait BlaBlaBusApiClient {
  def getStops(): Task[List[BusStop]]
  def getFares(
    updatedAfter: Option[LocalDateTime] = None
  ): Task[List[Fare]]
  def searchRoutes(request: SearchRequest): Task[List[Trip]]
  def searchRoutes(
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
    Header.Custom("Authorization", s"Token token=${config.apiKey}"),
    Header.Accept(MediaType.application.json)
  )

  private def makeRequest[T: JsonDecoder](request: Request): Task[T] =
    ZIO.scoped {
      client
        .request(request)
        .timeout(config.timeout)
        .someOrFail(HttpError(Status.RequestTimeout, "Request timed out"))
        .retry(Schedule.recurs(config.retries))
        .mapError {
          case e: BlaBlaBusApiError => e
          case e: Throwable       => NetworkError(e)
        }
        .flatMap { response =>
          if (response.status.isSuccess) {
            response.body.asString.mapError(NetworkError).flatMap { body =>
              ZIO
                .fromEither(body.fromJson[T])
                .mapError(e => BusApiParseError(s"Failed to parse response: $e. Body: $body"))
            }
          } else {
            response.body.asString.mapError(NetworkError).flatMap(body =>
              ZIO.fail(HttpError(response.status, s"Request failed with status ${response.status}. Body: $body"))
            )
          }
        }
    }

  def getStops(): Task[List[BusStop]] = {
    val url = s"${config.baseUrl}/${config.version}/stops"
    val effect: ZIO[Scope, BlaBlaBusApiError, List[BusStop]] = for {
      decodedUrl <- ZIO.fromEither(URL.decode(url)).mapError(e => BusApiParseError(s"Invalid URL: $e"))
      request = Request.get(decodedUrl).addHeaders(baseHeaders)
      stopsResponse <- makeRequest[StopsResponse](request)
    } yield stopsResponse.stops
    effect.provide(ZLayer.succeed(client) >>> Client.live.orDie)
  }

  def getFares(
    updatedAfter: Option[LocalDateTime] = None
  ): Task[List[Fare]] = {
    val queryParams = buildFareQueryParams(updatedAfter)
    val url = s"${config.baseUrl}/${config.version}/fares$queryParams"
    val effect: ZIO[Scope, BlaBlaBusApiError, List[Fare]] = for {
      decodedUrl <- ZIO.fromEither(URL.decode(url)).mapError(e => BusApiParseError(s"Invalid URL: $e"))
      request = Request.get(decodedUrl).addHeaders(baseHeaders)
      faresResponse <- makeRequest[FaresResponse](request)
    } yield faresResponse.fares
    effect.provide(ZLayer.succeed(client) >>> Client.live.orDie)
  }

  def searchRoutes(request: SearchRequest): Task[List[Trip]] = {
    val url = s"${config.baseUrl}/${config.version}/search"
    val requestBody = request.toJson
    val effect: ZIO[Scope, BlaBlaBusApiError, List[Trip]] = for {
      decodedUrl <- ZIO.fromEither(URL.decode(url)).mapError(e => BusApiParseError(s"Invalid URL: $e"))
      httpRequest = Request
        .post(decodedUrl, Body.fromString(requestBody))
        .addHeaders(baseHeaders)
        .addHeader(Header.ContentType(MediaType.application.json))
      tripsResponse <- makeRequest[TripsResponse](httpRequest)
    } yield tripsResponse.trips
    effect.provide(ZLayer.succeed(client) >>> Client.live.orDie)
  }

  def searchRoutes(
    transfers: Int = 0
  ): Task[List[Trip]] = {
    ZIO.succeed(List.empty)
  }

  private def buildFareQueryParams(
    updatedAfter: Option[LocalDateTime]
  ): String = {
    updatedAfter.map { dt =>
      s"?updated_after=${dt.format(DateTimeFormatter.ISO_DATE_TIME)}"
    }.getOrElse("")
  }
}

// Document Processing Integration
object BlaBlaBusDocumentProcessor {
  import Document._
  def stopToDocument(stop: BusStop): Document[String] = {
    Vertical(List(
      Horizontal(List(Leaf("Stop ID:"), Leaf(stop.id.toString))),
      Horizontal(List(Leaf("Name:"), Leaf(stop.long_name))),
      Horizontal(List(Leaf("Address:"), Leaf(stop.address.getOrElse("N/A")))),
      Horizontal(List(Leaf("Timezone:"), Leaf(stop.time_zone)))
    ))
  }

  def fareToDocument(fare: Fare): Document[String] = {
    Vertical(List(
      Horizontal(List(Leaf("Fare ID:"), Leaf(fare.id.toString))),
      Horizontal(List(Leaf("From:"), Leaf(fare.origin_id.toString))),
      Horizontal(List(Leaf("To:"), Leaf(fare.destination_id.toString))),
      Horizontal(List(Leaf("Price:"), Leaf(s"${fare.price_cents / 100.0} ${fare.price_currency}")))
    ))
  }

  def tripToDocument(trip: Trip): Document[String] = {
    Vertical(List(
      Horizontal(List(Leaf("Trip ID:"), Leaf(trip.id))),
      Horizontal(List(Leaf("From:"), Leaf(trip.origin_id.toString))),
      Horizontal(List(Leaf("To:"), Leaf(trip.destination_id.toString))),
      Horizontal(List(Leaf("Departure:"), Leaf(trip.departure))),
      Horizontal(List(Leaf("Arrival:"), Leaf(trip.arrival))),
      Horizontal(List(Leaf("Price:"), Leaf(s"${trip.price_cents / 100.0} ${trip.price_currency}")))
    ))
  }

  def searchResultsToDocument(origin: Int, destination: Int, date: LocalDate, trips: List[Trip]): Document[String] = {
    val header: Document[String] = Horizontal(List(Leaf(s"Search Results for $origin to $destination on $date")))
    val tripsDocs = trips.map(tripToDocument)
    Vertical(header :: tripsDocs)
  }
}

// ZIO Layer for dependency injection
object BlaBlaBusApiClient {
  val layer: ZLayer[BlaBlaBusConfig & Client, Nothing, BlaBlaBusApiClient] =
    ZLayer.fromZIO {
      for {
        config <- ZIO.service[BlaBlaBusConfig]
        client <- ZIO.service[Client]
      } yield new BlaBlaBusApiClientImpl(config, client)
    }

  val mockLayer: ZLayer[Any, Nothing, BlaBlaBusApiClient] =
    ZLayer.succeed(new MockBlaBlaBusApiClient)
}

// Mock implementation for testing
class MockBlaBlaBusApiClient extends BlaBlaBusApiClient {
  def getStops(): Task[List[BusStop]] = ZIO.succeed(List.empty)
  def getFares(updatedAfter: Option[LocalDateTime]): Task[List[Fare]] = ZIO.succeed(List.empty)
  def searchRoutes(request: SearchRequest): Task[List[Trip]] = ZIO.succeed(List.empty)
  def searchRoutes(transfers: Int): Task[List[Trip]] = ZIO.succeed(List.empty)
}
