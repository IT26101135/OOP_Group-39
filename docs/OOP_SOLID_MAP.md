# OOP and SOLID map

> **Current guide (10 Nov 2026):** use the [source-checked viva pack](viva-2026/README.md), [six individual diagrams](viva-2026/class-diagrams.html) and [six printable cheat sheets](viva-2026/cheat-sheets.pdf). The older material below is historical and may mention removed interfaces or earlier report layouts.

Owner: **IT26101135 - [Name]** (integration documentation).

These are concrete examples to explain, not a claim that every class satisfies every principle perfectly. Each member has all five requested concepts and all five SOLID letters below. A class-declaration row names a relationship rather than a callable method. Shared classes retain their single file owner even when another member explains how their module uses them.

| Member | Pillar or principle | Class | Method | One-sentence viva explanation |
|---|---|---|---|---|
| IT26101135 | Encapsulation | [Student](../src/main/java/com/drivingschool/student/model/Student.java) | `setPhone` | Private student details change through checked methods, so a bad phone number cannot be saved. |
| IT26101135 | Inheritance | [Student](../src/main/java/com/drivingschool/student/model/Student.java) | `Student` | Student extends User and reuses the common account identity, name and email rules. |
| IT26101135 | Abstraction | [User](../src/main/java/com/drivingschool/auth/model/User.java) | `getRole` | The abstract User class requires each account type to supply its role without defining one shared answer. |
| IT26101135 | Polymorphism | [Student](../src/main/java/com/drivingschool/student/model/Student.java) | `getDashboardPath` | The same User dashboard method returns the student page for Student and different pages for Admin and InstructorAccount. |
| IT26101135 | Exception handling | [StudentProfileService](../src/main/java/com/drivingschool/student/service/StudentProfileService.java) | `ensureAvailableEmail` | A duplicate email throws DuplicateRecordException, which GlobalExceptionHandler shows as a friendly conflict page. |
| IT26101135 | S | [StudentProfileController](../src/main/java/com/drivingschool/student/controller/StudentProfileController.java) | `updateProfile` | The controller accepts the web form while StudentProfileService checks the rules and UserRepository saves it. |
| IT26101135 | O | [User](../src/main/java/com/drivingschool/auth/model/User.java) | `class declaration` | A new account subtype can provide its own role and destination while existing account subclasses keep their behaviour. |
| IT26101135 | L | [Student](../src/main/java/com/drivingschool/student/model/Student.java) | `getRole` | A Student can be read as a User and still supplies a valid role and dashboard destination. |
| IT26101135 | I | [StudentProfiles](../src/main/java/com/drivingschool/student/api/StudentProfiles.java) | `getOwnProfile` | The profile contract exposes only the own-profile tasks needed by its page. |
| IT26101135 | D | [StudentProfileController](../src/main/java/com/drivingschool/student/controller/StudentProfileController.java) | `StudentProfileController` | The controller receives StudentProfiles through its constructor instead of creating a concrete profile service. |
| IT26101133 | Encapsulation | [Instructor](../src/main/java/com/drivingschool/instructor/model/Instructor.java) | `setYearsOfExperience` | Private teaching details are changed through setters that reject invalid experience and contact details. |
| IT26101133 | Inheritance | [InstructorRepository](../src/main/java/com/drivingschool/instructor/repository/InstructorRepository.java) | `InstructorRepository` | The instructor repository extends FileRepository and reuses its safe file operations. |
| IT26101133 | Abstraction | [InstructorOrder](../src/main/java/com/drivingschool/instructor/service/InstructorOrder.java) | `compare` | The sorter asks for a comparison without needing to know which teaching detail is being compared. |
| IT26101133 | Polymorphism | [InstructorSorter](../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java) | `swapNeighbours` | The same compare call uses InstructorNameOrder, InstructorExperienceOrder or InstructorRatingOrder to answer differently. |
| IT26101133 | Exception handling | [InstructorScheduleService](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java) | `ensureNoOverlap` | Overlapping working hours throw ValidationException and the shared handler explains the problem. |
| IT26101133 | S | [InstructorScheduleService](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java) | `addAvailability` | Scheduling rules stay in the service while its controller handles the form and its repository handles the file. |
| IT26101133 | O | [InstructorOrder](../src/main/java/com/drivingschool/instructor/service/InstructorOrder.java) | `compare` | Another comparison implementation can be added without rewriting the adjacent-swap bubble-sort passes; chooseOrder still needs to select the new option. |
| IT26101133 | L | [InstructorNameOrder](../src/main/java/com/drivingschool/instructor/service/InstructorNameOrder.java) | `compare` | Every comparison follows the same negative, zero or positive contract, so the sorter can substitute any existing order. |
| IT26101133 | I | [InstructorOrder](../src/main/java/com/drivingschool/instructor/service/InstructorOrder.java) | `compare` | The comparison interface has one job: compare two instructor cards. |
| IT26101133 | D | [InstructorAvailabilityController](../src/main/java/com/drivingschool/instructor/controller/InstructorAvailabilityController.java) | `InstructorAvailabilityController` | The controller receives InstructorScheduleOperations through its constructor. |
| IT26101134 | Encapsulation | [Booking](../src/main/java/com/drivingschool/booking/model/Booking.java) | `Booking` | Private lesson identity, time and status are checked before a valid request object exists. |
| IT26101134 | Inheritance | [BookingRepository](../src/main/java/com/drivingschool/booking/repository/BookingRepository.java) | `BookingRepository` | BookingRepository reuses the FileRepository parent rather than copying file-writing code. |
| IT26101134 | Abstraction | [BookingOrder](../src/main/java/com/drivingschool/booking/util/BookingOrder.java) | `order` | Callers ask for an ordered list through one small contract. |
| IT26101134 | Polymorphism | [BookingService](../src/main/java/com/drivingschool/booking/service/BookingService.java) | `getAllBookings` | The same order method builds a FIFO view through PendingRequestOrder or a chronological view through LessonTimeOrder. |
| IT26101134 | Exception handling | [BookingService](../src/main/java/com/drivingschool/booking/service/BookingService.java) | `parseFilterDate` | Malformed filter dates become ValidationException messages instead of an unhandled date parsing error. |
| IT26101134 | S | [BookingConflictChecker](../src/main/java/com/drivingschool/booking/service/BookingConflictChecker.java) | `conflictReason` | The conflict checker checks schedule rules while the queue keeps waiting order and repositories save records. |
| IT26101134 | O | [BookingOrder](../src/main/java/com/drivingschool/booking/util/BookingOrder.java) | `order` | Another ordering implementation can be supplied without changing the existing ordering classes. |
| IT26101134 | L | [LessonTimeOrder](../src/main/java/com/drivingschool/booking/util/LessonTimeOrder.java) | `order` | Both booking orders return a safe new list and leave the caller original unchanged; the caller intentionally chooses the needed subset. |
| IT26101134 | I | [StudentBookingOperations](../src/main/java/com/drivingschool/booking/service/StudentBookingOperations.java) | `requestLesson` | Student web pages receive their booking tasks separately from administrator tasks. |
| IT26101134 | D | [BookingController](../src/main/java/com/drivingschool/booking/controller/BookingController.java) | `BookingController` | The controller receives StudentBookingOperations and a package catalog through its constructor. |
| IT26101132 | Encapsulation | [Vehicle](../src/main/java/com/drivingschool/vehicle/model/Vehicle.java) | `setNextServiceDate` | Private maintenance dates are checked so the next service cannot be before the last service. |
| IT26101132 | Inheritance | [VehicleRepository](../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java) | `VehicleRepository` | VehicleRepository extends FileRepository and selects the vehicle record type and file. |
| IT26101132 | Abstraction | [VehicleOperations](../src/main/java/com/drivingschool/vehicle/api/VehicleOperations.java) | `createVehicle` | The fleet controller asks for vehicle tasks through an interface instead of file details. |
| IT26101132 | Polymorphism | [VehicleRepository](../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java) | `save` | A FileRepository reference to VehicleRepository dispatches its save override; the parent writer uses the vehicle file and record type. |
| IT26101132 | Exception handling | [VehicleService](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) | `ensureRegistrationNumberIsUnique` | A duplicate plate throws DuplicateRecordException and the controller redisplays the form with its message. |
| IT26101132 | S | [VehicleService](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) | `deleteVehicle` | The service checks lesson-history rules while VehicleRepository handles file deletion. |
| IT26101132 | O | [VehicleRepository](../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java) | `VehicleRepository` | A new child repository can select a different file and record type without rewriting shared storage. |
| IT26101132 | L | [VehicleRepository](../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java) | `update` | The child delegates to the same parent checks and preserves the shared update contract. |
| IT26101132 | I | [VehicleLookup](../src/main/java/com/drivingschool/vehicle/api/VehicleLookup.java) | `findBookingInfo` | Scheduling receives one narrow lookup rather than all fleet management operations. |
| IT26101132 | D | [VehicleController](../src/main/java/com/drivingschool/vehicle/controller/VehicleController.java) | `VehicleController` | The controller receives VehicleOperations through its constructor. |
| IT26101136 | Encapsulation | [LearningPackage](../src/main/java/com/drivingschool/learningpackage/model/LearningPackage.java) | `setPrice` | Private course details are checked before changing a price or lesson count. |
| IT26101136 | Inheritance | [LearningPackageRepository](../src/main/java/com/drivingschool/learningpackage/repository/LearningPackageRepository.java) | `LearningPackageRepository` | The package repository extends FileRepository and reuses its safe storage behaviour. |
| IT26101136 | Abstraction | [LearningRecord](../src/main/java/com/drivingschool/learningpackage/model/LearningRecord.java) | `getDisplayName` | A small interface lets a course or enrollment provide a readable heading. |
| IT26101136 | Polymorphism | [Enrollment](../src/main/java/com/drivingschool/learningpackage/model/Enrollment.java) | `getDisplayName` | LearningPackage returns its course name and Enrollment returns its enrollment heading through the same LearningRecord method; EnrollmentServiceTest demonstrates both types. |
| IT26101136 | Exception handling | [EnrollmentService](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) | `ensureDeletable` | ValidationException explains why an active enrollment, paid receipt or referenced enrollment must be kept. |
| IT26101136 | S | [EnrollmentService](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) | `enroll` | The enrollment service links the student and course while repositories save the records. |
| IT26101136 | O | [LearningRecord](../src/main/java/com/drivingschool/learningpackage/model/LearningRecord.java) | `getDisplayName` | Another learning record can supply its own heading without changing the existing implementations. |
| IT26101136 | L | [LearningPackage](../src/main/java/com/drivingschool/learningpackage/model/LearningPackage.java) | `getDisplayName` | Both learning record types supply a valid ID and readable heading through the same contract. |
| IT26101136 | I | [EnrollmentOperations](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentOperations.java) | `ownEnrollments` | The enrollment page receives only the operations it needs for its own records. |
| IT26101136 | D | [EnrollmentController](../src/main/java/com/drivingschool/learningpackage/controller/EnrollmentController.java) | `EnrollmentController` | The enrollment controller receives EnrollmentOperations through its constructor. |
| IT26101137 | Encapsulation | [Review](../src/main/java/com/drivingschool/review/model/Review.java) | `updateContent` | Private rating and comment fields change only after validation, and changed feedback returns to pending moderation. |
| IT26101137 | Inheritance | [ReviewRepository](../src/main/java/com/drivingschool/review/repository/ReviewRepository.java) | `ReviewRepository` | ReviewRepository extends FileRepository and chooses the reviews file. |
| IT26101137 | Abstraction | [ReviewOperations](../src/main/java/com/drivingschool/review/service/ReviewOperations.java) | `getVisibleReviews` | The feedback controller asks for allowed review tasks without depending on file storage. |
| IT26101137 | Polymorphism | [ReviewRepository](../src/main/java/com/drivingschool/review/repository/ReviewRepository.java) | `save` | A FileRepository reference to ReviewRepository dispatches its save override; the parent writer uses the review file and record type. |
| IT26101137 | Exception handling | [ReviewService](../src/main/java/com/drivingschool/review/service/ReviewService.java) | `ensureNoDuplicate` | DuplicateRecordException explains that the completed lesson already has a review. |
| IT26101137 | S | [AdminReportService](../src/main/java/com/drivingschool/report/service/AdminReportService.java) | `all` | Reports count saved records while the controller creates HTTP responses and the JSP draws charts. |
| IT26101137 | O | [ReviewRepository](../src/main/java/com/drivingschool/review/repository/ReviewRepository.java) | `ReviewRepository` | A new repository child can select its own record file without changing shared storage. |
| IT26101137 | L | [ReviewRepository](../src/main/java/com/drivingschool/review/repository/ReviewRepository.java) | `delete` | The child keeps the parent missing-record, validation and storage rules when deleting. |
| IT26101137 | I | [ReviewModeration](../src/main/java/com/drivingschool/review/service/ReviewModeration.java) | `approveReview` | Only administrator pages receive moderation operations; student pages use ReviewOperations. |
| IT26101137 | D | [AdminReportController](../src/main/java/com/drivingschool/report/controller/AdminReportController.java) | `AdminReportController` | The report controller receives ReportCatalog through its constructor. |

## Read these distinctions before the viva

- **Inheritance** needs an `extends` relationship for a parent class: module repositories extend `FileRepository`; Student/Admin/InstructorAccount extend User. Implementing an interface is the separate abstraction contract. No new inheritance level was added for these examples.
- **Polymorphism** means a call follows the actual object implementation. Instructor and booking orders use genuinely different algorithms or comparison rules. The vehicle/review repository overrides delegate to the same storage algorithm; their configured file and record type differ. Explain that dispatch honestly rather than claiming these repositories implement different saving algorithms.
- `LearningRecord` is an interface, not an abstract parent class. Its course/enrollment headings are both demonstrated in `EnrollmentServiceTest.displaysBothLearningRecordTypesThroughTheirContract`; the current JSPs also use the concrete records' own fields.
- **Exception handling** is included because the project brief asks for it as a fifth concept. It is shown by throwing the existing custom exceptions and handling them through `GlobalExceptionHandler` or a controller catch block.
- **O** does not promise every addition needs zero edits: for example `InstructorSorter.chooseOrder` must select any new comparator exposed by a new menu option. The bubble-sort passes remain reusable.
- `FileRepository<T>` uses `T` to mean the kind of record in that filing drawer. Its shared file locks, atomic replacement, JSON reader and ID counter are infrastructure; explain the purpose before discussing the implementation.
- `AccountWriteCoordinator.execute` lets one account change finish before another starts. Existing simple callbacks keep several steps inside that shared lock; they are not a database transaction or a guarantee across multiple application processes.
- Basic MVC roles are consistent: controller = web requests, service = school rules, repository = file storage, model = validated details. Constructor injection means Spring gives a class the helper it needs.

## Manual payment examples (2026-11-08)

| Member | Pillar or principle | Class | Method | One-sentence viva explanation |
|---|---|---|---|---|
| IT26101136 | Encapsulation | Enrollment | setStatus, setCancellationReason | Private state can contain only one of three payment states and a checked optional note. |
| IT26101136 | Abstraction / I | EnrollmentAdministration | markPaid, cancelRequest | The cashier page needs a small admin contract rather than all student operations. |
| IT26101136 | D / S | AdminEnrollmentController | constructor, markPaid | The controller receives its contract and handles forms while the service makes payment decisions. |
| IT26101136 | Exception handling | EnrollmentService | requireAwaiting, paymentStudent, payablePackage | Friendly exceptions explain repeated decisions, suspended students and inactive courses. |
| IT26101134 | S / Exception handling | PaidEnrollmentGuard | problemFor, requirePaid | One helper reads payment evidence and explains why a lesson cannot be promised. |
| IT26101135 | Encapsulation | DashboardSummary | getAwaitingPayments, setAwaitingPayments | A private validated count is shown through public methods. |
| IT26101132 | S | DemoDataSeedRunner | seedPaymentExample | The loader prepares examples without replacing an earlier cashier decision. |
| IT26101137 | S | AdminReportService | revenue | The report adds only captured amounts from Paid receipts. |

The existing inheritance, LearningRecord polymorphism and other SOLID examples above remain in place. The shared AccountWriteCoordinator lock serializes payment decisions and booking writes in one JVM. This is coordination, not a durable multi-file transaction or a lock across several application servers.
