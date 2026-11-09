# Java simplification and rubric protection

Date: 9 November 2026. Scope: member-approved removal of unused code, with no Git writes. This is a working-tree change, not a commit.

## Result

Production Java files: **163 → 152**. Test Java files: **78 → 73**. Eleven unused production files and five tests exclusively for those files were removed. Only two remaining production files changed: instructor/config/InstructorConfig.java (IT26101133) and vehicle/config/VehicleConfig.java (IT26101132). Their unused bean methods/imports were removed; member headers and working bean definitions remain.

The removed interfaces had no operational consumer. The instructor interval implementation was connected only to its unused lookup/service beans, not the working scheduling controllers. EnrollmentLookup had no implementation. VehicleLookup was not used by the booking conflict checker. Searching all src files after removal found no references to any removed type.

## Removed files

- `src/test/java/com/drivingschool/vehicle/service/VehicleLookupServiceTest.java`
- `src/test/java/com/drivingschool/instructor/model/InstructorAvailabilityTest.java`
- `src/test/java/com/drivingschool/instructor/repository/InstructorAvailabilityRepositoryTest.java`
- `src/test/java/com/drivingschool/instructor/service/InstructorAvailabilityServiceTest.java`
- `src/test/java/com/drivingschool/instructor/service/InstructorLookupServiceTest.java`
- `src/main/java/com/drivingschool/vehicle/api/VehicleBookingInfo.java`
- `src/main/java/com/drivingschool/vehicle/api/VehicleLookup.java`
- `src/main/java/com/drivingschool/vehicle/service/VehicleLookupService.java`
- `src/main/java/com/drivingschool/learningpackage/api/EnrollmentLookup.java`
- `src/main/java/com/drivingschool/learningpackage/api/EnrollmentSummary.java`
- `src/main/java/com/drivingschool/instructor/api/InstructorBookingInfo.java`
- `src/main/java/com/drivingschool/instructor/api/InstructorLookup.java`
- `src/main/java/com/drivingschool/instructor/model/InstructorAvailability.java`
- `src/main/java/com/drivingschool/instructor/repository/InstructorAvailabilityRepository.java`
- `src/main/java/com/drivingschool/instructor/service/InstructorAvailabilityService.java`
- `src/main/java/com/drivingschool/instructor/service/InstructorLookupService.java`

Ownership: instructor files IT26101133; vehicle files IT26101132; learningpackage files IT26101136. The removed instructor interval service's locking and stream-based lookup were not replaced: no working feature used them. This reduces concepts to explain without introducing a new abstraction or rewriting working code.

## What remains for the assessment

The supplied screenshot requires at least **two** of Create, Read, Update and Delete and gives OOP 40 marks. It does not award marks for having fewer files. Existing member CRUD and UI coverage is preserved, including the booking details third UI. This cleanup does not guarantee a mark: each member still needs to explain their code, diagrams and contribution.

| Member | Retained working evidence |
|---|---|
| IT26101135 | Auth/student CRUD and pages; private User/Student fields and validation; User → Student/Admin/InstructorAccount inheritance; StudentLookup implemented by StudentLookupService; session guards and custom exceptions. |
| IT26101133 | Instructor CRUD and pages; InstructorRepository inherits FileRepository; InstructorCatalog and InstructorScheduleOperations implementations; InstructorOrder with experience/name/rating implementations called through the interface; manual bubble sort. |
| IT26101134 | Booking request/read/reschedule/cancel and details UI; BookingRepository inherits FileRepository; booking operation interfaces and implementations; handwritten linked-node FIFO queue with private head/tail state; existing status validation. |
| IT26101132 | Vehicle CRUD and list/create/edit pages; VehicleRepository inherits FileRepository and implements VehicleStore; VehicleService implements VehicleOperations; private validated Vehicle fields; synthetic sample data and counters. |
| IT26101136 | Package and enrollment CRUD/pages; LearningRecord implemented by LearningPackage and Enrollment; repositories inherit FileRepository; EnrollmentService implements student/admin operation interfaces; read-only presentation helper stays. |
| IT26101137 | Review CRUD/moderation and report pages; ReviewRepository inherits FileRepository and implements ReviewStore; ReviewService implements ReviewOperations/ReviewModeration; validated private review fields. |

No controller, route, JSP, form field, storage format, payment rule or booking transition changed. Security, exception handling, file persistence, working interfaces and model inheritance remain. All remaining src files except the two configs and their two tests match their pre-cleanup SHA-256 digests.

### Mandatory algorithms

- `instructor/service/InstructorSorter.java`: manual outer passes call the manual adjacent-comparison loop in swapNeighbours. InstructorExperienceOrder compares years of experience. No library sort replaces it. The public instructor listing applies experience ordering and shows active instructors. Active does not mean free for a particular time slot; slot conflict checks remain separate.
- `booking/util/BookingRequestQueue.java`: manual Node/head/tail FIFO with enqueue/dequeue/position operations; BookingService keeps its existing queue processing and assignment guards. No java.util.Queue or LinkedList replacement was introduced.
- Algorithm implementations and existing algorithm/booking tests are byte-for-byte unchanged in this task. Runtime sample order is I002 (8 years), I004 (5), I005 (5), I001 (3), I003 (1).

## Why the other files remain

A reference search is not enough to prove a framework class is unused. Spring discovers annotated controllers/services/configurations, and servlet deployment uses ServletInitializer. These stay. Small interfaces used by real features are useful abstraction and polymorphism evidence; merging them into giant classes would make the viva harder and increase regression risk. This is the verified unused-code reduction, not a claim that 152 is the theoretical minimum file count.

Working availability uses AvailabilityRepository (availability.txt), InstructorLeaveRepository and InstructorScheduleService. It does not use the removed absolute-interval repository. Existing instructor-availability.txt records/counters, including the empty sample files, were deliberately retained untouched for compatibility. No live records were read, migrated or deleted for this cleanup.

## Tests changed deliberately

| Test | Change and reason |
|---|---|
| InstructorConfigTest.configRegistersSingletonBeansAndPersistsInstructor | Removed assertions/imports/mock setup for obsolete beans. Still checks live repository/service singleton wiring and creates/reloads an instructor from a temporary file. |
| VehicleConfigTest.shouldRegisterSingletonBeansAndPersistVehicle | Removed lookup adapter assertions/imports only. Still checks live repository/service wiring and creates/reloads every vehicle field. |
| InstructorAvailabilityTest | Removed 12 tests exclusively for the deleted unused interval model. |
| InstructorAvailabilityRepositoryTest | Removed 3 tests exclusively for the deleted interval repository. |
| InstructorAvailabilityServiceTest | Removed 11 tests exclusively for the deleted interval service. |
| InstructorLookupServiceTest | Removed 14 tests exclusively for the deleted lookup. |
| VehicleLookupServiceTest | Removed 6 tests exclusively for the deleted lookup. |

No new behavior was added and no retained feature test was deleted or disabled. The test total changes from 692 to 646 exactly because 46 obsolete tests were removed.

## Verification

Command: `.\mvnw.cmd clean verify`, Java 17.

```text
[INFO] Tests run: 646, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  01:21 min
[INFO] Finished at: 2026-11-09T20:16:06+05:30
```

Build log: `%TEMP%/drivespark-java-simplification-verify.log`. No own demo app was running at build time. The user's pre-existing port-8080 app was not terminated or used for checks.

After packaging, started the new WAR on port 18080 in a fresh shell against a new TEMP copy of sample-data. Run-only APP_DATA_DIRECTORY, demo/admin seed flags (false) and attendance flag (true) were cleared in finally; the owned app was stopped. HTTP verification: **45 PASS, 0 FAIL**. Evidence: `%TEMP%/java-cleanup-http-results.json`, `%TEMP%/java-cleanup-http.log`, `%TEMP%/java-cleanup-app.log`.

Checks cover all 16 sample logins (including refused Pending/Suspended accounts), 20 public/student/instructor/admin pages and CSV, manual experience sort order, null validity, expired/used-up/awaiting receipts, captured revenue, and ID allocation for temporary registration/contact records. All 24 original sample record/counter SHA-256 digests remain unchanged. Browser appearance is **UNVERIFIED** in this cleanup; no view or CSS changed. No full interactive journey is claimed beyond these HTTP checks and retained integration tests.

## Documentation and risk

Updated README, VIVA_QA, VIVA_GUIDE, current Mermaid/PlantUML diagrams and sample-data README. CONTRACTS, RUBRIC_AUDIT, BUSINESS_LOGIC_REVIEW and MERGE_ANALYSIS now explicitly label the removed proposed APIs as historical. Earlier dated reports/build totals remain historical evidence.

Removal risk: **Low for the current app**, backed by dependency checks, context startup, 646 tests and 45 HTTP checks. **Medium for an unmerged external branch** that still imports a deleted API: those members must reconcile with the working service interfaces before merging. No Git action was taken here. Further merging of active classes would carry more risk and could reduce the clarity of OOP evidence; it was not performed.
