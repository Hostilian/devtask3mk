package com.example

import cats.{Semigroup, Monoid}
import cats.syntax.all.*

// ====== ALGEBRAS & ALGEBRAIC STRUCTURES ======

// Document-specific algebras
object DocumentAlgebras {

  // ====== SEMIGROUP INSTANCES ======
  // Content semigroup for string concatenation
  implicit val stringContentSemigroup: Semigroup[String] = new Semigroup[String] {
    def combine(x: String, y: String): String = s"$x $y"
  }

  // Size semigroup for document metrics
  case class DocumentSize(width: Int, height: Int, leafCount: Int)

  implicit val documentSizeSemigroup: Semigroup[DocumentSize] = new Semigroup[DocumentSize] {
    def combine(x: DocumentSize, y: DocumentSize): DocumentSize =
      DocumentSize(
        x.width max y.width,
        x.height + y.height,
        x.leafCount + y.leafCount
      )
  }

  // ====== MONOID INSTANCES ======
  implicit val documentSizeMonoid: Monoid[DocumentSize] = new Monoid[DocumentSize] {
    def combine(x: DocumentSize, y: DocumentSize): DocumentSize =
      documentSizeSemigroup.combine(x, y)
    def empty: DocumentSize = DocumentSize(0, 0, 0)
  }

  // Content aggregation monoid
  case class ContentAggregate(values: List[String], totalLength: Int)

  implicit val contentAggregateMonoid: Monoid[ContentAggregate] = new Monoid[ContentAggregate] {
    def combine(x: ContentAggregate, y: ContentAggregate): ContentAggregate =
      ContentAggregate(
        x.values ++ y.values,
        x.totalLength + y.totalLength
      )
    def empty: ContentAggregate = ContentAggregate(Nil, 0)
  }

  // ====== DOCUMENT-SPECIFIC ALGEBRAS ======

  // Render algebra - different ways to render documents
  trait RenderAlgebra[A] {
    def renderLeaf(value: A): String
    def renderHorizontal(children: List[String]): String
    def renderVertical(children: List[String]): String
    def renderEmpty(): String
  }

  // ASCII renderer
  implicit val asciiRenderer: RenderAlgebra[String] = new RenderAlgebra[String] {
    def renderLeaf(value: String): String = s"[$value]"
    def renderHorizontal(children: List[String]): String = children.mkString(" | ")
    def renderVertical(children: List[String]): String = children.mkString("\n")
    def renderEmpty(): String = "∅"
  }

  // HTML renderer
  implicit val htmlRenderer: RenderAlgebra[String] = new RenderAlgebra[String] {
    def renderLeaf(value: String): String = s"<span>$value</span>"
    def renderHorizontal(children: List[String]): String =
      s"<div class='horizontal'>${children.mkString}</div>"
    def renderVertical(children: List[String]): String =
      s"<div class='vertical'>${children.mkString}</div>"
    def renderEmpty(): String = "<div class='empty'></div>"
  }

  // Metrics algebra - for calculating document properties
  trait MetricsAlgebra[A] {
    def leafMetrics(value: A): DocumentSize
    def combineHorizontal(sizes: List[DocumentSize]): DocumentSize
    def combineVertical(sizes: List[DocumentSize]): DocumentSize
    def emptyMetrics(): DocumentSize
  }

  implicit val stringMetrics: MetricsAlgebra[String] = new MetricsAlgebra[String] {
    def leafMetrics(value: String): DocumentSize =
      DocumentSize(value.length, 1, 1)

    def combineHorizontal(sizes: List[DocumentSize]): DocumentSize =
      DocumentSize(
        sizes.map(_.width).sum,
        sizes.map(_.height).maxOption.getOrElse(0),
        sizes.map(_.leafCount).sum
      )

    def combineVertical(sizes: List[DocumentSize]): DocumentSize =
      DocumentSize(
        sizes.map(_.width).maxOption.getOrElse(0),
        sizes.map(_.height).sum,
        sizes.map(_.leafCount).sum
      )

    def emptyMetrics(): DocumentSize = DocumentSize(0, 0, 0)
  }

  // ====== ALGEBRA OPERATIONS ======

  // Generic renderer using algebra
  def render[A](doc: Document[A])(implicit algebra: RenderAlgebra[A]): String = {
    Document.cata(doc)(
      algebra.renderLeaf,
      algebra.renderHorizontal,
      algebra.renderVertical,
      algebra.renderEmpty
    )
  }

  // Generic metrics calculator
  def calculateMetrics[A](doc: Document[A])(implicit algebra: MetricsAlgebra[A]): DocumentSize = {
    Document.cata(doc)(
      algebra.leafMetrics,
      algebra.combineHorizontal,
      algebra.combineVertical,
      algebra.emptyMetrics
    )
  }

  // Content aggregation using monoid
  def aggregateContent(doc: Document[String]): ContentAggregate = {
    Document.foldLeft(doc, contentAggregateMonoid.empty) { (acc, value) =>
      contentAggregateMonoid.combine(acc, ContentAggregate(List(value), value.length))
    }
  }

  // ====== ADVANCED ALGEBRAIC OPERATIONS ======

  // Distributive operations over documents
  def distribute[A, B](doc: Document[A], values: List[B]): Document[(A, B)] = {
    Document.map(doc)(a => values.map(b => (a, b))).flatMap { pairs =>
      Document.traverse[List, (A, B), (A, B)](Leaf(pairs.head))(List(_)) match {
        case head :: _ => head
        case Nil => Empty()
      }
    }
  }

  // Fold with different algebras for different types
  def foldWithTypeClasses[A: Semigroup](doc: Document[A]): A = {
    Document.foldLeft(doc, None: Option[A]) { (acc, value) =>
      acc match {
        case None => Some(value)
        case Some(existing) => Some(existing |+| value)
      }
    }.getOrElse(
      throw new RuntimeException("Cannot fold empty document without any values")
    )
  }
}

// ====== COMPOSITION & HIGHER-ORDER OPERATIONS ======
object DocumentComposition {

  // Compose two documents with a binary operation
  def zipWith[A, B, C](doc1: Document[A], doc2: Document[B])(f: (A, B) => C): Document[C] = {
    (doc1, doc2) match {
      case (Leaf(a), Leaf(b)) => Leaf(f(a, b))
      case (Horizontal(cells1), Horizontal(cells2)) =>
        Horizontal(cells1.zip(cells2).map { case (d1, d2) => zipWith(d1, d2)(f) })
      case (Vertical(cells1), Vertical(cells2)) =>
        Vertical(cells1.zip(cells2).map { case (d1, d2) => zipWith(d1, d2)(f) })
      case (Empty(), _) | (_, Empty()) => Empty()
      case _ => Empty() // Incompatible structures
    }
  }

  // Apply a document of functions to a document of values
  def ap[A, B](docF: Document[A => B], docA: Document[A]): Document[B] = {
    Document.flatMap(docF)(f => Document.map(docA)(f))
  }

  // Sequence a list of documents
  def sequence[A](docs: List[Document[A]]): Document[List[A]] = {
    docs.foldRight(Document.pure(List.empty[A])) { (doc, acc) =>
      Document.map2(doc, acc)(_ :: _)
    }
  }

  // Kleisli composition for document transformations
  def kleisliCompose[A, B, C](f: A => Document[B], g: B => Document[C]): A => Document[C] =
    a => Document.flatMap(f(a))(g)
}
