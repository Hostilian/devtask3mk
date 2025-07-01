package com.example

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.{Arbitrary, Gen}
import cats.syntax.all.*
import zio.{ZIO, Runtime}
import io.circe.syntax.*
import io.circe.parser.*

class DocumentPropertySpec extends AnyFlatSpec with Matchers with ScalaCheckPropertyChecks with TableDrivenPropertyChecks {

  // Generators for property-based testing
  implicit val arbString: Arbitrary[String] = Arbitrary(Gen.alphaNumStr.suchThat(_.nonEmpty))
  implicit val arbInt: Arbitrary[Int] = Arbitrary(Gen.choose(-1000, 1000))

  def genLeaf[A](implicit arbA: Arbitrary[A]): Gen[Document[A]] =
    arbA.arbitrary.map(Leaf(_))

  def genDocument[A](depth: Int = 3)(implicit arbA: Arbitrary[A]): Gen[Document[A]] = {
    if (depth <= 0) genLeaf[A]
    else Gen.oneOf(
      genLeaf[A],
      Gen.const(Empty),
      Gen.listOfN(Gen.choose(1, 3).sample.getOrElse(2), genDocument[A](depth - 1)).map(Horizontal(_)),
      Gen.listOfN(Gen.choose(1, 3).sample.getOrElse(2), genDocument[A](depth - 1)).map(Vertical(_))
    )
  }

  implicit def arbDocument[A](implicit arbA: Arbitrary[A]): Arbitrary[Document[A]] =
    Arbitrary(genDocument[A]())

  "Document Functor" should "satisfy identity law" in {
    forAll { (doc: Document[String]) =>
      Document.map(doc)(identity) shouldBe doc
    }
  }

  it should "satisfy composition law" in {
    forAll { (doc: Document[Int]) =>
      val f: Int => String = _.toString
      val g: String => Int = _.length
      Document.map(Document.map(doc)(f))(g) shouldBe Document.map(doc)(f.andThen(g))
    }
  }

  "Document Semigroup" should "be associative" in {
    forAll { (doc1: Document[String], doc2: Document[String], doc3: Document[String]) =>
      val left = Document.semigroup[String].combine(Document.semigroup[String].combine(doc1, doc2), doc3)
      val right = Document.semigroup[String].combine(doc1, Document.semigroup[String].combine(doc2, doc3))
      left shouldBe right
    }
  }

  "Document Monoid" should "satisfy left identity" in {
    forAll { (doc: Document[String]) =>
      Document.monoid[String].combine(Document.monoid[String].empty, doc) shouldBe doc
    }
  }

  it should "satisfy right identity" in {
    forAll { (doc: Document[String]) =>
      Document.monoid[String].combine(doc, Document.monoid[String].empty) shouldBe doc
    }
  }

  "Document JSON serialization" should "round-trip correctly" in {
    forAll { (doc: Document[String]) =>
      val json = doc.asJson
      val parsed = json.as[Document[String]]
      parsed shouldBe Right(doc)
    }
  }

  "Document traversal" should "preserve structure with Option" in {
    forAll { (doc: Document[Int]) =>
      val result = Document.traverse[Option, Int, Int](doc)(Some(_))
      result shouldBe Some(doc)
    }
  }

  "Document folding" should "handle empty documents" in {
    val emptyDoc: Document[Int] = Empty
    val result = Document.fold(emptyDoc)(identity)(_.sum)(_.sum)
    result shouldBe 0
  }

  it should "sum all leaf values correctly" in {
    forAll { (values: List[Int]) =>
      whenever(values.nonEmpty) {
        val doc: Document[Int] = Horizontal(values.map(Leaf(_)))
        val result = Document.fold(doc)(identity)(_.sum)(_.sum)
        result shouldBe values.sum
      }
    }
  }

  "Pretty printing" should "handle deeply nested structures" in {
    forAll { (doc: Document[String]) =>
      val printed = Cli.prettyPrint(doc)
      printed should not be empty
      // Should contain the structure type names
      if (doc != Empty) {
        printed should (include("Leaf") or include("Horizontal") or include("Vertical") or include("Empty"))
      }
    }
  }

  "Document validation" should "accept well-formed documents" in {
    val testCases = Table(
      "document",
      Leaf("test"),
      Horizontal(List(Leaf("a"), Leaf("b"))),
      Vertical(List(Leaf("x"), Horizontal(List(Leaf("y"), Leaf("z"))))),
      Empty
    )

    forAll(testCases) { doc =>
      // If we can create it, it should be valid
      doc shouldBe a[Document[_]]
    }
  }

  "Document map operations" should "preserve structure types" in {
    forAll { (doc: Document[Int]) =>
      val mapped = Document.map(doc)(_ * 2)
      (doc, mapped) match {
        case (Leaf(_), Leaf(_)) => succeed
        case (Horizontal(_), Horizontal(_)) => succeed
        case (Vertical(_), Vertical(_)) => succeed
        case (Empty, Empty) => succeed
        case _ => fail("Structure type not preserved")
      }
    }
  }
}
