package com.example

import cats.{~>, Id, Monad}
import cats.free.Free
import cats.syntax.all.*

// ====== FREE MONADS ======
// DSL for document operations using Free monads
sealed trait DocumentAlgebra[A]

case class CreateLeaf[A](value: A)                                    extends DocumentAlgebra[Document[A]]
case class CreateHorizontal[A](docs: List[Document[A]])               extends DocumentAlgebra[Document[A]]
case class CreateVertical[A](docs: List[Document[A]])                 extends DocumentAlgebra[Document[A]]
case class CombineDocuments[A](left: Document[A], right: Document[A]) extends DocumentAlgebra[Document[A]]
case class ValidateDocument[A](doc: Document[A]) extends DocumentAlgebra[Either[DocumentError, Document[A]]]

object DocumentDSL {
  type DocumentProgram[A] = Free[DocumentAlgebra, A]

  // Smart constructors
  def createLeaf[A](value: A): DocumentProgram[Document[A]] =
    Free.liftF(CreateLeaf(value))

  def createHorizontal[A](docs: List[Document[A]]): DocumentProgram[Document[A]] =
    Free.liftF(CreateHorizontal(docs))

  def createVertical[A](docs: List[Document[A]]): DocumentProgram[Document[A]] =
    Free.liftF(CreateVertical(docs))

  def combineDocuments[A](left: Document[A], right: Document[A]): DocumentProgram[Document[A]] =
    Free.liftF(CombineDocuments(left, right))

  def validateDocument[A](doc: Document[A]): DocumentProgram[Either[DocumentError, Document[A]]] =
    Free.liftF(ValidateDocument(doc))

  // Example program using the DSL
  def buildComplexDocument[A](values: List[A]): DocumentProgram[Document[A]] = for {
    leaves     <- values.traverse(createLeaf)
    horizontal <- createHorizontal(leaves.take(2))
    vertical   <- createVertical(leaves.drop(2))
    combined   <- combineDocuments(horizontal, vertical)
    validated  <- validateDocument(combined)
    result <- validated match {
      case Right(doc) => Free.pure(doc)
      case Left(_)    => createLeaf(values.head) // fallback
    }
  } yield result

  // Pure interpreter
  implicit val pureInterpreter: DocumentAlgebra ~> Id = new (DocumentAlgebra ~> Id) {
    def apply[A](fa: DocumentAlgebra[A]): Id[A] = fa match {
      case CreateLeaf(value)      => Leaf(value)
      case CreateHorizontal(docs) => Horizontal(docs)
      case CreateVertical(docs)   => Vertical(docs)
      case CombineDocuments(left, right) =>
        Document.semigroup.combine(left, right)
      case ValidateDocument(doc) =>
        // Simple validation: check if document is not empty
        doc match {
          case Empty() => Left(EmptyDocumentError)
          case _       => Right(doc)
        }
    }
  }

  // Effect interpreter (for demonstration with Option)
  val optionInterpreter: DocumentAlgebra ~> Option = new (DocumentAlgebra ~> Option) {
    def apply[A](fa: DocumentAlgebra[A]): Option[A] = fa match {
      case CreateLeaf(value)      => Some(Leaf(value))
      case CreateHorizontal(docs) => Some(Horizontal(docs))
      case CreateVertical(docs)   => Some(Vertical(docs))
      case CombineDocuments(left, right) =>
        Some(Document.semigroup.combine(left, right))
      case ValidateDocument(doc) =>
        doc match {
          case Empty() => None // Validation fails for empty documents
          case _       => Some(Right(doc))
        }
    }
  }

  // Run programs
  def runPure[A](program: DocumentProgram[A]): A =
    program.foldMap(pureInterpreter)

  def runOption[A](program: DocumentProgram[A]): Option[A] =
    program.foldMap(optionInterpreter)
}

// ====== TAGLESS FINAL (Alternative to Free) ======
// Higher-kinded type class for document operations
trait DocumentF[F[_]] {
  def createLeaf[A](value: A): F[Document[A]]
  def createHorizontal[A](docs: List[Document[A]]): F[Document[A]]
  def createVertical[A](docs: List[Document[A]]): F[Document[A]]
  def combineDocuments[A](left: Document[A], right: Document[A]): F[Document[A]]
  def validateDocument[A](doc: Document[A]): F[Either[DocumentError, Document[A]]]
}

object DocumentF {
  // Pure implementation
  implicit val pureDocumentF: DocumentF[Id] = new DocumentF[Id] {
    def createLeaf[A](value: A): Id[Document[A]]                      = Leaf(value)
    def createHorizontal[A](docs: List[Document[A]]): Id[Document[A]] = Horizontal(docs)
    def createVertical[A](docs: List[Document[A]]): Id[Document[A]]   = Vertical(docs)
    def combineDocuments[A](left: Document[A], right: Document[A]): Id[Document[A]] =
      Document.semigroup.combine(left, right)
    def validateDocument[A](doc: Document[A]): Id[Either[DocumentError, Document[A]]] =
      doc match {
        case Empty() => Left(EmptyDocumentError)
        case _       => Right(doc)
      }
  }

  // Option implementation
  implicit val optionDocumentF: DocumentF[Option] = new DocumentF[Option] {
    def createLeaf[A](value: A): Option[Document[A]]                      = Some(Leaf(value))
    def createHorizontal[A](docs: List[Document[A]]): Option[Document[A]] = Some(Horizontal(docs))
    def createVertical[A](docs: List[Document[A]]): Option[Document[A]]   = Some(Vertical(docs))
    def combineDocuments[A](left: Document[A], right: Document[A]): Option[Document[A]] =
      Some(Document.semigroup.combine(left, right))
    def validateDocument[A](doc: Document[A]): Option[Either[DocumentError, Document[A]]] =
      doc match {
        case Empty() => None // Validation fails
        case _       => Some(Right(doc))
      }
  }

  // Tagless final program
  def buildDocument[F[_]: Monad: DocumentF, A](values: List[A]): F[Document[A]] = {
    val F = implicitly[DocumentF[F]]
    for {
      leaves     <- values.traverse(F.createLeaf)
      horizontal <- F.createHorizontal(leaves.take(2))
      vertical   <- F.createVertical(leaves.drop(2))
      combined   <- F.combineDocuments(horizontal, vertical)
    } yield combined
  }
}
