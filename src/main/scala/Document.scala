package com.example

import cats.Applicative
import cats.Functor
import cats.Monad
import cats.Traverse
import cats.syntax.all.*
import io.circe.Decoder
import io.circe.DecodingFailure
import io.circe.Encoder
import io.circe.Json
import io.circe.syntax.*

// Data type for documents split horizontally or vertically
sealed trait Document[A]

case class Leaf[A](value: A)                       extends Document[A]
case class Horizontal[A](cells: List[Document[A]]) extends Document[A]
case class Vertical[A](cells: List[Document[A]])   extends Document[A]
case class Empty[A]()                              extends Document[A]

// Some error types for validation
sealed trait DocumentError
case object EmptyDocumentError                            extends DocumentError
case class ParseError(message: String)                    extends DocumentError
case class ValidationError(field: String, reason: String) extends DocumentError

// Document operations
sealed trait DocumentOp[A]
case class Insert[A](value: A, position: Position)    extends DocumentOp[A]
case class Delete[A](position: Position)              extends DocumentOp[A]
case class Update[A](position: Position, newValue: A) extends DocumentOp[A]

case class Position(row: Int, col: Int)

object Document {
  // Functor instance - lets you map over the values
  implicit val documentFunctor: Functor[Document] = new Functor[Document] {
    def map[A, B](fa: Document[A])(f: A => B): Document[B] = fa match {
      case Leaf(value)       => Leaf(f(value))
      case Horizontal(cells) => Horizontal(cells.map(map(_)(f)))
      case Vertical(cells)   => Vertical(cells.map(map(_)(f)))
      case Empty()           => Empty()
    }
  }

  def map[A, B](doc: Document[A])(f: A => B): Document[B] =
    documentFunctor.map(doc)(f)

  // Traverse instance - this is the important one for the assignment
  implicit val documentTraverse: Traverse[Document] = new Traverse[Document] {
    def traverse[G[_]: Applicative, A, B](fa: Document[A])(f: A => G[B]): G[Document[B]] = fa match {
      case Leaf(value)       => f(value).map(Leaf(_))
      case Horizontal(cells) => cells.traverse(traverse(_)(f)).map(Horizontal(_))
      case Vertical(cells)   => cells.traverse(traverse(_)(f)).map(Vertical(_))
      case Empty()           => Applicative[G].pure(Empty())
    }

    def foldLeft[A, B](fa: Document[A], b: B)(f: (B, A) => B): B = fa match {
      case Leaf(value)       => f(b, value)
      case Horizontal(cells) => cells.foldLeft(b)((acc, cell) => foldLeft(cell, acc)(f))
      case Vertical(cells)   => cells.foldLeft(b)((acc, cell) => foldLeft(cell, acc)(f))
      case Empty()           => b
    }

    def foldRight[A, B](fa: Document[A], lb: cats.Eval[B])(f: (A, cats.Eval[B]) => cats.Eval[B]): cats.Eval[B] =
      fa match {
        case Leaf(value)       => f(value, lb)
        case Horizontal(cells) => cats.Foldable[List].foldRight(cells, lb)((cell, acc) => foldRight(cell, acc)(f))
        case Vertical(cells)   => cats.Foldable[List].foldRight(cells, lb)((cell, acc) => foldRight(cell, acc)(f))
        case Empty()           => lb
      }
  }

  // The assignment function f
  def f[M[_]: Monad, A, B](g: A => M[B])(doc: Document[A]): M[Document[B]] =
    documentTraverse.traverse(doc)(g)

  def traverse[F[_]: Applicative, A, B](doc: Document[A])(g: A => F[B]): F[Document[B]] =
    documentTraverse.traverse(doc)(g)

  // Catamorphism - tears down the structure
  def cata[A, B](doc: Document[A])(
      leafAlg: A => B,
      horizontalAlg: List[B] => B,
      verticalAlg: List[B] => B,
      emptyAlg: () => B
  ): B = doc match {
    case Leaf(value)       => leafAlg(value)
    case Horizontal(cells) => horizontalAlg(cells.map(cata(_)(leafAlg, horizontalAlg, verticalAlg, emptyAlg)))
    case Vertical(cells)   => verticalAlg(cells.map(cata(_)(leafAlg, horizontalAlg, verticalAlg, emptyAlg)))
    case Empty()           => emptyAlg()
  }

  def fold[A, B](doc: Document[A])(f: A => B)(g: List[B] => B)(h: List[B] => B): B =
    cata(doc)(f, g, h, () => g(Nil))

  // Anamorphism - builds up structure
  def ana[A, B](seed: B)(
      coalg: B => Either[A, (List[B], Boolean)] // Left = Leaf, Right = (children, isHorizontal)
  ): Document[A] = coalg(seed) match {
    case Left(value)              => Leaf(value)
    case Right((children, true))  => Horizontal(children.map(child => ana(child)(coalg)))
    case Right((children, false)) => Vertical(children.map(child => ana(child)(coalg)))
  }

  // Semigroup and Monoid - for combining documents
  implicit def semigroup[A]: cats.Semigroup[Document[A]] = new cats.Semigroup[Document[A]] {
    def combine(x: Document[A], y: Document[A]): Document[A] = (x, y) match {
      case (Empty(), d)                     => d
      case (d, Empty())                     => d
      case (Horizontal(c1), Horizontal(c2)) => Horizontal(c1 ++ c2)
      case (Vertical(c1), Vertical(c2))     => Vertical(c1 ++ c2)
      case (d1, d2)                         => Vertical(List(d1, d2))
    }
  }

  implicit def monoid[A]: cats.Monoid[Document[A]] = new cats.Monoid[Document[A]] {
    def combine(x: Document[A], y: Document[A]): Document[A] = semigroup.combine(x, y)
    def empty: Document[A]                                   = Empty()
  }

  // Monad instance
  implicit val documentMonad: Monad[Document] = new Monad[Document] {
    def pure[A](x: A): Document[A] = Leaf(x)

    def flatMap[A, B](fa: Document[A])(f: A => Document[B]): Document[B] = fa match {
      case Leaf(value)       => f(value)
      case Horizontal(cells) => Horizontal(cells.map(flatMap(_)(f)))
      case Vertical(cells)   => Vertical(cells.map(flatMap(_)(f)))
      case Empty()           => Empty()
    }

    def tailRecM[A, B](a: A)(f: A => Document[Either[A, B]]): Document[B] =
      f(a) match {
        case Leaf(Left(a2)) => tailRecM(a2)(f)
        case Leaf(Right(b)) => Leaf(b)
        case Horizontal(cells) =>
          val results = cells.map {
            case Leaf(Left(a2)) => tailRecM(a2)(f)
            case Leaf(Right(b)) => Leaf(b)
            case other =>
              flatMap(other) {
                case Left(a2) => tailRecM(a2)(f)
                case Right(b) => Leaf(b)
              }
          }
          Horizontal(results)
        case Vertical(cells) =>
          val results = cells.map {
            case Leaf(Left(a2)) => tailRecM(a2)(f)
            case Leaf(Right(b)) => Leaf(b)
            case other =>
              flatMap(other) {
                case Left(a2) => tailRecM(a2)(f)
                case Right(b) => Leaf(b)
              }
          }
          Vertical(results)
        case Empty() => Empty()
      }
  }

  // Some helper methods
  def flatMap[A, B](doc: Document[A])(f: A => Document[B]): Document[B] =
    documentMonad.flatMap(doc)(f)

  def pure[A](value: A): Document[A] =
    documentMonad.pure(value)

  def map2[A, B, C](docA: Document[A], docB: Document[B])(f: (A, B) => C): Document[C] =
    flatMap(docA)(a => map(docB)(b => f(a, b)))

  def foldLeft[A, B](doc: Document[A], initial: B)(f: (B, A) => B): B =
    documentTraverse.foldLeft(doc, initial)(f)

  def foldRight[A, B](doc: Document[A], initial: B)(f: (A, B) => B): B =
    documentTraverse.foldRight(doc, cats.Eval.now(initial))((a, evalB) => evalB.map(b => f(a, b))).value

  // Validation stuff
  def validateDocument[F[_]: Applicative](doc: Document[String]): F[Document[String]] = {
    val validationRules: String => cats.data.ValidatedNel[String, String] = { value =>
      if (value.nonEmpty) cats.data.Validated.valid(value)
      else cats.data.Validated.invalidNel("Empty value not allowed")
    }

    import cats.data.ValidatedNel
    type ValidationResult[A] = ValidatedNel[String, A]

    traverse[ValidationResult, String, String](doc)(validationRules) match {
      case cats.data.Validated.Valid(validDoc) => Applicative[F].pure(validDoc)
      case cats.data.Validated.Invalid(errors) =>
        Applicative[F].pure(doc) // Just return original for now
    }
  }

  def parseDocument[A](
      input: String
  )(parser: String => Either[DocumentError, A]): Either[DocumentError, Document[A]] = {
    parser(input).map(Leaf(_))
  }

  // Phantom types for compile-time safety
  sealed trait DocumentState
  trait Valid   extends DocumentState
  trait Invalid extends DocumentState

  case class TypedDocument[A, S <: DocumentState](doc: Document[A])

  def validateAtCompileTime[A](doc: Document[A]): TypedDocument[A, Valid] =
    TypedDocument[A, Valid](doc)

  def processValidDocument[A](typedDoc: TypedDocument[A, Valid]): Document[A] =
    typedDoc.doc

  // JSON serialization
  implicit def encoder[A: Encoder]: Encoder[Document[A]] = {
    case Leaf(value)       => Json.obj("type" -> "leaf".asJson, "value" -> value.asJson)
    case Horizontal(cells) => Json.obj("type" -> "horizontal".asJson, "cells" -> cells.asJson)
    case Vertical(cells)   => Json.obj("type" -> "vertical".asJson, "cells" -> cells.asJson)
    case Empty()           => Json.obj("type" -> "empty".asJson)
  }

  implicit def decoder[A: Decoder]: Decoder[Document[A]] = Decoder.instance { c =>
    c.get[String]("type") flatMap {
      case "leaf"       => c.get[A]("value").map(Leaf(_))
      case "horizontal" => c.get[List[Document[A]]]("cells").map(Horizontal(_))
      case "vertical"   => c.get[List[Document[A]]]("cells").map(Vertical(_))
      case "empty"      => Right(Empty())
      case t            => Left(DecodingFailure(s"Unknown type: $t", c.history))
    }
  }
}
