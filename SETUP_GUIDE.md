# Complete Setup Guide 🛠️

*From zero to hero: everything you need to run this functional programming masterpiece*

## What You're Getting Into

This is a full-stack Scala project showcasing advanced functional programming concepts. You'll have:
- A CLI tool for document manipulation
- A web server with REST API  
- Comprehensive test suite
- Docker containerization
- CI/CD pipeline

No worries if you're new to this - I'll walk you through everything step by step.

## Prerequisites 📋

### 1. Java (Required)
You need Java 11 or higher. Check what you have:

```bash
java -version
```

If you don't have it or it's too old:
- **Windows:** Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or use [OpenJDK](https://adoptium.net/)
- **Mac:** `brew install openjdk@21` 
- **Linux:** `sudo apt install openjdk-21-jdk` (Ubuntu/Debian) or equivalent

### 2. SBT (Scala Build Tool)
This is how we compile and run Scala code.

**Install SBT:**
- **Windows:** Download from [scala-sbt.org](https://www.scala-sbt.org/download.html)
- **Mac:** `brew install sbt`
- **Linux:** Follow [official instructions](https://www.scala-sbt.org/download.html)

Check it works:
```bash
sbt --version
```

### 3. Git (Probably already have it)
```bash
git --version
```

### 4. IDE Setup (Recommended but Optional)

**VS Code (Easiest):**
1. Install [VS Code](https://code.visualstudio.com/)
2. Install the "Scala (Metals)" extension
3. Install "Scala Syntax (official)" extension

**IntelliJ IDEA:**
1. Install [IntelliJ IDEA](https://www.jetbrains.com/idea/)
2. Install the Scala plugin
3. Import as SBT project

## Getting Started 🚀

### 1. Clone the Project
```bash
git clone https://github.com/your-username/devtask3mk.git
cd devtask3mk
```

### 2. First Build
This will download all dependencies (might take a few minutes first time):
```bash
sbt compile
```

If you see errors, check:
- Java version is 11+
- SBT is properly installed  
- Internet connection is working

### 3. Run Tests
Make sure everything works:
```bash
sbt test
```

You should see all tests pass. If not, something's wrong with the setup.

### 4. Try the Examples
```bash
# See the assignment verification
sbt "runMain com.example.AssignmentVerification"

# See all FP concepts in action
sbt "runMain com.example.ComprehensiveExample"

# Try the simple example
sbt "runMain com.example.SimpleExample"
```

## Available Commands 🎮

### Building and Testing
```bash
sbt compile          # Compile the code
sbt test              # Run all tests
sbt clean             # Clean build artifacts
sbt scalafmtAll       # Format all code nicely
```

### Running Applications
```bash
# CLI application
sbt "runMain com.example.Cli"

# Web server (runs on http://localhost:8080)
sbt "runMain com.example.Server"

# Examples
sbt "runMain com.example.SimpleExample"
sbt "runMain com.example.ComprehensiveExample"
sbt "runMain com.example.AssignmentVerification"
```

### VS Code Tasks
If you're using VS Code, these are available in the Command Palette (Ctrl+Shift+P):
- **SBT Compile:** Build the project
- **SBT Test:** Run tests
- **Run CLI:** Start the command-line app
- **Run Server:** Start the web server
- **Format Code:** Auto-format everything

## Docker Setup 🐳

Want to run it in Docker? here's how:

### Build Image
```bash
docker build -t document-matrix .
```

### Run CLI
```bash
docker run -it document-matrix
```

### Run Server
```bash
docker run -p 8080:8080 document-matrix java -cp document-matrix_3.4.3-1.0.0.jar com.example.Server
```

### Docker Compose (if you extend it)
```bash
docker-compose up
```

## Understanding the Code Structure 📚

### Main Source Files
```
src/main/scala/
├── Document.scala              # 🏗️ Core data type and type class instances
├── AssignmentVerification.scala # ✅ Proves assignment requirements
├── ComprehensiveExample.scala   # 🌟 Shows all FP concepts together
├── SimpleExample.scala         # 🎯 Basic demo of main functionality
├── DocumentFree.scala          # 🆓 Free monad DSL implementation
├── DocumentAlgebras.scala      # 🎨 Different processing algebras  
├── DocumentOptics.scala        # 🔍 Lens-based deep updates
├── Cli.scala                   # 💻 Command-line interface
└── Server.scala                # 🌐 HTTP server with REST API
```

### Test Files
```
src/test/scala/
├── DocumentSpec.scala          # Basic functionality tests
├── AdvancedDocumentSpec.scala  # FP concept verification
└── DocumentPropertySpec.scala  # Mathematical law checking
```

### Start Reading Here
1. **Document.scala** - The core data type and type class instances
2. **SimpleExample.scala** - See the assignment function in action
3. **AssignmentVerification.scala** - Verify all requirements are met
4. **ComprehensiveExample.scala** - See everything working together

## Common Issues & Solutions 🔧

### "Package not found" errors
```bash
sbt clean
sbt compile
```

### Out of memory errors
Add to your shell profile (`.bashrc`, `.zshrc`, etc.):
```bash
export SBT_OPTS="-Xmx2G -XX:+UseConcMarkSweepGC"
```

### VS Code not recognizing Scala
1. Make sure Metals extension is installed
2. Open Command Palette (Ctrl+Shift+P)
3. Run "Metals: Import build"
4. Wait for it to finish

### Tests failing
Make sure you have a clean build:
```bash
sbt clean compile test
```

### Server not starting
Check if port 8080 is free:
```bash
# On Windows
netstat -an | findstr 8080

# On Mac/Linux  
lsof -i :8080
```

## Development Workflow 🔄

### Making Changes
1. Edit code in your IDE
2. `sbt compile` to check for errors
3. `sbt test` to verify tests still pass
4. `sbt scalafmtAll` to format nicely
5. Commit your changes

### Adding Dependencies
Edit `build.sbt` and add to `libraryDependencies`:
```scala
"org.some-org" %% "some-library" % "1.0.0"
```

### Creating New Files
Put them in `src/main/scala/com/example/` and make sure they have:
```scala
package com.example

// your code here
```

## CI/CD Pipeline 🚦

The project has GitHub Actions setup in `.github/workflows/ci.yml`:
- Builds on every push/PR
- Runs all tests
- Checks code formatting
- Builds Docker image

To see it in action, push to GitHub and check the Actions tab.

## Advanced Usage 🎓

### Custom Algebras
You can create your own rendering algebras:
```scala
implicit val myRenderer: RenderAlgebra[String] = new RenderAlgebra[String] {
  def renderLeaf(value: String): String = s"🎯 $value"
  def renderHorizontal(children: List[String]): String = children.mkString(" ➡️ ")
  def renderVertical(children: List[String]): String = children.mkString("\n⬇️\n")
  def renderEmpty(): String = "💀"
}
```

### Free Monad Programs
Build your own document manipulation programs:
```scala
val myProgram = for {
  doc1 <- createLeaf("Hello")
  doc2 <- createLeaf("World")
  combined <- createHorizontal(List(doc1, doc2))
  validated <- validateDocument(combined)
} yield validated
```

### JSON Integration
Documents can be serialized to/from JSON:
```scala
import io.circe.syntax._

val doc = Horizontal(List(Leaf("A"), Leaf("B")))
val json = doc.asJson
val parsed = json.as[Document[String]]
```

## Getting Help 🆘

### Documentation
- **README.md** - Main project overview
- **IMPLEMENTATION_SUMMARY.md** - Technical details of FP concepts
- **docs/CONTRIBUTING.md** - How to contribute
- **This file** - Complete setup guide

### If You're Stuck
1. Check this guide first
2. Look at the example files
3. Run `sbt compile` and fix any errors
4. Check the test files for usage examples
5. Google the error messages (Scala community is helpful)

### Understanding Functional Programming
If the FP concepts are confusing:
1. Start with **SimpleExample.scala**
2. Read about [Cats documentation](https://typelevel.org/cats/)
3. Check out [Scala with Cats](https://www.scalawithcats.com/) (free book)
4. The comments in the code explain a lot

## Next Steps 🎯

Once you have everything running:

1. **Explore:** Try running different examples and see what they do
2. **Modify:** Change some values and see what happens
3. **Extend:** Add your own document operations
4. **Learn:** Read through the code and understand the FP concepts
5. **Experiment:** Try building your own algebraic data types

## Performance Tips 🚀

### For Development
```bash
# Keep SBT running in a terminal to avoid restart overhead
sbt
> ~compile  # Auto-compile on file changes
> ~test      # Auto-test on file changes
```

### For Production
The Docker image is optimized for production use with:
- Multi-stage build (smaller final image)
- JVM tuning for containers
- Minimal runtime dependencies

---

*Happy coding! 🎉 If this guide helped you get up and running, you're ready to dive into some serious functional programming.*
