# Contributing Guide

Thank you for your interest in contributing to the AI Scientist Ecosystem!

## Repositories
Each module lives in its own repository under the org. Mirror structure while bootstrapping inside the meta repo.

## Branching
- main: Stable, release-ready
- feature/*: New functionality
- fix/*: Bug/security fixes
- docs/*: Documentation only

## Commit Convention
Use conventional commits:
- feat: new feature
- fix: bug fix
- docs: documentation
- chore: tooling/build config
- refactor: code change without feature/fix
- test: adding or refactoring tests

## Pull Requests Checklist
- Linked Issue
- Clear description & motivation
- Updated tests (if applicable)
- No broken lint/build
- Docs updated when behavior changes

## Code Style
Define per language (to be finalized). Use automated formatters (e.g. Prettier, Black, gofmt).

## Tests
Aim for meaningful coverage of core logic. Avoid brittle integration tests early.

## Reviews
Require at least one approval from CODEOWNERS of touched module. Address review comments before merging.

## Release Process
Tag versions on main after passing CI. Generate changelog automatically via action (to be added).

## Reporting Issues
Use issue templates. Provide reproduction steps and expected vs actual behavior.

## Security
Do not include secrets in commits. Report vulnerabilities privately (see SECURITY.md).

## Getting Help
Open a discussion thread or tag relevant team in an issue.
