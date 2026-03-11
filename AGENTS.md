# AGENTS.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Project Lombok** is a Java library that uses annotation processing and bytecode manipulation to eliminate boilerplate code. It operates as a Java agent and annotation processor, supporting multiple compilers (javac, ECJ) and IDEs (Eclipse, IntelliJ).

**License:** MIT

## Build System

### Apache Ant (Primary)
Lombok uses Ant with Ivy for dependency management. All development tasks use Ant.

**Essential Commands:**
```bash
ant quickstart          # Quick setup and help
ant help               # Detailed help system
ant help.IDE           # IDE setup help
ant help.test          # Testing help
ant help.compile       # Compilation help
ant help.packaging     # Packaging help
ant dist               # Build lombok.jar (main artifact)
ant compile            # Compile source code
ant compile.support    # Compile build support tools
ant test               # Run default test suite
ant test.broad         # Comprehensive testing across platforms
ant maven              # Package for Maven Central
```

**IDE Setup:**
```bash
ant eclipse            # Generate Eclipse project files (recommended)
ant intellij           # Generate IntelliJ project files (experimental)
```

**Note:** Eclipse is strongly recommended for development. IntelliJ support is experimental.

**Platform-Specific Testing:**
```bash
ant test.javacCurrent  # Test on current JVM
ant test.javac11       # Test against JDK 11
ant test.javac17       # Test against JDK 17
ant test.ecj11         # Test Eclipse compiler
ant test.eclipse-oxygen # Test Eclipse Oxygen integration
```

**Docker Integration Tests:**
All Docker tests run without local Maven/Gradle/Bazel installation. Tests are in `/docker/` directory organized by build tool and JDK version.

### Build System Details

**Stubs Compilation:**
Lombok uses a sophisticated compilation process with "stubs" - signature-only versions of library classes. Compilation order:
1. Compile stubs (signature-only classes from various library versions)
2. Compile lombok core with stubs on classpath
3. Package lombok without stubs

Different parts of lombok are compiled with different `-release` targets for compatibility.

**Multi-JDK Testing:**
- `javac6` and `javac8`: Don't require installed JDKs - downloads complete runtime classes and uses module limits to exclude the current VM's javac
- `javac11+`: Requires actual JDK installation - build auto-discovers or prompts for path
- **JVM Configuration:** Create `jvm.locations` file to specify JDK paths:
  ```
  j11 = /path/to/jdk-11
  j17 = /path/to/jdk-17
  ```
  Build will auto-create this file when you provide paths on first prompt.

## Contribution Workflow

### Before Starting Development
**IMPORTANT:** New features must be discussed on the Project Lombok Forum first. Pull requests for new features without prior discussion are unlikely to be accepted.

When proposing features:
- Provide code examples showing both annotation usage ("with lombok") and generated output ("vanilla Java")
- Understand existing design considerations to avoid duplication
- Include proof-of-concept implementations for better consideration

### Development Process
1. Discuss feature/change on Project Lombok Forum
2. Fork repository and create feature branch
3. Implement changes following architecture patterns
4. Add comprehensive test coverage
5. Submit pull request via GitHub

## Architecture

### Multi-Compiler Handler Pattern
Lombok implements platform-specific handlers for each annotation:

- **Annotations:** `src/core/lombok/` (e.g., `@Getter`, `@Setter`, `@Builder`)
- **Javac Handlers:** `src/core/lombok/javac/handlers/` (e.g., `HandleGetter.java`)
- **Eclipse Handlers:** `src/core/lombok/eclipse/handlers/` (e.g., `HandleGetter.java`)

Each annotation has separate implementations for javac and Eclipse Compiler (ECJ).

**Extension Pattern:** When extending Lombok, add handlers directly to existing packages (`lombok.javac.handlers` and `lombok.eclipse.handlers`) rather than creating separate jars.

### Source Organization

**Core Components:**
- `src/core/` - Platform-agnostic annotations and handlers
- `src/core8/` - Java 8+ features
- `src/core9/` - Java 9+ module system support
- `src/utils/` - AST manipulation utilities (javac and ECJ)

**Specialized Components:**
- `src/launch/` - Java agent bootstrap and launcher
- `src/delombok/` - Delombok tool (removes annotations, generates source)
- `src/eclipseAgent/` - Eclipse IDE integration via bytecode injection
- `src/installer/` - GUI/CLI installer
- `src/stubs/` - Compatibility stubs for various library versions

**Testing:**
- `test/transform/` - Transformation tests with before/after comparisons
  - `resource/before/` - Input Java files
  - `resource/after-delombok/` - Expected delombok output
  - `resource/after-ecj/` - Expected ECJ output
- `test/core/` - Unit tests
- `test/eclipse/` - Eclipse integration tests (require X11/xvfb)
- `test/manual/` - Manual compilation tests

### "Everything Jar" Architecture
Lombok ships as a single jar that serves multiple roles:
- Stand-alone Java application (GUI and CLI installer)
- Java agent (runtime bytecode modification)
- Annotation processor (compile-time code generation)
- Java module (JDK 9+ module system)
- Compile-time library dependency

### Shadow Class Loader System
To avoid namespace contamination in user projects, lombok uses a custom class loader system. Most classes are packaged with `.SCL.lombok` extension instead of `.class`, making them invisible to IDEs and auto-complete.

Only entry points remain visible as `.class` files:
- `module-info.class`
- `lombok/*.class` (public annotations)
- `lombok/experimental/**`
- `lombok/extern/**`
- `lombok/launch/**`

On JDK 9+, the module system's `export` feature further restricts visibility. On JDK 8 and below, the `.SCL.lombok` renaming provides this isolation.

## Testing Strategy

### Multi-Platform Support
Lombok tests against:
- **JDK versions:** 6, 8, 11-25 (including EA releases)
- **javac versions:** 6, 7, 8, current
- **ECJ versions:** 8, 11, 14, 16, 19
- **Eclipse versions:** Oxygen through 2025-03, plus I-builds
- **Build tools:** Maven, Gradle, Ant, Bazel (via Docker)
- **IDEs:** Eclipse, IntelliJ, VSCode, NetBeans

### Running Tests
- `ant test` - Default test suite (safe for quick validation)
- `ant test.broad` - Full test matrix across all supported versions
- `ant -noinput dist` - Build without interactive prompts (CI mode)
- Docker integration tests are in CI/GitHub Actions

**Note:** Eclipse tests require X11 display (use `xvfb-run ant test.eclipse-oxygen` for headless testing).

### CI Test Matrix (GitHub Actions)
Pull requests automatically run through a comprehensive test matrix:

**javac Tests:**
- JDK 11-25 against current javac
- JDK 11 against javac6 and javac8
- Uses Zulu distribution for stable releases, oracle-actions for EA releases (JDK 25+)

**Eclipse/ECJ Tests:**
- Eclipse: oxygen, 202006 (JDK8 variant), 202403, 202503, I-build
- Eclipse "full" variants: oxygen-full, 202403-full, 202503-full, I-build-full
- ECJ: versions 11, 14, 16, 19
- Runs with `xvfb-run` for headless X11 support
- Uses testenv caching for faster builds

**Docker Integration Tests (JDK 8, 11, 17, 21, 25):**
- Maven: `mvn compile`
- Gradle: `gradle assemble` (Gradle 9.1.0 for JDK 25)
- Ant: `ant dist`
- Bazel: `bazel build //:ProjectRunner`
- Tests both classpath and module configurations

**Manual Compilation Tests:**
- Located in `test/manual/compileTests/`
- Run via `./runTests.sh` across JDK 8, 11, 17, 21, 25

## Development Workflow

### Initial Setup
```bash
ant eclipse    # or ant intellij
ant dist       # Build lombok.jar
```

### Standard Development Cycle
```bash
ant compile    # Compile changes
ant test       # Run tests
ant dist       # Build distributable jar
```

### Debugging in Eclipse
Generate debug launch targets for specific platform versions:
```bash
ant eclipse.testtarget.javac    # Create javac debug target
ant eclipse.testtarget.ecj      # Create ECJ debug target
ant eclipse.testtarget.eclipse  # Create Eclipse debug target
```
These commands create launch configurations in Eclipse's debug menu for testing specific JVM and platform versions.

**Note:** IntelliJ debug targets are not currently generated (contributions welcome!).

### Adding New Features
1. **Discuss on Project Lombok Forum first** (required for new features)
2. Add annotation in `src/core/lombok/` or `src/core/lombok/experimental/`
3. Implement javac handler in `src/core/lombok/javac/handlers/`
4. Implement Eclipse handler in `src/core/lombok/eclipse/handlers/`
5. Add test cases in `test/transform/resource/before/`
6. Add expected outputs in `test/transform/resource/after-delombok/` and `after-ecj/`
7. Provide documentation with "with lombok" and "without lombok" examples
8. Run `ant test` to validate
9. Submit pull request

## Git Workflow

**Branches:**
- `master` - Stable releases (even version numbers like 1.18.42)
- `develop` - Active development branch
- Feature branches - Created from develop, rebased before merge

**Current branch:** `singular_optional`

**Versioning:**
- Even versions (e.g., 1.18.42) - Stable releases
- Odd versions (e.g., 1.18.43 "Edgy Guinea Pig") - Edge/development releases
- Version defined in `src/core/lombok/core/Version.java`

## Configuration

### lombok.config
Project-specific configuration files support:
- Feature flags (enable/disable specific lombok features)
- Code generation options
- Nullity annotation preferences
- Accessor naming conventions
- Feature usage warnings

Hierarchical configuration supported (project root → subdirectories).

## Key Technologies

- **AST Manipulation:** Direct javac and Eclipse AST transformation
- **Bytecode Engineering:** Embedded ASM library
- **Annotation Processing:** JSR-269 API
- **Java Agent:** Premain-Class agent mechanism
- **Eclipse Integration:** Runtime bytecode injection ("transplant" system)

## Important Notes

- **No local Maven/Gradle needed:** Ant handles all builds; Docker containers run integration tests
- **Multi-JDK discovery:** Build scripts auto-detect installed JDKs or prompt for paths
- **Single jar artifact:** Works across all Java platforms, compilers, and IDEs
- **Compile-time dependency:** Lombok should not be on runtime classpath (though it can be)
- **CI/CD:** GitHub Actions in `.github/workflows/ant.yml` - full matrix testing on push/PR
- **Community first:** Feature development requires forum discussion before implementation

## Resources

- Website: https://projectlombok.org
- Contributing Guide: https://projectlombok.org/contributing/
- Execution Path Documentation: https://projectlombok.org/contributing/contributing
- GitHub Wiki: Additional development resources
