# Repository Guidelines

## Project Structure & Module Organization
- Backend code lives in `src/main/java/com/webdev/webdev`, grouped by layer: `controller`, `service`, `mapper`, `model`, `config`, and `websocket`.
- Web assets are in `src/main/resources/static` (public HTML such as `index.html`, `login.html`) and `src/main/resources/templates` for server-rendered views.
- Application configuration is in `src/main/resources/application.yml`.
- Automated tests live in `src/test/java/com/webdev/webdev` (for example `CourseServiceTest.java`, `PermissionIntegrationTest.java`).
- Docker and database setup are in the project root: `docker-compose.yml`, `Dockerfile*`, and the SQL snapshot file.

## Build, Test, and Development Commands
- Build JAR locally:
  ```bash
  ./mvnw clean package
  ```
- Run tests:
  ```bash
  ./mvnw test
  ```
- Run Spring Boot locally:
  ```bash
  ./mvnw spring-boot:run
  ```
- With Docker (MySQL + dev container):
  ```bash
  docker compose up -d --build
  docker compose exec webdev bash    # then: ./mvnw spring-boot:run
  ```

## Coding Style & Naming Conventions
- Java 17, Spring Boot 2.7; use 4-space indentation and UTF-8 encoding.
- Packages are `lowercase`, classes `PascalCase`, methods and fields `camelCase`, constants `UPPER_SNAKE_CASE`.
- Name components by role: `*Controller`, `*Service`, `*Mapper`, `*Request`, `*Response`, `*Dto`.
- Prefer constructor or `@RequiredArgsConstructor` injection; avoid field injection for new code.

## Testing Guidelines
- Use the default Spring Boot test stack from `spring-boot-starter-test` (JUnit 5, MockMvc, etc.).
- Place unit and slice tests under the matching package in `src/test/java/com/webdev/webdev`.
- Name test classes `<ClassUnderTest>Test` and methods `should...` to describe behavior.
- Run `./mvnw test` before pushing; add tests for new endpoints, services, and regressions.

## Commit & Pull Request Guidelines
- Write concise, descriptive commit messages (English or Chinese) that state the change and scope, for example: `feat: add course search endpoint`.
- Keep commits focused; avoid mixing refactors and feature changes.
- For PRs, include: purpose, key changes, how to run or test, and screenshots/URLs for UI-facing changes (e.g. affected pages in `static/*.html` or `templates/`).

## Security & Configuration Tips
- Do not commit real credentials for non-local environments; treat values in `application.yml` as local-only defaults.
- For new configuration, prefer environment variables or Docker compose overrides instead of hardcoding secrets.

