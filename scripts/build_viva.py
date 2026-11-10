"""Generate the source-linked viva guide, member diagrams and printable sheets."""
from pathlib import Path
import re, json, html
ROOT = Path.cwd()
OUT = ROOT / 'docs/viva-2026'
OUT.mkdir(parents=True, exist_ok=True)
INDEX = {}
for base in ['src/main/java', 'src/test/java']:
    for file in (ROOT / base).rglob('*.java'):
        INDEX.setdefault(file.stem, file.relative_to(ROOT))
INDEX.update({'app.css': Path('src/main/resources/static/css/app.css'), 'package.json': Path('package.json'), 'pom.xml': Path('pom.xml'), 'Git plan': Path('docs/GIT_BRANCH_PLAN.md'), 'Merge analysis': Path('docs/MERGE_ANALYSIS.md')})
REFERENCES = []
BAD_REFERENCES = []
def ref(name, method):
    path = INDEX[name]
    text = (ROOT / path).read_text(encoding='utf-8')
    if method not in ['class declaration', 'planned workflow', 'historical analysis', 'build dependency', 'build:css']:
        if not (re.search(r'\b' + re.escape(method) + r'\s*\(', text) or method in text): BAD_REFERENCES.append((name,method))
    REFERENCES.append((path.as_posix(), method))
    return path.as_posix(), method
def q(question, answer, refs, browser=''):
    return dict(q=question, a=answer, refs=[ref(*r) for r in refs], browser=browser)
PROJECT = [
q('What does our system do?', 'In our system, I help a school manage students, lesson packages, bookings, vehicles and reviews. I give admins reports and saved website enquiries to read.', [('HomeController','showHomePage'),('AdminReportService','business'),('ContactService','messagesForAdmin')], 'Home > Packages > View package'),
q('What are the four roles?', 'I explain visitors, students, instructors and admins as four user experiences. I store only three Role enum values because a visitor is someone who is not logged in.', [('Role','class declaration'),('NavigationInterceptor','postHandle')]),
q('Why Spring Boot?', 'I use Spring Boot to start the Java web application and connect its components. I use constructor injection so Spring supplies the objects each controller needs.', [('DrivesparkAcademyApplication','main'),('BookingController','BookingController')]),
q('How does MVC work here?', 'I let controllers accept requests, services check rules and repositories read or save data. I pass a model to a JSP view to display the result.', [('BookingController','list'),('BookingService','getOwnBookings'),('BookingRepository','readAllStrict')], 'Student > My lessons'),
q('Why file-based storage?', 'I store one JSON object per line so the records are easy to inspect and run locally. I accept that this has fewer safety and querying features than a database.', [('FileRepository','readRecords'),('FileRepository','writeRecords')]),
q('Are file writes transactions?', 'I do not have a transaction across several files. I use file replacement and locks inside one JVM, but a crash between two file updates can still leave partial work.', [('FileRepository','writeRecords'),('AccountWriteCoordinator','execute'),('EnrollmentService','markPaid')]),
q('How are passwords stored?', 'I store a salted BCrypt hash instead of the original password. I use matches to check a login and never need to decrypt a password.', [('PasswordHasher','hash'),('PasswordHasher','matches')]),
q('How do sessions work?', 'I keep the logged-in user ID in a server-side HTTP session. I rotate the session on login and reload the account so a deleted or suspended account does not keep normal access.', [('AuthSessionService','loginWithResult'),('AuthSessionService','getCurrentUser')], 'Log in > Account > Log out'),
q('What is CSRF protection?', 'I put a session token in forms that change data. I reject unsafe requests with missing or wrong tokens so another website cannot simply submit a form as the signed-in user.', [('CsrfTokenService','isValid'),('CsrfFilter','doFilterInternal')]),
q('How do we protect admin pages?', 'I use access annotations and an interceptor to check login and role. I also check role and ownership in services before exposing or changing a record.', [('AccessInterceptor','preHandle'),('BookingService','getBookingDetails')]),
q('Where is validation done?', 'I use browser rules to help the user enter details. I repeat important checks in Java because someone can bypass the browser.', [('Student','setPhone'),('LearningPackage','setPrice'),('BookingService','requestLesson')]),
q('How do error pages help?', 'I convert known exceptions into safe pages with useful status codes and recovery links. I keep private record contents and internal paths out of the user message.', [('GlobalExceptionHandler','handleValidation'),('GlobalExceptionHandler','handleStorage'),('ErrorPageSupport','prepare')]),
q('How does payment confirmation work?', 'I first save an enrollment awaiting payment without granting lessons. I let an admin confirm received payment, then start validity and grant the package lesson count.', [('EnrollmentService','enroll'),('EnrollmentService','markPaid'),('EnrollmentService','activateLessons')], 'Student 8 > My enrollments > Payment instructions; admin > Awaiting payment'),
q('Why can students register freely?', 'I create a new student as Active so they can log in and choose a package. I still require a paid usable enrollment before booking, and admins can manage account status.', [('AuthService','registerStudent'),('PaidEnrollmentGuard','requirePaid')], 'Register > Log in > Packages'),
q('How is data seeded?', 'I enable the loader only with the demo setting and a fresh empty folder. I add missing records in dependency order, preserve handled requests and spent balances, and calculate ratings from approved reviews.', [('DemoDataSeedRunner','run'),('DemoDataSeedRunner','syncInstructorRatings'),('DemoDataSeedRunnerTest','seedCanRunTwiceWithoutDuplicateRecords')]),
q('How was the Tailwind UI built?', 'I use Tailwind utilities and shared CSS classes, then build a local stylesheet with npm run build:css. I use white account cards on soft grey, dark navigation and amber actions, while public photos stay cinematic.', [('package.json','build:css'),('app.css','body.ds-account-page')]),
q('What do reports count?', 'I count saved records and include only Paid enrollment amounts as revenue. I group revenue by enrollment month because I do not store a separate payment timestamp.', [('AdminReportService','business'),('AdminReportService','money')], 'Admin > Reports'),
q('How many tests do we have?', 'I can show the recorded final clean verify result of {{TEST_TOTAL}} tests with zero failures, errors or skips. I separate automated tests from browser checks and their stated limits.', [('pom.xml','build dependency'),('AdminReportServiceTest','businessCsvUsesNamesHeadersTotalsAndBom')]),
q('What are the custom ordering algorithms?', 'I use hand-written bubble sort to order instructors by a chosen detail. I use a linked FIFO queue for pending unassigned requests and rebuild its order from saved creation times.', [('InstructorSorter','sort'),('BookingRequestQueue','BookingRequestQueue')]),
q('What would I improve?', 'I would add a relational database with transactions and stronger audit records. I would then add real payment and email integrations with proper failure handling.', [('FileRepository','writeRecords'),('PasswordController','requestReset')])]

MEMBERS = {
'IT26101135': dict(title='Accounts, students and integration', model='Student', repo='UserRepository', service='StudentProfileService', controller='StudentProfileController', contract='StudentProfiles', encap='setPhone', create=('AuthService','registerStudent'), read=('StudentProfileService','getOwnProfile'), update=('StudentProfileService','updateOwnProfile'), delete=('StudentAccountService','deleteOwnAccount'), error=('StudentProfileService','ensureAvailableEmail'), extra=['AuthController','AuthSessionService','AuthService','PasswordHasher','CsrfFilter'], demo=['Open Register from the login page','Return using Log in','Log in as student1','Open Account settings','Open Change password without submitting']),
'IT26101133': dict(title='Instructors, availability and bubble sort', model='Instructor', repo='InstructorRepository', service='InstructorService', controller='InstructorAvailabilityController', contract='InstructorScheduleOperations', encap='setYearsOfExperience', create=('InstructorAccountService','create'), read=('InstructorService','sortInstructors'), update=('InstructorAccountService','update'), delete=('InstructorAccountService','delete'), error=('InstructorScheduleService','ensureNoOverlap'), extra=['InstructorSorter','InstructorOrder','InstructorScheduleService','AvailabilityRepository','InstructorLeaveRepository'], demo=['Open Instructors','Select Experience','Choose descending order','Click Sort','Open Packages']),
'IT26101134': dict(title='Bookings, FIFO and conflict rules', model='Booking', repo='BookingRepository', service='BookingService', controller='BookingController', contract='StudentBookingOperations', encap='Booking', create=('BookingService','requestLesson'), read=('BookingService','getOwnBookings'), update=('BookingService','rescheduleOwnBooking'), delete=('BookingService','deleteCancelledBooking'), error=('BookingService','parseFilterDate'), extra=['BookingRequestQueue','BookingConflictChecker','AdminBookingOperations','PaidEnrollmentGuard'], demo=['Open My lessons as student1','Open the first booking details','Inspect its waiting-list position','Click Back to bookings','Open History']),
'IT26101132': dict(title='Vehicles and seed data', model='Vehicle', repo='VehicleRepository', service='VehicleService', controller='VehicleController', contract='VehicleOperations', encap='setNextServiceDate', create=('VehicleService','createVehicle'), read=('VehicleService','getAllVehicles'), update=('VehicleService','updateVehicle'), delete=('VehicleService','deleteVehicle'), error=('VehicleService','ensureRegistrationNumberIsUnique'), extra=['VehicleStore','DemoDataSeedRunner','DemoDataSeedRunnerTest'], demo=['Open Vehicles as admin','Open Edit vehicle for the Alto','Return through Vehicles in the sidebar','Open Edit vehicle for the Honda Dio','Return to Vehicles and inspect warnings']),
'IT26101136': dict(title='Packages, enrollment and payment', model='LearningPackage', repo='LearningPackageRepository', service='LearningPackageService', controller='LearningPackageController', contract='LearningPackageCatalog', encap='setPrice', create=('LearningPackageService','createPackage'), read=('LearningPackageService','listPackages'), update=('LearningPackageService','updatePackage'), delete=('LearningPackageService','deletePackage'), error=('LearningPackageService','ensureUniqueName'), extra=['LearningRecord','Enrollment','EnrollmentService','EnrollmentOperations','EnrollmentAdministration','EnrollmentPresentationService'], demo=['Open My enrollments as student8','Open Payment instructions','Return to My enrollments','Open Packages','Open View package']),
'IT26101137': dict(title='Reviews, ratings and reports', model='Review', repo='ReviewRepository', service='ReviewService', controller='ReviewController', contract='ReviewOperations', encap='Review', create=('ReviewService','createReview'), read=('ReviewService','getVisibleReviews'), update=('ReviewService','updateOwnReview'), delete=('ReviewService','deleteOwnReview'), error=('ReviewService','ensureNoDuplicate'), extra=['ReviewModeration','ReportCatalog','AdminReportService','AdminReportController','BusinessReportSection'], demo=['Open Reports as admin','Download a section CSV','Open Print report','Cancel printing after inspecting it','Download full CSV'])}

DETAILS = {
'IT26101135': [
('Abstraction','I use abstract User for common account details. I require each account child to supply its own role and dashboard path.', [('User','getRole'),('User','getDashboardPath')]),
('Inheritance','I make Student extend User so it reuses account name, email and password-hash rules. I use Admin and InstructorAccount as other User children.', [('Student','class declaration'),('Admin','class declaration'),('InstructorAccount','class declaration')]),
('Polymorphism','I call getDashboardPath through a User reference after login. I get the student, instructor or admin destination from the actual account class.', [('AuthController','login'),('User','getDashboardPath')]),
('Single Responsibility and Open/Closed','I keep profile HTTP handling in StudentProfileController and rules in StudentProfileService. I can add a User child without rewriting existing children, but a new role still needs enum and access changes.', [('StudentProfileController','updateProfile'),('User','class declaration')]),
('Liskov, Interface Segregation and Dependency Inversion','I can use Student as User while keeping a valid role and destination. I give profile pages the smaller StudentProfiles contract. I inject that interface into StudentProfileController rather than constructing its service there.', [('Student','getRole'),('StudentProfiles','getOwnProfile'),('StudentProfileController','StudentProfileController')]),
('Registration, login and sessions','I register a student as Active with a unique email and hashed password. I create a session only after the password and account status pass. I keep payment eligibility separate from account registration.', [('AuthService','registerStudent'),('AuthSessionService','loginWithResult')])],
'IT26101133': [
('Abstraction','I use InstructorOrder to describe how two instructors are compared. I let the sorter use that contract rather than put every comparison inside the swap loop.', [('InstructorOrder','compare'),('InstructorSorter','swapNeighbours')]),
('Inheritance','I make InstructorRepository extend FileRepository for shared file operations. I keep Instructor as a separate profile because it does not extend the account User class.', [('InstructorRepository','class declaration'),('Instructor','class declaration')]),
('Polymorphism','I call the same compare method with name, experience or rating comparison objects. I get different decisions while keeping the same bubble-sort passes.', [('InstructorSorter','swapNeighbours'),('InstructorNameOrder','compare'),('InstructorExperienceOrder','compare'),('InstructorRatingOrder','compare')]),
('Single Responsibility and Open/Closed','I give InstructorSorter only the sorting job. I can add another InstructorOrder without rewriting the swap passes, although chooseOrder must still select the new option.', [('InstructorSorter','sort'),('InstructorSorter','chooseOrder')]),
('Liskov, Interface Segregation and Dependency Inversion','I let each InstructorOrder return a negative, zero or positive comparison result. I keep that interface to one compare method. I inject InstructorScheduleOperations into the availability controller.', [('InstructorNameOrder','compare'),('InstructorOrder','compare'),('InstructorAvailabilityController','InstructorAvailabilityController')]),
('Bubble sort, steps and complexity','I compare neighbours and swap them when they are in the wrong order. For ascending [8, 3, 12], I get [3, 8, 12] after the first pass and stop after a pass without swaps. I have O(n squared) worst-case time and O(n) best-case time, plus O(n) space for the copied list.', [('InstructorSorter','sort'),('InstructorSorter','swapNeighbours')])],
'IT26101134': [
('Abstraction','I separate StudentBookingOperations from AdminBookingOperations. I describe list ordering through BookingOrder without exposing the queue nodes.', [('StudentBookingOperations','class declaration'),('AdminBookingOperations','class declaration'),('BookingOrder','order')]),
('Inheritance','I make BookingRepository extend FileRepository for shared storage. I use a private linked Node inside the queue rather than claiming the queue inherits from Booking.', [('BookingRepository','class declaration'),('BookingRequestQueue','class declaration')]),
('Polymorphism','I call order through BookingOrder references for pending-request and lesson-time views. I remember that the pending view also filters requests, so those views do not have identical business meanings.', [('BookingService','getAllBookings'),('PendingRequestOrder','order'),('LessonTimeOrder','order')]),
('Single Responsibility and Open/Closed','I keep conflict checks in BookingConflictChecker and waiting order in BookingRequestQueue. I can add another BookingOrder without rewriting the existing ordering classes, but the caller must choose the right view.', [('BookingConflictChecker','conflictReason'),('BookingOrder','order')]),
('Liskov, Interface Segregation and Dependency Inversion','I preserve the shared save contract in BookingRepository. I separate student and admin booking contracts. I inject StudentBookingOperations into BookingController.', [('BookingRepository','save'),('StudentBookingOperations','class declaration'),('BookingController','BookingController')]),
('FIFO, enqueue, dequeue and complexity','I enqueue at the tail and dequeue the oldest head, each in O(1) time. I use FIFO so later unassigned requests do not jump ahead. I rebuild the queue with stable insertion sorting, which can take O(n squared), and position lookup takes O(n).', [('BookingRequestQueue','enqueue'),('BookingRequestQueue','dequeue'),('BookingRequestQueue','positionOf'),('BookingService','processNext')])],
'IT26101132': [
('Abstraction','I expose fleet actions through VehicleOperations. I use VehicleStore for storage tasks instead of putting JSON file code in the controller.', [('VehicleOperations','class declaration'),('VehicleStore','class declaration'),('VehicleController','VehicleController')]),
('Inheritance','I make VehicleRepository extend FileRepository to reuse storage, validation and ID counters. I do not copy the JSON writer into Vehicle.', [('VehicleRepository','class declaration')]),
('Polymorphism','I use a VehicleStore reference in VehicleService with VehicleRepository as the actual object. I call its storage methods while keeping the parent file rules.', [('VehicleService','VehicleService'),('VehicleRepository','save')]),
('Single Responsibility and Open/Closed','I keep vehicle rules in VehicleService and file writing in VehicleRepository. I can add another typed repository without rewriting the shared FileRepository writer.', [('VehicleService','deleteVehicle'),('VehicleRepository','class declaration'),('FileRepository','writeRecords')]),
('Liskov, Interface Segregation and Dependency Inversion','I delegate VehicleRepository updates to the same parent checks. I give the controller the VehicleOperations fleet contract. I inject that interface instead of constructing VehicleService in the controller.', [('VehicleRepository','update'),('VehicleOperations','class declaration'),('VehicleController','VehicleController')]),
('Expiry warnings and safe seeding','I show separate expired or overdue labels and warnings for dates within thirty days. I seed missing records without refilling spent lessons. I use fictional phone and NIC patterns accepted by the app, not real allocated identities.', [('Vehicle','isInsuranceWarning'),('Vehicle','isServiceWarning'),('DemoDataSeedRunner','run')])],
'IT26101136': [
('Abstraction','I use LearningPackageCatalog for package-reading tasks and EnrollmentOperations for student enrollment tasks. I keep admin payment tasks in EnrollmentAdministration.', [('LearningPackageCatalog','class declaration'),('EnrollmentOperations','class declaration'),('EnrollmentAdministration','class declaration')]),
('Inheritance','I make LearningPackageRepository extend FileRepository for storage rules. I do not make Enrollment extend LearningPackage because they are different records.', [('LearningPackageRepository','class declaration'),('Enrollment','class declaration')]),
('Polymorphism','I let LearningPackage and Enrollment implement the same LearningRecord display contract. I can call their display methods through that contract, although current pages use presentation helpers for resolved package names.', [('LearningRecord','getDisplayName'),('LearningPackage','getDisplayName'),('Enrollment','getDisplayName')]),
('Single Responsibility and Open/Closed','I build read-only labels in EnrollmentPresentationService while EnrollmentService handles payment rules. I can add another LearningRecord without rewriting existing records, although a page must choose to use it.', [('EnrollmentPresentationService','addStudent'),('LearningRecord','getDisplayName')]),
('Liskov, Interface Segregation and Dependency Inversion','I preserve the shared storage contract in LearningPackageRepository. I separate student EnrollmentOperations from admin EnrollmentAdministration. I inject EnrollmentOperations into EnrollmentController.', [('LearningPackageRepository','save'),('EnrollmentAdministration','class declaration'),('EnrollmentController','EnrollmentController')]),
('Payment confirmation and packages','I recheck the waiting request, student and course inside the shared account lock. I activate lessons and save the Paid receipt, with validity starting on confirmation day. I do not claim a transaction because those are separate file updates.', [('EnrollmentService','markPaid'),('EnrollmentService','activateLessons'),('AccountWriteCoordinator','execute')])],
'IT26101137': [
('Abstraction','I expose student feedback through ReviewOperations and moderation through ReviewModeration. I expose report tables through ReportCatalog so the controller does not calculate them.', [('ReviewOperations','class declaration'),('ReviewModeration','class declaration'),('ReportCatalog','business')]),
('Inheritance','I make ReviewRepository extend FileRepository for shared file operations. I keep Review as a model implementing Identifiable rather than a child of a service.', [('ReviewRepository','class declaration'),('Review','class declaration')]),
('Polymorphism','I call business through the ReportCatalog reference in the report controller. I receive the real AdminReportService object supplied by Spring.', [('AdminReportController','show'),('AdminReportService','business')]),
('Single Responsibility and Open/Closed','I calculate tables in AdminReportService and write downloads in AdminReportController. I can supply another ReportCatalog implementation without putting calculation rules in the controller.', [('AdminReportService','business'),('AdminReportController','export'),('ReportCatalog','business')]),
('Liskov, Interface Segregation and Dependency Inversion','I preserve the FileRepository contract in ReviewRepository. I split ordinary review operations from ReviewModeration. I inject ReviewOperations and ReportCatalog into their controllers.', [('ReviewRepository','save'),('ReviewModeration','class declaration'),('ReviewController','ReviewController'),('AdminReportController','AdminReportController')]),
('Paid-only revenue and business CSV','I add captured enrollment amounts only when isPaid is true and format them with two decimals. I write a UTF-8 BOM, a business header, named sections and totals. I quote cells, neutralize formula prefixes, and offer full and section downloads.', [('AdminReportService','business'),('AdminReportController','export'),('AdminReportController','csv')])]
}
for member, m in MEMBERS.items():
    questions = [
        q('What is my module responsible for?', f'In our system, I use separate classes for the {m["title"].lower()} module. I keep web requests, rules and storage in separate classes.', [(m['controller'],m['controller']),(m['service'],m['service'])]),
        q('Where are Create and Read?', f'I create through {m["create"][0]} using {m["create"][1]} and read through {m["read"][0]} using {m["read"][1]}. I let repositories handle files after service checks.', [m['create'],m['read']]),
        q('Where are Update and Delete?', f'I update through {m["update"][0]} using {m["update"][1]} and delete through {m["delete"][0]} using {m["delete"][1]}. I apply deletion rules instead of breaking saved lesson or payment history.', [m['update'],m['delete']]),
        q('Where did I use encapsulation?', f'I keep {m["model"]} fields private and use checked methods to control changes. I use {m["encap"]} to reject invalid values instead of trusting a form.', [(m['model'],m['encap'])])]
    for title, answer, references in DETAILS[member][:3]:
        questions.append(q('Where did I use ' + title.lower() + '?', answer, references))
    error = 'ValidationException' if member == 'IT26101134' else 'DuplicateRecordException'
    questions.append(q('How did I use exception handling as the fifth course topic?', f'I use {error} when this module cannot accept a requested change. I let the shared handler explain it safely instead of exposing file details.', [m['error'],('GlobalExceptionHandler','handleValidation' if error=='ValidationException' else 'handleDuplicate')]))
    for title, answer, references in DETAILS[member][3:]:
        questions.append(q('How do I explain ' + title + '?', answer, references))
    questions.append(q('What should I demonstrate honestly?', f'I can demonstrate the {m["title"].lower()} pages using disposable seeded data. I use server-side rules and tests as evidence, and mark an unconfirmed demonstration NEEDS CHECK.', [(m['service'],m['service'])], ' > '.join(m['demo'])))
    m['questions'] = questions

WHATIFS = [
q('What if two admins confirm the same payment?', 'I serialize the operation and reload the request inside the shared account lock. I grant lessons once; the second attempt sees it was already handled. I only have this protection in one JVM, not across app instances.', [('EnrollmentService','markPaid'),('EnrollmentService','requireAwaiting'),('AccountWriteCoordinator','execute')]),
q('What if a student double-clicks Book?', 'I disable an accepted submit in JavaScript and recheck conflicts and commitments under the shared booking lock. I reject a repeated unavailable overlapping time, but I do not have a general idempotency-key API.', [('BookingService','requestLesson'),('BookingService','requireCommitmentRoom')]),
q('What if a vehicle goes into maintenance after assignment?', 'I save the status but do not automatically cancel all existing bookings. I recheck conflicts when assigning or accepting, while completion is not a fresh vehicle-inspection check. I would ask the admin to reassign or cancel the affected lesson.', [('VehicleService','updateVehicle'),('BookingService','acceptAssignedLesson'),('BookingService','completeAssignedLesson')]),
q('What if a file is corrupted?', 'I use strict reads for important decisions, so damaged data raises StorageException instead of being silently trusted. I show a safe error page and would repair or restore a trusted copy before continuing.', [('FileRepository','readAllStrict'),('GlobalExceptionHandler','handleStorage')]),
q('What if an instructor adds leave on a confirmed lesson date?', 'I block leave over confirmed lessons or assigned pending lessons. I ask an administrator to resolve those lessons first. I use the shared app lock for schedule changes and booking writes.', [('InstructorScheduleService','validateLeave'),('InstructorScheduleService','deleteAvailability'),('BookingService','acceptAssignedLesson')]),
q('What if someone changes an ID in the URL?', 'I check role and record ownership instead of trusting the URL. I return a forbidden, missing-record or validation response when access is not allowed.', [('BookingService','getBookingDetails'),('EnrollmentService','requireOwnEnrollment')]),
q('What if the app restarts?', 'I keep records and counters in files and rebuild the queue from them. I lose ordinary in-memory sessions and reset codes. I do not use seeding to overwrite existing decisions.', [('BookingRequestQueue','BookingRequestQueue'),('PasswordManagementService','createResetToken'),('DemoDataSeedRunner','run')]),
q('What if a student cancels less than 24 hours before a lesson?', 'I reject the change with the default twenty-four-hour notice rule. I leave the booking intact; the admin has a separate cancellation operation for appropriate cases.', [('BookingService','cancelOwnBooking'),('BookingService','forceCancel'),('BookingCutoffTest','class declaration')]),
q('What if someone completes a future lesson?', 'I do not give students the completion operation. I block completion and no-show before lesson end with the default attendance setting. I show the lesson end time and disable attendance buttons until then. I allow an explicit override only for an isolated preview.', [('BookingService','completeAssignedLesson'),('Booking','complete'),('AttendancePresentationAdvice','isAvailable')]),
q('What if the package is unpaid, expired or used up?', 'I refuse booking when a paid receipt cannot supply usable lessons or cover the lesson date. I allow renewal of an expired or used-up package only with no open lesson and one payment request at a time. I keep old receipts and recheck eligibility when an administrator confirms payment.', [('PaidEnrollmentGuard','problemForLesson'),('EnrollmentService','renewalProblem'),('EnrollmentService','markPaid')]),
q('What if the course price changes later?', 'I keep the amount captured on an existing enrollment. I use that amount for revenue, so a catalog price change does not rewrite old paid amounts.', [('Enrollment','getAmount'),('AdminReportService','business')]),
q('What if a form has no CSRF token?', 'I reject the unsafe request before the controller changes data. I ask the user to reload the form rather than disabling the check.', [('CsrfFilter','doFilterInternal'),('CsrfFilterTest','class declaration')]),
q('What if someone deletes a referenced vehicle or package?', 'I refuse deletions that would break lesson or enrollment references. I use active or availability status where appropriate instead of deleting history.', [('VehicleService','deleteVehicle'),('LearningPackageService','deletePackage')]),
q('What if a review is edited or hidden?', 'I return an edited student review to pending approval. I calculate averages from approved reviews, so pending or hidden comments do not count.', [('ReviewService','updateOwnReview'),('ReviewService','recalculateRating'),('AdminReportService','business')]),
q('What if a crash occurs between balance and receipt writes?', 'I do not have rollback across those files, so partial state is possible. I would inspect the affected records rather than pretend the in-process lock is a transaction.', [('EnrollmentService','markPaid'),('FileRepository','writeRecords')])]
GROUP = [
q('Who did what?', 'I use member headers to explain the agreed module split, with integration under IT26101135. I do not treat a header as proof that only one person edited a file.', [('AuthController','class declaration'),('InstructorSorter','sort'),('BookingRequestQueue','enqueue'),('DemoDataSeedRunner','run'),('EnrollmentService','markPaid'),('AdminReportController','export')]),
q('How did we use Git and pull requests?', 'NEEDS CHECK: I can show local merge subjects mentioning PR 36 and PR 32, but cannot confirm every review or author action from code. I would ask our lead to verify the exact team workflow before describing it as our history.', [('Git plan','planned workflow')]),
q('How did we test together?', 'I can show the recorded final build, JUnit and MockMvc tests, and isolated browser evidence. I need the team to confirm who performed each rehearsal before claiming a personal contribution.', [('AdminReportServiceTest','class declaration'),('AuthWebIntegrationTest','class declaration')]),
q('How did we handle merge conflicts?', 'NEEDS CHECK: I can see a local merge subject about resolving instructor contracts. I cannot infer who resolved each conflict or the discussions, so I would verify that with the team.', [('Merge analysis','historical analysis')]),
q('What was the hardest bug?', 'NEEDS CHECK: I cannot know which bug the team found hardest without their account. I can explain the tested future-completion and payment-concurrency risks and show the current guards.', [('Booking','complete'),('EnrollmentService','requireAwaiting'),('EnrollmentPaymentServiceTest','class declaration')])]
BLUFF = [
('Payments','I confirm payments manually after the school checks receipt information. I do not claim a gateway or bank reconciliation.'),
('Email and receipts','I do not send email or upload receipts. I display contact instructions only when configured.'),
('Password recovery','I use an in-memory reset code shown locally. I do not claim verified email ownership, reset-email delivery or production-ready recovery.'),
('Storage and locking','I use files and locks inside one JVM. I do not claim multi-file transactions, cross-process locking or database rollback.'),
('Roles','I explain four user experiences, but only three stored roles. I do not invent a Visitor enum.'),
('Reports','I group revenue by enrollment month because payment dates are not stored. I do not claim an atomic multi-store report snapshot.'),
('Seed and claims','I use fictional identities and syntax-only phone/NIC patterns. I do not claim real accreditation, licences, pass rates or real student feedback.'),
('Team history and coverage','I mark unverified PR reviews, conflicts and personal contributions NEEDS CHECK. I do not say passing tests prove every browser state or complete WCAG compliance.')]

DIAGRAMS = {
'IT26101135': (['AuthController','AuthSessionService','AuthService','User','Student','Admin','InstructorAccount','UserRepository','FileRepository'], [('Student','User','extends'),('Admin','User','extends'),('InstructorAccount','User','extends'),('UserRepository','FileRepository','extends'),('AuthController','AuthSessionService','uses'),('AuthController','AuthService','uses'),('AuthSessionService','AuthService','uses'),('AuthService','UserRepository','uses')]),
'IT26101133': (['InstructorService','InstructorSorter','InstructorOrder','InstructorNameOrder','InstructorExperienceOrder','InstructorRatingOrder','Instructor','InstructorRepository','FileRepository','InstructorScheduleService','AvailabilityRepository','InstructorLeaveRepository'], [('InstructorRepository','FileRepository','extends'),('AvailabilityRepository','FileRepository','extends'),('InstructorLeaveRepository','FileRepository','extends'),('InstructorNameOrder','InstructorOrder','implements'),('InstructorExperienceOrder','InstructorOrder','implements'),('InstructorRatingOrder','InstructorOrder','implements'),('InstructorService','InstructorSorter','uses'),('InstructorSorter','InstructorOrder','uses'),('InstructorService','InstructorRepository','uses'),('InstructorScheduleService','AvailabilityRepository','uses'),('InstructorScheduleService','InstructorLeaveRepository','uses')]),
'IT26101134': (['BookingController','StudentBookingOperations','AdminBookingOperations','BookingService','BookingConflictChecker','BookingRequestQueue','Booking','BookingRepository','FileRepository','BookingOrder','PendingRequestOrder','LessonTimeOrder'], [('BookingService','StudentBookingOperations','implements'),('BookingService','AdminBookingOperations','implements'),('BookingRepository','FileRepository','extends'),('PendingRequestOrder','BookingOrder','implements'),('LessonTimeOrder','BookingOrder','implements'),('BookingController','StudentBookingOperations','uses'),('BookingService','BookingConflictChecker','uses'),('BookingService','BookingRequestQueue','uses'),('BookingService','BookingRepository','uses')]),
'IT26101132': (['VehicleController','VehicleOperations','VehicleService','VehicleStore','VehicleRepository','FileRepository','Vehicle','DemoDataSeedRunner'], [('VehicleService','VehicleOperations','implements'),('VehicleRepository','VehicleStore','implements'),('VehicleRepository','FileRepository','extends'),('VehicleController','VehicleOperations','uses'),('VehicleService','VehicleStore','uses'),('DemoDataSeedRunner','VehicleRepository','uses')]),
'IT26101136': (['LearningPackage','Enrollment','LearningRecord','LearningPackageCatalog','LearningPackageService','EnrollmentOperations','EnrollmentAdministration','EnrollmentService','LearningPackageRepository','EnrollmentRepository','FileRepository','EnrollmentPresentationService'], [('LearningPackage','LearningRecord','implements'),('Enrollment','LearningRecord','implements'),('LearningPackageService','LearningPackageCatalog','implements'),('EnrollmentService','EnrollmentOperations','implements'),('EnrollmentService','EnrollmentAdministration','implements'),('LearningPackageRepository','FileRepository','extends'),('EnrollmentRepository','FileRepository','extends'),('LearningPackageService','LearningPackageRepository','uses'),('EnrollmentService','EnrollmentRepository','uses'),('EnrollmentPresentationService','LearningPackageCatalog','uses')]),
'IT26101137': (['Review','ReviewOperations','ReviewModeration','ReviewService','ReviewRepository','FileRepository','ReportCatalog','AdminReportService','AdminReportController','BusinessReportSection'], [('ReviewService','ReviewOperations','implements'),('ReviewService','ReviewModeration','implements'),('ReviewRepository','FileRepository','extends'),('AdminReportService','ReportCatalog','implements'),('ReviewService','ReviewRepository','uses'),('AdminReportController','ReportCatalog','uses'),('AdminReportService','BusinessReportSection','creates')])}

SELECT = {
'AuthController':['register','login','logout'], 'AuthSessionService':['loginWithResult','getCurrentUser'], 'AuthService':['registerStudent','authenticate'],
'User':['getRole','getDashboardPath'],'Student':['setPhone','getRole'],'Admin':['getRole'],'InstructorAccount':['getRole'],
'FileRepository':['save','readAllStrict','update','delete'],'InstructorService':['createInstructor','sortInstructors'],'InstructorSorter':['sort','swapNeighbours'],
'InstructorOrder':['compare'],'InstructorNameOrder':['compare'],'InstructorExperienceOrder':['compare'],'InstructorRatingOrder':['compare'],
'Instructor':['setYearsOfExperience','getAverageRating'],'InstructorScheduleService':['addAvailability','addLeave'],
'BookingController':['list'],'StudentBookingOperations':['requestLesson','getOwnBookings'],'AdminBookingOperations':['processNext','forceCancel'],
'BookingService':['requestLesson','processNext','acceptAssignedLesson'],'BookingConflictChecker':['conflictReason'],'BookingRequestQueue':['enqueue','dequeue','positionOf'],
'Booking':['confirm','complete','cancel'],'BookingOrder':['order'],'PendingRequestOrder':['order'],'LessonTimeOrder':['order'],
'VehicleController':['listVehicles'],'VehicleOperations':['createVehicle','updateVehicle','deleteVehicle'],'VehicleService':['getAllVehicles','updateVehicle'],
'VehicleStore':['readAllStrict','save'],'Vehicle':['isInsuranceWarning','isServiceWarning'],'DemoDataSeedRunner':['run','syncInstructorRatings'],
'LearningRecord':['getDisplayName'],'LearningPackage':['setPrice','getDisplayName'],'Enrollment':['getAmount','isPaid','getDisplayName'],
'LearningPackageCatalog':['listPackages'],'LearningPackageService':['createPackage','updatePackage','deletePackage'],
'EnrollmentOperations':['enroll','ownEnrollments'],'EnrollmentAdministration':['markPaid','cancelRequest'],'EnrollmentService':['enroll','markPaid','cancelRequest'],
'EnrollmentPresentationService':['addStudent','forBookings'],'Review':['getRating','getStatus'],'ReviewOperations':['createReview','updateOwnReview','deleteOwnReview'],
'ReviewModeration':['approveReview','hideReview'],'ReviewService':['createReview','recalculateRating'],'ReportCatalog':['all','business'],
'AdminReportService':['business','revenue'],'AdminReportController':['show','export'],'BusinessReportSection':['getTitle','getHeadings','getRows']}

def methods_for(name):
    candidates = SELECT.get(name, ['save','readAllStrict','update'])
    text = (ROOT / INDEX[name]).read_text(encoding='utf-8')
    return [method for method in candidates if re.search(r'\b'+re.escape(method)+r'\s*\(',text)][:3]

def visibility(name,method):
    source=(ROOT/INDEX[name]).read_text(encoding='utf-8')
    found=re.search(r'\b(public|protected|private)\s+(?:(?:static|final|synchronized)\s+)*(?:[\w<>,?.\[\]]+\s+)?'+re.escape(method)+r'\s*\(',source)
    return {'private':'-','protected':'#','public':'+'}.get(found.group(1) if found else 'public','+')

def diagram(member):
    names, edges = DIAGRAMS[member]
    mermaid = ['classDiagram','direction TB']
    for name in names:
        source = (ROOT / INDEX[name]).read_text(encoding='utf-8')
        mermaid += ['class '+name+' {']
        if re.search(r'public interface\s+'+name,source): mermaid += ['  <<interface>>']
        for method in methods_for(name): mermaid += ['  '+visibility(name,method)+method+'()']
        mermaid += ['}']
    for child,parent,kind in edges:
        if kind in ['extends','implements']:
            source=(ROOT/INDEX[child]).read_text(encoding='utf-8')
            declaration=re.search(r'public (?:final |abstract )?(?:class|interface|record)\s+'+child+r'[^\{]*',source)
            assert declaration and parent in declaration.group(), (child,parent,kind)
        mermaid += [child+(' --|> ' if kind=='extends' else ' ..|> ' if kind=='implements' else ' --> ')+parent+(' : '+kind if kind not in ['extends','implements'] else '')]
    (OUT/(member+'.mmd')).write_text('\n'.join(mermaid)+'\n',encoding='utf-8')
    width=1200; height=120+((len(names)+2)//3)*190
    positions={name:(30+(i%3)*395,80+(i//3)*190) for i,name in enumerate(names)}
    svg=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}"><rect width="100%" height="100%" fill="white"/><defs><marker id="arrow" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto"><path d="M0 0 L10 5 L0 10" fill="none" stroke="#334155"/></marker><marker id="triangle" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="9" markerHeight="9" orient="auto"><path d="M0 0 L10 5 L0 10 Z" fill="white" stroke="#334155"/></marker></defs>', f'<text x="30" y="28" font-family="Arial" font-size="22" fill="#0f172a">{member}: {html.escape(MEMBERS[member]["title"])}</text>', '<text x="30" y="52" font-family="Arial" font-size="13" fill="#475569">Selected methods. Triangle: extends/implements; labelled arrow: dependency. Shared types are included as context.</text>']
    for child,parent,kind in edges:
        x,y=positions[child]; px,py=positions[parent]
        if y==py:
            a=(x+350 if px>x else x,y+75); b=(px if px>x else px+350,py+75)
        else: a=(x+175,y+150 if py>y else y); b=(px+175,py if py>y else py+150)
        dash=' stroke-dasharray="5 4"' if kind=='implements' else ''
        svg += [f'<path d="M{a[0]} {a[1]} L{b[0]} {b[1]}" fill="none" stroke="#64748b"{dash} marker-end="url(#{"triangle" if kind in ["extends","implements"] else "arrow"})"/>',f'<text x="{(a[0]+b[0])/2}" y="{(a[1]+b[1])/2-4}" font-family="Arial" font-size="10" fill="#334155">{kind}</text>']
    for name in names:
        x,y=positions[name];source=(ROOT/INDEX[name]).read_text(encoding='utf-8'); interface=bool(re.search(r'public interface\s+'+name,source))
        fields=re.findall(r'private\s+(?:final\s+)?([\w<>?,.]+)\s+(\w+)\s*;',source)[:2]
        svg += [f'<g font-family="Arial" fill="#0f172a"><rect x="{x}" y="{y}" width="350" height="150" rx="8" fill="{ "#eff6ff" if interface else "#f8fafc"}" stroke="#64748b"/><text x="{x+12}" y="{y+24}" font-size="15" font-weight="bold">{html.escape(name)}</text><line x1="{x}" y1="{y+36}" x2="{x+350}" y2="{y+36}" stroke="#cbd5e1"/>']
        lines=(['interface'] if interface else ['- '+field+': '+typ for typ,field in fields])+[visibility(name,method)+' '+method+'()' for method in methods_for(name)]
        for i,line in enumerate(lines[:5]):svg += [f'<text x="{x+12}" y="{y+55+i*18}" font-size="12">{html.escape(line)}</text>']
        svg += ['</g>']
    svg += ['</svg>'];(OUT/(member+'.svg')).write_text('\n'.join(svg),encoding='utf-8')
    return '\n'.join(mermaid)

def md_question(item, number):
    refs='; '.join(f'[{Path(p).name}](../../{p}) - `{method}`' for p,method in item['refs'])
    return f'### {number}. {item["q"]}\n\n{item["a"]}\n\n**Show in code:** {refs}.\n'+(f'\n**Show in browser:** {item["browser"]}.\n' if item['browser'] else '')+'\n'
def html_question(item):
    refs='; '.join(f'<a href="../../{p}">{html.escape(Path(p).name)}</a> - <code>{html.escape(method)}</code>' for p,method in item['refs'])
    return f'<article><h3>{html.escape(item["q"])}</h3><p>{html.escape(item["a"])}</p><p class="refs"><b>Show in code:</b> {refs}</p>'+ (f'<p class="refs"><b>Show in browser:</b> {html.escape(item["browser"])}</p>' if item['browser'] else '')+'</article>'
STYLE='''body{font:16px/1.6 Arial,sans-serif;color:#0f172a;background:#f1f5f9;margin:0}main{max-width:1100px;margin:auto;padding:32px}article,section{background:white;border:1px solid #e2e8f0;border-radius:12px;padding:20px;margin:16px 0}h1,h2,h3{line-height:1.25}a{color:#1e40af}code,.refs{font-size:12px;overflow-wrap:anywhere}.diagram{width:100%;height:auto}table{border-collapse:collapse;width:100%}td,th{border:1px solid #cbd5e1;text-align:left;padding:6px}.notice{background:#fff7ed;padding:16px;border-left:4px solid #b45309}@media print{@page{size:A4;margin:14mm}body{background:white;font-size:10pt}main{padding:0;max-width:none}article{break-inside:avoid;border:0;padding:0;margin:12px 0}.refs{font-size:8pt}a{color:#0f172a;text-decoration:none}.no-print{display:none}}'''

INTRO='''# DriveSpark viva pack - current code, 10 November 2026

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

**Evidence:** final `clean verify`: {{TEST_TOTAL}} tests, 0 failures, 0 errors,
0 skipped. Batch C improves shared forms and attendance controls. See [the Batch C review](../BATCH_C_REVIEW.md) for current browser and
print evidence and limits. Browser paths were checked on isolated seeded data;
not every write branch was exercised in the browser. Never perform destructive
demonstrations on live data. **NEEDS CHECK** marks unconfirmed claims.

## Downloads

- [Printable speaking guide](speaking-guide.html) and [PDF](speaking-guide.pdf)
- [Six one-page cheat sheets](cheat-sheets.html) and [PDF](cheat-sheets.pdf)
- [Individual class diagrams, printable](class-diagrams.html) and [PDF](class-diagrams.pdf)

## Project-level questions

'''
assert not BAD_REFERENCES, BAD_REFERENCES
book_md=INTRO;book_html='<h1>DriveSpark speaking guide</h1><p class="notice">Current code, 10 November 2026. Four standard OOP pillars plus exception handling as a course topic. Confirm lecturer terminology. NEEDS CHECK marks unconfirmed history or behavior.</p><p>Final clean verify: {{TEST_TOTAL}} tests, zero failures, errors or skips. Use disposable data for demonstrations.</p><h2>Project-level questions</h2>'
for i,item in enumerate(PROJECT,1):book_md+=md_question(item,i);book_html+=html_question(item)
cheats=[];diagram_html=[]
for member,m in MEMBERS.items():
    mermaid=diagram(member)
    inventory=[]
    for file in (ROOT/'src').rglob('*'):
        if file.is_file() and file.suffix in ['.java','.jsp','.jspf','.css','.js']:
            header=file.read_text(encoding='utf-8',errors='replace')[:1300]
            if re.search(r'(?:MEMBER\s*:|Module owner:)\s*'+member,header,re.I): inventory.append(file.relative_to(ROOT).as_posix())
    member_md=f'# {member}: {m["title"]}\n\n[Back to the full speaking guide](README.md). [Class diagram SVG]({member}.svg). [Mermaid source]({member}.mmd).\n\n![Class diagram]({member}.svg)\n\nSelected classes and methods are shown; shared dependencies are context, not ownership claims. Entities store foreign IDs rather than object compositions.\n\n```mermaid\n'+mermaid+'\n```\n\n'
    book_md+=f'## {member}: {m["title"]}\n\n[Individual guide and diagram]({member}.md).\n\n'
    book_html+=f'<h2>{member}: {html.escape(m["title"])}</h2>'
    for i,item in enumerate(m['questions'],1):
        member_md+=md_question(item,i);book_md+=md_question(item,i);book_html+=html_question(item)
    member_md+='## Files bearing this member header\n\n'+''.join(f'- [{p}](../../{p})\n' for p in sorted(inventory))
    (OUT/(member+'.md')).write_text(member_md,encoding='utf-8')
    diagram_html.append(f'<section class="diagram-page"><h1>{member}: {html.escape(m["title"])}</h1><img class="diagram" src="{member}.svg" alt="{member} class diagram"><p>Selected actual classes and methods. Shared dependencies are included as context. Source: {member}.mmd and linked Java files in {member}.md.</p></section>')
    keyfiles=list(dict.fromkeys([m[k] for k in ['model','repo','service','controller','contract']]+m['extra']))
    crud='; '.join(k.upper()+': '+m[k][0]+'.'+m[k][1] for k in ['create','read','update','delete'])
    oop='Encapsulation: '+m['model']+'.'+m['encap']+'; abstraction: '+m['contract']+'; inheritance: '+m['repo']+' extends FileRepository; polymorphism: '+DETAILS[member][2][2][0][0]+'.'+DETAILS[member][2][2][0][1]+'; exceptions: '+m['error'][0]+'.'+m['error'][1]
    solid='S/O: '+ '; '.join(a+'.'+b for a,b in DETAILS[member][3][2])+'; L/I/D: '+ '; '.join(a+'.'+b for a,b in DETAILS[member][4][2])
    cheats.append(f'<section class="cheat"><h1>{member}</h1><h2>{html.escape(m["title"])}</h2><p><b>Main files:</b> '+html.escape(', '.join(n+'.java' for n in keyfiles))+f'</p><p><b>CRUD:</b> {html.escape(crud)}</p><p><b>OOP locations:</b> {html.escape(oop)}</p><p><b>SOLID locations:</b> {html.escape(solid)}</p><p><b>My rule:</b> {html.escape(DETAILS[member][5][1])}</p><h3>Five-click/inspection demo</h3><p>Prepare the stated login in a disposable run; an inspection step may not need a click.</p><ol>'+''.join('<li>'+html.escape(step)+'</li>' for step in m['demo'])+'</ol><p><b>Do not bluff:</b> manual payment, no email delivery, no receipt upload, file storage, one-JVM locks, no multi-file transaction. Confirm personal Git and conflict history.</p><p>Full source paths and detailed OOP/SOLID examples: '+member+'.md. Final verification: {{TEST_TOTAL}} tests, zero failures/errors/skips.</p></section>')
book_md+='## Tricky what-if questions\n\n';book_html+='<h2>Tricky what-if questions</h2>'
for i,item in enumerate(WHATIFS,1):book_md+=md_question(item,i);book_html+=html_question(item)
book_md+='## Group questions\n\n';book_html+='<h2>Group questions</h2>'
for i,item in enumerate(GROUP,1):book_md+=md_question(item,i);book_html+=html_question(item)
BLUFF_REFS = {
'Payments':[ref('EnrollmentService','markPaid')],
'Email and receipts':[ref('EnrollmentController','payment')],
'Password recovery':[ref('PasswordController','requestReset'),ref('PasswordManagementService','createResetToken')],
'Storage and locking':[ref('FileRepository','writeRecords'),ref('AccountWriteCoordinator','execute')],
'Roles':[ref('Role','class declaration')],
'Reports':[ref('AdminReportService','business')],
'Seed and claims':[ref('DemoDataSeedRunner','seedAccounts')],
'Team history and coverage':[ref('Git plan','planned workflow')]}
book_md+='## Questions I must not bluff\n\n';book_html+='<h2>Questions I must not bluff</h2>'
for title,text in BLUFF:
    evidence='; '.join('['+Path(p).name+'](../../'+p+') - `'+method+'`' for p,method in BLUFF_REFS[title])
    book_md+='- **'+title+':** '+text+' **Show in code:** '+evidence+'.\n'
    book_html+=html_question(dict(q=title,a=text,refs=BLUFF_REFS[title],browser=''))
book_md+='\n## Repository-history evidence and limits\n\nRead-only local history contains merge subjects for PR #36 (041150f), PR #32 (392050d), and instructor-contract integration (3914174). These subjects do not prove review discussions, approvals, current PR publication or individual authorship. No Git write command was performed during this task.\n'
(OUT/'README.md').write_text(book_md,encoding='utf-8')
def html_file(name,title,body,extra=''):
    (OUT/name).write_text('<!doctype html><html lang="en"><head><meta charset="utf-8"><title>'+title+'</title><style>'+STYLE+extra+'</style></head><body><main>'+body+'</main></body></html>',encoding='utf-8')
html_file('speaking-guide.html','DriveSpark speaking guide',book_html)
html_file('cheat-sheets.html','DriveSpark - six member cheat sheets',''.join(cheats),'.cheat{break-after:page;padding:0;margin:0;border:0;border-radius:0}.cheat:last-child{break-after:auto}@media print{body{font-size:9pt;line-height:1.35}.cheat{height:267mm;box-sizing:border-box;overflow:visible}.cheat h1{font-size:20pt;margin:0 0 4mm}.cheat h2{font-size:14pt}.cheat h3{font-size:11pt}.cheat p{margin:3mm 0}.cheat ol{padding-left:5mm}}')
html_file('class-diagrams.html','DriveSpark - individual class diagrams',''.join(diagram_html),'@media print{@page{size:A4 landscape;margin:10mm}.diagram-page{break-after:page;border:0;padding:0;margin:0}.diagram-page:last-child{break-after:auto}.diagram-page h1{font-size:16pt}.diagram-page img{width:100%;height:165mm;object-fit:contain}.diagram-page p{font-size:9pt}}')
(OUT/'code-references.json').write_text(json.dumps(sorted(set(REFERENCES)),indent=2),encoding='utf-8')
print('Generated',len(PROJECT)+sum(len(m['questions']) for m in MEMBERS.values())+len(WHATIFS)+len(GROUP),'answers, 6 diagrams and 6 cheat sheets;',len(set(REFERENCES)),'source references checked.')

import xml.etree.ElementTree as ET
results=[ET.parse(p).getroot() for p in (ROOT/'target/surefire-reports').glob('TEST-*.xml')]
assert results and all(int(r.get('failures','0'))==0 and int(r.get('errors','0'))==0 for r in results)
total=sum(int(r.get('tests','0')) for r in results)
for file in OUT.iterdir():
    if file.suffix in ['.md','.html']:
        file.write_text(file.read_text(encoding='utf-8').replace('{{TEST_TOTAL}}',str(total)),encoding='utf-8')
print('Verified test total:',total)

trace="""
## Worked bubble-sort trace

Ascending example using [8, 3, 12]:

| Step | Comparison | Action | Values |
| --- | --- | --- | --- |
| Pass 1, pair 1 | 8 and 3 | Swap | 3, 8, 12 |
| Pass 1, pair 2 | 8 and 12 | Keep | 3, 8, 12 |
| Pass 2, pair 1 | 3 and 8 | Keep; no swaps in pass, stop | 3, 8, 12 |

The actual six-person descending example gives Pasindu (15), Piravarshini (12), Shahithiyan (8), Ruwini (5), Pragash (3), Sayumi (2). Worst time O(n squared), best O(n) with early exit; adjacent swaps use O(1) extra space but this method also copies the input list, so total extra space is O(n).
"""
p=OUT/'IT26101133.md';p.write_text(p.read_text(encoding='utf-8')+trace,encoding='utf-8')
trace="""
## FIFO trace

| Operation | Head to tail | Result |
| --- | --- | --- |
| enqueue B001 | B001 | head and tail are B001 |
| enqueue B002 | B001, B002 | tail becomes B002 |
| dequeue | B002 | returns B001; head becomes B002 |
| dequeue | empty | returns B002; head and tail become null |

The constructor filters pending unassigned requests, stable-sorts by createdAt, then links nodes. Assigned pending B003 is outside this queue. Processing saves assignment/rejection through BookingService; dequeue alone only changes the in-memory view.
"""
p=OUT/'IT26101134.md';p.write_text(p.read_text(encoding='utf-8')+trace,encoding='utf-8')

# All modules share these exception types through their checked repositories.
for member,m in MEMBERS.items():
    table="\n## Custom exceptions to point to\n\n| Exception | Actual source | Meaning |\n| --- | --- | --- |\n"
    pairs=[('ValidationException',m['model'],m['encap'],'Invalid input'),('DuplicateRecordException','FileRepository','save','An ID already exists; module helpers also check business duplicates'),('RecordNotFoundException','FileRepository','update','The requested stored record is missing'),('StorageException','FileRepository','readAllStrict','Damaged or unreadable storage')]
    for kind,name,method,meaning in pairs:
        path=INDEX[name].as_posix()
        table+='| '+kind+' | ['+name+'.java](../../'+path+') - `'+method+'` | '+meaning+' |\n'
    p=OUT/(member+'.md');p.write_text(p.read_text(encoding='utf-8')+table,encoding='utf-8')
extra="""\n## Enrollment CRUD alongside package CRUD\n\n| Operation | Actual method |\n| --- | --- |\n| Create unpaid request | EnrollmentService.enroll |\n| Read owned requests | EnrollmentService.ownEnrollments / ownEnrollment |\n| Update goal or confirm payment | EnrollmentService.updateLearningGoal / markPaid |\n| Delete unused unpaid request | EnrollmentService.deleteOwnEnrollment |\n\nPaid receipts and linked history are protected. Show in code: [EnrollmentService.java](../../src/main/java/com/drivingschool/learningpackage/service/EnrollmentService.java), the methods above.\n"""
p=OUT/'IT26101136.md';p.write_text(p.read_text(encoding='utf-8')+extra,encoding='utf-8')
extra="""\n## Availability and leave CRUD\n\nInstructorScheduleService has addAvailability, availability, updateAvailability and deleteAvailability. Its leave equivalents are addLeave, leaves, updateLeave and deleteLeave. Show in code: [InstructorScheduleService.java](../../src/main/java/com/drivingschool/instructor/service/InstructorScheduleService.java), those methods. Confirmed lessons protect working-hour edits and deletion. Leave is also blocked by assigned pending lessons; ask an administrator to resolve them first. Schedule writes share the existing booking/account lock.\n"""
p=OUT/'IT26101133.md';p.write_text(p.read_text(encoding='utf-8')+extra,encoding='utf-8')
