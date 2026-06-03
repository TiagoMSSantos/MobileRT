# Project AGENTS.md Guide
AGENTS.md give guide for AI agents work codebase.

## Project Structure for AI agents Navigation
- `/app/MobileRT`: C++ native code of MobileRT
- `/app/Components`: C++ native code of components for MobileRT
- `/app/Scenes`: C++ native code of some test scenes for MobileRT
- `/app/System_dependent`: C++ native code to integrate MobileRT with Qt and Android JNI
- `/app/third_party`: C++ native code of third party submodules that are used by MobileRT
- `/app/Unit_Testing`: C++ native code of unit tests of MobileRT
- `/app/web-server`: Simple web server code in Rust for MobileRT
- `/app/src`: Source Android code that uses MobileRT
  - `/main`: Production code
  - `/androidTest`: Android instrumentation tests
  - `/test`: Android unit tests
- `/scripts`: Shell scripts used by CI
- `/scripts/test`: Unit tests for shell scripts used by CI
- `/.github`: Github Actions
- `/.github/workflows`: Github Actions workflows
- `/.github/codeql`: CodeQL configurations
- `/deploy`: Take compiled code, package inside Docker image, distributable format (executable with MobileRT native library)
- Do not read or search files under these directories unless I explicitly ask: /build, /node_modules, /.git, /dist, /__pycache__.

## Coding Conventions for AI agents
### General Conventions
- All files end with empty line
- Don't delete comments — update when codebase changes
- Before editing any file, read it first. Before modifying a function, grep for all callers. Research before you edit.

### C++ Conventions
- Avoid `auto` keyword. Set type explicit

### Java Conventions
- Use `final` keyword whenever possible. Except interfaces and try-with-resources

### Kotlin Conventions
- Use builder pattern with private constructors

### Rust Conventions
- Avoid `unsafe` keyword

## Pull Request Guidelines for AI agents
PR must:
1. Clear description of changes per AGENTS.md
2. Reference related issues
3. All tests pass for AI-generated code
4. Screenshots for UI changes
5. Single concern per PR
6. Commit messages follow pattern:
  - Title format: {type}({scope}): {title}
    - Types: ci/build/chore/fix/feat/refactor/docs/style/perf/test
    - Scopes: module updated, or file name if fits
    - Titles start upper case, no terminal punctuation
  - Description text after title with 1 empty line between
  - After description, add test plan section
7. Never approve or merge open PRs, even if tests pass

## Testing Requirements for AI agents
Ensure Github Actions workflows ran and all tests passed:
- Android
  - If C++ native or Android code modified
- Native
  - If C++ native code modified
- Code Analysis
  - If C++ native or Android code modified
- Docker
  - If C++ native code modified
All checks must pass. Never approve or merge open PRs even if tests pass. AGENTS.md enforce these rules.

## Release notes Conventions for AI agents
Focus only on commits that change MobileRT source: C++ ray tracing engine, C++ components using MobileRT, interface layer (Qt, JNI, Android).
CI-only commits aggregate into single sentence/bullet — "CI improved".
Only 1 short sentence/bullet for all CI improvements.
Never write duplicated lines/bullets.
If duplicates exist, merge into single bullet.
If multiple bullets mention same dependency update, merge into one bullet with latest version.
Title: ## Release Notes
Each module (MobileRT, Components, Android, Qt) has changelog section.
E.g.:
### MobileRT
- Improved something
- Updated something
- Added something
- Can only contain C/C++ dependencies
### Components
- Improved something
- Updated something
- Added something
- Components do not contain any dependencies
### Android
- Improved something
- Updated something
- Added something
- Can only contain Java/Android dependencies
### Qt
- Improved something
- Updated something
- Added something
- Can only contain C++ Qt dependency
### Docker
- Improved something
- Updated something
- Added something
### Others
Others section covers non-MobileRT stuff:
- Dependencies of conanfiles go here
#### Web Server
- Improved something
- Updated something
- Added something
#### CI
- Improved/updated/added something (just 1 bullet should be written regarding CI. Don't write more than 1 bullet at most.)
- Shell script updates only in CI section
- Can only contain Github actions dependencies
#### Testing
- Improved/updated/added something
#### Documentation
- Improved something
- Updated something
- Added something
Again: CI max 1 short sentence/bullet.
Modules without changes: skip section entirely.
End with small disclaimer: notes written by AI.