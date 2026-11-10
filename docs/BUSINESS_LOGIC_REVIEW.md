# Business logic and role clarity review

> 2026-11-09 cleanup update: the unused instructor interval/lookup family, vehicle lookup family and enrollment lookup/summary were removed with member approval. Earlier descriptions of those contracts are historical, not current APIs. See [Java simplification record](JAVA_SIMPLIFICATION.md) for retained OOP evidence and verification.

Reviewed 9 November 2026. No source edits or Git mutations. Evidence combines source inspection and an isolated HTTP run at localhost:18080 with Java 17. See SMOKE_TEST.md for the 33-step execution record. Browser tools were unavailable: visual layout, actual click interaction, JavaScript toasts/dialogs and mobile navigation are UNVERIFIED. Click counts below are desktop link paths inferred from rendered HTML/JSP, excluding typing and login; they are not timed browser observations. Mobile menus usually add one click.

Source paths below are relative to src/main/java/com/drivingschool/ unless prefixed views/ (src/main/webapp/WEB-INF/views/). Method names identify the implementation evidence, not just a matching filename. “Student expectation” is the ordinary meaning of the action, not an invented school policy.

## State flows and lesson accounting

| Transition / actor | Implementation and actual rule | lessonsRemaining | Student expectation |
|---|---|---|---|
| Request enrollment / student | learningpackage/service/EnrollmentService.enroll, ensureCanEnroll: active package required; saves price snapshot, UNPAID/AWAITING_PAYMENT, no validity; one awaiting request or active package prevents another | Unchanged | Payment still needed; no lessons yet |
| Awaiting → Paid / admin | EnrollmentService.markPaid, activateLessons: checks Active student and active package; sets validity to confirmation date + current package validityDays; grants current package lessonCount; repeat confirmation refused | Set to package count | Payment unlocks advertised course |
| Awaiting → Cancelled / admin | EnrollmentService.cancelRequest: keeps receipt, rejects linked pending legacy bookings; Paid cannot be cancelled by this operation | Unchanged | Reason and payment history retained |
| Awaiting deletion / owning student | EnrollmentService.deleteOwnEnrollment, ensureDeletable: unpaid, non-active and no booking links only; cancelled receipts cannot be deleted | Unchanged | Withdrawing unpaid request is possible, although labelled Delete |
| Paid validity expires | booking/service/PaidEnrollmentGuard.packageProblem compares validUntil against TODAY, not lesson date; null legacy validity accepted; does not change status from Paid | Remains stored | Expired credit should be clearly distinguished from usable credit |
| Request booking / student | booking/service/BookingService.requestLesson: paid guard, generated slot, current package, conflict checks; saves unassigned Pending | No debit or reservation of balance | Request joins FIFO |
| Assign / admin | BookingService.assign/processNext; booking/model/Booking.assignForInstructor: FIFO head only; stores instructor/vehicle, remains Pending; drops out of unassigned FIFO | Unchanged | Waiting for instructor approval |
| Confirm / assigned instructor | BookingService.acceptAssignedLesson; Booking.confirm: checks paid guard/conflicts, Pending → Confirmed | Unchanged | A promised lesson |
| Reject / admin processing or assigned instructor decline | BookingService.assign rejects payment/conflict failures; declineAssignedLesson → Booking.reject; Pending only, reason required | Unchanged | Reason and ability to request another slot |
| Cancel / owner | BookingService.cancelOwnBooking → Booking.cancel: Pending/Confirmed only; at least configured 24 hours before start | Unchanged | No lesson lost; there was no earlier debit |
| Force cancel / admin | BookingService.forceCancel → Booking.forceCancel: reason required; Completed/Cancelled refused, but Rejected/No-show can become Cancelled | Unchanged | Administrative exception with reason |
| Reschedule / owner | BookingService.rescheduleOwnBooking: same notice rule, paid guard, slot/conflict recheck; saves NEW Pending ID/createdAt then cancels old | Unchanged | New queue position; UI explicitly warns |
| Reassign / admin | BookingService.reassign → Booking.reassign: Confirmed only, rechecks paid/conflicts; changes resources while remaining Confirmed | Unchanged | New instructor is now promised, without another acceptance |
| Complete / admin or assigned instructor | BookingService.complete/completeAssignedLesson → Booking.complete: Confirmed only; saves booking before decrementPackageLessons updates account | Minus one | Consume one delivered lesson |
| No-show / assigned instructor | BookingService.markNoShowAssignedLesson → Booking.markNoShow: Confirmed only, paid guard; saves NO_SHOW, no decrement call | Unchanged | Policy must explain whether absence consumes credit |

Runtime proof: E010 became Paid, S011 received 9 lessons, B016 went Pending → assigned Pending → Confirmed → Completed, balance became 8, R011 was approved and I001 rating changed 2.0 → 3.5. Completion happened a week before the lesson: a real defect, not proof of attendance.

## Direct answers with evidence

| Question | Yes / No / Partly and evidence | Student expectation / implication |
|---|---|---|
| Double approval? | **Yes.** BookingService.assign then acceptAssignedLesson. views/booking/request-card.jspf says “Assigned - waiting for instructor approval.” HTTP student1 B001 displayed that exact text. Before assignment it displays FIFO position and “waiting for assignment.” | Two stages are visible; submission success could state both stages |
| Cancel gives lesson back? | **Partly.** Balance stays the same; request/confirm never spent it. BookingService.cancelOwnBooking never credits account. | No balance loss, no artificial extra credit |
| No-show uses a lesson? | **No.** markNoShowAssignedLesson has no decrement; completeAssignedLesson does. | Publish the absence policy; do not describe this as a deduction |
| Reschedule loses queue position? | **Yes.** New ID and now-createdAt in rescheduleOwnBooking. views/booking/request-card.jspf warns before action. | A fresh request joins the back |
| Unpaid requests expire? | **No.** EnrollmentService.ensureCanEnroll/awaitingPayments/requireAwaiting have no age rule or timeout; no expiry processing found in main code. | They remain until deleted or handled; explain manual cancellation |
| Expired package blocked? | **Partly.** PaidEnrollmentGuard.packageProblem blocks when receipt expired as of today, inclusive validUntil; it does not check requested lesson date. | A future lesson after expiry can still be requested today |
| Revenue uses saved price? | **Yes.** report/service/AdminReportService.revenue sums Enrollment.getAmount for Paid receipts, not current package price. HTTP report and CSV returned Revenue section. | Historical receipts retain price |
| Leave over confirmed booking? | **Blocked.** instructor/service/InstructorScheduleService.addLeave/updateLeave → validateLeave → ensureDayHasNoConfirmedLessons; message says reschedule first. Assigned Pending is not protected. | No silent cancellation of a confirmed appointment |
| Which availability store? | **AvailabilityRepository**, availability.txt: SlotAvailabilityService.availableSlots and BookingConflictChecker.isWorking read weekly Availability blocks. InstructorScheduleService writes them. | Instructor’s weekly hours control offered slots |
| InstructorAvailabilityService used? | **No operational caller found.** Constructed by instructor/config/InstructorConfig.instructorAvailabilityService; separate InstructorAvailabilityRepository/instructor-availability.txt date-time store, not read by slot generator. | Do not claim this second service drives bookings |
| EnrollmentLookup / InstructorLookup used? | **No main feature consumer found.** EnrollmentLookup is only an interface; InstructorLookupService implements InstructorLookup and InstructorConfig creates it, but main controllers/services do not consume it. | Explain these as unused abstractions |
| Anyone reads ContactMessage? | **No inbox consumer found.** common/service/ContactService.submit validates and saves via ContactMessageRepository; repository infrastructure reads for IDs/storage, but no controller lists messages. | Sending a message does not place it in an admin inbox |
| Pending account status still used? | **Yes.** auth/service/AuthSessionService.loginWithResult blocks Pending; StudentAdministrationService.setStatus accepts it, admin UI offers it. README demo students 8–9 Pending. New registration is Active. | Registering students need no approval, imported/admin Pending accounts do |

### Linked deletion, suspension and deactivation

| Entity | Code behavior and method evidence | Student expectation / consequence |
|---|---|---|
| Student | StudentAccountService.deleteOwnAccount and StudentAdministrationService.delete refuse any enrollment, booking or review link. setStatus suspends without removing links; AuthSessionService.getCurrentUser reloads status and invalidates non-Active sessions; PaidEnrollmentGuard blocks subsequent lesson operations | History kept; suspension does not automatically cancel booked appointments |
| Instructor | InstructorAccountService.delete refuses assigned OR requested booking references and removes profile/login only when safe. InstructorService.setInstructorActive changes profile only; existing bookings persist. BookingConflictChecker rejects inactive instructor assignments/acceptance; getInstructorBookings/completeAssignedLesson reject inactive profile | Admin must resolve existing lessons; deactivation is distinct from login account suspension |
| Package | LearningPackageService.deletePackage refuses enrollment references and legacy package-ID booking references. Deactivation retains history; PaidEnrollmentGuard.packageProblem blocks future actions, including completing existing lessons | Existing paid students can become blocked when course is deactivated; confirm school policy |
| Vehicle | VehicleService.deleteVehicle refuses assigned historical booking links. MAINTENANCE is excluded by AdminBookingController.availableVehicles and refused by BookingService.assign/conflict checks. Changing status retains existing bookings | A maintenance change is not automatic reassignment; admin must handle affected lessons |

## Real problems, severity and smallest safe fixes

These are code findings unless explicitly marked runtime reproduced. No fixes were applied. High means a wrong result in an ordinary flow; Medium an edge case; Low cosmetic/unused. A policy question is not automatically a bug.

| Severity | File / method | What code does versus expectation | Smallest safe fix |
|---|---|---|---|
| High | booking/service/BookingService.complete, completeAssignedLesson; Booking.complete | Runtime reproduced: 16 Nov lesson completed on 9 Nov, consuming credit and enabling review/progress for undelivered teaching. Neither method checks end time. | Require now >= endTime before completion; same rule for no-show at appropriate attendance time |
| High | EnrollmentService.ensureCanEnroll; BookingService.decrementPackageLessons; UserRepository.hasPaidReceipt | Exhausted/expired Paid enrollment keeps activePackageId. No normal renewal transition found: ensureCanEnroll always refuses a new package while it is non-null. Student expects to buy more lessons. | Permit renewal when exhausted/expired after resolving open bookings; preserve old receipt/history |
| High | BookingService.requestLesson, completeAssignedLesson; PaidEnrollmentGuard.matchesBalance | Positive balance is checked but outstanding requests/confirmed lessons are not counted. One remaining credit can support multiple non-overlapping promises. Completing one leaves others blocked by requirePaid. | Cap outstanding commitments to remaining credits, consistently inside booking/account lock |
| Medium | PaidEnrollmentGuard.packageProblem | Checks today's date, allowing a lesson after validUntil; later acceptance/completion may fail once expiry passes. | Check lesson date against validity on request/assignment and define treatment of previously confirmed lessons |
| Medium | BookingService.complete/completeAssignedLesson | Booking saved Completed before account debit; two-file write failure can leave inconsistent history/balance. | Validate debit before saving; add recoverable journal/idempotent reconciliation for interrupted writes |
| Medium | InstructorScheduleService.deleteAvailability | Unlike updateAvailability, deletion does not call ensureBlockHasNoConfirmedLessons. Hours can be removed under confirmed lessons. | Apply same confirmed-lesson protection before deletion |
| Medium | InstructorScheduleService.validateLeave; BookingService.acceptAssignedLesson | Leave protects Confirmed only. Adding leave over assigned Pending succeeds, then acceptance fails. | Warn about pending assignments and resolve them before adding leave, or block explicitly |
| Medium | common/service/ContactService.submit | Saves message but no admin reader. Visitor expects school staff to receive it. | Provide an inbox or state explicitly that form is demonstration-only; do not promise delivery |
| Low | StudentAccountService.deleteOwnAccount; StudentAdministrationService.delete | Enrollment-only link blocks deletion but error mentions only lesson/review history. | Include enrollment/payment history in error |
| Low | InstructorAvailabilityService; EnrollmentLookup; InstructorLookupService/InstructorLookup | Unused feature paths / parallel availability store confuse maintenance and viva claims. | Remove unused contracts/store after checking compatibility, or clearly document legacy role |
| Low | views/student/profile.jsp | Shows current package ID rather than friendly package name; balance can look usable despite expiry/inactivity | Show name, validity and usable/blocked badge with next-step link |

Do not count corrected test-script assertions as app bugs: slot choices are radio inputs, and reviews use status=APPROVED rather than a boolean approved field.

### Known limitations to explain honestly in the viva

Manual payment confirmation is not gateway settlement; payment page asks students to contact school for bank details and email a receipt. No email delivery, transaction database, audit trail or pagination is supplied (README). File persistence and JVM locks do not provide cross-process transactions. Legacy Paid receipts with null validity are accepted intentionally by PaidEnrollmentGuard. FIFO applies to unassigned requests, not all Pending records. Rescheduling creates a new request. Cancellation does not credit lessons because debit occurs at completion. No-show currently costs zero credits; decide policy before changing it. Admin reassign preserves confirmation without new-instructor acceptance: a deliberate admin override only if the school accepts that rule. A setup page is not production monitoring. These limitations must not be presented as implemented capabilities.

## Role usability and clarity

Evidence: role navigation in views/common/navbar.jspf and dashboard-sidebar.jspf; student/profile.jsp, student/enrollments.jsp, booking/list.jsp and request-card.jspf; instructor/lesson-dashboard.jsp and availability.jsp; admin dashboard/enrollments/bookings/reviews/reports pages. Main journey pages returned HTTP 200. Additional role pages /instructor/availability, /admin/students, /admin/packages and /admin/vehicles returned 200. Actions not in the smoke checklist were reviewed in code only; their interactive behavior is UNVERIFIED. Counts are navigation clicks to reach the task, not every form submission. “Clear” judges rendered wording, not visual accessibility.

### Student

| Task | Clicks | Clarity | Confusion / feedback and smallest fix |
|---|---:|---|---|
| Register | 1 from public menu | Clear | Form labels and register-to-login redirect exist; browser password controls unverified |
| Log in | 1 | Clear | Wrong password and Suspended account returned 401 with explanation |
| Find package | 1 catalog, 2 detail | Clear | Named prices available; no change needed to navigation |
| Enroll | 2 to detail, then Enroll | Clear | Redirect to reference/payment page; describe the two subsequent approval stages |
| Payment instructions | 1 from enrollments | Confusing | Bank details absent, contact required; add actual school payment details before real use |
| Check enrollment status | 1 via top nav/dashboard link | Clear | Paid/Awaiting payment in list; add matching dashboard status rather than only package ID |
| Book lesson | 1 to page, then find times/send | Clear | Three instructions, slot radios, success redirect; state both admin assignment and instructor approval in success text |
| Queue position | 1 to bookings | Clear | FIFO position for unassigned Pending; assigned stage explicitly explained; show pending count on dashboard |
| Cancel | 1 to bookings, expand Change this request | Confusing | Notice text says advance notice, not exact hours in card; show “24 hours” and disable blocked action with reason |
| Reschedule | 1 plus expand | Confusing | Warns queue resets, but requires manual datetime rather than free-slot picker; reuse available-time selection |
| Progress | 1 | Clear | Completed count/hours and feedback; empty state should link to booking when nothing recorded |
| Lessons left | 0 on dashboard | Confusing | Stored balance shown without usable validity context; add expiry/inactive reason and package name |
| Leave review | 1 to Reviews | Clear | Completed-booking selector and moderation status; runtime created Pending then Approved |
| Understand blocked booking | 1 | Clear | bookingProblem and My enrollments link exist; renewal dead end remains a real business problem |

### Instructor

| Task | Clicks | Clarity | Confusion / feedback and smallest fix |
|---|---:|---|---|
| Log in | 1 public | Clear | Dashboard reached with sample account |
| Today's lessons | 0 dashboard | Confusing | Assigned list and lesson counters exist; add an explicit Today filter/link to avoid scanning history |
| Pending requests | 0 dashboard / 1 Assigned lessons anchor | Clear | Assigned Pending shown with actions; distinguish unassigned queue from instructor workload in counts |
| Set availability | 1 | Clear | Weekly hours form/list; render impact warning before deleting hours |
| Add leave | 1 same page | Confusing | Conflict message says reschedule confirmed lessons, but instructor cannot directly perform admin reassignment; link to contact admin/instructions |
| Accept / decline | 0 dashboard plus action | Clear | Assigned IDs/statuses and reason field; success flashes in controller; actual accept verified, decline unverified |
| Mark attendance | 0 dashboard plus action | Confusing | Complete/no-show allow premature recording; hide/disable until appropriate time and explain |
| Add notes and skill ratings | 0 after completion | Clear | Completed-only rule and labelled skill inputs; submission interaction unverified |
| See own reviews | 0 dashboard or 1 Reviews | Clear | Rating and own reviews on dashboard; repeated route is a shortcut, not a broken button |

### Admin

| Task | Clicks | Clarity | Confusion / feedback and smallest fix |
|---|---:|---|---|
| Today's attention | 0 dashboard | Confusing | Awaiting payment and pending totals present; no combined dated list of overdue queue, fleet warnings and unmoderated review count; add linked needs-attention panel |
| Confirm payment | 1 Awaiting payment | Clear | Action changes receipt and grants lessons; manual verification responsibility should remain explicit |
| Process FIFO queue | 1 Schedule | Clear | Queue/head and process-next exist; assigned Pending is distinct from unassigned queue; label counts separately |
| Assign | 1 Schedule | Clear | Active instructors and in-service vehicles; conflicts return reason; maintenance direct POST refused |
| Reassign | 1 Schedule plus action | Confusing | Keeps Confirmed without new acceptance; label as admin override and explain effect |
| Manage students | 1 Users | Clear | Edit/status/delete forms; change link-history error wording to include enrollment |
| Manage instructors | 1 dashboard instructor card | Confusing | Sidebar Users goes only to students; add Students / Instructors labels or both links |
| Manage packages | 1 | Clear | Edit/deactivate/delete available; warn about impact of deactivation on paid students |
| Manage vehicles | 1 | Clear | Maintenance status and warning text in list; add count/link on dashboard for warnings |
| Moderate reviews | 1 | Clear | Approve/hide controls; actual approval updated rating; add pending count on dashboard |
| Read reports / CSV | 1 Reports, 2 CSV | Clear | Tables and export returned 200; browser charts, print layout and downloaded file handling unverified |
| Check health | No menu path found | Missing | /setup-check reachable directly; add labelled setup/status link and storage last-error indicator |

Navigation repeats in desktop rail, top menu and mobile drawer; these are alternate paths, not demonstrated duplicate actions. No inert button was established by HTTP alone. Do not claim JavaScript tabs, confirmation dialog, charts, password toggles, toasts or mobile menu work without a browser test. Empty booking lists have the request form on the same page; empty leave list has Add leave day on the same page. Enrollment is absent from the student sidebar but available in top navigation/dashboard: it is not missing entirely.

Recommended small changes (do not build): (1) linked admin Needs attention panel; (2) setup/storage status card; (3) dashboard package name, validity and usable-status badge; (4) “What happens next” text after payment and booking actions; (5) show the exact cancellation cutoff and next available action on blocked controls. These improve visibility but do not repair the business defects above.
