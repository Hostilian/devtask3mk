package com.example

import org.fusesource.jansi.AnsiConsole
import org.fusesource.jansi.Ansi.{ansi, Color}
import zio.{ZIO, Console, UIO, Runtime}
import io.circe.parser.*
import cats.effect.IO
import zio.interop.catz.*

object Cli {
  def prettyPrint[A](doc: Document[A], indent: Int = 0): String = {
    val spaces = "  " * indent
    doc match {
      case Leaf(value) => s"${spaces}Leaf($value)"
      case Horizontal(cells) =>
        s"${spaces}Horizontal(\n${cells.map(prettyPrint(_, indent + 1)).mkString("\n")}\n${spaces})"
      case Vertical(cells) =>
        s"${spaces}Vertical(\n${cells.map(prettyPrint(_, indent + 1)).mkString("\n")}\n${spaces})"
      case Empty => s"${spaces}Empty"
    }
  }

  def runInteractive: ZIO[Any, Throwable, Unit] = for {
    _ <- ZIO.attempt(AnsiConsole.systemInstall())
    input <- Console.readLine("Enter JSON document (or 'exit' to quit): ")
    _ <- if (input == "exit") ZIO.unit else for {
      doc <- ZIO.fromEither(parse(input).flatMap(_.as[Document[String]]))
      _ <- Console.printLine(ansi.fg(Color.GREEN).a(prettyPrint(doc)).reset.toString)
    } yield ()
    _ <- if (input != "exit") runInteractive else ZIO.unit
  } yield ()

  def main(args: Array[String]): Unit = {
    val runtime = Runtime.default
    runtime.unsafeRun(runInteractive)
  }
}
