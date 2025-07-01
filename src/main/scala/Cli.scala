package com.example

import org.fusesource.jansi.AnsiConsole
import org.fusesource.jansi.Ansi.{ansi, Color}
import zio.{ZIO, Console, UIO, Runtime, Unsafe}
import io.circe.parser.*
import cats.effect.IO
import zio.interop.catz.*
import scala.io.StdIn

object Cli {
  def prettyPrint[A](doc: Document[A], indent: Int = 0): String = {
    val spaces = "  " * indent
    doc match {
      case Leaf(value) => s"${spaces}Leaf($value)"
      case Horizontal(cells) =>
        s"${spaces}Horizontal(\n${cells.map(prettyPrint(_, indent + 1)).mkString("\n")}\n${spaces})"
      case Vertical(cells) =>
        s"${spaces}Vertical(\n${cells.map(prettyPrint(_, indent + 1)).mkString("\n")}\n${spaces})"
      case Empty() => s"${spaces}Empty"
    }
  }

  def processDocument(input: String): ZIO[Any, Throwable, Unit] = for {
    doc <- ZIO.fromEither(parse(input).flatMap(_.as[Document[String]]))
    _ <- Console.printLine(ansi().nn.fg(Color.GREEN).nn.a(prettyPrint(doc)).nn.reset().toString)
  } yield ()

  def runFromStdin: ZIO[Any, Throwable, Unit] = for {
    _ <- ZIO.attempt(AnsiConsole.systemInstall())
    lines <- ZIO.attempt {
      Iterator.continually(StdIn.readLine()).takeWhile(Option(_).isDefined).mkString("\n")
    }
    _ <- if (lines.nonEmpty) processDocument(lines) else ZIO.unit
  } yield ()

  def runInteractive: ZIO[Any, Throwable, Unit] = for {
    _ <- ZIO.attempt(AnsiConsole.systemInstall())
    input <- Console.readLine("Enter JSON document (or 'exit' to quit): ")
    _ <- if (input == "exit") ZIO.unit else processDocument(input.nn)
    _ <- if (input != "exit") runInteractive else ZIO.unit
  } yield ()

  def main(args: Array[String]): Unit = {
    val runtime = Runtime.default
    val program = ZIO.attempt(System.in.nn.available() > 0).flatMap { hasInput =>
      if (hasInput) runFromStdin else runInteractive
    }.catchAll { _ =>
      // If we can't determine input availability, try stdin first, then fall back to interactive
      runFromStdin.catchAll(_ => runInteractive)
    }

    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.run(program).getOrThrow()
    }
  }
}
