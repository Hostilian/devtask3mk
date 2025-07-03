package com.example

import cats.Id
import cats.Monad
import cats.data.ValidatedNel
import cats.syntax.all.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.example.Document
import com.example.Document.{given, *}

import DocumentAlgebras.*
import DocumentDSL.{createLeaf, createHorizontal, runPure, runOption}
import DocumentComposition.*

class AdvancedDocumentSpec extends AnyFlatSpec with Matchers {

  // ====== TESTING THE ASSIGNMENT REQUIREMENTS ======

  "Document data type" should "represent subdivided documents" in {
    val doc: Document[String] = Vertical(
      List(
        Horizontal(List(Leaf("A"), Leaf("B"))),
        Horizontal(List(Leaf("C"), Leaf("D")))
      )
    )

    doc shouldBe a[Document[?]]
    doc.asInstanceOf[Vertical[String]].cells should have length 2
  }

  "Function f" should "satisfy f[Id](identity) = identity" in {
    val doc: Document[Int] = Horizontal(List(Leaf(1), Vertical(List(Leaf(2), Leaf(3)))))
    val result             = Document.f[Id, Int, Int](identity)(doc)
    result shouldBe doc
  }

  "Function f" should "satisfy f[Option](Some(_)) = Some(_)" in {
    val doc: Document[Int] = Horizontal(List(Leaf(1), Vertical(List(Leaf(2), Leaf(3)))))
    val result             = Document.f[Option, Int, Int](Some(_))(doc)
    result shouldBe Some(doc)
  }

  // ====== ALGEBRAIC DATA TYPES ======

  "Document ADT" should "demonstrate sum types with sealed traits" in {
    val docs: List[Document[String]] = List(
      Leaf("value"),
      Horizontal(List(Leaf("a"), Leaf("b"))),
      Vertical(List(Leaf("c"), Leaf("d"))),
      Empty()
    )

    docs.foreach(_ shouldBe a[Document[?]])
  }

  "DocumentError ADT" should "demonstrate error sum types" in {
    val errors: List[DocumentError] = List(
      EmptyDocumentError,
      DocumentParseError("invalid syntax"),
      ValidationError("field", "reason")
    )

    errors.foreach(_ shouldBe a[DocumentError])
  }

  // ====== HIGHER-KINDED TYPES ======

  "Document" should "work with different type constructors" in {
    def testWithF[F[_]](implicit F: Monad[F]): F[Document[String]] = {
      val doc = Leaf("test")
      Document.f(F.pure[String])(doc)
    }

    testWithF[Id] shouldBe Leaf("test")
    testWithF[Option] shouldBe Some(Leaf("test"))
    testWithF[List] shouldBe List(Leaf("test"))
  }

  // ====== RECURSION SCHEMES ======

  "Catamorphism" should "tear down document structure" in {
    val doc: Document[Int] = Horizontal(List(Leaf(1), Leaf(2), Leaf(3)))
    val sum = Document.cata(doc)(
      identity,
      _.sum,
      _.sum,
      () => 0
    )
    sum shouldBe 6
  }

  "Anamorphism" should "build up document structure" in {
    val result = Document.ana[String, Int](3) {
      case 0 => Left("zero")
      case 1 => Left("one")
      case n => Right((List(n - 1, n - 2), true))
    }

    result shouldBe a[Document[?]]
  }

  // ====== POLYMORPHISM & TYPE CLASSES ======

  "Document" should "demonstrate parametric polymorphism" in {
    def length[A](doc: Document[A]): Int =
      Document.foldLeft(doc, 0)((acc, _) => acc + 1)

    length(Leaf("string")) shouldBe 1
    length(Leaf(42)) shouldBe 1
    length(Horizontal(List(Leaf(1), Leaf(2)))) shouldBe 2
  }

  "Document" should "demonstrate ad-hoc polymorphism with type classes" in {
    import DocumentAlgebras.stringMetrics

    val doc     = Horizontal(List(Leaf("hello"), Leaf("world")))
    val metrics = calculateMetrics(doc)

    metrics.width shouldBe 10 // "hello" + "world"
    metrics.leafCount shouldBe 2
  }

  // ====== FUNCTORS, APPLICATIVES, MONADS ======

  "Document Functor" should "satisfy functor laws" in {
    val doc = Horizontal(List(Leaf(1), Leaf(2)))

    // Identity law
    Document.map(doc)(identity) shouldBe doc

    // Composition law
    val f = (x: Int) => x + 1
    val g = (x: Int) => x * 2
    Document.map(Document.map(doc)(f))(g) shouldBe Document.map(doc)(g compose f)
  }

  "Document Applicative" should "support applicative operations" in {
    val doc1 = Leaf(5)
    val doc2 = Leaf(3)

    val result = Document.map2(doc1, doc2)(_ + _)
    result shouldBe Leaf(8)
  }

  "Document Monad" should "satisfy monad laws" in {
    val value = 42
    val f     = (x: Int) => Leaf(x + 1)
    val doc   = Leaf(value)

    // Left identity: pure(a).flatMap(f) == f(a)
    Document.flatMap(Document.pure(value))(f) shouldBe f(value)

    // Right identity: m.flatMap(pure) == m
    Document.flatMap(doc)(Document.pure) shouldBe doc
  }

  // ====== FREE MONADS ======

  "Free monad DSL" should "compose operations" in {
    val program = for {
      leaf1      <- createLeaf("A")
      leaf2      <- createLeaf("B")
      horizontal <- createHorizontal(List(leaf1, leaf2))
    } yield horizontal

    val result = runPure(program)
    result shouldBe Horizontal(List(Leaf("A"), Leaf("B")))
  }

  "Free monad interpreter" should "work with different effects" in {
    import DocumentDSL.validateDocument
    val program = validateDocument(Empty[String]())

    runPure(program).shouldBe(Left(EmptyDocumentError))
    runOption(program).shouldBe(None)
  }

  // ====== TAGLESS FINAL ======

  "Tagless final" should "work with different interpreters" in {
    import DocumentF.*

    val result1 = buildDocument[Id, String](List("A", "B", "C"))
    val result2 = buildDocument[Option, String](List("A", "B", "C"))

    result1 shouldBe a[Document[?]]
    result2 shouldBe a[Some[?]]
  }

  // ====== EFFECTS & VALIDATION ======

  "Document validation" should "work with applicative validation" in {
    val doc    = Horizontal(List(Leaf("valid"), Leaf("")))
    val result = Document.validateDocument[Id](doc)

    result shouldBe a[Document[?]]
  }

  // ====== ALGEBRAS ======

  "Render algebra" should "support different renderers" in {
    val doc = Horizontal(List(Leaf("A"), Leaf("B")))

    val asciiResult = render(doc)(asciiRenderer)
    val htmlResult  = render(doc)(htmlRenderer)

    asciiResult should include("[A]")
    asciiResult should include("[B]")
    asciiResult should include("|")

    htmlResult should include("<span>A</span>")
    htmlResult should include("<span>B</span>")
    htmlResult should include("horizontal")
  }

  // ====== SEMIGROUP & MONOID ======

  "Document Semigroup" should "combine documents" in {
    val doc1 = Horizontal(List(Leaf("A")))
    val doc2 = Horizontal(List(Leaf("B")))

    val combined = Document.semigroup[String].combine(doc1, doc2)
    combined.shouldBe(Horizontal(List(Leaf("A"), Leaf("B"))))
  }

  "Document Monoid" should "have proper identity" in {
    val doc   = Leaf("test")
    val empty = Document.monoid[String].empty

    Document.monoid[String].combine(doc, empty).shouldBe(doc)
    Document.monoid[String].combine(empty, doc).shouldBe(doc)
  }

  // ====== TYPE SAFETY ======

  "Phantom types" should "enforce compile-time constraints" in {
    val doc       = Horizontal(List(Leaf("A"), Leaf("B")))
    val validDoc  = Document.validateAtCompileTime(doc)
    val processed = Document.processValidDocument(validDoc)

    processed shouldBe doc
  }

  // ====== COMPOSITION ======

  "Document composition" should "support zipWith" in {
    val doc1 = Horizontal(List(Leaf(1), Leaf(2)))
    val doc2 = Horizontal(List(Leaf(10), Leaf(20)))

    val result = zipWith(doc1, doc2)(_ + _)
    result shouldBe Horizontal(List(Leaf(11), Leaf(22)))
  }
}
