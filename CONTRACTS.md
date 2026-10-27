# DriveSpark Academy — Shared Contracts

## Status

The Phase 1 common foundation is available in develop.

Phase 2A adds the User, Student and Admin models and their tests.
These models become available in develop after the model-stage PR is merged.

Phase 2B adds PasswordHasher.
Phase 2C adds UserRepository and account persistence tests.
Each stage becomes available in develop after its PR is merged.

Phase 2D provides student registration and credential verification.
Phase 2E adds session authentication and role-checking methods.
The session API becomes available in develop after its PR is merged.

Phase 2F adds administrator startup seeding and shared account-write
coordination. These become available in develop after the admin-seed
PR is merged.

Controllers, route protection, CSRF protection and cross-module lookup
APIs marked pending must not be assumed to exist.

Repository: https://github.com/IT26101135/OOP_Group-39

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
- A: administrator

Format: one uppercase letter plus 001-999.
Exhausting the three-digit range raises ValidationException.
Administrator IDs use A001-A999.
Administrator accounts will be created through a controlled startup seed;
public administrator registration is not allowed.
Startup seeding is provided by Phase 2F after its PR is merged.
See the Administrator startup seed section.

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

User -> Student/Admin JSON serialization and reconstruction are implemented
and covered by model tests.

Phase 2C provides FileRepository integration through UserRepository,
with persistence tests for Student and Admin records.
This integration becomes available in develop after the Phase 2C PR is merged.
Do not enable unrestricted polymorphic default typing.

## User models — Phase 2A

These models become available to other branches after the model-stage
pull request is merged into develop.

Classes:
- com.drivingschool.auth.model.Role
- com.drivingschool.auth.model.User
- com.drivingschool.auth.model.Admin
- com.drivingschool.student.model.Student

User is abstract and implements Identifiable.

Shared fields:
- id: immutable; S001-S999 for students, A001-A999 for administrators.
- name: trimmed, 2-100 characters.
- email: trimmed, lowercase using Locale.ROOT, maximum 254 characters,
  with basic format validation.
- passwordHash: BCrypt encoded-format validation.

Student additionally requires:
- phone: trimmed, 7-15 digits, optionally beginning with +.

Roles:
- Student.getRole() returns Role.STUDENT.
- Admin.getRole() returns Role.ADMIN.
- There is no role setter.

Planned dashboard paths:
- Student: /student/profile
- Admin: /admin/dashboard

These paths are model return values only; their controllers and JSP pages
are not implemented yet.

JSON:
- The type property identifies STUDENT or ADMIN.
- Only the explicitly registered subtypes are supported.
- Role and dashboardPath are derived and excluded from stored JSON.
- passwordHash must remain serializable for file persistence.
- Do not expose these persisted models directly as public JSON responses
  or bind registration forms directly to them.

Validation:
- Constructors and setters validate model fields.
- Invalid setter input leaves the previous field value unchanged.
- AuthService enforces account-wide email uniqueness during registration.
- Coordinated email updates remain pending.
- BCrypt format validation does not generate hashes or verify passwords.

Still pending:
- Web integration of AuthSessionService.
- Route protection, ownership checks, controllers and JSP pages.

## PasswordHasher — Phase 2B

Available to other branches after the password-hasher PR is merged
into develop.

Class:
com.drivingschool.auth.util.PasswordHasher

Spring component with these public methods:

- String hash(String rawPassword)
- boolean matches(String rawPassword, String storedHash)

Behaviour:
- Uses BCrypt with strength 12 and a random salt.
- New passwords require at least 8 Unicode code points.
- Passwords must not exceed 72 UTF-8 bytes.
- Passwords are not trimmed or case-normalized.
- Invalid new passwords cause ValidationException.
- Incorrect passwords and invalid verification inputs return false.
- Generated hashes are compatible with Student and Admin.
- The helper does not read/write files or manage login sessions.

Call hash() when creating or changing a password.
Call matches() when checking a login password.
Do not hash the login input again and compare hash strings.

Do not log plaintext passwords or stored hashes.
Repository storage failures must still propagate; do not convert them
into ordinary password mismatches.

Registration and credential verification are provided by AuthService.
Account persistence is provided by UserRepository.
Session authentication is provided by AuthSessionService after the
Phase 2E PR is merged. Web integration remains pending.

## UserRepository — Phase 2C

Available to other branches after the user-repository PR is merged
into develop.

Class:
com.drivingschool.auth.repository.UserRepository

Extends:
FileRepository<User>

Constructor:
UserRepository(String dataDirectory, ObjectMapper objectMapper)

Storage:
- Uses app.data-directory, defaulting to ./data.
- Records: users.txt.
- Persistent ID counters: users.txt.ids.txt.
- Student and Admin records share this file.
- Student account details are not duplicated in students.txt.
- Each line contains one JSON object.
- The type property selects STUDENT or ADMIN.
- Password hashes are persisted; plaintext passwords are not.

IDs:
- nextId("S") reserves a student ID.
- nextId("A") reserves an administrator ID.

Authentication and account-integrity decisions must use strict reads.
Do not catch StorageException and treat it as an absent account.

The repository inherits storage CRUD operations.
It does not enforce email uniqueness, authorization, booking safeguards
or review cleanup. Those are service-layer responsibilities.

Other modules must use the agreed lookup/service APIs once available.
They must not read users.txt directly or access password hashes.

Do not manually create or reset account files or ID counters.
Do not commit runtime account records or credentials.

Pending:
- Web integration, route protection and ownership checks.
- Account update operations using AccountWriteCoordinator.
- Account deletion safeguards.

## AuthService — Phase 2D

Available to other branches after the auth-service PR is merged into develop.

Class:
com.drivingschool.auth.service.AuthService

Constructor:
AuthService(UserRepository userRepository, PasswordHasher passwordHasher)

Public methods:
- Student registerStudent(
  String name, String email, String phone, String rawPassword)
- Optional<User> authenticate(String email, String rawPassword)

Registration:
- Always creates a Student; callers cannot select a role or ID.
- Email is trimmed and converted to lowercase using Locale.ROOT.
- Email uniqueness covers both Student and Admin accounts.
- Uses strict repository reads.
- Hashes the password using PasswordHasher.
- Reserves an S-prefixed ID and saves the validated Student.
- Duplicate email raises DuplicateRecordException.
- Invalid input raises ValidationException.
- Failed operations may leave unused reserved IDs; gaps are permitted.

Credential verification:
- Returns the matching User only when the password is correct.
- Unknown email or incorrect password returns Optional.empty().
- Does not create or modify an HTTP session.
- Uses strict reads; StorageException is not converted to login failure.
- Multiple stored accounts matching the supplied email cause StorageException.
- Uses a dummy hash for unknown-email password checks.
- Returned User objects are internal and must not be exposed as public JSON.

Concurrency:
- Registration and administrator seeding execute their complete
  check/allocate/save operations through AccountWriteCoordinator.
- AccountWriteCoordinator uses a static synchronized method shared
  across service instances within the application JVM.
- Future account updates and deletion must use this coordinator too.
- Direct repository writes bypass business-operation coordination.
- Only one application process may use a data directory.

Still pending:
- Web integration of AuthSessionService, including controllers,
  JSP pages, route protection, ownership checks and CSRF protection.
- Account updates and deletion safeguards.

## AuthSessionService — Phase 2E

Available to other branches after the auth-session PR is merged into develop.

Class:
com.drivingschool.auth.service.AuthSessionService

Constructor:
AuthSessionService(AuthService authService, UserRepository userRepository)

Public API:
- boolean login(
  HttpServletRequest request, String email, String rawPassword)
- Optional<User> getCurrentUser(HttpServletRequest request)
- boolean isLoggedIn(HttpServletRequest request)
- boolean isAdmin(HttpServletRequest request)
- User requireLogin(HttpServletRequest request)
- User requireAdmin(HttpServletRequest request)
- void logout(HttpServletRequest request)

Request type:
jakarta.servlet.http.HttpServletRequest

Behaviour:
- login delegates credential verification to AuthService.
- Successful login invalidates any previous session and creates a new one.
- Failed credentials invalidate any previous session and return false.
- Storage failures propagate; they are not ordinary credential failures.
- Successful login sets a 30-minute inactivity timeout.
- Session identity is stored under drivespark.auth.userId.
- Only the user ID is stored, not a User object, role or password hash.
- Other modules must use this service instead of accessing that attribute.

Current account lookup:
- Uses UserRepository.findByIdStrict on each check.
- Missing session or anonymous session returns Optional.empty().
- Invalid session identity or a missing account invalidates the session.
- StorageException propagates.
- Checks do not create a session for anonymous users.

Authorization:
- requireLogin returns the current User or throws ResponseStatusException
  with HTTP 401.
- requireAdmin returns the current administrator or throws HTTP 403
  for an authenticated non-admin.
- Anonymous requireAdmin calls produce HTTP 401.
- isLoggedIn and isAdmin may throw StorageException.
- A role check does not replace ownership checks for student resources.

Logout:
- Invalidates an existing session.
- Does not create a new session.
- Repeated logout without a session is allowed.

Integration:
- Controllers receive HttpServletRequest from Spring MVC and pass it to
  this service.
- Do not expose returned User objects as public JSON.
- This service does not automatically protect routes.
- Controllers/interceptors must invoke the appropriate checks.

Still pending:
- Login/logout controllers and JSP pages.
- Access interceptor and route configuration.
- CSRF protection for state-changing browser requests.
- Browser/session-cookie integration tests.
- MVC handling and presentation of 401/403 errors.

## Administrator startup seed — Phase 2F

Available in develop after the admin-seed PR is merged.
Phase 2G adds StudentLookup and the immutable StudentSummary.
These become available in develop after the student-lookup PR is merged.

Classes:
- com.drivingschool.auth.util.AccountWriteCoordinator
- com.drivingschool.auth.service.AdminSeedService
- com.drivingschool.auth.config.AdminSeedRunner

Service method:
boolean seedIfNeeded(String name, String email, String rawPassword)

Behaviour:
- Uses strict account reads.
- Creates an Admin only when no administrator exists.
- Returns true when created, false when an administrator already exists.
- When an administrator already exists, leaves account records and ID
  counters unchanged.
- Creating a new administrator advances the A-prefixed ID counter.
- Rejects an email already used by a student.
- Uses PasswordHasher and an A-prefixed repository-generated ID.
- Registration and seeding share AccountWriteCoordinator.
- Invalid input and storage failures propagate.
- This is a startup operation, not a public administrator-registration API.

Environment variables:
- DRIVESPARK_ADMIN_SEED_ENABLED: defaults to false.
- DRIVESPARK_ADMIN_NAME: defaults to System Admin.
- DRIVESPARK_ADMIN_EMAIL: no default account email.
- DRIVESPARK_ADMIN_PASSWORD: no default password.

When enabled, a seed failure prevents successful application startup.
Existing administrators cause seeding to be skipped, even if seed
credentials are absent.

Disable seeding and remove the password from the local run configuration
after successful initial creation. Re-enable only for intentional bootstrap.

Never commit real seed credentials, runtime account files or hashes.
The seed is not a password-reset mechanism.

## StudentLookup — Phase 2G

Available to other branches after the student-lookup PR is merged into develop.

Interface:
com.drivingschool.student.api.StudentLookup

Implementation:
com.drivingschool.student.service.StudentLookupService

Result type:
com.drivingschool.student.dto.StudentSummary

Public API:
- boolean exists(String studentId)
- Optional<StudentSummary> findById(String studentId)

StudentSummary:
- String getId()
- String getName()
- Immutable; no setters.
- Contains no password hash, email or phone number.

Behaviour:
- Accepts student IDs S001-S999 exactly; does not trim IDs.
- Existing student: true / populated summary.
- Missing valid student ID: false / Optional.empty().
- Null, blank, malformed or non-student ID: ValidationException.
- Uses strict account reads.
- StorageException propagates; never convert it to "student not found".
- Each call reads current persisted data.
- Does not modify accounts or require an HTTP session.

Integration:
- Other modules inject StudentLookup through their constructors.
- Do not create another implementation or read users.txt directly.
- This lookup does not authorize an action.
- Student-owned requests must derive student identity from the
  authenticated user, not trust a browser-supplied student ID.
- Administrator actions must enforce administrator authorization.
- Lookup results are snapshots, not reservations.
- Coordinating booking creation with student deletion remains pending.

## Pending: cross-module lookups

These are capability names, not final Java declarations:

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