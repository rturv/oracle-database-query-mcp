AGENTS

This file documents build / lint / test commands and an internal code-style guide
for automated agents working in this repository. Place any repository-specific
decisions here so agents make consistent, safe changes.

- Repo: `postgres-database-query-mcp` (renamed from `oracle-database-query-mcp`)
- JVM: Java 21 (see `pom.xml` maven.compiler.release)
- Build system: Maven (use the wrapper: `./mvnw` or `mvnw.cmd` on Windows)

Enviroment: 
 - MVN installed at C:\programas\apache-maven-3.9.9
 - Several JDKs installed under `C:\programas` (examples: `jdk-21.0.10`, `jdk-21.0.2`, `jdk-25.0.2`).
IMPORTANT: Prefer a JDK 21 installation (example: `C:\programas\jdk-21.0.10`) for builds targeting Java 21

FOR DOCKER OPERATIONS, YOU MUST USE wsl, INSTALLED ON THAT MACHINE.

1) Build / Lint / Test commands

- Build (compile + package):
  - Unix: `./mvnw package`
  - Windows: `mvnw.cmd package`

- Run in dev mode (Quarkus live coding):
  - `./mvnw quarkus:dev`

- Create native build (GraalVM or container):
  - `./mvnw package -Dnative`
  - Containerized native build: `./mvnw package -Dnative -Dquarkus.native.container-build=true`

- Run the packaged JAR (stdio MCP mode):
  - `java -jar target/quarkus-app/quarkus-run.jar`

- Docker (JVM) build / run examples (provided in README):
 - `docker build -f src/main/docker/Dockerfile.jvm -t postgres-mcp-jvm .`
  - `docker run -i --rm -e JDBC_URL=... -e JDBC_USER=... -e JDBC_PASSWORD=... postgres-mcp-jvm`

- Tests (unit tests use Surefire / Quarkus JUnit5):
  - Run all tests: `./mvnw test`
  - Run only unit tests (skip integration tests controlled by profiles): `./mvnw -DskipITs=true test`
  - Run integration tests (failsafe): `./mvnw verify` (this will run failsafe goals if configured)

- Run a single test class or method (useful for iterative development):
  - Single class: `./mvnw -Dtest=OracleMcpServerTest test`
  - Single method: `./mvnw -Dtest=OracleMcpServerTest#methodName test`
  - Windows: replace `./mvnw` with `mvnw.cmd`.

- When tests fail and you need more logging, run with `-Dorg.slf4j.simpleLogger.defaultLogLevel=debug`
  or increase Quarkus logging via `-Dquarkus.log.level=DEBUG`.

2) Quick diagnostics

- Show effective pom: `./mvnw help:effective-pom`
- Run a single integration test with failsafe: `./mvnw -Dtest=!**/*IT.java verify`
- Show dependency tree: `./mvnw dependency:tree`

3) Agent code-style guidelines (high level)

- Language: Java 21. Prefer modern, safe idioms (records, text blocks) when they
  improve clarity and are compatible with the existing codebase.
- Keep changes minimal and focused for automated edits. Avoid refactors that
  touch many files unless the user asks for a large-scale change.

4) Formatting

- Preferred formatter: use `google-java-format` or an automatic formatter that
  preserves Java idioms. If adding a formatter, also add its configuration to
  the repo (Maven/Gradle plugin or editorconfig).
- Line length: ~100 characters preferred; break long expressions.
- Indentation: 4 spaces. Do not use tabs.
- Trailing whitespace: remove.

5) Imports

- Do not use wildcard imports (e.g. `import java.util.*`). Keep imports explicit.
- Order imports logically: standard library, third-party, then project imports.
- Use the IDE or formatter to sort and remove unused imports before committing.

6) Naming conventions

- Packages: lowercase dot-separated (existing pattern `es.rturv.mcp`).
- Classes: UpperCamelCase. Examples: `OracleService`, `OracleRepository`.
- Constants: `private static final` and UPPER_SNAKE_CASE.
- Methods and fields: lowerCamelCase.
- Records: appropriate for simple data holders (this project already uses `record RawQueryResult`).

7) Types and nullability

- Prefer using `Optional<T>` in public APIs when absence is a meaningful state
  and where callers should handle it explicitly (this project uses `Optional` correctly
  in service signatures).
- Keep method signatures small and explicit. Prefer returning typed objects
  over raw maps when possible.

8) Error handling and logging

- Log at the appropriate level:
  - `LOG.debug` for detailed developer info
  - `LOG.info` for important lifecycle events
  - `LOG.warn` for recoverable issues
  - `LOG.error` for exceptions and failures
- Do not leak stack traces or internal exception text to external callers. For
  this MCP server return user-facing messages via `ToolResponse.error(...)` and
  keep detailed logs in the server log (as the code currently does).
- Catch specific exceptions where possible (e.g., `SQLException`) and avoid
  overly broad `catch (Exception)` unless rethrowing.

9) Database / SQL safety

- Avoid building SQL by concatenating untrusted input. The codebase currently
  constructs SQL strings in some places (e.g. `describeTable`, `describeSchema`).
  Automated agents should:
  - Prefer `PreparedStatement` for dynamic values (use bind parameters).
  - If string concatenation is unavoidable for DDL-like queries, strictly
    validate and whitelist inputs (allow only expected identifier characters
    and uppercase the input).
- When altering session state (e.g. `ALTER SESSION SET CURRENT_SCHEMA`), ensure
  the schema value is validated and quoted appropriately to avoid injection.

10) Tests and test-writing guidance

- Tests use JUnit 5 (Quarkus JUnit5). Keep tests small and deterministic.
- Use `@QuarkusTest` when integration with Quarkus runtime is required; prefer
  plain unit tests for logic that doesn't need the container.
- When adding tests that need DB access, use testcontainers or mocks. Do not
  require a live Oracle DB for unit tests.

11) Commit / PR guidance for agents

- Make small commits with a single purpose. Commit message style:
  - Short title (imperative): `fix: avoid SQL injection in describeTable`
  - One-line summary is sufficient for small fixes.
- Do not push directly to protected branches. Create a feature branch and open
  a PR with a brief description and tests attached where relevant.

12) Files and config to consult

- `pom.xml` — build, compiler level (Java 21), surefire/failsafe configuration.
- `README.md` — run instructions and Dockerfile references.
- `COPILOT_CONFIG.md` — particular configuration notes for Copilot/Claude usage
  (agents integrating with Copilot should read it): `COPILOT_CONFIG.md`.
- `CLAUDE_CONFIG.md` — configuration for Claude-style MCP clients: `CLAUDE_CONFIG.md`.

13) Cursor / Copilot rules

- Cursor rules: this repository does not contain `.cursor/rules/` or `.cursorrules`.
- Copilot rules: there is `COPILOT_CONFIG.md` in the repository root with server
  configuration and examples. Agents should not overwrite or delete it; instead
  reference it when producing MCP server configuration snippets.

14) When you're blocked (agent rules)

- If a change touches security (credentials, secrets, connection strings,
  production URLs) ask the human. Do not commit secrets.
- If a requested refactor will touch more than ~10 files or change public APIs,
  ask a human and propose a small incremental plan.

15) Safety checklist before committing

- Run `./mvnw -Dtest=OracleMcpServerTest test` (or appropriate test) to ensure
  the local change passes the focused tests.
- Ensure no new TODOs or debug prints remain.
- Ensure new code compiles with `./mvnw -DskipTests package`.

16) Compilation, JDK selection and troubleshooting

- Preferred JDK: Use JDK 21 (project sets `<maven.compiler.release>21` in `pom.xml`).
- Common problem: the Quarkus Maven plugin requires at least Java 17; if `java -version` shows Java 1.8 you will get a plugin error like
  `Required Java version 17 is not met by current version: 1.8.0_xxx`.
- Quick verification commands:
  - `java -version` — check the JRE/JDK on your PATH
  - `mvn -v` or `./mvnw -v` — check Maven and Java used by Maven

- Use the included Maven wrapper (`./mvnw` or `mvnw.cmd`) where possible so builds are reproducible.

- Set JDK for a single session (examples):
  - Windows PowerShell (temporary for the session):
    ```powershell
    $env:JAVA_HOME = 'C:\programas\jdk-21.0.10'
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
    .\mvnw -DskipTests package
    ```
  - Windows CMD (temporary):
    ```cmd
    set "JAVA_HOME=C:\programas\jdk-21.0.10" && set "PATH=%JAVA_HOME%\bin;%PATH%" && mvnw.cmd -DskipTests package
    ```
  - Bash / WSL / Git Bash:
    ```bash
    export JAVA_HOME=/c/programas/jdk-21.0.10
    export PATH="$JAVA_HOME/bin:$PATH"
    ./mvnw -DskipTests package
    ```

- If you prefer a permanent system change on Windows, update `JAVA_HOME` in System Environment Variables or use `setx` (note `setx` persists but requires a new shell to take effect).
- Alternative: configure a Maven Toolchains file to point to a JDK 21 installation so CI and developers don't rely on PATH.
- Example build troubleshooting flow for agents:
  1. Run `java -version` and `./mvnw -v`.
 2. If Java < 17, set `JAVA_HOME` to an installed JDK 21 (check `C:\programas` for `jdk-21.*`).
 3. Re-run `./mvnw -DskipTests package`; if it fails, gather the full Maven logs with `./mvnw -e -X package` and report them.

If you update these rules, keep AGENTS.md in the repo root and keep the change
small and well-documented in the commit message.
