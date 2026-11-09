# Git branch plan

Owner: **IT26101135**. Checked on **2026-11-09**. Phase A only. Names are not needed; this plan uses IDs.

**Recommendation: ONE integration PR from `IT26101135-finalUI_changes` into `develop`.** Six separate whole-module PRs cannot currently be ordered so that every intermediate build works. There are cross-module dependency cycles. Exact member copy recipes are provided, but their build gates must not be bypassed.

**Not all work is committed in this checkout.** At the start there were 28 modified tracked files and four untracked source/test files. Copying from the current branch commit would miss some latest UI/error-handling work. Save the complete source snapshot first.

The agent used read-only Git only. All fetch, checkout, add, commit, push, branch and merge commands below are **for the team to run**, not actions already taken. Only this document is written in Phase A.

## 1. Actual comparison and limits

- Current branch: `IT26101135-finalUI_changes`.
- Current committed tip: `fa0bd38331abb49930bd5b702eeaa1e5439401f8`.
- No local branch named `develop` exists. The initial read-only comparison to `develop` failed.
- Cached remote-tracking **`origin/develop` does exist locally**, at `041150fe5fa2d78733898197d7234ba12eabb5a3`; its latest displayed commit date is 2026-11-06. Reading this ref did not contact GitHub.
- Committed comparison, `git diff origin/develop HEAD`: **325 files changed, 19148 insertions(+), 2959 deletions(-)**.
- Tracked working-tree comparison, `git diff origin/develop`: **325 files changed, 19482 insertions(+), 2982 deletions(-)**.
- The stat excludes untracked files. Including the four untracked paths gives **329 changed/deleted/new paths** before creating this plan.
- This plan adds one path: the ownership manifest therefore contains **330 paths**, each assigned exactly once.
- `pom.xml`, Maven wrapper files and `data/.gitkeep` are unchanged relative to this cached base. `git ls-files target node_modules data-demo data` returned only `data/.gitkeep`.
- Live GitHub refs, open PRs, branch protection and remote freshness are **UNVERIFIED**; no fetch was run.

Read-only commands used:

```powershell
git status --short --branch
git branch -a
git diff --stat origin/develop
git diff --name-status --no-renames origin/develop
git diff --name-status origin/develop HEAD
git ls-files --others --exclude-standard
git log -1 --format=%H HEAD
git log -1 --format=%H origin/develop
```

To refresh it, **you** may run `git fetch origin`. If local develop is absent, create it once with `git checkout -b develop origin/develop`; otherwise use `git checkout develop`. Then `git pull --ff-only origin develop`. If neither ref exists, fetch first. Stop if the refreshed diff differs from this manifest and update the plan before copying files.

<details>
<summary>Actual tracked diff --stat against cached origin/develop</summary>

```text
.gitignore                                         |    5 +-
 CONTRACTS.md                                       |  136 +-
 HELP.md                                            |   28 -
 README.md                                          |  298 ++++-
 REPORT.md                                          |   49 +
 docs/CRUD_MAP.md                                   |  156 +++
 docs/OOP_SOLID_MAP.md                              |   94 ++
 docs/TAILWIND_LICENSE.txt                          |   21 +
 docs/TEST_CHECKLIST.md                             |  212 +++
 docs/VIVA_GUIDE.md                                 |  729 ++++++++++
 package-lock.json                                  | 1021 ++++++++++++++
 package.json                                       |    9 +
 .../DrivesparkAcademyApplication.java              |   13 +
 .../java/com/drivingschool/ServletInitializer.java |   13 +
 .../admin/controller/AdminDashboardController.java |   23 +
 .../controller/AdminManagementController.java      |  276 ++++
 .../auth/annotation/RequireAdmin.java              |   11 +
 .../auth/annotation/RequireLogin.java              |   11 +
 .../drivingschool/auth/config/AdminSeedRunner.java |   13 +
 .../auth/controller/AuthController.java            |   65 +-
 .../auth/controller/PasswordController.java        |   85 ++
 .../com/drivingschool/auth/filter/CsrfFilter.java  |   13 +
 .../com/drivingschool/auth/form/LoginForm.java     |   17 +
 .../drivingschool/auth/form/RegistrationForm.java  |   23 +
 .../auth/interceptor/AccessInterceptor.java        |   16 +
 .../auth/interceptor/NavigationInterceptor.java    |   13 +
 .../drivingschool/auth/model/AccountStatus.java    |   19 +
 .../java/com/drivingschool/auth/model/Admin.java   |   18 +
 .../auth/model/InstructorAccount.java              |   45 +
 .../java/com/drivingschool/auth/model/Role.java    |   14 +-
 .../java/com/drivingschool/auth/model/User.java    |   61 +-
 .../auth/repository/UserRepository.java            |   96 +-
 .../auth/service/AdminSeedService.java             |   14 +
 .../drivingschool/auth/service/AuthService.java    |   33 +-
 .../auth/service/AuthSessionService.java           |   56 +-
 .../auth/service/CsrfTokenService.java             |   15 +
 .../drivingschool/auth/service/LoginResult.java    |   19 +
 .../auth/service/PasswordManagementService.java    |  115 ++
 .../auth/util/AccountWriteCoordinator.java         |   16 +
 .../drivingschool/auth/util/PasswordHasher.java    |   23 +-
 .../booking/controller/AdminBookingController.java |  175 +++
 .../booking/controller/BookingController.java      |  216 +++
 .../com/drivingschool/booking/model/Booking.java   |  119 +-
 .../drivingschool/booking/model/BookingStatus.java |   17 +-
 .../booking/repository/BookingRepository.java      |   35 +
 .../booking/service/AdminBookingOperations.java    |   37 +
 .../booking/service/BookingConflictChecker.java    |  178 +++
 .../booking/service/BookingService.java            |  592 +++++++++
 .../booking/service/PaidEnrollmentGuard.java       |   89 ++
 .../booking/service/SlotAvailabilityService.java   |  122 ++
 .../booking/service/StudentBookingOperations.java  |   37 +
 .../drivingschool/booking/util/BookingOrder.java   |   25 +
 .../booking/util/BookingRequestQueue.java          |  109 ++
 .../booking/util/LessonIntervalUtils.java          |   13 +
 .../booking/util/LessonTimeOrder.java              |   37 +
 .../booking/util/PendingRequestOrder.java          |   25 +
 .../common/config/CustomErrorViewResolver.java     |   49 +
 .../com/drivingschool/common/config/WebConfig.java |    1 +
 .../common/controller/HomeController.java          |   22 +-
 .../common/controller/PublicPagesController.java   |   48 +
 .../common/controller/SetupController.java         |    1 +
 .../common/exception/DuplicateRecordException.java |   12 +
 .../common/exception/GlobalExceptionHandler.java   |   67 +-
 .../common/exception/RecordNotFoundException.java  |   12 +
 .../common/exception/StorageException.java         |   12 +
 .../common/exception/ValidationException.java      |   12 +
 .../drivingschool/common/model/ContactMessage.java |   49 +
 .../common/model/DashboardSummary.java             |   98 ++
 .../drivingschool/common/model/Identifiable.java   |   13 +
 .../repository/ContactMessageRepository.java       |   17 +
 .../common/repository/FileRepository.java          |   70 +-
 .../common/service/ContactService.java             |   28 +
 .../common/service/DashboardSummaryProvider.java   |   19 +
 .../common/service/DashboardSummaryService.java    |   91 ++
 .../com/drivingschool/common/util/IdGenerator.java |    1 +
 .../instructor/api/InstructorBookingInfo.java      |    7 -
 .../instructor/api/InstructorLookup.java           |   14 -
 .../instructor/config/InstructorConfig.java        |   54 +-
 .../InstructorAvailabilityController.java          |  127 ++
 .../controller/InstructorDashboardController.java  |  212 +++
 .../controller/PublicInstructorController.java     |   50 +
 .../instructor/model/Availability.java             |   59 +
 .../drivingschool/instructor/model/Instructor.java |   92 ++
 .../instructor/model/InstructorAvailability.java   |   63 -
 .../instructor/model/InstructorLeave.java          |   54 +
 .../instructor/model/LessonRecord.java             |   89 ++
 .../repository/AvailabilityRepository.java         |   50 +
 .../InstructorAvailabilityRepository.java          |   23 -
 .../repository/InstructorLeaveRepository.java      |   50 +
 .../repository/InstructorRepository.java           |   35 +
 .../repository/LessonRecordRepository.java         |   50 +
 .../service/InstructorAccountService.java          |  152 +++
 .../service/InstructorAvailabilityService.java     |   80 --
 .../instructor/service/InstructorCatalog.java      |   24 +
 .../service/InstructorExperienceOrder.java         |   25 +
 .../service/InstructorLessonService.java           |  133 ++
 .../service/InstructorLookupService.java           |  102 --
 .../instructor/service/InstructorNameOrder.java    |   25 +
 .../instructor/service/InstructorOrder.java        |   23 +
 .../instructor/service/InstructorRatingOrder.java  |   25 +
 .../service/InstructorScheduleOperations.java      |   41 +
 .../service/InstructorScheduleService.java         |  226 ++++
 .../instructor/service/InstructorService.java      |   81 +-
 .../instructor/service/InstructorSorter.java       |   89 +-
 .../learningpackage/api/EnrollmentLookup.java      |    8 -
 .../learningpackage/api/EnrollmentSummary.java     |   13 -
 .../config/LearningPackageConfig.java              |   24 +-
 .../controller/AdminEnrollmentController.java      |   91 ++
 .../controller/EnrollmentController.java           |  159 +++
 .../controller/LearningPackageController.java      |   64 +
 .../PackageEnrollmentEntryController.java          |   46 +
 .../controller/PackageNavigationAdvice.java        |   46 +
 .../learningpackage/model/Enrollment.java          |  171 +++
 .../learningpackage/model/LearningPackage.java     |   62 +-
 .../learningpackage/model/LearningRecord.java      |   23 +
 .../model/PackageNavigationCard.java               |   50 +
 .../learningpackage/model/PackageType.java         |   19 +
 .../repository/EnrollmentRepository.java           |   53 +
 .../repository/LearningPackageRepository.java      |   36 +
 .../service/EnrollmentAdministration.java          |   36 +
 .../service/EnrollmentOperations.java              |   39 +
 .../learningpackage/service/EnrollmentService.java |  309 +++++
 .../service/LearningPackageCatalog.java            |   37 +
 .../service/LearningPackageService.java            |  127 +-
 .../report/controller/AdminReportController.java   |   71 +
 .../report/service/AdminReportService.java         |  198 +++
 .../report/service/ReportCatalog.java              |   21 +
 .../drivingschool/report/service/ReportRow.java    |   35 +
 .../report/service/ReportSection.java              |   37 +
 .../com/drivingschool/review/api/ReviewStore.java  |   44 +
 .../review/controller/AdminReviewController.java   |   97 ++
 .../review/controller/PublicReviewController.java  |   40 +
 .../review/controller/ReviewController.java        |  138 ++
 .../com/drivingschool/review/model/Review.java     |   72 +-
 .../drivingschool/review/model/ReviewStatus.java   |   11 +
 .../review/repository/ReviewRepository.java        |   41 +-
 .../review/service/ReviewModeration.java           |   31 +
 .../review/service/ReviewOperations.java           |   35 +
 .../review/service/ReviewService.java              |  279 +++-
 .../drivingschool/sample/DemoDataSeedRunner.java   |  286 ++++
 .../drivingschool/student/api/StudentLookup.java   |   15 +
 .../drivingschool/student/api/StudentProfiles.java |   28 +
 .../controller/StudentAccountController.java       |   49 +
 .../controller/StudentPasswordController.java      |   86 ++
 .../controller/StudentProfileController.java       |   40 +-
 .../controller/StudentProgressController.java      |   43 +
 .../drivingschool/student/dto/StudentProfile.java  |   54 +
 .../drivingschool/student/dto/StudentProgress.java |   35 +
 .../drivingschool/student/dto/StudentSummary.java  |   14 +
 .../student/form/StudentProfileForm.java           |   44 +
 .../com/drivingschool/student/model/Student.java   |  130 +-
 .../student/service/StudentAccountService.java     |   67 +
 .../service/StudentAdministrationService.java      |  153 +++
 .../student/service/StudentLookupService.java      |   30 +-
 .../student/service/StudentProfileService.java     |  112 +-
 .../vehicle/api/VehicleBookingInfo.java            |   12 +
 .../drivingschool/vehicle/api/VehicleLookup.java   |   14 +
 .../vehicle/api/VehicleOperations.java             |   46 +
 .../drivingschool/vehicle/api/VehicleStore.java    |   52 +
 .../vehicle/config/VehicleConfig.java              |   25 +-
 .../vehicle/controller/VehicleController.java      |   79 +-
 .../vehicle/form/VehicleCreateForm.java            |   41 +-
 .../vehicle/form/VehicleEditForm.java              |   47 +-
 .../vehicle/model/TransmissionType.java            |   11 +
 .../com/drivingschool/vehicle/model/Vehicle.java   |   78 +-
 .../vehicle/model/VehicleCategory.java             |   11 +
 .../vehicle/model/VehicleOperationalStatus.java    |   11 +
 .../vehicle/repository/VehicleRepository.java      |   41 +-
 .../vehicle/service/VehicleLookupService.java      |   38 +-
 .../vehicle/service/VehicleService.java            |  155 ++-
 src/main/resources/application.properties          |   16 +-
 src/main/resources/static/css/INTER_LICENSE.txt    |   93 ++
 src/main/resources/static/css/admin-dashboard.css  |   82 --
 src/main/resources/static/css/app.css              | 1395 +++++++++++++-------
 .../static/css/inter-latin-variable.woff2          |  Bin 0 -> 48256 bytes
 src/main/resources/static/css/profile.css          |   17 -
 src/main/resources/static/css/report.css           |   37 +
 src/main/resources/static/css/tailwind.css         |   13 +
 src/main/resources/static/css/vehicle.css          |  339 -----
 src/main/resources/static/images/README.md         |   34 +
 src/main/resources/static/images/cta-drive.webp    |  Bin 0 -> 245918 bytes
 src/main/resources/static/images/error-road.svg    |    6 +
 src/main/resources/static/images/favicon.svg       |   16 +
 src/main/resources/static/images/flexible-bg.webp  |  Bin 0 -> 110698 bytes
 src/main/resources/static/images/hero-road.webp    |  Bin 0 -> 122852 bytes
 src/main/resources/static/images/location.webp     |  Bin 0 -> 84334 bytes
 src/main/resources/static/images/logo.svg          |   10 +
 src/main/resources/static/images/mission.webp      |  Bin 0 -> 303206 bytes
 src/main/resources/static/images/promo-lesson.webp |  Bin 0 -> 131188 bytes
 src/main/resources/static/images/safety-bg.webp    |  Bin 0 -> 55860 bytes
 .../resources/static/images/vehicle-automatic.webp |  Bin 0 -> 29674 bytes
 .../resources/static/images/vehicle-manual.webp    |  Bin 0 -> 29378 bytes
 .../static/images/vehicle-motorcycle.webp          |  Bin 0 -> 42800 bytes
 src/main/resources/static/js/site.js               |  634 +++++++++
 .../webapp/WEB-INF/views/admin/booking-card.jspf   |   29 +
 src/main/webapp/WEB-INF/views/admin/bookings.jsp   |   34 +
 src/main/webapp/WEB-INF/views/admin/dashboard.jsp  |  117 +-
 .../webapp/WEB-INF/views/admin/enrollments.jsp     |   64 +
 .../webapp/WEB-INF/views/admin/instructors.jsp     |   57 +
 src/main/webapp/WEB-INF/views/admin/packages.jsp   |   84 ++
 src/main/webapp/WEB-INF/views/admin/reports.jsp    |   55 +
 src/main/webapp/WEB-INF/views/admin/reviews.jsp    |   46 +
 src/main/webapp/WEB-INF/views/admin/students.jsp   |   71 +
 .../webapp/WEB-INF/views/auth/forgot-password.jsp  |   26 +
 src/main/webapp/WEB-INF/views/auth/login.jsp       |   54 +-
 src/main/webapp/WEB-INF/views/auth/register.jsp    |   58 +-
 src/main/webapp/WEB-INF/views/booking/list.jsp     |   40 +
 .../webapp/WEB-INF/views/booking/request-card.jspf |   29 +
 .../views/common/components/breadcrumbs.jspf       |   13 +
 .../views/common/components/confirm-dialog.jspf    |   13 +
 .../views/common/components/empty-state.jspf       |   13 +
 .../views/common/components/page-header.jspf       |   13 +
 .../views/common/components/pagination.jspf        |   13 +
 .../views/common/components/review-tabs.jspf       |   19 +
 .../views/common/components/search-bar.jspf        |   13 +
 .../WEB-INF/views/common/components/skeleton.jspf  |   13 +
 .../views/common/components/star-display.jspf      |   15 +
 .../views/common/components/star-input.jspf        |   37 +
 .../views/common/components/status-badge.jspf      |   15 +
 .../WEB-INF/views/common/components/tabs.jspf      |   20 +
 .../WEB-INF/views/common/components/toasts.jspf    |   14 +
 .../WEB-INF/views/common/dashboard-sidebar.jspf    |   46 +
 src/main/webapp/WEB-INF/views/common/error-403.jsp |   15 +
 src/main/webapp/WEB-INF/views/common/error-404.jsp |   15 +
 src/main/webapp/WEB-INF/views/common/error-500.jsp |   15 +
 src/main/webapp/WEB-INF/views/common/error.jsp     |   34 +-
 src/main/webapp/WEB-INF/views/common/footer.jspf   |   43 +-
 src/main/webapp/WEB-INF/views/common/navbar.jspf   |  184 +--
 .../webapp/WEB-INF/views/common/setup-check.jsp    |   24 +-
 src/main/webapp/WEB-INF/views/home/index.jsp       |  199 +--
 .../WEB-INF/views/instructor/availability.jsp      |   59 +
 .../WEB-INF/views/instructor/lesson-dashboard.jsp  |   98 ++
 .../WEB-INF/views/learningpackage/detail.jsp       |   43 +
 .../webapp/WEB-INF/views/learningpackage/list.jsp  |   57 +
 src/main/webapp/WEB-INF/views/public/about.jsp     |   38 +
 src/main/webapp/WEB-INF/views/public/contact.jsp   |   44 +
 src/main/webapp/WEB-INF/views/public/faq.jsp       |   40 +
 .../webapp/WEB-INF/views/public/instructors.jsp    |   52 +
 src/main/webapp/WEB-INF/views/public/reviews.jsp   |   42 +
 src/main/webapp/WEB-INF/views/review/list.jsp      |   70 +
 .../webapp/WEB-INF/views/student/enrollments.jsp   |   97 ++
 src/main/webapp/WEB-INF/views/student/password.jsp |   20 +
 .../WEB-INF/views/student/payment-instructions.jsp |   54 +
 .../webapp/WEB-INF/views/student/profile-edit.jsp  |   51 +-
 src/main/webapp/WEB-INF/views/student/profile.jsp  |   70 +-
 src/main/webapp/WEB-INF/views/student/progress.jsp |   65 +
 src/main/webapp/WEB-INF/views/vehicle/create.jsp   |   69 +-
 src/main/webapp/WEB-INF/views/vehicle/edit.jsp     |   66 +-
 src/main/webapp/WEB-INF/views/vehicle/list.jsp     |  206 ++-
 .../admin/AdminDashboardWebIntegrationTest.java    |   16 +-
 .../drivingschool/auth/AuthWebIntegrationTest.java |   41 +-
 .../auth/config/AdminSeedRunnerTest.java           |    1 +
 .../auth/controller/AuthControllerTest.java        |    1 +
 .../controller/PackageEnrollmentReturnTest.java    |   45 +
 .../drivingschool/auth/filter/CsrfFilterTest.java  |    1 +
 .../auth/interceptor/AccessInterceptorTest.java    |   30 +-
 .../interceptor/NavigationInterceptorTest.java     |    1 +
 .../drivingschool/auth/model/UserModelTest.java    |    3 +-
 .../repository/StudentEnrollmentBalanceTest.java   |   90 ++
 .../auth/repository/UserRepositoryTest.java        |    1 +
 .../auth/service/AdminSeedServiceTest.java         |    1 +
 .../auth/service/AuthServiceTest.java              |   31 +-
 .../auth/service/AuthSessionServiceTest.java       |   22 +-
 .../auth/service/CsrfTokenServiceTest.java         |    1 +
 .../auth/util/PasswordHasherTest.java              |   11 +-
 .../booking/SchedulingWebIntegrationTest.java      |  341 +++++
 .../drivingschool/booking/model/BookingTest.java   |    5 +-
 .../BookingRepositorySpringIntegrationTest.java    |    1 +
 .../booking/repository/BookingRepositoryTest.java  |    1 +
 .../service/BookingConflictCheckerTest.java        |  134 ++
 .../booking/service/BookingCutoffTest.java         |   66 +
 .../booking/service/BookingServiceCrudTest.java    |  138 ++
 .../service/SlotAvailabilityServiceTest.java       |   46 +
 .../booking/service/SlotBoundaryTest.java          |   54 +
 .../booking/util/BookingOrderTest.java             |   55 +
 .../booking/util/BookingRequestQueueTest.java      |   38 +
 .../booking/util/LessonIntervalUtilsTest.java      |    1 +
 .../controller/DashboardSummaryServiceTest.java    |   80 ++
 .../common/controller/HomeControllerTest.java      |    1 +
 .../exception/AuthorizationErrorHandlerTest.java   |   29 +-
 .../exception/GlobalExceptionHandlerTest.java      |   19 +-
 .../common/repository/FileRepositoryTest.java      |    1 +
 .../drivingschool/common/util/IdGeneratorTest.java |    1 +
 .../instructor/config/InstructorConfigTest.java    |   34 +-
 .../model/InstructorAvailabilityTest.java          |  206 ---
 .../instructor/model/InstructorTest.java           |    1 +
 .../InstructorAvailabilityRepositoryTest.java      |   97 --
 .../repository/InstructorRepositoryTest.java       |    1 +
 .../service/InstructorAccountServiceTest.java      |   88 ++
 .../service/InstructorAvailabilityServiceTest.java |  221 ----
 .../service/InstructorLookupServiceTest.java       |  215 ---
 .../service/InstructorScheduleServiceTest.java     |  154 +++
 .../instructor/service/InstructorServiceTest.java  |    1 +
 .../instructor/service/InstructorSorterTest.java   |   27 +
 .../AdminEnrollmentWebIntegrationTest.java         |  143 ++
 .../EnrollmentWebIntegrationTest.java              |  169 +++
 .../config/LearningPackageConfigTest.java          |    1 +
 .../learningpackage/model/LearningPackageTest.java |    2 +-
 .../repository/LearningPackageRepositoryTest.java  |    1 +
 .../service/EnrollmentPaymentServiceTest.java      |  199 +++
 .../service/EnrollmentServiceTest.java             |  248 ++++
 .../service/LearningPackageLifecycleTest.java      |   97 ++
 .../service/LearningPackageServiceTest.java        |    1 +
 .../service/PackageNavigationAdviceTest.java       |   64 +
 .../review/ReviewWebIntegrationTest.java           |  219 +++
 .../com/drivingschool/review/model/ReviewTest.java |    1 +
 .../review/repository/ReviewRepositoryTest.java    |    3 +-
 .../review/service/AdminReportServiceTest.java     |  153 +++
 .../service/InstructorRatingRecalculationTest.java |   47 +
 .../service/ReviewServiceConcurrencyTest.java      |    1 +
 .../review/service/ReviewServiceTest.java          |    3 +-
 .../review/service/ReviewVisibilityTest.java       |   65 +
 .../sample/DemoDataSeedRunnerTest.java             |   84 ++
 .../student/StudentProfileWebIntegrationTest.java  |   21 +-
 .../student/service/StudentAccountCrudTest.java    |  124 ++
 .../student/service/StudentLookupServiceTest.java  |    1 +
 .../student/service/StudentProfileServiceTest.java |    3 +-
 .../vehicle/VehicleWebIntegrationTest.java         |   37 +-
 .../vehicle/config/VehicleConfigTest.java          |    1 +
 .../drivingschool/vehicle/model/VehicleTest.java   |    1 +
 .../vehicle/repository/VehicleRepositoryTest.java  |    1 +
 .../vehicle/service/VehicleLookupServiceTest.java  |    1 +
 .../vehicle/service/VehicleMaintenanceTest.java    |   99 ++
 .../vehicle/service/VehicleServiceTest.java        |    1 +
 tailwind.config.js                                 |   31 +
 325 files changed, 19482 insertions(+), 2982 deletions(-)
```

</details>

## 2. Single-owner rules

An existing member header is the first source of ownership. Header-free tests follow the module they test. Shared use does not mean shared Git ownership. A student may explain how they use a shared class without including it in their PR.

Header-free infrastructure/docs/assets are marked **PROPOSED** below. These choices say who should include the file, not who wrote every line. There is no unassigned path, but the team must review the proposed choices. No source header is changed here.

Important boundaries:

- `AdminManagementController.java` is owned by 135 although it calls instructor, package and student services. Other members must not duplicate it in their PRs.
- `auth/model/InstructorAccount.java` keeps its 135 header. Teaching profiles and scheduling stay with 133.
- `PackageEnrollmentReturnTest.java` tests `AuthController.login` and the return session, so 135 owns it. `StudentEnrollmentBalanceTest.java` tests `UserRepository`, so 135 owns it too.
- Review fragments `review-tabs.jspf`, `star-display.jspf` and `star-input.jspf` retain their 137 headers. Other shared fragments keep their listed owner.
- `tailwind.config.js` keeps its **135** header. The npm manifests, `app.css`, `tailwind.css` and `site.js` belong to **136**. They need coordination, not duplicate ownership.
- `docs/TEST_CHECKLIST.md` keeps its existing **132** owner line. `docs/TAILWIND_LICENSE.txt` is assigned to **136**; other current integration documents to **135**.
- Header-free `common` configuration/contact classes are proposed for **135** as shared foundation.
- Retired `admin-dashboard.css`, `profile.css` and `vehicle.css` are proposed for **136**, because their styles were consolidated into the shared UI. The document itself deletes nothing.

| Member | Changed/deleted/new paths, including this plan |
|---|---|
| IT26101135 | 114 |
| IT26101136 | 81 |
| IT26101133 | 44 |
| IT26101132 | 28 |
| IT26101134 | 32 |
| IT26101137 | 31 |
| **Total** | **330** |

### Shared, infrastructure, contracts and assets

These are a view of the full manifest, not extra assignments. Each shared file has one owner, even if every member uses it.

| Exact path | Sole owner | Reason |
|---|---|---|
| `.gitignore` | IT26101135 | PROPOSED: integration/foundation/documentation |
| `CONTRACTS.md` | IT26101135 | PROPOSED: integration/foundation/documentation |
| `HELP.md` | IT26101135 | PROPOSED: integration/foundation/documentation |
| `README.md` | IT26101135 | PROPOSED: integration/foundation/documentation |
| `REPORT.md` | IT26101135 | PROPOSED: integration/foundation/documentation |
| `docs/CRUD_MAP.md` | IT26101135 | PROPOSED: integration/foundation/documentation |
| `docs/GIT_BRANCH_PLAN.md` | IT26101135 | PROPOSED: integration/foundation/documentation |
| `docs/OOP_SOLID_MAP.md` | IT26101135 | PROPOSED: integration/foundation/documentation |
| `docs/TAILWIND_LICENSE.txt` | IT26101136 | PROPOSED: local Tailwind build/licence |
| `docs/TEST_CHECKLIST.md` | IT26101132 | Existing checklist owner line |
| `docs/VIVA_GUIDE.md` | IT26101135 | PROPOSED: integration/foundation/documentation |
| `package-lock.json` | IT26101136 | PROPOSED: local Tailwind build/licence |
| `package.json` | IT26101136 | PROPOSED: local Tailwind build/licence |
| `src/main/java/com/drivingschool/DrivesparkAcademyApplication.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/ServletInitializer.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/annotation/RequireAdmin.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/annotation/RequireLogin.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/config/AdminSeedRunner.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/filter/CsrfFilter.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/interceptor/AccessInterceptor.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/interceptor/NavigationInterceptor.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/model/AccountStatus.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/model/Admin.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/model/InstructorAccount.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/model/Role.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/model/User.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/repository/UserRepository.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/service/AdminSeedService.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/service/AuthService.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/service/AuthSessionService.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/service/CsrfTokenService.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/service/LoginResult.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/service/PasswordManagementService.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/util/AccountWriteCoordinator.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/auth/util/PasswordHasher.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/common/config/CustomErrorViewResolver.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/common/config/WebConfig.java` | IT26101135 | Package/test target; no member header found |
| `src/main/java/com/drivingschool/common/controller/HomeController.java` | IT26101135 | Package/test target; no member header found |
| `src/main/java/com/drivingschool/common/controller/PublicPagesController.java` | IT26101135 | Package/test target; no member header found |
| `src/main/java/com/drivingschool/common/controller/SetupController.java` | IT26101135 | Package/test target; no member header found |
| `src/main/java/com/drivingschool/common/exception/DuplicateRecordException.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/common/exception/ErrorPageSupport.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/common/exception/RecordNotFoundException.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/common/exception/StorageException.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/common/exception/ValidationException.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/common/model/ContactMessage.java` | IT26101135 | Package/test target; no member header found |
| `src/main/java/com/drivingschool/common/model/DashboardSummary.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/common/model/Identifiable.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/common/repository/ContactMessageRepository.java` | IT26101135 | Package/test target; no member header found |
| `src/main/java/com/drivingschool/common/repository/FileRepository.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/common/service/ContactService.java` | IT26101135 | Package/test target; no member header found |
| `src/main/java/com/drivingschool/common/service/DashboardSummaryProvider.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/common/service/DashboardSummaryService.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/common/util/IdGenerator.java` | IT26101135 | Package/test target; no member header found |
| `src/main/java/com/drivingschool/instructor/api/InstructorBookingInfo.java` | IT26101133 | Package/test target; no member header found |
| `src/main/java/com/drivingschool/instructor/api/InstructorLookup.java` | IT26101133 | Package/test target; no member header found |
| `src/main/java/com/drivingschool/learningpackage/api/EnrollmentLookup.java` | IT26101136 | Package/test target; no member header found |
| `src/main/java/com/drivingschool/learningpackage/api/EnrollmentSummary.java` | IT26101136 | Package/test target; no member header found |
| `src/main/java/com/drivingschool/learningpackage/model/LearningRecord.java` | IT26101136 | Existing member header |
| `src/main/java/com/drivingschool/review/api/ReviewStore.java` | IT26101137 | Existing member header |
| `src/main/java/com/drivingschool/student/api/StudentLookup.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/student/api/StudentProfiles.java` | IT26101135 | Existing member header |
| `src/main/java/com/drivingschool/vehicle/api/VehicleBookingInfo.java` | IT26101132 | Existing member header |
| `src/main/java/com/drivingschool/vehicle/api/VehicleLookup.java` | IT26101132 | Existing member header |
| `src/main/java/com/drivingschool/vehicle/api/VehicleOperations.java` | IT26101132 | Existing member header |
| `src/main/java/com/drivingschool/vehicle/api/VehicleStore.java` | IT26101132 | Existing member header |
| `src/main/resources/application.properties` | IT26101135 | PROPOSED: integration/foundation/documentation |
| `src/main/resources/static/css/INTER_LICENSE.txt` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/css/admin-dashboard.css` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/css/app.css` | IT26101136 | Existing member header |
| `src/main/resources/static/css/inter-latin-variable.woff2` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/css/profile.css` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/css/report.css` | IT26101137 | Existing member header |
| `src/main/resources/static/css/tailwind.css` | IT26101136 | Existing member header |
| `src/main/resources/static/css/vehicle.css` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/images/README.md` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/images/cta-drive.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/images/error-road.svg` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/images/favicon.svg` | IT26101136 | Existing member header |
| `src/main/resources/static/images/flexible-bg.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/images/hero-road.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/images/location.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/images/logo.svg` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/images/mission.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/images/promo-lesson.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/images/safety-bg.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/images/vehicle-automatic.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/images/vehicle-manual.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/images/vehicle-motorcycle.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet |
| `src/main/resources/static/js/site.js` | IT26101136 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/components/breadcrumbs.jspf` | IT26101136 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/components/confirm-dialog.jspf` | IT26101136 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/components/empty-state.jspf` | IT26101136 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/components/local-photo.jspf` | IT26101136 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/components/page-header.jspf` | IT26101136 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/components/pagination.jspf` | IT26101136 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/components/review-tabs.jspf` | IT26101137 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/components/search-bar.jspf` | IT26101136 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/components/skeleton.jspf` | IT26101136 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/components/star-display.jspf` | IT26101137 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/components/star-input.jspf` | IT26101137 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/components/status-badge.jspf` | IT26101136 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/components/tabs.jspf` | IT26101136 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/components/toasts.jspf` | IT26101136 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/dashboard-sidebar.jspf` | IT26101136 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/error-403.jsp` | IT26101135 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/error-404.jsp` | IT26101135 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/error-500.jsp` | IT26101135 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/error.jsp` | IT26101136 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/footer.jspf` | IT26101136 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/navbar.jspf` | IT26101136 | Existing member header |
| `src/main/webapp/WEB-INF/views/common/setup-check.jsp` | IT26101136 | Existing member header |
| `src/test/java/com/drivingschool/common/controller/DashboardSummaryServiceTest.java` | IT26101135 | Existing member header |
| `src/test/java/com/drivingschool/common/controller/HomeControllerTest.java` | IT26101135 | Package/test target; no member header found |
| `src/test/java/com/drivingschool/common/exception/AuthorizationErrorHandlerTest.java` | IT26101135 | Existing member header |
| `src/test/java/com/drivingschool/common/exception/ErrorStatusMappingTest.java` | IT26101135 | Existing member header |
| `src/test/java/com/drivingschool/common/exception/GlobalExceptionHandlerTest.java` | IT26101135 | Existing member header |
| `src/test/java/com/drivingschool/common/repository/FileRepositoryTest.java` | IT26101135 | Package/test target; no member header found |
| `src/test/java/com/drivingschool/common/util/IdGeneratorTest.java` | IT26101135 | Package/test target; no member header found |
| `tailwind.config.js` | IT26101135 | Existing member header |

### Complete changed-file manifest

`A`: new relative to cached develop; `M`: modified; `D`: deleted; `U`: currently untracked; `P`: this new plan. Snapshot-needed means its latest content is not in the current source commit. Deleted files still need an owner.

| Status | Exact path | Sole owner | Evidence | Snapshot commit needed |
|---|---|---|---|---|
| M | `.gitignore` | IT26101135 | PROPOSED: integration/foundation/documentation | No |
| M | `CONTRACTS.md` | IT26101135 | PROPOSED: integration/foundation/documentation | No |
| D | `HELP.md` | IT26101135 | PROPOSED: integration/foundation/documentation | No |
| M | `README.md` | IT26101135 | PROPOSED: integration/foundation/documentation | YES |
| A | `REPORT.md` | IT26101135 | PROPOSED: integration/foundation/documentation | No |
| A | `docs/CRUD_MAP.md` | IT26101135 | PROPOSED: integration/foundation/documentation | No |
| P | `docs/GIT_BRANCH_PLAN.md` | IT26101135 | PROPOSED: integration/foundation/documentation | YES |
| A | `docs/OOP_SOLID_MAP.md` | IT26101135 | PROPOSED: integration/foundation/documentation | No |
| A | `docs/TAILWIND_LICENSE.txt` | IT26101136 | PROPOSED: local Tailwind build/licence | No |
| A | `docs/TEST_CHECKLIST.md` | IT26101132 | Existing checklist owner line | No |
| A | `docs/VIVA_GUIDE.md` | IT26101135 | PROPOSED: integration/foundation/documentation | No |
| A | `package-lock.json` | IT26101136 | PROPOSED: local Tailwind build/licence | No |
| A | `package.json` | IT26101136 | PROPOSED: local Tailwind build/licence | No |
| M | `src/main/java/com/drivingschool/DrivesparkAcademyApplication.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/ServletInitializer.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/admin/controller/AdminDashboardController.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/admin/controller/AdminManagementController.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/annotation/RequireAdmin.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/annotation/RequireLogin.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/config/AdminSeedRunner.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/controller/AuthController.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/auth/controller/PasswordController.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/filter/CsrfFilter.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/form/LoginForm.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/form/RegistrationForm.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/interceptor/AccessInterceptor.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/interceptor/NavigationInterceptor.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/auth/model/AccountStatus.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/model/Admin.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/auth/model/InstructorAccount.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/model/Role.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/model/User.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/repository/UserRepository.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/service/AdminSeedService.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/service/AuthService.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/service/AuthSessionService.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/service/CsrfTokenService.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/auth/service/LoginResult.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/auth/service/PasswordManagementService.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/util/AccountWriteCoordinator.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/auth/util/PasswordHasher.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/booking/controller/AdminBookingController.java` | IT26101134 | Existing member header | No |
| A | `src/main/java/com/drivingschool/booking/controller/BookingController.java` | IT26101134 | Existing member header | No |
| M | `src/main/java/com/drivingschool/booking/model/Booking.java` | IT26101134 | Existing member header | No |
| M | `src/main/java/com/drivingschool/booking/model/BookingStatus.java` | IT26101134 | Existing member header | No |
| M | `src/main/java/com/drivingschool/booking/repository/BookingRepository.java` | IT26101134 | Existing member header | No |
| A | `src/main/java/com/drivingschool/booking/service/AdminBookingOperations.java` | IT26101134 | Existing member header | No |
| A | `src/main/java/com/drivingschool/booking/service/BookingConflictChecker.java` | IT26101134 | Existing member header | No |
| A | `src/main/java/com/drivingschool/booking/service/BookingService.java` | IT26101134 | Existing member header | No |
| A | `src/main/java/com/drivingschool/booking/service/PaidEnrollmentGuard.java` | IT26101134 | Existing member header | No |
| A | `src/main/java/com/drivingschool/booking/service/SlotAvailabilityService.java` | IT26101134 | Existing member header | No |
| A | `src/main/java/com/drivingschool/booking/service/StudentBookingOperations.java` | IT26101134 | Existing member header | No |
| A | `src/main/java/com/drivingschool/booking/util/BookingOrder.java` | IT26101134 | Existing member header | No |
| A | `src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java` | IT26101134 | Existing member header | No |
| M | `src/main/java/com/drivingschool/booking/util/LessonIntervalUtils.java` | IT26101134 | Existing member header | No |
| A | `src/main/java/com/drivingschool/booking/util/LessonTimeOrder.java` | IT26101134 | Existing member header | No |
| A | `src/main/java/com/drivingschool/booking/util/PendingRequestOrder.java` | IT26101134 | Existing member header | No |
| A | `src/main/java/com/drivingschool/common/config/CustomErrorViewResolver.java` | IT26101135 | Existing member header | YES |
| M | `src/main/java/com/drivingschool/common/config/WebConfig.java` | IT26101135 | Package/test target; no member header found | No |
| M | `src/main/java/com/drivingschool/common/controller/HomeController.java` | IT26101135 | Package/test target; no member header found | No |
| A | `src/main/java/com/drivingschool/common/controller/PublicPagesController.java` | IT26101135 | Package/test target; no member header found | No |
| M | `src/main/java/com/drivingschool/common/controller/SetupController.java` | IT26101135 | Package/test target; no member header found | No |
| M | `src/main/java/com/drivingschool/common/exception/DuplicateRecordException.java` | IT26101135 | Existing member header | No |
| U | `src/main/java/com/drivingschool/common/exception/ErrorPageSupport.java` | IT26101135 | Existing member header | YES |
| M | `src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java` | IT26101135 | Existing member header | YES |
| M | `src/main/java/com/drivingschool/common/exception/RecordNotFoundException.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/common/exception/StorageException.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/common/exception/ValidationException.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/common/model/ContactMessage.java` | IT26101135 | Package/test target; no member header found | No |
| A | `src/main/java/com/drivingschool/common/model/DashboardSummary.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/common/model/Identifiable.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/common/repository/ContactMessageRepository.java` | IT26101135 | Package/test target; no member header found | No |
| M | `src/main/java/com/drivingschool/common/repository/FileRepository.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/common/service/ContactService.java` | IT26101135 | Package/test target; no member header found | No |
| A | `src/main/java/com/drivingschool/common/service/DashboardSummaryProvider.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/common/service/DashboardSummaryService.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/common/util/IdGenerator.java` | IT26101135 | Package/test target; no member header found | No |
| D | `src/main/java/com/drivingschool/instructor/api/InstructorBookingInfo.java` | IT26101133 | Package/test target; no member header found | No |
| D | `src/main/java/com/drivingschool/instructor/api/InstructorLookup.java` | IT26101133 | Package/test target; no member header found | No |
| M | `src/main/java/com/drivingschool/instructor/config/InstructorConfig.java` | IT26101133 | Existing member header | No |
| A | `src/main/java/com/drivingschool/instructor/controller/InstructorAvailabilityController.java` | IT26101133 | Existing member header | No |
| A | `src/main/java/com/drivingschool/instructor/controller/InstructorDashboardController.java` | IT26101133 | Existing member header | No |
| A | `src/main/java/com/drivingschool/instructor/controller/PublicInstructorController.java` | IT26101133 | Existing member header | No |
| A | `src/main/java/com/drivingschool/instructor/model/Availability.java` | IT26101133 | Existing member header | No |
| M | `src/main/java/com/drivingschool/instructor/model/Instructor.java` | IT26101133 | Existing member header | No |
| D | `src/main/java/com/drivingschool/instructor/model/InstructorAvailability.java` | IT26101133 | Package/test target; no member header found | No |
| A | `src/main/java/com/drivingschool/instructor/model/InstructorLeave.java` | IT26101133 | Existing member header | No |
| A | `src/main/java/com/drivingschool/instructor/model/LessonRecord.java` | IT26101133 | Existing member header | No |
| A | `src/main/java/com/drivingschool/instructor/repository/AvailabilityRepository.java` | IT26101133 | Existing member header | No |
| D | `src/main/java/com/drivingschool/instructor/repository/InstructorAvailabilityRepository.java` | IT26101133 | Package/test target; no member header found | No |
| A | `src/main/java/com/drivingschool/instructor/repository/InstructorLeaveRepository.java` | IT26101133 | Existing member header | No |
| M | `src/main/java/com/drivingschool/instructor/repository/InstructorRepository.java` | IT26101133 | Existing member header | No |
| A | `src/main/java/com/drivingschool/instructor/repository/LessonRecordRepository.java` | IT26101133 | Existing member header | No |
| A | `src/main/java/com/drivingschool/instructor/service/InstructorAccountService.java` | IT26101133 | Existing member header | No |
| D | `src/main/java/com/drivingschool/instructor/service/InstructorAvailabilityService.java` | IT26101133 | Package/test target; no member header found | No |
| A | `src/main/java/com/drivingschool/instructor/service/InstructorCatalog.java` | IT26101133 | Existing member header | No |
| A | `src/main/java/com/drivingschool/instructor/service/InstructorExperienceOrder.java` | IT26101133 | Existing member header | No |
| A | `src/main/java/com/drivingschool/instructor/service/InstructorLessonService.java` | IT26101133 | Existing member header | No |
| D | `src/main/java/com/drivingschool/instructor/service/InstructorLookupService.java` | IT26101133 | Package/test target; no member header found | No |
| A | `src/main/java/com/drivingschool/instructor/service/InstructorNameOrder.java` | IT26101133 | Existing member header | No |
| A | `src/main/java/com/drivingschool/instructor/service/InstructorOrder.java` | IT26101133 | Existing member header | No |
| A | `src/main/java/com/drivingschool/instructor/service/InstructorRatingOrder.java` | IT26101133 | Existing member header | No |
| A | `src/main/java/com/drivingschool/instructor/service/InstructorScheduleOperations.java` | IT26101133 | Existing member header | No |
| A | `src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java` | IT26101133 | Existing member header | No |
| M | `src/main/java/com/drivingschool/instructor/service/InstructorService.java` | IT26101133 | Existing member header | No |
| M | `src/main/java/com/drivingschool/instructor/service/InstructorSorter.java` | IT26101133 | Existing member header | No |
| D | `src/main/java/com/drivingschool/learningpackage/api/EnrollmentLookup.java` | IT26101136 | Package/test target; no member header found | No |
| D | `src/main/java/com/drivingschool/learningpackage/api/EnrollmentSummary.java` | IT26101136 | Package/test target; no member header found | No |
| M | `src/main/java/com/drivingschool/learningpackage/config/LearningPackageConfig.java` | IT26101136 | Existing member header | No |
| A | `src/main/java/com/drivingschool/learningpackage/controller/AdminEnrollmentController.java` | IT26101136 | Existing member header | No |
| A | `src/main/java/com/drivingschool/learningpackage/controller/EnrollmentController.java` | IT26101136 | Existing member header | No |
| A | `src/main/java/com/drivingschool/learningpackage/controller/LearningPackageController.java` | IT26101136 | Existing member header | No |
| U | `src/main/java/com/drivingschool/learningpackage/controller/LocalPhotoAdvice.java` | IT26101136 | Existing member header | YES |
| A | `src/main/java/com/drivingschool/learningpackage/controller/PackageEnrollmentEntryController.java` | IT26101136 | Existing member header | No |
| A | `src/main/java/com/drivingschool/learningpackage/controller/PackageNavigationAdvice.java` | IT26101136 | Existing member header | No |
| A | `src/main/java/com/drivingschool/learningpackage/model/Enrollment.java` | IT26101136 | Existing member header | No |
| M | `src/main/java/com/drivingschool/learningpackage/model/LearningPackage.java` | IT26101136 | Existing member header | No |
| A | `src/main/java/com/drivingschool/learningpackage/model/LearningRecord.java` | IT26101136 | Existing member header | No |
| A | `src/main/java/com/drivingschool/learningpackage/model/PackageNavigationCard.java` | IT26101136 | Existing member header | No |
| A | `src/main/java/com/drivingschool/learningpackage/model/PackageType.java` | IT26101136 | Existing member header | No |
| A | `src/main/java/com/drivingschool/learningpackage/repository/EnrollmentRepository.java` | IT26101136 | Existing member header | No |
| M | `src/main/java/com/drivingschool/learningpackage/repository/LearningPackageRepository.java` | IT26101136 | Existing member header | No |
| A | `src/main/java/com/drivingschool/learningpackage/service/EnrollmentAdministration.java` | IT26101136 | Existing member header | No |
| A | `src/main/java/com/drivingschool/learningpackage/service/EnrollmentOperations.java` | IT26101136 | Existing member header | No |
| A | `src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java` | IT26101136 | Existing member header | No |
| A | `src/main/java/com/drivingschool/learningpackage/service/LearningPackageCatalog.java` | IT26101136 | Existing member header | No |
| M | `src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java` | IT26101136 | Existing member header | No |
| A | `src/main/java/com/drivingschool/report/controller/AdminReportController.java` | IT26101137 | Existing member header | No |
| A | `src/main/java/com/drivingschool/report/service/AdminReportService.java` | IT26101137 | Existing member header | No |
| A | `src/main/java/com/drivingschool/report/service/ReportCatalog.java` | IT26101137 | Existing member header | No |
| A | `src/main/java/com/drivingschool/report/service/ReportRow.java` | IT26101137 | Existing member header | No |
| A | `src/main/java/com/drivingschool/report/service/ReportSection.java` | IT26101137 | Existing member header | No |
| A | `src/main/java/com/drivingschool/review/api/ReviewStore.java` | IT26101137 | Existing member header | No |
| A | `src/main/java/com/drivingschool/review/controller/AdminReviewController.java` | IT26101137 | Existing member header | No |
| A | `src/main/java/com/drivingschool/review/controller/PublicReviewController.java` | IT26101137 | Existing member header | No |
| A | `src/main/java/com/drivingschool/review/controller/ReviewController.java` | IT26101137 | Existing member header | No |
| M | `src/main/java/com/drivingschool/review/model/Review.java` | IT26101137 | Existing member header | No |
| M | `src/main/java/com/drivingschool/review/model/ReviewStatus.java` | IT26101137 | Existing member header | No |
| M | `src/main/java/com/drivingschool/review/repository/ReviewRepository.java` | IT26101137 | Existing member header | No |
| A | `src/main/java/com/drivingschool/review/service/ReviewModeration.java` | IT26101137 | Existing member header | No |
| A | `src/main/java/com/drivingschool/review/service/ReviewOperations.java` | IT26101137 | Existing member header | No |
| M | `src/main/java/com/drivingschool/review/service/ReviewService.java` | IT26101137 | Existing member header | No |
| A | `src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java` | IT26101132 | Existing member header | No |
| M | `src/main/java/com/drivingschool/student/api/StudentLookup.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/student/api/StudentProfiles.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/student/controller/StudentAccountController.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/student/controller/StudentPasswordController.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/student/controller/StudentProfileController.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/student/controller/StudentProgressController.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/student/dto/StudentProfile.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/student/dto/StudentProgress.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/student/dto/StudentSummary.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/student/form/StudentProfileForm.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/student/model/Student.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/student/service/StudentAccountService.java` | IT26101135 | Existing member header | No |
| A | `src/main/java/com/drivingschool/student/service/StudentAdministrationService.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/student/service/StudentLookupService.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/student/service/StudentProfileService.java` | IT26101135 | Existing member header | No |
| M | `src/main/java/com/drivingschool/vehicle/api/VehicleBookingInfo.java` | IT26101132 | Existing member header | No |
| M | `src/main/java/com/drivingschool/vehicle/api/VehicleLookup.java` | IT26101132 | Existing member header | No |
| A | `src/main/java/com/drivingschool/vehicle/api/VehicleOperations.java` | IT26101132 | Existing member header | No |
| A | `src/main/java/com/drivingschool/vehicle/api/VehicleStore.java` | IT26101132 | Existing member header | No |
| M | `src/main/java/com/drivingschool/vehicle/config/VehicleConfig.java` | IT26101132 | Existing member header | No |
| M | `src/main/java/com/drivingschool/vehicle/controller/VehicleController.java` | IT26101132 | Existing member header | No |
| M | `src/main/java/com/drivingschool/vehicle/form/VehicleCreateForm.java` | IT26101132 | Existing member header | No |
| M | `src/main/java/com/drivingschool/vehicle/form/VehicleEditForm.java` | IT26101132 | Existing member header | No |
| M | `src/main/java/com/drivingschool/vehicle/model/TransmissionType.java` | IT26101132 | Existing member header | No |
| M | `src/main/java/com/drivingschool/vehicle/model/Vehicle.java` | IT26101132 | Existing member header | No |
| M | `src/main/java/com/drivingschool/vehicle/model/VehicleCategory.java` | IT26101132 | Existing member header | No |
| M | `src/main/java/com/drivingschool/vehicle/model/VehicleOperationalStatus.java` | IT26101132 | Existing member header | No |
| M | `src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java` | IT26101132 | Existing member header | No |
| M | `src/main/java/com/drivingschool/vehicle/service/VehicleLookupService.java` | IT26101132 | Existing member header | No |
| M | `src/main/java/com/drivingschool/vehicle/service/VehicleService.java` | IT26101132 | Existing member header | No |
| M | `src/main/resources/application.properties` | IT26101135 | PROPOSED: integration/foundation/documentation | No |
| A | `src/main/resources/static/css/INTER_LICENSE.txt` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | No |
| D | `src/main/resources/static/css/admin-dashboard.css` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | No |
| M | `src/main/resources/static/css/app.css` | IT26101136 | Existing member header | YES |
| A | `src/main/resources/static/css/inter-latin-variable.woff2` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | No |
| D | `src/main/resources/static/css/profile.css` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | No |
| A | `src/main/resources/static/css/report.css` | IT26101137 | Existing member header | No |
| A | `src/main/resources/static/css/tailwind.css` | IT26101136 | Existing member header | YES |
| D | `src/main/resources/static/css/vehicle.css` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | No |
| A | `src/main/resources/static/images/README.md` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | YES |
| A | `src/main/resources/static/images/cta-drive.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | No |
| A | `src/main/resources/static/images/error-road.svg` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | No |
| A | `src/main/resources/static/images/favicon.svg` | IT26101136 | Existing member header | No |
| A | `src/main/resources/static/images/flexible-bg.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | No |
| A | `src/main/resources/static/images/hero-road.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | No |
| A | `src/main/resources/static/images/location.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | No |
| A | `src/main/resources/static/images/logo.svg` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | No |
| A | `src/main/resources/static/images/mission.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | No |
| A | `src/main/resources/static/images/promo-lesson.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | No |
| A | `src/main/resources/static/images/safety-bg.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | No |
| A | `src/main/resources/static/images/vehicle-automatic.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | No |
| A | `src/main/resources/static/images/vehicle-manual.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | No |
| A | `src/main/resources/static/images/vehicle-motorcycle.webp` | IT26101136 | PROPOSED: shared UI asset or retired stylesheet | No |
| A | `src/main/resources/static/js/site.js` | IT26101136 | Existing member header | YES |
| A | `src/main/webapp/WEB-INF/views/admin/booking-card.jspf` | IT26101134 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/admin/bookings.jsp` | IT26101134 | Existing member header | No |
| M | `src/main/webapp/WEB-INF/views/admin/dashboard.jsp` | IT26101135 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/admin/enrollments.jsp` | IT26101136 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/admin/instructors.jsp` | IT26101133 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/admin/packages.jsp` | IT26101136 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/admin/reports.jsp` | IT26101137 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/admin/reviews.jsp` | IT26101137 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/admin/students.jsp` | IT26101135 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/auth/forgot-password.jsp` | IT26101135 | Existing member header | YES |
| M | `src/main/webapp/WEB-INF/views/auth/login.jsp` | IT26101135 | Existing member header | YES |
| M | `src/main/webapp/WEB-INF/views/auth/register.jsp` | IT26101135 | Existing member header | YES |
| A | `src/main/webapp/WEB-INF/views/booking/list.jsp` | IT26101134 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/booking/request-card.jspf` | IT26101134 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/common/components/breadcrumbs.jspf` | IT26101136 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/common/components/confirm-dialog.jspf` | IT26101136 | Existing member header | YES |
| A | `src/main/webapp/WEB-INF/views/common/components/empty-state.jspf` | IT26101136 | Existing member header | YES |
| U | `src/main/webapp/WEB-INF/views/common/components/local-photo.jspf` | IT26101136 | Existing member header | YES |
| A | `src/main/webapp/WEB-INF/views/common/components/page-header.jspf` | IT26101136 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/common/components/pagination.jspf` | IT26101136 | Existing member header | YES |
| A | `src/main/webapp/WEB-INF/views/common/components/review-tabs.jspf` | IT26101137 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/common/components/search-bar.jspf` | IT26101136 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/common/components/skeleton.jspf` | IT26101136 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/common/components/star-display.jspf` | IT26101137 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/common/components/star-input.jspf` | IT26101137 | Existing member header | YES |
| A | `src/main/webapp/WEB-INF/views/common/components/status-badge.jspf` | IT26101136 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/common/components/tabs.jspf` | IT26101136 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/common/components/toasts.jspf` | IT26101136 | Existing member header | YES |
| A | `src/main/webapp/WEB-INF/views/common/dashboard-sidebar.jspf` | IT26101136 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/common/error-403.jsp` | IT26101135 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/common/error-404.jsp` | IT26101135 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/common/error-500.jsp` | IT26101135 | Existing member header | No |
| M | `src/main/webapp/WEB-INF/views/common/error.jsp` | IT26101136 | Existing member header | YES |
| M | `src/main/webapp/WEB-INF/views/common/footer.jspf` | IT26101136 | Existing member header | YES |
| M | `src/main/webapp/WEB-INF/views/common/navbar.jspf` | IT26101136 | Existing member header | YES |
| M | `src/main/webapp/WEB-INF/views/common/setup-check.jsp` | IT26101136 | Existing member header | YES |
| M | `src/main/webapp/WEB-INF/views/home/index.jsp` | IT26101135 | Existing member header | YES |
| A | `src/main/webapp/WEB-INF/views/instructor/availability.jsp` | IT26101133 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/instructor/lesson-dashboard.jsp` | IT26101133 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/learningpackage/detail.jsp` | IT26101136 | Existing member header | YES |
| A | `src/main/webapp/WEB-INF/views/learningpackage/list.jsp` | IT26101136 | Existing member header | YES |
| A | `src/main/webapp/WEB-INF/views/public/about.jsp` | IT26101136 | Existing member header | YES |
| A | `src/main/webapp/WEB-INF/views/public/contact.jsp` | IT26101136 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/public/faq.jsp` | IT26101136 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/public/instructors.jsp` | IT26101133 | Existing member header | YES |
| A | `src/main/webapp/WEB-INF/views/public/reviews.jsp` | IT26101137 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/review/list.jsp` | IT26101137 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/student/enrollments.jsp` | IT26101136 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/student/password.jsp` | IT26101135 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/student/payment-instructions.jsp` | IT26101136 | Existing member header | No |
| M | `src/main/webapp/WEB-INF/views/student/profile-edit.jsp` | IT26101135 | Existing member header | No |
| M | `src/main/webapp/WEB-INF/views/student/profile.jsp` | IT26101135 | Existing member header | No |
| A | `src/main/webapp/WEB-INF/views/student/progress.jsp` | IT26101135 | Existing member header | No |
| M | `src/main/webapp/WEB-INF/views/vehicle/create.jsp` | IT26101132 | Existing member header | No |
| M | `src/main/webapp/WEB-INF/views/vehicle/edit.jsp` | IT26101132 | Existing member header | YES |
| M | `src/main/webapp/WEB-INF/views/vehicle/list.jsp` | IT26101132 | Existing member header | No |
| M | `src/test/java/com/drivingschool/admin/AdminDashboardWebIntegrationTest.java` | IT26101135 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/auth/AuthWebIntegrationTest.java` | IT26101135 | Existing member header | No |
| M | `src/test/java/com/drivingschool/auth/config/AdminSeedRunnerTest.java` | IT26101135 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/auth/controller/AuthControllerTest.java` | IT26101135 | Package/test target; no member header found | No |
| A | `src/test/java/com/drivingschool/auth/controller/PackageEnrollmentReturnTest.java` | IT26101135 | Existing member header | No |
| M | `src/test/java/com/drivingschool/auth/filter/CsrfFilterTest.java` | IT26101135 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/auth/interceptor/AccessInterceptorTest.java` | IT26101135 | Existing member header | YES |
| M | `src/test/java/com/drivingschool/auth/interceptor/NavigationInterceptorTest.java` | IT26101135 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/auth/model/UserModelTest.java` | IT26101135 | Package/test target; no member header found | No |
| A | `src/test/java/com/drivingschool/auth/repository/StudentEnrollmentBalanceTest.java` | IT26101135 | Existing member header | No |
| M | `src/test/java/com/drivingschool/auth/repository/UserRepositoryTest.java` | IT26101135 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/auth/service/AdminSeedServiceTest.java` | IT26101135 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/auth/service/AuthServiceTest.java` | IT26101135 | Existing member header | No |
| M | `src/test/java/com/drivingschool/auth/service/AuthSessionServiceTest.java` | IT26101135 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/auth/service/CsrfTokenServiceTest.java` | IT26101135 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/auth/util/PasswordHasherTest.java` | IT26101135 | Package/test target; no member header found | No |
| A | `src/test/java/com/drivingschool/booking/SchedulingWebIntegrationTest.java` | IT26101134 | Existing member header | No |
| M | `src/test/java/com/drivingschool/booking/model/BookingTest.java` | IT26101134 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/booking/repository/BookingRepositorySpringIntegrationTest.java` | IT26101134 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/booking/repository/BookingRepositoryTest.java` | IT26101134 | Package/test target; no member header found | No |
| A | `src/test/java/com/drivingschool/booking/service/BookingConflictCheckerTest.java` | IT26101134 | Existing member header | No |
| A | `src/test/java/com/drivingschool/booking/service/BookingCutoffTest.java` | IT26101134 | Existing member header | No |
| A | `src/test/java/com/drivingschool/booking/service/BookingServiceCrudTest.java` | IT26101134 | Existing member header | No |
| A | `src/test/java/com/drivingschool/booking/service/SlotAvailabilityServiceTest.java` | IT26101134 | Package/test target; no member header found | No |
| A | `src/test/java/com/drivingschool/booking/service/SlotBoundaryTest.java` | IT26101134 | Existing member header | No |
| A | `src/test/java/com/drivingschool/booking/util/BookingOrderTest.java` | IT26101134 | Existing member header | No |
| A | `src/test/java/com/drivingschool/booking/util/BookingRequestQueueTest.java` | IT26101134 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/booking/util/LessonIntervalUtilsTest.java` | IT26101134 | Package/test target; no member header found | No |
| A | `src/test/java/com/drivingschool/common/controller/DashboardSummaryServiceTest.java` | IT26101135 | Existing member header | No |
| M | `src/test/java/com/drivingschool/common/controller/HomeControllerTest.java` | IT26101135 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/common/exception/AuthorizationErrorHandlerTest.java` | IT26101135 | Existing member header | YES |
| U | `src/test/java/com/drivingschool/common/exception/ErrorStatusMappingTest.java` | IT26101135 | Existing member header | YES |
| M | `src/test/java/com/drivingschool/common/exception/GlobalExceptionHandlerTest.java` | IT26101135 | Existing member header | YES |
| M | `src/test/java/com/drivingschool/common/repository/FileRepositoryTest.java` | IT26101135 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/common/util/IdGeneratorTest.java` | IT26101135 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/instructor/config/InstructorConfigTest.java` | IT26101133 | Package/test target; no member header found | No |
| D | `src/test/java/com/drivingschool/instructor/model/InstructorAvailabilityTest.java` | IT26101133 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/instructor/model/InstructorTest.java` | IT26101133 | Package/test target; no member header found | No |
| D | `src/test/java/com/drivingschool/instructor/repository/InstructorAvailabilityRepositoryTest.java` | IT26101133 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/instructor/repository/InstructorRepositoryTest.java` | IT26101133 | Package/test target; no member header found | No |
| A | `src/test/java/com/drivingschool/instructor/service/InstructorAccountServiceTest.java` | IT26101133 | Existing member header | No |
| D | `src/test/java/com/drivingschool/instructor/service/InstructorAvailabilityServiceTest.java` | IT26101133 | Package/test target; no member header found | No |
| D | `src/test/java/com/drivingschool/instructor/service/InstructorLookupServiceTest.java` | IT26101133 | Package/test target; no member header found | No |
| A | `src/test/java/com/drivingschool/instructor/service/InstructorScheduleServiceTest.java` | IT26101133 | Existing member header | No |
| M | `src/test/java/com/drivingschool/instructor/service/InstructorServiceTest.java` | IT26101133 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/instructor/service/InstructorSorterTest.java` | IT26101133 | Package/test target; no member header found | No |
| A | `src/test/java/com/drivingschool/learningpackage/AdminEnrollmentWebIntegrationTest.java` | IT26101136 | Existing member header | No |
| A | `src/test/java/com/drivingschool/learningpackage/EnrollmentWebIntegrationTest.java` | IT26101136 | Existing member header | No |
| M | `src/test/java/com/drivingschool/learningpackage/config/LearningPackageConfigTest.java` | IT26101136 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/learningpackage/model/LearningPackageTest.java` | IT26101136 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/learningpackage/repository/LearningPackageRepositoryTest.java` | IT26101136 | Package/test target; no member header found | No |
| A | `src/test/java/com/drivingschool/learningpackage/service/EnrollmentPaymentServiceTest.java` | IT26101136 | Existing member header | No |
| A | `src/test/java/com/drivingschool/learningpackage/service/EnrollmentServiceTest.java` | IT26101136 | Existing member header | No |
| A | `src/test/java/com/drivingschool/learningpackage/service/LearningPackageLifecycleTest.java` | IT26101136 | Existing member header | No |
| M | `src/test/java/com/drivingschool/learningpackage/service/LearningPackageServiceTest.java` | IT26101136 | Package/test target; no member header found | No |
| A | `src/test/java/com/drivingschool/learningpackage/service/PackageNavigationAdviceTest.java` | IT26101136 | Existing member header | No |
| A | `src/test/java/com/drivingschool/review/ReviewWebIntegrationTest.java` | IT26101137 | Existing member header | No |
| M | `src/test/java/com/drivingschool/review/model/ReviewTest.java` | IT26101137 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/review/repository/ReviewRepositoryTest.java` | IT26101137 | Package/test target; no member header found | No |
| A | `src/test/java/com/drivingschool/review/service/AdminReportServiceTest.java` | IT26101137 | Existing member header | No |
| A | `src/test/java/com/drivingschool/review/service/InstructorRatingRecalculationTest.java` | IT26101137 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/review/service/ReviewServiceConcurrencyTest.java` | IT26101137 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/review/service/ReviewServiceTest.java` | IT26101137 | Package/test target; no member header found | No |
| A | `src/test/java/com/drivingschool/review/service/ReviewVisibilityTest.java` | IT26101137 | Existing member header | No |
| A | `src/test/java/com/drivingschool/sample/DemoDataSeedRunnerTest.java` | IT26101132 | Existing member header | No |
| M | `src/test/java/com/drivingschool/student/StudentProfileWebIntegrationTest.java` | IT26101135 | Package/test target; no member header found | No |
| A | `src/test/java/com/drivingschool/student/service/StudentAccountCrudTest.java` | IT26101135 | Existing member header | No |
| M | `src/test/java/com/drivingschool/student/service/StudentLookupServiceTest.java` | IT26101135 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/student/service/StudentProfileServiceTest.java` | IT26101135 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/vehicle/VehicleWebIntegrationTest.java` | IT26101132 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/vehicle/config/VehicleConfigTest.java` | IT26101132 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/vehicle/model/VehicleTest.java` | IT26101132 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/vehicle/repository/VehicleRepositoryTest.java` | IT26101132 | Package/test target; no member header found | No |
| M | `src/test/java/com/drivingschool/vehicle/service/VehicleLookupServiceTest.java` | IT26101132 | Package/test target; no member header found | No |
| A | `src/test/java/com/drivingschool/vehicle/service/VehicleMaintenanceTest.java` | IT26101132 | Existing member header | No |
| M | `src/test/java/com/drivingschool/vehicle/service/VehicleServiceTest.java` | IT26101132 | Package/test target; no member header found | No |
| A | `tailwind.config.js` | IT26101135 | Existing member header | No |

## 3. Dependencies and merge order

A dependency means a module imports or calls another module. It does not give it ownership of that module's files.

| Member | Current dependencies | Actual source evidence |
|---|---|---|
| IT26101135 | Enrollment, booking, instructor, vehicle; student progress also reads review data | `UserRepository` imports new `Enrollment` and `EnrollmentRepository`; `DashboardSummaryService` combines module repositories |
| IT26101136 | Authentication/student accounts and booking | `EnrollmentService` imports `AccountStatus`, `UserRepository`, `AuthSessionService` and `BookingRepository` |
| IT26101133 | Authentication, booking, student and review | `InstructorLessonService` uses `StudentProgress`; schedule/account services use bookings/users |
| IT26101132 | Common storage, auth and booking; seed runner also imports all teaching/enrollment/review modules | `VehicleService` reads `BookingRepository`; `DemoDataSeedRunner` creates records from multiple modules |
| IT26101134 | Auth, student, enrollment, instructor and vehicle | `BookingService`, `PaidEnrollmentGuard` and `BookingConflictChecker` check these records |
| IT26101137 | Auth, student, booking, instructor, enrollment and vehicle | `ReviewService` reads completed bookings/instructors; `AdminReportService` reads enrollments for Paid-only revenue |

### There is no green whole-module six-PR order yet

The cached baseline lacks the files marked A in the manifest. In particular:

1. The 135-owned `UserRepository` imports the 136-owned **new** `Enrollment` and `EnrollmentRepository`.
2. The 136-owned `EnrollmentService` imports the 135-owned **new** `AccountStatus`.
3. Therefore **135 needs 136, and 136 needs 135**. Whichever complete bundle goes first has a missing dependency. A list saying “foundation first” cannot solve that cycle.

Other standalone blockers include `InstructorLessonService` needing new `StudentProgress` (135), booking needing new `InstructorAccount` (135), the seed runner needing new enrollment/availability types, and reports needing new `EnrollmentRepository` (136).

| Whole-module PR | Compiles alone on cached develop? | Current merge gate |
|---|---|---|
| IT26101135-finalUI_changes, lead-only copy | No: new enrollment classes are absent | Blocked |
| IT26101136-finalUI_changes | No: new authentication classes are absent | Blocked |
| IT26101133-finalUI_changes | No: new StudentProgress is absent | Blocked |
| IT26101132-finalUI_changes | No: seed runner imports new types from other modules | Blocked |
| IT26101134-finalUI_changes | No: new account/enrollment dependencies are absent | Blocked |
| IT26101137-finalUI_changes | No: new enrollment repository is absent | Blocked |

This is a source-level missing-dependency finding. Six isolated branch builds were not run, and no independent build pass is claimed.

**Recommended actual merge order:** complete source snapshot → ONE integration PR into develop → full build/tests and six-owner review → lead release PR/merge from develop into main.

If six PRs are required, agree on a different delivery plan first: smaller dependency-first PRs per owner or a joint bootstrap/integration PR followed by smaller changes. That needs a separate file-level build audit and may need compatibility changes. No bootstrap refactor is performed here. Do not merge failing branches or silently copy someone else's files to hide a missing dependency.

For **reading/review only**, use 135 → 136 → 133 → 132 → 134 → 137. This is **not** a passing merge sequence. There is no valid green order for the six complete bundles under the current constraints.

The manifest makes the Git path sets disjoint, so planned PRs should not edit the same file. There can still be conflicts if develop changes after this audit, or someone copies broad folders. The lead integration controllers, navbar/components, shared docs and generated CSS are the main coordination points. Tailwind scans everyone's JSPs: a later JSP merge can regenerate `tailwind.css`. Only 136 stages that file; other members report the generated difference to 136.

### Required check after every real merge into develop

Stop the running demo app with Ctrl+C in its own terminal first: a running WAR can lock the output. Do not kill unrelated Java processes. Use temporary data for browser checks.

```powershell
git checkout develop
git pull --ff-only origin develop
npm ci
if ($LASTEXITCODE -ne 0) { throw "npm ci failed" }
npm run build:css
if ($LASTEXITCODE -ne 0) { throw "CSS build failed" }
.\mvnw.cmd clean verify
if ($LASTEXITCODE -ne 0) { throw "Build failed; do not merge or push" }
git status --short
```

Review any generated CSS difference with 136. Record the real test summary in the PR. Existing local Surefire reports contain 623 tests, zero failures/errors/skips; that is an earlier integrated baseline, **not a new Phase A run or a split-branch result**. This docs-only phase did not rebuild or restart the app.

Every eventual member PR must run `npm run build:css` and `.\mvnw.cmd clean verify` after merging, plus its three-click check below. A missing npm manifest on an early partial branch is a failed prerequisite, not a reason to skip the CSS check.

## 4. Save the complete source snapshot manually

### 4A. The uncommitted work must be saved first

Run this on the existing integration branch. Review every path. The list includes the four untracked source/test files and this new document. Copying a branch commit never includes dirty edits automatically.

Check for real secrets or personal data. Demo passwords exist deliberately in fixtures; they must not be real account passwords. If the team's rule literally forbids every password string, those demo fixtures need separately approved changes; this document does not edit them.

Stop the demo, then run the builds. Stop on failure:

```powershell
npm run build:css
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
.\mvnw.cmd clean verify
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
```

Stage only the reviewed current edits and inspect them:

```powershell
git status --short --branch
$pendingSourcePaths = @(
    "README.md"
    "docs/GIT_BRANCH_PLAN.md"
    "src/main/java/com/drivingschool/common/config/CustomErrorViewResolver.java"
    "src/main/java/com/drivingschool/common/exception/ErrorPageSupport.java"
    "src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java"
    "src/main/java/com/drivingschool/learningpackage/controller/LocalPhotoAdvice.java"
    "src/main/resources/static/css/app.css"
    "src/main/resources/static/css/tailwind.css"
    "src/main/resources/static/images/README.md"
    "src/main/resources/static/js/site.js"
    "src/main/webapp/WEB-INF/views/auth/forgot-password.jsp"
    "src/main/webapp/WEB-INF/views/auth/login.jsp"
    "src/main/webapp/WEB-INF/views/auth/register.jsp"
    "src/main/webapp/WEB-INF/views/common/components/confirm-dialog.jspf"
    "src/main/webapp/WEB-INF/views/common/components/empty-state.jspf"
    "src/main/webapp/WEB-INF/views/common/components/local-photo.jspf"
    "src/main/webapp/WEB-INF/views/common/components/pagination.jspf"
    "src/main/webapp/WEB-INF/views/common/components/star-input.jspf"
    "src/main/webapp/WEB-INF/views/common/components/toasts.jspf"
    "src/main/webapp/WEB-INF/views/common/error.jsp"
    "src/main/webapp/WEB-INF/views/common/footer.jspf"
    "src/main/webapp/WEB-INF/views/common/navbar.jspf"
    "src/main/webapp/WEB-INF/views/common/setup-check.jsp"
    "src/main/webapp/WEB-INF/views/home/index.jsp"
    "src/main/webapp/WEB-INF/views/learningpackage/detail.jsp"
    "src/main/webapp/WEB-INF/views/learningpackage/list.jsp"
    "src/main/webapp/WEB-INF/views/public/about.jsp"
    "src/main/webapp/WEB-INF/views/public/instructors.jsp"
    "src/main/webapp/WEB-INF/views/vehicle/edit.jsp"
    "src/test/java/com/drivingschool/auth/interceptor/AccessInterceptorTest.java"
    "src/test/java/com/drivingschool/common/exception/AuthorizationErrorHandlerTest.java"
    "src/test/java/com/drivingschool/common/exception/ErrorStatusMappingTest.java"
    "src/test/java/com/drivingschool/common/exception/GlobalExceptionHandlerTest.java"
)
git add -- $pendingSourcePaths
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git diff --cached --stat
git diff --cached
```

Only after that review:

```powershell
git commit -m "Complete integration UI and add team branch plan"
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git push origin IT26101135-finalUI_changes
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
```

If there is nothing to commit, inspect status instead of making an empty commit. If edits are added later, freeze them and update the plan before copying.

### 4B. Recommended: keep the integration branch and open one PR

On GitHub choose base **develop**, compare **IT26101135-finalUI_changes**. Ask all six owners to review their manifest rows. Merge only after the complete build, tests and temporary-data role checks pass.

**Title:** Integrate DriveSpark modules, manual payments and local premium UI

**Description:** Student accounts, instructor scheduling, vehicles, Paid-enrollment booking checks, the linked-node FIFO, package enrollment, review moderation and reports work together. The UI uses local assets and shared Tailwind styles. Attach the real test summary, screenshots, six-owner reviews and remaining UNVERIFIED checks. Payments are manual: the system sends no email and uploads no receipts.

This gives viva ownership without pretending there are six independently buildable module PRs.

### 4C. Six-branch option only: the lead branch name is occupied

**Do not run this section for the recommended integration PR.** The existing local and remote source branch already uses `IT26101135-finalUI_changes`. A new lead-only branch cannot use that name until it is freed. A normal push cannot safely replace the old remote history with an unrelated develop-based branch. Do not force push it.

After committing the complete source snapshot, publish a preserved source ref:

```powershell
git branch integration/finalUI-source-20261109 IT26101135-finalUI_changes
git push origin integration/finalUI-source-20261109
```

This is a proposed new archive ref, not one already verified to exist. Stop if it exists with different content. Verify on GitHub that it contains the complete snapshot and this plan.

The repository administrator must then **rename the old remote branch to `archive/IT26101135-finalUI_changes-20261109` using GitHub's branch rename action**. Coordinate this first, check open PRs/protection, and preserve the history. Do not delete or force-overwrite it. If rename permission is unavailable, use the integration option instead.

On the lead's machine, after the remote rename:

```powershell
git fetch origin
git branch -m IT26101135-finalUI_changes archive/IT26101135-finalUI_changes-20261109
git branch --set-upstream-to=origin/archive/IT26101135-finalUI_changes-20261109 archive/IT26101135-finalUI_changes-20261109
```

The member recipes use **`origin/integration/finalUI-source-20261109`** as their source. Once the lead-only branch reuses the old name, copying from the old name would copy the wrong tree. This safe preserved ref deliberately replaces the ambiguous source name in the requested commands.

If local develop does not exist, create it once:

```powershell
git fetch origin
git checkout -b develop origin/develop
```

Otherwise use `git checkout develop`. Check all target member names locally and on GitHub. If one is occupied, stop and coordinate; never delete an existing branch to make a recipe work. Keep the plan open from GitHub/the archive: older develop may not yet contain it.

## 5. Exact member copy recipes — build gate is currently blocked

These paths are exact and quoted for PowerShell. No folder wildcard accidentally takes another member's file. `git checkout SOURCE -- PATHS` copies **and stages** them; inspect the staged diff immediately.

Use Windows PowerShell with Java 17 selected in JAVA_HOME, Node/npm available, and the Maven wrapper. Confirm the Java version with `.\mvnw.cmd -v` before building. Do not copy a JDK path from another student's machine.

Each recipe assumes a clean working tree, a local develop branch, a complete preserved source snapshot and a free target branch name. The lead-name archive step above is mandatory for the six-way option. These are manual recipes for the team, not commands executed by the agent.

With the current complete-bundle dependencies, the guards are expected to stop at a build failure. **Do not remove the guards to push/merge a broken PR.** The copy lists can support a later agreed split, but do not solve the dependency cycle. Do not run all six blocks automatically in one worktree.

### IT26101135 — `IT26101135-finalUI_changes`

```powershell
$pendingWork = git status --porcelain
if ($pendingWork) { throw "Working tree is not clean. Save/review it first." }
git fetch origin
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git checkout develop
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git pull --ff-only origin develop
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git checkout -b IT26101135-finalUI_changes
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
$planSource = "origin/integration/finalUI-source-20261109"
$memberPaths = @(
    ".gitignore"
    "CONTRACTS.md"
    "README.md"
    "REPORT.md"
    "docs/CRUD_MAP.md"
    "docs/GIT_BRANCH_PLAN.md"
    "docs/OOP_SOLID_MAP.md"
    "docs/VIVA_GUIDE.md"
    "src/main/java/com/drivingschool/DrivesparkAcademyApplication.java"
    "src/main/java/com/drivingschool/ServletInitializer.java"
    "src/main/java/com/drivingschool/admin/controller/AdminDashboardController.java"
    "src/main/java/com/drivingschool/admin/controller/AdminManagementController.java"
    "src/main/java/com/drivingschool/auth/annotation/RequireAdmin.java"
    "src/main/java/com/drivingschool/auth/annotation/RequireLogin.java"
    "src/main/java/com/drivingschool/auth/config/AdminSeedRunner.java"
    "src/main/java/com/drivingschool/auth/controller/AuthController.java"
    "src/main/java/com/drivingschool/auth/controller/PasswordController.java"
    "src/main/java/com/drivingschool/auth/filter/CsrfFilter.java"
    "src/main/java/com/drivingschool/auth/form/LoginForm.java"
    "src/main/java/com/drivingschool/auth/form/RegistrationForm.java"
    "src/main/java/com/drivingschool/auth/interceptor/AccessInterceptor.java"
    "src/main/java/com/drivingschool/auth/interceptor/NavigationInterceptor.java"
    "src/main/java/com/drivingschool/auth/model/AccountStatus.java"
    "src/main/java/com/drivingschool/auth/model/Admin.java"
    "src/main/java/com/drivingschool/auth/model/InstructorAccount.java"
    "src/main/java/com/drivingschool/auth/model/Role.java"
    "src/main/java/com/drivingschool/auth/model/User.java"
    "src/main/java/com/drivingschool/auth/repository/UserRepository.java"
    "src/main/java/com/drivingschool/auth/service/AdminSeedService.java"
    "src/main/java/com/drivingschool/auth/service/AuthService.java"
    "src/main/java/com/drivingschool/auth/service/AuthSessionService.java"
    "src/main/java/com/drivingschool/auth/service/CsrfTokenService.java"
    "src/main/java/com/drivingschool/auth/service/LoginResult.java"
    "src/main/java/com/drivingschool/auth/service/PasswordManagementService.java"
    "src/main/java/com/drivingschool/auth/util/AccountWriteCoordinator.java"
    "src/main/java/com/drivingschool/auth/util/PasswordHasher.java"
    "src/main/java/com/drivingschool/common/config/CustomErrorViewResolver.java"
    "src/main/java/com/drivingschool/common/config/WebConfig.java"
    "src/main/java/com/drivingschool/common/controller/HomeController.java"
    "src/main/java/com/drivingschool/common/controller/PublicPagesController.java"
    "src/main/java/com/drivingschool/common/controller/SetupController.java"
    "src/main/java/com/drivingschool/common/exception/DuplicateRecordException.java"
    "src/main/java/com/drivingschool/common/exception/ErrorPageSupport.java"
    "src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java"
    "src/main/java/com/drivingschool/common/exception/RecordNotFoundException.java"
    "src/main/java/com/drivingschool/common/exception/StorageException.java"
    "src/main/java/com/drivingschool/common/exception/ValidationException.java"
    "src/main/java/com/drivingschool/common/model/ContactMessage.java"
    "src/main/java/com/drivingschool/common/model/DashboardSummary.java"
    "src/main/java/com/drivingschool/common/model/Identifiable.java"
    "src/main/java/com/drivingschool/common/repository/ContactMessageRepository.java"
    "src/main/java/com/drivingschool/common/repository/FileRepository.java"
    "src/main/java/com/drivingschool/common/service/ContactService.java"
    "src/main/java/com/drivingschool/common/service/DashboardSummaryProvider.java"
    "src/main/java/com/drivingschool/common/service/DashboardSummaryService.java"
    "src/main/java/com/drivingschool/common/util/IdGenerator.java"
    "src/main/java/com/drivingschool/student/api/StudentLookup.java"
    "src/main/java/com/drivingschool/student/api/StudentProfiles.java"
    "src/main/java/com/drivingschool/student/controller/StudentAccountController.java"
    "src/main/java/com/drivingschool/student/controller/StudentPasswordController.java"
    "src/main/java/com/drivingschool/student/controller/StudentProfileController.java"
    "src/main/java/com/drivingschool/student/controller/StudentProgressController.java"
    "src/main/java/com/drivingschool/student/dto/StudentProfile.java"
    "src/main/java/com/drivingschool/student/dto/StudentProgress.java"
    "src/main/java/com/drivingschool/student/dto/StudentSummary.java"
    "src/main/java/com/drivingschool/student/form/StudentProfileForm.java"
    "src/main/java/com/drivingschool/student/model/Student.java"
    "src/main/java/com/drivingschool/student/service/StudentAccountService.java"
    "src/main/java/com/drivingschool/student/service/StudentAdministrationService.java"
    "src/main/java/com/drivingschool/student/service/StudentLookupService.java"
    "src/main/java/com/drivingschool/student/service/StudentProfileService.java"
    "src/main/resources/application.properties"
    "src/main/webapp/WEB-INF/views/admin/dashboard.jsp"
    "src/main/webapp/WEB-INF/views/admin/students.jsp"
    "src/main/webapp/WEB-INF/views/auth/forgot-password.jsp"
    "src/main/webapp/WEB-INF/views/auth/login.jsp"
    "src/main/webapp/WEB-INF/views/auth/register.jsp"
    "src/main/webapp/WEB-INF/views/common/error-403.jsp"
    "src/main/webapp/WEB-INF/views/common/error-404.jsp"
    "src/main/webapp/WEB-INF/views/common/error-500.jsp"
    "src/main/webapp/WEB-INF/views/home/index.jsp"
    "src/main/webapp/WEB-INF/views/student/password.jsp"
    "src/main/webapp/WEB-INF/views/student/profile-edit.jsp"
    "src/main/webapp/WEB-INF/views/student/profile.jsp"
    "src/main/webapp/WEB-INF/views/student/progress.jsp"
    "src/test/java/com/drivingschool/admin/AdminDashboardWebIntegrationTest.java"
    "src/test/java/com/drivingschool/auth/AuthWebIntegrationTest.java"
    "src/test/java/com/drivingschool/auth/config/AdminSeedRunnerTest.java"
    "src/test/java/com/drivingschool/auth/controller/AuthControllerTest.java"
    "src/test/java/com/drivingschool/auth/controller/PackageEnrollmentReturnTest.java"
    "src/test/java/com/drivingschool/auth/filter/CsrfFilterTest.java"
    "src/test/java/com/drivingschool/auth/interceptor/AccessInterceptorTest.java"
    "src/test/java/com/drivingschool/auth/interceptor/NavigationInterceptorTest.java"
    "src/test/java/com/drivingschool/auth/model/UserModelTest.java"
    "src/test/java/com/drivingschool/auth/repository/StudentEnrollmentBalanceTest.java"
    "src/test/java/com/drivingschool/auth/repository/UserRepositoryTest.java"
    "src/test/java/com/drivingschool/auth/service/AdminSeedServiceTest.java"
    "src/test/java/com/drivingschool/auth/service/AuthServiceTest.java"
    "src/test/java/com/drivingschool/auth/service/AuthSessionServiceTest.java"
    "src/test/java/com/drivingschool/auth/service/CsrfTokenServiceTest.java"
    "src/test/java/com/drivingschool/auth/util/PasswordHasherTest.java"
    "src/test/java/com/drivingschool/common/controller/DashboardSummaryServiceTest.java"
    "src/test/java/com/drivingschool/common/controller/HomeControllerTest.java"
    "src/test/java/com/drivingschool/common/exception/AuthorizationErrorHandlerTest.java"
    "src/test/java/com/drivingschool/common/exception/ErrorStatusMappingTest.java"
    "src/test/java/com/drivingschool/common/exception/GlobalExceptionHandlerTest.java"
    "src/test/java/com/drivingschool/common/repository/FileRepositoryTest.java"
    "src/test/java/com/drivingschool/common/util/IdGeneratorTest.java"
    "src/test/java/com/drivingschool/student/StudentProfileWebIntegrationTest.java"
    "src/test/java/com/drivingschool/student/service/StudentAccountCrudTest.java"
    "src/test/java/com/drivingschool/student/service/StudentLookupServiceTest.java"
    "src/test/java/com/drivingschool/student/service/StudentProfileServiceTest.java"
    "tailwind.config.js"
)
git checkout $planSource -- $memberPaths
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
$memberDeletes = @(
    "HELP.md"
)
git rm --ignore-unmatch -- $memberDeletes
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git status
git diff --cached --name-status
git diff --cached
# Stop the demo before building. Do not continue on a failed prerequisite.
npm run build:css
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
.\mvnw.cmd clean verify
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git add -- $memberPaths
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git status
git diff --cached --stat
$outOfScopeChanges = git diff --name-only
if ($outOfScopeChanges) { throw "Unstaged/generated changes remain. Coordinate with their owner before committing." }
git commit -m "Finish authentication, student accounts and shared foundation"
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git push -u origin IT26101135-finalUI_changes
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
```

Then use GitHub New pull request: base `develop`, compare `IT26101135-finalUI_changes`. Use Draft while review is pending. A failed build must be fixed before continuing with commit/push; no isolated build pass is claimed. Deleted paths are handled by the exact git rm list. If there is no change to commit, inspect whether develop already contains it instead of making an empty PR.

### IT26101136 — `IT26101136-finalUI_changes`

```powershell
$pendingWork = git status --porcelain
if ($pendingWork) { throw "Working tree is not clean. Save/review it first." }
git fetch origin
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git checkout develop
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git pull --ff-only origin develop
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git checkout -b IT26101136-finalUI_changes
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
$planSource = "origin/integration/finalUI-source-20261109"
$memberPaths = @(
    "docs/TAILWIND_LICENSE.txt"
    "package-lock.json"
    "package.json"
    "src/main/java/com/drivingschool/learningpackage/config/LearningPackageConfig.java"
    "src/main/java/com/drivingschool/learningpackage/controller/AdminEnrollmentController.java"
    "src/main/java/com/drivingschool/learningpackage/controller/EnrollmentController.java"
    "src/main/java/com/drivingschool/learningpackage/controller/LearningPackageController.java"
    "src/main/java/com/drivingschool/learningpackage/controller/LocalPhotoAdvice.java"
    "src/main/java/com/drivingschool/learningpackage/controller/PackageEnrollmentEntryController.java"
    "src/main/java/com/drivingschool/learningpackage/controller/PackageNavigationAdvice.java"
    "src/main/java/com/drivingschool/learningpackage/model/Enrollment.java"
    "src/main/java/com/drivingschool/learningpackage/model/LearningPackage.java"
    "src/main/java/com/drivingschool/learningpackage/model/LearningRecord.java"
    "src/main/java/com/drivingschool/learningpackage/model/PackageNavigationCard.java"
    "src/main/java/com/drivingschool/learningpackage/model/PackageType.java"
    "src/main/java/com/drivingschool/learningpackage/repository/EnrollmentRepository.java"
    "src/main/java/com/drivingschool/learningpackage/repository/LearningPackageRepository.java"
    "src/main/java/com/drivingschool/learningpackage/service/EnrollmentAdministration.java"
    "src/main/java/com/drivingschool/learningpackage/service/EnrollmentOperations.java"
    "src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java"
    "src/main/java/com/drivingschool/learningpackage/service/LearningPackageCatalog.java"
    "src/main/java/com/drivingschool/learningpackage/service/LearningPackageService.java"
    "src/main/resources/static/css/INTER_LICENSE.txt"
    "src/main/resources/static/css/app.css"
    "src/main/resources/static/css/inter-latin-variable.woff2"
    "src/main/resources/static/css/tailwind.css"
    "src/main/resources/static/images/README.md"
    "src/main/resources/static/images/cta-drive.webp"
    "src/main/resources/static/images/error-road.svg"
    "src/main/resources/static/images/favicon.svg"
    "src/main/resources/static/images/flexible-bg.webp"
    "src/main/resources/static/images/hero-road.webp"
    "src/main/resources/static/images/location.webp"
    "src/main/resources/static/images/logo.svg"
    "src/main/resources/static/images/mission.webp"
    "src/main/resources/static/images/promo-lesson.webp"
    "src/main/resources/static/images/safety-bg.webp"
    "src/main/resources/static/images/vehicle-automatic.webp"
    "src/main/resources/static/images/vehicle-manual.webp"
    "src/main/resources/static/images/vehicle-motorcycle.webp"
    "src/main/resources/static/js/site.js"
    "src/main/webapp/WEB-INF/views/admin/enrollments.jsp"
    "src/main/webapp/WEB-INF/views/admin/packages.jsp"
    "src/main/webapp/WEB-INF/views/common/components/breadcrumbs.jspf"
    "src/main/webapp/WEB-INF/views/common/components/confirm-dialog.jspf"
    "src/main/webapp/WEB-INF/views/common/components/empty-state.jspf"
    "src/main/webapp/WEB-INF/views/common/components/local-photo.jspf"
    "src/main/webapp/WEB-INF/views/common/components/page-header.jspf"
    "src/main/webapp/WEB-INF/views/common/components/pagination.jspf"
    "src/main/webapp/WEB-INF/views/common/components/search-bar.jspf"
    "src/main/webapp/WEB-INF/views/common/components/skeleton.jspf"
    "src/main/webapp/WEB-INF/views/common/components/status-badge.jspf"
    "src/main/webapp/WEB-INF/views/common/components/tabs.jspf"
    "src/main/webapp/WEB-INF/views/common/components/toasts.jspf"
    "src/main/webapp/WEB-INF/views/common/dashboard-sidebar.jspf"
    "src/main/webapp/WEB-INF/views/common/error.jsp"
    "src/main/webapp/WEB-INF/views/common/footer.jspf"
    "src/main/webapp/WEB-INF/views/common/navbar.jspf"
    "src/main/webapp/WEB-INF/views/common/setup-check.jsp"
    "src/main/webapp/WEB-INF/views/learningpackage/detail.jsp"
    "src/main/webapp/WEB-INF/views/learningpackage/list.jsp"
    "src/main/webapp/WEB-INF/views/public/about.jsp"
    "src/main/webapp/WEB-INF/views/public/contact.jsp"
    "src/main/webapp/WEB-INF/views/public/faq.jsp"
    "src/main/webapp/WEB-INF/views/student/enrollments.jsp"
    "src/main/webapp/WEB-INF/views/student/payment-instructions.jsp"
    "src/test/java/com/drivingschool/learningpackage/AdminEnrollmentWebIntegrationTest.java"
    "src/test/java/com/drivingschool/learningpackage/EnrollmentWebIntegrationTest.java"
    "src/test/java/com/drivingschool/learningpackage/config/LearningPackageConfigTest.java"
    "src/test/java/com/drivingschool/learningpackage/model/LearningPackageTest.java"
    "src/test/java/com/drivingschool/learningpackage/repository/LearningPackageRepositoryTest.java"
    "src/test/java/com/drivingschool/learningpackage/service/EnrollmentPaymentServiceTest.java"
    "src/test/java/com/drivingschool/learningpackage/service/EnrollmentServiceTest.java"
    "src/test/java/com/drivingschool/learningpackage/service/LearningPackageLifecycleTest.java"
    "src/test/java/com/drivingschool/learningpackage/service/LearningPackageServiceTest.java"
    "src/test/java/com/drivingschool/learningpackage/service/PackageNavigationAdviceTest.java"
)
git checkout $planSource -- $memberPaths
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
$memberDeletes = @(
    "src/main/java/com/drivingschool/learningpackage/api/EnrollmentLookup.java"
    "src/main/java/com/drivingschool/learningpackage/api/EnrollmentSummary.java"
    "src/main/resources/static/css/admin-dashboard.css"
    "src/main/resources/static/css/profile.css"
    "src/main/resources/static/css/vehicle.css"
)
git rm --ignore-unmatch -- $memberDeletes
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git status
git diff --cached --name-status
git diff --cached
# Stop the demo before building. Do not continue on a failed prerequisite.
npm run build:css
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
.\mvnw.cmd clean verify
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git add -- $memberPaths
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git status
git diff --cached --stat
$outOfScopeChanges = git diff --name-only
if ($outOfScopeChanges) { throw "Unstaged/generated changes remain. Coordinate with their owner before committing." }
git commit -m "Finish packages, manual payments and shared UI"
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git push -u origin IT26101136-finalUI_changes
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
```

Then use GitHub New pull request: base `develop`, compare `IT26101136-finalUI_changes`. Use Draft while review is pending. A failed build must be fixed before continuing with commit/push; no isolated build pass is claimed. Deleted paths are handled by the exact git rm list. If there is no change to commit, inspect whether develop already contains it instead of making an empty PR.

### IT26101133 — `IT26101133-finalUI_changes`

```powershell
$pendingWork = git status --porcelain
if ($pendingWork) { throw "Working tree is not clean. Save/review it first." }
git fetch origin
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git checkout develop
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git pull --ff-only origin develop
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git checkout -b IT26101133-finalUI_changes
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
$planSource = "origin/integration/finalUI-source-20261109"
$memberPaths = @(
    "src/main/java/com/drivingschool/instructor/config/InstructorConfig.java"
    "src/main/java/com/drivingschool/instructor/controller/InstructorAvailabilityController.java"
    "src/main/java/com/drivingschool/instructor/controller/InstructorDashboardController.java"
    "src/main/java/com/drivingschool/instructor/controller/PublicInstructorController.java"
    "src/main/java/com/drivingschool/instructor/model/Availability.java"
    "src/main/java/com/drivingschool/instructor/model/Instructor.java"
    "src/main/java/com/drivingschool/instructor/model/InstructorLeave.java"
    "src/main/java/com/drivingschool/instructor/model/LessonRecord.java"
    "src/main/java/com/drivingschool/instructor/repository/AvailabilityRepository.java"
    "src/main/java/com/drivingschool/instructor/repository/InstructorLeaveRepository.java"
    "src/main/java/com/drivingschool/instructor/repository/InstructorRepository.java"
    "src/main/java/com/drivingschool/instructor/repository/LessonRecordRepository.java"
    "src/main/java/com/drivingschool/instructor/service/InstructorAccountService.java"
    "src/main/java/com/drivingschool/instructor/service/InstructorCatalog.java"
    "src/main/java/com/drivingschool/instructor/service/InstructorExperienceOrder.java"
    "src/main/java/com/drivingschool/instructor/service/InstructorLessonService.java"
    "src/main/java/com/drivingschool/instructor/service/InstructorNameOrder.java"
    "src/main/java/com/drivingschool/instructor/service/InstructorOrder.java"
    "src/main/java/com/drivingschool/instructor/service/InstructorRatingOrder.java"
    "src/main/java/com/drivingschool/instructor/service/InstructorScheduleOperations.java"
    "src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java"
    "src/main/java/com/drivingschool/instructor/service/InstructorService.java"
    "src/main/java/com/drivingschool/instructor/service/InstructorSorter.java"
    "src/main/webapp/WEB-INF/views/admin/instructors.jsp"
    "src/main/webapp/WEB-INF/views/instructor/availability.jsp"
    "src/main/webapp/WEB-INF/views/instructor/lesson-dashboard.jsp"
    "src/main/webapp/WEB-INF/views/public/instructors.jsp"
    "src/test/java/com/drivingschool/instructor/config/InstructorConfigTest.java"
    "src/test/java/com/drivingschool/instructor/model/InstructorTest.java"
    "src/test/java/com/drivingschool/instructor/repository/InstructorRepositoryTest.java"
    "src/test/java/com/drivingschool/instructor/service/InstructorAccountServiceTest.java"
    "src/test/java/com/drivingschool/instructor/service/InstructorScheduleServiceTest.java"
    "src/test/java/com/drivingschool/instructor/service/InstructorServiceTest.java"
    "src/test/java/com/drivingschool/instructor/service/InstructorSorterTest.java"
)
git checkout $planSource -- $memberPaths
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
$memberDeletes = @(
    "src/main/java/com/drivingschool/instructor/api/InstructorBookingInfo.java"
    "src/main/java/com/drivingschool/instructor/api/InstructorLookup.java"
    "src/main/java/com/drivingschool/instructor/model/InstructorAvailability.java"
    "src/main/java/com/drivingschool/instructor/repository/InstructorAvailabilityRepository.java"
    "src/main/java/com/drivingschool/instructor/service/InstructorAvailabilityService.java"
    "src/main/java/com/drivingschool/instructor/service/InstructorLookupService.java"
    "src/test/java/com/drivingschool/instructor/model/InstructorAvailabilityTest.java"
    "src/test/java/com/drivingschool/instructor/repository/InstructorAvailabilityRepositoryTest.java"
    "src/test/java/com/drivingschool/instructor/service/InstructorAvailabilityServiceTest.java"
    "src/test/java/com/drivingschool/instructor/service/InstructorLookupServiceTest.java"
)
git rm --ignore-unmatch -- $memberDeletes
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git status
git diff --cached --name-status
git diff --cached
# Stop the demo before building. Do not continue on a failed prerequisite.
npm run build:css
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
.\mvnw.cmd clean verify
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git add -- $memberPaths
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git status
git diff --cached --stat
$outOfScopeChanges = git diff --name-only
if ($outOfScopeChanges) { throw "Unstaged/generated changes remain. Coordinate with their owner before committing." }
git commit -m "Finish instructor profiles, availability and bubble sort"
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git push -u origin IT26101133-finalUI_changes
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
```

Then use GitHub New pull request: base `develop`, compare `IT26101133-finalUI_changes`. Use Draft while review is pending. A failed build must be fixed before continuing with commit/push; no isolated build pass is claimed. Deleted paths are handled by the exact git rm list. If there is no change to commit, inspect whether develop already contains it instead of making an empty PR.

### IT26101132 — `IT26101132-finalUI_changes`

```powershell
$pendingWork = git status --porcelain
if ($pendingWork) { throw "Working tree is not clean. Save/review it first." }
git fetch origin
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git checkout develop
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git pull --ff-only origin develop
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git checkout -b IT26101132-finalUI_changes
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
$planSource = "origin/integration/finalUI-source-20261109"
$memberPaths = @(
    "docs/TEST_CHECKLIST.md"
    "src/main/java/com/drivingschool/sample/DemoDataSeedRunner.java"
    "src/main/java/com/drivingschool/vehicle/api/VehicleBookingInfo.java"
    "src/main/java/com/drivingschool/vehicle/api/VehicleLookup.java"
    "src/main/java/com/drivingschool/vehicle/api/VehicleOperations.java"
    "src/main/java/com/drivingschool/vehicle/api/VehicleStore.java"
    "src/main/java/com/drivingschool/vehicle/config/VehicleConfig.java"
    "src/main/java/com/drivingschool/vehicle/controller/VehicleController.java"
    "src/main/java/com/drivingschool/vehicle/form/VehicleCreateForm.java"
    "src/main/java/com/drivingschool/vehicle/form/VehicleEditForm.java"
    "src/main/java/com/drivingschool/vehicle/model/TransmissionType.java"
    "src/main/java/com/drivingschool/vehicle/model/Vehicle.java"
    "src/main/java/com/drivingschool/vehicle/model/VehicleCategory.java"
    "src/main/java/com/drivingschool/vehicle/model/VehicleOperationalStatus.java"
    "src/main/java/com/drivingschool/vehicle/repository/VehicleRepository.java"
    "src/main/java/com/drivingschool/vehicle/service/VehicleLookupService.java"
    "src/main/java/com/drivingschool/vehicle/service/VehicleService.java"
    "src/main/webapp/WEB-INF/views/vehicle/create.jsp"
    "src/main/webapp/WEB-INF/views/vehicle/edit.jsp"
    "src/main/webapp/WEB-INF/views/vehicle/list.jsp"
    "src/test/java/com/drivingschool/sample/DemoDataSeedRunnerTest.java"
    "src/test/java/com/drivingschool/vehicle/VehicleWebIntegrationTest.java"
    "src/test/java/com/drivingschool/vehicle/config/VehicleConfigTest.java"
    "src/test/java/com/drivingschool/vehicle/model/VehicleTest.java"
    "src/test/java/com/drivingschool/vehicle/repository/VehicleRepositoryTest.java"
    "src/test/java/com/drivingschool/vehicle/service/VehicleLookupServiceTest.java"
    "src/test/java/com/drivingschool/vehicle/service/VehicleMaintenanceTest.java"
    "src/test/java/com/drivingschool/vehicle/service/VehicleServiceTest.java"
)
git checkout $planSource -- $memberPaths
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git status
git diff --cached --name-status
git diff --cached
# Stop the demo before building. Do not continue on a failed prerequisite.
npm run build:css
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
.\mvnw.cmd clean verify
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git add -- $memberPaths
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git status
git diff --cached --stat
$outOfScopeChanges = git diff --name-only
if ($outOfScopeChanges) { throw "Unstaged/generated changes remain. Coordinate with their owner before committing." }
git commit -m "Finish vehicles, expiry checks and repeat-safe demo data"
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git push -u origin IT26101132-finalUI_changes
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
```

Then use GitHub New pull request: base `develop`, compare `IT26101132-finalUI_changes`. Use Draft while review is pending. A failed build must be fixed before continuing with commit/push; no isolated build pass is claimed. Deleted paths are handled by the exact git rm list. If there is no change to commit, inspect whether develop already contains it instead of making an empty PR.

### IT26101134 — `IT26101134-finalUI_changes`

```powershell
$pendingWork = git status --porcelain
if ($pendingWork) { throw "Working tree is not clean. Save/review it first." }
git fetch origin
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git checkout develop
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git pull --ff-only origin develop
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git checkout -b IT26101134-finalUI_changes
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
$planSource = "origin/integration/finalUI-source-20261109"
$memberPaths = @(
    "src/main/java/com/drivingschool/booking/controller/AdminBookingController.java"
    "src/main/java/com/drivingschool/booking/controller/BookingController.java"
    "src/main/java/com/drivingschool/booking/model/Booking.java"
    "src/main/java/com/drivingschool/booking/model/BookingStatus.java"
    "src/main/java/com/drivingschool/booking/repository/BookingRepository.java"
    "src/main/java/com/drivingschool/booking/service/AdminBookingOperations.java"
    "src/main/java/com/drivingschool/booking/service/BookingConflictChecker.java"
    "src/main/java/com/drivingschool/booking/service/BookingService.java"
    "src/main/java/com/drivingschool/booking/service/PaidEnrollmentGuard.java"
    "src/main/java/com/drivingschool/booking/service/SlotAvailabilityService.java"
    "src/main/java/com/drivingschool/booking/service/StudentBookingOperations.java"
    "src/main/java/com/drivingschool/booking/util/BookingOrder.java"
    "src/main/java/com/drivingschool/booking/util/BookingRequestQueue.java"
    "src/main/java/com/drivingschool/booking/util/LessonIntervalUtils.java"
    "src/main/java/com/drivingschool/booking/util/LessonTimeOrder.java"
    "src/main/java/com/drivingschool/booking/util/PendingRequestOrder.java"
    "src/main/webapp/WEB-INF/views/admin/booking-card.jspf"
    "src/main/webapp/WEB-INF/views/admin/bookings.jsp"
    "src/main/webapp/WEB-INF/views/booking/list.jsp"
    "src/main/webapp/WEB-INF/views/booking/request-card.jspf"
    "src/test/java/com/drivingschool/booking/SchedulingWebIntegrationTest.java"
    "src/test/java/com/drivingschool/booking/model/BookingTest.java"
    "src/test/java/com/drivingschool/booking/repository/BookingRepositorySpringIntegrationTest.java"
    "src/test/java/com/drivingschool/booking/repository/BookingRepositoryTest.java"
    "src/test/java/com/drivingschool/booking/service/BookingConflictCheckerTest.java"
    "src/test/java/com/drivingschool/booking/service/BookingCutoffTest.java"
    "src/test/java/com/drivingschool/booking/service/BookingServiceCrudTest.java"
    "src/test/java/com/drivingschool/booking/service/SlotAvailabilityServiceTest.java"
    "src/test/java/com/drivingschool/booking/service/SlotBoundaryTest.java"
    "src/test/java/com/drivingschool/booking/util/BookingOrderTest.java"
    "src/test/java/com/drivingschool/booking/util/BookingRequestQueueTest.java"
    "src/test/java/com/drivingschool/booking/util/LessonIntervalUtilsTest.java"
)
git checkout $planSource -- $memberPaths
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git status
git diff --cached --name-status
git diff --cached
# Stop the demo before building. Do not continue on a failed prerequisite.
npm run build:css
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
.\mvnw.cmd clean verify
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git add -- $memberPaths
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git status
git diff --cached --stat
$outOfScopeChanges = git diff --name-only
if ($outOfScopeChanges) { throw "Unstaged/generated changes remain. Coordinate with their owner before committing." }
git commit -m "Finish Paid-only bookings, conflict checks and FIFO queue"
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git push -u origin IT26101134-finalUI_changes
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
```

Then use GitHub New pull request: base `develop`, compare `IT26101134-finalUI_changes`. Use Draft while review is pending. A failed build must be fixed before continuing with commit/push; no isolated build pass is claimed. Deleted paths are handled by the exact git rm list. If there is no change to commit, inspect whether develop already contains it instead of making an empty PR.

### IT26101137 — `IT26101137-finalUI_changes`

```powershell
$pendingWork = git status --porcelain
if ($pendingWork) { throw "Working tree is not clean. Save/review it first." }
git fetch origin
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git checkout develop
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git pull --ff-only origin develop
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git checkout -b IT26101137-finalUI_changes
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
$planSource = "origin/integration/finalUI-source-20261109"
$memberPaths = @(
    "src/main/java/com/drivingschool/report/controller/AdminReportController.java"
    "src/main/java/com/drivingschool/report/service/AdminReportService.java"
    "src/main/java/com/drivingschool/report/service/ReportCatalog.java"
    "src/main/java/com/drivingschool/report/service/ReportRow.java"
    "src/main/java/com/drivingschool/report/service/ReportSection.java"
    "src/main/java/com/drivingschool/review/api/ReviewStore.java"
    "src/main/java/com/drivingschool/review/controller/AdminReviewController.java"
    "src/main/java/com/drivingschool/review/controller/PublicReviewController.java"
    "src/main/java/com/drivingschool/review/controller/ReviewController.java"
    "src/main/java/com/drivingschool/review/model/Review.java"
    "src/main/java/com/drivingschool/review/model/ReviewStatus.java"
    "src/main/java/com/drivingschool/review/repository/ReviewRepository.java"
    "src/main/java/com/drivingschool/review/service/ReviewModeration.java"
    "src/main/java/com/drivingschool/review/service/ReviewOperations.java"
    "src/main/java/com/drivingschool/review/service/ReviewService.java"
    "src/main/resources/static/css/report.css"
    "src/main/webapp/WEB-INF/views/admin/reports.jsp"
    "src/main/webapp/WEB-INF/views/admin/reviews.jsp"
    "src/main/webapp/WEB-INF/views/common/components/review-tabs.jspf"
    "src/main/webapp/WEB-INF/views/common/components/star-display.jspf"
    "src/main/webapp/WEB-INF/views/common/components/star-input.jspf"
    "src/main/webapp/WEB-INF/views/public/reviews.jsp"
    "src/main/webapp/WEB-INF/views/review/list.jsp"
    "src/test/java/com/drivingschool/review/ReviewWebIntegrationTest.java"
    "src/test/java/com/drivingschool/review/model/ReviewTest.java"
    "src/test/java/com/drivingschool/review/repository/ReviewRepositoryTest.java"
    "src/test/java/com/drivingschool/review/service/AdminReportServiceTest.java"
    "src/test/java/com/drivingschool/review/service/InstructorRatingRecalculationTest.java"
    "src/test/java/com/drivingschool/review/service/ReviewServiceConcurrencyTest.java"
    "src/test/java/com/drivingschool/review/service/ReviewServiceTest.java"
    "src/test/java/com/drivingschool/review/service/ReviewVisibilityTest.java"
)
git checkout $planSource -- $memberPaths
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git status
git diff --cached --name-status
git diff --cached
# Stop the demo before building. Do not continue on a failed prerequisite.
npm run build:css
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
.\mvnw.cmd clean verify
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git add -- $memberPaths
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git status
git diff --cached --stat
$outOfScopeChanges = git diff --name-only
if ($outOfScopeChanges) { throw "Unstaged/generated changes remain. Coordinate with their owner before committing." }
git commit -m "Finish reviews, moderation and Paid-only reports"
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git push -u origin IT26101137-finalUI_changes
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
```

Then use GitHub New pull request: base `develop`, compare `IT26101137-finalUI_changes`. Use Draft while review is pending. A failed build must be fixed before continuing with commit/push; no isolated build pass is claimed. Deleted paths are handled by the exact git rm list. If there is no change to commit, inspect whether develop already contains it instead of making an empty PR.

### Lead shared-files command

The complete 135 recipe already includes these paths. This optional command is for reviewing that subset **inside the lead branch**, not for making a seventh PR or pretending it builds alone. Do not run it on another member's branch.

```powershell
$planSource = "origin/integration/finalUI-source-20261109"
$leadSharedPaths = @(
    ".gitignore"
    "CONTRACTS.md"
    "README.md"
    "REPORT.md"
    "docs/CRUD_MAP.md"
    "docs/GIT_BRANCH_PLAN.md"
    "docs/OOP_SOLID_MAP.md"
    "docs/VIVA_GUIDE.md"
    "src/main/java/com/drivingschool/DrivesparkAcademyApplication.java"
    "src/main/java/com/drivingschool/ServletInitializer.java"
    "src/main/java/com/drivingschool/auth/annotation/RequireAdmin.java"
    "src/main/java/com/drivingschool/auth/annotation/RequireLogin.java"
    "src/main/java/com/drivingschool/auth/config/AdminSeedRunner.java"
    "src/main/java/com/drivingschool/auth/filter/CsrfFilter.java"
    "src/main/java/com/drivingschool/auth/interceptor/AccessInterceptor.java"
    "src/main/java/com/drivingschool/auth/interceptor/NavigationInterceptor.java"
    "src/main/java/com/drivingschool/auth/model/AccountStatus.java"
    "src/main/java/com/drivingschool/auth/model/Admin.java"
    "src/main/java/com/drivingschool/auth/model/InstructorAccount.java"
    "src/main/java/com/drivingschool/auth/model/Role.java"
    "src/main/java/com/drivingschool/auth/model/User.java"
    "src/main/java/com/drivingschool/auth/repository/UserRepository.java"
    "src/main/java/com/drivingschool/auth/service/AdminSeedService.java"
    "src/main/java/com/drivingschool/auth/service/AuthService.java"
    "src/main/java/com/drivingschool/auth/service/AuthSessionService.java"
    "src/main/java/com/drivingschool/auth/service/CsrfTokenService.java"
    "src/main/java/com/drivingschool/auth/service/LoginResult.java"
    "src/main/java/com/drivingschool/auth/service/PasswordManagementService.java"
    "src/main/java/com/drivingschool/auth/util/AccountWriteCoordinator.java"
    "src/main/java/com/drivingschool/auth/util/PasswordHasher.java"
    "src/main/java/com/drivingschool/common/config/CustomErrorViewResolver.java"
    "src/main/java/com/drivingschool/common/config/WebConfig.java"
    "src/main/java/com/drivingschool/common/controller/HomeController.java"
    "src/main/java/com/drivingschool/common/controller/PublicPagesController.java"
    "src/main/java/com/drivingschool/common/controller/SetupController.java"
    "src/main/java/com/drivingschool/common/exception/DuplicateRecordException.java"
    "src/main/java/com/drivingschool/common/exception/ErrorPageSupport.java"
    "src/main/java/com/drivingschool/common/exception/GlobalExceptionHandler.java"
    "src/main/java/com/drivingschool/common/exception/RecordNotFoundException.java"
    "src/main/java/com/drivingschool/common/exception/StorageException.java"
    "src/main/java/com/drivingschool/common/exception/ValidationException.java"
    "src/main/java/com/drivingschool/common/model/ContactMessage.java"
    "src/main/java/com/drivingschool/common/model/DashboardSummary.java"
    "src/main/java/com/drivingschool/common/model/Identifiable.java"
    "src/main/java/com/drivingschool/common/repository/ContactMessageRepository.java"
    "src/main/java/com/drivingschool/common/repository/FileRepository.java"
    "src/main/java/com/drivingschool/common/service/ContactService.java"
    "src/main/java/com/drivingschool/common/service/DashboardSummaryProvider.java"
    "src/main/java/com/drivingschool/common/service/DashboardSummaryService.java"
    "src/main/java/com/drivingschool/common/util/IdGenerator.java"
    "src/main/java/com/drivingschool/student/api/StudentLookup.java"
    "src/main/java/com/drivingschool/student/api/StudentProfiles.java"
    "src/main/resources/application.properties"
    "src/main/webapp/WEB-INF/views/common/error-403.jsp"
    "src/main/webapp/WEB-INF/views/common/error-404.jsp"
    "src/main/webapp/WEB-INF/views/common/error-500.jsp"
    "src/test/java/com/drivingschool/common/controller/DashboardSummaryServiceTest.java"
    "src/test/java/com/drivingschool/common/controller/HomeControllerTest.java"
    "src/test/java/com/drivingschool/common/exception/AuthorizationErrorHandlerTest.java"
    "src/test/java/com/drivingschool/common/exception/ErrorStatusMappingTest.java"
    "src/test/java/com/drivingschool/common/exception/GlobalExceptionHandlerTest.java"
    "src/test/java/com/drivingschool/common/repository/FileRepositoryTest.java"
    "src/test/java/com/drivingschool/common/util/IdGeneratorTest.java"
    "tailwind.config.js"
)
git checkout $planSource -- $leadSharedPaths
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
$leadSharedDeletes = @(
    "HELP.md"
)
git rm --ignore-unmatch -- $leadSharedDeletes
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git add -- $leadSharedPaths
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git status
git diff --cached --stat
```

## 6. PR titles and description templates

Use the manifest for exact scope and paste actual results. These templates describe current code, not extra features to implement. All three-click demos use temporary demo data. CRUD deletions remain guarded by the existing rules; never delete live records for a demo.

Some instructor/package admin CRUD methods live in the lead-owned `AdminManagementController`. Explain that shared route honestly; do not claim the controller file belongs to three people.

### IT26101135 PR template

**Title:** Authentication, student accounts and shared integration foundation

**What changed:** Finish authentication, student accounts and shared foundation. Include only this owner's manifest paths.

**CRUD entity:** Student account/profile.

**OOP/SOLID highlights:** Student extends User; StudentProfileService implements StudentProfiles; controllers receive contracts through constructors. Shared exceptions keep errors friendly. Controller/service/repository jobs stay separate (S); constructor contracts show D. Do not say every class demonstrates every principle.

**Three-click demo:** Sign up → submit a temporary registration → log in and view the profile.

**Tests included:**

- `src/test/java/com/drivingschool/admin/AdminDashboardWebIntegrationTest.java`
- `src/test/java/com/drivingschool/auth/AuthWebIntegrationTest.java`
- `src/test/java/com/drivingschool/auth/config/AdminSeedRunnerTest.java`
- `src/test/java/com/drivingschool/auth/controller/AuthControllerTest.java`
- `src/test/java/com/drivingschool/auth/controller/PackageEnrollmentReturnTest.java`
- `src/test/java/com/drivingschool/auth/filter/CsrfFilterTest.java`
- `src/test/java/com/drivingschool/auth/interceptor/AccessInterceptorTest.java`
- `src/test/java/com/drivingschool/auth/interceptor/NavigationInterceptorTest.java`
- `src/test/java/com/drivingschool/auth/model/UserModelTest.java`
- `src/test/java/com/drivingschool/auth/repository/StudentEnrollmentBalanceTest.java`
- `src/test/java/com/drivingschool/auth/repository/UserRepositoryTest.java`
- `src/test/java/com/drivingschool/auth/service/AdminSeedServiceTest.java`
- `src/test/java/com/drivingschool/auth/service/AuthServiceTest.java`
- `src/test/java/com/drivingschool/auth/service/AuthSessionServiceTest.java`
- `src/test/java/com/drivingschool/auth/service/CsrfTokenServiceTest.java`
- `src/test/java/com/drivingschool/auth/util/PasswordHasherTest.java`
- `src/test/java/com/drivingschool/common/controller/DashboardSummaryServiceTest.java`
- `src/test/java/com/drivingschool/common/controller/HomeControllerTest.java`
- `src/test/java/com/drivingschool/common/exception/AuthorizationErrorHandlerTest.java`
- `src/test/java/com/drivingschool/common/exception/ErrorStatusMappingTest.java`
- `src/test/java/com/drivingschool/common/exception/GlobalExceptionHandlerTest.java`
- `src/test/java/com/drivingschool/common/repository/FileRepositoryTest.java`
- `src/test/java/com/drivingschool/common/util/IdGeneratorTest.java`
- `src/test/java/com/drivingschool/student/StudentProfileWebIntegrationTest.java`
- `src/test/java/com/drivingschool/student/service/StudentAccountCrudTest.java`
- `src/test/java/com/drivingschool/student/service/StudentLookupServiceTest.java`
- `src/test/java/com/drivingschool/student/service/StudentProfileServiceTest.java`

**Validation:** run `npm run build:css` and `.\mvnw.cmd clean verify`, then paste the real Tests run/Failures/Errors/Skipped summary. Individual split-branch builds are **UNVERIFIED**.

**Known limitations:** Account balances read enrollment data. Shared integration controllers need other modules; the source archive must include the latest uncommitted work. The current whole-module split is blocked by Section 3; do not hide this in the PR.

### IT26101136 PR template

**Title:** Learning packages, manual enrollment payments and shared UI

**What changed:** Finish packages, manual payments and shared UI. Include only this owner's manifest paths.

**CRUD entity:** LearningPackage and Enrollment.

**OOP/SOLID highlights:** LearningRecord is an interface extending Identifiable; LearningPackage and Enrollment implement it. Repositories inherit FileRepository; operation/catalog contracts keep controllers small. Controller/service/repository jobs stay separate (S); constructor contracts show D. Do not say every class demonstrates every principle.

**Three-click demo:** Packages → View package → Enroll as a fresh student with no existing awaiting request.

**Tests included:**

- `src/test/java/com/drivingschool/learningpackage/AdminEnrollmentWebIntegrationTest.java`
- `src/test/java/com/drivingschool/learningpackage/EnrollmentWebIntegrationTest.java`
- `src/test/java/com/drivingschool/learningpackage/config/LearningPackageConfigTest.java`
- `src/test/java/com/drivingschool/learningpackage/model/LearningPackageTest.java`
- `src/test/java/com/drivingschool/learningpackage/repository/LearningPackageRepositoryTest.java`
- `src/test/java/com/drivingschool/learningpackage/service/EnrollmentPaymentServiceTest.java`
- `src/test/java/com/drivingschool/learningpackage/service/EnrollmentServiceTest.java`
- `src/test/java/com/drivingschool/learningpackage/service/LearningPackageLifecycleTest.java`
- `src/test/java/com/drivingschool/learningpackage/service/LearningPackageServiceTest.java`
- `src/test/java/com/drivingschool/learningpackage/service/PackageNavigationAdviceTest.java`

**Validation:** run `npm run build:css` and `.\mvnw.cmd clean verify`, then paste the real Tests run/Failures/Errors/Skipped summary. Individual split-branch builds are **UNVERIFIED**.

**Known limitations:** Payments are manual: no email sending or receipt upload. This module needs new account/booking types. Generated CSS depends on all JSPs. The current whole-module split is blocked by Section 3; do not hide this in the PR.

### IT26101133 PR template

**Title:** Instructor profiles, availability and handwritten bubble sort

**What changed:** Finish instructor profiles, availability and bubble sort. Include only this owner's manifest paths.

**CRUD entity:** Instructor profile and availability.

**OOP/SOLID highlights:** InstructorRepository inherits FileRepository. InstructorOrder has name, experience and rating implementations. InstructorSorter.sort uses handwritten neighbour swaps; InstructorScheduleOperations separates web code from rules. Controller/service/repository jobs stay separate (S); constructor contracts show D. Do not say every class demonstrates every principle.

**Three-click demo:** Instructors → select Rating → Sort.

**Tests included:**

- `src/test/java/com/drivingschool/instructor/config/InstructorConfigTest.java`
- `src/test/java/com/drivingschool/instructor/model/InstructorTest.java`
- `src/test/java/com/drivingschool/instructor/repository/InstructorRepositoryTest.java`
- `src/test/java/com/drivingschool/instructor/service/InstructorAccountServiceTest.java`
- `src/test/java/com/drivingschool/instructor/service/InstructorScheduleServiceTest.java`
- `src/test/java/com/drivingschool/instructor/service/InstructorServiceTest.java`
- `src/test/java/com/drivingschool/instructor/service/InstructorSorterTest.java`

**Validation:** run `npm run build:css` and `.\mvnw.cmd clean verify`, then paste the real Tests run/Failures/Errors/Skipped summary. Individual split-branch builds are **UNVERIFIED**.

**Known limitations:** Scheduling and lessons use bookings and student progress. Authentication identity stays in the lead-owned InstructorAccount class. The current whole-module split is blocked by Section 3; do not hide this in the PR.

### IT26101132 PR template

**Title:** Vehicle CRUD, expiry checks and repeat-safe demo data

**What changed:** Finish vehicles, expiry checks and repeat-safe demo data. Include only this owner's manifest paths.

**CRUD entity:** Vehicle.

**OOP/SOLID highlights:** VehicleRepository extends FileRepository and implements VehicleStore. VehicleService implements VehicleOperations; VehicleController receives that small contract. Controller/service/repository jobs stay separate (S); constructor contracts show D. Do not say every class demonstrates every principle.

**Three-click demo:** Admin Dashboard → Vehicles → Add vehicle, using temporary data before saving.

**Tests included:**

- `src/test/java/com/drivingschool/sample/DemoDataSeedRunnerTest.java`
- `src/test/java/com/drivingschool/vehicle/VehicleWebIntegrationTest.java`
- `src/test/java/com/drivingschool/vehicle/config/VehicleConfigTest.java`
- `src/test/java/com/drivingschool/vehicle/model/VehicleTest.java`
- `src/test/java/com/drivingschool/vehicle/repository/VehicleRepositoryTest.java`
- `src/test/java/com/drivingschool/vehicle/service/VehicleLookupServiceTest.java`
- `src/test/java/com/drivingschool/vehicle/service/VehicleMaintenanceTest.java`
- `src/test/java/com/drivingschool/vehicle/service/VehicleServiceTest.java`

**Validation:** run `npm run build:css` and `.\mvnw.cmd clean verify`, then paste the real Tests run/Failures/Errors/Skipped summary. Individual split-branch builds are **UNVERIFIED**.

**Known limitations:** Vehicle rules read bookings. The seed runner imports all modules and must only use temporary demo storage. The current whole-module split is blocked by Section 3; do not hide this in the PR.

### IT26101134 PR template

**Title:** Paid-only bookings, conflict checks and linked-node FIFO

**What changed:** Finish Paid-only bookings, conflict checks and FIFO queue. Include only this owner's manifest paths.

**CRUD entity:** Booking.

**OOP/SOLID highlights:** BookingRepository inherits FileRepository. BookingOrder has lesson-time and pending-request implementations. BookingRequestQueue.enqueue/dequeue use handwritten linked nodes; conflict checking has its own class. Controller/service/repository jobs stay separate (S); constructor contracts show D. Do not say every class demonstrates every principle.

**Three-click demo:** Student Dashboard → Bookings → request an available lesson with a Paid enrollment.

**Tests included:**

- `src/test/java/com/drivingschool/booking/SchedulingWebIntegrationTest.java`
- `src/test/java/com/drivingschool/booking/model/BookingTest.java`
- `src/test/java/com/drivingschool/booking/repository/BookingRepositorySpringIntegrationTest.java`
- `src/test/java/com/drivingschool/booking/repository/BookingRepositoryTest.java`
- `src/test/java/com/drivingschool/booking/service/BookingConflictCheckerTest.java`
- `src/test/java/com/drivingschool/booking/service/BookingCutoffTest.java`
- `src/test/java/com/drivingschool/booking/service/BookingServiceCrudTest.java`
- `src/test/java/com/drivingschool/booking/service/SlotAvailabilityServiceTest.java`
- `src/test/java/com/drivingschool/booking/service/SlotBoundaryTest.java`
- `src/test/java/com/drivingschool/booking/util/BookingOrderTest.java`
- `src/test/java/com/drivingschool/booking/util/BookingRequestQueueTest.java`
- `src/test/java/com/drivingschool/booking/util/LessonIntervalUtilsTest.java`

**Validation:** run `npm run build:css` and `.\mvnw.cmd clean verify`, then paste the real Tests run/Failures/Errors/Skipped summary. Individual split-branch builds are **UNVERIFIED**.

**Known limitations:** Eligibility is enforced on the server. File storage is not a database transaction and locking is inside one app process. The current whole-module split is blocked by Section 3; do not hide this in the PR.

### IT26101137 PR template

**Title:** Review CRUD, moderation, Paid-only revenue and CSV reports

**What changed:** Finish reviews, moderation and Paid-only reports. Include only this owner's manifest paths.

**CRUD entity:** Review.

**OOP/SOLID highlights:** ReviewRepository inherits FileRepository. ReviewOperations and ReviewModeration separate actions. AdminReportService implements ReportCatalog, injected into AdminReportController. Controller/service/repository jobs stay separate (S); constructor contracts show D. Do not say every class demonstrates every principle.

**Three-click demo:** Admin Dashboard → Reviews → Reports; inspect demo review visibility and Paid-only totals.

**Tests included:**

- `src/test/java/com/drivingschool/review/ReviewWebIntegrationTest.java`
- `src/test/java/com/drivingschool/review/model/ReviewTest.java`
- `src/test/java/com/drivingschool/review/repository/ReviewRepositoryTest.java`
- `src/test/java/com/drivingschool/review/service/AdminReportServiceTest.java`
- `src/test/java/com/drivingschool/review/service/InstructorRatingRecalculationTest.java`
- `src/test/java/com/drivingschool/review/service/ReviewServiceConcurrencyTest.java`
- `src/test/java/com/drivingschool/review/service/ReviewServiceTest.java`
- `src/test/java/com/drivingschool/review/service/ReviewVisibilityTest.java`

**Validation:** run `npm run build:css` and `.\mvnw.cmd clean verify`, then paste the real Tests run/Failures/Errors/Skipped summary. Individual split-branch builds are **UNVERIFIED**.

**Known limitations:** Public reviews must be approved. Reports need enrollment/booking/instructor/vehicle data. No new report-filter feature is claimed. The current whole-module split is blocked by Section 3; do not hide this in the PR.

## 7. Before you push — every member

A failed or unavailable check is not a pass.

| Member | Own-file check | Extra check |
|---|---|---|
| IT26101135 | Only 135 rows; shared foundation/docs have one owner | Complete source archive includes the four formerly untracked files; occupied branch name is handled safely |
| IT26101136 | Only 136 rows; no differently owned instructor/review fragments | Generated tailwind.css matches the full JSP build; local font/image licences and credits remain included |
| IT26101133 | Only 133 rows and its exact old instructor removals | Handwritten bubble sort remains; availability/lesson/account tests accompany the code |
| IT26101132 | Only 132 rows, including docs/TEST_CHECKLIST.md | Seed data is repeat-safe and only used in temporary storage |
| IT26101134 | Only 134 rows | Linked-node FIFO and server Paid/conflict checks remain; booking tests are included |
| IT26101137 | Only 137 rows, including the three shared review fragments | Approved-only visibility, Paid-only totals and existing CSV are checked |

For each member:

- [ ] Confirm the branch and `git diff --cached --name-status`. Every staged path is yours.
- [ ] Fetch/pull develop yourself and recheck this plan against the updated base. No force push.
- [ ] Stop the demo before building. `npm run build:css` and `.\mvnw.cmd clean verify` must both pass; record the real summary.
- [ ] Resolve the split dependency blocker. A local build must not rely on another member's untracked/uncommitted source.
- [ ] Do not stage `target/`, `node_modules/`, `data-demo/`, actual `data/` records, uploads, `.env`, local settings or logs. `data/.gitkeep` is intentional.
- [ ] No real passwords, access tokens, keys or personal records. Demonstration credentials must stay clearly demo-only, never personal credentials.
- [ ] Check `src/main/resources/static/css/tailwind.css` is tracked so the app runs without a CSS build. Only 136 includes changes to it; everyone else checks it is already available from develop.
- [ ] Keep headers, comments, CRUD/OOP/SOLID labels, routes, field names, seed loader, security, handwritten sort and FIFO intact.
- [ ] Use temporary data for the three-click demo. Mark anything unseen UNVERIFIED. Never delete/process/suspend live records just to demonstrate code.
- [ ] No unexpected generated CSS change remains. Ask 136 to supply the needed update instead of adding their file under your PR.
- [ ] PR base is develop, description matches the final scope, and another member reviewed it.

The existing `.gitignore` covers `/target/`, `/node_modules/`, `/data-demo/`, `/data/*` except `.gitkeep`, local environment files and logs. It is not edited here. A custom upload directory outside data is not automatically covered; inspect the actual directory before staging anything.

## 8. If something goes wrong

### Conflict or diverged pull

Stop and read `git status` and `git diff`. Do not pick ours/theirs for the entire project, force push, hard-reset or Git-clean the repository. Tell the single owner of the affected file. Use the IDE conflict editor to read both versions, keep the intended behaviour, then stage only the resolved path and finish the merge commit. Run the checks again. To cancel a local merge that is still in progress, the team may use `git merge --abort`; first confirm status actually says a merge is in progress.

A `pull --ff-only` refusal means the histories diverged. Stop and coordinate with the lead rather than forcing it.

### Wrong file staged before committing

Example: a non-UI member accidentally staged app.css. Unstage it without discarding its content:

```powershell
git diff --cached -- src/main/resources/static/css/app.css
git restore --staged -- src/main/resources/static/css/app.css
git status --short
```

If it is only an accidental copied version, HEAD has the wanted version, and no personal edits need saving, its owner may discard that known copy:

```powershell
git restore --source=HEAD --worktree -- src/main/resources/static/css/app.css
```

Do not use the second command on work you want to keep. If a newly copied file is absent from HEAD, unstaging may leave an untracked file: preserve it and ask the lead to remove only that accidental copy after review. Do not use broad clean commands. An untracked Java file can make a local build pass even though CI will not receive it.

### Wrong commit before pushing

Do not rewrite the source archive. Find the exact commit with `git log`, then use the IDE/GitHub client's Revert Commit action to make an ordinary undo commit. Inspect it and rebuild. For a merge commit or an uncertain commit, ask the lead which parent/change to revert. No force push or blind branch deletion is needed.

### Path missing in the source checkout

The source archive may not include the uncommitted files, the archive ref may be wrong, or develop may have changed. Inspect the frozen source on GitHub and update the manifest. Do not silently omit required files.

### Build fails after copying only your files

That is expected for this current six-way complete-bundle split. Do not weaken tests, add somebody else's paths, or bypass the guard. Use the integration option or agree on a separately verified smaller split. No compatibility refactor is supplied by this document.

## 9. Lead release: develop into main

Only the lead does this after the integration checks and reviews pass. Recommended: open a release PR **develop → main** on GitHub and merge it after review/checks. If the team instead chooses a local merge, the equivalent manual commands follow. Do not use both release methods.

If local main is absent, create it once with `git checkout -b main origin/main`; otherwise use `git checkout main`. The agent created neither main nor develop and did not change either ref.

```powershell
git fetch origin
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git checkout develop
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git pull --ff-only origin develop
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git checkout main
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git pull --ff-only origin main
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
git diff --stat main..develop
git merge --no-ff develop
if ($LASTEXITCODE -ne 0) { throw "Resolve or abort with the lead; do not push" }
# Stop the demo app before building.
npm ci
if ($LASTEXITCODE -ne 0) { throw "npm ci failed" }
npm run build:css
if ($LASTEXITCODE -ne 0) { throw "CSS build failed" }
.\mvnw.cmd clean verify
if ($LASTEXITCODE -ne 0) { throw "Tests failed; do not push main" }
git status --short
$releasePending = git status --porcelain
if ($releasePending) { throw "Unexpected changes remain; review them before pushing main" }
git push origin main
if ($LASTEXITCODE -ne 0) { throw "Stopped: previous command failed. Review it before continuing." }
```

Review native Git failures before continuing; do not push if checkout/pull/merge failed. If main is protected, use the GitHub release PR. If main changes during the process, inspect the new base rather than force pushing. A completed local merge with failing tests must not be pushed; coordinate a fix or reviewed revert with the lead.

After merging, the receiving machine runs `npm ci` (or the documented `npm install` setup), `npm run build:css` and `.\mvnw.cmd clean verify`. Start the app with temporary demo data. Checklist:

- [ ] Visitor home/packages/instructors/about/FAQ/contact/login/sign-up.
- [ ] Student registration/login/profile/enrollment/payment instructions and booking.
- [ ] Instructor lesson dashboard and availability.
- [ ] Admin awaiting payments, vehicles, moderation and reports/CSV.
- [ ] Wrong-role requests denied; lessons and revenue use Paid enrollment rules.
- [ ] Bubble sort, FIFO and missing-photo fallbacks work.
- [ ] No destructive actions on live data; no real records staged.
- [ ] tailwind.css committed; target/node_modules/runtime folders excluded.

## 10. Lower-risk alternative and recommendation

| Option | Pros for this group | Cons |
|---|---|---|
| ONE integration PR from IT26101135-finalUI_changes into develop | Preserves integrated changes/deletions; avoids missing-type cycles and lead-name collision; one complete build/UI snapshot; easier for first-year students | Large PR; all six owners must review their paths; does not give six independent module-code PRs |
| Six develop-based member branches with file-only copies | Clear individual path diffs and review responsibility | Not independently buildable now; no green whole-bundle order; source branch needs safe archival; shared CSS needs coordinated follow-ups |

**Choose the integration PR for the current code.** Use the ownership table and each member's demo for the viva. Ask all six owners to review their part in the one PR. Do not claim Git authorship or PR reviews that have not happened. Old feature refs exist locally, but this audit does not prove the history of GitHub reviews or who merged them.

If marking requires six new PRs, show the dependency cycle to the lecturer and agree on smaller staged PRs before Git work. One file owner is useful, but does not make modules independent.

## 11. Group-chat message

Copy as plain text:

```text
Team, please read docs/GIT_BRANCH_PLAN.md before changing branches.

Our branch names are:
IT26101135-finalUI_changes
IT26101133-finalUI_changes
IT26101134-finalUI_changes
IT26101132-finalUI_changes
IT26101136-finalUI_changes
IT26101137-finalUI_changes

The plan gives every changed file one owner, including tests, shared files and deletions. There are still uncommitted edits, so the lead must save the complete source snapshot first.

Recommended merge order: ONE integration PR from IT26101135-finalUI_changes into develop, full build/tests and six-owner review, then the lead merges develop into main. The six complete module PRs have dependency cycles; do not merge them with failed builds. Their reading order is 135, 136, 133, 132, 134, 137, not a passing merge sequence.

If six PRs are required, coordinate the smaller split and archive first. Copy only your listed files. Never force push or commit target, node_modules, runtime data or real credentials. Keep the handwritten sort and FIFO intact.
```

## 12. Unverified, unclear and not done

- Fresh remote develop/main, live PRs and branch protection: cached refs only; no fetch or GitHub write was run.
- Whether a lecturer requires six new module-code PRs. The technical recommendation is one integration PR; the team owns the marking decision.
- No isolated split branches/intermediate merges were built. Missing dependencies are demonstrated from imports and newly added baseline paths.
- Header-free ownership choices are proposed, not proven authorship. The per-file reason column identifies them. No source headers were changed to force ownership.
- Local develop is absent; the team must create/update it manually.
- At audit time, the source had 28 dirty tracked files and four untracked source/test files. This new plan also needs committing. The recorded HEAD is the audit tip, not the future frozen snapshot commit.
- The published lead branch name is occupied. Reusing it for a lead-only branch requires coordinated archival/rename; no force overwrite is offered.
- Existing integrated tests do not certify future member branches. Record actual test/browser results after every real merge using temporary data.
- Phase B and C files do not exist yet. Assign their future documents to 135 unless the team agrees otherwise, and update this manifest/source snapshot before using it. No diagram folders were created.

Only `docs/GIT_BRANCH_PLAN.md` was created. No source, image, test, Git ref or folder layout was changed. STOP after Phase A.

