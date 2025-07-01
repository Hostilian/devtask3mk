package com.example

import cats.{Id, Monad}
import cats.syntax.all.*
import cats.syntax.semigroup.*
import DocumentAlgebras.*
import DocumentDSL.*
import DocumentComposition.*

/**
 * This shows off basically every functional programming concept 
 * from the assignment working together. It's like a demo reel 
 * but for type theory nerds.
 */
object ComprehensiveExample extends App {

  println("🚀 Comprehensive FP Showcase")
  println("Buckle up, we're about to see every concept in action...\n")

  // ====== ALGEBRAIC DATA TYPES IN ACTION ======
  println("🧮 Algebraic Data Types")

  // Sum types (sealed trait + case classes)
  val leafDoc: Document[String] = Leaf("Hello")
  val horizontalDoc: Document[String] = Horizontal(List(Leaf("A"), Leaf("B")))
  val verticalDoc: Document[String] = Vertical(List(Leaf("C"), Leaf("D")))
  val emptyDoc: Document[String] = Empty()

  // Product types in action
  val position = Position(1, 2)
  val operation: DocumentOp[String] = Insert("New Value", position)

  println(s"Leaf: $leafDoc")
  println(s"Horizontal: $horizontalDoc")
  println(s"Vertical: $verticalDoc")
  println(s"Empty: $emptyDoc")
  println(s"Operation: $operation")

  // ====== HIGHER-KINDED TYPES & POLYMORPHISM ======
  println("\n🎭 Higher-Kinded Types & Polymorphism")

  // Parametric polymorphism - works for any type A
  def documentLength[A](doc: Document[A]): Int =
    Document.foldLeft(doc, 0)((acc, _) => acc + 1)

  println(s"Length of string doc: ${documentLength(horizontalDoc)}")
  println(s"Length of int doc: ${documentLength(Document.map(horizontalDoc)(_.length))}")

  // Ad-hoc polymorphism via type classes
  val metrics = calculateMetrics(horizontalDoc)
  println(s"Document metrics: $metrics")

  // ====== THE ASSIGNMENT'S MAIN REQUIREMENT ======
  println("\n🎯 Assignment Function f[M[_]: Monad, A, B]")
  println("This is the big one from the assignment...")

  // f[M[_]: Monad, A, B]: (A => M[B]) => D[A] => M[D[B]]
  val testDoc = Horizontal(List(Leaf(1), Leaf(2), Leaf(3)))

  // Test f[Id](identity) = identity
  val identityResult = Document.f[Id, Int, Int](identity)(testDoc)
  println(s"✓ f[Id](identity) law holds: ${identityResult == testDoc}")

  // Test f[Option](Some(_)) = Some(_)
  val optionResult = Document.f[Option, Int, Int](Some(_))(testDoc)
  println(s"✓ f[Option](Some(_)) law holds: ${optionResult == Some(testDoc)}")

  // More interesting transformations
  val stringTransform = Document.f[Option, Int, String](i => Some(s"num_$i"))(testDoc)
  println(s"Cool transformation: $stringTransform")

  // ====== FUNCTORS, APPLICATIVES, MONADS ======
  println("\n🔧 Functors, Applicatives, Monads")
  println("The classic FP trio in action...")

  // Functor map
  val mapped = Document.map(testDoc)(_ * 2)
  println(s"Functor map (*2): $mapped")

  // Applicative operations
  val doc1 = Leaf(5)
  val doc2 = Leaf(3)
  val applicativeResult = Document.map2(doc1, doc2)(_ + _)
  println(s"Applicative combine: $applicativeResult")

  // Monadic operations
  val monadicResult = Document.flatMap(doc1)(x => Horizontal(List(Leaf(x), Leaf(x * 2))))
  println(s"Monadic flatMap: $monadicResult")

  // ====== RECURSION SCHEMES ======
  println("\n♻️ Recursion Schemes")
  println("Math-y ways to traverse structures...")

  // Catamorphism - tear down structure
  val sum = Document.cata(testDoc)(
    identity,          // what to do with leaves
    _.sum,            // what to do with horizontal 
    _.sum,            // what to do with vertical
    () => 0           // what to do with empty
  )
  println(s"Catamorphism (tearing down to sum): $sum")

  // Anamorphism - build up structure
  val unfolded = Document.ana[String, Int](3) { n =>
    if (n <= 0) Left("base")
    else Right((List(n-1, n-2), true))
  }
  println(s"Anamorphism result: $unfolded")

  // ====== FREE MONADS ======
  println("\n=== Free Monads ===")

  val freeProgram = for {
    leaf1 <- createLeaf("Free")
    leaf2 <- createLeaf("Monad")
    combined <- createHorizontal(List(leaf1, leaf2))
    validated <- validateDocument(combined)
    result <- validated match {
      case Right(doc) => buildComplexDocument(List("Complex", "Document"))
      case Left(_) => createLeaf("Fallback")
    }
  } yield result

  val freePureResult = runPure(freeProgram)
  val freeOptionResult = runOption(freeProgram)
  println(s"Free monad pure: $freePureResult")
  println(s"Free monad option: $freeOptionResult")

  // ====== TAGLESS FINAL ======
  println("\n=== Tagless Final ===")

  import DocumentF.*
  val taglessResult1 = buildDocument[Id, String](List("Tagless", "Final"))
  val taglessResult2 = buildDocument[Option, String](List("With", "Effects"))

  println(s"Tagless final Id: $taglessResult1")
  println(s"Tagless final Option: $taglessResult2")

  // ====== EFFECTS & VALIDATION ======
  println("\n=== Effects & Validation ===")

  val validDoc = Horizontal(List(Leaf("valid"), Leaf("data")))
  val invalidDoc = Empty[String]()

  val validationResult1 = Document.validateDocument[Id](validDoc)
  val validationResult2 = Document.validateDocument[Id](invalidDoc)

  println(s"Valid document validation: $validationResult1")
  println(s"Invalid document validation: $validationResult2")

  // ====== ALGEBRAS ======
  println("\n=== Algebras ===")

  val complexDoc = Vertical(List(
    Horizontal(List(Leaf("A"), Leaf("B"))),
    Horizontal(List(Leaf("C"), Leaf("D")))
  ))

  // Different rendering algebras
  val asciiRendered = render(complexDoc)(asciiRenderer)
  val htmlRendered = render(complexDoc)(htmlRenderer)

  println("ASCII rendering:")
  println(asciiRendered)
  println("\nHTML rendering:")
  println(htmlRendered)

  // ====== SEMIGROUP & MONOID ======
  println("\n=== Semigroup & Monoid ===")

  val doc_a = Horizontal(List(Leaf("A")))
  val doc_b = Horizontal(List(Leaf("B")))
  val doc_c = Horizontal(List(Leaf("C")))
  val empty = Document.monoid[String].empty

  // Semigroup associativity
  val leftAssoc = Document.semigroup[String].combine(Document.semigroup[String].combine(doc_a, doc_b), doc_c)
  val rightAssoc = Document.semigroup[String].combine(doc_a, Document.semigroup[String].combine(doc_b, doc_c))
  println(s"Semigroup associative: ${leftAssoc == rightAssoc}")

  // Monoid identity
  val leftIdentity = Document.monoid[String].combine(empty, doc_a)
  val rightIdentity = Document.monoid[String].combine(doc_a, empty)
  println(s"Monoid left identity: ${leftIdentity == doc_a}")
  println(s"Monoid right identity: ${rightIdentity == doc_a}")

  // ====== TYPE SAFETY ======
  println("\n=== Type Safety ===")

  // Phantom types for compile-time guarantees
  val unsafeDoc = Horizontal(List(Leaf("data")))
  val safeDoc = Document.validateAtCompileTime(unsafeDoc)
  val processed = Document.processValidDocument(safeDoc)

  println(s"Type-safe processing: ${processed == unsafeDoc}")

  // ====== COMPOSITION ======
  println("\n=== Composition ===")

  val nums1 = Horizontal(List(Leaf(1), Leaf(2)))
  val nums2 = Horizontal(List(Leaf(10), Leaf(20)))

  val zipped = zipWith(nums1, nums2)(_ + _)
  val sequenced = sequence(List(Leaf(1), Leaf(2), Leaf(3)))

  println(s"ZipWith result: $zipped")
  println(s"Sequence result: $sequenced")

  // ====== ADVANCED COMBINATIONS ======
  println("\n=== Advanced Combinations ===")

  // Combining multiple concepts
  val advancedExample = for {
    // Create with Free monad
    initial <- createLeaf("Start")

    // Transform with regular functor
    transformed = Document.map(initial)(_.toUpperCase)

    // Validate with effects
    validated <- validateDocument(transformed)

    // Process result
    final_result <- validated match {
      case Right(doc) =>
        // Use catamorphism to extract and rebuild
        val content = Document.cata(doc)(
          identity,
          _.mkString(" "),
          _.mkString(" "),
          () => "empty"
        )
        createLeaf(s"Processed: $content")
      case Left(_) =>
        createLeaf("Error")
    }
  } yield final_result

  val advancedResult = runPure(advancedExample)
  println(s"Advanced combination: $advancedResult")

  println("\n=== Summary ===")
  println("✓ Algebraic data types (sums, products, unit)")
  println("✓ Recursion schemes (catamorphism, anamorphism)")
  println("✓ Higher-kinded types")
  println("✓ Polymorphism (parametric, ad-hoc, typeclasses)")
  println("✓ Functors, applicatives, monads, composition")
  println("✓ Free monads")
  println("✓ Effects and validation")
  println("✓ Algebras")
  println("✓ Semigroup, monoid")
  println("✓ Type safety")
  println("✓ Validation, parsing")
  println("✓ Type driven development")
  println("✓ Assignment requirement: f[M[_]: Monad, A, B]: (A => M[B]) => D[A] => M[D[B]]")
}
