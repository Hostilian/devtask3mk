package com.example

import cats.Id
import cats.syntax.all.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import zio.Runtime
import zio.ZIO
import zio.interop.catz.*
import zio.test.*
import zio.test.Assertion.equalTo

import Document.*

class DocumentSpec extends AnyFlatSpec with Matchers {
  "Document" should "satisfy functor identity law" in {
    val doc: Document[Int] = Horizontal(List(Leaf(1), Vertical(List(Leaf(2), Leaf(3)))))
    Document.map(doc)(identity) shouldBe doc
  }

  "Document" should "satisfy monad identity law for Option" in {
    val doc: Document[Int] = Horizontal(List(Leaf(1), Vertical(List(Leaf(2), Leaf(3)))))
    Document.f[Option, Int, Int](Some(_))(doc) shouldBe Some(doc)
  }

  "Document" should "support semigroup combine" in {
    val doc1: Document[Int] = Horizontal(List(Leaf(1)))
    val doc2: Document[Int] = Horizontal(List(Leaf(2)))
    Document.semigroup[Int].combine(doc1, doc2) shouldBe Horizontal(List(Leaf(1), Leaf(2)))
  }

  "Document" should "support pretty printing" in {
    val doc: Document[String] = Vertical(List(Leaf("Hello"), Horizontal(List(Leaf("World"), Leaf("!")))))
    val printed               = Cli.prettyPrint(doc)
    printed should include("Vertical")
    printed should include("Leaf(Hello)")
    printed should include("Horizontal")
  }

  "Document" should "support folding" in {
    val doc: Document[Int] = Horizontal(List(Leaf(1), Leaf(2), Leaf(3)))
    val sum                = Document.fold(doc)(identity)(_.sum)(_.sum)
    sum shouldBe 6
  }
}

object DocumentZioSpec extends ZIOSpecDefault {
  def spec: Spec[Any, Nothing] = suite("Document ZIO Tests")(
    test("monad transform with ZIO") {
      val doc: Document[Int] = Vertical(List(Leaf(1), Leaf(2)))
      for {
        result <- Document.f[({ type F[A] = ZIO[Any, Nothing, A] })#F, Int, String](i => ZIO.succeed(i.toString))(doc)
      } yield assert(result)(equalTo(Vertical(List(Leaf("1"), Leaf("2")))))
    },
    test("catamorphism with ZIO") {
      val doc: Document[Int] = Horizontal(List(Leaf(1), Leaf(2), Leaf(3)))
      val result             = Document.fold(doc)(identity)(_.sum)(_.sum)
      assert(result)(equalTo(6))
    },
    test("monoid laws") {
      val doc1: Document[String] = Leaf("a")
      val doc2: Document[String] = Leaf("b")
      val doc3: Document[String] = Leaf("c")

      // Associativity
      val left  = Document.monoid[String].combine(Document.monoid[String].combine(doc1, doc2), doc3)
      val right = Document.monoid[String].combine(doc1, Document.monoid[String].combine(doc2, doc3))

      // Identity
      val leftIdentity  = Document.monoid[String].combine(Document.monoid[String].empty, doc1)
      val rightIdentity = Document.monoid[String].combine(doc1, Document.monoid[String].empty)

      assert(left)(equalTo(right)) &&
      assert(leftIdentity)(equalTo(doc1)) &&
      assert(rightIdentity)(equalTo(doc1))
    }
  )
}
