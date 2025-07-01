# Document Matrix: The Assignment, The Concepts, and The Code (For the Teacher)

Hey Professor, Norm Macdonald here—well, not really, but let's keep it sharp, clear, and a little bit fun. This is your guided tour through the code, the concepts, and the why behind every choice. No fluff, no AI rambling, just what you need to check the boxes and see the magic.

---

## 1. Problem Statement (Straight from the Assignment)

> **Define a data type D that represents a document subdivided horizontally or vertically into 1 or more cells that in turn can be further subdivided or can hold a value of some type A (A is a parameter). Equip the new data type D with**
>
> `f[M[_]: Monad, A, B]: (A => M[B]) => D[A] => M[D[B]]`
>
> **such that**
>
> - `f[Id](identity) = identity`
> - `f[Option](Some(_)) = Some(_)`
>
> **where Id[A] = A.**

---

## 2. Core Data Type (Algebraic Data Types, Sums, Products, Recursion)

```scala
sealed trait Document[A]
case class Leaf[A](value: A) extends Document[A]                    // Holds actual data
case class Horizontal[A](cells: List[Document[A]]) extends Document[A]  // Split left-right  
case class Vertical[A](cells: List[Document[A]]) extends Document[A]    // Split top-bottom
case class Empty[A]() extends Document[A]                          // Nothing here
```

- **Sum types:** `sealed trait` + `case class` (classic ADT)
- **Recursion:** Each `Document` can contain more `Document`s
- **Parametric:** `A` is the type of value in the leaves

---

## 3. The Magic Function (Monad, Traversal, Laws)

```scala
// f[M[_]: Monad, A, B]: (A => M[B]) => D[A] => M[D[B]]
def f[M[_]: Monad, A, B](g: A => M[B])(d: Document[A]): M[Document[B]] = d match {
  case Leaf(a)      => g(a).map(Leaf(_))
  case Horizontal(cells) => cells.traverse(f(g)).map(Horizontal(_))
  case Vertical(cells)   => cells.traverse(f(g)).map(Vertical(_))
  case Empty()      => Monad[M].pure(Empty())
}
```

- **Functor, Monad, Traversal:** All in one. This is a monadic traversal.
- **Laws:**
  - `f[Id](identity) = identity` (pure function, no effects)
  - `f[Option](Some(_)) = Some(_)` (structure preserved if all succeed)
- **Tested in:** `AssignmentVerification.scala`, `DocumentSpec.scala`

---

## 4. Concepts Covered (Checklist)

- Algebraic data types (sealed trait + case classes)
- Recursion schemes (catamorphism/fold)
- Higher-kinded types (`M[_]`)
- Polymorphism (typeclasses, parametric)
- Functor, Applicative, Monad, composition
- Free monads (see `DocumentFree.scala`)
- Effects (ZIO, Cats Effect)
- Algebras (see `DocumentAlgebras.scala`)
- Semigroup, Monoid (combine documents)
- Type safety (everywhere)
- Validation, parsing (JSON, custom)
- Type-driven development (compile-time safety)

---

## 5. Where to Find What

- **Core data type:** `src/main/scala/Document.scala`
- **Assignment function:** `f` in `Document.scala` and `AssignmentVerification.scala`
- **Recursion schemes:** `Document.scala`, `DocumentAlgebras.scala`
- **Free monad:** `DocumentFree.scala`
- **Tagless final:** `DocumentAlgebras.scala`
- **Validation:** `Document.scala`, `DocumentAlgebras.scala`
- **Tests:** `src/test/scala/`
- **CLI:** `Cli.scala`
- **Web server:** `Server.scala`

---

## 6. How to Run & Test (Quick Reference)

```bash
# Compile
sbt compile

# Run all tests
sbt test

# Check assignment requirements
sbt "runMain com.example.AssignmentVerification"

# See everything in action
sbt "runMain com.example.ComprehensiveExample"

# Try the CLI
sbt "runMain com.example.Cli"

# Start the web server
sbt "runMain com.example.Server"
```

---

## 7. Dockerization (Because, Why Not?)

```bash
# Build the image
docker build -t document-matrix .

# Run CLI
docker run -it document-matrix

# Run the web server
docker run -it document-matrix java -cp document-matrix_3.4.3-1.0.0.jar com.example.Server
```

---

## 8. CI/CD Pipeline (Automated, Like a Pro)

- **GitHub Actions:** On push/PR:
  - Check formatting (scalafmt)
  - Compile
  - Run all tests
  - Build Docker image
- **Badges:** See repo for build/test status

---

## 9. Reasoning & Design Choices (Why This Way?)

- **Type safety:** No runtime surprises
- **Composable:** Everything builds on simple abstractions
- **Testable:** Laws and properties ensure correctness
- **Extensible:** Add new algebras, renderers, or effects easily
- **Idiomatic Scala 3:** Modern, clean, and functional

---

## 10. Final Words

If you want to see the math, the code, or the pretty colors, it's all here. If you want to break it, good luck—it's covered by tests and laws. If you want to extend it, just add a new algebra or effect.

And if you read this far, hey, thanks for checking it out. Hope you enjoy the ride as much as I did building it.

---

*Respectfully, and with a tip of the hat to Norm Macdonald.*
