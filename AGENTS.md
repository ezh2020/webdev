# Repository Guidelines

## Project Structure & Modules
- Backend Java source lives in `src/main/java/com/webdev/webdev`, grouped by layer: `controller`, `service`, `mapper`, and `model`.
- Configuration, templates, and static assets live under `src/main/resources` (e.g. `application.yml`, `templates/`, `static/`).
- Tests are in `src/test/java` (for example `MyDemoTest.java`); add new tests in matching package structures.

## Build, Test, and Run
- Build artifact: `./mvnw clean package` – compiles the app and produces a runnable JAR in `target/`.
- Run locally: `./mvnw spring-boot:run` – starts the Spring Boot application with the current `application.yml`.
- Run tests: `./mvnw test` – executes the JUnit test suite.

## Coding Style & Naming
- Use Java 17, 4‑space indentation, and standard Spring Boot conventions.
- Organize classes by layer and suffix: `*Controller`, `*Service`, `*Mapper`, `*Repository` (if added), and domain `*` models under `model/`.
- Prefer constructor or `@RequiredArgsConstructor` injection (Lombok) for services and controllers.
- Keep REST endpoints in controllers thin; move business logic into services.

## Testing Guidelines
- Use the Spring Boot testing stack from `spring-boot-starter-test` (JUnit, Mockito).
- Place tests in `src/test/java` mirroring the main package; name classes `*Test` (e.g. `UserServiceTest`).
- When adding features, add or update tests to cover typical flows and at least one failure/edge scenario.

## Commits & Pull Requests
- Write concise, imperative commit messages, e.g. `Add course enrollment endpoint` or `Fix login redirect`.
- For pull requests, include: a short summary, list of key changes, any breaking impacts, and how to reproduce or verify (commands, URLs, sample payloads).
- Link issues or tasks where applicable and mention any database or configuration changes required.

