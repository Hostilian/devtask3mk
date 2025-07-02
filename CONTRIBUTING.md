# Contributing to Document Matrix

Thank you for your interest in contributing to Document Matrix! This document provides guidelines and information for contributors.

## 🎯 Ways to Contribute

- 🐛 **Bug Reports** - Report issues and bugs
- 💡 **Feature Requests** - Suggest new features
- 📚 **Documentation** - Improve docs and examples  
- 🧪 **Tests** - Add or improve test coverage
- 💻 **Code** - Implement features and fix bugs
- 🎨 **Examples** - Add practical usage examples

## 🚀 Getting Started

### 1. Development Setup

```bash
# Fork and clone the repository
git clone https://github.com/YOUR_USERNAME/devtask3mk.git
cd devtask3mk

# Install dependencies
sbt compile

# Run tests to verify setup
sbt test
```

### 2. Development Environment

**Recommended Setup:**
- **Java 21+** (Eclipse Temurin)
- **SBT 1.11.0+**
- **VS Code** with Metals extension, or **IntelliJ IDEA** with Scala plugin
- **Git** for version control

**Optional Tools:**
- **Docker** for containerized testing
- **scalafmt** for code formatting (configured in project)

### 3. Project Structure

```
devtask3mk/
├── src/main/scala/           # Main source code
│   ├── Document.scala        # Core ADT and type classes
│   ├── DocumentAlgebras.scala # Tagless final patterns
│   ├── DocumentFree.scala    # Free monad DSL
│   ├── DocumentOptics.scala  # Lens/Prism operations
│   ├── Server.scala         # HTTP server
│   └── Cli.scala           # CLI utilities
├── src/test/scala/          # Test suites
│   ├── DocumentSpec.scala   # Unit tests
│   ├── DocumentPropertySpec.scala # Property tests
│   └── AdvancedDocumentSpec.scala # Integration tests
├── .github/workflows/       # CI/CD configuration
├── docs/                   # Documentation
└── wiki/                   # GitHub wiki pages
```

## 📝 Development Workflow

### 1. Creating Issues

Before starting work:
1. **Search existing issues** to avoid duplicates
2. **Create a detailed issue** describing:
   - Problem or feature request
   - Expected behavior
   - Current behavior (for bugs)
   - Reproduction steps (for bugs)
   - Proposed solution (for features)

### 2. Branch Strategy

```bash
# Create feature branch from main
git checkout main
git pull origin main
git checkout -b feature/your-feature-name

# Or for bug fixes
git checkout -b fix/issue-description
```

**Branch Naming:**
- `feature/feature-name` - New features
- `fix/bug-description` - Bug fixes
- `docs/documentation-update` - Documentation changes
- `test/test-improvements` - Test additions/improvements

### 3. Making Changes

#### Code Standards
- **Scala 3 syntax** - Use modern Scala 3 features
- **Functional style** - Pure functions, immutable data
- **Type safety** - Leverage the type system
- **Documentation** - Add ScalaDoc for public APIs

#### Code Formatting
```bash
# Format code before committing
sbt scalafmtAll

# Check formatting
sbt scalafmtCheck
```

#### Testing Requirements
- **Unit tests** for all new functionality
- **Property tests** for mathematical laws
- **Integration tests** for API changes
- **All tests must pass**

```bash
# Run all tests
sbt test

# Run specific test suite
sbt "testOnly com.example.DocumentSpec"

# Run tests with coverage
sbt coverage test coverageReport
```

### 4. Commit Guidelines

**Commit Message Format:**
```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `test`: Test additions/changes
- `refactor`: Code refactoring
- `style`: Code formatting
- `ci`: CI/CD changes

**Examples:**
```bash
feat(document): add semigroup instance with associativity

- Implement lawful semigroup for Document[A]
- Add property tests for associativity law
- Update documentation with examples

Closes #123

fix(server): resolve port binding issue

- Fix server startup when port is already in use
- Add proper error handling and logging
- Add integration test for port conflicts

test(property): add functor law verification

- Add property-based tests for functor laws
- Verify identity and composition properties
- Add arbitrary generators for Document types
```

### 5. Pull Request Process

#### Before Submitting
```bash
# Ensure code is formatted
sbt scalafmtAll

# Run full test suite
sbt clean test

# Check for compilation warnings
sbt compile

# Update documentation if needed
```

#### Pull Request Checklist
- [ ] **Code compiles** without errors or warnings
- [ ] **All tests pass** including new tests
- [ ] **Code is formatted** with scalafmt
- [ ] **Documentation updated** for API changes
- [ ] **Commit messages** follow guidelines
- [ ] **Branch is up to date** with main

#### PR Description Template
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Test improvement
- [ ] Refactoring

## Testing
- [ ] Unit tests added/updated
- [ ] Property tests added/updated
- [ ] Integration tests added/updated
- [ ] All tests pass

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No breaking changes (or documented)

## Related Issues
Closes #issue_number
```

## 🧪 Testing Guidelines

### Test Categories

#### 1. Unit Tests (`DocumentSpec.scala`)
```scala
"Document" should "support basic operations" in {
  val doc = Horizontal(List(Leaf(1), Leaf(2)))
  val result = doc.map(_ * 2)
  result shouldBe Horizontal(List(Leaf(2), Leaf(4)))
}
```

#### 2. Property Tests (`DocumentPropertySpec.scala`)
```scala
property("functor identity law") {
  forAll { (doc: Document[Int]) =>
    doc.map(identity) shouldBe doc
  }
}
```

#### 3. Integration Tests (`AdvancedDocumentSpec.scala`)
```scala
"HTTP API" should "handle complex documents" in {
  // Test full request/response cycle
}
```

### Test Writing Guidelines
- **Test public APIs** - Focus on behavior, not implementation
- **Use descriptive names** - Clear test descriptions
- **Test edge cases** - Empty, single element, large collections
- **Property-based testing** - For mathematical laws and invariants
- **Mock external dependencies** - Keep tests fast and reliable

## 📚 Documentation Standards

### Code Documentation
```scala
/**
 * Represents a hierarchical document structure.
 * 
 * Documents can be subdivided horizontally or vertically,
 * creating complex layouts from simple components.
 * 
 * @tparam A the type of values contained in leaf nodes
 */
sealed trait Document[A]

/**
 * Combines two documents using semigroup operation.
 * 
 * @param other the document to combine with this one
 * @return a new document containing both structures
 * @example {{{
 * val doc1 = Horizontal(List(Leaf("A")))
 * val doc2 = Horizontal(List(Leaf("B")))
 * val combined = doc1 |+| doc2
 * // Result: Horizontal(List(Leaf("A"), Leaf("B")))
 * }}}
 */
def combine(other: Document[A]): Document[A]
```

### Wiki Documentation
- **Clear examples** with expected outputs
- **Step-by-step guides** for complex operations
- **Cross-references** between related concepts
- **Updated with API changes**

## 🔍 Code Review Process

### For Contributors
- **Respond promptly** to review feedback
- **Ask questions** if feedback is unclear
- **Make requested changes** in additional commits
- **Update tests** based on feedback

### Review Criteria
- **Functionality** - Does it work as intended?
- **Testing** - Adequate test coverage?
- **Documentation** - Public APIs documented?
- **Style** - Follows project conventions?
- **Performance** - No obvious performance issues?
- **Breaking Changes** - Properly documented and justified?

## 🚀 Release Process

### Version Numbering
We follow [Semantic Versioning](https://semver.org/):
- **MAJOR** - Breaking changes
- **MINOR** - New features (backward compatible)
- **PATCH** - Bug fixes (backward compatible)

### Release Checklist
1. Update version in `build.sbt`
2. Update `CHANGELOG.md`
3. Create release PR
4. Tag release after merge
5. GitHub Actions handles deployment

## 🆘 Getting Help

### Community Resources
- **[GitHub Discussions](../../discussions)** - General questions and ideas
- **[GitHub Issues](../../issues)** - Bug reports and feature requests
- **[Wiki](wiki)** - Comprehensive documentation

### Development Questions
- **Setup issues** - Check [Getting Started](wiki/Getting-Started)
- **API usage** - See [API Documentation](wiki/API-Documentation)
- **Examples** - Browse [Examples](wiki/Examples)
- **Architecture** - Read [Architecture Overview](wiki/Architecture-Overview)

### Contact
- **Maintainers** - Tag `@maintainer` in issues/PRs
- **Community** - Use GitHub Discussions
- **Security Issues** - Email (if applicable)

## 🏆 Recognition

Contributors are recognized in:
- **GitHub Contributors** page
- **Release notes** for significant contributions
- **README.md** for major features

## 📄 License

By contributing to Document Matrix, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to Document Matrix! 🎉

**Questions?** Feel free to ask in [GitHub Discussions](../../discussions) or create an issue.
