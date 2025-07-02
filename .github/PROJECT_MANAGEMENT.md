# Project Management Templates

This directory contains templates and configurations for GitHub project management features.

## 📋 Project Boards

### Development Board
A Kanban-style board for tracking development progress:

**Columns:**
- 🆕 **Backlog** - New issues and feature requests
- 🔄 **In Progress** - Currently being worked on
- 👀 **In Review** - Pull requests under review
- ✅ **Done** - Completed items

### Release Planning Board
For planning and tracking releases:

**Columns:**
- 🎯 **Planned** - Features planned for upcoming releases
- 🚀 **In Development** - Features being developed
- 🧪 **Testing** - Features in testing phase
- 📦 **Ready for Release** - Features ready to be released
- ✅ **Released** - Features included in releases

## 🏷️ Label System

### Priority Labels
- `priority: critical` 🔴 - Critical issues requiring immediate attention
- `priority: high` 🟠 - High priority issues
- `priority: medium` 🟡 - Medium priority issues  
- `priority: low` 🔵 - Low priority issues

### Type Labels
- `type: bug` 🐛 - Bug reports
- `type: feature` ✨ - New feature requests
- `type: enhancement` ⚡ - Improvements to existing features
- `type: documentation` 📚 - Documentation improvements
- `type: test` 🧪 - Test-related changes
- `type: refactor` ♻️ - Code refactoring

### Status Labels
- `status: blocked` 🚫 - Blocked by other issues
- `status: help-wanted` 🆘 - Community help needed
- `status: good-first-issue` 🌱 - Good for newcomers
- `status: wip` 🚧 - Work in progress
- `status: needs-review` 👀 - Needs code review

### Component Labels
- `component: core` 🎯 - Core Document ADT
- `component: http` 🌐 - HTTP server/API
- `component: tests` 🧪 - Testing infrastructure
- `component: docs` 📚 - Documentation
- `component: ci` ⚙️ - CI/CD pipeline
- `component: optics` 🔍 - Monocle integration
- `component: free-monad` 🆓 - Free monad DSL

## 🎯 Milestones

### v1.0.0 - Core Release
- Complete Document ADT with all type class instances
- HTTP API with full functionality
- Comprehensive test suite
- Complete documentation

### v1.1.0 - Advanced Features
- Free monad DSL enhancements
- Advanced optics operations
- Performance optimizations
- Extended examples

### v1.2.0 - Ecosystem Integration
- Additional effect system integrations
- More serialization formats
- Plugin system
- Community contributions

## 📊 Project Templates

### Feature Development Template
```markdown
## Feature Overview
Brief description of the feature

## Acceptance Criteria
- [ ] Criteria 1
- [ ] Criteria 2
- [ ] Criteria 3

## Technical Requirements
- [ ] API design completed
- [ ] Implementation plan created
- [ ] Test strategy defined

## Documentation Requirements
- [ ] API documentation
- [ ] Usage examples
- [ ] Wiki updates

## Definition of Done
- [ ] Feature implemented
- [ ] Tests written and passing
- [ ] Documentation updated
- [ ] Code reviewed and approved
```

### Bug Fix Template
```markdown
## Bug Description
Clear description of the bug

## Impact Assessment
- Severity: High/Medium/Low
- Affected users: All/Subset/Edge case
- Workaround available: Yes/No

## Fix Strategy
- [ ] Root cause identified
- [ ] Fix approach validated
- [ ] Test strategy defined

## Verification
- [ ] Bug reproduced
- [ ] Fix implemented
- [ ] Tests added
- [ ] Manual verification completed
```

## 🔄 Workflow Integration

### Issue Lifecycle
1. **Created** - New issue created with appropriate labels
2. **Triaged** - Issue reviewed and prioritized
3. **Assigned** - Developer assigned to work on issue
4. **In Progress** - Work begins, status updated
5. **In Review** - Pull request created and reviewed
6. **Done** - Issue closed when PR merged

### Automation Rules
- Auto-assign labels based on file paths
- Auto-move issues to "In Progress" when PR created
- Auto-close issues when PR merged
- Auto-add to project boards based on labels

## 📈 Metrics and Reporting

### Key Metrics
- **Velocity** - Issues closed per sprint
- **Lead Time** - Time from issue creation to closure
- **Cycle Time** - Time from development start to completion
- **Bug Rate** - Bugs found vs features delivered

### Regular Reviews
- **Weekly** - Sprint planning and progress review
- **Monthly** - Milestone progress and priority adjustment
- **Quarterly** - Project roadmap and goal assessment
