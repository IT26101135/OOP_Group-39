# DriveSpark viva pack - current code, 10 November 2026

Use these answers as short speaking notes, not a claim of personal authorship.
There are 20 project answers, 12 per member, 15 what-if answers and 5 group answers:
112 answers in total. Each speaking answer has 2-4 short sentences. Code evidence
and browser paths are separate prompts, not additional sentences to memorize.

Four conventional OOP pillars are encapsulation, abstraction, inheritance and
polymorphism. This guide treats exception handling as the fifth course topic,
matching the existing project labels. **NEEDS CHECK:** confirm the lecturer's
preferred five-item list; do not claim a universal fifth OOP pillar. Classes and
objects are also demonstrated by each model and constructor.

SOLID examples are local examples, not proof that every class is perfectly SOLID.
Some dependencies remain concrete repositories. Shared classes keep their source
file owner even when another member explains how their module uses them.

**Evidence:** final `clean verify`: 694 tests, 0 failures, 0 errors,
0 skipped. Batch C improves shared forms and attendance controls. See [the Batch C review](../BATCH_C_REVIEW.md) for current browser and
print evidence and limits. Browser paths were checked on isolated seeded data;
not every write branch was exercised in the browser. Never perform destructive
demonstrations on live data. **NEEDS CHECK** marks unconfirmed claims.

## Downloads

- [Printable speaking guide](speaking-guide.html) and [PDF](speaking-guide.pdf)
- [Six one-page cheat sheets](cheat-sheets.html) and [PDF](cheat-sheets.pdf)
- [Individual class diagrams, printable](class-diagrams.html) and [PDF](class-diagrams.pdf)

## Project-level questions

### 1. What does our system do?

In our system, I help a school manage students, lesson packages, bookings, vehicles and reviews. I give admins reports and saved website enquiries to read.

**Show in code:** [HomeController.java](../../src/main/java/com/drivingschool/common/controller/HomeController.java) - `showHomePage`; [AdminReportService.java](../../src/main/java/com/drivingschool/report/service/AdminReportService.java) - `business`; [ContactService.java](../../src/main/java/com/drivingschool/common/service/ContactService.java) - `messagesForAdmin`.

**Show in browser:** Home > Packages > View package.

### 2. What are the four roles?

I explain visitors, students, instructors and admins as four user experiences. I store only three Role enum values because a visitor is someone who is not logged in.

**Show in code:** [Role.java](../../src/main/java/com/drivingschool/auth/model/Role.java) - `class declaration`; [NavigationInterceptor.java](../../src/main/java/com/drivingschool/auth/interceptor/NavigationInterceptor.java) - `postHandle`.

### 3. Why Spring Boot?

I use Spring Boot to start the Java web application and connect its components. I use constructor injection so Spring supplies the objects each controller needs.

**Show in code:** [DrivesparkAcademyApplication.java](../../src/main/java/com/drivingschool/DrivesparkAcademyApplication.java) - `main`; [BookingController.java](../../src/main/java/com/drivingschool/booking/controller/BookingController.java) - `BookingController`.

### 4. How does MVC work here?

I let controllers accept requests, services check rules and repositories read or save data. I pass a model to a JSP view to display the result.

**Show in code:** [BookingController.java](../../src/main/java/com/drivingschool/booking/controller/BookingController.java) - `list`; [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `getOwnBookings`; [BookingRepository.java](../../src/main/java/com/drivingschool/booking/repository/BookingRepository.java) - `readAllStrict`.

**Show in browser:** Student > My lessons.

### 5. Why file-based storage?

I store one JSON object per line so the records are easy to inspect and run locally. I accept that this has fewer safety and querying features than a database.

**Show in code:** [FileRepository.java](../../src/main/java/com/drivingschool/common/repository/FileRepository.java) - `readRecords`; [FileRepository.java](../../src/main/java/com/drivingschool/common/repository/FileRepository.java) - `writeRecords`.

### 6. Are file writes transactions?

I do not have a transaction across several files. I use file replacement and locks inside one JVM, but a crash between two file updates can still leave partial work.

**Show in code:** [FileRepository.java](../../src/main/java/com/drivingschool/common/repository/FileRepository.java) - `writeRecords`; [AccountWriteCoordinator.java](../../src/main/java/com/drivingschool/auth/util/AccountWriteCoordinator.java) - `execute`; [EnrollmentService.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) - `markPaid`.

### 7. How are passwords stored?

I store a salted BCrypt hash instead of the original password. I use matches to check a login and never need to decrypt a password.

**Show in code:** [PasswordHasher.java](../../src/main/java/com/drivingschool/auth/util/PasswordHasher.java) - `hash`; [PasswordHasher.java](../../src/main/java/com/drivingschool/auth/util/PasswordHasher.java) - `matches`.

### 8. How do sessions work?

I keep the logged-in user ID in a server-side HTTP session. I rotate the session on login and reload the account so a deleted or suspended account does not keep normal access.

**Show in code:** [AuthSessionService.java](../../src/main/java/com/drivingschool/auth/service/AuthSessionService.java) - `loginWithResult`; [AuthSessionService.java](../../src/main/java/com/drivingschool/auth/service/AuthSessionService.java) - `getCurrentUser`.

**Show in browser:** Log in > Account > Log out.

### 9. What is CSRF protection?

I put a session token in forms that change data. I reject unsafe requests with missing or wrong tokens so another website cannot simply submit a form as the signed-in user.

**Show in code:** [CsrfTokenService.java](../../src/main/java/com/drivingschool/auth/service/CsrfTokenService.java) - `isValid`; [CsrfFilter.java](../../src/main/java/com/drivingschool/auth/filter/CsrfFilter.java) - `doFilterInternal`.

### 10. How do we protect admin pages?

I use access annotations and an interceptor to check login and role. I also check role and ownership in services before exposing or changing a record.

**Show in code:** [AccessInterceptor.java](../../src/main/java/com/drivingschool/auth/interceptor/AccessInterceptor.java) - `preHandle`; [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `getBookingDetails`.

### 11. Where is validation done?

I use browser rules to help the user enter details. I repeat important checks in Java because someone can bypass the browser.

**Show in code:** [Student.java](../../src/main/java/com/drivingschool/student/model/Student.java) - `setPhone`; [LearningPackage.java](../../src/main/java/com/drivingschool/learningpackage/model/LearningPackage.java) - `setPrice`; [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `requestLesson`.

### 12. How do error pages help?

I convert known exceptions into safe pages with useful status codes and recovery links. I keep private record contents and internal paths out of the user message.

**Show in code:** [GlobalExceptionHandler.java](../../src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java) - `handleValidation`; [GlobalExceptionHandler.java](../../src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java) - `handleStorage`; [ErrorPageSupport.java](../../src/main/java/com/drivingschool/common/exception/ErrorPageSupport.java) - `prepare`.

### 13. How does payment confirmation work?

I first save an enrollment awaiting payment without granting lessons. I let an admin confirm received payment, then start validity and grant the package lesson count.

**Show in code:** [EnrollmentService.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) - `enroll`; [EnrollmentService.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) - `markPaid`; [EnrollmentService.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) - `activateLessons`.

**Show in browser:** Student 8 > My enrollments > Payment instructions; admin > Awaiting payment.

### 14. Why can students register freely?

I create a new student as Active so they can log in and choose a package. I still require a paid usable enrollment before booking, and admins can manage account status.

**Show in code:** [AuthService.java](../../src/main/java/com/drivingschool/auth/service/AuthService.java) - `registerStudent`; [PaidEnrollmentGuard.java](../../src/main/java/com/drivingschool/booking/service/PaidEnrollmentGuard.java) - `requirePaid`.

**Show in browser:** Register > Log in > Packages.

### 15. How is data seeded?

I enable the loader only with the demo setting and a fresh empty folder. I add missing records in dependency order, preserve handled requests and spent balances, and calculate ratings from approved reviews.

**Show in code:** [DemoDataSeedRunner.java](../../src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java) - `run`; [DemoDataSeedRunner.java](../../src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java) - `syncInstructorRatings`; [DemoDataSeedRunnerTest.java](../../src/test/java/com/drivingschool/sample/DemoDataSeedRunnerTest.java) - `seedCanRunTwiceWithoutDuplicateRecords`.

### 16. How was the Tailwind UI built?

I use Tailwind utilities and shared CSS classes, then build a local stylesheet with npm run build:css. I use white account cards on soft grey, dark navigation and amber actions, while public photos stay cinematic.

**Show in code:** [package.json](../../package.json) - `build:css`; [app.css](../../src/main/resources/static/css/app.css) - `body.ds-account-page`.

### 17. What do reports count?

I count saved records and include only Paid enrollment amounts as revenue. I group revenue by enrollment month because I do not store a separate payment timestamp.

**Show in code:** [AdminReportService.java](../../src/main/java/com/drivingschool/report/service/AdminReportService.java) - `business`; [AdminReportService.java](../../src/main/java/com/drivingschool/report/service/AdminReportService.java) - `money`.

**Show in browser:** Admin > Reports.

### 18. How many tests do we have?

I can show the recorded final clean verify result of 694 tests with zero failures, errors or skips. I separate automated tests from browser checks and their stated limits.

**Show in code:** [pom.xml](../../pom.xml) - `build dependency`; [AdminReportServiceTest.java](../../src/test/java/com/drivingschool/review/service/AdminReportServiceTest.java) - `businessCsvUsesNamesHeadersTotalsAndBom`.

### 19. What are the custom ordering algorithms?

I use hand-written bubble sort to order instructors by a chosen detail. I use a linked FIFO queue for pending unassigned requests and rebuild its order from saved creation times.

**Show in code:** [InstructorSorter.java](../../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java) - `sort`; [BookingRequestQueue.java](../../src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java) - `BookingRequestQueue`.

### 20. What would I improve?

I would add a relational database with transactions and stronger audit records. I would then add real payment and email integrations with proper failure handling.

**Show in code:** [FileRepository.java](../../src/main/java/com/drivingschool/common/repository/FileRepository.java) - `writeRecords`; [PasswordController.java](../../src/main/java/com/drivingschool/auth/controller/PasswordController.java) - `requestReset`.

## IT26101135: Accounts, students and integration

[Individual guide and diagram](IT26101135.md).

### 1. What is my module responsible for?

In our system, I use separate classes for the accounts, students and integration module. I keep web requests, rules and storage in separate classes.

**Show in code:** [StudentProfileController.java](../../src/main/java/com/drivingschool/student/controller/StudentProfileController.java) - `StudentProfileController`; [StudentProfileService.java](../../src/main/java/com/drivingschool/student/service/StudentProfileService.java) - `StudentProfileService`.

### 2. Where are Create and Read?

I create through AuthService using registerStudent and read through StudentProfileService using getOwnProfile. I let repositories handle files after service checks.

**Show in code:** [AuthService.java](../../src/main/java/com/drivingschool/auth/service/AuthService.java) - `registerStudent`; [StudentProfileService.java](../../src/main/java/com/drivingschool/student/service/StudentProfileService.java) - `getOwnProfile`.

### 3. Where are Update and Delete?

I update through StudentProfileService using updateOwnProfile and delete through StudentAccountService using deleteOwnAccount. I apply deletion rules instead of breaking saved lesson or payment history.

**Show in code:** [StudentProfileService.java](../../src/main/java/com/drivingschool/student/service/StudentProfileService.java) - `updateOwnProfile`; [StudentAccountService.java](../../src/main/java/com/drivingschool/student/service/StudentAccountService.java) - `deleteOwnAccount`.

### 4. Where did I use encapsulation?

I keep Student fields private and use checked methods to control changes. I use setPhone to reject invalid values instead of trusting a form.

**Show in code:** [Student.java](../../src/main/java/com/drivingschool/student/model/Student.java) - `setPhone`.

### 5. Where did I use abstraction?

I use abstract User for common account details. I require each account child to supply its own role and dashboard path.

**Show in code:** [User.java](../../src/main/java/com/drivingschool/auth/model/User.java) - `getRole`; [User.java](../../src/main/java/com/drivingschool/auth/model/User.java) - `getDashboardPath`.

### 6. Where did I use inheritance?

I make Student extend User so it reuses account name, email and password-hash rules. I use Admin and InstructorAccount as other User children.

**Show in code:** [Student.java](../../src/main/java/com/drivingschool/student/model/Student.java) - `class declaration`; [Admin.java](../../src/main/java/com/drivingschool/auth/model/Admin.java) - `class declaration`; [InstructorAccount.java](../../src/main/java/com/drivingschool/auth/model/InstructorAccount.java) - `class declaration`.

### 7. Where did I use polymorphism?

I call getDashboardPath through a User reference after login. I get the student, instructor or admin destination from the actual account class.

**Show in code:** [AuthController.java](../../src/main/java/com/drivingschool/auth/controller/AuthController.java) - `login`; [User.java](../../src/main/java/com/drivingschool/auth/model/User.java) - `getDashboardPath`.

### 8. How did I use exception handling as the fifth course topic?

I use DuplicateRecordException when this module cannot accept a requested change. I let the shared handler explain it safely instead of exposing file details.

**Show in code:** [StudentProfileService.java](../../src/main/java/com/drivingschool/student/service/StudentProfileService.java) - `ensureAvailableEmail`; [GlobalExceptionHandler.java](../../src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java) - `handleDuplicate`.

### 9. How do I explain Single Responsibility and Open/Closed?

I keep profile HTTP handling in StudentProfileController and rules in StudentProfileService. I can add a User child without rewriting existing children, but a new role still needs enum and access changes.

**Show in code:** [StudentProfileController.java](../../src/main/java/com/drivingschool/student/controller/StudentProfileController.java) - `updateProfile`; [User.java](../../src/main/java/com/drivingschool/auth/model/User.java) - `class declaration`.

### 10. How do I explain Liskov, Interface Segregation and Dependency Inversion?

I can use Student as User while keeping a valid role and destination. I give profile pages the smaller StudentProfiles contract. I inject that interface into StudentProfileController rather than constructing its service there.

**Show in code:** [Student.java](../../src/main/java/com/drivingschool/student/model/Student.java) - `getRole`; [StudentProfiles.java](../../src/main/java/com/drivingschool/student/api/StudentProfiles.java) - `getOwnProfile`; [StudentProfileController.java](../../src/main/java/com/drivingschool/student/controller/StudentProfileController.java) - `StudentProfileController`.

### 11. How do I explain Registration, login and sessions?

I register a student as Active with a unique email and hashed password. I create a session only after the password and account status pass. I keep payment eligibility separate from account registration.

**Show in code:** [AuthService.java](../../src/main/java/com/drivingschool/auth/service/AuthService.java) - `registerStudent`; [AuthSessionService.java](../../src/main/java/com/drivingschool/auth/service/AuthSessionService.java) - `loginWithResult`.

### 12. What should I demonstrate honestly?

I can demonstrate the accounts, students and integration pages using disposable seeded data. I use server-side rules and tests as evidence, and mark an unconfirmed demonstration NEEDS CHECK.

**Show in code:** [StudentProfileService.java](../../src/main/java/com/drivingschool/student/service/StudentProfileService.java) - `StudentProfileService`.

**Show in browser:** Open Register from the login page > Return using Log in > Log in as student1 > Open Account settings > Open Change password without submitting.

## IT26101133: Instructors, availability and bubble sort

[Individual guide and diagram](IT26101133.md).

### 1. What is my module responsible for?

In our system, I use separate classes for the instructors, availability and bubble sort module. I keep web requests, rules and storage in separate classes.

**Show in code:** [InstructorAvailabilityController.java](../../src/main/java/com/drivingschool/instructor/controller/InstructorAvailabilityController.java) - `InstructorAvailabilityController`; [InstructorService.java](../../src/main/java/com/drivingschool/instructor/service/InstructorService.java) - `InstructorService`.

### 2. Where are Create and Read?

I create through InstructorAccountService using create and read through InstructorService using sortInstructors. I let repositories handle files after service checks.

**Show in code:** [InstructorAccountService.java](../../src/main/java/com/drivingschool/instructor/service/InstructorAccountService.java) - `create`; [InstructorService.java](../../src/main/java/com/drivingschool/instructor/service/InstructorService.java) - `sortInstructors`.

### 3. Where are Update and Delete?

I update through InstructorAccountService using update and delete through InstructorAccountService using delete. I apply deletion rules instead of breaking saved lesson or payment history.

**Show in code:** [InstructorAccountService.java](../../src/main/java/com/drivingschool/instructor/service/InstructorAccountService.java) - `update`; [InstructorAccountService.java](../../src/main/java/com/drivingschool/instructor/service/InstructorAccountService.java) - `delete`.

### 4. Where did I use encapsulation?

I keep Instructor fields private and use checked methods to control changes. I use setYearsOfExperience to reject invalid values instead of trusting a form.

**Show in code:** [Instructor.java](../../src/main/java/com/drivingschool/instructor/model/Instructor.java) - `setYearsOfExperience`.

### 5. Where did I use abstraction?

I use InstructorOrder to describe how two instructors are compared. I let the sorter use that contract rather than put every comparison inside the swap loop.

**Show in code:** [InstructorOrder.java](../../src/main/java/com/drivingschool/instructor/service/InstructorOrder.java) - `compare`; [InstructorSorter.java](../../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java) - `swapNeighbours`.

### 6. Where did I use inheritance?

I make InstructorRepository extend FileRepository for shared file operations. I keep Instructor as a separate profile because it does not extend the account User class.

**Show in code:** [InstructorRepository.java](../../src/main/java/com/drivingschool/instructor/repository/InstructorRepository.java) - `class declaration`; [Instructor.java](../../src/main/java/com/drivingschool/instructor/model/Instructor.java) - `class declaration`.

### 7. Where did I use polymorphism?

I call the same compare method with name, experience or rating comparison objects. I get different decisions while keeping the same bubble-sort passes.

**Show in code:** [InstructorSorter.java](../../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java) - `swapNeighbours`; [InstructorNameOrder.java](../../src/main/java/com/drivingschool/instructor/service/InstructorNameOrder.java) - `compare`; [InstructorExperienceOrder.java](../../src/main/java/com/drivingschool/instructor/service/InstructorExperienceOrder.java) - `compare`; [InstructorRatingOrder.java](../../src/main/java/com/drivingschool/instructor/service/InstructorRatingOrder.java) - `compare`.

### 8. How did I use exception handling as the fifth course topic?

I use DuplicateRecordException when this module cannot accept a requested change. I let the shared handler explain it safely instead of exposing file details.

**Show in code:** [InstructorScheduleService.java](../../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java) - `ensureNoOverlap`; [GlobalExceptionHandler.java](../../src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java) - `handleDuplicate`.

### 9. How do I explain Single Responsibility and Open/Closed?

I give InstructorSorter only the sorting job. I can add another InstructorOrder without rewriting the swap passes, although chooseOrder must still select the new option.

**Show in code:** [InstructorSorter.java](../../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java) - `sort`; [InstructorSorter.java](../../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java) - `chooseOrder`.

### 10. How do I explain Liskov, Interface Segregation and Dependency Inversion?

I let each InstructorOrder return a negative, zero or positive comparison result. I keep that interface to one compare method. I inject InstructorScheduleOperations into the availability controller.

**Show in code:** [InstructorNameOrder.java](../../src/main/java/com/drivingschool/instructor/service/InstructorNameOrder.java) - `compare`; [InstructorOrder.java](../../src/main/java/com/drivingschool/instructor/service/InstructorOrder.java) - `compare`; [InstructorAvailabilityController.java](../../src/main/java/com/drivingschool/instructor/controller/InstructorAvailabilityController.java) - `InstructorAvailabilityController`.

### 11. How do I explain Bubble sort, steps and complexity?

I compare neighbours and swap them when they are in the wrong order. For ascending [8, 3, 12], I get [3, 8, 12] after the first pass and stop after a pass without swaps. I have O(n squared) worst-case time and O(n) best-case time, plus O(n) space for the copied list.

**Show in code:** [InstructorSorter.java](../../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java) - `sort`; [InstructorSorter.java](../../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java) - `swapNeighbours`.

### 12. What should I demonstrate honestly?

I can demonstrate the instructors, availability and bubble sort pages using disposable seeded data. I use server-side rules and tests as evidence, and mark an unconfirmed demonstration NEEDS CHECK.

**Show in code:** [InstructorService.java](../../src/main/java/com/drivingschool/instructor/service/InstructorService.java) - `InstructorService`.

**Show in browser:** Open Instructors > Select Experience > Choose descending order > Click Sort > Open Packages.

## IT26101134: Bookings, FIFO and conflict rules

[Individual guide and diagram](IT26101134.md).

### 1. What is my module responsible for?

In our system, I use separate classes for the bookings, fifo and conflict rules module. I keep web requests, rules and storage in separate classes.

**Show in code:** [BookingController.java](../../src/main/java/com/drivingschool/booking/controller/BookingController.java) - `BookingController`; [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `BookingService`.

### 2. Where are Create and Read?

I create through BookingService using requestLesson and read through BookingService using getOwnBookings. I let repositories handle files after service checks.

**Show in code:** [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `requestLesson`; [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `getOwnBookings`.

### 3. Where are Update and Delete?

I update through BookingService using rescheduleOwnBooking and delete through BookingService using deleteCancelledBooking. I apply deletion rules instead of breaking saved lesson or payment history.

**Show in code:** [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `rescheduleOwnBooking`; [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `deleteCancelledBooking`.

### 4. Where did I use encapsulation?

I keep Booking fields private and use checked methods to control changes. I use Booking to reject invalid values instead of trusting a form.

**Show in code:** [Booking.java](../../src/main/java/com/drivingschool/booking/model/Booking.java) - `Booking`.

### 5. Where did I use abstraction?

I separate StudentBookingOperations from AdminBookingOperations. I describe list ordering through BookingOrder without exposing the queue nodes.

**Show in code:** [StudentBookingOperations.java](../../src/main/java/com/drivingschool/booking/service/StudentBookingOperations.java) - `class declaration`; [AdminBookingOperations.java](../../src/main/java/com/drivingschool/booking/service/AdminBookingOperations.java) - `class declaration`; [BookingOrder.java](../../src/main/java/com/drivingschool/booking/util/BookingOrder.java) - `order`.

### 6. Where did I use inheritance?

I make BookingRepository extend FileRepository for shared storage. I use a private linked Node inside the queue rather than claiming the queue inherits from Booking.

**Show in code:** [BookingRepository.java](../../src/main/java/com/drivingschool/booking/repository/BookingRepository.java) - `class declaration`; [BookingRequestQueue.java](../../src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java) - `class declaration`.

### 7. Where did I use polymorphism?

I call order through BookingOrder references for pending-request and lesson-time views. I remember that the pending view also filters requests, so those views do not have identical business meanings.

**Show in code:** [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `getAllBookings`; [PendingRequestOrder.java](../../src/main/java/com/drivingschool/booking/util/PendingRequestOrder.java) - `order`; [LessonTimeOrder.java](../../src/main/java/com/drivingschool/booking/util/LessonTimeOrder.java) - `order`.

### 8. How did I use exception handling as the fifth course topic?

I use ValidationException when this module cannot accept a requested change. I let the shared handler explain it safely instead of exposing file details.

**Show in code:** [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `parseFilterDate`; [GlobalExceptionHandler.java](../../src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java) - `handleValidation`.

### 9. How do I explain Single Responsibility and Open/Closed?

I keep conflict checks in BookingConflictChecker and waiting order in BookingRequestQueue. I can add another BookingOrder without rewriting the existing ordering classes, but the caller must choose the right view.

**Show in code:** [BookingConflictChecker.java](../../src/main/java/com/drivingschool/booking/service/BookingConflictChecker.java) - `conflictReason`; [BookingOrder.java](../../src/main/java/com/drivingschool/booking/util/BookingOrder.java) - `order`.

### 10. How do I explain Liskov, Interface Segregation and Dependency Inversion?

I preserve the shared save contract in BookingRepository. I separate student and admin booking contracts. I inject StudentBookingOperations into BookingController.

**Show in code:** [BookingRepository.java](../../src/main/java/com/drivingschool/booking/repository/BookingRepository.java) - `save`; [StudentBookingOperations.java](../../src/main/java/com/drivingschool/booking/service/StudentBookingOperations.java) - `class declaration`; [BookingController.java](../../src/main/java/com/drivingschool/booking/controller/BookingController.java) - `BookingController`.

### 11. How do I explain FIFO, enqueue, dequeue and complexity?

I enqueue at the tail and dequeue the oldest head, each in O(1) time. I use FIFO so later unassigned requests do not jump ahead. I rebuild the queue with stable insertion sorting, which can take O(n squared), and position lookup takes O(n).

**Show in code:** [BookingRequestQueue.java](../../src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java) - `enqueue`; [BookingRequestQueue.java](../../src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java) - `dequeue`; [BookingRequestQueue.java](../../src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java) - `positionOf`; [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `processNext`.

### 12. What should I demonstrate honestly?

I can demonstrate the bookings, fifo and conflict rules pages using disposable seeded data. I use server-side rules and tests as evidence, and mark an unconfirmed demonstration NEEDS CHECK.

**Show in code:** [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `BookingService`.

**Show in browser:** Open My lessons as student1 > Open the first booking details > Inspect its waiting-list position > Click Back to bookings > Open History.

## IT26101132: Vehicles and seed data

[Individual guide and diagram](IT26101132.md).

### 1. What is my module responsible for?

In our system, I use separate classes for the vehicles and seed data module. I keep web requests, rules and storage in separate classes.

**Show in code:** [VehicleController.java](../../src/main/java/com/drivingschool/vehicle/controller/VehicleController.java) - `VehicleController`; [VehicleService.java](../../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) - `VehicleService`.

### 2. Where are Create and Read?

I create through VehicleService using createVehicle and read through VehicleService using getAllVehicles. I let repositories handle files after service checks.

**Show in code:** [VehicleService.java](../../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) - `createVehicle`; [VehicleService.java](../../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) - `getAllVehicles`.

### 3. Where are Update and Delete?

I update through VehicleService using updateVehicle and delete through VehicleService using deleteVehicle. I apply deletion rules instead of breaking saved lesson or payment history.

**Show in code:** [VehicleService.java](../../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) - `updateVehicle`; [VehicleService.java](../../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) - `deleteVehicle`.

### 4. Where did I use encapsulation?

I keep Vehicle fields private and use checked methods to control changes. I use setNextServiceDate to reject invalid values instead of trusting a form.

**Show in code:** [Vehicle.java](../../src/main/java/com/drivingschool/vehicle/model/Vehicle.java) - `setNextServiceDate`.

### 5. Where did I use abstraction?

I expose fleet actions through VehicleOperations. I use VehicleStore for storage tasks instead of putting JSON file code in the controller.

**Show in code:** [VehicleOperations.java](../../src/main/java/com/drivingschool/vehicle/api/VehicleOperations.java) - `class declaration`; [VehicleStore.java](../../src/main/java/com/drivingschool/vehicle/api/VehicleStore.java) - `class declaration`; [VehicleController.java](../../src/main/java/com/drivingschool/vehicle/controller/VehicleController.java) - `VehicleController`.

### 6. Where did I use inheritance?

I make VehicleRepository extend FileRepository to reuse storage, validation and ID counters. I do not copy the JSON writer into Vehicle.

**Show in code:** [VehicleRepository.java](../../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java) - `class declaration`.

### 7. Where did I use polymorphism?

I use a VehicleStore reference in VehicleService with VehicleRepository as the actual object. I call its storage methods while keeping the parent file rules.

**Show in code:** [VehicleService.java](../../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) - `VehicleService`; [VehicleRepository.java](../../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java) - `save`.

### 8. How did I use exception handling as the fifth course topic?

I use DuplicateRecordException when this module cannot accept a requested change. I let the shared handler explain it safely instead of exposing file details.

**Show in code:** [VehicleService.java](../../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) - `ensureRegistrationNumberIsUnique`; [GlobalExceptionHandler.java](../../src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java) - `handleDuplicate`.

### 9. How do I explain Single Responsibility and Open/Closed?

I keep vehicle rules in VehicleService and file writing in VehicleRepository. I can add another typed repository without rewriting the shared FileRepository writer.

**Show in code:** [VehicleService.java](../../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) - `deleteVehicle`; [VehicleRepository.java](../../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java) - `class declaration`; [FileRepository.java](../../src/main/java/com/drivingschool/common/repository/FileRepository.java) - `writeRecords`.

### 10. How do I explain Liskov, Interface Segregation and Dependency Inversion?

I delegate VehicleRepository updates to the same parent checks. I give the controller the VehicleOperations fleet contract. I inject that interface instead of constructing VehicleService in the controller.

**Show in code:** [VehicleRepository.java](../../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java) - `update`; [VehicleOperations.java](../../src/main/java/com/drivingschool/vehicle/api/VehicleOperations.java) - `class declaration`; [VehicleController.java](../../src/main/java/com/drivingschool/vehicle/controller/VehicleController.java) - `VehicleController`.

### 11. How do I explain Expiry warnings and safe seeding?

I show separate expired or overdue labels and warnings for dates within thirty days. I seed missing records without refilling spent lessons. I use fictional phone and NIC patterns accepted by the app, not real allocated identities.

**Show in code:** [Vehicle.java](../../src/main/java/com/drivingschool/vehicle/model/Vehicle.java) - `isInsuranceWarning`; [Vehicle.java](../../src/main/java/com/drivingschool/vehicle/model/Vehicle.java) - `isServiceWarning`; [DemoDataSeedRunner.java](../../src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java) - `run`.

### 12. What should I demonstrate honestly?

I can demonstrate the vehicles and seed data pages using disposable seeded data. I use server-side rules and tests as evidence, and mark an unconfirmed demonstration NEEDS CHECK.

**Show in code:** [VehicleService.java](../../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) - `VehicleService`.

**Show in browser:** Open Vehicles as admin > Open Edit vehicle for the Alto > Return through Vehicles in the sidebar > Open Edit vehicle for the Honda Dio > Return to Vehicles and inspect warnings.

## IT26101136: Packages, enrollment and payment

[Individual guide and diagram](IT26101136.md).

### 1. What is my module responsible for?

In our system, I use separate classes for the packages, enrollment and payment module. I keep web requests, rules and storage in separate classes.

**Show in code:** [LearningPackageController.java](../../src/main/java/com/drivingschool/learningpackage/controller/LearningPackageController.java) - `LearningPackageController`; [LearningPackageService.java](../../src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java) - `LearningPackageService`.

### 2. Where are Create and Read?

I create through LearningPackageService using createPackage and read through LearningPackageService using listPackages. I let repositories handle files after service checks.

**Show in code:** [LearningPackageService.java](../../src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java) - `createPackage`; [LearningPackageService.java](../../src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java) - `listPackages`.

### 3. Where are Update and Delete?

I update through LearningPackageService using updatePackage and delete through LearningPackageService using deletePackage. I apply deletion rules instead of breaking saved lesson or payment history.

**Show in code:** [LearningPackageService.java](../../src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java) - `updatePackage`; [LearningPackageService.java](../../src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java) - `deletePackage`.

### 4. Where did I use encapsulation?

I keep LearningPackage fields private and use checked methods to control changes. I use setPrice to reject invalid values instead of trusting a form.

**Show in code:** [LearningPackage.java](../../src/main/java/com/drivingschool/learningpackage/model/LearningPackage.java) - `setPrice`.

### 5. Where did I use abstraction?

I use LearningPackageCatalog for package-reading tasks and EnrollmentOperations for student enrollment tasks. I keep admin payment tasks in EnrollmentAdministration.

**Show in code:** [LearningPackageCatalog.java](../../src/main/java/com/drivingschool/learningpackage/service/LearningPackageCatalog.java) - `class declaration`; [EnrollmentOperations.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentOperations.java) - `class declaration`; [EnrollmentAdministration.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentAdministration.java) - `class declaration`.

### 6. Where did I use inheritance?

I make LearningPackageRepository extend FileRepository for storage rules. I do not make Enrollment extend LearningPackage because they are different records.

**Show in code:** [LearningPackageRepository.java](../../src/main/java/com/drivingschool/learningpackage/repository/LearningPackageRepository.java) - `class declaration`; [Enrollment.java](../../src/main/java/com/drivingschool/learningpackage/model/Enrollment.java) - `class declaration`.

### 7. Where did I use polymorphism?

I let LearningPackage and Enrollment implement the same LearningRecord display contract. I can call their display methods through that contract, although current pages use presentation helpers for resolved package names.

**Show in code:** [LearningRecord.java](../../src/main/java/com/drivingschool/learningpackage/model/LearningRecord.java) - `getDisplayName`; [LearningPackage.java](../../src/main/java/com/drivingschool/learningpackage/model/LearningPackage.java) - `getDisplayName`; [Enrollment.java](../../src/main/java/com/drivingschool/learningpackage/model/Enrollment.java) - `getDisplayName`.

### 8. How did I use exception handling as the fifth course topic?

I use DuplicateRecordException when this module cannot accept a requested change. I let the shared handler explain it safely instead of exposing file details.

**Show in code:** [LearningPackageService.java](../../src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java) - `ensureUniqueName`; [GlobalExceptionHandler.java](../../src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java) - `handleDuplicate`.

### 9. How do I explain Single Responsibility and Open/Closed?

I build read-only labels in EnrollmentPresentationService while EnrollmentService handles payment rules. I can add another LearningRecord without rewriting existing records, although a page must choose to use it.

**Show in code:** [EnrollmentPresentationService.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentPresentationService.java) - `addStudent`; [LearningRecord.java](../../src/main/java/com/drivingschool/learningpackage/model/LearningRecord.java) - `getDisplayName`.

### 10. How do I explain Liskov, Interface Segregation and Dependency Inversion?

I preserve the shared storage contract in LearningPackageRepository. I separate student EnrollmentOperations from admin EnrollmentAdministration. I inject EnrollmentOperations into EnrollmentController.

**Show in code:** [LearningPackageRepository.java](../../src/main/java/com/drivingschool/learningpackage/repository/LearningPackageRepository.java) - `save`; [EnrollmentAdministration.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentAdministration.java) - `class declaration`; [EnrollmentController.java](../../src/main/java/com/drivingschool/learningpackage/controller/EnrollmentController.java) - `EnrollmentController`.

### 11. How do I explain Payment confirmation and packages?

I recheck the waiting request, student and course inside the shared account lock. I activate lessons and save the Paid receipt, with validity starting on confirmation day. I do not claim a transaction because those are separate file updates.

**Show in code:** [EnrollmentService.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) - `markPaid`; [EnrollmentService.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) - `activateLessons`; [AccountWriteCoordinator.java](../../src/main/java/com/drivingschool/auth/util/AccountWriteCoordinator.java) - `execute`.

### 12. What should I demonstrate honestly?

I can demonstrate the packages, enrollment and payment pages using disposable seeded data. I use server-side rules and tests as evidence, and mark an unconfirmed demonstration NEEDS CHECK.

**Show in code:** [LearningPackageService.java](../../src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java) - `LearningPackageService`.

**Show in browser:** Open My enrollments as student8 > Open Payment instructions > Return to My enrollments > Open Packages > Open View package.

## IT26101137: Reviews, ratings and reports

[Individual guide and diagram](IT26101137.md).

### 1. What is my module responsible for?

In our system, I use separate classes for the reviews, ratings and reports module. I keep web requests, rules and storage in separate classes.

**Show in code:** [ReviewController.java](../../src/main/java/com/drivingschool/review/controller/ReviewController.java) - `ReviewController`; [ReviewService.java](../../src/main/java/com/drivingschool/review/service/ReviewService.java) - `ReviewService`.

### 2. Where are Create and Read?

I create through ReviewService using createReview and read through ReviewService using getVisibleReviews. I let repositories handle files after service checks.

**Show in code:** [ReviewService.java](../../src/main/java/com/drivingschool/review/service/ReviewService.java) - `createReview`; [ReviewService.java](../../src/main/java/com/drivingschool/review/service/ReviewService.java) - `getVisibleReviews`.

### 3. Where are Update and Delete?

I update through ReviewService using updateOwnReview and delete through ReviewService using deleteOwnReview. I apply deletion rules instead of breaking saved lesson or payment history.

**Show in code:** [ReviewService.java](../../src/main/java/com/drivingschool/review/service/ReviewService.java) - `updateOwnReview`; [ReviewService.java](../../src/main/java/com/drivingschool/review/service/ReviewService.java) - `deleteOwnReview`.

### 4. Where did I use encapsulation?

I keep Review fields private and use checked methods to control changes. I use Review to reject invalid values instead of trusting a form.

**Show in code:** [Review.java](../../src/main/java/com/drivingschool/review/model/Review.java) - `Review`.

### 5. Where did I use abstraction?

I expose student feedback through ReviewOperations and moderation through ReviewModeration. I expose report tables through ReportCatalog so the controller does not calculate them.

**Show in code:** [ReviewOperations.java](../../src/main/java/com/drivingschool/review/service/ReviewOperations.java) - `class declaration`; [ReviewModeration.java](../../src/main/java/com/drivingschool/review/service/ReviewModeration.java) - `class declaration`; [ReportCatalog.java](../../src/main/java/com/drivingschool/report/service/ReportCatalog.java) - `business`.

### 6. Where did I use inheritance?

I make ReviewRepository extend FileRepository for shared file operations. I keep Review as a model implementing Identifiable rather than a child of a service.

**Show in code:** [ReviewRepository.java](../../src/main/java/com/drivingschool/review/repository/ReviewRepository.java) - `class declaration`; [Review.java](../../src/main/java/com/drivingschool/review/model/Review.java) - `class declaration`.

### 7. Where did I use polymorphism?

I call business through the ReportCatalog reference in the report controller. I receive the real AdminReportService object supplied by Spring.

**Show in code:** [AdminReportController.java](../../src/main/java/com/drivingschool/report/controller/AdminReportController.java) - `show`; [AdminReportService.java](../../src/main/java/com/drivingschool/report/service/AdminReportService.java) - `business`.

### 8. How did I use exception handling as the fifth course topic?

I use DuplicateRecordException when this module cannot accept a requested change. I let the shared handler explain it safely instead of exposing file details.

**Show in code:** [ReviewService.java](../../src/main/java/com/drivingschool/review/service/ReviewService.java) - `ensureNoDuplicate`; [GlobalExceptionHandler.java](../../src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java) - `handleDuplicate`.

### 9. How do I explain Single Responsibility and Open/Closed?

I calculate tables in AdminReportService and write downloads in AdminReportController. I can supply another ReportCatalog implementation without putting calculation rules in the controller.

**Show in code:** [AdminReportService.java](../../src/main/java/com/drivingschool/report/service/AdminReportService.java) - `business`; [AdminReportController.java](../../src/main/java/com/drivingschool/report/controller/AdminReportController.java) - `export`; [ReportCatalog.java](../../src/main/java/com/drivingschool/report/service/ReportCatalog.java) - `business`.

### 10. How do I explain Liskov, Interface Segregation and Dependency Inversion?

I preserve the FileRepository contract in ReviewRepository. I split ordinary review operations from ReviewModeration. I inject ReviewOperations and ReportCatalog into their controllers.

**Show in code:** [ReviewRepository.java](../../src/main/java/com/drivingschool/review/repository/ReviewRepository.java) - `save`; [ReviewModeration.java](../../src/main/java/com/drivingschool/review/service/ReviewModeration.java) - `class declaration`; [ReviewController.java](../../src/main/java/com/drivingschool/review/controller/ReviewController.java) - `ReviewController`; [AdminReportController.java](../../src/main/java/com/drivingschool/report/controller/AdminReportController.java) - `AdminReportController`.

### 11. How do I explain Paid-only revenue and business CSV?

I add captured enrollment amounts only when isPaid is true and format them with two decimals. I write a UTF-8 BOM, a business header, named sections and totals. I quote cells, neutralize formula prefixes, and offer full and section downloads.

**Show in code:** [AdminReportService.java](../../src/main/java/com/drivingschool/report/service/AdminReportService.java) - `business`; [AdminReportController.java](../../src/main/java/com/drivingschool/report/controller/AdminReportController.java) - `export`; [AdminReportController.java](../../src/main/java/com/drivingschool/report/controller/AdminReportController.java) - `csv`.

### 12. What should I demonstrate honestly?

I can demonstrate the reviews, ratings and reports pages using disposable seeded data. I use server-side rules and tests as evidence, and mark an unconfirmed demonstration NEEDS CHECK.

**Show in code:** [ReviewService.java](../../src/main/java/com/drivingschool/review/service/ReviewService.java) - `ReviewService`.

**Show in browser:** Open Reports as admin > Download a section CSV > Open Print report > Cancel printing after inspecting it > Download full CSV.

## Tricky what-if questions

### 1. What if two admins confirm the same payment?

I serialize the operation and reload the request inside the shared account lock. I grant lessons once; the second attempt sees it was already handled. I only have this protection in one JVM, not across app instances.

**Show in code:** [EnrollmentService.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) - `markPaid`; [EnrollmentService.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) - `requireAwaiting`; [AccountWriteCoordinator.java](../../src/main/java/com/drivingschool/auth/util/AccountWriteCoordinator.java) - `execute`.

### 2. What if a student double-clicks Book?

I disable an accepted submit in JavaScript and recheck conflicts and commitments under the shared booking lock. I reject a repeated unavailable overlapping time, but I do not have a general idempotency-key API.

**Show in code:** [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `requestLesson`; [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `requireCommitmentRoom`.

### 3. What if a vehicle goes into maintenance after assignment?

I save the status but do not automatically cancel all existing bookings. I recheck conflicts when assigning or accepting, while completion is not a fresh vehicle-inspection check. I would ask the admin to reassign or cancel the affected lesson.

**Show in code:** [VehicleService.java](../../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) - `updateVehicle`; [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `acceptAssignedLesson`; [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `completeAssignedLesson`.

### 4. What if a file is corrupted?

I use strict reads for important decisions, so damaged data raises StorageException instead of being silently trusted. I show a safe error page and would repair or restore a trusted copy before continuing.

**Show in code:** [FileRepository.java](../../src/main/java/com/drivingschool/common/repository/FileRepository.java) - `readAllStrict`; [GlobalExceptionHandler.java](../../src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java) - `handleStorage`.

### 5. What if an instructor adds leave on a confirmed lesson date?

I block leave over confirmed lessons or assigned pending lessons. I ask an administrator to resolve those lessons first. I use the shared app lock for schedule changes and booking writes.

**Show in code:** [InstructorScheduleService.java](../../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java) - `validateLeave`; [InstructorScheduleService.java](../../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java) - `deleteAvailability`; [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `acceptAssignedLesson`.

### 6. What if someone changes an ID in the URL?

I check role and record ownership instead of trusting the URL. I return a forbidden, missing-record or validation response when access is not allowed.

**Show in code:** [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `getBookingDetails`; [EnrollmentService.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) - `requireOwnEnrollment`.

### 7. What if the app restarts?

I keep records and counters in files and rebuild the queue from them. I lose ordinary in-memory sessions and reset codes. I do not use seeding to overwrite existing decisions.

**Show in code:** [BookingRequestQueue.java](../../src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java) - `BookingRequestQueue`; [PasswordManagementService.java](../../src/main/java/com/drivingschool/auth/service/PasswordManagementService.java) - `createResetToken`; [DemoDataSeedRunner.java](../../src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java) - `run`.

### 8. What if a student cancels less than 24 hours before a lesson?

I reject the change with the default twenty-four-hour notice rule. I leave the booking intact; the admin has a separate cancellation operation for appropriate cases.

**Show in code:** [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `cancelOwnBooking`; [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `forceCancel`; [BookingCutoffTest.java](../../src/test/java/com/drivingschool/booking/service/BookingCutoffTest.java) - `class declaration`.

### 9. What if someone completes a future lesson?

I do not give students the completion operation. I block completion and no-show before lesson end with the default attendance setting. I show the lesson end time and disable attendance buttons until then. I allow an explicit override only for an isolated preview.

**Show in code:** [BookingService.java](../../src/main/java/com/drivingschool/booking/service/BookingService.java) - `completeAssignedLesson`; [Booking.java](../../src/main/java/com/drivingschool/booking/model/Booking.java) - `complete`; [AttendancePresentationAdvice.java](../../src/main/java/com/drivingschool/booking/controller/AttendancePresentationAdvice.java) - `isAvailable`.

### 10. What if the package is unpaid, expired or used up?

I refuse booking when a paid receipt cannot supply usable lessons or cover the lesson date. I allow renewal of an expired or used-up package only with no open lesson and one payment request at a time. I keep old receipts and recheck eligibility when an administrator confirms payment.

**Show in code:** [PaidEnrollmentGuard.java](../../src/main/java/com/drivingschool/booking/service/PaidEnrollmentGuard.java) - `problemForLesson`; [EnrollmentService.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) - `renewalProblem`; [EnrollmentService.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) - `markPaid`.

### 11. What if the course price changes later?

I keep the amount captured on an existing enrollment. I use that amount for revenue, so a catalog price change does not rewrite old paid amounts.

**Show in code:** [Enrollment.java](../../src/main/java/com/drivingschool/learningpackage/model/Enrollment.java) - `getAmount`; [AdminReportService.java](../../src/main/java/com/drivingschool/report/service/AdminReportService.java) - `business`.

### 12. What if a form has no CSRF token?

I reject the unsafe request before the controller changes data. I ask the user to reload the form rather than disabling the check.

**Show in code:** [CsrfFilter.java](../../src/main/java/com/drivingschool/auth/filter/CsrfFilter.java) - `doFilterInternal`; [CsrfFilterTest.java](../../src/test/java/com/drivingschool/auth/filter/CsrfFilterTest.java) - `class declaration`.

### 13. What if someone deletes a referenced vehicle or package?

I refuse deletions that would break lesson or enrollment references. I use active or availability status where appropriate instead of deleting history.

**Show in code:** [VehicleService.java](../../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) - `deleteVehicle`; [LearningPackageService.java](../../src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java) - `deletePackage`.

### 14. What if a review is edited or hidden?

I return an edited student review to pending approval. I calculate averages from approved reviews, so pending or hidden comments do not count.

**Show in code:** [ReviewService.java](../../src/main/java/com/drivingschool/review/service/ReviewService.java) - `updateOwnReview`; [ReviewService.java](../../src/main/java/com/drivingschool/review/service/ReviewService.java) - `recalculateRating`; [AdminReportService.java](../../src/main/java/com/drivingschool/report/service/AdminReportService.java) - `business`.

### 15. What if a crash occurs between balance and receipt writes?

I do not have rollback across those files, so partial state is possible. I would inspect the affected records rather than pretend the in-process lock is a transaction.

**Show in code:** [EnrollmentService.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) - `markPaid`; [FileRepository.java](../../src/main/java/com/drivingschool/common/repository/FileRepository.java) - `writeRecords`.

## Group questions

### 1. Who did what?

I use member headers to explain the agreed module split, with integration under IT26101135. I do not treat a header as proof that only one person edited a file.

**Show in code:** [AuthController.java](../../src/main/java/com/drivingschool/auth/controller/AuthController.java) - `class declaration`; [InstructorSorter.java](../../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java) - `sort`; [BookingRequestQueue.java](../../src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java) - `enqueue`; [DemoDataSeedRunner.java](../../src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java) - `run`; [EnrollmentService.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) - `markPaid`; [AdminReportController.java](../../src/main/java/com/drivingschool/report/controller/AdminReportController.java) - `export`.

### 2. How did we use Git and pull requests?

NEEDS CHECK: I can show local merge subjects mentioning PR 36 and PR 32, but cannot confirm every review or author action from code. I would ask our lead to verify the exact team workflow before describing it as our history.

**Show in code:** [GIT_BRANCH_PLAN.md](../../docs/GIT_BRANCH_PLAN.md) - `planned workflow`.

### 3. How did we test together?

I can show the recorded final build, JUnit and MockMvc tests, and isolated browser evidence. I need the team to confirm who performed each rehearsal before claiming a personal contribution.

**Show in code:** [AdminReportServiceTest.java](../../src/test/java/com/drivingschool/review/service/AdminReportServiceTest.java) - `class declaration`; [AuthWebIntegrationTest.java](../../src/test/java/com/drivingschool/auth/AuthWebIntegrationTest.java) - `class declaration`.

### 4. How did we handle merge conflicts?

NEEDS CHECK: I can see a local merge subject about resolving instructor contracts. I cannot infer who resolved each conflict or the discussions, so I would verify that with the team.

**Show in code:** [MERGE_ANALYSIS.md](../../docs/MERGE_ANALYSIS.md) - `historical analysis`.

### 5. What was the hardest bug?

NEEDS CHECK: I cannot know which bug the team found hardest without their account. I can explain the tested future-completion and payment-concurrency risks and show the current guards.

**Show in code:** [Booking.java](../../src/main/java/com/drivingschool/booking/model/Booking.java) - `complete`; [EnrollmentService.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) - `requireAwaiting`; [EnrollmentPaymentServiceTest.java](../../src/test/java/com/drivingschool/learningpackage/service/EnrollmentPaymentServiceTest.java) - `class declaration`.

## Questions I must not bluff

- **Payments:** I confirm payments manually after the school checks receipt information. I do not claim a gateway or bank reconciliation. **Show in code:** [EnrollmentService.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) - `markPaid`.
- **Email and receipts:** I do not send email or upload receipts. I display contact instructions only when configured. **Show in code:** [EnrollmentController.java](../../src/main/java/com/drivingschool/learningpackage/controller/EnrollmentController.java) - `payment`.
- **Password recovery:** I use an in-memory reset code shown locally. I do not claim verified email ownership, reset-email delivery or production-ready recovery. **Show in code:** [PasswordController.java](../../src/main/java/com/drivingschool/auth/controller/PasswordController.java) - `requestReset`; [PasswordManagementService.java](../../src/main/java/com/drivingschool/auth/service/PasswordManagementService.java) - `createResetToken`.
- **Storage and locking:** I use files and locks inside one JVM. I do not claim multi-file transactions, cross-process locking or database rollback. **Show in code:** [FileRepository.java](../../src/main/java/com/drivingschool/common/repository/FileRepository.java) - `writeRecords`; [AccountWriteCoordinator.java](../../src/main/java/com/drivingschool/auth/util/AccountWriteCoordinator.java) - `execute`.
- **Roles:** I explain four user experiences, but only three stored roles. I do not invent a Visitor enum. **Show in code:** [Role.java](../../src/main/java/com/drivingschool/auth/model/Role.java) - `class declaration`.
- **Reports:** I group revenue by enrollment month because payment dates are not stored. I do not claim an atomic multi-store report snapshot. **Show in code:** [AdminReportService.java](../../src/main/java/com/drivingschool/report/service/AdminReportService.java) - `business`.
- **Seed and claims:** I use fictional identities and syntax-only phone/NIC patterns. I do not claim real accreditation, licences, pass rates or real student feedback. **Show in code:** [DemoDataSeedRunner.java](../../src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java) - `seedAccounts`.
- **Team history and coverage:** I mark unverified PR reviews, conflicts and personal contributions NEEDS CHECK. I do not say passing tests prove every browser state or complete WCAG compliance. **Show in code:** [GIT_BRANCH_PLAN.md](../../docs/GIT_BRANCH_PLAN.md) - `planned workflow`.

## Repository-history evidence and limits

Read-only local history contains merge subjects for PR #36 (041150f), PR #32 (392050d), and instructor-contract integration (3914174). These subjects do not prove review discussions, approvals, current PR publication or individual authorship. No Git write command was performed during this task.
