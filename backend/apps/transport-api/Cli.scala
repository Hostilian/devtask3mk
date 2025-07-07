package com.example

import cats.effect.IO
import io.circe.parser.*
import org.fusesource.jansi.Ansi.Color
import org.fusesource.jansi.Ansi.ansi
import org.fusesource.jansi.AnsiConsole
import zio.Console
import zio.Runtime
import zio.UIO
import zio.Unsafe
import zio.ZIO
import zio.interop.catz.*

import scala.io.StdIn

object Cli {

  def showWelcome(): ZIO[Any, Throwable, Unit] = for {
    _ <- Console.printLine(ansi().nn.fg(Color.CYAN).nn.a("📊 Document Matrix CLI").nn.reset().toString)
    _ <- Console.printLine("Built with functional programming concepts in Scala")
    _ <- Console.printLine("Enter JSON documents to see them parsed and pretty-printed")
    _ <- Console.printLine("")
    _ <- Console.printLine("Example JSON:")
    _ <- Console.printLine(
      """{"type": "horizontal", "cells": [{"type": "leaf", "value": "Hello"}, {"type": "leaf", "value": "World"}]}"""
    )
    _ <- Console.printLine("")
  } yield ()

  def prettyPrint[A](doc: Document[A], indent: Int = 0): String = {
    val spaces = "  " * indent
    doc match {
      case Leaf(value) => s"${spaces}Leaf($value)"
      case Horizontal(cells) =>
        s"${spaces}Horizontal:\n${cells.map(prettyPrint(_, indent + 1)).mkString("\n")}"
      case Vertical(cells) =>
        s"${spaces}Vertical:\n${cells.map(prettyPrint(_, indent + 1)).mkString("\n")}"
      case Empty() => s"${spaces}Empty"
    }
  }

  def processDocument(input: String): ZIO[Any, Throwable, Unit] = for {
    doc <- ZIO.fromEither(parse(input).flatMap(_.as[Document[String]]))
    _   <- Console.printLine(ansi().nn.fg(Color.GREEN).nn.a(prettyPrint(doc)).nn.reset().toString)
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
    _ <- showWelcome()
    _ <- interactiveLoop
  } yield ()

  def interactiveLoop: ZIO[Any, Throwable, Unit] = for {
    input <- Console.readLine("📝 Enter JSON document (or 'exit' to quit): ")
    _ <-
      if (input eq null) {
        ZIO.unit
      } else if (input == "exit") {
        Console.printLine("👋 Thanks for using Document Matrix!")
      } else {
        processDocument(input) *> interactiveLoop
      }
  } yield ()

  def main(args: Array[String]): Unit = {
    val runtime = Runtime.default
    val program = ZIO
      .attempt(System.in.nn.available() > 0)
      .flatMap { hasInput =>
        if (hasInput) runFromStdin else runInteractive
      }
      .catchAll { _ =>
        // If we can't determine input availability, try stdin first, then fall back to interactive
        runFromStdin.catchAll(_ => runInteractive)
      }

    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.run(program).getOrThrow()
    }
  }
}
