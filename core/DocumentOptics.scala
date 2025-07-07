package com.example

import monocle.{Lens, Optional, Prism}
import monocle.macros.GenLens

/** Advanced functional patterns using optics for deep document manipulation. This showcases modern functional
  * programming techniques for immutable updates.
  */
object DocumentOptics {

  // Prisms for extracting specific document types
  val leafPrism: Prism[Document[String], String] = Prism[Document[String], String] {
    case Leaf(value) => Some(value)
    case _           => None
  }(Leaf(_))

  val horizontalPrism: Prism[Document[String], List[Document[String]]] =
    Prism[Document[String], List[Document[String]]] {
      case Horizontal(cells) => Some(cells)
      case _                 => None
    }(Horizontal(_))

  val verticalPrism: Prism[Document[String], List[Document[String]]] =
    Prism[Document[String], List[Document[String]]] {
      case Vertical(cells) => Some(cells)
      case _               => None
    }(Vertical(_))

  // Optics for deep access and modification
  def updateAllLeaves(doc: Document[String], f: String => String): Document[String] = {
    Document.map(doc)(f)
  }

  def getFirstLeafValue(doc: Document[String]): Option[String] = {
    def findFirst(d: Document[String]): Option[String] = d match {
      case Leaf(value)       => Some(value)
      case Horizontal(cells) => cells.flatMap(findFirst).headOption
      case Vertical(cells)   => cells.flatMap(findFirst).headOption
      case Empty()           => None
    }
    findFirst(doc)
  }

  def countLeaves(doc: Document[String]): Int = {
    Document.fold(doc)(_ => 1)(_.sum)(_.sum)
  }

  def transformAtPath(doc: Document[String], path: List[Int], f: String => String): Document[String] = {
    def transform(d: Document[String], remaining: List[Int]): Document[String] = remaining match {
      case Nil => Document.map(d)(f)
      case index :: rest =>
        d match {
          case Horizontal(cells) if index < cells.length =>
            val updated = cells.updated(index, transform(cells(index), rest))
            Horizontal(updated)
          case Vertical(cells) if index < cells.length =>
            val updated = cells.updated(index, transform(cells(index), rest))
            Vertical(updated)
          case _ => d // Path not found, return unchanged
        }
    }
    transform(doc, path)
  }

  // Extension methods for more ergonomic usage
  implicit class DocumentOps[A](doc: Document[A]) {
    def mapLeaves(f: A => A): Document[A] = Document.map(doc)(f)

    def filterLeaves(predicate: A => Boolean): List[A] = {
      Document.fold(doc)(a => if (predicate(a)) List(a) else Nil)(ls => ls.flatten)(ls => ls.flatten)
    }

    def depth: Int = {
      Document.fold(doc)(_ => 1)(depths => if (depths.isEmpty) 1 else depths.max + 1)(depths =>
        if (depths.isEmpty) 1 else depths.max + 1
      )
    }

    def leafCount: Int = {
      Document.fold(doc)(_ => 1)(_.sum)(_.sum)
    }
  }
}
