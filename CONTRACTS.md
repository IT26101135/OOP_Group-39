# DriveSpark Academy — Shared Contracts

## Status

The Phase 1 common foundation and Phase 2A–2G authentication backend
are available in develop.

Available backend components:
- User, Student, Admin and Role models.
- PasswordHasher.
- UserRepository.
- AuthService.
- AuthSessionService.
- Administrator startup seeding and AccountWriteCoordinator.
- StudentLookup and StudentSummary.

Phase 2H implements authentication controllers and JSPs, CSRF enforcement,
controller access annotations, navigation integration and web tests (PR #24).

Phase 3A implements authenticated student profile reading and updates to
name, email and phone through StudentProfileService (PR #27).
The leader reported BUILD SUCCESS for the focused profile tests and full
Maven clean verify with administrator startup seeding disabled.

Branch availability: Phase 2H requires PR #24 to be merged into develop;
Phase 3A requires PR #27 to be merged into develop. Confirm the relevant
PR is marked Merged and update your branch before depending on these APIs.
Implementation and passing local tests alone do not confirm a PR merge.

Student profile controllers/forms/JSPs (Phase 3B), administrator dashboard
functionality, password changes, account deletion safeguards and cross-module
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
Startup seeding is available through Phase 2F.
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

Phase 2H additionally handles MVC ResponseStatusException with status
401 or 403 using fixed, user-safe messages and the shared error page.
See Status for Phase 2H branch availability (PR #24).

CSRF filter rejections occur outside MVC and use a separate HTTP 403
response path.

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
This integration is available in develop.
Do not enable unrestricted polymorphic default typing.

## User models — Phase 2A

Available in develop.

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
- StudentProfileService coordinates student email updates in Phase 3A.
  See Status for branch availability.
- BCrypt format validation does not generate hashes or verify passwords.

Web authentication and annotation-based controller protection are
provided by Phase 2H; see Status for branch availability (PR #24).

Still pending:
- Student profile and administrator dashboard controllers/JSPs.
- Password changes, administrator account editing and deletion safeguards.
- Ownership checks for other modules; student self-profile access is in Phase 3A.

## PasswordHasher — Phase 2B

Available in develop.

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
Session authentication is provided by AuthSessionService.

The browser authentication flow is provided by Phase 2H.
See Status for branch availability (PR #24).

## UserRepository — Phase 2C

Available in develop.

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

Student self-profile updates use AccountWriteCoordinator through
StudentProfileService in Phase 3A; see Status for branch availability.

Pending:
- Password changes and administrator account editing.
- Resource ownership enforcement in module business operations.
- Account deletion safeguards and review cleanup.

Authentication web integration is provided by Phase 2H.
See Status for branch availability (PR #24).

## AuthService — Phase 2D

Available in develop.

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
- Phase 3A student profile updates use this same coordinator for the complete
  authentication reload, validation, uniqueness check and update operation.
- Future password changes, other account updates and deletion must use it too.
- Direct repository writes bypass business-operation coordination.
- Only one application process may use a data directory.

Authentication controllers, JSPs, CSRF enforcement and controller
access checks are provided by Phase 2H; see Status for branch availability (PR #24).

Still pending:
- Password changes, administrator account editing and deletion safeguards.
- Module-specific resource ownership checks.

## AuthSessionService — Phase 2E

Available in develop.

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

Phase 2H integrates this service with authentication controllers,
JSPs, navigation, CSRF enforcement and controller access annotations.

Verification includes full Spring application MockMvc tests and
manual browser checks. MockMvc does not execute JSP rendering or
replace real-browser cookie verification.

Module controllers must still declare their access requirements
and enforce resource ownership. See the Phase 2H contract.

## Administrator startup seed — Phase 2F

Available in develop.

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

Available in develop.

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

## Authentication web integration — Phase 2H

Implemented in PR #24. See Status for branch availability.
Other branches must include the merged changes before using these APIs.

### Classes and configuration

- com.drivingschool.auth.form.RegistrationForm
- com.drivingschool.auth.form.LoginForm
- com.drivingschool.auth.controller.AuthController
- com.drivingschool.auth.service.CsrfTokenService
- com.drivingschool.auth.filter.CsrfFilter
- com.drivingschool.auth.annotation.RequireLogin
- com.drivingschool.auth.annotation.RequireAdmin
- com.drivingschool.auth.interceptor.AccessInterceptor
- com.drivingschool.auth.interceptor.NavigationInterceptor
- com.drivingschool.common.config.WebConfig

Do not create competing authentication, CSRF or navigation mechanisms.
Coordinate changes to shared WebConfig, navbar and CSS with the leader.

### Authentication routes

| Method | Route | Behaviour |
|---|---|---|
| GET | /register | Displays the student registration form |
| POST | /register | Registers a student; redirects to /login?registered |
| GET | /login | Displays the login form |
| POST | /login | Verifies credentials and establishes a new session |
| POST | /logout | Invalidates the session; redirects to /login?loggedOut |

Successful login redirects to / until profile/dashboard routes exist.

Authenticated visitors to registration/login GET or POST routes
are redirected to /. POST requests still require CSRF validation.

There is no GET logout action or public administrator registration.

Registration:
- Allowed fields: name, email, phone, password, confirmPassword.
- ID, role and passwordHash cannot be selected through the form.
- Password confirmation is checked by the controller.
- Existing service/model/password validation remains authoritative.
- Invalid registration returns HTTP 400.
- Duplicate email returns HTTP 409.
- Successful registration does not automatically log the student in.

Login:
- Allowed fields: email and password.
- Incorrect credentials return HTTP 401 with a generic message.
- Failed credential verification invalidates the previous session.
- The redisplayed form receives a token for the new anonymous session.
- StorageException propagates instead of becoming a credential mismatch.

Form objects are temporary input holders, not persisted domain objects.
Do not store them in sessions or files, log their passwords, or repopulate
password inputs when displaying an error.

JSPs:
- src/main/webapp/WEB-INF/views/auth/register.jsp
- src/main/webapp/WEB-INF/views/auth/login.jsp

### CSRF protection

CsrfTokenService public API:
- String getOrCreateToken(HttpServletRequest request)
- boolean isValid(HttpServletRequest request)

Form parameter:
_csrf

Session attribute:
drivespark.csrf.token

The token contains 32 cryptographically random bytes encoded as
URL-safe Base64 without padding.

- getOrCreateToken creates a session/token when needed.
- isValid never creates a session.
- Missing, malformed, incorrect or duplicate token parameters fail.
- Tokens belong to a session and are not persisted in files.
- Session replacement requires a fresh token for subsequent forms.

CsrfFilter validates requests before controller execution.
GET, HEAD, OPTIONS and TRACE are exempt from token validation.
All other request methods require a valid token.
Exemption from CSRF checks does not authorize an HTTP method or action.

Login, registration and logout POSTs are not exempt.
Missing/invalid tokens produce HTTP 403 before business logic runs.

Current support is for form parameters. Do not assume JSON bodies or
custom CSRF headers are supported. Coordinate multipart/AJAX integration
before introducing it.

Never place tokens in URLs or logs.
Never perform state-changing business operations through GET.

JSP attributes:
- csrfParameterName: String
- csrfToken: String

Example hidden field, with JSTL core declared:

```jsp
<input type="hidden"
       name="<c:out value='${csrfParameterName}' />"
       value="<c:out value='${csrfToken}' />">
```

Use c:url for form actions and application links.

AuthController supplies tokens for anonymous authentication forms.
NavigationInterceptor supplies a logout token for authenticated views.

Other form pages must ensure a current token is supplied before rendering.
Do not hard-code tokens or disable validation when a form fails.

CSRF protection does not replace login, role or ownership checks.

### Controller access requirements

Annotations:
- com.drivingschool.auth.annotation.RequireLogin
- com.drivingschool.auth.annotation.RequireAdmin

Apply them to a controller class or individual handler method.

- RequireLogin allows an existing authenticated account of either role.
- RequireAdmin allows only an authenticated administrator.
- Administrator requirements take priority.
- A method-level RequireLogin cannot weaken a class-level RequireAdmin.
- Anonymous access to a protected handler produces HTTP 401.
- Authenticated non-admin access to an admin handler produces HTTP 403.

Unannotated handlers remain public.
A URL beginning with /admin is not automatically protected.

Every new module endpoint must have its access requirements reviewed.
These annotations apply to Spring MVC controller handling, not arbitrary
service calls, scheduled tasks or direct repository operations.

Ownership and student-only restrictions remain module responsibilities.
RequireLogin alone does not enforce either.

Obtain the acting account through AuthSessionService.
Do not trust IDs or roles supplied by browser forms.

### Shared navigation

NavigationInterceptor adds display attributes after normal controller
execution and before view rendering:

- navLoggedIn: Boolean
- navRole: String — PUBLIC, STUDENT or ADMIN
- navDisplayName: String — empty for anonymous visitors
- navHomeActive: Boolean

Authenticated views also receive the CSRF attributes for POST logout.

- User objects and password hashes are not exposed through these attributes.
- Display names must be escaped with c:out.
- Redirects receive no navigation or CSRF model additions.
- Navigation-rendered pages use Cache-Control: no-store.
- Navigation visibility is not authorization.

Current navbar:
- Anonymous: Home, Login, Register.
- Authenticated: Home, escaped display name, POST logout.

Profile/dashboard navigation remains pending until those routes exist.

The navigation post-handler callback is not a guarantee that every
exception-rendered page receives these attributes.

### Authorization error presentation

GlobalExceptionHandler maps MVC ResponseStatusException:
- HTTP 401 to common/error with a fixed login-required message.
- HTTP 403 to common/error with a fixed access-denied message.

Other ResponseStatusException status codes retain Spring's handling.

CsrfFilter failures occur before MVC and use sendError(403).
They do not pass through the MVC exception handler.

### Verification

Tests cover:
- Token generation, session binding and invalid-token rejection.
- Filter rejection before controller execution.
- Form binding and authentication controller behaviour.
- Safe navigation attributes.
- Access requirements and HTTP 401/403 presentation.
- Integrated registration, login, session replacement and logout.
- Anonymous/student/admin access through test-only protected endpoints.

Integration tests use temporary storage and disable startup seeding.
An administrator may be created explicitly inside temporary test storage.

Manual browser checks cover:
- JSP rendering and styling.
- Registration and duplicate-email rejection.
- Failed-login retry with a refreshed token.
- Logged-in/out navbar states and POST logout.
- Mobile layout and keyboard navigation.
- Rejection of a tampered CSRF token.

Still pending:
- Student profile web integration (Phase 3B); the authenticated read/update
  service and its self-profile authorization are implemented in Phase 3A.
- Password changes and administrator account editing.
- Administrator dashboard functionality.
- Account deletion safeguards and review cleanup.
- Protection and verification of each future module endpoint.

## Student profile service — Phase 3A

Implemented in PR #27. See Status for branch availability.
This increment provides a service and tests, not HTTP routes or JSP pages.

Classes:
- com.drivingschool.student.dto.StudentProfile
- com.drivingschool.student.service.StudentProfileService

Constructor:

```java
StudentProfileService(
        UserRepository userRepository,
        AuthSessionService authSessionService)
```

Public API (request type: jakarta.servlet.http.HttpServletRequest):

```java
StudentProfile getOwnProfile(HttpServletRequest request)
StudentProfile updateOwnProfile(
        HttpServletRequest request, String name, String email, String phone)
```

StudentProfile is immutable and exposes only:
- String getId()
- String getName()
- String getEmail()
- String getPhone()

It contains no password hash or role and has no setters. It is profile output,
not a browser form model. Other modules should continue using StudentLookup
for the agreed minimal student lookup rather than accessing private profile data.

### Authorization and identity

- Both methods require an existing authenticated Student through AuthSessionService.
- Anonymous callers and sessions whose account no longer exists receive HTTP 401.
- Administrators receive HTTP 403; this is not an administrator student-edit API.
- The acting student ID comes from the session service, not a request field.
- Callers cannot select the account ID, role or password hash to update.
- Reads use the current persisted account, not a cached session User object.

### Validation and updates

- Name: trimmed, 2-100 characters.
- Email: trimmed, lowercase using Locale.ROOT, maximum 254 characters,
  with the existing User model's email validation.
- Phone: trimmed, 7-15 digits, optionally beginning with +.
- A replacement Student is validated before any existing model is changed.
- ID and password hash are preserved; no new account ID is allocated.
- Email uniqueness covers Student and Admin accounts using strict reads.
- An email belonging to another account raises DuplicateRecordException.
- An existing duplicate email anywhere in the strict account list causes
  StorageException during an otherwise valid update, including a no-op.
  This operation does not repair account data.
- Invalid replacement values raise ValidationException.
- A normalized unchanged submission returns the current profile without writing,
  after strict account/email-integrity checks.
- Changed values are persisted through UserRepository.update().
- StorageException propagates; it is not converted to missing-account or
  duplicate-email responses.

### Coordination and sessions

- The complete update runs inside AccountWriteCoordinator.execute(), including
  the authenticated account reload, validation, uniqueness check and write.
- Registration, administrator seeding and student profile updates share this
  coordinator across service instances within one JVM.
- Direct repository writes bypass these business rules and coordination.
- Only one application process may use a data directory.
- Profile updates do not replace or invalidate the session. AuthSessionService
  reloads the account by ID, so subsequent reads observe the updated details.
- Future account deletion still needs agreed cross-module safeguards.

### Verification and remaining work

StudentProfileServiceTest contains 14 tests covering safe DTO output,
anonymous/admin rejection, own-profile persistence, unchanged submissions,
invalid input, student/admin email conflicts, duplicate stored emails,
missing accounts, storage failures and concurrent email claims.

Tests use temporary file storage and real session handling with mocked
credential authentication. Concurrency cases cover two profile service
instances and profile updates racing with AuthService registration (mocked
password hasher). These are service tests, not full Spring/CSRF/browser tests.

The leader reported successful focused tests and full clean verify with
app.admin-seed.enabled=false. This does not establish profile web behaviour.

Pending Phase 3B:
- Profile controller, form binding, display/edit JSPs and navigation link.
- Controller access annotations, CSRF integration and escaped display values.
- Web integration tests and browser checks.

No profile HTTP endpoint is introduced by Phase 3A. Login redirects and
navigation remain as documented in Phase 2H until the web increment changes them.
Password changes, administrator profile management and account deletion are
separate work. Booking-reference checks and review cleanup remain pending.

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