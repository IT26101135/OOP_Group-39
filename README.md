# DriveSpark Academy

Driving School Registration and Scheduling System.
SE1020 – Object Oriented Programming.

## Technology

- Java 17
- Spring Boot 3.5.16
- Maven Wrapper
- Spring MVC, JSP and JSTL
- HTML, CSS and JavaScript
- WAR packaging
- File storage: one JSON object per line in .txt files
- No database

## Open in IntelliJ

1. Clone the repository and open the project folder.
2. Select Java 17 as the Project SDK and Maven Runner JRE.
3. Reload Maven and wait for dependencies to download.

## Run on Windows

Run from the project root:

```powershell
.\mvnw.cmd spring-boot:run
```

Open http://localhost:8080/setup-check.

Stop the application with Ctrl+C.

## Build on Windows

```powershell
.\mvnw.cmd clean verify
```

Run the packaged application:

```powershell
java -jar target/drivespark-academy.war
```

Stop any existing application using port 8080 first.

## Architecture

Controller -> Service -> Repository.

- Models represent application data.
- Services contain business rules.
- Repositories handle file reading and writing.
- Controllers handle web requests.
- JSP pages display information.

Java packages: src/main/java/com/drivingschool
JSP pages: src/main/webapp/WEB-INF/views
Static assets: src/main/resources/static
Runtime records: ./data

Run from the project root so ./data resolves consistently.

## Team responsibilities

| Member | Responsibility |
|---|---|
| IT26101135 | Students, authentication, shared foundation and integration |
| IT26101133 | Instructors and bubble sort |
| IT26101134 | Bookings, FIFO queue and scheduling |
| IT26101132 | Vehicles, sample data and test checklist |
| IT26101136 | Learning packages and shared UI |
| IT26101137 | Reviews, moderation and report assembly |

## Git workflow

- main: stable releases
- develop: integration branch
- feature/ITxxxxxxxx-topic: individual work

Create feature branches from develop.
Open pull requests into develop.
The leader reviews and merges teammate contributions.
After the initial setup, do not push directly to main.
Each member must use their own Git identity.

## Current implementation

Phase 0: project configuration and JSP setup-check page.

Authentication, CRUD and file persistence are not implemented yet.
Shared APIs will be documented in CONTRACTS.md during foundation work.
