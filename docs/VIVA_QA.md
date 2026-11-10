# Viva questions and answers

## Batch C display rules, 10 November 2026

The [visual review](BATCH_C_REVIEW.md) and regenerated [speaking pack](viva-2026/README.md)
cover 694 tests. Earlier batch notes below are retained as history.

**Why is Complete disabled before a lesson ends?**
In our system, I show the lesson end time beside the attendance buttons.
I use the same clock and attendance setting as the server, so the page reflects the existing rule.
I ask the instructor to refresh after that time; the page does not update itself automatically.
The explicit early-attendance preview setting enables the controls only for that isolated run.

Show in code: `booking/controller/AttendancePresentationAdvice.java`, `isAvailable` and `availableAt`;
`booking/model/Booking.java`, `complete` and `markNoShow`; `common/components/attendance-time.jspf`.
Show in browser: Instructor 4 > Dashboard > future confirmed lesson; Admin > Bookings > Manage lesson.

**Did the rating calculation change?**
In our system, I keep the saved average and show one decimal on the page.
For example, a stored average of 4.33 appears as 4.3 out of 5.
I still use the existing reviews for that calculation.

Show in code: `common/controller/SchoolPresentationAdvice.java`, `PublicDisplay.rating`;
`public/instructors.jsp`; `instructor/lesson-dashboard.jsp`.
Show in browser: Admin > Reviews > approve the seeded pending review; Visitor > Instructors.

## Batch B data and recovery rules, 10 November 2026

These answers and the regenerated [speaking pack](viva-2026/README.md) supersede
older claims that nobody can read saved contact messages. Batch A rules still apply.

**Where does the contact form go?**
In our system, I save the message for an administrator to read on the dashboard.
I do not send an email or reply from the website. I show staff the sender's address
so they can follow up outside the system.

Show in code: `common/service/ContactService.java`, `submit` and `messagesForAdmin`;
`admin/controller/AdminDashboardController.java`, `showDashboard`;
`admin/dashboard.jsp`, Website enquiries.
Show in browser: Visitor > Contact us > Send message; Admin > Dashboard > Website enquiries.

**What happens on a double click or a quick retry?**
In our system, I block repeated accepted submits in the browser. I also merge the
same contact message sent again within one minute. I still use server payment and
booking checks, because a browser guard alone cannot protect saved data.

Show in code: `static/js/site.js`, submit handlers and `showSubmitLoading`;
`ContactService.submit`; `EnrollmentService.requireAwaiting`; `BookingService.requestLesson`.

**Can Escape accidentally confirm an old action?**
In our system, I clear the old dialog decision each time it opens. I also clear
the pending form when it closes. I submit only when the current dialog is confirmed.

Show in code: `static/js/site.js`, `confirmDialog` open and close handlers.
Show in browser: Student > Dashboard > Account settings > Delete my account > Escape.
This only opens and cancels the dialog; do not confirm a real deletion for the viva.

**What happens if someone changes a schedule filter in the address?**
In our system, I check the status and date range before showing results. I return
a helpful 400 page for invalid values and give a link back to the schedule.

Show in code: `BookingService.filterBookings` and `parseFilterStatus`;
`admin/bookings.jsp`, string comparison with `status.name()`.
Show in browser: Admin > Bookings > filter, or `/admin/bookings?status=BOGUS`.

**How do I keep error links useful?**
In our system, I keep the correct error status and show a safe HTML page. I link
back to a known form, list or the current role's dashboard. I do not send a student
to an administrator's form.

Show in code: `GlobalExceptionHandler.handleFormBinding`, `handleWrongMethod`;
`ErrorPageSupport.prepare`, `recordList` and `allowedLink`.

**What does a student with no package see?**
In our system, I show a package link before asking for a lesson time. I show the
package allowance without pretending unpaid lessons are usable. I show friendly
lesson-type names while keeping their existing stored values.

Show in code: `EnrollmentPresentationService.noPackage`, `describe`;
`EnrollmentUsageView.isDetailsAvailable`; `BookingController.list`;
`SchoolPresentationAdvice.PublicDisplay.lessonTypeInput`; `StudentProfileService.storedLessonType`.

Final checks: **691 tests passed, zero failures/errors/skips**, CSS build passed,
and **31 Batch B browser checks passed**. Normal smoke: **30 PASS, 3 UNVERIFIED**;
the three still require successful completion of a future lesson with the gate on.
See [the Batch B review](BATCH_B_REVIEW.md) for precise evidence and limitations.

## Batch A rules, 10 November 2026

This section supersedes older renewal and no-show answers below. The regenerated
[speaking pack](viva-2026/README.md) and its PDFs include these rules.

**Can a lesson be marked completed or no-show early?**
In our system, I wait until the lesson ends for both actions. I keep the default
attendance setting true. I can use an explicit override in a fresh isolated preview.

Show in code: `booking/model/Booking.java`, `complete` and `markNoShow`;
`booking/service/BookingService.java`, `markNoShowAssignedLesson`;
`application.properties`, `app.attendance.require-lesson-ended`.
Show in browser: Instructor login > My schedule > a Confirmed lesson > attendance.

**Can a student buy another package?**
In our system, I allow another package when the old one is used up or expired and
there is no open lesson. I keep one payment request at a time and keep the old
receipts. I check the rules again when the administrator confirms payment.

Show in code: `learningpackage/service/EnrollmentService.java`, `ensureCanEnroll`,
`renewalProblem`, `paymentStudent` and `markPaid`.
Show in browser: Student6 login > My enrollments > Choose another package > enroll;
Admin login > Awaiting payment > Confirm payment; Student6 > My enrollments.

**Does requesting a renewal give lessons immediately?**
In our system, I save an unpaid request first. I change the active package and
give the new lesson balance only after payment confirmation. I keep the previous
payment and lesson records for history.

Show in code: `EnrollmentService.enroll`, `markPaid` and `activateLessons`.

**What if the lesson date is after the package expiry?**
In our system, I refuse a new promise after the expiry date. I include the last
valid day and still support older paid receipts with no expiry recorded. I do not
change old receipt dates to make a request pass.

Show in code: `booking/service/PaidEnrollmentGuard.java`, `problemForLesson` and
`requirePaidForLesson`; `BookingService.requestLesson`, `rescheduleOwnBooking`,
`assign`, `acceptAssignedLesson` and `reassign`.

**Can an instructor delete hours or add leave over a lesson?**
In our system, I protect confirmed lessons when working hours are edited or deleted.
I also block leave over an assigned pending lesson and ask an administrator to
resolve it first. I use the existing shared app lock during these schedule changes.

Show in code: `instructor/service/InstructorScheduleService.java`,
`deleteAvailability`, `updateAvailability`, `validateLeave`;
`auth/util/AccountWriteCoordinator.java`, `execute`.
Show in browser: Instructor4 > Availability > remove hours covering B004;
Instructor6 > Availability > add leave on B003's date.

**Why did a normal startup show only one instructor and one package?**
In our system, I load the records from the selected data folder. A normal startup
does not run the optional fixture loader. I use the preview script for a fresh
folder with six instructors and seven packages.

Show in code: `application.properties`, `app.data-directory` and seed settings;
`sample/DemoDataSeedRunner.java`, `run`; `scripts/run-demo.ps1`.

**Are the changes a database transaction?**
In our system, I coordinate writers inside this Java process. I still do not have
rollback across files or locking across two running application processes.

Validation: **673 tests, zero failures/errors/skips**, `BUILD SUCCESS`, 3 minutes
34 seconds. Existing commitment-cap tests remain enabled. See the
[Batch A review](BATCH_A_REVIEW.md) and [current smoke evidence](SMOKE_TEST.md)
for browser results and their limits. No team-history claim was added.

> **Current guide (10 Nov 2026):** use the [source-checked viva pack](viva-2026/README.md), [six individual diagrams](viva-2026/class-diagrams.html) and [six printable cheat sheets](viva-2026/cheat-sheets.pdf). The older material below is historical and may mention removed interfaces or earlier report layouts.

## Current seed revision: Batch 3, 10 November 2026

This section supersedes older fixture counts and scenario IDs in the historical
checks below. The current loader and checked-in snapshot use the same records:
19 accounts, 6 instructors, 12 students, 7 packages, 6 vehicles, 9 enrollments,
24 bookings, 16 reviews, 42 weekly availability blocks, 3 lesson notes and 1 day off.
Paid revenue is **LKR 190,500**; 12 approved reviews determine instructor ratings.

Existing admin, instructor1-5 and student1-10 login emails and `DriveSpark123!`
are unchanged. `instructor6@drivingschool.test` is Sayumi Perera. Added students
use `chamodi.abeysinghe@learners.example.test` and
`nivethan.yogarajah@learners.example.test`, with the same fixture password.
Student 8 is Active; Student 9 is Pending and Student 10 is Suspended.

- Experience descending: Pasindu, Piravarshini, Shahithiyan, Ruwini, Pragash, Sayumi.
- FIFO: B001 then B002. Assigned pending B003 belongs to Tharushi and Sayumi.
- E006 is expired; E007 is used up; E008 awaits payment and E009 is cancelled,
  both belonging to Dharshan (Student 8). There is no E010 in this snapshot.
- V003 is in maintenance. V006 (Honda Dio) has insurance and service due soon.
- Notes N001/B006, N002/B007 and N003/B021 give progress examples.
- A new account starts at S013; a new package P008; a new booking B025.

Use a **fresh empty data folder** for the loader, or a fresh temporary copy of
`sample-data` with both seed loaders disabled. Existing data-demo folders keep
old names. All phone/NIC fixtures are deliberately non-real syntax-only values;
all emails use reserved `.test` domains. Do not dial them or treat them as civil
identity examples. No licence or accreditation claims are seeded.

See [the current sample-data guide](../sample-data/README.md) and
[Batch 3 verification](BATCH_3_SEED_REVIEW.md) for the current data and checks.
The older runs below remain historical evidence, not results for this dataset.


Owner: **IT26101135**, integration documentation. Phase C. Checked against the current checkout on **9 November 2026**. Member IDs are used without personal names.

These are short answers to practise aloud. “I used” means the member can explain the code assigned to their module; it does not prove that member personally authored every shared line. Show the source before making a claim. Keep [CLASS_DIAGRAMS.md](CLASS_DIAGRAMS.md), [CRUD_MAP.md](CRUD_MAP.md) and [VIVA_GUIDE.md](VIVA_GUIDE.md) nearby, but follow the actual code if an older map differs.

This guide contains **20 project questions, 12 questions for each of six members, 15 what-if questions and 5 group questions**: **112 answers**. Each spoken answer has two to four sentences. The code references are separate prompts, not extra sentences to memorize.

## Evidence and honest limits

- **Source confirmed** means the named class/method and described rule were inspected. It does not mean a browser demo was run.
- **NEEDS CHECK** marks browser rehearsals, personal team history, print pagination or a behavior this documentation task could not confirm. Do not turn those into claims of successful testing.
- The **77 existing Surefire XML reports** currently total **663 tests, 0 failures, 0 errors, 0 skipped**. Their local file modification times run from **2026-11-09 14:32:42 to 14:33:40**. These are existing results, not a build run by this agent.
- The historical 14:22:26 failed build is described in MERGE_ANALYSIS.md. Its obsolete root build.log was removed in the approved cleanup. Later dated verification sections supersede that old result; do not combine results from different runs.
- The project targets Java 17 in `pom.xml`. Use the intended runtime when rehearsing; this guide did not change the Java installation or run the app.
- All demos that save data require an isolated temporary demo directory. Do not suspend, delete, process the queue, hide feedback or confirm/cancel payments on live data for a viva rehearsal.
- The brief asks for five OOP topics; here these are encapsulation, inheritance, abstraction, polymorphism and exception handling. We also explain the five SOLID letters separately.
- Only this new Markdown document is written in Phase C. No source files, tests, routes, form names or Git state are changed.

## 1. Project-level questions

### P01. What does DriveSpark Academy do?

In our system, I help students choose a learning package, request lessons and read instructor feedback. Admins manage the school records and confirm manual payments. Instructors manage their availability and assigned lessons.

**Show in code:** [HomeController.java](../src/main/java/com/drivingschool/common/controller/HomeController.java) — `showHomePage`; [BookingController.java](../src/main/java/com/drivingschool/booking/controller/BookingController.java) — `list`.

**Show in browser:** / → Packages → Instructors. **NEEDS CHECK:** rehearsal path, not a browser test performed for this document.

### P02. Who are the four roles?

In our system, I treat a visitor as someone who is not logged in. The three saved account roles are STUDENT, INSTRUCTOR and ADMIN. Each account type supplies its own role and dashboard destination.

**Show in code:** [Role.java](../src/main/java/com/drivingschool/auth/model/Role.java) — `enum declaration`; [User.java](../src/main/java/com/drivingschool/auth/model/User.java) — `getRole`; [NavigationInterceptor.java](../src/main/java/com/drivingschool/auth/interceptor/NavigationInterceptor.java) — `postHandle`.

### P03. How is the application organized?

In our system, I used Spring Boot to start a Java MVC web application. A controller accepts a request, a service checks the school rules and a repository reads or writes records. JSP pages display the result rather than deciding payment or booking eligibility.

**Show in code:** [DrivesparkAcademyApplication.java](../src/main/java/com/drivingschool/DrivesparkAcademyApplication.java) — `main`; [BookingController.java](../src/main/java/com/drivingschool/booking/controller/BookingController.java) — `request`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `requestLesson`.

### P04. Why do we use file storage, and what is its limit?

In our system, I save each record as a JSON line in a text file so the storage is easy to inspect for a small coursework project. I reuse FileRepository for reading, IDs and replacement writes. It is not a database, so it has no cross-file transactions or database foreign keys.

**Show in code:** [FileRepository.java](../src/main/java/com/drivingschool/common/repository/FileRepository.java) — `writeRecords`; [FileRepository.java](../src/main/java/com/drivingschool/common/repository/FileRepository.java) — `atomicWrite`.

### P05. Can two application processes safely write the same data?

In our system, I use Java locks to coordinate writers inside one running app. Those locks are not shared by another JVM, so I would not run two writers against the same data folder. I would improve this with database transactions or a deliberate cross-process storage design.

**Show in code:** [FileRepository.java](../src/main/java/com/drivingschool/common/repository/FileRepository.java) — `save`; [AccountWriteCoordinator.java](../src/main/java/com/drivingschool/auth/util/AccountWriteCoordinator.java) — `execute`.

### P06. How are passwords protected?

In our system, I save BCrypt hashes instead of readable passwords. PasswordHasher hashes new passwords and compares a login password with the saved hash. Hashing is not reversible encryption, and I never need to recover the original password.

**Show in code:** [PasswordHasher.java](../src/main/java/com/drivingschool/auth/util/PasswordHasher.java) — `hash`; [PasswordHasher.java](../src/main/java/com/drivingschool/auth/util/PasswordHasher.java) — `matches`; [AuthService.java](../src/main/java/com/drivingschool/auth/service/AuthService.java) — `authenticate`.

### P07. How does login use a session?

In our system, I check the password and real account status before creating a fresh session. I store the user ID, set a 30-minute inactivity timeout and reload the account during access checks. Logout invalidates the session.

**Show in code:** [AuthSessionService.java](../src/main/java/com/drivingschool/auth/service/AuthSessionService.java) — `loginWithResult`; [AuthSessionService.java](../src/main/java/com/drivingschool/auth/service/AuthSessionService.java) — `getCurrentUser`; [AuthSessionService.java](../src/main/java/com/drivingschool/auth/service/AuthSessionService.java) — `logout`.

**Show in browser:** /login → sign in → Dashboard → Log out. **NEEDS CHECK:** rehearsal path, not a browser test performed for this document.

### P08. What is CSRF protection?

In our system, I put a session token into forms that change data. CsrfFilter rejects an unsafe request if the token is missing or invalid. This helps stop another website from submitting a form using a student's existing session.

**Show in code:** [CsrfTokenService.java](../src/main/java/com/drivingschool/auth/service/CsrfTokenService.java) — `getOrCreateToken`; [CsrfTokenService.java](../src/main/java/com/drivingschool/auth/service/CsrfTokenService.java) — `isValid`; [CsrfFilter.java](../src/main/java/com/drivingschool/auth/filter/CsrfFilter.java) — `doFilterInternal`.

### P09. How do we stop the wrong role from opening an admin page?

In our system, I use RequireAdmin and RequireLogin annotations with AccessInterceptor. Services also check roles and record ownership where needed. Hiding a menu link alone would not protect a route.

**Show in code:** [AccessInterceptor.java](../src/main/java/com/drivingschool/auth/interceptor/AccessInterceptor.java) — `preHandle`; [AuthSessionService.java](../src/main/java/com/drivingschool/auth/service/AuthSessionService.java) — `requireAdmin`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `requireStudent`.

### P10. Where does validation happen?

In our system, I validate bound form fields and also check models and service rules. A changed browser form cannot make an unpaid receipt usable for booking. I keep server checks even when the page already disables a button.

**Show in code:** [AuthController.java](../src/main/java/com/drivingschool/auth/controller/AuthController.java) — `register`; [Enrollment.java](../src/main/java/com/drivingschool/learningpackage/model/Enrollment.java) — `setStatus`; [PaidEnrollmentGuard.java](../src/main/java/com/drivingschool/booking/service/PaidEnrollmentGuard.java) — `requirePaid`.

### P11. How are errors shown?

In our system, I map validation errors to 400, missing records to 404, duplicates to 409 and storage problems to 500. The shared error page stays common/error, including 403 access errors. I choose recovery links for the user's role and a known local form link for validation errors.

**Show in code:** [GlobalExceptionHandler.java](../src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java) — `handleValidation`; [CustomErrorViewResolver.java](../src/main/java/com/drivingschool/common/config/CustomErrorViewResolver.java) — `resolveErrorView`; [ErrorPageSupport.java](../src/main/java/com/drivingschool/common/exception/ErrorPageSupport.java) — `prepare`.

### P12. Why can a student log in immediately after registering?

In our system, I create new students with ACTIVE status because registration is separate from payment approval. PENDING and SUSPENDED still exist for admin-controlled accounts and checks. The Pending approval login message is only selected when the saved account is actually Pending.

**Show in code:** [AuthService.java](../src/main/java/com/drivingschool/auth/service/AuthService.java) — `registerStudent`; [AuthSessionService.java](../src/main/java/com/drivingschool/auth/service/AuthSessionService.java) — `loginWithResult`.

**Show in browser:** /register → submit valid account → /login. **NEEDS CHECK:** rehearsal path, not a browser test performed for this document.

### P13. How does manual payment confirmation work?

In our system, I first save an Awaiting payment enrollment with the selected package's price. The student pays by bank transfer or at the office and follows the instructions to contact the admin outside the app. An admin marks the receipt Paid before lessons become usable.

**Show in code:** [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `enroll`; [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `markPaid`; [EnrollmentController.java](../src/main/java/com/drivingschool/learningpackage/controller/EnrollmentController.java) — `payment`.

**Show in browser:** /packages → package detail → Enroll → payment instructions; admin → Awaiting payment. **NEEDS CHECK:** rehearsal path, not a browser test performed for this document.

### P14. What exactly is needed before a student can book?

In our system, I require the saved enrollment to be Paid and owned by the student. The active receipt and package IDs must match, the student must be Active and have lessons left, and the package must still be active. A saved expiry date must not be before today.

**Show in code:** [PaidEnrollmentGuard.java](../src/main/java/com/drivingschool/booking/service/PaidEnrollmentGuard.java) — `problemFor`; [PaidEnrollmentGuard.java](../src/main/java/com/drivingschool/booking/service/PaidEnrollmentGuard.java) — `matchesBalance`; [PaidEnrollmentGuard.java](../src/main/java/com/drivingschool/booking/service/PaidEnrollmentGuard.java) — `packageProblem`.

**Show in browser:** /enrollments → inspect status → /bookings. **NEEDS CHECK:** rehearsal path, not a browser test performed for this document.

### P15. How does the sample data loader work?

In our system, I enable the demo loader explicitly and keep it separate from the optional first-admin bootstrap. It creates linked examples in dependency order and checks existing records before adding them again. For tests and rehearsal I use temporary data instead of the school's real folder.

**Show in code:** [DemoDataSeedRunner.java](../src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java) — `run`; [DemoDataSeedRunner.java](../src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java) — `seedAccounts`; [AdminSeedRunner.java](../src/main/java/com/drivingschool/auth/config/AdminSeedRunner.java) — `run`.

### P16. How was the Tailwind interface built?

In our system, I compile Tailwind 3.4.17 locally from app.css using npm run build:css. The configuration scans JSP, JSPF and JavaScript files, and the JSPs use the built local tailwind.css. Our photos and fonts are local assets, and the design does not use Rivian assets.

**Show in code:** [app.css](../src/main/resources/static/css/app.css) — `@tailwind directives`; [tailwind.config.js](../tailwind.config.js) — `content setting`; [package.json](../package.json) — `scripts.build:css`; [README.md](../README.md) — `Credits section`.

### P17. How do animations stay usable and accessible?

In our system, I check prefers-reduced-motion before starting motion effects. The package menu has keyboard and focus handling as well as a delayed pointer close. The ordinary Packages link still has a real destination when JavaScript is off.

**Show in code:** [site.js](../src/main/resources/static/js/site.js) — `prepareMotion`; [site.js](../src/main/resources/static/js/site.js) — `preparePackageMenu`; [navbar.jspf](../src/main/webapp/WEB-INF/views/common/navbar.jspf) — `Packages link`.

**Show in browser:** / → Packages menu → Tab, Enter and Esc. **NEEDS CHECK:** rehearsal path, not a browser test performed for this document.

### P18. What tests exist and how many are passing?

I checked the existing Surefire XML reports, which record 663 tests with 0 failures, 0 errors and 0 skipped. We have model, service, repository, security and web integration tests using temporary data. I did not run a new build while writing this guide, and those test reports alone do not prove every browser page or final WAR packaging.

**Show in code:** [FileRepositoryTest.java](../src/test/java/com/drivingschool/common/repository/FileRepositoryTest.java) — `test class`; [SchedulingWebIntegrationTest.java](../src/test/java/com/drivingschool/booking/SchedulingWebIntegrationTest.java) — `test class`; [pom.xml](../pom.xml) — `test dependency and Java version`.

### P19. How do you trace one feature through the code?

In our system, I start at the route's controller method and follow its service call. I then show the model checks and repository operation before returning to the JSP. That makes the web, rule and storage responsibilities easy to explain.

**Show in code:** [EnrollmentController.java](../src/main/java/com/drivingschool/learningpackage/controller/EnrollmentController.java) — `enroll`; [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `enroll`; [EnrollmentRepository.java](../src/main/java/com/drivingschool/learningpackage/repository/EnrollmentRepository.java) — `save`.

### P20. What would you improve next?

I would first add database transactions, stronger production password recovery and proper payment receipt handling. I would also unify the two instructor availability stores before exposing the incoming lookup as the real timetable. These are improvements, not features I claim are already complete.

**Show in code:** [FileRepository.java](../src/main/java/com/drivingschool/common/repository/FileRepository.java) — `atomicWrite`; [PasswordManagementService.java](../src/main/java/com/drivingschool/auth/service/PasswordManagementService.java) — `createResetToken`


## 2. IT26101135 — Authentication, Students and integration

### 135-01. What is your module responsible for?

In our system, I handle student registration, login, sessions and the student's account/profile. I also own shared security, error handling and integration files. A shared file can support another member's feature without becoming jointly owned.

**Show in code:** [AuthController.java](../src/main/java/com/drivingschool/auth/controller/AuthController.java) — `register`; [StudentProfileController.java](../src/main/java/com/drivingschool/student/controller/StudentProfileController.java) — `showProfile`.

### 135-02. Where are your four CRUD operations?

I create a student with AuthService.registerStudent and read the profile with StudentProfileService.getOwnProfile. I update it with updateOwnProfile and remove an eligible account with StudentAccountService.deleteOwnAccount. UserRepository provides the file-level save, read, update and delete operations.

**Show in code:** [AuthService.java](../src/main/java/com/drivingschool/auth/service/AuthService.java) — `registerStudent`; [StudentProfileService.java](../src/main/java/com/drivingschool/student/service/StudentProfileService.java) — `getOwnProfile`; [StudentProfileService.java](../src/main/java/com/drivingschool/student/service/StudentProfileService.java) — `updateOwnProfile`; [StudentAccountService.java](../src/main/java/com/drivingschool/student/service/StudentAccountService.java) — `deleteOwnAccount`.

### 135-03. How do registration and login keep their jobs separate?

I let registration validate the email, hash the password and save an Active student. Login separately checks the saved hash and then Pending or Suspended status before opening a session. Registering does not confirm payment or unlock a package.

**Show in code:** [AuthService.java](../src/main/java/com/drivingschool/auth/service/AuthService.java) — `registerStudent`; [AuthSessionService.java](../src/main/java/com/drivingschool/auth/service/AuthSessionService.java) — `loginWithResult`.

### 135-04. Where did you use encapsulation?

I kept account fields private in User and student details private in Student. I change details through methods such as setPhone so invalid data is checked before it is stored.

**Show in code:** [User.java](../src/main/java/com/drivingschool/auth/model/User.java) — `setEmail`; [Student.java](../src/main/java/com/drivingschool/student/model/Student.java) — `setPhone`.

### 135-05. Where did you use inheritance?

I made Student extend the abstract User class. It reuses the account ID, name, email and password-hash rules instead of copying them.

**Show in code:** [Student.java](../src/main/java/com/drivingschool/student/model/Student.java) — `class declaration`; [User.java](../src/main/java/com/drivingschool/auth/model/User.java) — `User`.

### 135-06. Where did you use abstraction?

I used abstract User methods for the role and dashboard destination. I also used StudentProfiles as the small contract for reading and editing the signed-in profile.

**Show in code:** [User.java](../src/main/java/com/drivingschool/auth/model/User.java) — `getDashboardPath`; [StudentProfiles.java](../src/main/java/com/drivingschool/student/api/StudentProfiles.java) — `getOwnProfile`.

### 135-07. Where did you use polymorphism?

I call getDashboardPath through User, but Student, Admin and InstructorAccount supply different destinations. The method name is the same while the account type decides its answer.

**Show in code:** [Student.java](../src/main/java/com/drivingschool/student/model/Student.java) — `getDashboardPath`; [Admin.java](../src/main/java/com/drivingschool/auth/model/Admin.java) — `getDashboardPath`; [InstructorAccount.java](../src/main/java/com/drivingschool/auth/model/InstructorAccount.java) — `getDashboardPath`.

### 135-08. What exception can your module raise?

I throw DuplicateRecordException when a profile email would duplicate another account. The shared exception handler turns it into a friendly conflict response rather than silently accepting the duplicate.

**Show in code:** [StudentProfileService.java](../src/main/java/com/drivingschool/student/service/StudentProfileService.java) — `ensureAvailableEmail`; [GlobalExceptionHandler.java](../src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java) — `handleDuplicate`.

### 135-09. How does your module show Single Responsibility?

I let StudentProfileController handle the web request and StudentProfileService check profile rules. UserRepository saves accounts, so the JSP does not write files or decide account ownership.

**Show in code:** [StudentProfileController.java](../src/main/java/com/drivingschool/student/controller/StudentProfileController.java) — `updateProfile`; [StudentProfileService.java](../src/main/java/com/drivingschool/student/service/StudentProfileService.java) — `updateOwnProfile`; [UserRepository.java](../src/main/java/com/drivingschool/auth/repository/UserRepository.java) — `update`.

### 135-10. How do Open/Closed and Liskov apply?

I can add another User subtype with its own role and dashboard methods without rewriting the existing subclasses. A Student can be used where User is expected and still gives a valid role and destination. Adding a role would still need security and menu updates, so I do not claim the whole app would change automatically.

**Show in code:** [User.java](../src/main/java/com/drivingschool/auth/model/User.java) — `getRole`; [Student.java](../src/main/java/com/drivingschool/student/model/Student.java) — `getRole`.

### 135-11. How do Interface Segregation and Dependency Inversion apply?

I used StudentProfiles to expose only the profile operations its page needs. StudentProfileController receives that interface through its constructor instead of building a concrete service itself.

**Show in code:** [StudentProfiles.java](../src/main/java/com/drivingschool/student/api/StudentProfiles.java) — `updateOwnProfile`; [StudentProfileController.java](../src/main/java/com/drivingschool/student/controller/StudentProfileController.java) — `StudentProfileController`.

### 135-12. What tests and limits should you show?

I can show tests for duplicate registration, hashed passwords, sessions, CSRF and profile ownership. I also explain that password reset is a local demonstration, not email delivery or a production recovery system.

**Show in code:** [AuthServiceTest.java](../src/test/java/com/drivingschool/auth/service/AuthServiceTest.java) — `concurrentDuplicateRegistrationsCreateOnlyOneAccount`; [StudentProfileWebIntegrationTest.java](../src/test/java/com/drivingschool/student/StudentProfileWebIntegrationTest.java) — `browserIdentityRoleAndPasswordFieldsCannotChangeTargetAccount`; [PasswordManagementService.java](../src/main/java/com/drivingschool/auth/service/PasswordManagementService.java) — `createResetToken`.

**Show in browser:** /forgot-password → request local demonstration reset. **NEEDS CHECK:** rehearsal path, not a browser test performed for this document.

## 3. IT26101133 — Instructors and bubble sort

### 133-01. What is your module responsible for?

In our system, I manage instructor profiles, weekly availability, leave and lesson notes. I also provide the hand-written bubble sort used on the public instructor list. The teaching profile and the instructor login account are separate records paired by ID.

**Show in code:** [InstructorService.java](../src/main/java/com/drivingschool/instructor/service/InstructorService.java) — `createInstructor`; [InstructorScheduleService.java](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java) — `addAvailability`; [InstructorAccountService.java](../src/main/java/com/drivingschool/instructor/service/InstructorAccountService.java) — `create`.

### 133-02. Where are your four CRUD operations?

I create a profile with createInstructor, read it with getInstructorById, update it with updateInstructor and delete it with deleteInstructor in InstructorService. The routed weekly schedule also has addAvailability, availability, updateAvailability and deleteAvailability. Admin account-aware changes go through InstructorAccountService.

**Show in code:** [InstructorService.java](../src/main/java/com/drivingschool/instructor/service/InstructorService.java) — `createInstructor`; [InstructorService.java](../src/main/java/com/drivingschool/instructor/service/InstructorService.java) — `getInstructorById`; [InstructorService.java](../src/main/java/com/drivingschool/instructor/service/InstructorService.java) — `updateInstructor`; [InstructorService.java](../src/main/java/com/drivingschool/instructor/service/InstructorService.java) — `deleteInstructor`; [InstructorScheduleService.java](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java) — `updateAvailability`.

### 133-03. Explain your bubble sort with an example and its cost.

I compare neighboring experience values and swap them when the requested order is wrong. For ascending [3, 1, 2], my first pass makes [1, 3, 2] and then [1, 2, 3]. A pass with no swaps stops the algorithm; worst-case comparisons take O(n²), best-case O(n), and copying the list needs O(n) extra space. Equal values keep their original order because I do not swap them.

**Show in code:** [InstructorSorter.java](../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java) — `sort`; [InstructorSorter.java](../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java) — `swapNeighbours`.

**Show in browser:** /instructors → choose experience → choose ascending → sort. **NEEDS CHECK:** rehearsal path, not a browser test performed for this document.

### 133-04. Where did you use encapsulation?

I keep the instructor's experience, email and profile details private. Methods such as setYearsOfExperience check the value instead of allowing any caller to write the field directly.

**Show in code:** [Instructor.java](../src/main/java/com/drivingschool/instructor/model/Instructor.java) — `setYearsOfExperience`; [Instructor.java](../src/main/java/com/drivingschool/instructor/model/Instructor.java) — `setEmail`.

### 133-05. Where did you use inheritance?

I made InstructorRepository extend FileRepository<Instructor> to reuse storage operations. The teaching Instructor model does not extend User; InstructorAccount is the separate login child of User.

**Show in code:** [InstructorRepository.java](../src/main/java/com/drivingschool/instructor/repository/InstructorRepository.java) — `class declaration`; [InstructorAccount.java](../src/main/java/com/drivingschool/auth/model/InstructorAccount.java) — `class declaration`.

### 133-06. Where did you use abstraction?

I used InstructorOrder to say how two instructor cards are compared without fixing the comparison detail. I also use InstructorScheduleOperations so the availability controller asks for schedule tasks rather than file operations.

**Show in code:** [InstructorOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorOrder.java) — `compare`; [InstructorScheduleOperations.java](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleOperations.java) — `addAvailability`.

### 133-07. Where did you use polymorphism?

I use the same InstructorOrder.compare call for name, experience or rating comparisons. The three implementation classes give different answers while the bubble-sort passes stay the same.

**Show in code:** [InstructorSorter.java](../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java) — `swapNeighbours`; [InstructorNameOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorNameOrder.java) — `compare`; [InstructorExperienceOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorExperienceOrder.java) — `compare`; [InstructorRatingOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorRatingOrder.java) — `compare`.

### 133-08. What exception can your module raise?

I throw DuplicateRecordException when two weekly working blocks overlap. Invalid leave dates or leave that clashes with a confirmed lesson raise ValidationException. These are shared custom exceptions rather than separate copies for each module.

**Show in code:** [InstructorScheduleService.java](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java) — `ensureNoOverlap`; [InstructorScheduleService.java](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java) — `validateLeave`; [InstructorScheduleService.java](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java) — `ensureDayHasNoConfirmedLessons`.

### 133-09. How does your module show Single Responsibility?

I let InstructorSorter order cards while InstructorScheduleService checks timetable rules. Repositories save files and controllers handle browser forms, so the sorting algorithm does not also manage login or storage.

**Show in code:** [InstructorSorter.java](../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java) — `sort`; [InstructorScheduleService.java](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java) — `addAvailability`.

### 133-10. How do Open/Closed and Liskov apply?

I can add another InstructorOrder implementation without rewriting the bubble-sort passes. Every order must follow the same negative, zero and positive comparison meaning, so it can replace another order. The chooseOrder method would still need to select a new option.

**Show in code:** [InstructorOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorOrder.java) — `compare`; [InstructorSorter.java](../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java) — `chooseOrder`; [InstructorNameOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorNameOrder.java) — `compare`.

### 133-11. How do Interface Segregation and Dependency Inversion apply?

I keep InstructorOrder small with one comparison method. InstructorAvailabilityController receives InstructorScheduleOperations through its constructor, so it depends on the contract instead of constructing a service.

**Show in code:** [InstructorOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorOrder.java) — `compare`; [InstructorAvailabilityController.java](../src/main/java/com/drivingschool/instructor/controller/InstructorAvailabilityController.java) — `InstructorAvailabilityController`.

### 133-12. What is the important availability limitation after the merge?

The website uses weekly Availability records and instructor leave. BookingConflictChecker reads these records before assignment. The unused date-time interval implementation has been removed; no data migration is needed for the working weekly schedule.

**Show in code:** [AvailabilityRepository.java](../src/main/java/com/drivingschool/instructor/repository/AvailabilityRepository.java) — `AvailabilityRepository`; [BookingConflictChecker.java](../src/main/java/com/drivingschool/booking/service/BookingConflictChecker.java) — `isWorking`

## 4. IT26101134 — Bookings, FIFO and scheduling

### 134-01. What is your module responsible for?

In our system, I handle lesson requests, the FIFO waiting queue, assignment, rescheduling and booking outcomes. I check saved payment and timetable rules before a booking is allowed to use resources. Admin assignment remains Pending until the instructor accepts it.

**Show in code:** [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `requestLesson`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `assign`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `acceptAssignedLesson`.

### 134-02. Where are your four CRUD operations?

I create with requestLesson, read with getOwnBookings, update with rescheduleOwnBooking and delete with deleteCancelledBooking in BookingService. Cancel is an UPDATE to status, while DELETE removes an already-cancelled owned record. BookingRepository supplies the matching file operations.

**Show in code:** [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `requestLesson`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `getOwnBookings`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `rescheduleOwnBooking`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `deleteCancelledBooking`.

### 134-03. Explain FIFO, enqueue, dequeue and the cost.

I add a new node at the tail with enqueue and remove the head with dequeue, so A then B then C leaves as A then B then C. Those two operations are O(1) because I keep head and tail links. When I rebuild the queue, I first filter Pending unassigned bookings and use stable insertion sort by createdAt, which can take O(n²). I use FIFO so a newer request does not jump ahead of an older one.

**Show in code:** [BookingRequestQueue.java](../src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java) — `BookingRequestQueue`; [BookingRequestQueue.java](../src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java) — `enqueue`; [BookingRequestQueue.java](../src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java) — `dequeue`.

### 134-04. Where did you use encapsulation?

I keep booking identity, times and status private. Methods such as confirm and cancel check allowed states before changing them, so a page cannot directly rewrite status.

**Show in code:** [Booking.java](../src/main/java/com/drivingschool/booking/model/Booking.java) — `confirm`; [Booking.java](../src/main/java/com/drivingschool/booking/model/Booking.java) — `cancel`.

### 134-05. Where did you use inheritance?

I made BookingRepository extend FileRepository<Booking>. It keeps the parent's strict read and update rules while selecting booking records and their file.

**Show in code:** [BookingRepository.java](../src/main/java/com/drivingschool/booking/repository/BookingRepository.java) — `class declaration`; [BookingRepository.java](../src/main/java/com/drivingschool/booking/repository/BookingRepository.java) — `update`.

### 134-06. Where did you use abstraction?

I used separate StudentBookingOperations and AdminBookingOperations interfaces. I also used BookingOrder for the small job of returning an ordered list.

**Show in code:** [StudentBookingOperations.java](../src/main/java/com/drivingschool/booking/service/StudentBookingOperations.java) — `requestLesson`; [AdminBookingOperations.java](../src/main/java/com/drivingschool/booking/service/AdminBookingOperations.java) — `assign`; [BookingOrder.java](../src/main/java/com/drivingschool/booking/util/BookingOrder.java) — `order`.

### 134-07. Where did you use polymorphism?

I call order through BookingOrder, but PendingRequestOrder returns the unassigned FIFO waiting line and LessonTimeOrder returns lessons by start time. Both return a new list without changing the caller's list. I choose the appropriate order because their intended subsets differ.

**Show in code:** [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `getAllBookings`; [PendingRequestOrder.java](../src/main/java/com/drivingschool/booking/util/PendingRequestOrder.java) — `order`; [LessonTimeOrder.java](../src/main/java/com/drivingschool/booking/util/LessonTimeOrder.java) — `order`.

### 134-08. What exception can your module raise?

I use ValidationException when payment, a time choice or the notice-period rule is invalid. PaidEnrollmentGuard.requirePaid throws that exception using the saved receipt state. For queue assignment, I can instead save a rejected booking with the returned payment or conflict reason.

**Show in code:** [PaidEnrollmentGuard.java](../src/main/java/com/drivingschool/booking/service/PaidEnrollmentGuard.java) — `requirePaid`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `cancelOwnBooking`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `assign`.

### 134-09. How does your module show Single Responsibility?

I keep waiting order in BookingRequestQueue and conflict rules in BookingConflictChecker. BookingService coordinates the operation and BookingRepository saves its result.

**Show in code:** [BookingRequestQueue.java](../src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java) — `dequeue`; [BookingConflictChecker.java](../src/main/java/com/drivingschool/booking/service/BookingConflictChecker.java) — `conflictReason`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `processNext`.

### 134-10. How do Open/Closed and Liskov apply?

I can add a BookingOrder implementation without changing the existing order classes. A caller can use either current implementation through the same order method and expect a separate returned list. I still need to choose the correct order for the page's purpose.

**Show in code:** [BookingOrder.java](../src/main/java/com/drivingschool/booking/util/BookingOrder.java) — `order`; [LessonTimeOrder.java](../src/main/java/com/drivingschool/booking/util/LessonTimeOrder.java) — `order`; [PendingRequestOrder.java](../src/main/java/com/drivingschool/booking/util/PendingRequestOrder.java) — `order`.

### 134-11. How do Interface Segregation and Dependency Inversion apply?

I give the student controller StudentBookingOperations instead of all administrator operations. BookingController receives that interface through its constructor, while AdminBookingController receives AdminBookingOperations.

**Show in code:** [BookingController.java](../src/main/java/com/drivingschool/booking/controller/BookingController.java) — `BookingController`; [AdminBookingController.java](../src/main/java/com/drivingschool/booking/controller/AdminBookingController.java) — `AdminBookingController`.

### 134-12. What do your tests prove and what is the main limit?

I can show queue ordering tests, conflict tests, booking CRUD tests and notice-boundary tests. Queue data is rebuilt from bookings after restart rather than being a separate permanent queue file. Our locks are only inside this JVM, and separate file writes are not a transaction.

**Show in code:** [BookingRequestQueueTest.java](../src/test/java/com/drivingschool/booking/util/BookingRequestQueueTest.java) — `ordersPendingByCreationAndSkipsCancelledAndAssignedRequests`; [BookingConflictCheckerTest.java](../src/test/java/com/drivingschool/booking/service/BookingConflictCheckerTest.java) — `rejectsStudentOverlap`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `processNext`.

## 5. IT26101132 — Vehicles, sample data and test checklist

### 132-01. What is your module responsible for?

In our system, I manage teaching vehicles and their service or insurance dates. I also provide repeat-safe sample records and the test checklist. I keep sample data separate from the real data folder during rehearsal.

**Show in code:** [VehicleService.java](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) — `createVehicle`; [DemoDataSeedRunner.java](../src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java) — `run`; [TEST_CHECKLIST.md](../docs/TEST_CHECKLIST.md) — `checklist sections`.

### 132-02. Where are your four CRUD operations?

I use VehicleService.createVehicle, getAllVehicles, updateVehicle and deleteVehicle for CRUD. VehicleController exposes the forms and VehicleRepository handles the matching save, read, update and delete calls.

**Show in code:** [VehicleService.java](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) — `createVehicle`; [VehicleService.java](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) — `getAllVehicles`; [VehicleService.java](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) — `updateVehicle`; [VehicleService.java](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) — `deleteVehicle`.

### 132-03. How do expiry warnings work?

I report an insurance or service warning when its date is no later than today plus 30 days, including already-overdue dates. A separate expired or overdue method checks whether the date is before today, and a missing date does not raise that flag. These warnings do not automatically change operationalStatus or cancel bookings.

**Show in code:** [Vehicle.java](../src/main/java/com/drivingschool/vehicle/model/Vehicle.java) — `withinThirtyDays`; [Vehicle.java](../src/main/java/com/drivingschool/vehicle/model/Vehicle.java) — `isInsuranceExpired`; [Vehicle.java](../src/main/java/com/drivingschool/vehicle/model/Vehicle.java) — `isServiceOverdue`.

**Show in browser:** /admin/vehicles → inspect warning badges. **NEEDS CHECK:** rehearsal path, not a browser test performed for this document.

### 132-04. Where did you use encapsulation?

I keep vehicle details and maintenance dates private. For example, setNextServiceDate rejects a next service date that is not after the saved last service date.

**Show in code:** [Vehicle.java](../src/main/java/com/drivingschool/vehicle/model/Vehicle.java) — `setNextServiceDate`; [Vehicle.java](../src/main/java/com/drivingschool/vehicle/model/Vehicle.java) — `normalizeRegistrationNumber`.

### 132-05. Where did you use inheritance?

I made VehicleRepository extend FileRepository<Vehicle>. It reuses strict reading, ID reservation and safe file replacement rather than copying those operations.

**Show in code:** [VehicleRepository.java](../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java) — `class declaration`; [FileRepository.java](../src/main/java/com/drivingschool/common/repository/FileRepository.java) — `nextId`.

### 132-06. Where did you use abstraction?

I used VehicleOperations for the fleet page and VehicleStore for storage operations. The controller and service can ask for those jobs without knowing the file-writing details.

**Show in code:** [VehicleOperations.java](../src/main/java/com/drivingschool/vehicle/api/VehicleOperations.java) — `createVehicle`; [VehicleStore.java](../src/main/java/com/drivingschool/vehicle/api/VehicleStore.java) — `save`.

### 132-07. Where did you use polymorphism?

I can use VehicleRepository through VehicleStore or its FileRepository parent type, and Java dispatches its overrides. Its save override delegates to the shared writer configured for Vehicle records. I do not claim that we already have two different VehicleStore implementations.

**Show in code:** [VehicleRepository.java](../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java) — `save`; [VehicleService.java](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) — `VehicleService`.

### 132-08. What exception can your module raise?

I throw DuplicateRecordException when another vehicle already has the normalized registration number. I also raise ValidationException when deletion would remove a vehicle referenced by lesson history.

**Show in code:** [VehicleService.java](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) — `ensureRegistrationNumberIsUnique`; [VehicleService.java](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) — `deleteVehicle`.

### 132-09. How does your module show Single Responsibility?

I keep history and duplicate-number rules in VehicleService. VehicleRepository writes files and VehicleController accepts browser forms.

**Show in code:** [VehicleService.java](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) — `deleteVehicle`; [VehicleRepository.java](../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java) — `delete`; [VehicleController.java](../src/main/java/com/drivingschool/vehicle/controller/VehicleController.java) — `deleteVehicle`.

### 132-10. How do Open/Closed and Liskov apply?

I can create another child repository for a different record type without rewriting FileRepository. VehicleRepository keeps the parent's update and deletion rules by delegating to them. It does not silently ignore missing records or damaged storage.

**Show in code:** [VehicleRepository.java](../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java) — `update`; [VehicleRepository.java](../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java) — `delete`; [FileRepository.java](../src/main/java/com/drivingschool/common/repository/FileRepository.java) — `update`.

### 132-11. How do Interface Segregation and Dependency Inversion apply?

VehicleController receives VehicleOperations and VehicleService receives VehicleStore through constructors. These interfaces separate fleet actions from file storage. BookingConflictChecker reads VehicleRepository for assignment checks.

**Show in code:** [VehicleController.java](../src/main/java/com/drivingschool/vehicle/controller/VehicleController.java) — `VehicleController`; [VehicleService.java](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) — `VehicleService`; [BookingConflictChecker.java](../src/main/java/com/drivingschool/booking/service/BookingConflictChecker.java) — `conflictReason`.

### 132-12. How do you prove sample data and dates are safe?

I can show a test that runs the loader repeatedly without duplicating linked examples. The vehicle tests check the 30-day boundary, overdue dates and an invalid edit leaving the stored record unchanged. This is not a scheduled real-world inspection or an automatic notification service.

**Show in code:** [DemoDataSeedRunnerTest.java](../src/test/java/com/drivingschool/sample/DemoDataSeedRunnerTest.java) — `seedCanRunTwiceWithoutDuplicateRecords`; [VehicleMaintenanceTest.java](../src/test/java/com/drivingschool/vehicle/service/VehicleMaintenanceTest.java) — `renewalBoundaryIncludesThirtyDays`; [VehicleMaintenanceTest.java](../src/test/java/com/drivingschool/vehicle/service/VehicleMaintenanceTest.java) — `invalidServiceDateDoesNotChangeStoredVehicle`.

## 6. IT26101136 — Learning packages and shared UI

### 136-01. What is your module responsible for?

In our system, I manage learning packages, enrollment receipts and manual payment confirmation. I also supply package data for the shared public UI and menu. The package is an offered course, while an enrollment belongs to one student.

**Show in code:** [LearningPackageService.java](../src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java) — `createPackage`; [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `enroll`; [PackageNavigationAdvice.java](../src/main/java/com/drivingschool/learningpackage/controller/PackageNavigationAdvice.java) — `choices`.

### 136-02. Where are your four CRUD operations?

I use createPackage, listPackages, updatePackage and deletePackage in LearningPackageService for package CRUD. I use enroll, ownEnrollments, updateLearningGoal and deleteOwnEnrollment in EnrollmentService for enrollment CRUD. A paid or otherwise protected receipt cannot simply be deleted.

**Show in code:** [LearningPackageService.java](../src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java) — `createPackage`; [LearningPackageService.java](../src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java) — `listPackages`; [LearningPackageService.java](../src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java) — `updatePackage`; [LearningPackageService.java](../src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java) — `deletePackage`; [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `deleteOwnEnrollment`.

### 136-03. What exactly changes when payment is confirmed?

I require an admin and reload the Awaiting payment receipt under the shared account lock. I reject an inactive package or a student who is Suspended or otherwise not Active. I set the receipt Paid, set expiry from the confirmation date, and save the student's active IDs and lesson allowance. The amount remains the price captured when the request was created.

**Show in code:** [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `markPaid`; [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `paymentStudent`; [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `activateLessons`.

**Show in browser:** /admin/enrollments → Mark as Paid → confirm → student /enrollments. **NEEDS CHECK:** rehearsal path, not a browser test performed for this document.

### 136-04. Where did you use encapsulation?

I keep price and lesson counts private in LearningPackage and check changes through methods such as setPrice. Enrollment.setStatus accepts only AWAITING_PAYMENT, PAID or CANCELLED. Permission and allowed payment transitions are checked in the service.

**Show in code:** [LearningPackage.java](../src/main/java/com/drivingschool/learningpackage/model/LearningPackage.java) — `setPrice`; [Enrollment.java](../src/main/java/com/drivingschool/learningpackage/model/Enrollment.java) — `setStatus`; [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `requireAwaiting`.

### 136-05. Where did you use inheritance?

I made LearningPackageRepository and EnrollmentRepository extend FileRepository with their own record types. LearningPackage does not extend Enrollment, and LearningRecord is an interface rather than a parent class.

**Show in code:** [LearningPackageRepository.java](../src/main/java/com/drivingschool/learningpackage/repository/LearningPackageRepository.java) — `class declaration`; [EnrollmentRepository.java](../src/main/java/com/drivingschool/learningpackage/repository/EnrollmentRepository.java) — `class declaration`; [LearningRecord.java](../src/main/java/com/drivingschool/learningpackage/model/LearningRecord.java) — `interface declaration`.

### 136-06. Where did you use abstraction?

I used LearningRecord for a saved ID and readable display name. I separate student enrollment tasks in EnrollmentOperations from cashier tasks in EnrollmentAdministration.

**Show in code:** [LearningRecord.java](../src/main/java/com/drivingschool/learningpackage/model/LearningRecord.java) — `getDisplayName`; [EnrollmentOperations.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentOperations.java) — `ownEnrollments`; [EnrollmentAdministration.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentAdministration.java) — `markPaid`.

### 136-07. Where did you use polymorphism?

I can use either LearningPackage or Enrollment through LearningRecord. The same getDisplayName method returns the course name for one and an enrollment heading for the other. A unit test demonstrates both types through their shared contract.

**Show in code:** [LearningPackage.java](../src/main/java/com/drivingschool/learningpackage/model/LearningPackage.java) — `getDisplayName`; [Enrollment.java](../src/main/java/com/drivingschool/learningpackage/model/Enrollment.java) — `getDisplayName`; [EnrollmentServiceTest.java](../src/test/java/com/drivingschool/learningpackage/service/EnrollmentServiceTest.java) — `displaysBothLearningRecordTypesThroughTheirContract`.

### 136-08. What exception can your module raise?

I throw DuplicateRecordException when a student already has an Awaiting payment request or active paid package. I use ValidationException for a repeated payment decision or an unsafe deletion. Controllers or the shared handler turn those into friendly messages.

**Show in code:** [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `ensureCanEnroll`; [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `requireAwaiting`; [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `ensureDeletable`.

### 136-09. How does your module show Single Responsibility?

I let EnrollmentService decide eligibility and payment transitions while the controller handles forms and redirects. EnrollmentRepository saves receipts, and the JSP only displays the instructions or state.

**Show in code:** [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `markPaid`; [AdminEnrollmentController.java](../src/main/java/com/drivingschool/learningpackage/controller/AdminEnrollmentController.java) — `markPaid`; [EnrollmentRepository.java](../src/main/java/com/drivingschool/learningpackage/repository/EnrollmentRepository.java) — `update`.

### 136-10. How do Open/Closed and Liskov apply?

I can add another LearningRecord implementation that supplies an ID and display name without changing the existing implementations. LearningPackage and Enrollment both keep that small contract when used through it. This does not mean their business rules are interchangeable.

**Show in code:** [LearningRecord.java](../src/main/java/com/drivingschool/learningpackage/model/LearningRecord.java) — `getDisplayName`; [LearningPackage.java](../src/main/java/com/drivingschool/learningpackage/model/LearningPackage.java) — `getDisplayName`; [Enrollment.java](../src/main/java/com/drivingschool/learningpackage/model/Enrollment.java) — `getDisplayName`.

### 136-11. How do Interface Segregation and Dependency Inversion apply?

I give the student page EnrollmentOperations and the payment-admin page EnrollmentAdministration. Each controller receives its small interface through its constructor, and one EnrollmentService implements both.

**Show in code:** [EnrollmentController.java](../src/main/java/com/drivingschool/learningpackage/controller/EnrollmentController.java) — `EnrollmentController`; [AdminEnrollmentController.java](../src/main/java/com/drivingschool/learningpackage/controller/AdminEnrollmentController.java) — `AdminEnrollmentController`; [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `class declaration`

### 136-12. How do you preserve old enrollment files?

I default an old receipt with no payment-state fields to Paid, while an explicit old UNPAID value stays Awaiting payment. I compute the reference as ENR- plus its ID and keep paymentStatus as a derived compatibility value. I do not invent an expiry for an old receipt that did not save one.

**Show in code:** [Enrollment.java](../src/main/java/com/drivingschool/learningpackage/model/Enrollment.java) — `initialStatus`; [Enrollment.java](../src/main/java/com/drivingschool/learningpackage/model/Enrollment.java) — `getReferenceCode`; [Enrollment.java](../src/main/java/com/drivingschool/learningpackage/model/Enrollment.java) — `getPaymentStatus`; [EnrollmentServiceTest.java](../src/test/java/com/drivingschool/learningpackage/service/EnrollmentServiceTest.java) — `loadsOldFormatWithoutPaymentFieldsAsPaidAndActive`.

## 7. IT26101137 — Reviews, moderation and reports

### 137-01. What is your module responsible for?

In our system, I manage student reviews, admin moderation and read-only reports. I connect new reviews to a student's completed lesson and update instructor averages from approved reviews. Reports summarize existing records rather than processing real payments.

**Show in code:** [ReviewService.java](../src/main/java/com/drivingschool/review/service/ReviewService.java) — `createReview`; [ReviewService.java](../src/main/java/com/drivingschool/review/service/ReviewService.java) — `approvedAverage`; [AdminReportService.java](../src/main/java/com/drivingschool/report/service/AdminReportService.java) — `all`.

### 137-02. Where are your four CRUD operations?

I create with createReview, read with getVisibleReviews, edit with updateOwnReview and delete with deleteOwnReview in ReviewService. The author can only change their own review, while admin moderation has separate methods. ReviewRepository supplies the file operations.

**Show in code:** [ReviewService.java](../src/main/java/com/drivingschool/review/service/ReviewService.java) — `createReview`; [ReviewService.java](../src/main/java/com/drivingschool/review/service/ReviewService.java) — `getVisibleReviews`; [ReviewService.java](../src/main/java/com/drivingschool/review/service/ReviewService.java) — `updateOwnReview`; [ReviewService.java](../src/main/java/com/drivingschool/review/service/ReviewService.java) — `deleteOwnReview`.

### 137-03. How do Paid-only revenue and CSV export work?

I add the captured amounts of Paid enrollments only, so Awaiting payment and Cancelled requests do not increase revenue. The screen and CSV use ReportCatalog.all for the same report rows. The CSV helper quotes cells, doubles embedded quotes and prefixes dangerous spreadsheet formula starts.

**Show in code:** [AdminReportService.java](../src/main/java/com/drivingschool/report/service/AdminReportService.java) — `revenue`; [AdminReportService.java](../src/main/java/com/drivingschool/report/service/AdminReportService.java) — `all`; [AdminReportController.java](../src/main/java/com/drivingschool/report/controller/AdminReportController.java) — `export`; [AdminReportController.java](../src/main/java/com/drivingschool/report/controller/AdminReportController.java) — `csv`.

**Show in browser:** /admin/reports → Export CSV. **NEEDS CHECK:** rehearsal path, not a browser test performed for this document.

### 137-04. Where did you use encapsulation?

I keep rating, comment and status private in Review. updateContent validates edits and returns changed feedback to Pending moderation instead of letting a student directly publish it.

**Show in code:** [Review.java](../src/main/java/com/drivingschool/review/model/Review.java) — `updateContent`; [Review.java](../src/main/java/com/drivingschool/review/model/Review.java) — `updateStatus`.

### 137-05. Where did you use inheritance?

I made ReviewRepository extend FileRepository<Review>. Its overrides delegate to the shared storage behavior, including missing-record and damaged-file checks.

**Show in code:** [ReviewRepository.java](../src/main/java/com/drivingschool/review/repository/ReviewRepository.java) — `class declaration`; [ReviewRepository.java](../src/main/java/com/drivingschool/review/repository/ReviewRepository.java) — `update`.

### 137-06. Where did you use abstraction?

I use ReviewOperations for student/public feedback tasks and ReviewModeration for admin decisions. I also use ReportCatalog so the report controller asks for totals without knowing how the files are counted.

**Show in code:** [ReviewOperations.java](../src/main/java/com/drivingschool/review/service/ReviewOperations.java) — `getVisibleReviews`; [ReviewModeration.java](../src/main/java/com/drivingschool/review/service/ReviewModeration.java) — `approveReview`; [ReportCatalog.java](../src/main/java/com/drivingschool/report/service/ReportCatalog.java) — `all`.

### 137-07. Where did you use polymorphism?

I can use ReviewRepository through ReviewStore or its FileRepository parent and Java calls the concrete overrides. The parent writer is configured for Review records. I do not claim there are already two different ReviewStore implementations.

**Show in code:** [ReviewRepository.java](../src/main/java/com/drivingschool/review/repository/ReviewRepository.java) — `save`; [ReviewService.java](../src/main/java/com/drivingschool/review/service/ReviewService.java) — `ReviewService`.

### 137-08. What exception can your module raise?

I throw DuplicateRecordException when a completed lesson already has a review. I also reject a review for another student's lesson or one that is not completed. Hiding a review does not make the same lesson eligible for a duplicate.

**Show in code:** [ReviewService.java](../src/main/java/com/drivingschool/review/service/ReviewService.java) — `ensureNoDuplicate`; [ReviewService.java](../src/main/java/com/drivingschool/review/service/ReviewService.java) — `createReview`.

### 137-09. How does your module show Single Responsibility?

I keep review and moderation rules in ReviewService and reporting totals in AdminReportService. AdminReportController creates the response and CSV, while the JSP draws the report presentation.

**Show in code:** [ReviewService.java](../src/main/java/com/drivingschool/review/service/ReviewService.java) — `approveReview`; [AdminReportService.java](../src/main/java/com/drivingschool/report/service/AdminReportService.java) — `all`; [AdminReportController.java](../src/main/java/com/drivingschool/report/controller/AdminReportController.java) — `export`.

### 137-10. How do Open/Closed and Liskov apply?

I can add another repository child for another record type without rewriting FileRepository. ReviewRepository preserves the parent's save, update and delete promises by delegating to them. I still keep authorization in the service rather than weakening storage rules.

**Show in code:** [ReviewRepository.java](../src/main/java/com/drivingschool/review/repository/ReviewRepository.java) — `ReviewRepository`; [ReviewRepository.java](../src/main/java/com/drivingschool/review/repository/ReviewRepository.java) — `delete`; [FileRepository.java](../src/main/java/com/drivingschool/common/repository/FileRepository.java) — `delete`.

### 137-11. How do Interface Segregation and Dependency Inversion apply?

I keep moderation tasks out of the student-facing ReviewOperations interface. ReviewController receives ReviewOperations, and AdminReportController receives ReportCatalog through constructors. That is a concrete example of depending on small contracts.

**Show in code:** [ReviewModeration.java](../src/main/java/com/drivingschool/review/service/ReviewModeration.java) — `approveReview`; [ReviewController.java](../src/main/java/com/drivingschool/review/controller/ReviewController.java) — `ReviewController`; [AdminReportController.java](../src/main/java/com/drivingschool/report/controller/AdminReportController.java) — `AdminReportController`.

### 137-12. What do your tests cover and what should you not overclaim?

I can show tests for concurrent duplicate feedback, moderation, visibility, Paid-only revenue and CSV escaping. Public review methods return approved feedback, but an instructor's own feedback view follows its separate role-specific visibility rule. The reports page has the existing totals and export, not a new report-filters feature.

**Show in code:** [ReviewServiceConcurrencyTest.java](../src/test/java/com/drivingschool/review/service/ReviewServiceConcurrencyTest.java) — `twoServiceInstancesAllowOnlyOneConcurrentSubmission`; [AdminReportServiceTest.java](../src/test/java/com/drivingschool/review/service/AdminReportServiceTest.java) — `revenueExcludesWaitingAndCancelledRequests`; [AdminReportServiceTest.java](../src/test/java/com/drivingschool/review/service/AdminReportServiceTest.java) — `csvEscapesTextAndKeepsExactNumbers`; [ReviewService.java](../src/main/java/com/drivingschool/review/service/ReviewService.java) — `canSeeReview`.

## 8. Tricky what-if questions

### T01. What if two admins confirm the same payment?

I reload the enrollment inside AccountWriteCoordinator's shared lock before changing it. In one running app, the first action marks it Paid and the second sees an already-handled state and gets a friendly message. I do not claim this Java lock coordinates a second JVM or makes the two files transactional.

**Show in code:** [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `markPaid`; [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `requireAwaiting`; [AccountWriteCoordinator.java](../src/main/java/com/drivingschool/auth/util/AccountWriteCoordinator.java) — `execute`; [EnrollmentPaymentServiceTest.java](../src/test/java/com/drivingschool/learningpackage/service/EnrollmentPaymentServiceTest.java) — `concurrentConfirmationsHandleReceiptOnce`.

### T02. What if a student double-clicks Book?

I recheck payment and conflicts while holding the booking lock. On the normal instructor-slot route, an overlapping request already saved by the first click makes the second fail the slot or conflict check. There is no general request-idempotency token, so I would not call every possible repeated operation idempotent.

**Show in code:** [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `requestLesson`; [BookingConflictChecker.java](../src/main/java/com/drivingschool/booking/service/BookingConflictChecker.java) — `conflictReason`.

### T03. What if a vehicle goes into maintenance after assignment?

I save the changed vehicle status, but the edit does not automatically cancel every linked booking. Assignment, reassignment and instructor acceptance use current resource checks and can reject an unavailable vehicle. An admin must review already-confirmed lessons; I do not claim a background maintenance monitor exists.

**Show in code:** [VehicleService.java](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) — `updateVehicle`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `reassign`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `acceptAssignedLesson`; [BookingConflictChecker.java](../src/main/java/com/drivingschool/booking/service/BookingConflictChecker.java) — `conflictReason`.

### T04. What if a data file is corrupted?

I use strict repository reads for decisions and writes, so damaged data raises StorageException instead of being overwritten as though it were empty. The non-strict read methods can return readable rows while logging damaged rows, which is a different contract. I would investigate a backup and the damaged file before allowing more writes, not silently delete the data.

**Show in code:** [FileRepository.java](../src/main/java/com/drivingschool/common/repository/FileRepository.java) — `readRecords`; [FileRepository.java](../src/main/java/com/drivingschool/common/repository/FileRepository.java) — `save`; [GlobalExceptionHandler.java](../src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java) — `handleStorage`.

### T05. What if an instructor requests leave with lessons already booked?

I reject leave when that date has a Confirmed lesson for the instructor. Pending requests or assignments do not trigger that specific leave guard, but later conflict checking sees the saved leave. I would not claim every schedule edit or deletion automatically reschedules lessons.

**Show in code:** [InstructorScheduleService.java](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java) — `validateLeave`; [InstructorScheduleService.java](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java) — `ensureDayHasNoConfirmedLessons`; [BookingConflictChecker.java](../src/main/java/com/drivingschool/booking/service/BookingConflictChecker.java) — `isOnLeave`.

### T06. What if someone changes a record ID in the URL or form?

I compare the saved record's owner with the signed-in user instead of trusting the supplied ID. For example, ownEnrollment rejects another student's receipt and cancelOwnBooking rejects someone else's request. A valid-looking ID is not permission.

**Show in code:** [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `requireOwnEnrollment`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `cancelOwnBooking`; [StudentProfileController.java](../src/main/java/com/drivingschool/student/controller/StudentProfileController.java) — `configureBinding`.

### T07. What if the app restarts?

I reload saved records from the data files and rebuild the FIFO queue from Pending unassigned bookings when needed. Password reset codes are in memory and disappear when the JVM restarts. I do not rely on a previous browser login surviving the restart.

**Show in code:** [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `processNext`; [BookingRequestQueue.java](../src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java) — `BookingRequestQueue`; [PasswordManagementService.java](../src/main/java/com/drivingschool/auth/service/PasswordManagementService.java) — `createResetToken`.

### T08. What if a student cancels less than 24 hours before a lesson?

I reject a student cancellation or reschedule inside the configured notice period. The default is 24 hours, but the application property can change it. The admin force-cancel path is a different operation that records a reason.

**Show in code:** [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `cancelOwnBooking`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `rescheduleOwnBooking`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `forceCancel`; [application.properties](../src/main/resources/application.properties) — `app.booking.change-cutoff-hours`.

### T09. What if the package is deactivated before payment confirmation?

I check the package again when the admin marks payment Paid. If it is inactive, I reject confirmation and leave the request Awaiting payment. The admin can cancel the unpaid request so the student can request another package.

**Show in code:** [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `payablePackage`; [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `markPaid`; [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `cancelRequest`.

### T10. What if an admin tries to confirm payment for a Suspended student?

I refuse confirmation with a reason rather than granting lessons. Login and subsequent account reloads also check the current status. The admin must resolve the account status separately instead of using payment to bypass suspension.

**Show in code:** [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `paymentStudent`; [AuthSessionService.java](../src/main/java/com/drivingschool/auth/service/AuthSessionService.java) — `loginWithResult`; [AuthSessionService.java](../src/main/java/com/drivingschool/auth/service/AuthSessionService.java) — `getCurrentUser`.

### T11. What if an unpaid or cancelled enrollment still has a Pending queue request?

I reject matching Pending requests when an Awaiting payment enrollment is cancelled. If an invalid old request reaches queue assignment, PaidEnrollmentGuard returns a reason and assign saves it as Rejected. That keeps the queue moving without treating an unpaid request as eligible.

**Show in code:** [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `rejectPendingRequests`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `assign`; [PaidEnrollmentGuard.java](../src/main/java/com/drivingschool/booking/service/PaidEnrollmentGuard.java) — `problemFor`.

### T12. What if no lessons remain, or the enrollment has expired?

I refuse a new booking when the active Paid receipt has no remaining lesson balance or its saved expiry is before today. An old Paid receipt with no saved expiry is not given a guessed expiry. I do not automatically renew or sell a new package in that guard.

**Show in code:** [PaidEnrollmentGuard.java](../src/main/java/com/drivingschool/booking/service/PaidEnrollmentGuard.java) — `matchesBalance`; [PaidEnrollmentGuard.java](../src/main/java/com/drivingschool/booking/service/PaidEnrollmentGuard.java) — `packageProblem`.

### T13. What if Complete is clicked twice, or the student is a no-show?

I only allow Booking.complete from Confirmed, so a normal repeat cannot deduct another lesson after the first completion. I reduce the student's lesson balance after saving completion; request creation does not reduce it. The no-show method changes status without calling the lesson-decrement helper.

**Show in code:** [Booking.java](../src/main/java/com/drivingschool/booking/model/Booking.java) — `complete`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `completeAssignedLesson`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `markNoShowAssignedLesson`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `decrementPackageLessons`.

### T14. What if the app crashes between updating two files?

I cannot promise a cross-file rollback because we have no database transaction. The repository atomically replaces an individual file, but payment activation and lesson completion each involve more than one file. I would inspect and reconcile affected records rather than claim our lock repairs a crash.

**Show in code:** [FileRepository.java](../src/main/java/com/drivingschool/common/repository/FileRepository.java) — `atomicWrite`; [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `markPaid`; [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) — `completeAssignedLesson`.

### T15. What if a student edits an approved review or tries to review the same lesson again?

I send changed review content back to Pending and recalculate its instructor's approved average. I reject another review for the same completed lesson even if the earlier review is hidden. Only an admin can approve or hide reviews through moderation methods.

**Show in code:** [Review.java](../src/main/java/com/drivingschool/review/model/Review.java) — `updateContent`; [ReviewService.java](../src/main/java/com/drivingschool/review/service/ReviewService.java) — `updateOwnReview`; [ReviewService.java](../src/main/java/com/drivingschool/review/service/ReviewService.java) — `ensureNoDuplicate`; [ReviewService.java](../src/main/java/com/drivingschool/review/service/ReviewService.java) — `approveReview`.

## 9. Group questions

Local read-only history includes `7d4bdbd` (develop integration), `041150f` (PR #36, enrollment) and `392050d` (PR #32, instructor booking contracts). This shows recorded merge subjects; it is not evidence of who reviewed them, that all current remote branches match, or that every member used a particular workflow.

### G01. Who owns which parts of the project?

**NEEDS CHECK:** Confirm each member's actual contribution before describing personal work.

I explain the agreed ownership by ID: IT26101135 handles accounts and integration, IT26101133 instructors, IT26101134 bookings, IT26101132 vehicles and sample data, IT26101136 packages, and IT26101137 reviews and reports. I use source headers and the file plan as ownership evidence, not proof that one person wrote every line alone.

**Show in code:** [GIT_BRANCH_PLAN.md](../docs/GIT_BRANCH_PLAN.md) — `ownership manifest`; [AuthController.java](../src/main/java/com/drivingschool/auth/controller/AuthController.java) — `class declaration`; [InstructorSorter.java](../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java) — `class declaration`.

### G02. How did the group use Git and pull requests?

**NEEDS CHECK:** Local history is evidence of recorded merges; who reviewed each PR and current GitHub state are not verified.

I can show local history containing feature PR merges and the later develop integration merge. Our branch plan gives the assigned paths and recommends one integration PR because the modules depend on each other. I would only describe reviews, approvals or remote branch status that the group can actually show.

**Show in code:** [GIT_BRANCH_PLAN.md](../docs/GIT_BRANCH_PLAN.md) — `merge order and integration alternative`.

### G03. How did the group test the system?

I can show JUnit tests for rules and repositories, MockMvc tests for web and security behavior, and the manual checklist. The current existing XML reports record 663 passing tests, but I separate that from browser checks or a full build I did not run for this document. I rehearse writes with temporary demo data.

**Show in code:** [SchedulingWebIntegrationTest.java](../src/test/java/com/drivingschool/booking/SchedulingWebIntegrationTest.java) — `test class`; [DemoDataSeedRunnerTest.java](../src/test/java/com/drivingschool/sample/DemoDataSeedRunnerTest.java) — `seedCanRunTwiceWithoutDuplicateRecords`; [TEST_CHECKLIST.md](../docs/TEST_CHECKLIST.md) — `role checklists`.

### G04. How did you handle merge conflicts?

**NEEDS CHECK:** Confirm the team's exact conflict-resolution actions and review history; this is a truthful method to explain, not an invented account of personal work.

I would compare both versions, keep the intended behavior and run the relevant tests after resolving a conflict. For the instructor merge, I can explain why both availability families were retained rather than deleting a teammate's tests. I would not say that our two stores are already unified.

**Show in code:** [MERGE_ANALYSIS.md](../docs/MERGE_ANALYSIS.md) — `instructor configuration and separate storage analysis`; [InstructorConfig.java](../src/main/java/com/drivingschool/instructor/config/InstructorConfig.java) — `instructorLookup`.

### G05. What was the hardest bug?

**NEEDS CHECK:** The technical example is in source; the team's personal judgement and prior reproduction need confirmation.

For a concrete example, I would explain the Packages menu closing while the pointer crossed toward its panel. The current code cancels closing on re-entry, waits 220 milliseconds before a real pointer-leave close and ignores height-only resize. I would only call it our hardest bug if the group agrees that reflects our experience.

**Show in code:** [site.js](../src/main/resources/static/js/site.js) — `preparePackageMenu`.

## 10. Questions you must not bluff

Use these honest lines when asked about a feature beyond the project. They describe limits, not an approved plan to add features.

| Topic | What I can honestly say | Show in code |
| --- | --- | --- |
| Payments | I record a manual admin decision; I do not connect to a bank, charge a card, automatically verify a receipt or issue refunds. | [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `markPaid, cancelRequest` |
| Email and receipt upload | I show a configurable admin address and instructions. The app does not send payment emails or upload receipt files. | [EnrollmentController.java](../src/main/java/com/drivingschool/learningpackage/controller/EnrollmentController.java) — `payment`; [payment-instructions.jsp](../src/main/webapp/WEB-INF/views/student/payment-instructions.jsp) — `payment instructions` |
| Password recovery | I display a local demonstration token instead of emailing it. I do not present this as a production recovery service. | [PasswordController.java](../src/main/java/com/drivingschool/auth/controller/PasswordController.java) — `requestReset`; [PasswordManagementService.java](../src/main/java/com/drivingschool/auth/service/PasswordManagementService.java) — `createResetToken` |
| Transactions and multiple servers | I coordinate selected writes inside one JVM and atomically replace individual files. I cannot promise all-or-nothing writes across files or protection against another JVM. | [FileRepository.java](../src/main/java/com/drivingschool/common/repository/FileRepository.java) — `atomicWrite`; [AccountWriteCoordinator.java](../src/main/java/com/drivingschool/auth/util/AccountWriteCoordinator.java) — `execute` |
| Recovery and scale | I have no automatic database recovery or production backup service. Whole-file reads/replacements are a small-project storage design, not a large-scale database. | [FileRepository.java](../src/main/java/com/drivingschool/common/repository/FileRepository.java) — `readRecords, writeRecords` |
| Vehicle warnings | I calculate warning badges from dates. I do not automatically send reminders, inspect cars or cancel their bookings. | [Vehicle.java](../src/main/java/com/drivingschool/vehicle/model/Vehicle.java) — `withinThirtyDays`; [VehicleService.java](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) — `updateVehicle` |
| Enrollment fields | I compute the reference from the ID and paymentStatus from the real status. The working receipt saves enrolledAt and validUntil, not a stored validFrom payment-confirmation timestamp. | [Enrollment.java](../src/main/java/com/drivingschool/learningpackage/model/Enrollment.java) — `getReferenceCode, getPaymentStatus`; [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) — `markPaid` |
| Reports | I count captured Paid amounts as revenue; enrollment-count charts can include other states. The existing reports and CSV do not include the rejected report-filters feature. | [AdminReportService.java](../src/main/java/com/drivingschool/report/service/AdminReportService.java) — `revenue, enrollmentsByPackage, enrollmentsByMonth` |
| Design and images | I use DriveSpark content and local assets with README credits. I do not claim Rivian assets or endorsement. | [README.md](../README.md) — `Credits`; [README.md](../src/main/resources/static/images/README.md) — `photo credits` |
| Real school details | I identify sample numbers, placeholder contact/location details and demonstration records as samples. A polished interface does not make those details real. | [footer.jspf](../src/main/webapp/WEB-INF/views/common/footer.jspf) — `fine print`; [DemoDataSeedRunner.java](../src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java) — `run` |
| Testing | I can quote the existing 663/0/0/0 XML results with their date. I cannot say I reran Maven or inspected every page while producing this guide. | [pom.xml](../pom.xml) — `test dependency`; [TEST_CHECKLIST.md](../docs/TEST_CHECKLIST.md) — `browser rehearsal checklist` |

### Small terms to explain plainly

- **Interface:** a short promise of available methods; a class implements the work.
- **Abstract class:** a parent that cannot be created on its own; a child completes its abstract methods.
- **Generic T:** a placeholder for a record type; FileRepository<Booking> applies shared file code to bookings.
- **Record:** a small read-only Java data card with generated constructor/accessors, such as StudentSummary; it is not automatically a saved entity or an implemented feature.
- **Lock / synchronized:** one thread enters a protected section at a time within that JVM; it is not a transaction.
- **MVC:** controller handles the request, model carries data and rules, and the JSP view displays it. Services and repositories further organize that model work.
- **OOP / SOLID:** concrete examples explain choices; labels do not prove that every class is perfectly decoupled. The booking checker still uses concrete repositories.

## 11. Printable member cheat sheets

Each following sheet is compact and intended for one A4 page when printed on its own. **NEEDS CHECK:** actual page fit depends on the Markdown/PDF renderer and font size; no print preview was run. Print from one member heading to its divider. The complete current file inventory is in section 12; keep that long appendix out of the one-page print.

The five-interaction demos start at the stated page with prepared temporary accounts/data. Typing values is preparation within a step; selecting an option counts as an interaction. Where needed, use a second already-open admin session. All scripts are **NEEDS CHECK** until rehearsed; extra native-control or navigation interactions may be needed.

<div style="break-before: page; page-break-before: always;"></div>

### IT26101135 — Authentication, Students and integration

**My key Java files:** [AuthController](../src/main/java/com/drivingschool/auth/controller/AuthController.java), [AuthService](../src/main/java/com/drivingschool/auth/service/AuthService.java), [AuthSessionService](../src/main/java/com/drivingschool/auth/service/AuthSessionService.java), [User](../src/main/java/com/drivingschool/auth/model/User.java), [Student](../src/main/java/com/drivingschool/student/model/Student.java), [StudentProfileService](../src/main/java/com/drivingschool/student/service/StudentProfileService.java), [StudentProfiles](../src/main/java/com/drivingschool/student/api/StudentProfiles.java), [StudentAccountService](../src/main/java/com/drivingschool/student/service/StudentAccountService.java), [UserRepository](../src/main/java/com/drivingschool/auth/repository/UserRepository.java).

**My JSPs:** `auth/login.jsp`, `auth/register.jsp`, `student/profile.jsp`, `student/profile-edit.jsp`.

**CSS/JS:** Build configuration: `tailwind.config.js` (IT26101135). Shared runtime CSS/JS and layout fragments belong to IT26101136.

**CRUD:** CREATE `AuthService.registerStudent`; READ `StudentProfileService.getOwnProfile`; UPDATE `StudentProfileService.updateOwnProfile`; DELETE `StudentAccountService.deleteOwnAccount`.

**Five OOP topics:** Encapsulation: `Student.setPhone`. Inheritance: `Student extends User`. Abstraction: `User.getRole`, `StudentProfiles`. Polymorphism: account subclasses' `getDashboardPath`. Exceptions: `StudentProfileService.ensureAvailableEmail` throws `DuplicateRecordException`.

**SOLID:** S: controller/web, service/rules, repository/files. O: new User subtype supplies its role/destination. L: Student works through User. I: StudentProfiles is small. D: StudentProfileController receives StudentProfiles.

**Five-interaction demo — NEEDS CHECK.** Start: Visitor at `/register`, fresh temporary student account.

1. Click Create account after entering valid registration details.
2. Click Log in after entering the new credentials.
3. Click Profile in the student sidebar.
4. Click Save changes after editing the phone or address.
5. Click Log out.

**Say this limitation:** Password recovery shows a demo token; no email is sent. Deletion is blocked by attached bookings, reviews or enrollments.

---

<div style="break-before: page; page-break-before: always;"></div>

### IT26101133 — Instructors, availability and bubble sort

**My key Java files:** [Instructor](../src/main/java/com/drivingschool/instructor/model/Instructor.java), [InstructorService](../src/main/java/com/drivingschool/instructor/service/InstructorService.java), [InstructorSorter](../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java), [InstructorOrder](../src/main/java/com/drivingschool/instructor/service/InstructorOrder.java), [InstructorRepository](../src/main/java/com/drivingschool/instructor/repository/InstructorRepository.java), [InstructorScheduleService](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java), [InstructorAvailabilityController](../src/main/java/com/drivingschool/instructor/controller/InstructorAvailabilityController.java).

**My JSPs:** `public/instructors.jsp`, `instructor/availability.jsp`, `instructor/lesson-dashboard.jsp`.

**CSS/JS:** No separate module CSS/JS file; uses the shared stylesheet, script and navbar.

**CRUD:** Profile C/R/U/D: `InstructorService.createInstructor/getInstructorById/updateInstructor/deleteInstructor`. Weekly hours: `InstructorScheduleService.addAvailability/availability/updateAvailability/deleteAvailability`.

**Five OOP topics:** Encapsulation: `Instructor.setYearsOfExperience`. Inheritance: InstructorRepository → FileRepository. Abstraction: InstructorOrder.compare. Polymorphism: name/experience/rating implementations. Exceptions: schedule ensureNoOverlap → DuplicateRecordException.

**SOLID:** S: sorter only orders. O: add comparison without changing sort passes (selector still changes). L: orders keep compare's sign meaning. I: one-method InstructorOrder. D: availability controller receives InstructorScheduleOperations.

**Five-interaction demo — NEEDS CHECK.** Start: Visitor at `/instructors`. Prepare the page before beginning; these five interactions show sorting.

1. Choose Experience in Sort by.
2. Choose Ascending in Order.
3. Click Sort and explain neighboring swaps.
4. Choose Average rating in Sort by.
5. Click Sort again.

**Say this limitation:** Worst-case bubble sort is O(n²); queue restore uses a different algorithm. The working schedule uses weekly availability and leave records.

---

<div style="break-before: page; page-break-before: always;"></div>

### IT26101134 — Bookings, FIFO and scheduling

**My key Java files:** [Booking](../src/main/java/com/drivingschool/booking/model/Booking.java), [BookingService](../src/main/java/com/drivingschool/booking/service/BookingService.java), [StudentBookingOperations](../src/main/java/com/drivingschool/booking/service/StudentBookingOperations.java), [BookingController](../src/main/java/com/drivingschool/booking/controller/BookingController.java), [AdminBookingController](../src/main/java/com/drivingschool/booking/controller/AdminBookingController.java), [BookingRequestQueue](../src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java), [BookingConflictChecker](../src/main/java/com/drivingschool/booking/service/BookingConflictChecker.java), [PaidEnrollmentGuard](../src/main/java/com/drivingschool/booking/service/PaidEnrollmentGuard.java).

**My JSPs:** `booking/list.jsp`, `booking/request-card.jspf`, `admin/bookings.jsp`.

**CSS/JS:** No separate module CSS/JS file; uses shared components and script.

**CRUD:** `BookingService`: CREATE requestLesson; READ getOwnBookings; UPDATE rescheduleOwnBooking; DELETE deleteCancelledBooking. Cancellation itself is an UPDATE.

**Five OOP topics:** Encapsulation: Booking state methods. Inheritance: BookingRepository → FileRepository. Abstraction: BookingOrder. Polymorphism: PendingRequestOrder vs LessonTimeOrder. Exceptions: PaidEnrollmentGuard.requirePaid → ValidationException.

**SOLID:** S: queue keeps order; checker checks conflicts. O: add BookingOrder. L: both orders return safe new lists for their purpose. I: student/admin operation contracts. D: BookingController receives StudentBookingOperations.

**Five-interaction demo — NEEDS CHECK.** Start: Student at `/bookings`, with a Paid active package and a prepared valid date. Type the date before step 2; temporary data only.

1. Choose an instructor.
2. Click Find available times for the prepared date.
3. Select an offered future lesson time.
4. Click Send lesson request.
5. Click Change this request on the saved request; explain its FIFO position and notice rule.

**Say this limitation:** enqueue/dequeue O(1); queue reconstruction can be O(n²). Admin assignment stays Pending until instructor acceptance. Cross-file updates are not transactional.

---

<div style="break-before: page; page-break-before: always;"></div>

### IT26101132 — Vehicles, sample data and test checklist

**My key Java files:** [Vehicle](../src/main/java/com/drivingschool/vehicle/model/Vehicle.java), [VehicleController](../src/main/java/com/drivingschool/vehicle/controller/VehicleController.java), [VehicleService](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java), [VehicleOperations](../src/main/java/com/drivingschool/vehicle/api/VehicleOperations.java), [VehicleStore](../src/main/java/com/drivingschool/vehicle/api/VehicleStore.java), [VehicleRepository](../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java), [DemoDataSeedRunner](../src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java).

**My JSPs:** `vehicle/list.jsp`, `vehicle/create.jsp`, `vehicle/edit.jsp`; checklist `docs/TEST_CHECKLIST.md`.

**CSS/JS:** No separate module CSS/JS file; uses shared fleet-form styles.

**CRUD:** `VehicleService`: CREATE createVehicle; READ getAllVehicles; UPDATE updateVehicle; DELETE deleteVehicle. History-linked vehicles are protected.

**Five OOP topics:** Encapsulation: Vehicle.setNextServiceDate. Inheritance: VehicleRepository → FileRepository. Abstraction: VehicleOperations/VehicleStore. Polymorphism: repository overrides through its parent/interface. Exceptions: duplicate registration → DuplicateRecordException.

**SOLID:** S: service checks rules; repository saves. O: new repository child can choose a record/file. L: child keeps shared update rules. I: VehicleOperations separates fleet actions from VehicleStore storage. D: controller receives VehicleOperations.

**Five-interaction demo — NEEDS CHECK.** Start: Admin at `/admin/vehicles`, unique prepared registration and valid date values; temporary data only.

1. Click the add/create vehicle link.
2. Click Create vehicle after filling the form.
3. Click Edit on that vehicle.
4. Click Save changes after editing the next-service date.
5. Click Vehicles to read the saved card and warning.

**Say this limitation:** Warnings include overdue dates and dates up to 30 days ahead; they do not auto-cancel bookings. Repeated seeding is tested, not a backup/restore feature.

---

<div style="break-before: page; page-break-before: always;"></div>

### IT26101136 — Learning packages, enrollment and payment

**My key Java files:** [LearningPackage](../src/main/java/com/drivingschool/learningpackage/model/LearningPackage.java), [Enrollment](../src/main/java/com/drivingschool/learningpackage/model/Enrollment.java), [LearningRecord](../src/main/java/com/drivingschool/learningpackage/model/LearningRecord.java), [LearningPackageService](../src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java), [EnrollmentService](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java), [EnrollmentAdministration](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentAdministration.java), [EnrollmentController](../src/main/java/com/drivingschool/learningpackage/controller/EnrollmentController.java), [AdminEnrollmentController](../src/main/java/com/drivingschool/learningpackage/controller/AdminEnrollmentController.java).

**My JSPs:** `learningpackage/list.jsp`, `learningpackage/detail.jsp`, `student/enrollments.jsp`, `student/payment-instructions.jsp`, `admin/enrollments.jsp`.

**CSS/JS:** IT26101136 owns `app.css`, built `tailwind.css`, `site.js`, shared navbar/footer and UI fragments. These files are shared by every module.

**CRUD:** Package: LearningPackageService createPackage/listPackages/updatePackage/deletePackage. Enrollment: EnrollmentService enroll/ownEnrollments/updateLearningGoal/deleteOwnEnrollment.

**Five OOP topics:** Encapsulation: Enrollment.setStatus. Inheritance: repositories → FileRepository. Abstraction: LearningRecord. Polymorphism: course/receipt getDisplayName. Exceptions: requireAwaiting/paymentStudent/payablePackage → friendly validation errors.

**SOLID:** S: service handles payment rules. O: add LearningRecord implementation. L: each supplies ID/title. I: student and admin interfaces separate. D: controllers receive EnrollmentOperations/EnrollmentAdministration.

**Five-interaction demo — NEEDS CHECK.** Start: Student at `/packages`, no active/waiting enrollment; a second prepared admin session. Temporary data only.

1. Click View package on an active course.
2. Click Enroll in this package; read the payment instructions.
3. Click Awaiting payment in the prepared admin session.
4. Click Mark as Paid for the matching reference.
5. Click Continue in the confirmation dialog; explain the unlocked balance. Refresh the student's enrollments afterwards as an extra check.

**Say this limitation:** Payment is manual; no email or receipt upload. Cancelled/Paid history is protected.

---

<div style="break-before: page; page-break-before: always;"></div>

### IT26101137 — Reviews, moderation and reports

**My key Java files:** [Review](../src/main/java/com/drivingschool/review/model/Review.java), [ReviewService](../src/main/java/com/drivingschool/review/service/ReviewService.java), [ReviewOperations](../src/main/java/com/drivingschool/review/service/ReviewOperations.java), [ReviewModeration](../src/main/java/com/drivingschool/review/service/ReviewModeration.java), [ReviewRepository](../src/main/java/com/drivingschool/review/repository/ReviewRepository.java), [AdminReportService](../src/main/java/com/drivingschool/report/service/AdminReportService.java), [ReportCatalog](../src/main/java/com/drivingschool/report/service/ReportCatalog.java), [AdminReportController](../src/main/java/com/drivingschool/report/controller/AdminReportController.java).

**My JSPs:** `review/list.jsp`, `admin/reviews.jsp`, `admin/reports.jsp`.

**CSS/JS:** IT26101137 owns `static/css/report.css` for report charts and print. Reports also use the shared app.css and site.js owned by IT26101136.

**CRUD:** `ReviewService`: CREATE createReview; READ getVisibleReviews; UPDATE updateOwnReview; DELETE deleteOwnReview. Admin approve/hide are UPDATE.

**Five OOP topics:** Encapsulation: Review.updateContent. Inheritance: ReviewRepository → FileRepository. Abstraction: ReviewOperations/ReportCatalog. Polymorphism: concrete repository overrides through parent/interface. Exceptions: ensureNoDuplicate → DuplicateRecordException.

**SOLID:** S: report service counts; controller exports. O: shared storage accepts new repository children. L: child keeps delete checks. I: moderation separate from student tasks. D: AdminReportController receives ReportCatalog.

**Five-interaction demo — NEEDS CHECK.** Start: Admin at `/admin/reviews` with a Pending demo review for a completed lesson; temporary data only.

1. Click Approve on the prepared Pending review.
2. Click Instructors and inspect the approved rating.
3. Click Dashboard to return to admin navigation.
4. Click Reports.
5. Click Export CSV.

**Say this limitation:** Public feedback is Approved only; author/instructor/admin visibility differs. Paid-only revenue uses captured enrollment amounts. No new report-filter feature exists.

---


## 12. Complete current code-file inventory by member

This appendix makes “my files” precise without making the printed cheat sheet several pages long. It covers **298 current Java, JSP/JSPF, CSS/JS and test files**: 160 main Java files, 56 JSP/JSPF files, five CSS/JS/build-script files and 77 Java test files. It excludes photos, fonts, data, Maven output, npm dependencies and general documentation; their project ownership is outside this code inventory.

A member header or existing “Module owner” comment is used when present. **† NEEDS CHECK** means no owner comment was found near the file's top, so ownership was inferred from the agreed module. This is an ownership assumption, not proof of individual authorship; no header was changed. Each file is listed exactly once.

<details>
<summary>IT26101135 — 105 code/test files (expand)</summary>

**Java (63):**

- [src/main/java/com/drivingschool/admin/controller/AdminDashboardController.java](../src/main/java/com/drivingschool/admin/controller/AdminDashboardController.java)
- [src/main/java/com/drivingschool/admin/controller/AdminManagementController.java](../src/main/java/com/drivingschool/admin/controller/AdminManagementController.java)
- [src/main/java/com/drivingschool/auth/annotation/RequireAdmin.java](../src/main/java/com/drivingschool/auth/annotation/RequireAdmin.java)
- [src/main/java/com/drivingschool/auth/annotation/RequireLogin.java](../src/main/java/com/drivingschool/auth/annotation/RequireLogin.java)
- [src/main/java/com/drivingschool/auth/config/AdminSeedRunner.java](../src/main/java/com/drivingschool/auth/config/AdminSeedRunner.java)
- [src/main/java/com/drivingschool/auth/controller/AuthController.java](../src/main/java/com/drivingschool/auth/controller/AuthController.java)
- [src/main/java/com/drivingschool/auth/controller/PasswordController.java](../src/main/java/com/drivingschool/auth/controller/PasswordController.java)
- [src/main/java/com/drivingschool/auth/filter/CsrfFilter.java](../src/main/java/com/drivingschool/auth/filter/CsrfFilter.java)
- [src/main/java/com/drivingschool/auth/form/LoginForm.java](../src/main/java/com/drivingschool/auth/form/LoginForm.java)
- [src/main/java/com/drivingschool/auth/form/RegistrationForm.java](../src/main/java/com/drivingschool/auth/form/RegistrationForm.java)
- [src/main/java/com/drivingschool/auth/interceptor/AccessInterceptor.java](../src/main/java/com/drivingschool/auth/interceptor/AccessInterceptor.java)
- [src/main/java/com/drivingschool/auth/interceptor/NavigationInterceptor.java](../src/main/java/com/drivingschool/auth/interceptor/NavigationInterceptor.java)
- [src/main/java/com/drivingschool/auth/model/AccountStatus.java](../src/main/java/com/drivingschool/auth/model/AccountStatus.java)
- [src/main/java/com/drivingschool/auth/model/Admin.java](../src/main/java/com/drivingschool/auth/model/Admin.java)
- [src/main/java/com/drivingschool/auth/model/InstructorAccount.java](../src/main/java/com/drivingschool/auth/model/InstructorAccount.java)
- [src/main/java/com/drivingschool/auth/model/Role.java](../src/main/java/com/drivingschool/auth/model/Role.java)
- [src/main/java/com/drivingschool/auth/model/User.java](../src/main/java/com/drivingschool/auth/model/User.java)
- [src/main/java/com/drivingschool/auth/repository/UserRepository.java](../src/main/java/com/drivingschool/auth/repository/UserRepository.java)
- [src/main/java/com/drivingschool/auth/service/AdminSeedService.java](../src/main/java/com/drivingschool/auth/service/AdminSeedService.java)
- [src/main/java/com/drivingschool/auth/service/AuthService.java](../src/main/java/com/drivingschool/auth/service/AuthService.java)
- [src/main/java/com/drivingschool/auth/service/AuthSessionService.java](../src/main/java/com/drivingschool/auth/service/AuthSessionService.java)
- [src/main/java/com/drivingschool/auth/service/CsrfTokenService.java](../src/main/java/com/drivingschool/auth/service/CsrfTokenService.java)
- [src/main/java/com/drivingschool/auth/service/LoginResult.java](../src/main/java/com/drivingschool/auth/service/LoginResult.java)
- [src/main/java/com/drivingschool/auth/service/PasswordManagementService.java](../src/main/java/com/drivingschool/auth/service/PasswordManagementService.java)
- [src/main/java/com/drivingschool/auth/util/AccountWriteCoordinator.java](../src/main/java/com/drivingschool/auth/util/AccountWriteCoordinator.java)
- [src/main/java/com/drivingschool/auth/util/PasswordHasher.java](../src/main/java/com/drivingschool/auth/util/PasswordHasher.java)
- [src/main/java/com/drivingschool/common/config/CustomErrorViewResolver.java](../src/main/java/com/drivingschool/common/config/CustomErrorViewResolver.java)
- [src/main/java/com/drivingschool/common/config/WebConfig.java](../src/main/java/com/drivingschool/common/config/WebConfig.java)
- [src/main/java/com/drivingschool/common/controller/HomeController.java](../src/main/java/com/drivingschool/common/controller/HomeController.java)
- [src/main/java/com/drivingschool/common/controller/PublicPagesController.java](../src/main/java/com/drivingschool/common/controller/PublicPagesController.java)
- [src/main/java/com/drivingschool/common/controller/SetupController.java](../src/main/java/com/drivingschool/common/controller/SetupController.java)
- [src/main/java/com/drivingschool/common/exception/DuplicateRecordException.java](../src/main/java/com/drivingschool/common/exception/DuplicateRecordException.java)
- [src/main/java/com/drivingschool/common/exception/ErrorPageSupport.java](../src/main/java/com/drivingschool/common/exception/ErrorPageSupport.java)
- [src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java](../src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java)
- [src/main/java/com/drivingschool/common/exception/RecordNotFoundException.java](../src/main/java/com/drivingschool/common/exception/RecordNotFoundException.java)
- [src/main/java/com/drivingschool/common/exception/StorageException.java](../src/main/java/com/drivingschool/common/exception/StorageException.java)
- [src/main/java/com/drivingschool/common/exception/ValidationException.java](../src/main/java/com/drivingschool/common/exception/ValidationException.java)
- [src/main/java/com/drivingschool/common/model/ContactMessage.java](../src/main/java/com/drivingschool/common/model/ContactMessage.java)
- [src/main/java/com/drivingschool/common/model/DashboardSummary.java](../src/main/java/com/drivingschool/common/model/DashboardSummary.java)
- [src/main/java/com/drivingschool/common/model/Identifiable.java](../src/main/java/com/drivingschool/common/model/Identifiable.java)
- [src/main/java/com/drivingschool/common/repository/ContactMessageRepository.java](../src/main/java/com/drivingschool/common/repository/ContactMessageRepository.java)
- [src/main/java/com/drivingschool/common/repository/FileRepository.java](../src/main/java/com/drivingschool/common/repository/FileRepository.java)
- [src/main/java/com/drivingschool/common/service/ContactService.java](../src/main/java/com/drivingschool/common/service/ContactService.java)
- [src/main/java/com/drivingschool/common/service/DashboardSummaryProvider.java](../src/main/java/com/drivingschool/common/service/DashboardSummaryProvider.java)
- [src/main/java/com/drivingschool/common/service/DashboardSummaryService.java](../src/main/java/com/drivingschool/common/service/DashboardSummaryService.java)
- [src/main/java/com/drivingschool/common/util/IdGenerator.java](../src/main/java/com/drivingschool/common/util/IdGenerator.java)
- [src/main/java/com/drivingschool/DrivesparkAcademyApplication.java](../src/main/java/com/drivingschool/DrivesparkAcademyApplication.java)
- [src/main/java/com/drivingschool/ServletInitializer.java](../src/main/java/com/drivingschool/ServletInitializer.java)
- [src/main/java/com/drivingschool/student/api/StudentLookup.java](../src/main/java/com/drivingschool/student/api/StudentLookup.java)
- [src/main/java/com/drivingschool/student/api/StudentProfiles.java](../src/main/java/com/drivingschool/student/api/StudentProfiles.java)
- [src/main/java/com/drivingschool/student/controller/StudentAccountController.java](../src/main/java/com/drivingschool/student/controller/StudentAccountController.java)
- [src/main/java/com/drivingschool/student/controller/StudentPasswordController.java](../src/main/java/com/drivingschool/student/controller/StudentPasswordController.java)
- [src/main/java/com/drivingschool/student/controller/StudentProfileController.java](../src/main/java/com/drivingschool/student/controller/StudentProfileController.java)
- [src/main/java/com/drivingschool/student/controller/StudentProgressController.java](../src/main/java/com/drivingschool/student/controller/StudentProgressController.java)
- [src/main/java/com/drivingschool/student/dto/StudentProfile.java](../src/main/java/com/drivingschool/student/dto/StudentProfile.java)
- [src/main/java/com/drivingschool/student/dto/StudentProgress.java](../src/main/java/com/drivingschool/student/dto/StudentProgress.java)
- [src/main/java/com/drivingschool/student/dto/StudentSummary.java](../src/main/java/com/drivingschool/student/dto/StudentSummary.java)
- [src/main/java/com/drivingschool/student/form/StudentProfileForm.java](../src/main/java/com/drivingschool/student/form/StudentProfileForm.java)
- [src/main/java/com/drivingschool/student/model/Student.java](../src/main/java/com/drivingschool/student/model/Student.java)
- [src/main/java/com/drivingschool/student/service/StudentAccountService.java](../src/main/java/com/drivingschool/student/service/StudentAccountService.java)
- [src/main/java/com/drivingschool/student/service/StudentAdministrationService.java](../src/main/java/com/drivingschool/student/service/StudentAdministrationService.java)
- [src/main/java/com/drivingschool/student/service/StudentLookupService.java](../src/main/java/com/drivingschool/student/service/StudentLookupService.java)
- [src/main/java/com/drivingschool/student/service/StudentProfileService.java](../src/main/java/com/drivingschool/student/service/StudentProfileService.java)

**JSP/JSPF (13):**

- [src/main/webapp/WEB-INF/views/admin/dashboard.jsp](../src/main/webapp/WEB-INF/views/admin/dashboard.jsp)
- [src/main/webapp/WEB-INF/views/admin/students.jsp](../src/main/webapp/WEB-INF/views/admin/students.jsp)
- [src/main/webapp/WEB-INF/views/auth/forgot-password.jsp](../src/main/webapp/WEB-INF/views/auth/forgot-password.jsp)
- [src/main/webapp/WEB-INF/views/auth/login.jsp](../src/main/webapp/WEB-INF/views/auth/login.jsp)
- [src/main/webapp/WEB-INF/views/auth/register.jsp](../src/main/webapp/WEB-INF/views/auth/register.jsp)
- [src/main/webapp/WEB-INF/views/common/error-403.jsp](../src/main/webapp/WEB-INF/views/common/error-403.jsp)
- [src/main/webapp/WEB-INF/views/common/error-404.jsp](../src/main/webapp/WEB-INF/views/common/error-404.jsp)
- [src/main/webapp/WEB-INF/views/common/error-500.jsp](../src/main/webapp/WEB-INF/views/common/error-500.jsp)
- [src/main/webapp/WEB-INF/views/home/index.jsp](../src/main/webapp/WEB-INF/views/home/index.jsp)
- [src/main/webapp/WEB-INF/views/student/password.jsp](../src/main/webapp/WEB-INF/views/student/password.jsp)
- [src/main/webapp/WEB-INF/views/student/profile-edit.jsp](../src/main/webapp/WEB-INF/views/student/profile-edit.jsp)
- [src/main/webapp/WEB-INF/views/student/profile.jsp](../src/main/webapp/WEB-INF/views/student/profile.jsp)
- [src/main/webapp/WEB-INF/views/student/progress.jsp](../src/main/webapp/WEB-INF/views/student/progress.jsp)

**CSS/JS (1):**

- [tailwind.config.js](../tailwind.config.js)

**Tests (28):**

- [src/test/java/com/drivingschool/admin/AdminDashboardWebIntegrationTest.java](../src/test/java/com/drivingschool/admin/AdminDashboardWebIntegrationTest.java)
- [src/test/java/com/drivingschool/auth/AuthWebIntegrationTest.java](../src/test/java/com/drivingschool/auth/AuthWebIntegrationTest.java)
- [src/test/java/com/drivingschool/auth/config/AdminSeedRunnerTest.java](../src/test/java/com/drivingschool/auth/config/AdminSeedRunnerTest.java)
- [src/test/java/com/drivingschool/auth/controller/AuthControllerTest.java](../src/test/java/com/drivingschool/auth/controller/AuthControllerTest.java)
- [src/test/java/com/drivingschool/auth/controller/PackageEnrollmentReturnTest.java](../src/test/java/com/drivingschool/auth/controller/PackageEnrollmentReturnTest.java)
- [src/test/java/com/drivingschool/auth/filter/CsrfFilterTest.java](../src/test/java/com/drivingschool/auth/filter/CsrfFilterTest.java)
- [src/test/java/com/drivingschool/auth/interceptor/AccessInterceptorTest.java](../src/test/java/com/drivingschool/auth/interceptor/AccessInterceptorTest.java)
- [src/test/java/com/drivingschool/auth/interceptor/NavigationInterceptorTest.java](../src/test/java/com/drivingschool/auth/interceptor/NavigationInterceptorTest.java)
- [src/test/java/com/drivingschool/auth/model/UserModelTest.java](../src/test/java/com/drivingschool/auth/model/UserModelTest.java)
- [src/test/java/com/drivingschool/auth/repository/StudentEnrollmentBalanceTest.java](../src/test/java/com/drivingschool/auth/repository/StudentEnrollmentBalanceTest.java)
- [src/test/java/com/drivingschool/auth/repository/UserRepositoryTest.java](../src/test/java/com/drivingschool/auth/repository/UserRepositoryTest.java)
- [src/test/java/com/drivingschool/auth/service/AdminSeedServiceTest.java](../src/test/java/com/drivingschool/auth/service/AdminSeedServiceTest.java)
- [src/test/java/com/drivingschool/auth/service/AuthServiceTest.java](../src/test/java/com/drivingschool/auth/service/AuthServiceTest.java)
- [src/test/java/com/drivingschool/auth/service/AuthSessionServiceTest.java](../src/test/java/com/drivingschool/auth/service/AuthSessionServiceTest.java)
- [src/test/java/com/drivingschool/auth/service/CsrfTokenServiceTest.java](../src/test/java/com/drivingschool/auth/service/CsrfTokenServiceTest.java)
- [src/test/java/com/drivingschool/auth/util/PasswordHasherTest.java](../src/test/java/com/drivingschool/auth/util/PasswordHasherTest.java)
- [src/test/java/com/drivingschool/common/controller/DashboardSummaryServiceTest.java](../src/test/java/com/drivingschool/common/controller/DashboardSummaryServiceTest.java)
- [src/test/java/com/drivingschool/common/controller/HomeControllerTest.java](../src/test/java/com/drivingschool/common/controller/HomeControllerTest.java)
- [src/test/java/com/drivingschool/common/exception/AuthorizationErrorHandlerTest.java](../src/test/java/com/drivingschool/common/exception/AuthorizationErrorHandlerTest.java)
- [src/test/java/com/drivingschool/common/exception/ErrorStatusMappingTest.java](../src/test/java/com/drivingschool/common/exception/ErrorStatusMappingTest.java)
- [src/test/java/com/drivingschool/common/exception/GlobalExceptionHandlerTest.java](../src/test/java/com/drivingschool/common/exception/GlobalExceptionHandlerTest.java)
- [src/test/java/com/drivingschool/common/repository/FileRepositoryTest.java](../src/test/java/com/drivingschool/common/repository/FileRepositoryTest.java)
- [src/test/java/com/drivingschool/common/util/IdGeneratorTest.java](../src/test/java/com/drivingschool/common/util/IdGeneratorTest.java)
- [src/test/java/com/drivingschool/DrivesparkAcademyApplicationTests.java](../src/test/java/com/drivingschool/DrivesparkAcademyApplicationTests.java) — **† NEEDS CHECK: module-based owner.**
- [src/test/java/com/drivingschool/student/service/StudentAccountCrudTest.java](../src/test/java/com/drivingschool/student/service/StudentAccountCrudTest.java)
- [src/test/java/com/drivingschool/student/service/StudentLookupServiceTest.java](../src/test/java/com/drivingschool/student/service/StudentLookupServiceTest.java)
- [src/test/java/com/drivingschool/student/service/StudentProfileServiceTest.java](../src/test/java/com/drivingschool/student/service/StudentProfileServiceTest.java)
- [src/test/java/com/drivingschool/student/StudentProfileWebIntegrationTest.java](../src/test/java/com/drivingschool/student/StudentProfileWebIntegrationTest.java)

</details>

<details>
<summary>IT26101133 — 44 code/test files (expand)</summary>

**Java (29):**

- [src/main/java/com/drivingschool/instructor/config/InstructorConfig.java](../src/main/java/com/drivingschool/instructor/config/InstructorConfig.java)
- [src/main/java/com/drivingschool/instructor/controller/InstructorAvailabilityController.java](../src/main/java/com/drivingschool/instructor/controller/InstructorAvailabilityController.java)
- [src/main/java/com/drivingschool/instructor/controller/InstructorDashboardController.java](../src/main/java/com/drivingschool/instructor/controller/InstructorDashboardController.java)
- [src/main/java/com/drivingschool/instructor/controller/PublicInstructorController.java](../src/main/java/com/drivingschool/instructor/controller/PublicInstructorController.java)
- [src/main/java/com/drivingschool/instructor/model/Availability.java](../src/main/java/com/drivingschool/instructor/model/Availability.java)
- [src/main/java/com/drivingschool/instructor/model/Instructor.java](../src/main/java/com/drivingschool/instructor/model/Instructor.java)
- [src/main/java/com/drivingschool/instructor/model/InstructorLeave.java](../src/main/java/com/drivingschool/instructor/model/InstructorLeave.java)
- [src/main/java/com/drivingschool/instructor/model/LessonRecord.java](../src/main/java/com/drivingschool/instructor/model/LessonRecord.java)
- [src/main/java/com/drivingschool/instructor/repository/AvailabilityRepository.java](../src/main/java/com/drivingschool/instructor/repository/AvailabilityRepository.java)
- [src/main/java/com/drivingschool/instructor/repository/InstructorLeaveRepository.java](../src/main/java/com/drivingschool/instructor/repository/InstructorLeaveRepository.java)
- [src/main/java/com/drivingschool/instructor/repository/InstructorRepository.java](../src/main/java/com/drivingschool/instructor/repository/InstructorRepository.java)
- [src/main/java/com/drivingschool/instructor/repository/LessonRecordRepository.java](../src/main/java/com/drivingschool/instructor/repository/LessonRecordRepository.java)
- [src/main/java/com/drivingschool/instructor/service/InstructorAccountService.java](../src/main/java/com/drivingschool/instructor/service/InstructorAccountService.java)
- [src/main/java/com/drivingschool/instructor/service/InstructorCatalog.java](../src/main/java/com/drivingschool/instructor/service/InstructorCatalog.java)
- [src/main/java/com/drivingschool/instructor/service/InstructorExperienceOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorExperienceOrder.java)
- [src/main/java/com/drivingschool/instructor/service/InstructorLessonService.java](../src/main/java/com/drivingschool/instructor/service/InstructorLessonService.java)
- [src/main/java/com/drivingschool/instructor/service/InstructorNameOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorNameOrder.java)
- [src/main/java/com/drivingschool/instructor/service/InstructorOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorOrder.java)
- [src/main/java/com/drivingschool/instructor/service/InstructorRatingOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorRatingOrder.java)
- [src/main/java/com/drivingschool/instructor/service/InstructorScheduleOperations.java](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleOperations.java)
- [src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java)
- [src/main/java/com/drivingschool/instructor/service/InstructorService.java](../src/main/java/com/drivingschool/instructor/service/InstructorService.java)
- [src/main/java/com/drivingschool/instructor/service/InstructorSorter.java](../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java)

**JSP/JSPF (4):**

- [src/main/webapp/WEB-INF/views/admin/instructors.jsp](../src/main/webapp/WEB-INF/views/admin/instructors.jsp)
- [src/main/webapp/WEB-INF/views/instructor/availability.jsp](../src/main/webapp/WEB-INF/views/instructor/availability.jsp)
- [src/main/webapp/WEB-INF/views/instructor/lesson-dashboard.jsp](../src/main/webapp/WEB-INF/views/instructor/lesson-dashboard.jsp)
- [src/main/webapp/WEB-INF/views/public/instructors.jsp](../src/main/webapp/WEB-INF/views/public/instructors.jsp)

**CSS/JS (0):**

None in this inventory.

**Tests (11):**

- [src/test/java/com/drivingschool/instructor/config/InstructorConfigTest.java](../src/test/java/com/drivingschool/instructor/config/InstructorConfigTest.java)
- [src/test/java/com/drivingschool/instructor/model/InstructorTest.java](../src/test/java/com/drivingschool/instructor/model/InstructorTest.java)
- [src/test/java/com/drivingschool/instructor/repository/InstructorRepositoryTest.java](../src/test/java/com/drivingschool/instructor/repository/InstructorRepositoryTest.java)
- [src/test/java/com/drivingschool/instructor/service/InstructorAccountServiceTest.java](../src/test/java/com/drivingschool/instructor/service/InstructorAccountServiceTest.java)
- [src/test/java/com/drivingschool/instructor/service/InstructorScheduleServiceTest.java](../src/test/java/com/drivingschool/instructor/service/InstructorScheduleServiceTest.java)
- [src/test/java/com/drivingschool/instructor/service/InstructorServiceTest.java](../src/test/java/com/drivingschool/instructor/service/InstructorServiceTest.java)
- [src/test/java/com/drivingschool/instructor/service/InstructorSorterTest.java](../src/test/java/com/drivingschool/instructor/service/InstructorSorterTest.java)

</details>

<details>
<summary>IT26101134 — 32 code/test files (expand)</summary>

**Java (16):**

- [src/main/java/com/drivingschool/booking/controller/AdminBookingController.java](../src/main/java/com/drivingschool/booking/controller/AdminBookingController.java)
- [src/main/java/com/drivingschool/booking/controller/BookingController.java](../src/main/java/com/drivingschool/booking/controller/BookingController.java)
- [src/main/java/com/drivingschool/booking/model/Booking.java](../src/main/java/com/drivingschool/booking/model/Booking.java)
- [src/main/java/com/drivingschool/booking/model/BookingStatus.java](../src/main/java/com/drivingschool/booking/model/BookingStatus.java)
- [src/main/java/com/drivingschool/booking/repository/BookingRepository.java](../src/main/java/com/drivingschool/booking/repository/BookingRepository.java)
- [src/main/java/com/drivingschool/booking/service/AdminBookingOperations.java](../src/main/java/com/drivingschool/booking/service/AdminBookingOperations.java)
- [src/main/java/com/drivingschool/booking/service/BookingConflictChecker.java](../src/main/java/com/drivingschool/booking/service/BookingConflictChecker.java)
- [src/main/java/com/drivingschool/booking/service/BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java)
- [src/main/java/com/drivingschool/booking/service/PaidEnrollmentGuard.java](../src/main/java/com/drivingschool/booking/service/PaidEnrollmentGuard.java)
- [src/main/java/com/drivingschool/booking/service/SlotAvailabilityService.java](../src/main/java/com/drivingschool/booking/service/SlotAvailabilityService.java)
- [src/main/java/com/drivingschool/booking/service/StudentBookingOperations.java](../src/main/java/com/drivingschool/booking/service/StudentBookingOperations.java)
- [src/main/java/com/drivingschool/booking/util/BookingOrder.java](../src/main/java/com/drivingschool/booking/util/BookingOrder.java)
- [src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java](../src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java)
- [src/main/java/com/drivingschool/booking/util/LessonIntervalUtils.java](../src/main/java/com/drivingschool/booking/util/LessonIntervalUtils.java)
- [src/main/java/com/drivingschool/booking/util/LessonTimeOrder.java](../src/main/java/com/drivingschool/booking/util/LessonTimeOrder.java)
- [src/main/java/com/drivingschool/booking/util/PendingRequestOrder.java](../src/main/java/com/drivingschool/booking/util/PendingRequestOrder.java)

**JSP/JSPF (4):**

- [src/main/webapp/WEB-INF/views/admin/booking-card.jspf](../src/main/webapp/WEB-INF/views/admin/booking-card.jspf)
- [src/main/webapp/WEB-INF/views/admin/bookings.jsp](../src/main/webapp/WEB-INF/views/admin/bookings.jsp)
- [src/main/webapp/WEB-INF/views/booking/list.jsp](../src/main/webapp/WEB-INF/views/booking/list.jsp)
- [src/main/webapp/WEB-INF/views/booking/request-card.jspf](../src/main/webapp/WEB-INF/views/booking/request-card.jspf)

**CSS/JS (0):**

None in this inventory.

**Tests (12):**

- [src/test/java/com/drivingschool/booking/model/BookingTest.java](../src/test/java/com/drivingschool/booking/model/BookingTest.java)
- [src/test/java/com/drivingschool/booking/repository/BookingRepositorySpringIntegrationTest.java](../src/test/java/com/drivingschool/booking/repository/BookingRepositorySpringIntegrationTest.java)
- [src/test/java/com/drivingschool/booking/repository/BookingRepositoryTest.java](../src/test/java/com/drivingschool/booking/repository/BookingRepositoryTest.java)
- [src/test/java/com/drivingschool/booking/SchedulingWebIntegrationTest.java](../src/test/java/com/drivingschool/booking/SchedulingWebIntegrationTest.java)
- [src/test/java/com/drivingschool/booking/service/BookingConflictCheckerTest.java](../src/test/java/com/drivingschool/booking/service/BookingConflictCheckerTest.java)
- [src/test/java/com/drivingschool/booking/service/BookingCutoffTest.java](../src/test/java/com/drivingschool/booking/service/BookingCutoffTest.java)
- [src/test/java/com/drivingschool/booking/service/BookingServiceCrudTest.java](../src/test/java/com/drivingschool/booking/service/BookingServiceCrudTest.java)
- [src/test/java/com/drivingschool/booking/service/SlotAvailabilityServiceTest.java](../src/test/java/com/drivingschool/booking/service/SlotAvailabilityServiceTest.java)
- [src/test/java/com/drivingschool/booking/service/SlotBoundaryTest.java](../src/test/java/com/drivingschool/booking/service/SlotBoundaryTest.java)
- [src/test/java/com/drivingschool/booking/util/BookingOrderTest.java](../src/test/java/com/drivingschool/booking/util/BookingOrderTest.java)
- [src/test/java/com/drivingschool/booking/util/BookingRequestQueueTest.java](../src/test/java/com/drivingschool/booking/util/BookingRequestQueueTest.java)
- [src/test/java/com/drivingschool/booking/util/LessonIntervalUtilsTest.java](../src/test/java/com/drivingschool/booking/util/LessonIntervalUtilsTest.java)

</details>

<details>
<summary>IT26101132 — 27 code/test files (expand)</summary>

**Java (16):**

- [src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java](../src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java)
- [src/main/java/com/drivingschool/vehicle/api/VehicleOperations.java](../src/main/java/com/drivingschool/vehicle/api/VehicleOperations.java)
- [src/main/java/com/drivingschool/vehicle/api/VehicleStore.java](../src/main/java/com/drivingschool/vehicle/api/VehicleStore.java)
- [src/main/java/com/drivingschool/vehicle/config/VehicleConfig.java](../src/main/java/com/drivingschool/vehicle/config/VehicleConfig.java)
- [src/main/java/com/drivingschool/vehicle/controller/VehicleController.java](../src/main/java/com/drivingschool/vehicle/controller/VehicleController.java)
- [src/main/java/com/drivingschool/vehicle/form/VehicleCreateForm.java](../src/main/java/com/drivingschool/vehicle/form/VehicleCreateForm.java)
- [src/main/java/com/drivingschool/vehicle/form/VehicleEditForm.java](../src/main/java/com/drivingschool/vehicle/form/VehicleEditForm.java)
- [src/main/java/com/drivingschool/vehicle/model/TransmissionType.java](../src/main/java/com/drivingschool/vehicle/model/TransmissionType.java)
- [src/main/java/com/drivingschool/vehicle/model/Vehicle.java](../src/main/java/com/drivingschool/vehicle/model/Vehicle.java)
- [src/main/java/com/drivingschool/vehicle/model/VehicleCategory.java](../src/main/java/com/drivingschool/vehicle/model/VehicleCategory.java)
- [src/main/java/com/drivingschool/vehicle/model/VehicleOperationalStatus.java](../src/main/java/com/drivingschool/vehicle/model/VehicleOperationalStatus.java)
- [src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java](../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java)
- [src/main/java/com/drivingschool/vehicle/service/VehicleService.java](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java)

**JSP/JSPF (3):**

- [src/main/webapp/WEB-INF/views/vehicle/create.jsp](../src/main/webapp/WEB-INF/views/vehicle/create.jsp)
- [src/main/webapp/WEB-INF/views/vehicle/edit.jsp](../src/main/webapp/WEB-INF/views/vehicle/edit.jsp)
- [src/main/webapp/WEB-INF/views/vehicle/list.jsp](../src/main/webapp/WEB-INF/views/vehicle/list.jsp)

**CSS/JS (0):**

None in this inventory.

**Tests (8):**

- [src/test/java/com/drivingschool/sample/DemoDataSeedRunnerTest.java](../src/test/java/com/drivingschool/sample/DemoDataSeedRunnerTest.java)
- [src/test/java/com/drivingschool/vehicle/config/VehicleConfigTest.java](../src/test/java/com/drivingschool/vehicle/config/VehicleConfigTest.java)
- [src/test/java/com/drivingschool/vehicle/model/VehicleTest.java](../src/test/java/com/drivingschool/vehicle/model/VehicleTest.java)
- [src/test/java/com/drivingschool/vehicle/repository/VehicleRepositoryTest.java](../src/test/java/com/drivingschool/vehicle/repository/VehicleRepositoryTest.java)
- [src/test/java/com/drivingschool/vehicle/service/VehicleMaintenanceTest.java](../src/test/java/com/drivingschool/vehicle/service/VehicleMaintenanceTest.java)
- [src/test/java/com/drivingschool/vehicle/service/VehicleServiceTest.java](../src/test/java/com/drivingschool/vehicle/service/VehicleServiceTest.java)
- [src/test/java/com/drivingschool/vehicle/VehicleWebIntegrationTest.java](../src/test/java/com/drivingschool/vehicle/VehicleWebIntegrationTest.java)

</details>

<details>
<summary>IT26101136 — 59 code/test files (expand)</summary>

**Java (21):**

- [src/main/java/com/drivingschool/learningpackage/config/LearningPackageConfig.java](../src/main/java/com/drivingschool/learningpackage/config/LearningPackageConfig.java)
- [src/main/java/com/drivingschool/learningpackage/controller/AdminEnrollmentController.java](../src/main/java/com/drivingschool/learningpackage/controller/AdminEnrollmentController.java)
- [src/main/java/com/drivingschool/learningpackage/controller/EnrollmentController.java](../src/main/java/com/drivingschool/learningpackage/controller/EnrollmentController.java)
- [src/main/java/com/drivingschool/learningpackage/controller/LearningPackageController.java](../src/main/java/com/drivingschool/learningpackage/controller/LearningPackageController.java)
- [src/main/java/com/drivingschool/learningpackage/controller/LocalPhotoAdvice.java](../src/main/java/com/drivingschool/learningpackage/controller/LocalPhotoAdvice.java)
- [src/main/java/com/drivingschool/learningpackage/controller/PackageEnrollmentEntryController.java](../src/main/java/com/drivingschool/learningpackage/controller/PackageEnrollmentEntryController.java)
- [src/main/java/com/drivingschool/learningpackage/controller/PackageNavigationAdvice.java](../src/main/java/com/drivingschool/learningpackage/controller/PackageNavigationAdvice.java)
- [src/main/java/com/drivingschool/learningpackage/model/Enrollment.java](../src/main/java/com/drivingschool/learningpackage/model/Enrollment.java)
- [src/main/java/com/drivingschool/learningpackage/model/LearningPackage.java](../src/main/java/com/drivingschool/learningpackage/model/LearningPackage.java)
- [src/main/java/com/drivingschool/learningpackage/model/LearningRecord.java](../src/main/java/com/drivingschool/learningpackage/model/LearningRecord.java)
- [src/main/java/com/drivingschool/learningpackage/model/PackageNavigationCard.java](../src/main/java/com/drivingschool/learningpackage/model/PackageNavigationCard.java)
- [src/main/java/com/drivingschool/learningpackage/model/PackageType.java](../src/main/java/com/drivingschool/learningpackage/model/PackageType.java)
- [src/main/java/com/drivingschool/learningpackage/repository/EnrollmentRepository.java](../src/main/java/com/drivingschool/learningpackage/repository/EnrollmentRepository.java)
- [src/main/java/com/drivingschool/learningpackage/repository/LearningPackageRepository.java](../src/main/java/com/drivingschool/learningpackage/repository/LearningPackageRepository.java)
- [src/main/java/com/drivingschool/learningpackage/service/EnrollmentAdministration.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentAdministration.java)
- [src/main/java/com/drivingschool/learningpackage/service/EnrollmentOperations.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentOperations.java)
- [src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java)
- [src/main/java/com/drivingschool/learningpackage/service/LearningPackageCatalog.java](../src/main/java/com/drivingschool/learningpackage/service/LearningPackageCatalog.java)
- [src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java](../src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java)

**JSP/JSPF (25):**

- [src/main/webapp/WEB-INF/views/admin/enrollments.jsp](../src/main/webapp/WEB-INF/views/admin/enrollments.jsp)
- [src/main/webapp/WEB-INF/views/admin/packages.jsp](../src/main/webapp/WEB-INF/views/admin/packages.jsp)
- [src/main/webapp/WEB-INF/views/common/components/confirm-dialog.jspf](../src/main/webapp/WEB-INF/views/common/components/confirm-dialog.jspf)
- [src/main/webapp/WEB-INF/views/common/components/empty-state.jspf](../src/main/webapp/WEB-INF/views/common/components/empty-state.jspf)
- [src/main/webapp/WEB-INF/views/common/components/local-photo.jspf](../src/main/webapp/WEB-INF/views/common/components/local-photo.jspf)
- [src/main/webapp/WEB-INF/views/common/components/page-header.jspf](../src/main/webapp/WEB-INF/views/common/components/page-header.jspf)
- [src/main/webapp/WEB-INF/views/common/components/status-badge.jspf](../src/main/webapp/WEB-INF/views/common/components/status-badge.jspf)
- [src/main/webapp/WEB-INF/views/common/components/tabs.jspf](../src/main/webapp/WEB-INF/views/common/components/tabs.jspf)
- [src/main/webapp/WEB-INF/views/common/components/toasts.jspf](../src/main/webapp/WEB-INF/views/common/components/toasts.jspf)
- [src/main/webapp/WEB-INF/views/common/dashboard-sidebar.jspf](../src/main/webapp/WEB-INF/views/common/dashboard-sidebar.jspf)
- [src/main/webapp/WEB-INF/views/common/error.jsp](../src/main/webapp/WEB-INF/views/common/error.jsp)
- [src/main/webapp/WEB-INF/views/common/footer.jspf](../src/main/webapp/WEB-INF/views/common/footer.jspf)
- [src/main/webapp/WEB-INF/views/common/navbar.jspf](../src/main/webapp/WEB-INF/views/common/navbar.jspf)
- [src/main/webapp/WEB-INF/views/common/setup-check.jsp](../src/main/webapp/WEB-INF/views/common/setup-check.jsp)
- [src/main/webapp/WEB-INF/views/learningpackage/detail.jsp](../src/main/webapp/WEB-INF/views/learningpackage/detail.jsp)
- [src/main/webapp/WEB-INF/views/learningpackage/list.jsp](../src/main/webapp/WEB-INF/views/learningpackage/list.jsp)
- [src/main/webapp/WEB-INF/views/public/about.jsp](../src/main/webapp/WEB-INF/views/public/about.jsp)
- [src/main/webapp/WEB-INF/views/public/contact.jsp](../src/main/webapp/WEB-INF/views/public/contact.jsp)
- [src/main/webapp/WEB-INF/views/public/faq.jsp](../src/main/webapp/WEB-INF/views/public/faq.jsp)
- [src/main/webapp/WEB-INF/views/student/enrollments.jsp](../src/main/webapp/WEB-INF/views/student/enrollments.jsp)
- [src/main/webapp/WEB-INF/views/student/payment-instructions.jsp](../src/main/webapp/WEB-INF/views/student/payment-instructions.jsp)

**CSS/JS (3):**

- [src/main/resources/static/css/app.css](../src/main/resources/static/css/app.css)
- [src/main/resources/static/css/tailwind.css](../src/main/resources/static/css/tailwind.css)
- [src/main/resources/static/js/site.js](../src/main/resources/static/js/site.js)

**Tests (10):**

- [src/test/java/com/drivingschool/learningpackage/AdminEnrollmentWebIntegrationTest.java](../src/test/java/com/drivingschool/learningpackage/AdminEnrollmentWebIntegrationTest.java)
- [src/test/java/com/drivingschool/learningpackage/config/LearningPackageConfigTest.java](../src/test/java/com/drivingschool/learningpackage/config/LearningPackageConfigTest.java)
- [src/test/java/com/drivingschool/learningpackage/EnrollmentWebIntegrationTest.java](../src/test/java/com/drivingschool/learningpackage/EnrollmentWebIntegrationTest.java)
- [src/test/java/com/drivingschool/learningpackage/model/LearningPackageTest.java](../src/test/java/com/drivingschool/learningpackage/model/LearningPackageTest.java)
- [src/test/java/com/drivingschool/learningpackage/repository/LearningPackageRepositoryTest.java](../src/test/java/com/drivingschool/learningpackage/repository/LearningPackageRepositoryTest.java)
- [src/test/java/com/drivingschool/learningpackage/service/EnrollmentPaymentServiceTest.java](../src/test/java/com/drivingschool/learningpackage/service/EnrollmentPaymentServiceTest.java)
- [src/test/java/com/drivingschool/learningpackage/service/EnrollmentServiceTest.java](../src/test/java/com/drivingschool/learningpackage/service/EnrollmentServiceTest.java)
- [src/test/java/com/drivingschool/learningpackage/service/LearningPackageLifecycleTest.java](../src/test/java/com/drivingschool/learningpackage/service/LearningPackageLifecycleTest.java)
- [src/test/java/com/drivingschool/learningpackage/service/LearningPackageServiceTest.java](../src/test/java/com/drivingschool/learningpackage/service/LearningPackageServiceTest.java)
- [src/test/java/com/drivingschool/learningpackage/service/PackageNavigationAdviceTest.java](../src/test/java/com/drivingschool/learningpackage/service/PackageNavigationAdviceTest.java)

</details>

<details>
<summary>IT26101137 — 31 code/test files (expand)</summary>

**Java (15):**

- [src/main/java/com/drivingschool/report/controller/AdminReportController.java](../src/main/java/com/drivingschool/report/controller/AdminReportController.java)
- [src/main/java/com/drivingschool/report/service/AdminReportService.java](../src/main/java/com/drivingschool/report/service/AdminReportService.java)
- [src/main/java/com/drivingschool/report/service/ReportCatalog.java](../src/main/java/com/drivingschool/report/service/ReportCatalog.java)
- [src/main/java/com/drivingschool/report/service/ReportRow.java](../src/main/java/com/drivingschool/report/service/ReportRow.java)
- [src/main/java/com/drivingschool/report/service/ReportSection.java](../src/main/java/com/drivingschool/report/service/ReportSection.java)
- [src/main/java/com/drivingschool/review/api/ReviewStore.java](../src/main/java/com/drivingschool/review/api/ReviewStore.java)
- [src/main/java/com/drivingschool/review/controller/AdminReviewController.java](../src/main/java/com/drivingschool/review/controller/AdminReviewController.java)
- [src/main/java/com/drivingschool/review/controller/PublicReviewController.java](../src/main/java/com/drivingschool/review/controller/PublicReviewController.java)
- [src/main/java/com/drivingschool/review/controller/ReviewController.java](../src/main/java/com/drivingschool/review/controller/ReviewController.java)
- [src/main/java/com/drivingschool/review/model/Review.java](../src/main/java/com/drivingschool/review/model/Review.java)
- [src/main/java/com/drivingschool/review/model/ReviewStatus.java](../src/main/java/com/drivingschool/review/model/ReviewStatus.java)
- [src/main/java/com/drivingschool/review/repository/ReviewRepository.java](../src/main/java/com/drivingschool/review/repository/ReviewRepository.java)
- [src/main/java/com/drivingschool/review/service/ReviewModeration.java](../src/main/java/com/drivingschool/review/service/ReviewModeration.java)
- [src/main/java/com/drivingschool/review/service/ReviewOperations.java](../src/main/java/com/drivingschool/review/service/ReviewOperations.java)
- [src/main/java/com/drivingschool/review/service/ReviewService.java](../src/main/java/com/drivingschool/review/service/ReviewService.java)

**JSP/JSPF (7):**

- [src/main/webapp/WEB-INF/views/admin/reports.jsp](../src/main/webapp/WEB-INF/views/admin/reports.jsp)
- [src/main/webapp/WEB-INF/views/admin/reviews.jsp](../src/main/webapp/WEB-INF/views/admin/reviews.jsp)
- [src/main/webapp/WEB-INF/views/common/components/review-tabs.jspf](../src/main/webapp/WEB-INF/views/common/components/review-tabs.jspf)
- [src/main/webapp/WEB-INF/views/common/components/star-display.jspf](../src/main/webapp/WEB-INF/views/common/components/star-display.jspf)
- [src/main/webapp/WEB-INF/views/common/components/star-input.jspf](../src/main/webapp/WEB-INF/views/common/components/star-input.jspf)
- [src/main/webapp/WEB-INF/views/public/reviews.jsp](../src/main/webapp/WEB-INF/views/public/reviews.jsp)
- [src/main/webapp/WEB-INF/views/review/list.jsp](../src/main/webapp/WEB-INF/views/review/list.jsp)

**CSS/JS (1):**

- [src/main/resources/static/css/report.css](../src/main/resources/static/css/report.css)

**Tests (8):**

- [src/test/java/com/drivingschool/review/model/ReviewTest.java](../src/test/java/com/drivingschool/review/model/ReviewTest.java)
- [src/test/java/com/drivingschool/review/repository/ReviewRepositoryTest.java](../src/test/java/com/drivingschool/review/repository/ReviewRepositoryTest.java)
- [src/test/java/com/drivingschool/review/ReviewWebIntegrationTest.java](../src/test/java/com/drivingschool/review/ReviewWebIntegrationTest.java)
- [src/test/java/com/drivingschool/review/service/AdminReportServiceTest.java](../src/test/java/com/drivingschool/review/service/AdminReportServiceTest.java)
- [src/test/java/com/drivingschool/review/service/InstructorRatingRecalculationTest.java](../src/test/java/com/drivingschool/review/service/InstructorRatingRecalculationTest.java)
- [src/test/java/com/drivingschool/review/service/ReviewServiceConcurrencyTest.java](../src/test/java/com/drivingschool/review/service/ReviewServiceConcurrencyTest.java)
- [src/test/java/com/drivingschool/review/service/ReviewServiceTest.java](../src/test/java/com/drivingschool/review/service/ReviewServiceTest.java)
- [src/test/java/com/drivingschool/review/service/ReviewVisibilityTest.java](../src/test/java/com/drivingschool/review/service/ReviewVisibilityTest.java)

</details>


## 13. Before the viva: verification checklist and unresolved points

- **Code references:** named Java methods, constructors and JS functions are checked against the actual files. Method arguments are not reproduced in every answer; open the linked method to show the complete signature.
- **Older notes:** the previous OOP map describes the overlap exception as ValidationException, but the inspected `InstructorScheduleService.ensureNoOverlap` throws **DuplicateRecordException**. This guide follows source. That older map was not edited in this documentation-only phase.
- **Current reports:** existing XML totals are 663 / 0 / 0 / 0. The older root build log is a separate failed run. No test was added, deleted or changed, and no new Maven or npm command was run.
- **Simplification:** explain the working weekly availability store and the actual enrollment/payment interfaces; unused proposed integrations have been removed.
- **Historical merge analysis:** the earlier missing-bean diagnosis is historical. Current InstructorConfig contains instructorAvailabilityRepository, instructorLookup and instructorAvailabilityService; do not repeat the older wiring omission as though it were still present.
- **Ownership:** confirm the 13 †-marked file assignments with the team, including the incoming contract/test files and root application test. The assigned IDs follow the agreed modules, while headers are missing.
- **Team history:** confirm personal contributions, reviewers, conflict-resolution steps and the hardest-bug story. Local Git log subjects alone do not establish those facts or current remote state.
- **Browser rehearsals — NEEDS CHECK:** none of this phase's click scripts or HTTP flows was manually exercised. Prepare compatible instructor/vehicle types, working hours, valid future times, Paid lesson balance and completed lessons/reviews where required.
- **Print — NEEDS CHECK:** the six cheat sheets target one A4 page each, but actual pagination was not previewed. Print one sheet at a time; GitHub may strip the inline page-break styling.
- **New full build — NEEDS CHECK:** existing test reports were read, not regenerated. Do not say this agent ran `.\\mvnw.cmd clean verify` for Phase C.
- **Scope:** only `docs/VIVA_QA.md` is created. No source/configuration/test file or existing diagram/plan is edited, no application folder changes are made, and no Git write command is run.

**Phase C stop point:** the viva guide is delivered. No further phase work or source fixes are performed.


## Batch 1: attendance and commitment rules (9 November 2026)

**Why can I not complete next week's lesson?** BookingService passes the school Clock and app.attendance.require-lesson-ended (default true) to Booking.complete. Confirmed lessons become completable at their end; no-show becomes available at their start. The error gives the available school date/time before any status or credit change.

**How can we rehearse without waiting?** Start an isolated temporary-data demo in a fresh shell with DRIVESPARK_ATTENDANCE_REQUIRE_LESSON_ENDED=false. This bypasses both attendance time gates, but not paid enrollment, roles, ownership or status rules. Clear the run-only variables after stopping it; demo completion does not prove attendance.

**Can one remaining lesson support several requests?** No. BookingService.requireCommitmentRoom reloads the stored balance and counts Pending/Confirmed commitments under the existing shared lock. Reschedule excludes the replaced request; assignment excludes its target before checking room for the same request. Cancellation frees room without adding credit because requests are not debited.

**Did this batch implement renewal or change FIFO/payment rules?** No. Later-batch findings remain unresolved. The FIFO queue, bubble sort, payment confirmation and unused contracts were left unchanged.


Batch 1 verification supersedes the earlier NEEDS CHECK for this build: `npm run build:css` succeeded; `.\mvnw.cmd clean verify` finished BUILD SUCCESS at 17:59:20+05:30 on 9 November 2026, with **672 tests, 0 failures, 0 errors, 0 skipped**, including WAR packaging. Steps 14–24 passed over HTTP in both isolated runs (gate ON refuses future completion; OFF permits the full completion/review journey). Extra cap and attendance probes passed. Browser rendering remains UNVERIFIED. Both demos stopped and emitted confirmation that their run-only demo variables were cleared. See SMOKE_TEST.md for all changed tests and evidence; Batch 2/3 remains pending approval.


## Step 1: connecting packages and lessons in the UI

**Where does the booking's package come from?** Follow Booking.enrollmentId to Enrollment.packageId and then the package catalog. EnrollmentPresentationService builds ID maps once per presentation request, rather than reading the enrollment file for each row. Instructor/admin controllers supply only bookings already authorized by their existing services. Student output is filtered to their own receipts.

**What does EnrollmentUsageView do?** It carries display labels and counts without changing stored records. It is a display helper, not a stored enrollment balance. Pending is labelled Requested, Confirmed is separate, and Completed counts delivered lessons recorded against that enrollment; payment status and usability status remain separate.

**Does each Paid receipt store a balance?** No: Student stores lessonsRemaining and the current enrollment/package IDs. Older receipts must not display the current account balance. They show “Historical enrollment — remaining balance not recorded”; the catalog count is labelled Current package offering because purchased lesson counts are not snapshotted.

**What happens at zero balance?** Existing Student.setLessonsRemaining(0) clears both active IDs. The display recognizes the latest Paid receipt when the account has zero balance and no active IDs; it represents the exhausted package, while older Paid receipts remain historical. This corrects the earlier Step 0 assumption that zero balance always retains active IDs. No model or renewal rule was edited in Step 1.

**How are damaged references handled?** A missing package shows “Package unavailable (P001)” with its actual package ID and type “Not recorded”. A booking with no matching enrollment shows “Package unavailable”. Null validity shows “Validity not recorded”. These are presentation fallbacks; file corruption or an unreadable storage file is still a genuine storage error, not silently ignored.

**Does the new View packages link mean renewal works?** No. Used up/Expired text says “Contact the school to renew. Buying another package is not available yet.” Renewal implementation remains outside this task. Book links are absent on expired, used-up, awaiting-payment and cancelled receipts.

### Step 1 file ownership inventory

Paths below are relative to the project root. Existing headers, comments, routes and form field names were preserved.

| Owner | Java file changed/added | Purpose |
|---|---|---|
| IT26101136 | src/main/java/com/drivingschool/learningpackage/dto/EnrollmentUsageView.java (new) | Read-only display DTO |
| IT26101136 | src/main/java/com/drivingschool/learningpackage/service/EnrollmentPresentationService.java (new) | One catalog load per presentation call, ID-map resolution, safe labels/counts |
| IT26101136 | src/main/java/com/drivingschool/learningpackage/controller/EnrollmentController.java | Add owned receipt usage model data |
| IT26101135 | src/main/java/com/drivingschool/student/controller/StudentProfileController.java | Add dashboard package summary |
| IT26101134 | src/main/java/com/drivingschool/booking/controller/BookingController.java | Add header/row summaries and reuse loaded catalog for package selector |
| IT26101134 | src/main/java/com/drivingschool/booking/controller/AdminBookingController.java | Add booking-linked package map |
| IT26101133 | src/main/java/com/drivingschool/instructor/controller/InstructorDashboardController.java | Add assigned booking-linked package map |
| IT26101136 | src/test/java/com/drivingschool/learningpackage/service/EnrollmentPresentationServiceTest.java (new) | Presentation and bounded-read tests |

| Owner | View file changed/added | Purpose |
|---|---|---|
| IT26101136 | src/main/webapp/WEB-INF/views/common/components/enrollment-usage.jspf (new) | Shared student usage display |
| IT26101135 | src/main/webapp/WEB-INF/views/student/profile.jsp | Friendly current package card |
| IT26101136 | src/main/webapp/WEB-INF/views/student/enrollments.jsp | Usage, validity, fallbacks and usable-only next step |
| IT26101134 | src/main/webapp/WEB-INF/views/booking/list.jsp | Package usage in booking header |
| IT26101134 | src/main/webapp/WEB-INF/views/booking/request-card.jspf | Package name/type per student booking |
| IT26101134 | src/main/webapp/WEB-INF/views/admin/booking-card.jspf | Package name/type per admin booking |
| IT26101133 | src/main/webapp/WEB-INF/views/instructor/lesson-dashboard.jsp | Package name/type per assigned booking |

Also changed: generated src/main/resources/static/css/tailwind.css (IT26101136, required CSS build), README.md and docs/VIVA_QA.md (IT26101135, integration documentation). Pre-existing home/index.jsp and Batch 1 changes were not edited by this task. No progress, catalog, payment or report view changes; no existing contract classes edited.


### Step 1 verification evidence — 9 November 2026

The app was stopped before each build. The final `npm run build:css` succeeded (437ms); its existing Browserslist dataset warning remains. Final Java 17 `.\mvnw.cmd clean verify` result:

```text
[INFO] Tests run: 684, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  01:23 min
[INFO] Finished at: 2026-11-09T18:45:40+05:30
```

Full log: C:/Users/user/AppData/Local/Temp/drivespark-usage-verify.log. WAR packaging and repackaging succeeded. Expected storage-failure test logging is not a failure in this aggregate.

No existing test file was changed by Step 1. All 12 added tests are in EnrollmentPresentationServiceTest, owned by IT26101136:

| New test | Why it was added |
|---|---|
| countsOnlyMatchingEnrollmentAndStudentAndKeepsCommitmentsSeparate | Scope usage by receipt, separate Pending/Confirmed/Completed, exclude cancelled/no-show/rejected |
| historicalReceiptNeverBorrowsCurrentBalance | Keep old Paid receipts separate from current balance; exact historical wording |
| expiredReceiptHasNoBookLinkAndDoesNotPromiseRenewal | Expired state and honest next step |
| validityIncludesToday | Validity date is inclusive |
| nullValidityHasExactFallbackAndRemainsUsable | Legacy null validity fallback |
| missingPackageIsSafeAndNotBookable | Deleted-package name/type fallback and unavailable state |
| usedUpReceiptHasNoBookLink | Existing zero-balance setter clears pointers; display still recognizes exhaustion |
| unpaidAndCancelledReceiptsNeverOfferBooking | Neither state supplies Book links |
| fullCommitmentsKeepPackageActiveButLinkToExistingBookings | Pending plus Confirmed commitment count and next step at cap |
| bookingWithMissingEnrollmentDoesNotProduceAnError | Missing enrollment resolves to safe labels |
| catalogsAreLoadedOnceAndHistoricalBookingUsesItsOwnPackage | Mock verification of one enrollment/catalog read; row resolves through its own receipt |
| studentCatalogsLoadedOnceAndOtherStudentsReceiptsAreNotExposed | Owned receipt filtering, bounded reads and reuse of supplied booking list |

During development the new expired fixture was corrected to enroll before its expiry. The new used-up test exposed the existing zero-balance pointer-clearing rule; the presentation helper was updated accordingly. No existing business-rule tests were weakened or switched to demo mode.

The final packaged app ran in a fresh shell on localhost:18080. Only that process set DRIVESPARK_DEMO_SEED_ENABLED=true, DRIVESPARK_ADMIN_SEED_ENABLED=false and APP_DATA_DIRECTORY=C:/Users/user/AppData/Local/Temp/drivespark-usage-step1-final. The attendance setting retained its default. The app was stopped after checks and the shell emitted “Demo variables cleared”. No live data was changed.

Chrome 156 headless was driven through its local debugging connection using the README sample student1, instructor1 and admin logins. Viewport was explicitly set and measured as **1536 x 864**, device scale 1. Reduced motion was enabled so scroll-reveal animation did not obscure the captured rows. All five actual routes were verified after login; document width never exceeded viewport width. Screenshots were opened and visually inspected:

| Page / area | Result | Screenshot under C:/Users/user/AppData/Local/Temp/drivespark-usage-evidence/ |
|---|---|---|
| Student dashboard package card | PASS: name/type, Active, 9 left, 2 Requested, 0 Confirmed, validity, next link | student-student-profile.png |
| Student enrollments summary | PASS: name/type, usage, validity, status and next link | student-enrollments.png |
| Student booking header | PASS: same current-enrollment summary | student-bookings.png |
| Student booking rows | PASS: booking-linked package/type alongside enrollment and queue | student-bookings-row.png |
| Instructor assigned booking rows | PASS: Demo Package 6 / AUTOMATIC for B006 | instructor-instructor-dashboard-row.png |
| Admin booking rows | PASS: Demo Package 1 / MANUAL and Demo Package 2 / AUTOMATIC on their respective rows | admin-admin-bookings-row.png |

Normal-page evidence and captured DOM text: browser-results.json in that same temporary evidence directory. Screenshots cover the new display areas, not every item further down each list. Mobile/responsive sizes other than 1536 x 864, all possible historic-data combinations, and a full regression of transactional buttons remain **UNVERIFIED**. This display task did not click destructive actions.

HTTP fallback checks used isolated demo fixtures only, restored in finally after each run. No source, storage schema or live data was changed to create these cases. All **25 assertions passed**, all target responses HTTP 200:

| Fixture / scope | Checks | Evidence |
|---|---:|---|
| Normal student dashboard, enrollments and bookings | 3 | Package name/type, balance/count labels, validity and Active |
| Normal instructor/admin bookings | 2 | Correct package/type shown |
| Expired student receipt | 1 | Expired, renewal limitation wording, no Book link |
| Used up with cleared account pointers | 2 | Used up and 0 left on dashboard/enrollments; no enrollment Book link |
| Null receipt validity | 3 | Validity not recorded on all student summary pages |
| Deleted package | 5 | Package unavailable (P001) / Not recorded across student, instructor and admin pages |
| Missing enrollment | 5 | Safe row/summary fallback or empty owned enrollment page; no error page |
| Old Paid receipt plus current Paid receipt | 2 | Historical wording, old package/type, current account still shows 9 left |
| Awaiting payment and Cancelled | 2 | Exact status wording, no Book link on either receipt |

Raw evidence: C:/Users/user/AppData/Local/Temp/drivespark-usage-evidence/http-results.json. Harnesses are usage_http.py and usage_browser.cjs under the temporary directory. The Cancelled fixture belongs to S008 in the seed, not S009; the harness was corrected after checking the seed. A harness string-encoding issue was also corrected before the final 25/25 run. These were harness issues, not application failures.

Both the final demo and dedicated test browser were stopped after verification. No Git write commands were used. Progress, catalog, payment, report pages remain outside that earlier UI change. The unused enrollment contract was subsequently removed in the approved Java cleanup.


## Read-only booking details: IT26101134's third UI

The standalone `booking/details.jsp` is reached through GET `/bookings/{bookingId}/details`, linked from the existing student and admin booking cards. It is a third full booking JSP, alongside booking/list.jsp and admin/bookings.jsp. It displays lesson and request dates, state, package/type/validity, enrollment, student/resource IDs, unassigned FIFO position and stored rejection/cancellation reason. It has no booking mutation form; the normal role navigation still includes its shared logout form outside the page's main content.

**Where is ownership enforced?** BookingDetailsController has RequireLogin; BookingService.getBookingDetails independently requires login, permits only STUDENT or ADMIN, validates the booking ID, finds the saved booking, and rejects a non-owning student. Authorization runs before package presentation data is prepared. Instructor access is403 even for a booking assigned to them: their existing instructor dashboard is unchanged. The admin role may read any booking and returns to /admin/bookings.

**Does opening the page dequeue anything?** No. For unassigned Pending only, the controller calls the existing authorized queue-position read. It rebuilds a temporary FIFO view without processing it. Assigned Pending says it is waiting for instructor approval; historical records have no FIFO position. No credit, status or resource assignment changes occur.

### Files changed in this follow-up

All application/test files listed below belong to **IT26101134**; original headers/comments were retained. New files have member headers and step/READ comments.

| File | Change |
|---|---|
| src/main/java/com/drivingschool/booking/controller/BookingDetailsController.java (new) | GET-only details route, login annotation, authorized model preparation and role-specific return link |
| src/main/java/com/drivingschool/booking/service/BookingService.java | Added getBookingDetails, an independently guarded read operation |
| src/main/webapp/WEB-INF/views/booking/details.jsp (new) | Standalone read-only UI |
| src/main/webapp/WEB-INF/views/booking/request-card.jspf | Student details link |
| src/main/webapp/WEB-INF/views/admin/booking-card.jspf | Admin details link |
| src/test/java/com/drivingschool/booking/SchedulingWebIntegrationTest.java | Eight additional integration tests; no existing tests weakened |

Generated CSS was rebuilt using the existing pipeline (shared UI owner136). README.md, this integration guide and an audit follow-up note were updated by135's documentation scope. No payment/scheduling/attendance transition, sort/queue implementation, storage format, existing route or form field was changed by this follow-up.

### Tests and actual verification

Eight tests added to SchedulingWebIntegrationTest:

1. ownerCanReadBookingDetailsWithoutChangingRecords — view/model, no-store, positive FIFO position, unchanged bookings file and balance.
2. adminCanReadAnotherStudentsBookingDetails — admin permission and correct return URL.
3. otherStudentCannotReadBookingDetails —403 without booking/package model data; direct service role check also exercised.
4. detailsRequireLoginAndRejectInstructorRole — login redirect, instructor403 including guesses of unknown booking IDs.
5. detailsHandleInvalidAndMissingBookingIds —400 for invalid ID and404 for a missing valid ID.
6. detailsHandleMissingEnrollmentAndPackage —200 with display fallback for incomplete historical references using temporary test repositories.
7. assignedAndHistoricalDetailsDoNotShowQueuePosition —no unassigned position for assigned Pending or rejected history; reason retained.
8. detailsUrlDoesNotAcceptPost —405 on the new URL; Pending status unchanged.

The pre-existing demo was already stopped before building. `npm run build:css` passed (438ms; existing Browserslist dataset warning). Java 17 `.\mvnw.cmd clean verify` produced:

```text
[INFO] Tests run: 692, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  01:21 min
[INFO] Finished at: 2026-11-09T19:19:04+05:30
```

SchedulingWebIntegrationTest itself ran25 tests with0 failures/errors/skips. The expected POST-not-supported warning is the new read-only-route test, not a failing check. WAR packaging/repackage succeeded. Full log: C:/Users/user/AppData/Local/Temp/drivespark-booking-details-verify.log.

The verified WAR was then run on localhost:18080 in a fresh shell with DRIVESPARK_DEMO_SEED_ENABLED=true, DRIVESPARK_ADMIN_SEED_ENABLED=false and APP_DATA_DIRECTORY=C:/Users/user/AppData/Local/Temp/drivespark-booking-details-viva1 set only for that run. Default attendance rules remained on. HTTP checks used real cookies and sample README logins, with no booking actions:

| Check | Actual evidence | Result |
|---|---|---|
| Student1 reads B001 |200, Booking details / Demo Package1 / MANUAL / FIFO position1|PASS|
| Details main content is read-only |No form inside rendered main element|PASS|
| Admin reads B001 |200, student S001 and return URL /admin/bookings|PASS|
| Student2 attempts B001 |403 Access denied|PASS|
| Instructor1 attempts B001 |403 Access denied|PASS|
| Anonymous attempts B001 |Redirect followed to /login?required, login page200|PASS|
| Owner requests missing B999 |404|PASS|
| Owner requests invalid B000 |400, Booking ID must be B001 to B999.|PASS|
| Student list contains details link |200, /bookings/B001/details and View booking details|PASS|
| Admin list contains details link |200, same link/label|PASS|
| Persistence unchanged |Byte-for-byte comparison of every temporary .txt record/counter file before and after checks|PASS|

Raw results: C:/Users/user/AppData/Local/Temp/booking-details-http-results.json; harness: booking_details_http.py. Browser appearance/interaction and viewport screenshots of the new page remain **UNVERIFIED**; HTTP200 did compile/render the JSP. Previous1536x864 checks concerned the earlier package-usage pages, not this new page. The demo was stopped and its shell emitted “Demo variables cleared”. No live data or Git history was changed. Stop for user approval.


Cleanup note (9 November 2026): unused pagination.jspf, skeleton.jspf, search-bar.jspf and breadcrumbs.jspf were removed after approval. They are no longer part of the current file inventory. Existing search/breadcrumb controls are implemented by their pages; no feature or Java class was removed.


## Approved cleanup verification — 9 November 2026

Removed exactly 49 approved files: obsolete root build.log; the unused shared pagination.jspf, skeleton.jspf, search-bar.jspf and breadcrumbs.jspf fragments; and 44 .gitkeep placeholders in populated src directories. Kept data/.gitkeep and src/main/java/com/drivingschool/student/repository/.gitkeep (its directory is otherwise empty). No directories were recursively deleted.

README and current viva inventories no longer link to removed fragments. Historical Git-plan/merge-analysis paths remain explicitly labelled as dated evidence, not current file manifests. The rubric audit has a cleanup follow-up notice. Existing page search/breadcrumb controls were untouched.

All **241 Java files**, including tests, were checked against pre-cleanup SHA-256 hashes: **0 changed or missing**. No new tests or Java classes were introduced. No active source references to the four removed fragments remain.

Java 17 `.\mvnw.cmd clean verify` returned:

```text
[INFO] Tests run: 692, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  01:21 min
[INFO] Finished at: 2026-11-09T20:04:11+05:30
```

WAR packaging/repackaging succeeded. The new build output is stored outside the repository at C:/Users/user/AppData/Local/Temp/drivespark-approved-cleanup-verify.log, not a recreated root build.log. The exact removal manifest and Java hash snapshot are drivespark-cleanup-removed.json and drivespark-cleanup-java-before.json in the same temporary directory.

No demo was launched and no live data was used for this cleanup verification. Browser scenarios were not rerun (**UNVERIFIED**); the unused-include reference check and full existing test/build suite are the validation for this file-only cleanup. No Git write command was used. Stop for approval.

## Java cleanup (2026-11-09)

Unused instructor interval/lookup, vehicle lookup and enrollment lookup classes were removed. Working CRUD pages, file storage, OOP interfaces, the manual instructor bubble sort and manual booking queue remain. The empty legacy `instructor-availability.txt` sample and its counter are retained for compatibility and are not read by the application. See the [removal and verification record](JAVA_SIMPLIFICATION.md).
