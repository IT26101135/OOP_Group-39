# DriveSpark Academy — Shared Contracts

## Status

This document accompanies the Phase 1 common foundation.

The common API below is available after this change is merged into develop.
Authentication and cross-module lookup APIs listed as pending must not be
assumed to exist.

Repository:
https://github.com/IT26101135/OOP_Group-39

Leader: IT26101135

## Technology and structure

- Java 17
- Spring Boot 3.5.16
- Maven and WAR packaging
- Spring MVC, JSP and JSTL
- No database
- UTF-8 .txt files containing one JSON object per line

Base Java package: com.drivingschool

Layers:
Controller -> Service -> Repository

JSPs:
src/main/webapp/WEB-INF/views/<module>/

Runtime data:
./data, relative to the application's working directory

Run the application from the project root.

## Implemented: Identifiable

Package:
com.drivingschool.common.model

```java
public interface Identifiable {
    String getId();
}
```

Every persisted model must implement this interface directly or inherit
its implementation.

IDs must be non-blank and immutable after record creation.

## Implemented: FileRepository<T>

Package:
com.drivingschool.common.repository

Declaration:

```java
public abstract class FileRepository<T extends Identifiable>
```

Protected constructor:

```java
FileRepository(
    Path dataDirectory,
    String fileName,
    Class<T> type,
    ObjectMapper objectMapper
)
```

Module repositories extend this class.

Use Spring's injected ObjectMapper and the configured data directory.
The repository copies the mapper configuration internally.

Do not create another common repository implementation.

### Public API

| Method | Return type | Behaviour |
|---|---|---|
| readAll() | List<T> | Returns valid records; logs/skips invalid lines |
| findById(String id) | Optional<T> | Ordinary lookup; may read a partially corrupted file |
| readAllStrict() | List<T> | Throws StorageException if any non-blank record is invalid |
| findByIdStrict(String id) | Optional<T> | Validates the complete file before returning a result |
| save(T record) | void | Creates a record; rejects an existing ID |
| update(T record) | void | Replaces an existing record with the same ID |
| delete(String id) | void | Deletes an existing record |
| nextId(String prefix) | String | Persists an ID reservation and returns the ID |

Use strict reads for:
- Authentication-related lookups.
- Uniqueness and eligibility decisions.
- Booking conflict checks.
- Reference checks before deletion.
- Any decision requiring complete stored information.

Do not catch StorageException and convert it into false, an empty list,
or "no references found" for a safety check.

Ordinary reads are suitable only where displaying a partial list is
acceptable. They log corruption but do not expose a completeness flag.

### Persistence rules

- Simple filenames such as reviews.txt are accepted.
- New repositories create missing directories and storage files.
- Empty record files return empty lists.
- Blank lines are ignored.
- Malformed records, invalid IDs and duplicate IDs are treated as invalid.
- Writes and ID allocation are blocked if record corruption is detected.
- Serialization and filesystem failures are reported as StorageException.
- Temporary files and atomic replacement are used for writes.
- A filesystem that cannot support the required atomic move causes failure.
- Returned models are detached; call update() to persist changes.

### ID counters

Each records file has a separate counter file.

Example:
reviews.txt
reviews.txt.ids.txt

Back up and restore both together.

nextId() reserves a number immediately. Gaps are allowed if a subsequent
operation fails or a reserved ID is not used.

The allocator does not reuse deleted IDs while its counters are preserved.
New records must obtain IDs through nextId(); do not accept IDs from forms.

Missing or invalid counters must not be silently reset.
Existing manually created/imported files require a coordinated import setup.

### Concurrency boundary

A single shared lock serializes repository operations within one JVM.

Only one running application process may use a data directory.

Separate calls such as:
check uniqueness -> nextId -> save
are not one atomic business operation.

Services must coordinate compound operations where races matter.
The leader must approve the shared concurrency approach.

## Implemented: IdGenerator

Package:
com.drivingschool.common.util

```java
public static String nextId(String prefix, int lastIssuedNumber)
```

This utility validates and formats an ID only.
It does not persist or reserve an ID.

Application services should allocate IDs through the repository.

Prefixes:
- S: student
- I: instructor
- B: booking
- V: vehicle
- P: learning package
- R: review

Format: one uppercase letter plus 001-999.
Exhausting the three-digit range raises ValidationException.
Admin ID policy is pending.

## Implemented: exceptions

Package:
com.drivingschool.common.exception

Constructors:

```java
new ValidationException(String message)
new DuplicateRecordException(String message)
new RecordNotFoundException(String message)
new StorageException(String message, Throwable cause)
```

Global MVC exception handling:
- ValidationException: HTTP 400
- RecordNotFoundException: HTTP 404
- DuplicateRecordException: HTTP 409
- StorageException: HTTP 500 with a generic browser message

Shared JSP:
src/main/webapp/WEB-INF/views/common/error.jsp

The first three exception messages must be suitable for users.
Never include passwords, raw records or sensitive internal details.

This handler does not handle every possible Spring error or startup failure.

## Model and serialization responsibilities

Models validate their domain fields; services enforce business rules.
The generic repository validates record identity, not every domain rule.

Provide a Jackson-compatible construction strategy.
Test serialization and deserialization of each actual module model.

Preserve stored creation timestamps when reading records.
Do not replace stored timestamps with the current time during loading.

Use the injected Spring ObjectMapper so configured date/time modules are
retained. Timestamp format and User subtype configuration must be verified
in integration tests before relying on them.

User -> Student/Admin polymorphic storage remains pending.
Do not enable unrestricted polymorphic default typing.

## Pending: authentication

The leader will implement and document these capabilities:

- getCurrentUser()
- isLoggedIn()
- isAdmin()
- requireLogin()
- requireAdmin()

Their exact Java signatures, packages, session attributes and routes are
not yet published.

Do not create competing authentication or session implementations.
Do not treat a temporary stub as production authorization.

## Pending: cross-module lookups

These are capability names, not final Java declarations:

- StudentLookup: exists(id), findById(id)
- InstructorLookup: exists(id), isAvailable(id, dateTime)
- VehicleLookup: exists(id), isAvailable(id, dateTime)
- PackageLookup: exists(id)
- BookingLookup: student/instructor/vehicle reference checks
- Review-owned cleanup operation for student account deletion

Exact types, packages, status semantics and failure behaviour require
agreement with the leader and the owning member.

Additional package-entitlement and scheduling contracts remain pending.
Never read or modify another module's files directly.

## Agreed review policies

Owner: IT26101137.

- Review target: DriveSpark Academy as a school.
- No completed booking is required in this version.
- Student must be logged in and have an existing account.
- One stored review per student, including pending or hidden reviews.
- After deletion, the student may submit another review.
- Rating: integer 1-5.
- Comment: trimmed, 10-1,000 characters.
- New review status: PENDING.
- Students see approved reviews and their own reviews in every status.
- Administrators see all reviews and may approve or hide them.
- Public review browsing is not included.
- Students may edit/delete only their own reviews.
- Actual rating/comment changes reset status to PENDING.
- Unchanged submissions do not reset status.
- Administrators moderate status, not the student's content.
- No separate administrator review-delete action.

Fields:
id, studentId, rating, comment, status, createdAt, updatedAt.

Use LocalDateTime with the agreed Asia/Colombo application timezone.
Set timestamps on the server; preserve createdAt during updates.
ISO local date-time persistence requires a model round-trip test.

When a student account is deleted, remove the student's reviews through
a review-owned service operation. Its contract, concurrency and partial
failure handling remain pending.

Browser input must not control identity, author, moderation status
or timestamps.

## Other conventions

Dates: yyyy-MM-dd
Times: HH:mm

Booking statuses:
PENDING, CONFIRMED, COMPLETED, CANCELLED

Review statuses:
PENDING, APPROVED, HIDDEN

Instructors do not have login accounts.

Other unresolved policies include booking transition rules, overlap
intervals, FIFO handling of unavailable requests, package entitlements,
historical booking retention and instructor/package deletion eligibility.

## Team workflow

- Create feature branches from develop.
- Use feature/ITxxxxxxxx-topic.
- Commit with your own Git identity.
- Run clean verify before requesting review.
- Submit PRs into develop.
- Leader reviews and merges.
- Stable releases move from develop into main.
- Coordinate changes to shared files.
- Keep runtime records and credentials out of Git.

When the foundation is merged, each member must update their feature
branch from develop and provide this document plus relevant shared source
files to their AI assistant.