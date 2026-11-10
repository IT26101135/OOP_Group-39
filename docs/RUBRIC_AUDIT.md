# SE1020 rubric audit — DriveSpark Academy

> 2026-11-09 cleanup update: the unused instructor interval/lookup family, vehicle lookup family and enrollment lookup/summary were removed with member approval. Earlier descriptions of those contracts are historical, not current APIs. See [Java simplification record](JAVA_SIMPLIFICATION.md) for retained OOP evidence and verification.

**Approved cleanup follow-up:** unused pagination.jspf, skeleton.jspf, search-bar.jspf and breadcrumbs.jspf were removed, along with obsolete build.log and 44 redundant populated-src placeholders. data/.gitkeep, the empty student/repository placeholder and every Java file were retained. The unused-scaffold recommendation in this dated audit has therefore been superseded; other Java cleanup suggestions were not implemented.

**Approved follow-up, 9 November2026:** IT26101134 now has a third standalone booking UI, `booking/details.jsp`, at GET `/bookings/{bookingId}/details`. Student ownership/admin guards and eight tests were added; clean verify passed692 tests, and11 isolated-demo HTTP checks passed. The original audit below remains a dated pre-addition assessment; its two-UI shortfall is now resolved in the working tree. See docs/VIVA_QA.md for exact files and evidence. Other audit recommendations remain unimplemented.

Audit date: 9 November 2026. Analysis only; only this document was written. No source edits, app launch, test run, dependency installation or Git mutation was performed. The supplied requirements and mark allocation are the rubric for this audit; independent inspection of the original institutional brief is **UNVERIFIED** because the original brief was not supplied.

Evidence is the current working tree, Java/JSP source, existing documentation and local read-only Git history. Existing `target/surefire-reports/TEST-*.xml` contains **78 reports / 684 tests / 0 failures / 0 errors / 0 skipped**. That is previously generated evidence, not a new run in this audit. The user's 663-test baseline predates the latest additions. Tests alone do not prove individual understanding, submission readiness or every UI requirement. Source member headers show assigned ownership, not independently verified authorship.

## 1. Rubric readiness and priorities

| Area / marks | Source-backed assessment | Main submission risk | Smallest action, risk and owner |
|---|---|---|---|
| OOP / 40 | All six modules have encapsulation, an interface/abstract parent and overriding; some inheritance is repository inheritance rather than entity inheritance | Students may repeat annotation labels without explaining dispatch, validation or file handling | Rehearse one real method chain and the examples in section 3 per student. Low; each member alone |
| CRUD / 10 | Each member's business entity has all four operation types, with file persistence | Shared controllers must not be misrepresented as independently authored code | Use section 2 to distinguish shared route handling from owned service/model work. Low; each member |
| File read/write / 10 | JSON Lines text files through java.nio Files; no database persistence found | Loader source exists, but sample record files are ignored/untracked | Add a sanitized `sample-data/` submission bundle after team approval. Low code risk, Medium data-disclosure risk; IT26101132 +135 review |
| Completeness / 10 | All three roles and both mandatory algorithms exist | Booking owner has only two full JSPs; stale report describes old payment/seed behavior | One small owned read-only booking detail page and updated report. Medium; IT26101134; report IT26101137 with135 |
| Individual/GitHub history / 5 | All six institutional identities appear in local HEAD history | Integration author dominates counts; latest changes and diagram files are not in HEAD | Include truthful history appendix and explain shared integration. Low;135 assembles with137; user performs Git work |
| Presentation / 10 | UI and sample login documentation exist; prior browser evidence is recorded | Six students' live explanations and final presentation delivery are UNVERIFIED | Each rehearse their own backend and one failure path. Low; all six |
| Class diagrams / 5 | Eleven Mermaid figures plus PlantUML copies exist locally; key types/method names match inspected source | One missing package dependency, stale statements and untracked submission artifacts | Correct document only and submit diagrams; Low;135, with each member verifying their section |
| Teamwork / 5 | Fixed feature ownership, shared contracts and merges are visible | Actual task sharing/individual understanding cannot be inferred from commits alone | Use genuine task records and explain cross-module handoffs. Low; all six |
| Coding standards / 5 | Feature packages and consistent model/service/repository names | Overlong methods/JSP lines, repeated generic comments and mixed formatting | Small formatting/comment changes on owned files; Low, no broad rewrite |

This is a risk assessment, not a predicted mark or a guarantee of acceptance. First prioritize the third booking UI, committed sample records, a final report with history, and viva understanding. Do not rewrite working storage/security or replace the mandatory algorithms merely to reduce the line count.

Risk terms used throughout: **Low** = documentation/formatting or a local read-only addition; **Medium** = a localized behavior-preserving refactor needing regression tests; **High** = cross-module, storage, authentication or concurrency changes. No proposed change is authorized by this audit.

## 2. Individual CRUD and UI evidence

All Java paths below are relative to `src/main/java/com/drivingschool/`; JSP paths to `src/main/webapp/WEB-INF/views/`. A repository's CRUD method can be an override delegating to `common/repository/FileRepository`, not a second file implementation. Protected deletion is still DELETE: the successful path exists, while linked history is deliberately refused. A service's READ can call `findByIdStrict` indirectly through the authenticated session. Every member must explain that call rather than claim every read is `readAllStrict`.

`AdminManagementController` belongs to IT26101135/shared integration, while its instructor operations call IT26101133 services and package operations call IT26101136 services. The tables attribute the business module to its fixed owner; they do not transfer ownership of the shared controller. Existing routes and form fields were not changed.

### IT26101135

| Entity | CRUD | Controller method and URL | Service method(s) | Repository method | JSP |
|---|---|---|---|---|---|
| Student account/profile | CREATE | `AuthController.register` — `POST /register` | `AuthService.registerStudent` | `UserRepository.save` | `auth/register.jsp` |
| Student account/profile | READ | `StudentProfileController.showProfile` — `GET /student/profile` | `StudentProfileService.getOwnProfile` | `UserRepository.findByIdStrict` via current-session reload | `student/profile.jsp` |
| Student account/profile | UPDATE | `StudentProfileController.updateProfile` — `POST /student/profile/edit` | `StudentProfileService.updateOwnProfile` | `UserRepository.update` | `student/profile-edit.jsp` |
| Student account/profile | DELETE | `StudentAccountController.delete` — `POST /student/account/delete` | `StudentAccountService.deleteOwnAccount` | `UserRepository.delete` | `student/profile.jsp` |

### IT26101133

| Entity | CRUD | Controller method and URL | Service method(s) | Repository method | JSP |
|---|---|---|---|---|---|
| Instructor profile | CREATE | `AdminManagementController.createInstructor` — `POST /admin/instructors` | `InstructorAccountService.create` → `InstructorService.createInstructor` | `InstructorRepository.save` | `admin/instructors.jsp` |
| Instructor profile | READ | `AdminManagementController.instructors` — `GET /admin/instructors` | `InstructorService.getAllInstructors` | `InstructorRepository.readAll` | `admin/instructors.jsp` |
| Instructor profile | UPDATE | `AdminManagementController.updateInstructor` — `POST /admin/instructors/update` | `InstructorAccountService.update` → `InstructorService.updateInstructor` | `InstructorRepository.update` | `admin/instructors.jsp` |
| Instructor profile | DELETE | `AdminManagementController.deleteInstructor` — `POST /admin/instructors/delete` | `InstructorAccountService.delete` → `InstructorService.deleteInstructor` | `InstructorRepository.delete` | `admin/instructors.jsp` |

### IT26101134

| Entity | CRUD | Controller method and URL | Service method(s) | Repository method | JSP |
|---|---|---|---|---|---|
| Booking | CREATE | `BookingController.request` — `POST /bookings` | `BookingService.requestLesson` | `BookingRepository.save` | `booking/list.jsp` |
| Booking | READ | `BookingController.list` — `GET /bookings` | `BookingService.getOwnBookings` | `BookingRepository.readAllStrict` | `booking/list.jsp` |
| Booking | UPDATE | `BookingController.cancel` — `POST /bookings/cancel` | `BookingService.cancelOwnBooking` | `BookingRepository.update` | `booking/list.jsp` |
| Booking | DELETE | `BookingController.delete` — `POST /bookings/delete` | `BookingService.deleteCancelledBooking` | `BookingRepository.delete` | `booking/list.jsp` |

### IT26101132

| Entity | CRUD | Controller method and URL | Service method(s) | Repository method | JSP |
|---|---|---|---|---|---|
| Vehicle | CREATE | `VehicleController.createVehicle` — `POST /admin/vehicles/create` | `VehicleService.createVehicle` | `VehicleRepository.save` | `vehicle/create.jsp` |
| Vehicle | READ | `VehicleController.listVehicles` — `GET /admin/vehicles` | `VehicleService.getAllVehicles` | `VehicleRepository.readAll` | `vehicle/list.jsp` |
| Vehicle | UPDATE | `VehicleController.updateVehicle` — `POST /admin/vehicles/{id}/edit` | `VehicleService.updateVehicle` | `VehicleRepository.update` | `vehicle/edit.jsp` |
| Vehicle | DELETE | `VehicleController.deleteVehicle` — `POST /admin/vehicles/{id}/delete` | `VehicleService.deleteVehicle` | `VehicleRepository.delete` | `vehicle/list.jsp` |

### IT26101136

| Entity | CRUD | Controller method and URL | Service method(s) | Repository method | JSP |
|---|---|---|---|---|---|
| Package | CREATE | `AdminManagementController.createPackage` — `POST /admin/packages` | `LearningPackageService.createPackage` | `LearningPackageRepository.save` | `admin/packages.jsp` |
| Package | READ | `AdminManagementController.packages` — `GET /admin/packages` | `LearningPackageService.listPackages` | `LearningPackageRepository.readAllStrict` | `admin/packages.jsp` |
| Package | UPDATE | `AdminManagementController.updatePackage` — `POST /admin/packages/update` | `LearningPackageService.updatePackage` | `LearningPackageRepository.update` | `admin/packages.jsp` |
| Package | DELETE | `AdminManagementController.deletePackage` — `POST /admin/packages/delete` | `LearningPackageService.deletePackage` | `LearningPackageRepository.delete` | `admin/packages.jsp` |

Additional owned enrollment CRUD is independently present: `EnrollmentController.enroll` POST `/enrollments` → `EnrollmentService.enroll` → `EnrollmentRepository.save`; `list` GET `/enrollments` now uses `EnrollmentPresentationService.addStudent` → `EnrollmentRepository.readAllStrict` (the older CRUD map still names `ownEnrollments`); `update` POST `/enrollments/update` → `updateLearningGoal` → `update`; `delete` POST `/enrollments/delete` → `deleteOwnEnrollment` → `delete`. All use `student/enrollments.jsp`; creation redirects to `student/payment-instructions.jsp`. Only unused unpaid unlinked enrollment deletion is allowed.

### IT26101137

| Entity | CRUD | Controller method and URL | Service method(s) | Repository method | JSP |
|---|---|---|---|---|---|
| Review | CREATE | `ReviewController.create` — `POST /reviews` | `ReviewService.createReview` | `ReviewRepository.save` | `review/list.jsp` |
| Review | READ | `ReviewController.list` — `GET /reviews` | `ReviewService.getVisibleReviews` | `ReviewRepository.readAllStrict` | `review/list.jsp` |
| Review | UPDATE | `ReviewController.update` — `POST /reviews/update` | `ReviewService.updateOwnReview` | `ReviewRepository.update` | `review/list.jsp` |
| Review | DELETE | `ReviewController.delete` — `POST /reviews/delete` | `ReviewService.deleteOwnReview` | `ReviewRepository.delete` | `review/list.jsp` |

### UI counts: standalone pages, not repeated controls

| Fixed owner and role | Three or more owned JSP UIs | CRUD verdict | UI verdict / smallest honest addition |
|---|---|---|---|
| IT26101135 — Authentication, Students, shared foundation, integration | `auth/register.jsp`, `auth/login.jsp`, `student/profile.jsp`, `student/profile-edit.jsp`, `student/password.jsp`, `student/progress.jsp`, `admin/students.jsp` (plus shared pages) | Meets: C/R/U/D | Meets, at least 7 listed. No addition needed. Low: rehearse role guards and storage |
| IT26101133 — Instructors and bubble sort | `public/instructors.jsp`, `admin/instructors.jsp`, `instructor/availability.jsp`, `instructor/lesson-dashboard.jsp` | Meets: C/R/U/D | Meets: 4. No additional page needed |
| IT26101134 — Bookings, FIFO queue and scheduling | `booking/list.jsp`, `admin/bookings.jsp` | Meets: C/R/U/D | **Conservative shortfall: 2 standalone JSP UIs.** `booking/request-card.jspf` and `admin/booking-card.jspf` are includes inside those two pages, not independently reachable UIs. Forms/tabs on one page might count under a permissive marker, but acceptance is UNVERIFIED. Smallest honest addition: a read-only booking detail JSP with ownership/admin checks, reached from an existing card. Medium;134 can implement locally,135 reviews access rules |
| IT26101132 — Vehicles, sample data, test checklist | `vehicle/list.jsp`, `vehicle/create.jsp`, `vehicle/edit.jsp` | Meets: C/R/U/D | Meets: 3. `/admin/vehicles` renders `vehicle/list.jsp`; do not invent `admin/vehicles.jsp` as another page |
| IT26101136 — Learning packages, shared UI | `learningpackage/list.jsp`, `learningpackage/detail.jsp`, `admin/packages.jsp`, `student/enrollments.jsp`, `student/payment-instructions.jsp`, `admin/enrollments.jsp` | Meets: C/R/U/D on package; also enrollment | Meets: at least 6; shared fragments are extra, not needed for the minimum |
| IT26101137 — Reviews, moderation, report assembly | `review/list.jsp`, `public/reviews.jsp`, `admin/reviews.jsp`, `admin/reports.jsp` | Meets: C/R/U/D on review | Meets: 4. CSV is a useful output, not another JSP |

Source correction to the older docs/CRUD_MAP.md: instructor getAllInstructors and vehicle getAllVehicles call **readAll (lenient)**, not readAllStrict. The table above uses the current calls. Package/review lists use strict reads; profile read reloads by ID through the session service. Updating that older map is a Low-risk documentation task for135 with133/132 review, not an application change.

Additional instructor scheduling CRUD exists in `InstructorAvailabilityController.page/addBlock/updateBlock/deleteBlock` → `InstructorScheduleService.availability/addAvailability/updateAvailability/deleteAvailability` → `AvailabilityRepository.readAllStrict/save/update/delete`, rendered in `instructor/availability.jsp`. This strengthens133's evidence without changing roles.

The booking UI issue can also be raised with the marker using the real request/slot-selection/history interfaces; do not state there are three independent pages unless a third exists or the marker explicitly accepts multiple interfaces within one JSP. Do not take a teammate's UI, change its header, or count a copied page as a contribution.

## 3. OOP evidence for each member

Information hiding means callers cannot directly alter internal fields or storage mechanics. Private fields alone are not the full story: validated state changes, safe copies and controlled DTOs make the boundary meaningful. Exception handling is listed because it matters to the viva, although it is not itself one of the usual four OOP pillars.

| Member | Encapsulation / information hiding | Inheritance | Abstraction | Polymorphism / exact dispatch | Custom exceptions and method evidence |
|---|---|---|---|---|---|
|135|`auth/model/User`: private account fields; `setEmail` validates; `student/model/Student.setPhone`, `setLessonsRemaining` validate. `StudentProfileService.toProfile` returns display data without password hash|`Student`, `Admin`, `InstructorAccount` extend abstract `User`; `UserRepository` extends `FileRepository<User>`|`User.getRole/getDashboardPath` are abstract; `StudentProfiles` implemented by `StudentProfileService`; controller receives `StudentProfiles`|`Student.getRole/getDashboardPath`, `Admin.getRole/getDashboardPath`, `InstructorAccount.getRole/getDashboardPath` override the same parent methods. `AuthController.login` acts on a `User` returned by the session service|`AuthService.registerStudent/findUniqueEmail`: ValidationException/DuplicateRecordException; `StudentProfileService.updateOwnProfile`: duplicate input errors; `FileRepository.readRecords/atomicWrite`: StorageException; `GlobalExceptionHandler.handleValidation/handleDuplicate/handleStorage` renders responses|
|133|`instructor/model/Instructor`: private name/experience; `setName`, `setYearsOfExperience` validate; `getVehicleTypes` returns a copy|`InstructorRepository extends FileRepository<Instructor>`; `InstructorAccount extends User` is the linked login identity, **Instructor profile does not extend User**|`InstructorOrder.compare` and `InstructorCatalog`; `InstructorService implements InstructorCatalog`|`InstructorExperienceOrder.compare`, `InstructorNameOrder.compare`, `InstructorRatingOrder.compare` through `InstructorOrder` in `InstructorSorter.swapNeighbours`|`InstructorService.ensureEmailAvailable` throws DuplicateRecordException; `getInstructorById` throws RecordNotFoundException; model setters and `InstructorScheduleService.validateLeave` throw ValidationException|
|134|`booking/model/Booking`: private status/resources; `confirm`, `cancel`, `complete`, `markNoShow` validate transitions; `BookingRequestQueue.Node`, head/tail are private|`BookingRepository extends FileRepository<Booking>`|`StudentBookingOperations` and `AdminBookingOperations` implemented by `BookingService`; `BookingOrder` interface|`PendingRequestOrder.order` and `LessonTimeOrder.order` implement `BookingOrder`; `BookingService.getAllBookings` uses `byRequestTime` and `byLessonTime` through that type|`BookingService.assign` rejects out-of-order IDs using ValidationException; `findBooking` throws RecordNotFoundException; `Booking.complete` throws ValidationException before an early completion; global handler provides readable response|
|132|`vehicle/model/Vehicle`: private registration/status/date fields; constructor and `normalizeRegistrationNumber`, `setLastServiceDate`, `setNextServiceDate` protect valid state; controller form binders prevent ID tampering|`VehicleRepository extends FileRepository<Vehicle>`|`VehicleStore` implemented by `VehicleRepository`; `VehicleOperations` implemented by `VehicleService`; service/controller receive those contracts|`VehicleRepository.readAllStrict/save/update/delete` override parent methods. `VehicleService` calls via `VehicleStore`; inherited file implementation handles Vehicle as Identifiable. `DemoDataSeedRunner.run` overrides CommandLineRunner.run|`VehicleService.createVehicle/updateVehicle` detect duplicate plates; `deleteVehicle` throws ValidationException for linked history; `requireVehicle` throws RecordNotFoundException; storage exceptions propagate|
|136|`LearningPackage`: private price/count/validity; `setPrice` (line140), `setLessonCount` (171) validate. `Enrollment.setStatus` (108) restricts receipt states; presentation DTO fields are final|`LearningPackageRepository extends FileRepository<LearningPackage>`; `EnrollmentRepository extends FileRepository<Enrollment>`. `LearningRecord` **extends an interface**, Identifiable; this is not domain-class inheritance|`LearningRecord`, `LearningPackageCatalog`, `EnrollmentOperations`, `EnrollmentAdministration` implemented by their models/services|`LearningPackage.getDisplayName` (229) and `Enrollment.getDisplayName` (170) implement the same LearningRecord method; pages call the heading property. Repository overrides provide a stronger Java base-type dispatch example; do not claim a heterogeneous List<LearningRecord> is used if it is not|`EnrollmentService.ensureCanEnroll` throws DuplicateRecordException; `requireOwnEnrollment` validates ID and throws RecordNotFoundException; `LearningPackageService.deletePackage` throws ValidationException for references|
|137|`review/model/Review`: private rating/comment/status; `updateContent` (171), `updateStatus` (202) validate before modifying; approved public rows are separated from private moderation state|`ReviewRepository extends FileRepository<Review>`; Review itself implements Identifiable and does not extend another domain entity|`ReviewStore` implemented by `ReviewRepository`; `ReviewOperations`/`ReviewModeration` by `ReviewService`; `ReportCatalog` by `AdminReportService`|`ReviewRepository.readAllStrict/save/update/delete` override FileRepository methods and are called through ReviewStore. `AdminReportService.all` overrides ReportCatalog.all, used by `AdminReportController.show/export`|`ReviewService.createReview/createReviewRecord/ensureNoDuplicate` rejects uncompleted/duplicate feedback; `requireReview` reports missing records; model update methods throw ValidationException; shared handler translates errors|

**Hard-to-see pillars:**132,134,136,137 rely substantially on shared repository inheritance; do not invent inheritance such as Vehicle extends User or Enrollment extends LearningPackage. The smallest addition is a documentation/live-debug demonstration of the actual `extends FileRepository<T>` and override call, **Low risk**, each member alone. No new inheritance hierarchy is required by the supplied brief.133 should distinguish InstructorAccount from Instructor;136 should distinguish interface extension from class inheritance and explain that EL property dispatch is not proof of a Java list typed LearningRecord. Low-risk rehearsal, not source changes.

Each student should be able to say what `T extends Identifiable` means, show an override that calls `super`, explain interface versus implementation, and follow one invalid input into a custom exception. If they cannot explain an example, the pillar is present in code but individual viva readiness is **UNVERIFIED**. OOP labels in comments are prompts, not proof on their own.

## 4. Mandatory hand-written algorithms

### 4.1 Queue — verified present and operationally connected

`booking/util/BookingRequestQueue.java` is a hand-written linked queue. It has private Node(Booking,next), head, tail and size. It imports ArrayList/List only for construction/display; no java.util.Queue, LinkedList or ArrayDeque implements its queue behavior.

1. `BookingService.requestLesson` saves a Pending record in bookings.txt. It does not maintain a hidden permanent Java queue across requests.
2. `new BookingRequestQueue(savedBookings)` filters **Pending AND instructorId == null**, restores submission order using a hand-written stable insertion sort by createdAt, then calls `enqueue` for each ordered record. Equal timestamps keep file order.
3. `enqueue`: create Node, attach it to tail.next (or head if empty), update tail and size. O(1).
4. `dequeue`: return head.booking, move head to head.next, clear tail when empty, decrement size. O(1). Empty returns null.
5. `BookingService.processNext` checks admin, holds the shared booking lock, rebuilds the queue and calls dequeue. It then calls `assign`, which rebuilds another queue and verifies the ID is still peek() before saving assignment/rejection. Dequeue changes only the temporary queue; saving booking status/assignment makes progress persistent.
6. Assignment keeps Pending but adds an instructor, so the next rebuild excludes it. Instructor acceptance is a separate step. Cancellation/rejection also removes a record from the unassigned line on the next rebuild. Reschedule creates a new submission at the back.

UI: `AdminBookingController.list` supplies pendingQueue/queuePositions/queueSize to `admin/bookings.jsp`; its queue panel posts `/admin/bookings/process-next`. `admin/booking-card.jspf` shows FIFO position. `BookingController.addQueuePositions` and `booking/request-card.jspf` show students “FIFO position …” or “Assigned - waiting for instructor approval.” No library queue is being passed off as the required implementation.

Complexity: queue primitives enqueue/dequeue/peek/size/isEmpty O(1); positionOf/toList O(n); reconstruction O(n²) worst case due to insertion sort, O(n) best for ordered input plus filtering; O(n) space. Full service processing includes file reads and reconstruction, so it is **not O(1)** merely because dequeue is.

Five-request viva example: file order [R3 created09:03, R1 09:01, R5 09:05, R2 09:02, R4 09:04] becomes head R1 → R2 → R3 → R4 → R5 tail. Enqueue R6 attaches at the back. Dequeue returns R1 and leaves R2 at head. If R2 is assigned, it remains Pending awaiting instructor but disappears from the reconstructed unassigned queue. Cancellation of R3 skips it on reconstruction; it does not reorder R4/R5. Explain that R1/R2 here are teaching labels, not valid stored B001-format IDs.

Recommendation: **keep and rehearse, Low risk**. Do not remove, replace with java.util.Queue, or simplify away persistence/FIFO checks.

### 4.2 Bubble sort — verified present, experience-based and active-only in the public UI

- `instructor/service/InstructorSorter.sort` (line28) runs the outer pass loop; `swapNeighbours` (43) runs the inner adjacent-comparison loop. Nested execution is split into two methods, not absent.
- It calls `InstructorOrder.compare`, swaps neighboring elements with `sorted.set(index,next)` / `set(index+1,current)`, and breaks early if no swap occurred. Input is copied to a new ArrayList.
- `InstructorExperienceOrder.compare` (22) calls Integer.compare on **getYearsOfExperience**, not rating, age or name. Descending swaps when comparison<0; ascending when >0. Equal experience does not swap, so the sort is stable.
- `InstructorService.sortInstructors` (173) calls this hand-written sorter. `PublicInstructorController.list` defaults `sortBy=experience`, `direction=desc`, sorts all stored profiles, **then filters isActive**. Sorting then filtering preserves the sorted order of the active subset. Available here means active/publicly offered, **not free at an arbitrary lesson time**.
- User trigger: `/instructors`, select Experience in the Sort by menu, choose direction, click Sort. The default page also invokes experience descending. `public/instructors.jsp` uses form field names sortBy/direction. There is no Collections.sort/stream sorted in this actual public instructor ordering path.
- The unused `InstructorLookupService.isWorkingAvailable` does use stream.sorted, but sorts **availability intervals by start time**, not instructors by experience. It does not replace the mandatory bubble sort. Do not confuse these paths in the viva.

Worked five-instructor example, all Active, descending experience. IDs below are illustrative instructor names; parentheses are years:

| Stage | Order | What happened |
|---|---|---|
| Start | A(3), B(8), C(1), D(5), E(5) | Original order |
| Pass1 | B(8), A(3), D(5), E(5), C(1) | A/B swap; C moves right past D and E |
| Pass2 | B(8), D(5), E(5), A(3), C(1) | A moves right; tied D/E retain order |
| Pass3 | B(8), D(5), E(5), A(3), C(1) | No swaps; stop early |

If B is inactive, the public result is D(5), E(5), A(3), C(1), after filtering. Worst/average time O(n²), best O(n) with early termination, O(n) extra space for the copied list, O(1) extra swap work. A fully reversed five-item list can take 4 passes and 10 comparisons. Recommendation: **keep and rehearse, Low risk**; no algorithm rewrite needed.

## 5. File read/write and sample records

### Shared storage, in plain words

`common/repository/FileRepository<T extends Identifiable>` does the actual disk work. Subclasses select the record class and filename. A `.txt` file is **UTF-8 JSON Lines**: one complete JSON object per nonblank line, not a database table and not a single JSON array.

- READ: `readRecords` (277–336) opens `Files.newBufferedReader(..., UTF_8)` at282 and loops over `input.readLine()` at288. Jackson converts each line into a model. Strict reads fail when a row is damaged; lenient reads log/skip invalid rows. It does **not** literally call Files.readAllLines; BufferedReader plus java.nio.Files is equally genuine file reading.
- CREATE: `save` (166) strictly reads current records, checks duplicate ID, adds the validated record, then `writeRecords`.
- UPDATE: `update` (200) reads the list, locates the matching ID and replaces that list entry; it rewrites the file. It does not edit bytes in place.
- DELETE: `delete` (223) finds and removes the entry, then rewrites the remainder. Business services first refuse unsafe linked-history deletion.
- WRITE: `writeRecords` (401) serializes each record with a newline. `atomicWrite` (421) creates a temporary sibling file, uses `Files.writeString` at429 and `Files.move(... ATOMIC_MOVE, REPLACE_EXISTING)` to replace the target. Failed temporary writes are cleaned up; IOExceptions become StorageException. Do not claim this is a multi-file transaction or that it locks other JVMs.
- IDs: `nextId` reads a companion `<filename>.ids.txt` JSON counter, also considers existing IDs, reserves the next number and writes the counter. Keep companions with imported sample files. IDs are not arbitrary unlimited integers: validation uses letter + three digits, with finite ranges.
- Synchronization coordinates in-process operations. It is not a relational foreign-key engine, database or cross-process lock.

| Member | Repository/data files | What their service saves/loads |
|---|---|---|
|135|UserRepository → `users.txt`; ContactMessageRepository → `contact-messages.txt`; shared FileRepository handles every module|Registration/profile/account updates and deletion; polymorphic User/Student/Admin/InstructorAccount JSON; ContactService.submit saves contacts. Passwords are BCrypt hashes, not plaintext |
|133|InstructorRepository → `instructors.txt`; AvailabilityRepository → `availability.txt`; InstructorLeaveRepository → `instructor-leave.txt`; LessonRecordRepository → `lesson-records.txt`|Profiles, weekly working blocks, leave and lesson feedback. Separate InstructorAvailabilityRepository → `instructor-availability.txt` exists for the unused interval contract path; it is not the slot generator's weekly store |
|134|BookingRepository → `bookings.txt`|Pending requests, resource assignments, statuses and history; FIFO is reconstructed from this file |
|132|VehicleRepository → `vehicles.txt`; DemoDataSeedRunner writes cross-module fixture repositories|Vehicle details/status/dates and sanitized rehearsal fixtures; runner is not an alternative database |
|136|LearningPackageRepository → `packages.txt`; EnrollmentRepository → `enrollments.txt`|Catalog and receipt/status/validity data; EnrollmentUsageView is only presentation data and has no file |
|137|ReviewRepository → `reviews.txt`; AdminReportService reads enrollment/booking/review/instructor/vehicle files|Review content/moderation is persisted; report sections/CSV are generated, not a new stored report database |

Storage root is `app.data-directory` (default ./data). Every main `.txt` listed has a `.txt.ids.txt` companion; twelve repositories include the separate legacy interval/contact stores. No database dependency/connection is needed by these persistence paths. Names like items.txt in the brief are examples: real domain filenames such as vehicles.txt and users.txt fulfill the same file-storage requirement.

### Are sample files committed?

Verified with `git ls-files data data-demo sample-data samples` and `git check-ignore -v`: **only data/.gitkeep is tracked**. `/data/*` is ignored except .gitkeep; `/data-demo/` is ignored by .gitignore line20. Local data and data-demo contain JSON Lines records and ID companions, but those local files will not appear in a fresh clone just because they exist here. Do not publish their contents without a privacy review.

`sample/DemoDataSeedRunner.java` **is tracked**. It is an opt-in fixture generator, enabled by DRIVESPARK_DEMO_SEED_ENABLED with a clean isolated directory; README lists demo logins. Its presence helps repeatability but is not the same as submitting sample record files if the brief asks for them.

Smallest proposal: commit a separate, non-ignored `sample-data/` folder (creation is not part of this audit). Include:

1. `README.md` with intended demo directory, safe copy/import procedure, package/booking relationships, fixed or refreshable dates, and known fake account identities.
2. Sanitized `users.txt`, `instructors.txt`, `packages.txt`, `enrollments.txt`, `vehicles.txt`, `bookings.txt`, `availability.txt`, `instructor-leave.txt`, `lesson-records.txt`, `reviews.txt`, and empty `contact-messages.txt`; include an explicitly empty legacy instructor-availability.txt if required by the current loader.
3. Matching `.ids.txt` companions, or a tested documented initialization procedure compatible with FileRepository. Keep all linked IDs consistent.
4. Only invented .test identities, dummy contacts and hashes for **demo-only** passwords; no real passwords/hashes, personal records, reset tokens or session material. A publicly documented throwaway demo password is not a production credential. Review files before submission.
5. Examples of Awaiting payment/Paid/Cancelled receipts, Pending/assigned Pending/Confirmed/Completed/Cancelled/No-show/Rejected bookings, maintained vehicles and moderation states. Date-sensitive examples should be refreshed from the seed runner for the actual viva.

Risk **Low code / Medium privacy**, owner132 can prepare synthetic files alone;135 checks loading and authentication, each owner checks relationships. User performs all Git operations. Do not remove sample data or the seed runner. No sample files were created or altered during this audit.


## 6. First-year explainability: local simplifications, not a rewrite

The existence of a stream, Optional, generic type or lock is not a rubric violation. “Too advanced” means a likely viva explanation burden. The alternatives below are proposals only; **their equivalence has not been executed in this audit**. Preserve null checks, short-circuiting, exception type/message, duplicate checks, iteration order, stable ties, timing, authorization and locks. A loop replacing an Optional lookup should use the same repository read, not add a second read of the file.

### 6.1 Concrete examples by owner

| Owner | File and physical lines | Explanation burden | Smallest behavior-preserving alternative | Risk / safe independent scope |
|---|---|---|---|---|
|135|`auth/service/AdminSeedService.java:53–74`, seedIfNeeded | Supplier lambda around stream anyMatch for admin/email existence | Inside the same AccountWriteCoordinator callback, use a boolean and for-each loop with break; keep the existing order (admin check before email validation) | Medium: authentication setup;135 alone with seed tests. Do not remove the security class or lock |
|135|`student/service/StudentAccountService.java:55–60`, deleteOwnAccount | Three anyMatch expressions hide three independent file/history checks | Read each list once, use three loops/flags, then the same refusal condition. Do not return early after checking only bookings: the original code reads reviews/enrollments too | Medium;135 with deletion/history tests |
|133|`instructor/service/InstructorAccountService.java:103–106`, update; `InstructorLessonService.java:65–66`, saveNotes | Optional.orElseThrow lambda | Store the same Optional, if empty throw the identical named exception, otherwise get; do not replace with a second repository lookup | Low;133 only, same test assertions |
|133|`instructor/service/InstructorLookupService.java:34–38,60–64`, findById/isWorkingAvailable (currently unconnected) | Optional.map plus stream filter/comparing/toList | Explicit Optional branch; for-each filter and stable insertion sort of intervals by start time. Keep exact interval coverage logic. This is **not** the required instructor bubble sort | Medium; keep/document first;133 only with interval tests; do not remove the OOP contract |
|134|`booking/service/BookingConflictChecker.java:90–116`, conflictReason | Several orElseThrow lambdas in a long validation chain | Explicit Optional branches, each keeping its existing error; keep the validation sequence so the first displayed conflict stays identical | Medium;134, coordinate133/132/136 for cross-module validation |
|134|`booking/service/BookingService.java:119,159,221,406–446` | Shared lock plus request/reschedule/assignment sequences | Already uses loops; no equivalent “plain loop” replaces synchronization. Keep the lock and extract small pure counting/validation helpers **inside** it if needed. Explain one cashier desk | High to change lock/ordering, Medium for helper extraction; joint135 review |
|132|`sample/DemoDataSeedRunner.java:197–205`, seedBookings | Multiple variables in one declaration, modular arithmetic and modern switch expression | Separate student/instructor/vehicle declarations; use explicit if/else for the identical numbered status sets; keep the same 15-record relationships and idempotency checks | Medium;132 alone with seed/report arithmetic tests |
|132|`vehicle/service/VehicleService.java:175–187,209`, deleteVehicle/requireVehicle | synchronized method, Optional helper and linked-record refusal | Keep the synchronized method; its deletion loop is already beginner-readable. Explain Optional as “found/not found”; retain helper rather than duplicate file reading | Low to document, High to remove synchronization;132 +135 for locking changes |
|136|`learningpackage/service/LearningPackageService.java:226–241`, deletePackage | Two stream.anyMatch link checks nested under PACKAGE_WRITE_LOCK | Explicit loops under the same lock; throw the same ValidationException at the first matching legacy booking/enrollment; preserve optional-null repository behavior | Medium;136 with linked-delete tests |
|136|`learningpackage/service/EnrollmentService.java:71,141,160,242,256` | Supplier lambdas coordinate multiple record changes | Keep AccountWriteCoordinator. A mechanically equivalent full rewrite would synchronize on AccountWriteCoordinator.class around the same body, but this duplicates an important shared convention; prefer documenting the callback | High for rewrite, Low for a short explanation;136 must coordinate135 |
|136|`learningpackage/service/EnrollmentPresentationService.java:51–94,128–181` | Maps, many status branches, latest-Paid fallback and long constructor calls | Keep ID maps to avoid per-row file reads. Extract named status/next-link helpers with the same precedence and retain historical balance rules; use expanded if statements instead of combined boolean expressions | Medium;136 with all12 presentation tests and role-page checks |
|137|`report/controller/AdminReportController.java:69`, csv | Chained escaping and string concatenation hides CSV quoting | Character loop: append opening quote; for each char append it and append an extra quote when it is a double quote; append closing quote. Keep spreadsheet-formula protection applied **before** this loop | Medium;137 with quote/comma/newline/formula examples |
|137|`review/service/ReviewService.java:91–92,106,183,196,207,222,237` | Optional and review-wide locking | Expand Optional lookup as below; keep synchronized REVIEW_WRITE_LOCK for duplicate prevention/moderation and recalculate approved rating after the same writes | Medium for Optional wording, High for lock removal;137 +133 review rating behavior |
|Shared135|`common/repository/FileRepository.java:35–47,277–336,421–451`; `auth/util/AccountWriteCoordinator.java:36–37` | Generic T, Jackson typed readers, try-with-resources, atomic moves and synchronized Supplier | Keep and explain. Replacing with six copied file classes is not a small identical simplification; manually closing resources can weaken exception safety. No rewrite recommended | High for source rewrite; Low for viva diagram/comments,135 only |

Examples of equivalent beginner-style building blocks (illustrative code, not applied patches):

```java
// Replacement for AdminSeedService's admin anyMatch, within the existing locked callback.
boolean adminExists = false;
for (User user : users) {
    if (user.getRole() == Role.ADMIN) {
        adminExists = true;
        break;
    }
}

// Replacement for a not-found Optional chain; preserve its exact original exception.
Optional<Booking> found = bookings.findByIdStrict(bookingId);
if (found.isEmpty()) {
    throw new RecordNotFoundException("Booking not found.");
}
Booking booking = found.get();

// Replacement for LearningPackageService's legacy booking anyMatch,
// within the existing PACKAGE_WRITE_LOCK; keep the subsequent enrollment check too.
if (bookings != null) {
    for (Booking booking : bookings.readAllStrict()) {
        if (id.equals(booking.getEnrollmentId())) {
            throw new ValidationException("Package cannot be deleted because lesson history refers to it.");
        }
    }
}
```

For StudentAccountService preserve eager evaluation of its three reads: three separate flag-building loops, followed by `if (hasBookings || hasReviews || hasEnrollments)`. Combining all checks into one early return can hide an error in a later file that the current strict reads would report. For InstructorLookupService's interval pipeline, filter into an ArrayList, then insertion-sort only when previous.startTime isAfter(current.startTime); this retains stable equal-time order. Do not change the public instructor bubble sort.

### 6.2 Long methods over 25 physical lines

Physical spans include comments/formatting and are not cyclomatic-complexity scores. Current examples found by brace-span inspection:

| Owner | File.method | Lines / span | Smallest safe recommendation | Risk |
|---|---|---|---|---|
|135|common/repository/FileRepository.readRecords|277–336,60 lines|Keep shared IO logic; document strict/lenient behavior, possibly extract parse-one-line helper only after tests|High for refactor; Low to document |
|135|auth/controller/AuthController.login|150–209,60|Extract result-to-message mapping without changing session/error behavior|Medium |
|133|instructor/service/InstructorAvailabilityService.addAvailability|33–79,47|Unused feature path: document first; no viva-driven rewrite needed|Low documentation |
|133|instructor/service/InstructorService.createInstructor|51–86,36|Separate validation/uniqueness helper from object construction without changing sequence|Medium |
|134|booking/service/BookingConflictChecker.conflictReason|82–135,54|Name small rule helpers; preserve first-failure ordering|Medium |
|134|booking/service/BookingService.rescheduleOwnBooking|217–258,42|Keep whole replace/cancel operation under lock; extract pure checks only|Medium; High if lock/write order changes |
|132|vehicle/controller/VehicleController.updateVehicle|209–270,62|Extract repeated form-model preparation; keep binder allowlist/status/error routes|Medium |
|132|vehicle/controller/VehicleController.createVehicle|120–171,52|Same local form preparation extraction|Medium |
|136|learningpackage/service/LearningPackageService.createPackage|78–126,49|Extract package validation/helper under same lock|Medium |
|136|learningpackage/service/EnrollmentPresentationService.describe|128–181,54|Separate status choice from labels/next step, retaining precedence|Medium |
|137|review/model/Review.updateStatus|202–231,30|Expand or separate validation from mutation, retaining boolean changed return|Medium |
|137|review/model/Review.updateContent|171–199,29|Same; do not remove validation or moderation reset|Medium |

These are explanation candidates, not mandatory defects or a request to refactor every >25-line method. Constructors with many validated fields may naturally exceed25 lines. Prioritize explaining the existing code before changing it near a viva.

### 6.3 Unused contracts and dead/scaffold paths — separate from working OOP

Static references were searched in src/main, not inferred from tests. “No production consumer found” means no call/injection consumer found beyond configuration/its own implementation; reflective/external use is **UNVERIFIED**. Tested-but-unconnected code is not the same as failing code.

| Owner | Types/path | Finding | Recommendation / risk |
|---|---|---|---|
|133|`instructor/api/InstructorLookup`, `InstructorBookingInfo`; `InstructorLookupService`|Configured in InstructorConfig, methods tested, but no main feature consumes the contract. Its interval availability does not drive booking slots|**Keep and document as unconnected contract**, Low. It illustrates abstraction; do not delete for this audit |
|133|`InstructorAvailabilityService`, `InstructorAvailabilityRepository`, `InstructorAvailability` and their InstructorConfig beans|Separate datetime-interval store. Actual instructor UI/slot generation uses InstructorScheduleService + AvailabilityRepository weekly blocks and InstructorLeaveRepository|**Keep/document**, Low. Unifying stores would be High-risk cross-module behavior work, not cleanup |
|136|`learningpackage/api/EnrollmentLookup`, `EnrollmentSummary`|Interface/record exist; no main implementation/consumer found. New EnrollmentUsageView is a distinct presentation DTO|**Keep/document**, Low; do not rename or delete teammates' contract classes |
|132|`vehicle/api/VehicleLookup`, `VehicleBookingInfo`, `VehicleLookupService`, VehicleConfig.vehicleLookup|Configured read-only DTO adapter; no operational consumer found in booking conflict checking, which uses repositories|Keep/document, Low; protects an abstraction example; do not claim it is the live conflict path |
|133|`InstructorService.getInstructorsSortedByExperience` at168 and sorter's convenience wrapper|Method has no external main-source call found. Public route calls sortInstructors instead|Keep convenience method and explain actual route. Low. Mandatory sorter stays |
|136/shared UI|`common/components/pagination.jspf`, `skeleton.jspf`, `search-bar.jspf`, `breadcrumbs.jspf`|Search finds their own filename/header but no include reference in main pages; scaffold presence does not establish actual pagination/search features|Removed in the later explicitly approved cleanup, Low risk; current page search/breadcrumb controls remain |
|135/common|ContactMessage/ContactMessageRepository/ContactService.submit|**Not dead:** contact submission saves records. No admin inbox reader found. Generic repository reading for IDs is not an inbox|Keep; document no email delivery/inbox and use honest page wording. Low; coordinate136 shared page owner |

Do not label Pending account status dead: login still blocks it, admin forms expose it and demo fixtures include it. Do not delete the queue, bubble sort, sample data, security classes or examples that carry OOP evidence. No unused contract removal is recommended by this audit.

## 7. Coding standards and documentation accuracy

| Common issue | Exact example | Consequence | Smallest fix / owner / risk |
|---|---|---|---|
| Extremely long JSP lines | `student/password.jsp:20` is1724 characters; booking/card forms also combine many tags | Hard to review form fields, escaping and ownership controls | Reformat tags/attributes only;135 (other owners on own pages); Low. Check generated whitespace does not enter inputs |
| Inconsistent indentation | `common/repository/FileRepository.java:298`, validateRecord(record) is indented outside its surrounding block visually | Hides actual try/block flow during explanation | Indentation only,135, Low; no lock/try movement |
| Compressed Java methods | Repository CRUD overrides on single lines, e.g. VehicleRepository.readAllStrict at45; DTO getters/constructor arguments | Can obscure override/delegation for beginners | Expand selected teaching examples;132/136, Low; do not duplicate file logic |
| Multi-variable declaration/clever arithmetic | `sample/DemoDataSeedRunner.java:197` assigns student/instructor/vehicle together using modulo | Fixture links are hard to explain | Three named declarations and a short mapping comment,132, Low; status rewrite would be Medium |
| Repeated generic comments and overstated SOLID labels | Repeated “This class is responsible for application rules”; LearningRecord has repeated SOLID:I comments; many headers say see labels without a specific example | Label density substitutes for explanation and can overclaim SRP/DIP | Keep required headers/labels; replace redundant prose with one accurate local example, each owner, Low |
| Naming/semantic ambiguity | Availability vs InstructorAvailability; package “Active” versus account Active versus Paid | Students may explain the wrong store/status | Add glossary and state diagrams;133/136/135, Low. No class renaming needed |
| Placeholder identity headers | `[Name]` remains in member headers; README ownership name placeholders | Does not establish personal authorship; weak presentation polish | Fill only verified names, keep IDs/roles; each member, Low; do not infer authorship from a name change |
| Stale report facts | REPORT.md says new registrations Pending, enrollment simulated Paid, seven enrollments and575 tests | Conflicts with current registration/payment fixtures and prior verification |137 assembles corrected final report,135 and each owner verify; Low. Do not silently replace a dated test result with an unrun claim |
| Incomplete deliverables | REPORT.md screenshots are placeholders; no Git history appendix; local diagrams/VIVA document untracked | Working application can still miss required report/submission items | Finish genuine screenshot/history appendix and submit reviewed artifacts via user-managed Git; Low code risk / High delivery risk if omitted |

Generally good naming exists: BookingStatus/AccountStatus enums, descriptive service methods and feature packages. This does not cancel the issues above. Java records, pattern matching, switch expressions and constructor injection are allowed Java17 features, not automatically forbidden technology. Spring annotations and JSP/JSTL belong to the existing Spring MVC implementation; no database backend was found in the inspected persistence route. Full dependency/license compliance is outside this audit and **UNVERIFIED**.

## 8. Git history and individual contribution

Read-only commands used: `git log --format`, `git log --all --format`, `git log --no-merges --format`, `git rev-parse --is-shallow-repository`, `git ls-files`, `git check-ignore`, and read-only status/diff inspection. No fetch or remote mutation. Raw author identity uses `%an <%ae>`; email case is preserved in the first table. HEAD at audit: **7d4bdbd**, dated2026-11-09, merge into the current final UI branch. Repository is not shallow (`false`). GitHub's current remote branch, contributors UI and any un-fetched remote commits are **UNVERIFIED**.

### Raw author identities

| Author name | Email | Commits reachable from HEAD | Commits across all local refs |
|---|---|---:|---:|
| IT26101135 | it26101135@my.sliit.lk |36|36|
| Thivviyapragash. N | it26101135@my.sliit.lk |35|36|
| Shahiththiyan.J | it26101133@my.sliit.lk |12|14|
| it26101134-art | it26101134@my.sliit.lk |9|13|
| Piravarshini | IT26101132@my.sliit.lk |12|12|
| it26101136 | it26101136@my.sliit.lk |10|10|
| Jayathma P.K.S | it26101137@my.sliit.lk |11|13|
| Total | |125|134|

Grouped for explanation by case-insensitive institutional email (not a history rewrite):

| Fixed member | HEAD all commits | HEAD non-merge commits | HEAD merge commits | All local refs |
|---|---:|---:|---:|---:|
|135|71|35|36|72|
|133|12|7|5|14|
|134|9|6|3|13|
|132|12|10|2|12|
|136|10|10|0|10|
|137|11|8|3|13|
| Total |125|76|49|134|

All six appear.135 has two display names but the same email and many integration merges. The oldest/teammate work remains reachable; raw totals are not six comparable measures of code effort. Non-merge counts reduce merge inflation but still do not measure understanding, code quality or lines actually authored. Do not equate header ownership with Git authorship or assume a merge author wrote every merged file.

A marker on this checked-out history would also see the large recent135 integration/snapshot/cleanup work:

- `c8cc13e` — chore: final UI, payment flow and docs.
- `fa0bd38` — feat: packages mega-menu with role-aware actions.
- `68d6e37` — chore: remove unused legacy files, CSS and imports; untrack demo data.
- `7395c7d` / `5fe541b` — snapshot/WIP commits.

The later attendance/UI presentation changes currently exist in the working tree, not these commits. `docs/CLASS_DIAGRAMS.md`, `docs/VIVA_QA.md` and diagram sources are currently untracked; a marker cloning HEAD would not receive them. The same is true for this new audit until the user chooses to include it. A working local folder is not proof of a submitted GitHub artifact.

REPORT.md does not include the required commit-history appendix. Smallest truthful addition (**Low**,137 report assembly +135 integration): an actual dated export/table from read-only `git log --date=iso-strict --format="%h | %ad | %an | %ae | %s"`, with HEAD, scope and the grouped explanation above. Attach genuine contribution descriptions agreed by each member. The user alone handles staging/commits/pushes.

Do not fabricate commits, backdate work, rewrite authors to divide credit, pad history, or claim integrated code was independently written by six people. Members should use their own identities for genuine future work and be prepared to explain the work attributed to them. Full assessment of team process/individual effort remains **UNVERIFIED**.

## 9. Class diagrams compared with current source

Inspected all11 Mermaid blocks in docs/CLASS_DIAGRAMS.md and cross-checked class names, displayed method names, key fields and relationships against Java. The document explicitly hides parameter lists/overloads and most getters, so `method()` is not automatically a wrong no-argument signature. No nonexistent project class or displayed method name was found among its class boxes. Source comparison is not a guarantee that Mermaid/PlantUML rendering/export works; fresh renderer verification in this audit is **UNVERIFIED**.

Correct teaching details already present: Instructor is not a User subclass; InstructorAccount is. LearningRecord is an interface. Queue Node is private. Paid receipt checks and two-stage assignment are distinguished. File repositories inherit the generic parent; availability stores are separate; enrollment payment/reference fields are computed. Do not “fix” these into incorrect inheritance or a database diagram.

| Location | Mismatch / stale statement | Smallest honest change | Risk / owner |
|---|---|---|---|
| CLASS_DIAGRAMS.md:20, package figure/table | Claims55 direct project-package import pairs. Current source has56: **instructor → learningpackage** was added by InstructorDashboardController's presentation dependency; the arrow/table entry is missing | Add that arrow/table entry and update count to56; update PlantUML package copy too | Low;135 alone |
| Sections3/4/7 (student, booking, enrollment diagrams) and omission index | New EnrollmentPresentationService/EnrollmentUsageView and five controller presentation dependencies are absent. Teaching figures are intentionally selective, so this is an incompleteness update, not invented old classes | Add the two types/dependencies or a concise current omission-index entry explaining read-only maps and authorization scope. Do not rename existing EnrollmentSummary | Low;135 with136 verifies helper;133/134 confirm calls |
| Section7 note on EnrollmentController:757 | Says CREATE/READ/UPDATE/DELETE use the student interface. Current **list** prepares read data through EnrollmentPresentationService and repository maps rather than EnrollmentOperations.ownEnrollments (other routes still use the interface) | Distinguish list presentation read from write/payment calls | Low;135/136 |
| Registration→enrollment→booking sequence around1010–1029 | Request sequence lacks new commitment-cap validation inside the existing lock | Add a note before save; no payment flow rewrite | Low;135 +134 |
| Queue-processing sequence around1068–1080 | Payment-valid assignment path omits requireCommitmentRoom; over-cap historical data throws before resource assignment | Add a cap-refusal branch/note before resource lookup | Low;135 +134 |
| Attendance behavior not represented in selected Booking operations | New complete(now,setting)/no-show timing guards and default true configuration absent | Optional small attendance note/flow, since overload omission is declared. Do not claim parameter hiding is a broken method signature | Low;135 +134/133 |
| Final line1414 and verification notes | Phase-B stop text says VIVA_QA was not created in that phase; true historically, but obsolete as a current stop instruction. Existing docs now include later verification/UI work | Mark Phase-B text historical and link latest VIVA verification; keep dated evidence separate | Low;135 |
| Version control delivery | Diagram .md and PlantUML folder are untracked locally | User should include approved final diagrams in submission; audit performs no Git write | Low code risk; High omission risk;135/user |

The ER figure is a logical link map, not a relational schema or enforced FK contract. Its one-to-one role/profile arrows describe intended application relationships, not separate SQL tables. Legacy data can violate those links; file storage has no FK enforcement. The diagram already warns about this, so absence of SQL constraints is not a diagram bug.

No blanket diagram regeneration or source refactor is needed. Update the matching PlantUML alternatives when correcting Mermaid; do not deliver two contradictory diagram sets. Risk Low,135 owns the documents and each member reviews their own classes.

## 10. What each member can safely prepare alone

These are proposed tasks, not edits made here. Keep fixed module ownership and coordinate shared interfaces; the user performs all branch/Git operations.

| Member | Safe independent work | Risk | Work requiring coordination |
|---|---|---|---|
|135|Correct diagram package arrow/count, local documentation, explain User/FileRepository/session flow and truthful history appendix|Low|Storage, authentication or AccountWriteCoordinator changes are High; review across all modules. Shared-controller changes require affected owner input |
|133|Rehearse five-instructor bubble sort example, experience comparator and active filtering; document legacy interval versus weekly availability; local formatting|Low|Availability behavior, role/login identity and review-driven rating updates involve134/135/137. Do not change mandatory sort |
|134|Prepare a small owned booking-detail UI with authorized read and test, or obtain explicit marker acceptance of current interfaces; rehearse persistent FIFO reconstruction|Medium for new route/UI|Shared lock/conflict/payment changes require135/133/132/136; High for changing lock/write ordering. Queue implementation remains |
|132|Prepare sanitized sample-data bundle and document real file names; show vehicle create/edit/list and JSON Lines IO; format local seed declarations|Medium overall due to privacy/link review; Low formatting|135 checks account/hash/load behavior;136/134/137/133 check fixture links and dates |
|136|Explain LearningRecord, receipt versus account balance and read-only usage maps; document unused contracts; local JSP/Java formatting|Low|Deleting/changing contracts or renewal/payment/state rules needs135/134; High for cross-module logic rewrites |
|137|Assemble final report, genuine screenshots and actual Git appendix; reconcile dated test figures and seed totals; rehearse review moderation/rating/CSV|Low|133 verifies rating effects,132 seed arithmetic,135 Git/shared integration; CSV/refactoring is Medium with regression examples |

Minimum readiness checklist: all six can trace one C/R/U/D chain and a failed validation;134's third UI requirement is resolved honestly; sample files are included in the submission; final report contains genuine history/screenshots; diagrams describe the delivered version; local untracked work is deliberately submitted by the user. Student viva performance, institutional acceptance of within-page UI counts, remote GitHub state and the final presentation remain **UNVERIFIED**.

Stop point: analysis complete. Only docs/RUBRIC_AUDIT.md was written. Await user approval before any source or deliverable changes recommended here.
