package com.example

import com.example.Document
import com.example.Document.*
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class DocumentPropertySpec extends AnyPropSpec with ScalaCheckPropertyChecks with Matchers {

  // Generator for Document[A]
  def genDocument[A: Arbitrary]: Gen[Document[A]] = {
    val genLeaf = Arbitrary.arbitrary[A].map(Leaf(_))
    def genNode: Gen[Document[A]] = Gen.oneOf(
      genHorizontal,
      genVertical
    )
    def genHorizontal = Gen.listOf(genSized).map(Horizontal(_))
    def genVertical   = Gen.listOf(genSized).map(Vertical(_))

    def genSized: Gen[Document[A]] = Gen.sized { size =>
      if (size <= 0) genLeaf
      else Gen.resize(size / 2, Gen.oneOf(genLeaf, genNode))
    }

    Gen.oneOf(
      genLeaf,
      Gen.const(Empty[A]()),
      genSized
    )
  }

  implicit def arbitraryDocument[A: Arbitrary]: Arbitrary[Document[A]] =
    Arbitrary(genDocument[A])

  property("map identity") {
    forAll { (doc: Document[Int]) =>
      Document.map(doc)(identity) shouldBe doc
    }
  }

  property("map composition") {
    forAll { (doc: Document[Int], f: Int => String, g: String => Double) =>
      val h = g compose f
      Document.map(Document.map(doc)(f))(g) shouldBe Document.map(doc)(h)
    }
  }

  property("traverse identity") {
    forAll { (doc: Document[Int]) =>
      import cats.instances.option.*
      Document.traverse(doc)(Option(_)) shouldBe Some(doc)
    }
  }

  property("semigroup associativity") {
    forAll { (d1: Document[Int], d2: Document[Int], d3: Document[Int]) =>
      import cats.syntax.semigroup.*
      (d1 |+| d2) |+| d3 shouldBe d1 |+| (d2 |+| d3)
    }
  }

  property("monoid identity") {
    forAll { (doc: Document[Int]) =>
      import cats.syntax.monoid.*
      import cats.Monoid
      doc |+| Monoid[Document[Int]].empty shouldBe doc
      Monoid[Document[Int]].empty |+| doc shouldBe doc
    }
  }

  property("decoder(encoder(x)) == x") {
    forAll { (doc: Document[Int]) =>
      import io.circe.syntax.*
      import io.circe.parser.decode
      decode[Document[Int]](doc.asJson.noSpaces) shouldBe Right(doc)
    }
  }

  property("empty document serialization") {
    import io.circe.syntax.*
    import io.circe.parser.decode
    val emptyDoc: Document[Int] = Empty()
    val json                    = emptyDoc.asJson
    decode[Document[Int]](json.noSpaces) shouldBe Right(emptyDoc)
  }

  property("leaf prism roundtrip") {
    forAll { (value: String) =>
      DocumentOptics.leafPrism.reverseGet(value) shouldBe Leaf(value)
      DocumentOptics.leafPrism.getOption(Leaf(value)) shouldBe Some(value)
    }
  }

  property("horizontal prism roundtrip") {
    forAll { (list: List[Document[String]]) =>
      DocumentOptics.horizontalPrism.reverseGet(list) shouldBe Horizontal(list)
      DocumentOptics.horizontalPrism.getOption(Horizontal(list)) shouldBe Some(list)
    }
  }

  property("vertical prism roundtrip") {
    forAll { (list: List[Document[String]]) =>
      DocumentOptics.verticalPrism.reverseGet(list) shouldBe Vertical(list)
      DocumentOptics.verticalPrism.getOption(Vertical(list)) shouldBe Some(list)
    }
  }

  property("generated documents are valid") {
    forAll { (doc: Document[Int]) =>
      doc shouldBe a[Document[?]]
    }
  }
}
