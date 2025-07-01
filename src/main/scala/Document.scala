package com.example

import cats.Monad
import cats.syntax.all.*
import io.circe.{Decoder, Encoder, Json, DecodingFailure}
import io.circe.syntax.*

sealed trait Document[A]
case class Leaf[A](value: A) extends Document[A]
case class Horizontal[A](cells: List[Document[A]]) extends Document[A]
case class Vertical[A](cells: List[Document[A]]) extends Document[A]
case class Empty[A]() extends Document[A]

object Document {
  // Functor
  def map[A, B](doc: Document[A])(f: A => B): Document[B] = doc match {
    case Leaf(value) => Leaf(f(value))
    case Horizontal(cells) => Horizontal(cells.map(map(_)(f)))
    case Vertical(cells) => Vertical(cells.map(map(_)(f)))
    case Empty() => Empty()
  }

  // Applicative/Traversable
  def traverse[F[_]: Monad, A, B](doc: Document[A])(f: A => F[B]): F[Document[B]] = doc match {
    case Leaf(value) => f(value).map(Leaf(_))
    case Horizontal(cells) => cells.traverse(traverse(_)(f)).map(Horizontal(_))
    case Vertical(cells) => cells.traverse(traverse(_)(f)).map(Vertical(_))
    case Empty() => Monad[F].pure(Empty())
  }

  // Catamorphism
  def fold[A, B](doc: Document[A])(f: A => B)(g: List[B] => B)(h: List[B] => B): B = doc match {
    case Leaf(value) => f(value)
    case Horizontal(cells) => g(cells.map(fold(_)(f)(g)(h)))
    case Vertical(cells) => h(cells.map(fold(_)(f)(g)(h)))
    case Empty() => g(Nil)
  }

  // Semigroup
  implicit def semigroup[A]: cats.Semigroup[Document[A]] = (x: Document[A], y: Document[A]) => (x, y) match {
    case (Empty(), doc) => doc
    case (doc, Empty()) => doc
    case (Horizontal(c1), Horizontal(c2)) => Horizontal(c1 ++ c2)
    case (Vertical(c1), Vertical(c2)) => Vertical(c1 ++ c2)
    case (Horizontal(c1), doc) => Horizontal(c1 :+ doc)
    case (doc, Horizontal(c2)) => Horizontal(doc +: c2)
    case (Vertical(c1), doc) => Vertical(c1 :+ doc)
    case (doc, Vertical(c2)) => Vertical(doc +: c2)
    case _ => Horizontal(List(x, y))
  }

  // Monoid
  implicit def monoid[A]: cats.Monoid[Document[A]] = new cats.Monoid[Document[A]] {
    def combine(x: Document[A], y: Document[A]): Document[A] = semigroup.combine(x, y)
    def empty: Document[A] = Empty()
  }

  // Serialization
  implicit def encoder[A: Encoder]: Encoder[Document[A]] = {
    case Leaf(value) => Json.obj("type" -> "leaf".asJson, "value" -> value.asJson)
    case Horizontal(cells) => Json.obj("type" -> "horizontal".asJson, "cells" -> cells.asJson)
    case Vertical(cells) => Json.obj("type" -> "vertical".asJson, "cells" -> cells.asJson)
    case Empty() => Json.obj("type" -> "empty".asJson)
  }

  implicit def decoder[A: Decoder]: Decoder[Document[A]] = Decoder.instance { c =>
    c.get[String]("type") flatMap {
      case "leaf" => c.get[A]("value").map(Leaf(_))
      case "horizontal" => c.get[List[Document[A]]]("cells").map(Horizontal(_))
      case "vertical" => c.get[List[Document[A]]]("cells").map(Vertical(_))
      case "empty" => Right(Empty())
      case t => Left(DecodingFailure(s"Unknown type: $t", c.history))
    }
  }
}
