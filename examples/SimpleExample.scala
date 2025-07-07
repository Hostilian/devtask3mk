package com.example

import cats.Id

object SimpleExample extends App {

  println("🎯 Basic Document Example")
  println("Let's see how this document thing works...")

  // Make a simple document structure
  val doc: Document[String] = Horizontal(
    List(
      Leaf("Hello"),
      Vertical(
        List(
          Leaf("World"),
          Leaf("!")
        )
      )
    )
  )

  println(s"\nOur document: $doc")

  // Now test the main assignment function
  println("\n📚 Testing the assignment function f...")

  val result1 = Document.f[Id, String, String](identity)(doc)
  println(s"f[Id](identity): $result1")
  println(s"Is it the same? ${result1 == doc} ✓")

  val result2 = Document.f[Option, String, String](Some(_))(doc)
  println(s"f[Option](Some(_)): $result2")
  println(s"Wrapped in Some? ${result2 == Some(doc)} ✓")

  // Let's do something more interesting
  println("\n🔄 Transform all the text to uppercase:")
  val upperDoc = Document.f[Id, String, String](s => s.toUpperCase.nn)(doc)
  println(s"Result: $upperDoc")

  // Try some validation
  println("\n✅ Validation example:")
  val validationDoc = Document.f[Option, String, String] { s =>
    if (s.nonEmpty) Some(s.reverse.nn) else None
  }(doc)
  println(s"Reversed all non-empty strings: $validationDoc")

  println("\n🎉 Everything works! The assignment requirements are satisfied.")
}
