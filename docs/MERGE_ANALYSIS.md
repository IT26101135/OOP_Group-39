# Merge test analysis

> 2026-11-09 cleanup update: the unused instructor interval/lookup family, vehicle lookup family and enrollment lookup/summary were removed with member approval. Earlier descriptions of those contracts are historical, not current APIs. See [Java simplification record](JAVA_SIMPLIFICATION.md) for retained OOP evidence and verification.

**Historical snapshot:** the root build.log cited below was removed during the approved cleanup on 9 November 2026. The old failing-run observations below are preserved as history, not current build evidence. See the latest dated verification in VIVA_QA.md.

Owner: IT26101135 (integration). Date: 9 November 2026.

## Scope and baseline

This is analysis only. No source files, tests, configuration, data files or Git index entries were changed. No Maven build was started by this analysis. No Git write command was run. The only file created is this document.

The existing build completed at **2026-11-09 14:22:26 +05:30**. At that time, `build.log` and that run's `target/surefire-reports/TEST-*.xml` agreed:

```text
Tests run: 663, Failures: 14, Errors: 1, Skipped: 0
BUILD FAILURE
```

That means 648 tests passed, 14 assertions failed and 1 test errored. The reports were initially still being written; the table below uses the completed run, not that partial result. The reported runtime was Java 26, while `pom.xml` targets Java 17. This is a reproducibility difference, not an established cause of these failures.

Read-only `git status --short` shows the incoming instructor and enrollment contract files and tests staged for the ongoing merge. `git diff --name-only --diff-filter=U` returned no paths. That confirms no currently unmerged paths in the index; it does not commit or finish the merge.

## All failing and erroring tests

Class names below are under `com.drivingschool`. The source location is the first project frame in the report. Reasons are copied from the first message line. A1, A2 and A3 are different symptoms of the same likely test-data isolation problem; B is a separate, confirmed wiring problem.

| Group | Test class and method | Kind | First reason line | Source location |
| --- | --- | --- | --- | --- |
| A1 | `admin.AdminDashboardWebIntegrationTest.administratorCanOpenDashboardAndVehiclesThenLogOut` | failure | `expected: <true> but was: <false>` | `AdminDashboardWebIntegrationTest.java:87` |
| A1 | `auth.AuthWebIntegrationTest.adminEndpointRejectsAnonymousAndStudentButAllowsAdmin` | failure | `expected: <true> but was: <false>` | `AuthWebIntegrationTest.java:204` |
| A3 | `booking.repository.BookingRepositorySpringIntegrationTest.springInjectedObjectMapperStoresIsoDateTimes` | failure | `expected: <1> but was: <16>` | `BookingRepositorySpringIntegrationTest.java:91` |
| B | `instructor.config.InstructorConfigTest.configRegistersSingletonBeansAndPersistsInstructor` | error | `No qualifying bean of type 'com.drivingschool.instructor.repository.InstructorAvailabilityRepository' available` | `InstructorConfigTest.java:58` |
| A1 | `student.StudentProfileWebIntegrationTest.administratorCannotReadOrUpdateStudentProfile` | failure | `expected: <true> but was: <false>` | `StudentProfileWebIntegrationTest.java:71` |
| A2 | `vehicle.VehicleWebIntegrationTest.duplicateRegistrationNumberOnEditReturnsConflict` | failure | `Range for response status value 401 expected:<REDIRECTION> but was:<CLIENT_ERROR>` | `VehicleWebIntegrationTest.java:648` |
| A2 | `vehicle.VehicleWebIntegrationTest.successfulEditChangesOnlyEditableVehicleFields` | failure | `Range for response status value 401 expected:<REDIRECTION> but was:<CLIENT_ERROR>` | `VehicleWebIntegrationTest.java:648` |
| A2 | `vehicle.VehicleWebIntegrationTest.successfulCreationPersistsVehicleAndRedirects` | failure | `Range for response status value 401 expected:<REDIRECTION> but was:<CLIENT_ERROR>` | `VehicleWebIntegrationTest.java:648` |
| A2 | `vehicle.VehicleWebIntegrationTest.invalidCreationRedisplaysFormWithoutSaving` | failure | `Range for response status value 401 expected:<REDIRECTION> but was:<CLIENT_ERROR>` | `VehicleWebIntegrationTest.java:648` |
| A2 | `vehicle.VehicleWebIntegrationTest.invalidEditRedisplaysFormWithoutPersistingChanges` | failure | `Range for response status value 401 expected:<REDIRECTION> but was:<CLIENT_ERROR>` | `VehicleWebIntegrationTest.java:648` |
| A2 | `vehicle.VehicleWebIntegrationTest.administratorCanUpdateVehicleTypeTransmissionAndStatus` | failure | `Range for response status value 401 expected:<REDIRECTION> but was:<CLIENT_ERROR>` | `VehicleWebIntegrationTest.java:648` |
| A2 | `vehicle.VehicleWebIntegrationTest.administratorCanAccessListCreateAndEditPages` | failure | `Range for response status value 401 expected:<REDIRECTION> but was:<CLIENT_ERROR>` | `VehicleWebIntegrationTest.java:648` |
| A2 | `vehicle.VehicleWebIntegrationTest.missingAndInvalidCsrfTokensCannotCreateOrUpdateVehicles` | failure | `Range for response status value 401 expected:<REDIRECTION> but was:<CLIENT_ERROR>` | `VehicleWebIntegrationTest.java:648` |
| A2 | `vehicle.VehicleWebIntegrationTest.duplicateRegistrationNumberReturnsConflict` | failure | `Range for response status value 401 expected:<REDIRECTION> but was:<CLIENT_ERROR>` | `VehicleWebIntegrationTest.java:648` |
| A2 | `vehicle.VehicleWebIntegrationTest.invalidAndMissingVehicleIdsAreRejected` | failure | `Range for response status value 401 expected:<REDIRECTION> but was:<CLIENT_ERROR>` | `VehicleWebIntegrationTest.java:648` |

## Group A: tests have unexpected starting data — 14 failures

### A1: an administrator already exists — 3 failures

The admin-dashboard, authentication and student-profile tests call `AdminSeedService.seedIfNeeded(...)` and expect `true`. That method deliberately returns `false` when **any** administrator already exists. See `src/main/java/com/drivingschool/auth/service/AdminSeedService.java:56` and the three source locations in the table. This is a setup assertion; the failing tests have not yet reached their intended admin page checks.

The tests use temporary directories and explicitly disable `app.admin-seed.enabled`, but do not explicitly disable `app.demo-seed.enabled`. Those are separate switches. An enabled demo loader can create an administrator even with the admin bootstrap disabled. There is no evidence that the production rule to create only the first administrator is wrong.

### A2: vehicle tests log in with the wrong password for an existing admin — 10 failures

All ten vehicle failures stop at `VehicleWebIntegrationTest.login`, line 648, before the intended vehicle operation. The response is HTTP 401, not a redirect. The captured response says `Email or password is incorrect.`

`src/test/java/com/drivingschool/vehicle/VehicleWebIntegrationTest.java:614` contains `loginAsAdmin()`. It reuses any existing administrator's email, but `login()` always supplies the vehicle test's own password. In the failing XML request output, the selected email is **admin@drivingschool.test**, the demo loader's administrator. That account is created using `DemoDataSeedRunner.DEMO_PASSWORD`, which differs from the vehicle fixture password. The test therefore does not establish the intended authenticated session. This does not demonstrate a broken vehicle controller, CSRF rule or password hasher.

### A3: the booking storage test finds 16 rows instead of 1 — 1 failure

`src/test/java/com/drivingschool/booking/repository/BookingRepositorySpringIntegrationTest.java` starts the application with a temporary directory and `--app.admin-seed.enabled=false`, saves one booking and expects one line in `bookings.txt` at line 91. It does not explicitly disable demo seeding. `DemoDataSeedRunner.seedBookings()` creates 15 bookings (`src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java:192`). Fifteen demo bookings plus the test booking explains the observed 16 lines.

The test already passed its save-and-reload field assertions before failing on the line count. The later raw JSON assertions are not reached. Do not change the expected count to 16 or remove those assertions; the intended test should still start with an empty temporary repository.

### Evidence, ownership and confidence

| Evidence | What it tells us |
| --- | --- |
| `src/main/resources/application.properties:24` | Demo seeding defaults to false but can be enabled through `DRIVESPARK_DEMO_SEED_ENABLED` or a higher-priority Spring property. |
| `src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java:38` | The demo runner is conditional on `app.demo-seed.enabled=true`. |
| Same file, lines 80–81 and 192–193 | It creates the exact demo admin email seen in the vehicle requests and 15 bookings. |
| The five failing integration test classes | They disable the admin bootstrap but do not pin the independent demo switch to false. |
| `SchedulingWebIntegrationTest`, `EnrollmentWebIntegrationTest`, `AdminEnrollmentWebIntegrationTest`, `ReviewWebIntegrationTest` | These already explicitly set `app.demo-seed.enabled=false`; this is an existing project pattern. |

**Confirmed:** unexpected existing admin/setup outcomes, the demo admin selected by vehicle tests, the 16-line booking file and the source mechanisms above. **Strong inference:** demo seeding entered the affected test application contexts. **UNVERIFIED:** exactly which setting enabled it in the Maven process that produced these reports. The reports do not record that effective property or its source. The current shell's selected environment-variable check does not reconstruct a completed process's environment. Do not claim an incoming instructor class enabled demo seeding: no such change was found.

This involves our existing integration setup, not a proven fault in the incoming contracts:

| Files involved | Owner | Role in the problem |
| --- | --- | --- |
| `src/test/java/com/drivingschool/admin/AdminDashboardWebIntegrationTest.java` | IT26101135 | Expects a fresh admin fixture. |
| `src/test/java/com/drivingschool/auth/AuthWebIntegrationTest.java` | IT26101135 | Expects a fresh admin fixture. |
| `src/test/java/com/drivingschool/student/StudentProfileWebIntegrationTest.java` | IT26101135 | Expects a fresh admin fixture. |
| `src/test/java/com/drivingschool/vehicle/VehicleWebIntegrationTest.java` | IT26101132 | Reuses an existing admin but assumes its own password. |
| `src/test/java/com/drivingschool/booking/repository/BookingRepositorySpringIntegrationTest.java` | IT26101134 | Expects an otherwise empty booking file. |
| `src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java` | IT26101132 | Legitimate optional loader; keep it intact. |
| `src/main/java/com/drivingschool/auth/service/AdminSeedService.java`, `src/main/resources/application.properties` | IT26101135 | Existing bootstrap rule and separate seed settings; do not weaken them. |

### Smallest safe fix to approve

1. First reproduce with both seed settings explicitly disabled for tests. A controlled future command is `.\mvnw.cmd clean verify -Dapp.demo-seed.enabled=false -Dapp.admin-seed.enabled=false`. This command was **not run** during this analysis. If it clears the 14 assertions, it supports the environment/isolation diagnosis without changing production code.
2. For durable isolation, add `registry.add("app.demo-seed.enabled", () -> false);` to the existing `@DynamicPropertySource` method in the four web integration classes above. Their existing temporary directories and assertions stay unchanged.
3. Add `"--app.demo-seed.enabled=false"` to the existing application arguments in `BookingRepositorySpringIntegrationTest`. Keep its temporary directory, ISO date checks and expected one-line count.
4. Keep `DemoDataSeedRunnerTest` and its explicit, temporary-directory seed checks intact. Do not disable or delete the actual sample loader, reset real data, change demo credentials or loosen authentication to make these tests pass.

These are test-fixture settings, not behavior changes. Do not edit booking, payment, availability or FIFO rules for this group. If the controlled rerun still fails with seeding explicitly off, inspect the effective Spring property sources and actual temporary repository paths before approving a different fix. An alternative source of pre-existing data remains UNVERIFIED until that check.

## Group B: incoming instructor configuration contract is not wired — 1 error

The teammate's updated `src/test/java/com/drivingschool/instructor/config/InstructorConfigTest.java` registers only `InstructorConfig`, an `ObjectMapper` and a mocked `AuthSessionService` in an `AnnotationConfigApplicationContext`. `context.refresh()` succeeds. It retrieves the existing instructor repository and service, then fails at line 58 when requesting `InstructorAvailabilityRepository`.

The current `src/main/java/com/drivingschool/instructor/config/InstructorConfig.java` defines only `instructorRepository(...)` and `instructorService(...)`. The three incoming concrete classes have no Spring component annotations, and this isolated test does not scan the application. The missing bean is therefore expected from the current configuration. This is **not** an application-context startup failure or a duplicate-bean error.

The test would next ask for `InstructorLookup` and `InstructorAvailabilityService`, which are also not registered. Those are predicted follow-on omissions from source inspection, not additional errors counted in this run.

**Ownership:** IT26101133's incoming availability/lookup classes and expanded configuration test, together with our retained two-bean `InstructorConfig`. The integration gap is the missing additive registrations; no constructor compilation failure was found.

### Smallest safe fix to approve

Add three ordinary `@Bean` methods in `InstructorConfig`, with beginner-level comments and the existing member header preserved:

| Bean | Constructor/dependencies verified in source |
| --- | --- |
| `InstructorAvailabilityRepository` | `new InstructorAvailabilityRepository(Path.of(dataDirectory), objectMapper)` using the existing configured data directory. |
| `InstructorLookup` | `new InstructorLookupService(instructorRepository, instructorAvailabilityRepository)`. Return the interface or implementation consistently, with exactly one bean implementing `InstructorLookup`. |
| `InstructorAvailabilityService` | `new InstructorAvailabilityService(instructorRepository, instructorAvailabilityRepository, authSessionService)`. |

Retain the two existing beans. Do not replace them, add a component scan to make the test less precise, or register the new classes both by component annotations and `@Bean`. The full application already supplies `AuthSessionService`; the isolated test explicitly supplies its mock. Leave the configuration test's singleton and persistence assertions intact.

Whether these proposed registrations make the full build pass is **UNVERIFIED** until approved edits and a new verification run. Do not report a future successful result now.

## Keep both contract families without changing the working flows

| Incoming teammate code | Existing working code | Decision and caution |
| --- | --- | --- |
| IT26101133: `InstructorAvailability`, `InstructorAvailabilityRepository`, `InstructorAvailabilityService` | IT26101133: `Availability`, `AvailabilityRepository`, `InstructorScheduleService`, `InstructorScheduleOperations` | Keep both. The incoming repository writes `instructor-availability.txt`; the working repository writes `availability.txt`. They are different types and files, so adding the missing beans does not overwrite the current schedule file. They are **not synchronized**. Do not present them as interchangeable or connect both write services to the same UI. |
| IT26101133: `InstructorLookup`, `InstructorLookupService`, `InstructorBookingInfo` | Working `BookingConflictChecker` uses `AvailabilityRepository` and `InstructorLeaveRepository` | Keep the incoming lookup contract and implementation, but do not replace the current conflict checker. The new `isWorkingAvailable(...)` reads incoming interval records, not the working schedule and leave records. Its source checks active instructors and continuous interval coverage; it is not a substitute for all current booking checks. |
| IT26101136: `EnrollmentLookup`, `EnrollmentSummary` | IT26101136: `EnrollmentOperations`, `EnrollmentAdministration`, `EnrollmentService`, `Enrollment` | Keep the new read contract and summary. They do not duplicate the student/payment operation interfaces. `EnrollmentService` currently implements `EnrollmentOperations` and `EnrollmentAdministration`, not `EnrollmentLookup`. No current main-source consumer injects `EnrollmentLookup`, and no failure requests that bean. No adapter is needed to fix the reported failures. |

The incoming enrollment interface is `Optional<EnrollmentSummary> findById(String enrollmentId)`. The summary holds ID links, purchased lesson allowance and validity dates, but no payment state. It must not become a new way to bypass the Paid-enrollment rule. If a future consumer needs it, approve a small adapter with explicit payment/access semantics and tests separately. Do not replace `EnrollmentService`, auto-confirm payments or infer booking eligibility from the summary alone.

The incoming instructor interfaces and concrete classes can coexist by Java type and file path. That does **not** make the separate scheduling stores one source of truth. Keep the current routed availability, booking, leave, payment and FIFO implementation authoritative. No data migration, file deletion, dual write or route change is proposed.

The incoming Java files also lack the full member-header/method-comment style used by the existing project. That is a documentation gap, not a cause of these test failures. If those files are touched in a later approved fix, preserve behavior and apply the required headers/comments; do not expand this merge repair into a rewrite.

## What the new tests actually show

| Test class (all under `com.drivingschool.instructor`) | Tests | Failures | Errors |
| --- | ---: | ---: | ---: |
| `model.InstructorAvailabilityTest` | 12 | 0 | 0 |
| `repository.InstructorAvailabilityRepositoryTest` | 3 | 0 | 0 |
| `service.InstructorAvailabilityServiceTest` | 11 | 0 | 0 |
| `service.InstructorLookupServiceTest` | 14 | 0 | 0 |
| Updated `config.InstructorConfigTest` | 1 | 0 | 1 |

All 40 tests for the added instructor model/repository/services passed in this run. Their standalone unit tests do not prove Spring wiring or compatibility with the existing schedule store. The expanded configuration test exposes a real wiring omission; it should be kept. Existing `InstructorScheduleServiceTest` (8 tests) and `InstructorSorterTest` (10 tests) also passed. These results do not justify deleting either availability family or rewriting the hand-written bubble sort.

There is no `NoUniqueBeanDefinitionException`, bean-definition override error or `Failed to load ApplicationContext` in `build.log`. No reported test fails for a changed Java constructor, an enrollment-contract bean, old Pending-student behavior, manual payment behavior or FIFO behavior. Other untested integration gaps may still exist; a passing individual suite is not proof of every browser flow.

## Proposed approval scope and verification

| Group | Proposed edit scope | What must stay unchanged |
| --- | --- | --- |
| A: 14 failures | Explicitly disable demo seed in the four affected web-test property methods and the one repository-test startup argument list, after a controlled isolation check. | Every assertion, test method, test count, security rule and production service. |
| B: 1 error | Add three bean methods/imports to `src/main/java/com/drivingschool/instructor/config/InstructorConfig.java` (owner IT26101133). | Existing bean methods, configuration-test assertions, working schedule service and booking dependencies. |
| Contracts | Keep all incoming instructor and enrollment contract files. | No adapter, replacement, deletion or payment/FIFO rewrite in this repair. |

After approval, use only temporary test storage and explicitly controlled seed settings, run the relevant five integration suites plus `InstructorConfigTest`, then run the complete Maven verification. Expected test count remains **663** because no tests are removed or added by these minimal proposals. Record the actual result, including any newly exposed assertion; do not assume that all 15 problems are fixed in advance. Prefer the project's Java 17 runtime for a comparable final verification and report any runtime difference.

### UNVERIFIED / limits

- Exact effective demo-seed value and its origin in the completed test JVM. The demo-data explanation is strongly supported but its external trigger is not captured.
- Whether the suggested additive beans plus isolated fixtures clear all failures on a rerun. No fixes or test reruns were performed.
- Live browser flows and a full application launch after adding the proposed instructor beans. This task is report/source analysis only.
- Any future use of the incoming lookup interfaces against the existing production schedule/payment stores. There are currently no main-source lookup consumers to verify.
- The user's statement that the merge is still uncommitted is consistent with the staged merge changes; no Git operation was used to complete or alter it.

**Stop point:** analysis delivered. Await approval before any source, configuration or test edits. No tests are proposed for deletion.
