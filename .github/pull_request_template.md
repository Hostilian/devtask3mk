## 📋 Pull Request Description

### Type of Change
- [ ] 🐛 Bug fix (non-breaking change which fixes an issue)
- [ ] ✨ New feature (non-breaking change which adds functionality)
- [ ] 💥 Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] 📚 Documentation update
- [ ] 🧪 Test improvement
- [ ] ♻️ Code refactoring
- [ ] 🎨 Style/formatting changes
- [ ] ⚙️ CI/CD changes

### Summary
Provide a brief description of what this PR does:

### Related Issues
- Closes #issue_number
- Related to #issue_number
- Depends on #pr_number

## 🧪 Testing

### Test Coverage
- [ ] Unit tests added/updated
- [ ] Property tests added/updated  
- [ ] Integration tests added/updated
- [ ] All existing tests pass
- [ ] New tests pass

### Manual Testing
Describe any manual testing performed:

```bash
# Commands used for testing
sbt test
sbt "testOnly com.example.DocumentSpec"
```

## 📚 Documentation

### Documentation Updates
- [ ] API documentation updated (ScalaDoc)
- [ ] Wiki pages updated
- [ ] README updated
- [ ] Examples added/updated
- [ ] CHANGELOG updated

### Breaking Changes
If this introduces breaking changes, describe:
1. What breaks?
2. How to migrate existing code?
3. Why is this change necessary?

## 🔍 Code Quality

### Checklist
- [ ] Code follows project style guidelines
- [ ] Code is formatted with scalafmt
- [ ] No compiler warnings
- [ ] Self-review completed
- [ ] Complex code is commented
- [ ] Public APIs have ScalaDoc

### Performance Considerations
- [ ] No obvious performance regressions
- [ ] Memory usage considered
- [ ] Algorithmic complexity documented (if applicable)

## 🎯 Implementation Details

### Technical Approach
Explain the technical approach and design decisions:

### Code Examples
Provide examples of the new functionality:

```scala
// Before (if applicable)
val oldWay = Document.oldMethod(param)

// After
val newWay = Document.newMethod(param)
```

### Alternative Approaches
Were there other ways to implement this? Why was this approach chosen?

## 🔧 Development Environment

### Build and Test Results
```bash
# Paste relevant build/test output
[info] All tests passed.
[info] Total time: 5 s
```

### Environment Details
- **OS**: [e.g. macOS 12.0, Ubuntu 20.04]
- **Java Version**: [e.g. 21.0.1]
- **SBT Version**: [e.g. 1.11.0]
- **Scala Version**: [e.g. 3.4.3]

## 📷 Screenshots (if applicable)
Add screenshots to help explain your changes:

## ✅ Final Checklist

### Before Requesting Review
- [ ] Branch is up to date with main
- [ ] All commits have clear messages
- [ ] PR title follows conventional commits format
- [ ] All CI checks pass
- [ ] Ready for review

### Reviewer Guidelines
Please check:
- [ ] Functionality works as described
- [ ] Code quality meets project standards
- [ ] Tests are comprehensive
- [ ] Documentation is clear and complete
- [ ] No obvious security issues

## 🎉 Additional Notes
Any additional information, concerns, or context for reviewers:

---

**Thank you for contributing to Document Matrix!** 🚀
