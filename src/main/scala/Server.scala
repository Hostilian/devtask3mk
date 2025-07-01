package com.example

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

object Server {
  val dsl = Http4sDsl[Task]
  import dsl.*

  implicit val documentEntityDecoder: EntityDecoder[Task, Document[String]] = jsonOf[Task, Document[String]]

  val routes: HttpRoutes[Task] = HttpRoutes.of[Task] {
    case req @ POST -> Root / "render" =>
      for {
        doc <- req.as[Document[String]]
        resp <- Ok(Cli.prettyPrint(doc))
      } yield resp
    case req @ POST -> Root / "validate" =>
      for {
        doc <- req.as[Document[String]]
        resp <- Ok("Valid document")
      } yield resp
    case GET -> Root / "health" =>
      Ok("Server is running")
  }

  def runServer: Task[Unit] =
    EmberServerBuilder
      .default[Task]
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
