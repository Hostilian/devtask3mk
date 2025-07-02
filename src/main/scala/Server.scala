package com.example

import cats.syntax.semigroupk._ // for <+>
import com.example.Document._ // for Vertical, prettyPrint, etc.
import org.http4s.QueryParamDecoder
import org.http4s.ParseFailure
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import io.circe.syntax.*
import org.http4s.EntityDecoder
import org.http4s.HttpRoutes
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import zio.Runtime
import zio.Task
import zio.Unsafe
import zio.interop.catz.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import zio.json._
import zio.json.DecoderOps
import scala.util.Try
import java.time.LocalDate
import org.http4s.QueryParamDecoder
import org.http4s.ParseFailure

// Implicit decoder for LocalDate query params
implicit val localDateQueryParamDecoder: QueryParamDecoder[LocalDate] =
  QueryParamDecoder[String].emap { str =>
    val safeStr = Option(str).getOrElse("")
    Try(LocalDate.parse(safeStr)).toEither.left.map(t => ParseFailure("Invalid date", t.getMessage.nn)).map(_.nn)
  }.map(_.nn)
object DateParam extends org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher[LocalDate]("date")

// Query parameter extractors (move outside Server object to avoid cyclic reference)
import org.http4s.dsl.impl.{QueryParamDecoderMatcher, OptionalQueryParamDecoderMatcher}
object OriginParam extends QueryParamDecoderMatcher[Int]("origin")
object DestinationParam extends QueryParamDecoderMatcher[Int]("destination")

object Server {
  val dsl = Http4sDsl[Task]
  import dsl.*

  implicit val documentEntityDecoder: EntityDecoder[Task, Document[String]] = jsonOf[Task, Document[String]]

  // BlaBlaCar Bus API integration routes
  val busApiRoutes: HttpRoutes[Task] = HttpRoutes.of[Task] {
    // Search routes endpoint
    case req @ POST -> Root / "api" / "bus" / "search" =>
      import BlaBlaBusCodecs.given
      for {
        bodyStr <- req.as[String]
        searchReq = bodyStr.fromJson[SearchRequest]
        result <- searchReq match {
          case Right(request) =>
            val mockTrips = List(
              Trip(
                id = "demo-trip-1",
                origin_id = request.origin_id,
                destination_id = request.destination_id,
                departure = s"${request.date}T08:30:00+01:00",
                arrival = s"${request.date}T12:45:00+01:00",
                available = true,
                price_cents = 2599,
                price_currency = "EUR",
                legs = List.empty,
                passengers = List.empty
              )
            )
            val doc = BlaBlaBusDocumentProcessor.searchResultsToDocument(
              request.origin_id,
              request.destination_id,
              LocalDate.parse(request.date.nn).nn,
              mockTrips
            )
            Ok(Cli.prettyPrint(doc))
          case _ =>
            BadRequest(s"Invalid search request: $searchReq")
        }
      } yield result

    // Get stops endpoint
    case GET -> Root / "api" / "bus" / "stops" =>
      val mockStops = List(
        BusStop(
          id = 1,
          short_name = "Paris Bercy",
          long_name = "Paris Bercy Station",
          time_zone = "Europe/Paris",
          latitude = Some(48.838424),
          longitude = Some(2.382411),
          destinations_ids = List(2, 3),
          address = Some("48 bis Boulevard de Bercy 75012 Paris"),
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
          is_meta_gare = None,
          stops = None,
          _carrier_id = None
        )
      )
      val stopsDoc = Vertical(List(
        BusStop(
          id = 1,
          short_name = "Paris Bercy",
          long_name = "Paris Bercy Station",
          time_zone = "Europe/Paris",
          latitude = Some(48.838424),
          longitude = Some(2.382411),
          destinations_ids = List(2, 3),
          address = Some("48 bis Boulevard de Bercy 75012 Paris"),
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
          is_meta_gare = None,
          stops = None,
          _carrier_id = None
        )
      ))
      Ok(Cli.prettyPrint(stopsDoc))

    // Display trip details
    case GET -> Root / "api" / "bus" / "trip" / tripId =>
      val mockTrip = Trip(
        id = tripId,
        origin_id = 1,
        destination_id = 2,
        departure = "2024-01-15T08:30:00+01:00",
        arrival = "2024-01-15T12:45:00+01:00",
        available = true,
        price_cents = 2599,
        price_currency = "EUR",
        is_promo = Some(false),
        is_refundable = Some(true),
        legs = List.empty,
        passengers = List.empty
      )
      val tripDoc = BlaBlaBusDocumentProcessor.tripToDocument(mockTrip)
      Ok(Cli.prettyPrint(tripDoc))

    // Quick search with query parameters
    case GET -> Root / "api" / "bus" / "quick-search" :?
        OriginParam(origin) +& DestinationParam(destination) +& DateParam(date) =>
      val tomorrow = LocalDate.now().nn.plusDays(1)
      val searchDate = date.getOrElse(LocalDate.now().nn.plusDays(1)).nn
      val mockTrips = List(
        Trip(
          id = s"quick-${origin}-${destination}",
          origin_id = origin,
          destination_id = destination,
          departure = s"${searchDate}T08:30:00+01:00",
          arrival = s"${searchDate}T12:45:00+01:00",
          available = true,
          price_cents = 2599,
          price_currency = "EUR",
          legs = List.empty,
          passengers = List.empty
        )
      )
      val resultsDoc = BlaBlaBusDocumentProcessor.searchResultsToDocument(
        origin, destination, searchDate, mockTrips
      )
      Ok(Cli.prettyPrint(resultsDoc))
  }

  val routes: HttpRoutes[Task] = HttpRoutes.of[Task] {
    case req @ POST -> Root / "render" =>
      for {
        doc  <- req.as[Document[String]]
        resp <- Ok(Cli.prettyPrint(doc))
      } yield resp
    case req @ POST -> Root / "validate" =>
      for {
        doc  <- req.as[Document[String]]
        resp <- Ok("Valid document")
      } yield resp
    case GET -> Root / "health" =>
      Ok("Server is running")
  } <+> busApiRoutes // Combine regular routes with bus API routes

  def runServer: Task[Unit] =
    EmberServerBuilder
      .default[Task] // Note: Deprecation warning - but still functional
      .withHttpApp(routes.orNotFound)
      .withHost(Host.fromString("localhost").get)
      .withPort(Port.fromInt(8081).get)
      .build
      .useForever

  def main(args: Array[String]): Unit = {
    val runtime = Runtime.default
    println("Starting server on http://localhost:8081")
    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.run(runServer).getOrThrow()
    }
  }
}
