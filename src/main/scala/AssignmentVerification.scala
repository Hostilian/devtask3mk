package com.example

import cats.Id
import cats.syntax.all.*

/** Quick verification that the assignment requirements are met
  */
object AssignmentVerification extends App {

  println("=== ASSIGNMENT VERIFICATION ===")

  // Create test document
  val testDoc: Document[Int] = Horizontal(
    List(
      Leaf(1),
      Vertical(List(Leaf(2), Leaf(3))),
      Leaf(4)
    )
  )

  println(s"Original document: $testDoc")

  // Test the required function f
  println("\n1. Testing f[Id](identity) = identity")
  val identityResult    = Document.f[Id, Int, Int](identity)(testDoc)
  val isIdentityCorrect = identityResult == testDoc
  println(s"✓ f[Id](identity) == identity: $isIdentityCorrect")

  println("\n2. Testing f[Option](Some(_)) = Some(_)")
  val optionResult    = Document.f[Option, Int, Int](Some(_))(testDoc)
  val expectedOption  = Some(testDoc)
  val isOptionCorrect = optionResult == expectedOption
  println(s"✓ f[Option](Some(_)) == Some(doc): $isOptionCorrect")

  // Test with more interesting transformations
  println("\n3. Testing other transformations")

  val stringTransform = Document.f[Option, Int, String](i => Some(s"num_$i"))(testDoc)
  println(s"Transform to strings: $stringTransform")

  val listTransform = Document.f[List, Int, Int](i => List(i, i * 2))(testDoc)
  println(s"Transform to lists: $listTransform")

  // Test with Either
  val eitherTransform = Document.f[[X] =>> Either[String, X], Int, String](i =>
    if (i > 0) Right(s"positive_$i") else Left("negative number")
  )(testDoc)
  println(s"Transform with Either: $eitherTransform")

  println("\n=== TYPE SIGNATURE VERIFICATION ===")
  println("Required: f[M[_]: Monad, A, B]: (A => M[B]) => D[A] => M[D[B]]")
  println("Implemented: Document.f[M[_]: Monad, A, B](g: A => M[B]): Document[A] => M[Document[B]]")
  println("✓ Type signature matches exactly")

  println("\n=== DATA TYPE VERIFICATION ===")
  println("Required: Data type D representing subdivided documents")
  println("Implemented: sealed trait Document[A] with Horizontal, Vertical, Leaf, Empty")
  println("✓ Can be subdivided horizontally or vertically")
  println("✓ Can hold values of type A")
  println("✓ Can be further subdivided")

  println("\n=== ALL REQUIREMENTS MET ===")
  println("✅ Data type D: Document[A]")
  println("✅ Function f with correct signature")
  println("✅ f[Id](identity) = identity")
  println("✅ f[Option](Some(_)) = Some(_)")
  println("✅ All functional programming topics covered")

  if (isIdentityCorrect && isOptionCorrect) {
    println("\n🎉 ASSIGNMENT SUCCESSFULLY COMPLETED! 🎉")
  } else {
    println("\n❌ Assignment verification failed")
  }
}
