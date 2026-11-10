# Viva guide

> **Current guide (10 Nov 2026):** use the [source-checked viva pack](viva-2026/README.md), [six individual diagrams](viva-2026/class-diagrams.html) and [six printable cheat sheets](viva-2026/cheat-sheets.pdf). The older material below is historical and may mention removed interfaces or earlier report layouts.

Owner: **IT26101135 - [Name]** (integration documentation). Member names remain placeholders until the team supplies them.

Begin with your five-sentence story, then trace one browser request through controller, service, model and repository. Each production file below has one owner, matching its source header. A module can use another member's shared file without owning it. Java labels omit `src/main/java/com/drivingschool/`; JSP labels omit `src/main/webapp/WEB-INF/views/`. Links point to the complete path.

The five-click scripts assume the stated login and starting page. Typing prepared form values is part of the step; selecting an option counts as an interaction. Use isolated temporary demo data for saves. Do not delete, hide, suspend, force-cancel or process a queue in live data. These are rehearsal instructions, not claims that every write has been manually tested.

## IT26101135 — [Name]

**Module:** Authentication, Students and shared integration.

### My files

**Java (62):**

- [admin/controller/AdminDashboardController.java](../src/main/java/com/drivingschool/admin/controller/AdminDashboardController.java)
- [admin/controller/AdminManagementController.java](../src/main/java/com/drivingschool/admin/controller/AdminManagementController.java)
- [auth/annotation/RequireAdmin.java](../src/main/java/com/drivingschool/auth/annotation/RequireAdmin.java)
- [auth/annotation/RequireLogin.java](../src/main/java/com/drivingschool/auth/annotation/RequireLogin.java)
- [auth/config/AdminSeedRunner.java](../src/main/java/com/drivingschool/auth/config/AdminSeedRunner.java)
- [auth/controller/AuthController.java](../src/main/java/com/drivingschool/auth/controller/AuthController.java)
- [auth/controller/PasswordController.java](../src/main/java/com/drivingschool/auth/controller/PasswordController.java)
- [auth/filter/CsrfFilter.java](../src/main/java/com/drivingschool/auth/filter/CsrfFilter.java)
- [auth/form/LoginForm.java](../src/main/java/com/drivingschool/auth/form/LoginForm.java)
- [auth/form/RegistrationForm.java](../src/main/java/com/drivingschool/auth/form/RegistrationForm.java)
- [auth/interceptor/AccessInterceptor.java](../src/main/java/com/drivingschool/auth/interceptor/AccessInterceptor.java)
- [auth/interceptor/NavigationInterceptor.java](../src/main/java/com/drivingschool/auth/interceptor/NavigationInterceptor.java)
- [auth/model/AccountStatus.java](../src/main/java/com/drivingschool/auth/model/AccountStatus.java)
- [auth/model/Admin.java](../src/main/java/com/drivingschool/auth/model/Admin.java)
- [auth/model/InstructorAccount.java](../src/main/java/com/drivingschool/auth/model/InstructorAccount.java)
- [auth/model/Role.java](../src/main/java/com/drivingschool/auth/model/Role.java)
- [auth/model/User.java](../src/main/java/com/drivingschool/auth/model/User.java)
- [auth/repository/UserRepository.java](../src/main/java/com/drivingschool/auth/repository/UserRepository.java)
- [auth/service/AdminSeedService.java](../src/main/java/com/drivingschool/auth/service/AdminSeedService.java)
- [auth/service/AuthService.java](../src/main/java/com/drivingschool/auth/service/AuthService.java)
- [auth/service/AuthSessionService.java](../src/main/java/com/drivingschool/auth/service/AuthSessionService.java)
- [auth/service/CsrfTokenService.java](../src/main/java/com/drivingschool/auth/service/CsrfTokenService.java)
- [auth/service/LoginResult.java](../src/main/java/com/drivingschool/auth/service/LoginResult.java)
- [auth/service/PasswordManagementService.java](../src/main/java/com/drivingschool/auth/service/PasswordManagementService.java)
- [auth/util/AccountWriteCoordinator.java](../src/main/java/com/drivingschool/auth/util/AccountWriteCoordinator.java)
- [auth/util/PasswordHasher.java](../src/main/java/com/drivingschool/auth/util/PasswordHasher.java)
- [common/config/CustomErrorViewResolver.java](../src/main/java/com/drivingschool/common/config/CustomErrorViewResolver.java)
- [common/config/WebConfig.java](../src/main/java/com/drivingschool/common/config/WebConfig.java)
- [common/controller/HomeController.java](../src/main/java/com/drivingschool/common/controller/HomeController.java)
- [common/controller/PublicPagesController.java](../src/main/java/com/drivingschool/common/controller/PublicPagesController.java)
- [common/controller/SetupController.java](../src/main/java/com/drivingschool/common/controller/SetupController.java)
- [common/exception/DuplicateRecordException.java](../src/main/java/com/drivingschool/common/exception/DuplicateRecordException.java)
- [common/exception/GlobalExceptionHandler.java](../src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java)
- [common/exception/RecordNotFoundException.java](../src/main/java/com/drivingschool/common/exception/RecordNotFoundException.java)
- [common/exception/StorageException.java](../src/main/java/com/drivingschool/common/exception/StorageException.java)
- [common/exception/ValidationException.java](../src/main/java/com/drivingschool/common/exception/ValidationException.java)
- [common/model/ContactMessage.java](../src/main/java/com/drivingschool/common/model/ContactMessage.java)
- [common/model/DashboardSummary.java](../src/main/java/com/drivingschool/common/model/DashboardSummary.java)
- [common/model/Identifiable.java](../src/main/java/com/drivingschool/common/model/Identifiable.java)
- [common/repository/ContactMessageRepository.java](../src/main/java/com/drivingschool/common/repository/ContactMessageRepository.java)
- [common/repository/FileRepository.java](../src/main/java/com/drivingschool/common/repository/FileRepository.java)
- [common/service/ContactService.java](../src/main/java/com/drivingschool/common/service/ContactService.java)
- [common/service/DashboardSummaryProvider.java](../src/main/java/com/drivingschool/common/service/DashboardSummaryProvider.java)
- [common/service/DashboardSummaryService.java](../src/main/java/com/drivingschool/common/service/DashboardSummaryService.java)
- [common/util/IdGenerator.java](../src/main/java/com/drivingschool/common/util/IdGenerator.java)
- [DrivesparkAcademyApplication.java](../src/main/java/com/drivingschool/DrivesparkAcademyApplication.java)
- [ServletInitializer.java](../src/main/java/com/drivingschool/ServletInitializer.java)
- [student/api/StudentLookup.java](../src/main/java/com/drivingschool/student/api/StudentLookup.java)
- [student/api/StudentProfiles.java](../src/main/java/com/drivingschool/student/api/StudentProfiles.java)
- [student/controller/StudentAccountController.java](../src/main/java/com/drivingschool/student/controller/StudentAccountController.java)
- [student/controller/StudentPasswordController.java](../src/main/java/com/drivingschool/student/controller/StudentPasswordController.java)
- [student/controller/StudentProfileController.java](../src/main/java/com/drivingschool/student/controller/StudentProfileController.java)
- [student/controller/StudentProgressController.java](../src/main/java/com/drivingschool/student/controller/StudentProgressController.java)
- [student/dto/StudentProfile.java](../src/main/java/com/drivingschool/student/dto/StudentProfile.java)
- [student/dto/StudentProgress.java](../src/main/java/com/drivingschool/student/dto/StudentProgress.java)
- [student/dto/StudentSummary.java](../src/main/java/com/drivingschool/student/dto/StudentSummary.java)
- [student/form/StudentProfileForm.java](../src/main/java/com/drivingschool/student/form/StudentProfileForm.java)
- [student/model/Student.java](../src/main/java/com/drivingschool/student/model/Student.java)
- [student/service/StudentAccountService.java](../src/main/java/com/drivingschool/student/service/StudentAccountService.java)
- [student/service/StudentAdministrationService.java](../src/main/java/com/drivingschool/student/service/StudentAdministrationService.java)
- [student/service/StudentLookupService.java](../src/main/java/com/drivingschool/student/service/StudentLookupService.java)
- [student/service/StudentProfileService.java](../src/main/java/com/drivingschool/student/service/StudentProfileService.java)

**JSP / JSPF (13):**

- [admin/dashboard.jsp](../src/main/webapp/WEB-INF/views/admin/dashboard.jsp)
- [admin/students.jsp](../src/main/webapp/WEB-INF/views/admin/students.jsp)
- [auth/forgot-password.jsp](../src/main/webapp/WEB-INF/views/auth/forgot-password.jsp)
- [auth/login.jsp](../src/main/webapp/WEB-INF/views/auth/login.jsp)
- [auth/register.jsp](../src/main/webapp/WEB-INF/views/auth/register.jsp)
- [common/error-403.jsp](../src/main/webapp/WEB-INF/views/common/error-403.jsp)
- [common/error-404.jsp](../src/main/webapp/WEB-INF/views/common/error-404.jsp)
- [common/error-500.jsp](../src/main/webapp/WEB-INF/views/common/error-500.jsp)
- [home/index.jsp](../src/main/webapp/WEB-INF/views/home/index.jsp)
- [student/password.jsp](../src/main/webapp/WEB-INF/views/student/password.jsp)
- [student/profile-edit.jsp](../src/main/webapp/WEB-INF/views/student/profile-edit.jsp)
- [student/profile.jsp](../src/main/webapp/WEB-INF/views/student/profile.jsp)
- [student/progress.jsp](../src/main/webapp/WEB-INF/views/student/progress.jsp)

**CSS / JS (1):**

- [tailwind.config.js](../tailwind.config.js)

Integration documents: `README.md`, `docs/CRUD_MAP.md`, `docs/OOP_SOLID_MAP.md`, `docs/VIVA_GUIDE.md` and `docs/TAILWIND_LICENSE.txt`. Shared npm build wiring in `package.json` also belongs to 135.

### My module in five sentences

My module registers students and checks their login details. It stores password hashes and keeps profile changes behind validated methods. New student accounts are Active and can log in immediately; explicitly Pending or Suspended accounts stay blocked. Students can read and update their own profiles, while account deletion is refused when learning history still refers to them. The shared session, role, CSRF, exception and file-storage code helps all six modules work together.

### My CRUD entity and methods

| Entity | Operation | Repository → service → controller | URL | JSP |
|---|---|---|---|---|
| Student account/profile | CREATE | `UserRepository.save` → `AuthService.registerStudent` → `AuthController.register` | `POST /register` | `auth/register.jsp` |
| Student account/profile | READ | `UserRepository.readAllStrict` → `StudentProfileService.getOwnProfile` → `StudentProfileController.showProfile` | `GET /student/profile` | `student/profile.jsp` |
| Student account/profile | UPDATE | `UserRepository.update` → `StudentProfileService.updateOwnProfile` → `StudentProfileController.updateProfile` | `POST /student/profile/edit` | `student/profile-edit.jsp` |
| Student account/profile | DELETE | `UserRepository.delete` → `StudentAccountService.deleteOwnAccount` → `StudentAccountController.delete` | `POST /student/account/delete` | `student/profile.jsp` |

The arrow starts at the filing drawer; an incoming browser request travels through these layers in reverse. See [CRUD_MAP.md](CRUD_MAP.md) for separate layer rows and deletion restrictions.

### My OOP and SOLID examples

| Item | File and method | What I can say |
|---|---|---|
| Encapsulation | [Student.java](../src/main/java/com/drivingschool/student/model/Student.java) · `setPhone` | Private student details change through checked methods, so a bad phone number cannot be saved. |
| Inheritance | [Student.java](../src/main/java/com/drivingschool/student/model/Student.java) · `Student` | Student extends User and reuses the common account identity, name and email rules. |
| Abstraction | [User.java](../src/main/java/com/drivingschool/auth/model/User.java) · `getRole` | The abstract User class requires each account type to supply its role without defining one shared answer. |
| Polymorphism | [Student.java](../src/main/java/com/drivingschool/student/model/Student.java) · `getDashboardPath` | The same User dashboard method returns the student page for Student and different pages for Admin and InstructorAccount. |
| Exception handling | [StudentProfileService.java](../src/main/java/com/drivingschool/student/service/StudentProfileService.java) · `ensureAvailableEmail` | A duplicate email throws DuplicateRecordException, which GlobalExceptionHandler shows as a friendly conflict page. |
| S | [StudentProfileController.java](../src/main/java/com/drivingschool/student/controller/StudentProfileController.java) · `updateProfile` | The controller accepts the web form while StudentProfileService checks the rules and UserRepository saves it. |
| O | [User.java](../src/main/java/com/drivingschool/auth/model/User.java) · `class declaration` | A new account subtype can provide its own role and destination while existing account subclasses keep their behaviour. |
| L | [Student.java](../src/main/java/com/drivingschool/student/model/Student.java) · `getRole` | A Student can be read as a User and still supplies a valid role and dashboard destination. |
| I | [StudentProfiles.java](../src/main/java/com/drivingschool/student/api/StudentProfiles.java) · `getOwnProfile` | The profile contract exposes only the own-profile tasks needed by its page. |
| D | [StudentProfileController.java](../src/main/java/com/drivingschool/student/controller/StudentProfileController.java) · `StudentProfileController` | The controller receives StudentProfiles through its constructor instead of creating a concrete profile service. |

### Five likely questions

1. **Why store a password hash?** It lets us check a password without saving its original text.
2. **How do login and permission differ?** Login identifies the account; role and ownership checks decide what it may do.
3. **Where is your CRUD?** AuthService creates the account, StudentProfileService reads and updates it, and StudentAccountService checks safe deletion.
4. **Why refuse an account deletion?** Bookings, reviews or enrollment receipts still refer to that account.
5. **What is constructor injection?** Spring gives the controller its StudentProfiles helper through the constructor instead of the controller building one itself.

### Five-click demo

Sign in as an active student and start on Home. Use temporary demo data for any save.

1. Click **Dashboard / Profile** to read the current student details.
2. Click **Edit profile**, then type a small valid name or phone correction.
3. Click **Save changes** and show the refreshed profile.
4. Click **Progress** to show this account feedback.
5. Click **My enrollments** and explain why saved receipts protect account deletion.

### Tests to show the examiner

- [student/service/StudentAccountCrudTest.java](../src/test/java/com/drivingschool/student/service/StudentAccountCrudTest.java)
- [student/StudentProfileWebIntegrationTest.java](../src/test/java/com/drivingschool/student/StudentProfileWebIntegrationTest.java)
- [auth/service/AuthServiceTest.java](../src/test/java/com/drivingschool/auth/service/AuthServiceTest.java)
- [common/exception/GlobalExceptionHandlerTest.java](../src/test/java/com/drivingschool/common/exception/GlobalExceptionHandlerTest.java)

## IT26101133 — [Name]

**Module:** Instructors and bubble sort.

### My files

**Java (23):**

- [instructor/config/InstructorConfig.java](../src/main/java/com/drivingschool/instructor/config/InstructorConfig.java)
- [instructor/controller/InstructorAvailabilityController.java](../src/main/java/com/drivingschool/instructor/controller/InstructorAvailabilityController.java)
- [instructor/controller/InstructorDashboardController.java](../src/main/java/com/drivingschool/instructor/controller/InstructorDashboardController.java)
- [instructor/controller/PublicInstructorController.java](../src/main/java/com/drivingschool/instructor/controller/PublicInstructorController.java)
- [instructor/model/Availability.java](../src/main/java/com/drivingschool/instructor/model/Availability.java)
- [instructor/model/Instructor.java](../src/main/java/com/drivingschool/instructor/model/Instructor.java)
- [instructor/model/InstructorLeave.java](../src/main/java/com/drivingschool/instructor/model/InstructorLeave.java)
- [instructor/model/LessonRecord.java](../src/main/java/com/drivingschool/instructor/model/LessonRecord.java)
- [instructor/repository/AvailabilityRepository.java](../src/main/java/com/drivingschool/instructor/repository/AvailabilityRepository.java)
- [instructor/repository/InstructorLeaveRepository.java](../src/main/java/com/drivingschool/instructor/repository/InstructorLeaveRepository.java)
- [instructor/repository/InstructorRepository.java](../src/main/java/com/drivingschool/instructor/repository/InstructorRepository.java)
- [instructor/repository/LessonRecordRepository.java](../src/main/java/com/drivingschool/instructor/repository/LessonRecordRepository.java)
- [instructor/service/InstructorAccountService.java](../src/main/java/com/drivingschool/instructor/service/InstructorAccountService.java)
- [instructor/service/InstructorCatalog.java](../src/main/java/com/drivingschool/instructor/service/InstructorCatalog.java)
- [instructor/service/InstructorExperienceOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorExperienceOrder.java)
- [instructor/service/InstructorLessonService.java](../src/main/java/com/drivingschool/instructor/service/InstructorLessonService.java)
- [instructor/service/InstructorNameOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorNameOrder.java)
- [instructor/service/InstructorOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorOrder.java)
- [instructor/service/InstructorRatingOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorRatingOrder.java)
- [instructor/service/InstructorScheduleOperations.java](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleOperations.java)
- [instructor/service/InstructorScheduleService.java](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java)
- [instructor/service/InstructorService.java](../src/main/java/com/drivingschool/instructor/service/InstructorService.java)
- [instructor/service/InstructorSorter.java](../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java)

**JSP / JSPF (4):**

- [admin/instructors.jsp](../src/main/webapp/WEB-INF/views/admin/instructors.jsp)
- [instructor/availability.jsp](../src/main/webapp/WEB-INF/views/instructor/availability.jsp)
- [instructor/lesson-dashboard.jsp](../src/main/webapp/WEB-INF/views/instructor/lesson-dashboard.jsp)
- [public/instructors.jsp](../src/main/webapp/WEB-INF/views/public/instructors.jsp)

**CSS / JS (0):**

No separately owned files in this group; these pages use the shared UI maintained by 136.

The URL `/instructor/dashboard` renders `instructor/lesson-dashboard.jsp`, which contains the assigned lessons, profile and feedback sections.

### My module in five sentences

My module keeps instructor teaching profiles separate from their login accounts. Administrators maintain these profiles while instructors manage their own weekly hours and leave. The public instructor page uses my hand-written bubble sort to order names, experience or ratings. Instructors see assigned lessons and save feedback and skill scores for completed lessons. Validation prevents overlapping working blocks and protects confirmed lessons from unsafe availability edits.

### My CRUD entity and methods

| Entity | Operation | Repository → service → controller | URL | JSP |
|---|---|---|---|---|
| Instructor profile | CREATE | `InstructorRepository.save` → `InstructorService.createInstructor` → `InstructorAccountService.create` → `AdminManagementController.createInstructor` | `POST /admin/instructors` | `admin/instructors.jsp` |
| Instructor profile | READ | `InstructorRepository.readAllStrict` → `InstructorService.getAllInstructors` → `AdminManagementController.instructors` | `GET /admin/instructors` | `admin/instructors.jsp` |
| Instructor profile | UPDATE | `InstructorRepository.update` → `InstructorService.updateInstructor` → `InstructorAccountService.update` → `AdminManagementController.updateInstructor` | `POST /admin/instructors/update` | `admin/instructors.jsp` |
| Instructor profile | DELETE | `InstructorRepository.delete` → `InstructorService.deleteInstructor` → `InstructorAccountService.delete` → `AdminManagementController.deleteInstructor` | `POST /admin/instructors/delete` | `admin/instructors.jsp` |
| Instructor availability | CREATE | `AvailabilityRepository.save` → `InstructorScheduleService.addAvailability` → `InstructorAvailabilityController.addBlock` | `POST /instructor/availability` | `instructor/availability.jsp` |
| Instructor availability | READ | `AvailabilityRepository.readAllStrict` → `InstructorScheduleService.availability` → `InstructorAvailabilityController.page` | `GET /instructor/availability` | `instructor/availability.jsp` |
| Instructor availability | UPDATE | `AvailabilityRepository.update` → `InstructorScheduleService.updateAvailability` → `InstructorAvailabilityController.updateBlock` | `POST /instructor/availability/update` | `instructor/availability.jsp` |
| Instructor availability | DELETE | `AvailabilityRepository.delete` → `InstructorScheduleService.deleteAvailability` → `InstructorAvailabilityController.deleteBlock` | `POST /instructor/availability/delete` | `instructor/availability.jsp` |

The arrow starts at the filing drawer; an incoming browser request travels through these layers in reverse. See [CRUD_MAP.md](CRUD_MAP.md) for separate layer rows and deletion restrictions.

### My OOP and SOLID examples

| Item | File and method | What I can say |
|---|---|---|
| Encapsulation | [Instructor.java](../src/main/java/com/drivingschool/instructor/model/Instructor.java) · `setYearsOfExperience` | Private teaching details are changed through setters that reject invalid experience and contact details. |
| Inheritance | [InstructorRepository.java](../src/main/java/com/drivingschool/instructor/repository/InstructorRepository.java) · `InstructorRepository` | The instructor repository extends FileRepository and reuses its safe file operations. |
| Abstraction | [InstructorOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorOrder.java) · `compare` | The sorter asks for a comparison without needing to know which teaching detail is being compared. |
| Polymorphism | [InstructorSorter.java](../src/main/java/com/drivingschool/instructor/service/InstructorSorter.java) · `swapNeighbours` | The same compare call uses InstructorNameOrder, InstructorExperienceOrder or InstructorRatingOrder to answer differently. |
| Exception handling | [InstructorScheduleService.java](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java) · `ensureNoOverlap` | Overlapping working hours throw ValidationException and the shared handler explains the problem. |
| S | [InstructorScheduleService.java](../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java) · `addAvailability` | Scheduling rules stay in the service while its controller handles the form and its repository handles the file. |
| O | [InstructorOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorOrder.java) · `compare` | Another comparison implementation can be added without rewriting the adjacent-swap bubble-sort passes; chooseOrder still needs to select the new option. |
| L | [InstructorNameOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorNameOrder.java) · `compare` | Every comparison follows the same negative, zero or positive contract, so the sorter can substitute any existing order. |
| I | [InstructorOrder.java](../src/main/java/com/drivingschool/instructor/service/InstructorOrder.java) · `compare` | The comparison interface has one job: compare two instructor cards. |
| D | [InstructorAvailabilityController.java](../src/main/java/com/drivingschool/instructor/controller/InstructorAvailabilityController.java) · `InstructorAvailabilityController` | The controller receives InstructorScheduleOperations through its constructor. |

### Five likely questions

1. **How does bubble sort work?** Compare neighbours, swap those in the wrong order and repeat passes until the list is ordered.
2. **Why is it stable?** Equal comparisons are not swapped, so ties keep their original relative order.
3. **What is its time cost?** Worst case is O(n squared); the existing early stop makes an already sorted list O(n).
4. **Where is polymorphism?** The same InstructorOrder.compare call compares names, experience or ratings through different implementations.
5. **Why check lessons before changing working hours?** A student may already have a confirmed lesson in those hours, and an edit must protect it.

### Five-click demo

Open `/instructors` first. This five-interaction demo changes display order only.

1. Choose **Experience** in **Sort by**.
2. Choose **Descending** in **Order**.
3. Click **Sort** and show the most experienced instructor first.
4. Choose **Name** in **Sort by**, keeping descending order.
5. Click **Sort** and explain neighbouring comparisons and stable ties.

### Tests to show the examiner

- [instructor/service/InstructorAccountServiceTest.java](../src/test/java/com/drivingschool/instructor/service/InstructorAccountServiceTest.java)
- [instructor/service/InstructorScheduleServiceTest.java](../src/test/java/com/drivingschool/instructor/service/InstructorScheduleServiceTest.java)
- [instructor/service/InstructorSorterTest.java](../src/test/java/com/drivingschool/instructor/service/InstructorSorterTest.java)

## IT26101134 — [Name]

**Module:** Bookings, FIFO and scheduling.

### My files

**Java (16):**

- [booking/service/PaidEnrollmentGuard.java](../src/main/java/com/drivingschool/booking/service/PaidEnrollmentGuard.java)
- [booking/controller/AdminBookingController.java](../src/main/java/com/drivingschool/booking/controller/AdminBookingController.java)
- [booking/controller/BookingController.java](../src/main/java/com/drivingschool/booking/controller/BookingController.java)
- [booking/model/Booking.java](../src/main/java/com/drivingschool/booking/model/Booking.java)
- [booking/model/BookingStatus.java](../src/main/java/com/drivingschool/booking/model/BookingStatus.java)
- [booking/repository/BookingRepository.java](../src/main/java/com/drivingschool/booking/repository/BookingRepository.java)
- [booking/service/AdminBookingOperations.java](../src/main/java/com/drivingschool/booking/service/AdminBookingOperations.java)
- [booking/service/BookingConflictChecker.java](../src/main/java/com/drivingschool/booking/service/BookingConflictChecker.java)
- [booking/service/BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java)
- [booking/service/SlotAvailabilityService.java](../src/main/java/com/drivingschool/booking/service/SlotAvailabilityService.java)
- [booking/service/StudentBookingOperations.java](../src/main/java/com/drivingschool/booking/service/StudentBookingOperations.java)
- [booking/util/BookingOrder.java](../src/main/java/com/drivingschool/booking/util/BookingOrder.java)
- [booking/util/BookingRequestQueue.java](../src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java)
- [booking/util/LessonIntervalUtils.java](../src/main/java/com/drivingschool/booking/util/LessonIntervalUtils.java)
- [booking/util/LessonTimeOrder.java](../src/main/java/com/drivingschool/booking/util/LessonTimeOrder.java)
- [booking/util/PendingRequestOrder.java](../src/main/java/com/drivingschool/booking/util/PendingRequestOrder.java)

**JSP / JSPF (4):**

- [admin/booking-card.jspf](../src/main/webapp/WEB-INF/views/admin/booking-card.jspf)
- [admin/bookings.jsp](../src/main/webapp/WEB-INF/views/admin/bookings.jsp)
- [booking/list.jsp](../src/main/webapp/WEB-INF/views/booking/list.jsp)
- [booking/request-card.jspf](../src/main/webapp/WEB-INF/views/booking/request-card.jspf)

**CSS / JS (0):**

No separately owned files in this group; these pages use the shared UI maintained by 136.

### My module in five sentences

My module turns a student choice into a pending lesson request. It checks the package, future time, instructor hours, leave and overlapping lessons. A hand-written linked-node FIFO queue keeps unassigned requests in submission order. Administrators assign resources to the oldest request and instructors accept or decline their assigned lessons. Cancellation, rescheduling, attendance and restricted deletion keep lesson history and lesson balances consistent.

### My CRUD entity and methods

| Entity | Operation | Repository → service → controller | URL | JSP |
|---|---|---|---|---|
| Booking | CREATE | `BookingRepository.save` → `BookingService.requestLesson` → `BookingController.request` | `POST /bookings` | `booking/list.jsp` |
| Booking | READ | `BookingRepository.readAllStrict` → `BookingService.getOwnBookings` → `BookingController.list` | `GET /bookings` | `booking/list.jsp` |
| Booking | UPDATE | `BookingRepository.update` → `BookingService.cancelOwnBooking` → `BookingController.cancel` | `POST /bookings/cancel` | `booking/list.jsp` |
| Booking | DELETE | `BookingRepository.delete` → `BookingService.deleteCancelledBooking` → `BookingController.delete` | `POST /bookings/delete` | `booking/list.jsp` |

The arrow starts at the filing drawer; an incoming browser request travels through these layers in reverse. See [CRUD_MAP.md](CRUD_MAP.md) for separate layer rows and deletion restrictions.

### My OOP and SOLID examples

| Item | File and method | What I can say |
|---|---|---|
| Encapsulation | [Booking.java](../src/main/java/com/drivingschool/booking/model/Booking.java) · `Booking` | Private lesson identity, time and status are checked before a valid request object exists. |
| Inheritance | [BookingRepository.java](../src/main/java/com/drivingschool/booking/repository/BookingRepository.java) · `BookingRepository` | BookingRepository reuses the FileRepository parent rather than copying file-writing code. |
| Abstraction | [BookingOrder.java](../src/main/java/com/drivingschool/booking/util/BookingOrder.java) · `order` | Callers ask for an ordered list through one small contract. |
| Polymorphism | [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) · `getAllBookings` | The same order method builds a FIFO view through PendingRequestOrder or a chronological view through LessonTimeOrder. |
| Exception handling | [BookingService.java](../src/main/java/com/drivingschool/booking/service/BookingService.java) · `parseFilterDate` | Malformed filter dates become ValidationException messages instead of an unhandled date parsing error. |
| S | [BookingConflictChecker.java](../src/main/java/com/drivingschool/booking/service/BookingConflictChecker.java) · `conflictReason` | The conflict checker checks schedule rules while the queue keeps waiting order and repositories save records. |
| O | [BookingOrder.java](../src/main/java/com/drivingschool/booking/util/BookingOrder.java) · `order` | Another ordering implementation can be supplied without changing the existing ordering classes. |
| L | [LessonTimeOrder.java](../src/main/java/com/drivingschool/booking/util/LessonTimeOrder.java) · `order` | Both booking orders return a safe new list and leave the caller original unchanged; the caller intentionally chooses the needed subset. |
| I | [StudentBookingOperations.java](../src/main/java/com/drivingschool/booking/service/StudentBookingOperations.java) · `requestLesson` | Student web pages receive their booking tasks separately from administrator tasks. |
| D | [BookingController.java](../src/main/java/com/drivingschool/booking/controller/BookingController.java) · `BookingController` | The controller receives StudentBookingOperations and a package catalog through its constructor. |

### Five likely questions

1. **What is FIFO?** First in, first out: assign the oldest waiting request first.
2. **What are head and tail?** Head is the next node to take; tail is the last node where the next request is attached.
3. **Why have two orders?** Submission time decides fairness, while lesson start time makes the diary readable.
4. **Why recheck a displayed free slot?** Another request may take it before submission, and browser values can be changed.
5. **How does rescheduling work?** Check and save a replacement, cancel the old request and put the new request at the FIFO tail.

### Five-click demo

Sign in as student1 in temporary demo data. Prepare an instructor and a free date at least two days ahead. Do not cancel a live request or process its queue.

1. Click **Book / My bookings** and point to a saved FIFO position.
2. Click **Find available times** after choosing the prepared instructor and date.
3. Click an available hour and explain the server recheck before a request is saved.
4. Click **Current requests** to show waiting and active requests.
5. Click **History** to show earlier lesson states without submitting any action.

### Tests to show the examiner

- [booking/service/BookingServiceCrudTest.java](../src/test/java/com/drivingschool/booking/service/BookingServiceCrudTest.java)
- [booking/SchedulingWebIntegrationTest.java](../src/test/java/com/drivingschool/booking/SchedulingWebIntegrationTest.java)
- [booking/util/BookingRequestQueueTest.java](../src/test/java/com/drivingschool/booking/util/BookingRequestQueueTest.java)
- [booking/service/BookingConflictCheckerTest.java](../src/test/java/com/drivingschool/booking/service/BookingConflictCheckerTest.java)

## IT26101132 — [Name]

**Module:** Vehicles, sample data and checklist.

### My files

**Java (16):**

- [sample/DemoDataSeedRunner.java](../src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java)
- [vehicle/api/VehicleOperations.java](../src/main/java/com/drivingschool/vehicle/api/VehicleOperations.java)
- [vehicle/api/VehicleStore.java](../src/main/java/com/drivingschool/vehicle/api/VehicleStore.java)
- [vehicle/config/VehicleConfig.java](../src/main/java/com/drivingschool/vehicle/config/VehicleConfig.java)
- [vehicle/controller/VehicleController.java](../src/main/java/com/drivingschool/vehicle/controller/VehicleController.java)
- [vehicle/form/VehicleCreateForm.java](../src/main/java/com/drivingschool/vehicle/form/VehicleCreateForm.java)
- [vehicle/form/VehicleEditForm.java](../src/main/java/com/drivingschool/vehicle/form/VehicleEditForm.java)
- [vehicle/model/TransmissionType.java](../src/main/java/com/drivingschool/vehicle/model/TransmissionType.java)
- [vehicle/model/Vehicle.java](../src/main/java/com/drivingschool/vehicle/model/Vehicle.java)
- [vehicle/model/VehicleCategory.java](../src/main/java/com/drivingschool/vehicle/model/VehicleCategory.java)
- [vehicle/model/VehicleOperationalStatus.java](../src/main/java/com/drivingschool/vehicle/model/VehicleOperationalStatus.java)
- [vehicle/repository/VehicleRepository.java](../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java)
- [vehicle/service/VehicleService.java](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java)

**JSP / JSPF (3):**

- [vehicle/create.jsp](../src/main/webapp/WEB-INF/views/vehicle/create.jsp)
- [vehicle/edit.jsp](../src/main/webapp/WEB-INF/views/vehicle/edit.jsp)
- [vehicle/list.jsp](../src/main/webapp/WEB-INF/views/vehicle/list.jsp)

**CSS / JS (0):**

Vehicle pages use the shared Tailwind styles owned by IT26101136.

[TEST_CHECKLIST.md](TEST_CHECKLIST.md) also belongs to this member.



### My module in five sentences

My module keeps each teaching vehicle identity and maintenance dates together. Administrators can create, list, update and safely delete unreferenced vehicle records. Validation prevents duplicate registration plates and inconsistent service dates. The page distinguishes missing dates, upcoming renewals, expired insurance and overdue service. I also maintain repeat-safe sample data and the checklist used to rehearse the demonstration.

### My CRUD entity and methods

| Entity | Operation | Repository → service → controller | URL | JSP |
|---|---|---|---|---|
| Vehicle | CREATE | `VehicleRepository.save` → `VehicleService.createVehicle` → `VehicleController.createVehicle` | `POST /admin/vehicles/create` | `vehicle/create.jsp` |
| Vehicle | READ | `VehicleRepository.readAllStrict` → `VehicleService.getAllVehicles` → `VehicleController.listVehicles` | `GET /admin/vehicles` | `vehicle/list.jsp` |
| Vehicle | UPDATE | `VehicleRepository.update` → `VehicleService.updateVehicle` → `VehicleController.updateVehicle` | `POST /admin/vehicles/{id}/edit` | `vehicle/edit.jsp` |
| Vehicle | DELETE | `VehicleRepository.delete` → `VehicleService.deleteVehicle` → `VehicleController.deleteVehicle` | `POST /admin/vehicles/{id}/delete` | `vehicle/list.jsp` |

The arrow starts at the filing drawer; an incoming browser request travels through these layers in reverse. See [CRUD_MAP.md](CRUD_MAP.md) for separate layer rows and deletion restrictions.

### My OOP and SOLID examples

| Item | File and method | What I can say |
|---|---|---|
| Encapsulation | [Vehicle.java](../src/main/java/com/drivingschool/vehicle/model/Vehicle.java) · `setNextServiceDate` | Private maintenance dates are checked so the next service cannot be before the last service. |
| Inheritance | [VehicleRepository.java](../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java) · `VehicleRepository` | VehicleRepository extends FileRepository and selects the vehicle record type and file. |
| Abstraction | [VehicleOperations.java](../src/main/java/com/drivingschool/vehicle/api/VehicleOperations.java) · `createVehicle` | The fleet controller asks for vehicle tasks through an interface instead of file details. |
| Polymorphism | [VehicleRepository.java](../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java) · `save` | A FileRepository reference to VehicleRepository dispatches its save override; the parent writer uses the vehicle file and record type. |
| Exception handling | [VehicleService.java](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) · `ensureRegistrationNumberIsUnique` | A duplicate plate throws DuplicateRecordException and the controller redisplays the form with its message. |
| S | [VehicleService.java](../src/main/java/com/drivingschool/vehicle/service/VehicleService.java) · `deleteVehicle` | The service checks lesson-history rules while VehicleRepository handles file deletion. |
| O | [VehicleRepository.java](../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java) · `VehicleRepository` | A new child repository can select a different file and record type without rewriting shared storage. |
| L | [VehicleRepository.java](../src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java) · `update` | The child delegates to the same parent checks and preserves the shared update contract. |
| D | [VehicleController.java](../src/main/java/com/drivingschool/vehicle/controller/VehicleController.java) · `VehicleController` | The controller receives VehicleOperations through its constructor. |

### Five likely questions

1. **How are duplicate plates detected?** Normalize spaces, hyphens and letter case, then compare the plate with saved vehicles.
2. **Why use private fields?** Checked methods stop callers from storing invalid details directly.
3. **Why not call an unknown date up to date?** A missing date means the renewal or service date has not been recorded.
4. **When can a vehicle be deleted?** Only when no saved lesson still refers to it.
5. **Why can the seed run twice?** It checks for existing accounts and records before adding the original fixtures.

### Five-click demo

Sign in as the demo administrator in temporary data. Prepare a unique plate such as VIVA8888 and valid vehicle details.

1. Click **Vehicles** to show fleet cards and renewal warnings.
2. Click **Add vehicle** to open the form.
3. Click **Create vehicle** after entering the prepared details.
4. Click **Edit vehicle** on the new card and change its model or service date.
5. Click **Save changes** and explain the tested deletion guard without deleting live data.

### Tests to show the examiner

- [vehicle/service/VehicleMaintenanceTest.java](../src/test/java/com/drivingschool/vehicle/service/VehicleMaintenanceTest.java)
- [vehicle/service/VehicleServiceTest.java](../src/test/java/com/drivingschool/vehicle/service/VehicleServiceTest.java)
- [vehicle/VehicleWebIntegrationTest.java](../src/test/java/com/drivingschool/vehicle/VehicleWebIntegrationTest.java)
- [sample/DemoDataSeedRunnerTest.java](../src/test/java/com/drivingschool/sample/DemoDataSeedRunnerTest.java)

## IT26101136 — [Name]

**Module:** Learning packages, enrollment and shared UI.

### My files

**Java (15):**

- [learningpackage/service/EnrollmentAdministration.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentAdministration.java)
- [learningpackage/controller/AdminEnrollmentController.java](../src/main/java/com/drivingschool/learningpackage/controller/AdminEnrollmentController.java)
- [learningpackage/config/LearningPackageConfig.java](../src/main/java/com/drivingschool/learningpackage/config/LearningPackageConfig.java)
- [learningpackage/controller/EnrollmentController.java](../src/main/java/com/drivingschool/learningpackage/controller/EnrollmentController.java)
- [learningpackage/controller/LearningPackageController.java](../src/main/java/com/drivingschool/learningpackage/controller/LearningPackageController.java)
- [learningpackage/model/Enrollment.java](../src/main/java/com/drivingschool/learningpackage/model/Enrollment.java)
- [learningpackage/model/LearningPackage.java](../src/main/java/com/drivingschool/learningpackage/model/LearningPackage.java)
- [learningpackage/model/LearningRecord.java](../src/main/java/com/drivingschool/learningpackage/model/LearningRecord.java)
- [learningpackage/model/PackageType.java](../src/main/java/com/drivingschool/learningpackage/model/PackageType.java)
- [learningpackage/repository/EnrollmentRepository.java](../src/main/java/com/drivingschool/learningpackage/repository/EnrollmentRepository.java)
- [learningpackage/repository/LearningPackageRepository.java](../src/main/java/com/drivingschool/learningpackage/repository/LearningPackageRepository.java)
- [learningpackage/service/EnrollmentOperations.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentOperations.java)
- [learningpackage/service/EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java)
- [learningpackage/service/LearningPackageCatalog.java](../src/main/java/com/drivingschool/learningpackage/service/LearningPackageCatalog.java)
- [learningpackage/service/LearningPackageService.java](../src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java)

**JSP / JSPF (24):**

- [student/payment-instructions.jsp](../src/main/webapp/WEB-INF/views/student/payment-instructions.jsp)
- [admin/enrollments.jsp](../src/main/webapp/WEB-INF/views/admin/enrollments.jsp)
- [admin/packages.jsp](../src/main/webapp/WEB-INF/views/admin/packages.jsp)
- [common/components/confirm-dialog.jspf](../src/main/webapp/WEB-INF/views/common/components/confirm-dialog.jspf)
- [common/components/empty-state.jspf](../src/main/webapp/WEB-INF/views/common/components/empty-state.jspf)
- [common/components/page-header.jspf](../src/main/webapp/WEB-INF/views/common/components/page-header.jspf)
- [common/components/status-badge.jspf](../src/main/webapp/WEB-INF/views/common/components/status-badge.jspf)
- [common/components/tabs.jspf](../src/main/webapp/WEB-INF/views/common/components/tabs.jspf)
- [common/components/toasts.jspf](../src/main/webapp/WEB-INF/views/common/components/toasts.jspf)
- [common/dashboard-sidebar.jspf](../src/main/webapp/WEB-INF/views/common/dashboard-sidebar.jspf)
- [common/error.jsp](../src/main/webapp/WEB-INF/views/common/error.jsp)
- [common/footer.jspf](../src/main/webapp/WEB-INF/views/common/footer.jspf)
- [common/navbar.jspf](../src/main/webapp/WEB-INF/views/common/navbar.jspf)
- [common/setup-check.jsp](../src/main/webapp/WEB-INF/views/common/setup-check.jsp)
- [learningpackage/detail.jsp](../src/main/webapp/WEB-INF/views/learningpackage/detail.jsp)
- [learningpackage/list.jsp](../src/main/webapp/WEB-INF/views/learningpackage/list.jsp)
- [public/about.jsp](../src/main/webapp/WEB-INF/views/public/about.jsp)
- [public/contact.jsp](../src/main/webapp/WEB-INF/views/public/contact.jsp)
- [public/faq.jsp](../src/main/webapp/WEB-INF/views/public/faq.jsp)
- [student/enrollments.jsp](../src/main/webapp/WEB-INF/views/student/enrollments.jsp)

**CSS / JS (3):**

- [static/css/app.css](../src/main/resources/static/css/app.css)
- [static/css/tailwind.css](../src/main/resources/static/css/tailwind.css)
- [static/js/site.js](../src/main/resources/static/js/site.js)

### My module in five sentences

My module describes the driving courses offered by the school. Administrators can create, read, update and safely delete packages that have no attached history. Students request a course and receive an Awaiting payment receipt; only administrator payment confirmation grants lessons. Students can read enrollment history and update a learning goal, while Paid, Cancelled or referenced enrollments are retained. I also maintain shared cards, navigation, motion and accessible browser helpers.

### My CRUD entity and methods

| Entity | Operation | Repository → service → controller | URL | JSP |
|---|---|---|---|---|
| Package | CREATE | `LearningPackageRepository.save` → `LearningPackageService.createPackage` → `AdminManagementController.createPackage` | `POST /admin/packages` | `admin/packages.jsp` |
| Package | READ | `LearningPackageRepository.readAllStrict` → `LearningPackageService.listPackages` → `AdminManagementController.packages` | `GET /admin/packages` | `admin/packages.jsp` |
| Package | UPDATE | `LearningPackageRepository.update` → `LearningPackageService.updatePackage` → `AdminManagementController.updatePackage` | `POST /admin/packages/update` | `admin/packages.jsp` |
| Package | DELETE | `LearningPackageRepository.delete` → `LearningPackageService.deletePackage` → `AdminManagementController.deletePackage` | `POST /admin/packages/delete` | `admin/packages.jsp` |
| Enrollment | CREATE | `EnrollmentRepository.save` → `EnrollmentService.enroll` → `EnrollmentController.enroll` | `POST /enrollments` | `learningpackage/detail.jsp` |
| Enrollment | READ | `EnrollmentRepository.readAllStrict` → `EnrollmentService.ownEnrollments` → `EnrollmentController.list` | `GET /enrollments` | `student/enrollments.jsp` |
| Enrollment | UPDATE | `EnrollmentRepository.update` → `EnrollmentService.updateLearningGoal` → `EnrollmentController.update` | `POST /enrollments/update` | `student/enrollments.jsp` |
| Enrollment | DELETE | `EnrollmentRepository.delete` → `EnrollmentService.deleteOwnEnrollment` → `EnrollmentController.delete` | `POST /enrollments/delete` | `student/enrollments.jsp` |

The arrow starts at the filing drawer; an incoming browser request travels through these layers in reverse. See [CRUD_MAP.md](CRUD_MAP.md) for separate layer rows and deletion restrictions.

### My OOP and SOLID examples

| Item | File and method | What I can say |
|---|---|---|
| Encapsulation | [LearningPackage.java](../src/main/java/com/drivingschool/learningpackage/model/LearningPackage.java) · `setPrice` | Private course details are checked before changing a price or lesson count. |
| Inheritance | [LearningPackageRepository.java](../src/main/java/com/drivingschool/learningpackage/repository/LearningPackageRepository.java) · `LearningPackageRepository` | The package repository extends FileRepository and reuses its safe storage behaviour. |
| Abstraction | [LearningRecord.java](../src/main/java/com/drivingschool/learningpackage/model/LearningRecord.java) · `getDisplayName` | A small interface lets a course or enrollment provide a readable heading. |
| Polymorphism | [Enrollment.java](../src/main/java/com/drivingschool/learningpackage/model/Enrollment.java) · `getDisplayName` | LearningPackage returns its course name and Enrollment returns its enrollment heading through the same LearningRecord method; EnrollmentServiceTest demonstrates both types. |
| Exception handling | [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) · `ensureDeletable` | ValidationException explains why an active enrollment, paid receipt or referenced enrollment must be kept. |
| S | [EnrollmentService.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java) · `enroll` | The enrollment service links the student and course while repositories save the records. |
| O | [LearningRecord.java](../src/main/java/com/drivingschool/learningpackage/model/LearningRecord.java) · `getDisplayName` | Another learning record can supply its own heading without changing the existing implementations. |
| L | [LearningPackage.java](../src/main/java/com/drivingschool/learningpackage/model/LearningPackage.java) · `getDisplayName` | Both learning record types supply a valid ID and readable heading through the same contract. |
| I | [EnrollmentOperations.java](../src/main/java/com/drivingschool/learningpackage/service/EnrollmentOperations.java) · `ownEnrollments` | The enrollment page receives only the operations it needs for its own records. |
| D | [EnrollmentController.java](../src/main/java/com/drivingschool/learningpackage/controller/EnrollmentController.java) · `EnrollmentController` | The enrollment controller receives EnrollmentOperations through its constructor. |

### Five likely questions

1. **Package or enrollment?** A package is an offered course; an enrollment is one student purchase record and learning link.
2. **Why save the purchase amount?** A later package price change must not change an old receipt or revenue report.
3. **Why only one active package?** The account currently has one active course and lesson balance, so two active links would be ambiguous.
4. **Where is inheritance and polymorphism?** Repositories extend FileRepository; LearningPackage and Enrollment provide different headings through LearningRecord.
5. **Why retain paid enrollments?** They are purchase history; deletion allows only inactive unpaid enrollments with no booking references.

### Five-click demo

Sign in as student1 and open `/packages` first. The seeded account already has an enrollment, so do not submit a second purchase.

1. Click the first package detail link and show price, lessons and validity.
2. Click **Dashboard / Profile** to show the active package and lesson balance.
3. Click **My enrollments** to read the saved purchase amount and learning goal.
4. Click the learning-goal field and type a short goal in temporary demo data.
5. Click **Save learning goal** and show that ownership and the receipt amount stay unchanged.

### Tests to show the examiner

- [learningpackage/AdminEnrollmentWebIntegrationTest.java](../src/test/java/com/drivingschool/learningpackage/AdminEnrollmentWebIntegrationTest.java)
- [learningpackage/service/EnrollmentPaymentServiceTest.java](../src/test/java/com/drivingschool/learningpackage/service/EnrollmentPaymentServiceTest.java)
- [learningpackage/service/LearningPackageLifecycleTest.java](../src/test/java/com/drivingschool/learningpackage/service/LearningPackageLifecycleTest.java)
- [learningpackage/service/EnrollmentServiceTest.java](../src/test/java/com/drivingschool/learningpackage/service/EnrollmentServiceTest.java)
- [learningpackage/EnrollmentWebIntegrationTest.java](../src/test/java/com/drivingschool/learningpackage/EnrollmentWebIntegrationTest.java)

## IT26101137 — [Name]

**Module:** Reviews, moderation and reports.

### My files

**Java (15):**

- [report/controller/AdminReportController.java](../src/main/java/com/drivingschool/report/controller/AdminReportController.java)
- [report/service/AdminReportService.java](../src/main/java/com/drivingschool/report/service/AdminReportService.java)
- [report/service/ReportCatalog.java](../src/main/java/com/drivingschool/report/service/ReportCatalog.java)
- [report/service/ReportRow.java](../src/main/java/com/drivingschool/report/service/ReportRow.java)
- [report/service/ReportSection.java](../src/main/java/com/drivingschool/report/service/ReportSection.java)
- [review/api/ReviewStore.java](../src/main/java/com/drivingschool/review/api/ReviewStore.java)
- [review/controller/AdminReviewController.java](../src/main/java/com/drivingschool/review/controller/AdminReviewController.java)
- [review/controller/PublicReviewController.java](../src/main/java/com/drivingschool/review/controller/PublicReviewController.java)
- [review/controller/ReviewController.java](../src/main/java/com/drivingschool/review/controller/ReviewController.java)
- [review/model/Review.java](../src/main/java/com/drivingschool/review/model/Review.java)
- [review/model/ReviewStatus.java](../src/main/java/com/drivingschool/review/model/ReviewStatus.java)
- [review/repository/ReviewRepository.java](../src/main/java/com/drivingschool/review/repository/ReviewRepository.java)
- [review/service/ReviewModeration.java](../src/main/java/com/drivingschool/review/service/ReviewModeration.java)
- [review/service/ReviewOperations.java](../src/main/java/com/drivingschool/review/service/ReviewOperations.java)
- [review/service/ReviewService.java](../src/main/java/com/drivingschool/review/service/ReviewService.java)

**JSP / JSPF (7):**

- [admin/reports.jsp](../src/main/webapp/WEB-INF/views/admin/reports.jsp)
- [admin/reviews.jsp](../src/main/webapp/WEB-INF/views/admin/reviews.jsp)
- [common/components/review-tabs.jspf](../src/main/webapp/WEB-INF/views/common/components/review-tabs.jspf)
- [common/components/star-display.jspf](../src/main/webapp/WEB-INF/views/common/components/star-display.jspf)
- [common/components/star-input.jspf](../src/main/webapp/WEB-INF/views/common/components/star-input.jspf)
- [public/reviews.jsp](../src/main/webapp/WEB-INF/views/public/reviews.jsp)
- [review/list.jsp](../src/main/webapp/WEB-INF/views/review/list.jsp)

**CSS / JS (1):**

- [static/css/report.css](../src/main/resources/static/css/report.css)

### My module in five sentences

My module lets students write feedback about their own completed lessons. It validates stars and comments and prevents duplicate reviews for a lesson. Students edit or delete their own feedback while administrators approve, hide or delete reviews. Only approved feedback appears publicly and contributes to instructor ratings. Reports count existing records for tables, SVG charts, CSV downloads and a white print layout.

### My CRUD entity and methods

| Entity | Operation | Repository → service → controller | URL | JSP |
|---|---|---|---|---|
| Review | CREATE | `ReviewRepository.save` → `ReviewService.createReview` → `ReviewController.create` | `POST /reviews` | `review/list.jsp` |
| Review | READ | `ReviewRepository.readAllStrict` → `ReviewService.getVisibleReviews` → `ReviewController.list` | `GET /reviews` | `review/list.jsp` |
| Review | UPDATE | `ReviewRepository.update` → `ReviewService.updateOwnReview` → `ReviewController.update` | `POST /reviews/update` | `review/list.jsp` |
| Review | DELETE | `ReviewRepository.delete` → `ReviewService.deleteOwnReview` → `ReviewController.delete` | `POST /reviews/delete` | `review/list.jsp` |

The arrow starts at the filing drawer; an incoming browser request travels through these layers in reverse. See [CRUD_MAP.md](CRUD_MAP.md) for separate layer rows and deletion restrictions.

### My OOP and SOLID examples

| Item | File and method | What I can say |
|---|---|---|
| Encapsulation | [Review.java](../src/main/java/com/drivingschool/review/model/Review.java) · `updateContent` | Private rating and comment fields change only after validation, and changed feedback returns to pending moderation. |
| Inheritance | [ReviewRepository.java](../src/main/java/com/drivingschool/review/repository/ReviewRepository.java) · `ReviewRepository` | ReviewRepository extends FileRepository and chooses the reviews file. |
| Abstraction | [ReviewOperations.java](../src/main/java/com/drivingschool/review/service/ReviewOperations.java) · `getVisibleReviews` | The feedback controller asks for allowed review tasks without depending on file storage. |
| Polymorphism | [ReviewRepository.java](../src/main/java/com/drivingschool/review/repository/ReviewRepository.java) · `save` | A FileRepository reference to ReviewRepository dispatches its save override; the parent writer uses the review file and record type. |
| Exception handling | [ReviewService.java](../src/main/java/com/drivingschool/review/service/ReviewService.java) · `ensureNoDuplicate` | DuplicateRecordException explains that the completed lesson already has a review. |
| S | [AdminReportService.java](../src/main/java/com/drivingschool/report/service/AdminReportService.java) · `all` | Reports count saved records while the controller creates HTTP responses and the JSP draws charts. |
| O | [ReviewRepository.java](../src/main/java/com/drivingschool/review/repository/ReviewRepository.java) · `ReviewRepository` | A new repository child can select its own record file without changing shared storage. |
| L | [ReviewRepository.java](../src/main/java/com/drivingschool/review/repository/ReviewRepository.java) · `delete` | The child keeps the parent missing-record, validation and storage rules when deleting. |
| I | [ReviewModeration.java](../src/main/java/com/drivingschool/review/service/ReviewModeration.java) · `approveReview` | Only administrator pages receive moderation operations; student pages use ReviewOperations. |
| D | [AdminReportController.java](../src/main/java/com/drivingschool/report/controller/AdminReportController.java) · `AdminReportController` | The report controller receives ReportCatalog through its constructor. |

### Five likely questions

1. **Why require a completed lesson?** The review should describe a completed lesson belonging to the signed-in student.
2. **What happens after editing approved feedback?** Changed feedback becomes pending again and the public instructor rating is recalculated.
3. **How is an instructor rating calculated?** Add approved stars and divide by their count; no approved feedback gives zero.
4. **Do chart bars invent values?** No, each uses the exact table count, scaled against the largest count in that section.
5. **Why quote CSV cells?** Commas, quotation marks and line breaks must stay inside their original spreadsheet cell.

### Five-click demo

Sign in as student6 in temporary demo data. This account owns seeded reviews; rehearse edits only in temporary data.

1. Click **Reviews** to show public feedback and this student own reviews.
2. Click **Edit your feedback** on an owned card.
3. Click the third star and type a valid comment.
4. Click **Save review** and show its pending moderation status.
5. Click **Edit your feedback** again to read back the saved stars and comment.

### Tests to show the examiner

- [review/ReviewWebIntegrationTest.java](../src/test/java/com/drivingschool/review/ReviewWebIntegrationTest.java)
- [review/service/ReviewVisibilityTest.java](../src/test/java/com/drivingschool/review/service/ReviewVisibilityTest.java)
- [review/service/AdminReportServiceTest.java](../src/test/java/com/drivingschool/review/service/AdminReportServiceTest.java)
- [review/service/InstructorRatingRecalculationTest.java](../src/test/java/com/drivingschool/review/service/InstructorRatingRecalculationTest.java)

## Shared file ownership

| File | One owner |
|---|---|
| [src/main/webapp/WEB-INF/views/common/components/confirm-dialog.jspf](../src/main/webapp/WEB-INF/views/common/components/confirm-dialog.jspf) | IT26101136 |
| [src/main/webapp/WEB-INF/views/common/components/empty-state.jspf](../src/main/webapp/WEB-INF/views/common/components/empty-state.jspf) | IT26101136 |
| [src/main/webapp/WEB-INF/views/common/components/page-header.jspf](../src/main/webapp/WEB-INF/views/common/components/page-header.jspf) | IT26101136 |
| [src/main/webapp/WEB-INF/views/common/components/review-tabs.jspf](../src/main/webapp/WEB-INF/views/common/components/review-tabs.jspf) | IT26101137 |
| [src/main/webapp/WEB-INF/views/common/components/star-display.jspf](../src/main/webapp/WEB-INF/views/common/components/star-display.jspf) | IT26101137 |
| [src/main/webapp/WEB-INF/views/common/components/star-input.jspf](../src/main/webapp/WEB-INF/views/common/components/star-input.jspf) | IT26101137 |
| [src/main/webapp/WEB-INF/views/common/components/status-badge.jspf](../src/main/webapp/WEB-INF/views/common/components/status-badge.jspf) | IT26101136 |
| [src/main/webapp/WEB-INF/views/common/components/tabs.jspf](../src/main/webapp/WEB-INF/views/common/components/tabs.jspf) | IT26101136 |
| [src/main/webapp/WEB-INF/views/common/components/toasts.jspf](../src/main/webapp/WEB-INF/views/common/components/toasts.jspf) | IT26101136 |
| [src/main/webapp/WEB-INF/views/common/dashboard-sidebar.jspf](../src/main/webapp/WEB-INF/views/common/dashboard-sidebar.jspf) | IT26101136 |
| [src/main/webapp/WEB-INF/views/common/error-403.jsp](../src/main/webapp/WEB-INF/views/common/error-403.jsp) | IT26101135 |
| [src/main/webapp/WEB-INF/views/common/error-404.jsp](../src/main/webapp/WEB-INF/views/common/error-404.jsp) | IT26101135 |
| [src/main/webapp/WEB-INF/views/common/error-500.jsp](../src/main/webapp/WEB-INF/views/common/error-500.jsp) | IT26101135 |
| [src/main/webapp/WEB-INF/views/common/error.jsp](../src/main/webapp/WEB-INF/views/common/error.jsp) | IT26101136 |
| [src/main/webapp/WEB-INF/views/common/footer.jspf](../src/main/webapp/WEB-INF/views/common/footer.jspf) | IT26101136 |
| [src/main/webapp/WEB-INF/views/common/navbar.jspf](../src/main/webapp/WEB-INF/views/common/navbar.jspf) | IT26101136 |
| [src/main/webapp/WEB-INF/views/common/setup-check.jsp](../src/main/webapp/WEB-INF/views/common/setup-check.jsp) | IT26101136 |
| [src/main/resources/static/css/app.css](../src/main/resources/static/css/app.css) | IT26101136 |
| [src/main/resources/static/css/report.css](../src/main/resources/static/css/report.css) | IT26101137 |
| [src/main/resources/static/css/tailwind.css](../src/main/resources/static/css/tailwind.css) | IT26101136 |
| [src/main/resources/static/js/site.js](../src/main/resources/static/js/site.js) | IT26101136 |
| [tailwind.config.js](../tailwind.config.js) | IT26101135 |

Shared Java foundation (`common/**`, accounts and application startup) belongs to 135. Each member's repositories reuse FileRepository. InstructorAccount belongs to 135 even though 133 coordinates it with the teaching profile. AdminManagementController belongs to 135 even where 133/136 explain their forms. Other modules consume the shared UI without claiming ownership of its files.

## Short explanations of harder shared code

- **Generic T:** the filing drawer holds one chosen record type, such as Vehicle or Review; it does not mix unrelated records in one drawer.
- **Optional:** a lookup may return a record or no record; check before using it.
- **Clock:** the app uses Colombo time, while tests can supply a fixed time.
- **Lock / synchronized:** one change finishes before a conflicting thread changes data in this JVM; several file writes are still not a database transaction.
- **Atomic replacement:** write a complete temporary file, then replace the old file to reduce partial-write damage.
- **Existing callback:** AccountWriteCoordinator runs the supplied steps while holding its shared account lock.
- **Record:** a small read-only data holder whose constructor and accessors Java supplies.
- **Bubble sort:** worst case O(n squared), best case O(n) with the existing early stop; ties stay stable and the caller list is copied.
- **Linked FIFO:** enqueue/dequeue with head and tail are O(1); finding a queue position walks the nodes, O(n). Restoring timestamp order uses manual insertion sorting, O(n squared) worst case.
- **Reports:** bar widths compare groups within one section; tables and CSV keep the exact values.

## Verification limits

Consult [TEST_CHECKLIST.md](TEST_CHECKLIST.md) for dated evidence. A passing JUnit test does not establish a visual browser pass. Pages or states not viewed at 375, 768 and 1280 pixels remain visually UNVERIFIED. Destructive live actions were not submitted. The complete role/menu/static-file audit and Git branch plan belong to Phase 6.

## Manual payment flow: five-step viva demo (2026-11-08)

Use a fresh temporary demo directory. The app does not send email or accept uploaded receipts. Use your own email service outside the application; the administrator checks payment manually.

1. **Register and log in** as a new student (IT26101135). The saved account is Active immediately; an explicitly Pending account still cannot log in.
2. **Enroll** in a package (IT26101136). Show the captured amount, `ENR-<id>` reference, Awaiting payment state, configured contact email and payment instructions. No lessons or validity are granted yet.
3. **Try Book / My bookings** (IT26101134). The page explains the Paid requirement and links to My enrollments. A forged POST is checked again on the server.
4. **Use a separate admin session**, open Awaiting payment and confirm **Mark as Paid** (IT26101136). The cashier checks the receipt outside this system first. Confirmation starts validity today and grants the package's lesson count. Repeating the action says already handled.
5. **Return to the student session and book** an available instructor hour. The request joins the hand-written FIFO queue. Its payment is checked again on reschedule, assignment, reassignment, FIFO processing, instructor acceptance and completion.

### Files and explanations to show

- IT26101135: `UserRepository.clearUnpaidBalance` hides unsupported balances; `DashboardSummaryService.countPayments` calculates the waiting count and Paid revenue without writing records. `StudentEnrollmentBalanceTest` covers Paid, unpaid, Cancelled and missing receipts.
- IT26101136: `Enrollment.setStatus` validates three clear states; `getPaymentStatus` maps only Paid to PAID. Missing old payment fields default to Paid. `EnrollmentAdministration` is a small admin interface, injected into `AdminEnrollmentController`. `EnrollmentService.markPaid` and `cancelRequest` reload and decide inside the shared cashier lock. `EnrollmentPaymentServiceTest` covers repeat clicks and competing administrators; `AdminEnrollmentWebIntegrationTest` covers real roles and CSRF.
- IT26101134: `PaidEnrollmentGuard.problemFor` reads the receipt, its owner, balance, course and expiry. `BookingService` uses the same lock as account changes. Invalid unpaid or Cancelled queue heads become REJECTED with an explanation. Student cancellation, force cancellation, rejection and deletion of already-cancelled bookings remain available for cleanup.
- IT26101132: `DemoDataSeedRunner.seedPaymentExample` adds one Awaiting payment and one Cancelled receipt alongside seven Paid receipts. Restarting does not duplicate receipts, undo decisions or refill an exhausted balance. Student8 is Active for the cashier demo; student9 remains Pending and student10 Suspended.
- IT26101137: `AdminReportService.revenue` counts Paid receipts only. Awaiting payment and Cancelled amounts never enter the Revenue report or its CSV rows.

### Short answers for the payment demo

- **Why is enrollment not booking permission?** An unpaid request is only a payment slip; a Paid receipt and remaining lessons are needed to reserve a lesson.
- **Why save the amount?** A later course price change must not change the student's requested price or old revenue.
- **Why one lock?** It acts like one cashier desk, so two administrators cannot both decide the same request or refill lessons twice. The lock protects this one running Java application, not several servers sharing files.
- **What happens to cancelled requests?** Their reference and optional reason stay in history, and any linked Pending bookings are rejected. The student may request a package again.
- **Why can an admin not confirm a Suspended student or inactive course?** The current account and course must permit learning before lesson tickets are issued. A Pending student also needs account approval first.


Cleanup note (9 November 2026): unused pagination.jspf, skeleton.jspf, search-bar.jspf and breadcrumbs.jspf were removed after approval. They are no longer part of the current file inventory. Existing search/breadcrumb controls are implemented by their pages; no feature or Java class was removed.
