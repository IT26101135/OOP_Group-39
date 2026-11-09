# Smoke test: executed checklist

**Batch 1 retest is recorded below. It supersedes the original future-completion failure for current code; the original 33-step run remains historical evidence.**

Executed 9 November 2026, about 17:40 Asia/Colombo. HTTP requests against a fresh isolated demo at http://localhost:18080, with cookies and actual CSRF tokens; redirects followed, so recorded 200 means the final page. No browser was available: click descriptions are the equivalent manual procedure, not claimed mouse interaction. No screenshots. Only temporary demo records were changed. Stored JSON evidence was read from the demo directory, never the live ./data directory.

Use README sample password **DriveSpark123!** for all seeded accounts: admin@drivingschool.test, instructor1@drivingschool.test through instructor5@drivingschool.test, student1@drivingschool.test through student10@drivingschool.test. Students 8–9 Pending; student10 Suspended. The newly registered smoke@drivingschool.test uses that same password. Log out before switching accounts in a manual browser; the script used separate cookie jars.

Run setup: a fresh PowerShell process set DRIVESPARK_DEMO_SEED_ENABLED=true, DRIVESPARK_ADMIN_SEED_ENABLED=false, APP_DATA_DIRECTORY=C:/Users/user/AppData/Local/Temp/drivespark-review-ba109cec37cf4342b73a676190818118. Maven ran with --server.port=18080. Initial Java 11 launch failed to load RunMojo; selecting installed Java 17 in this child shell started current sources successfully. Demo variables were removed in finally when the run ended; no persistent environment settings were made. Use another fresh empty temporary folder when repeating. Dates and IDs below describe this execution: for a later repeat choose equivalent future slots and substitute generated IDs. The within-24-hour check must use a lesson less than 24 hours from the run time.

PASS means the specified HTTP/content/storage assertion succeeded; it does not certify the full visual UI. FAIL means observed behavior contradicted the stated expectation. UNVERIFIED means evidence was insufficient even though the request was attempted. The future completion check intentionally expects refusal; the actual app allowed completion, so later steps continued using that prematurely completed record. This validates downstream plumbing, not real attendance or passage of time.

| # | Log in as | What to click / enter | What you should see | PASS | FAIL | UNVERIFIED | Evidence |
|---:|---|---|---|:---:|:---:|:---:|---|
| 1 | Visitor | Open / | DriveSpark displayed | X |  |  | HTTP 200; /;  |
| 2 | Visitor | Open /packages | Demo Package displayed | X |  |  | HTTP 200; /packages;  |
| 3 | Visitor | Open /instructors | Instructor displayed | X |  |  | HTTP 200; /instructors;  |
| 4 | Visitor | Open /public/reviews | review displayed | X |  |  | HTTP 200; /public/reviews;  |
| 5 | New student | Register with name Smoke Student, smoke@drivingschool.test, 0771234567 and sample password | Account created | X |  |  | HTTP 200; /login?registered;  |
| 6 | Smoke student | Log in | Student dashboard | X |  |  | HTTP 200; /student/profile;  |
| 7 | Smoke student | Packages > Demo Package 1 > Enroll | Awaiting payment and payment instructions | X |  |  | HTTP 200; /enrollments/E010/payment; {'id': 'E010', 'studentId': 'S011', 'packageId': 'P001', 'enrolledAt': '2026-11-09T17:40:02.3866091', 'amount': 12500.0, 'paymentStatus': 'UNPAID', 'status': 'AWAITING_PAYMENT', 'learningGoal': None, 'validUntil': None, 'cancellationReason': None, 'referenceCode': 'ENR-E010'} |
| 8 | Smoke student | My enrollments > Payment instructions | Reference and payment contact | X |  |  | HTTP 200; /enrollments/E010/payment;                         Demo Package 1                Awaiting payment                                   Amount  LKR 12500.00                     Reference code  ENR-E010                                                                                 Pay by bank transfer or at our office, then email your payment receipt and reference code to  admin@drivespark.test .                       Contact the school for bank details. Lessons become available after an administrator confirms payment. Your p |
| 9 | Smoke student | Try sending a booking while unpaid | Blocked; no booking saved | X |  |  | HTTP 400; /bookings;  |
| 10 | Admin | Log in > Dashboard | Awaiting payment card | X |  |  | HTTP 200; /admin/dashboard;  |
| 11 | Admin | Awaiting payment > E010 > Mark paid | Paid; 9 lessons granted | X |  |  | HTTP 200; /admin/enrollments;  |
| 12 | Smoke student | My enrollments | Paid visible | X |  |  | HTTP 200; /enrollments;  |
| 13 | Smoke student | Book / My bookings > I001 > 16 Nov 2026 > Find available times | Available hour choices | X |  |  | HTTP 200; repeat GET /bookings?instructorId=I001&date=2026-11-16; actual time input values: ['2026-11-16T08:00', '2026-11-16T09:00', '2026-11-16T10:00', '2026-11-16T11:00', '2026-11-16T12:00', '2026-11-16T13:00', '2026-11-16T14:00', '2026-11-16T15:00', '2026-11-16T16:00']; initial option-only parser was incorrect. |
| 14 | Smoke student | Select 2026-11-16T11:00 > Send request | Pending request | X |  |  | HTTP 200; /bookings; {'id': 'B016', 'studentId': 'S011', 'enrollmentId': 'E010', 'startTime': '2026-11-16T11:00:00', 'endTime': '2026-11-16T12:00:00', 'instructorId': None, 'vehicleId': None, 'status': 'PENDING', 'createdAt': '2026-11-09T17:40:03.197834', 'rejectionReason': None, 'requestedInstructorId': 'I001', 'requestedVehicleId': None} |
| 15 | Smoke student | Read request waiting-line position | Queue position visible |  |  | X | HTTP 200; /bookings; Booking B016; original probe checked booking ID and generic queue wording, not numeric position. Numeric position for B016 was not retained; do not claim proof. |
| 16 | Admin | Schedule > assign FIFO head B001 to I001 and maintenance vehicle V005 (direct HTTP because UI filters it) | Rejected; vehicle not assigned | X |  |  | HTTP 400; /admin/bookings/assign; V005 MAINTENANCE; B001 vehicleId=None |
| 17 | Admin | Schedule > process older FIFO heads, then assign B016 to I001/V001 | Pending, assigned to instructor | X |  |  | HTTP 200; /admin/bookings; {'id': 'B016', 'studentId': 'S011', 'enrollmentId': 'E010', 'startTime': '2026-11-16T11:00:00', 'endTime': '2026-11-16T12:00:00', 'instructorId': 'I001', 'vehicleId': 'V001', 'status': 'PENDING', 'createdAt': '2026-11-09T17:40:03.197834', 'rejectionReason': None, 'requestedInstructorId': 'I001', 'requestedVehicleId': None} |
| 18 | Smoke student | My bookings after admin assignment | Awaiting instructor acceptance | X |  |  | Independent student1 GET /bookings HTTP 200 after assignment: B001 2026-11-10 09:00 to 10:00 PENDING Enrollment E001 Assigned - waiting for instructor approval. Instructor I001 - Vehicle V001. This verifies the stage using B001, not B016. |
| 19 | Instructor 1 | Log in > Assigned lessons | Assigned request B016 | X |  |  | HTTP 200; /instructor/dashboard;  |
| 20 | Instructor 1 | Accept B016 | Confirmed | X |  |  | HTTP 200; /instructor/dashboard; {'id': 'B016', 'studentId': 'S011', 'enrollmentId': 'E010', 'startTime': '2026-11-16T11:00:00', 'endTime': '2026-11-16T12:00:00', 'instructorId': 'I001', 'vehicleId': 'V001', 'status': 'CONFIRMED', 'createdAt': '2026-11-09T17:40:03.197834', 'rejectionReason': None, 'requestedInstructorId': 'I001', 'requestedVehicleId': None} |
| 21 | Instructor 1 | Complete B016 (future lesson, validation probe) | Reject completion before lesson occurs |  | X |  | HTTP 200; /instructor/dashboard; Actual status=COMPLETED |
| 22 | Smoke student | Dashboard > lessons left | 9 becomes 8 after completion | X |  |  | HTTP 200; /student/profile; Stored balance=8 |
| 23 | Smoke student | Progress | Completed lesson counted | X |  |  | HTTP 200; /student/progress; Stored completed booking B016 |
| 24 | Smoke student | Reviews > completed B016 > 5 stars > submit | Review awaiting moderation | X |  |  | HTTP 200; /reviews; {'id': 'R011', 'studentId': 'S011', 'bookingId': 'B016', 'instructorId': 'I001', 'rating': 5, 'comment': 'Smoke test clear patient teaching.', 'status': 'PENDING', 'createdAt': '2026-11-09T17:40:04.2953145', 'updatedAt': '2026-11-09T17:40:04.2953145'} |
| 25 | Admin | Reviews > Approve R011 | Approved review | X |  |  | HTTP 200; /admin/reviews; {'id': 'R011', 'studentId': 'S011', 'bookingId': 'B016', 'instructorId': 'I001', 'rating': 5, 'comment': 'Smoke test clear patient teaching.', 'status': 'APPROVED', 'createdAt': '2026-11-09T17:40:04.2953145', 'updatedAt': '2026-11-09T17:40:04.450832'}; assertion corrected: status field is APPROVED, not boolean approved. |
| 26 | Visitor | Instructors > Instructor 1 | Rating changes after approval | X |  |  | HTTP 200 /instructors; I001 stored averageRating changed from 2.0 before approval to 3.5 after approval. |
| 27 | Admin | Reports | Revenue and booking reports | X |  |  | HTTP 200; /admin/reports;  |
| 28 | Admin | Reports > Export CSV | CSV sections downloaded | X |  |  | HTTP 200; /admin/reports/export.csv; report,label,value  "Enrollments per package","P001","5"  "Enrollments per package","P002","1"  "Enrollments per package","P003","1"  "Enrollments per package","P004","1"  "Enrollm |
| 29 | Smoke student | Open /admin/dashboard in address bar | Access denied 403 | X |  |  | HTTP 403; /admin/dashboard;  |
| 30 | Student 1 | Log in with wrong password | Error; no dashboard | X |  |  | HTTP 401; /login;  |
| 31 | Student 10 (Suspended) | Log in with correct sample password | Suspended explanation | X |  |  | HTTP 401; /login;  |
| 32 | Student 1 | Cancel B001 on 10 Nov at 09:00 (within 24 hours of this run) | 24-hour refusal; booking unchanged | X |  |  | HTTP 400; /bookings/cancel; Actual status=PENDING |
| 33 | Admin | Open /setup-check | Setup checks displayed | X |  |  | HTTP 200; /setup-check; Setup page is not a full health monitor |

## Final summary

| Total steps | Passed | Failed | Unverified |
|---:|---:|---:|---:|
| 33 | 31 | 1 | 1 |

Failures ranked by severity:

1. **High — step 21:** instructor completed future B016 (16 November) on 9 November; HTTP 200, persisted COMPLETED, balance 9 → 8. BookingService.completeAssignedLesson and Booking.complete only check status, not lesson end time. The same omission exists in admin completion. Smallest fix: require the lesson to have ended before completion; define an attendance time gate for no-show.

Evidence limitations:

- Step 15 was attempted but its numeric B016 position was not captured. A later student1 response explicitly displayed B015 “FIFO position 2”, which confirms the page can render numeric queue positions, but does not recover the original B016 observation.
- Step 18 uses a separate assigned B001 observation to prove the intermediate state; it does not pretend the original loose B016 assertion captured exact wording.
- Initial script assertions incorrectly searched option tags for radio slots and an approved boolean instead of status. Reinspection found actual time radio values and APPROVED; these were corrected as test-script errors, not product failures.
- Wrong-password response was HTTP 401 with password wording; suspended login was HTTP 401 with suspension wording. Maintenance assignment was HTTP 400 and left B001 vehicleId null. Cancellation within 24 hours was HTTP 400 and left B001 Pending. Full error wording and JavaScript presentation were not retained for these probes.
- Report CSV returned HTTP 200 with header report,label,value and Revenue section. Browser download behavior, print, animations, accessibility, mobile layout, dialog behavior and visual success-toast display remain UNVERIFIED.
- Report checks show sections exist, not that every aggregate was independently reconciled. Revenue implementation was inspected separately in BUSINESS_LOGIC_REVIEW.md.
- HTTP role guard covers student → /admin/dashboard, not every protected URL. This is a smoke checklist, not a complete authorization audit.

Recommended visibility improvements (recommendations only):

1. Admin Needs attention panel linking awaiting payments, unassigned queue, pending instructor approvals, fleet warnings and unmoderated reviews.
2. Setup/storage status card with last successful read/write or error.
3. Student package name, validity and usable-status badge beside balance.
4. A What happens next box after enrollment, payment confirmation and booking submission.
5. Exact 24-hour cutoff and actionable blocked-state messages beside cancellation/reschedule controls.

No source changes were made. Runtime was stopped after testing; temporary data and test scripts remain under the temporary directory for inspection. Approval is required before any implementation work.


## Batch 1 retest — 9 November 2026, 17:59–18:02 Asia/Colombo

Scope: attendance time gates and commitment cap only. No browser tool was available; all recorded evidence is HTTP with cookie sessions/CSRF and read-only inspection of isolated JSON records. Mouse interaction, toast rendering, dialogs and visual layout remain **UNVERIFIED**.

The demo app was stopped before building. `npm run build:css` succeeded (Done in 450ms; existing Browserslist outdated-data warning). With Java 17, `.\mvnw.cmd clean verify` returned:

```text
[INFO] Tests run: 672, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  01:14 min
[INFO] Finished at: 2026-11-09T17:59:20+05:30
```

Log: C:/Users/user/AppData/Local/Temp/drivespark-batch1-verify.log. Expected IOException error logs came from storage-error tests; the actual aggregate has zero errors/failures. WAR packaging and repackage both succeeded. The CSS command regenerated identical tracked CSS; it did not produce a content diff.

Two fresh PowerShell app processes launched the verified WAR with Java 17 on port 18080. Both set DRIVESPARK_DEMO_SEED_ENABLED=true, DRIVESPARK_ADMIN_SEED_ENABLED=false and APP_DATA_DIRECTORY for that run only. The ON run omitted the attendance override, proving the default true. The OFF run set DRIVESPARK_ATTENDANCE_REQUIRE_LESSON_ENDED=false. Each process cleared its demo variables in finally after stopping; both emitted “Demo variables cleared”. Both apps are stopped.

Temporary data roots: C:/Users/user/AppData/Local/Temp/drivespark-batch1-on and drivespark-batch1-off. Each was new and isolated from ./data. The script registered batch@drivingschool.test (DriveSpark123!, phone 0771234567), enrolled in P001 and admin marked E010 Paid before step 14. S011 began with 9 credits. Sample admin/instructor accounts and passwords are unchanged from README. Both runs used B016 at 16 November 11:00–12:00 and processed earlier FIFO heads first. Repeat on another date with a suitable future slot and new temporary folder.

For manual repetition use the original checklist's clicks for steps 14–24 with these generated IDs. In ON mode, step 21 must refuse future completion; steps 22–24 below verify unchanged balance, no false progress and no review. The original completed-lesson expectations for 22–24 are exercised in OFF mode. No waiting until lesson end is claimed. Actual ended-lesson success and start/end boundaries are covered by automated tests with explicit times.

| Step | Setting | Expected behavior | Result | Evidence |
|---|---|---|---|---|
| 14 | on | New Pending request | PASS | 200 /bookings; B016 status=PENDING |
| 15 | on | Numeric FIFO position | PASS | 200 /bookings; FIFO position 4 |
| 16 | on | Maintenance vehicle refused | PASS | 400 /admin/bookings/assign; Choose a vehicle that is in service. Back to the form Go to my dashboard Back to home  |
| 17 | on | FIFO assignment remains Pending | PASS | 200 /admin/bookings; B016 PENDING I001/V001 |
| 18 | on | Intermediate state explained | PASS | 200 /bookings; Assigned - waiting for instructor approval. |
| 19 | on | Instructor sees assigned request | PASS | 200 /instructor/dashboard; B016 in dashboard HTML |
| 20 | on | Instructor confirms | PASS | 200 /instructor/dashboard; B016 CONFIRMED |
| 21 | on | Future completion refused with time | PASS | 400 /instructor/complete; This lesson has not ended yet. Completion becomes available at 2026-11-16 12:00 (Asia/Colombo). Back to the form Go to my dashboard Back to home  |
| 22 | on | Balance stays 9 when refused | PASS | 200 /student/profile; lessonsRemaining=9; dashboard LESSONS REMAINING present=True |
| 23 | on | No false progress | PASS | 200 /student/progress; completed bookings=0; progress HTTP 200 |
| 24 | on | Review blocked for uncompleted lesson | PASS | 400 /reviews; review=None |
| A1 | on | Future no-show refused with start time | PASS | 400 /instructor/no-show; This lesson has not started yet. No-show becomes available at 2026-11-16 11:00 (Asia/Colombo). Back to the form Go to my dashboard Back to home  |
| A2 | on | Admin future completion also refused | PASS | 400 /admin/bookings/complete; status=CONFIRMED |
| C1 | on | Commitment cap refuses extra request | PASS | 400 /bookings; You have 9 lessons left and 9 already requested |
| 14 | off | New Pending request | PASS | 200 /bookings; B016 status=PENDING |
| 15 | off | Numeric FIFO position | PASS | 200 /bookings; FIFO position 4 |
| 16 | off | Maintenance vehicle refused | PASS | 400 /admin/bookings/assign; Choose a vehicle that is in service. Back to the form Go to my dashboard Back to home  |
| 17 | off | FIFO assignment remains Pending | PASS | 200 /admin/bookings; B016 PENDING I001/V001 |
| 18 | off | Intermediate state explained | PASS | 200 /bookings; Assigned - waiting for instructor approval. |
| 19 | off | Instructor sees assigned request | PASS | 200 /instructor/dashboard; B016 in dashboard HTML |
| 20 | off | Instructor confirms | PASS | 200 /instructor/dashboard; B016 CONFIRMED |
| 21 | off | Explicit demo override permits completion | PASS | 200 /instructor/dashboard; B016 COMPLETED |
| 22 | off | Balance falls 9 to 8 | PASS | 200 /student/profile; lessonsRemaining=8; dashboard LESSONS REMAINING present=True |
| 23 | off | Completed lesson counted | PASS | 200 /student/progress; completed bookings=1; progress HTTP 200 |
| 24 | off | Completed lesson review saved Pending | PASS | 200 /reviews; review={'id': 'R011', 'studentId': 'S011', 'bookingId': 'B016', 'instructorId': 'I001', 'rating': 5, 'comment': 'Batch one smoke test feedback.', 'status': 'PENDING', 'createdAt': '2026-11-09T18:01:34.9350324', 'updatedAt': '2026-11-09T18:01:34.9350324'} |
| C1 | off | Commitment cap refuses extra request | PASS | 400 /bookings; You have 8 lessons left and 8 already requested |

| Retest scope | Passed | Failed | Unverified |
|---|---:|---:|---:|
| Steps 14–24, ON | 11 | 0 | 0 |
| Steps 14–24, OFF | 11 | 0 | 0 |
| Extra HTTP checks A1/A2/C1 | 4 | 0 | 0 |
| Browser rendering / interaction | 0 | 0 | Not exercised |

A1 is no-show before start; A2 is admin completion before end; C1 fills real credits through ordinary student requests and attempts one extra non-overlapping request. The cap remains active in the demo override. No live data was edited. Raw HTTP assertion results are retained at C:/Users/user/AppData/Local/Temp/batch1-results-on.json and batch1-results-off.json; script at batch1_smoke.py. Progress evidence above proves persisted completed-booking count plus successful progress response, not a pixel-level check of the displayed counter.

### Every test changed, and why

There were **no existing successful future-completion tests**. The three existing successful model tests used 1 November 2026 (already past at this review). They now pass an explicit end time, removing dependence on the date the tests happen to run:

| Test file | Existing test changed | Reason |
|---|---|---|
| booking/model/BookingTest.java | shouldCompleteConfirmedBooking | Complete exactly at the end boundary; deterministic ended-lesson success |
| booking/model/BookingTest.java | shouldRejectCancellingCompletedBooking | Create its Completed fixture using explicit end time before testing cancellation refusal |
| booking/model/BookingTest.java | shouldPreserveCompletedBookingAfterJsonRoundTrip | Create its Completed fixture using explicit end time before testing persistence |

Existing `SchedulingWebIntegrationTest.instructorAndCompletionWritesRequirePaidReceipt` attempts future admin/instructor completion and no-show but expects payment rejection; left unchanged to preserve that rule. Review tests construct historical status fixtures directly and do not invoke completion; left unchanged. No existing tests were globally switched to demo mode.

New tests (9):

| Test file | Test added | Purpose |
|---|---|---|
| BookingTest | refusesFutureCompletionWithoutChangingState | One second before end refused, time in message, Confirmed preserved |
| BookingTest | demoSettingAllowsFutureCompletion | Explicit false permits future completion |
| BookingTest | noShowStartsAtLessonStartAndDemoCanBypassTime | Before start refused, exact start allowed, false bypass allowed |
| BookingServiceCrudTest | capsRequestsAndCountsConfirmedLessons | Simpler request overload caps commitments; Confirmed counts; cancellation frees room |
| SchedulingWebIntegrationTest | futureAttendanceIsRefusedForAdminAndInstructor | Both completion entry points and no-show refuse future attendance; no debit |
| SchedulingWebIntegrationTest | endedLessonCanBeCompleted | Admin completion of ended lesson debits once; duplicate rejected |
| SchedulingWebIntegrationTest | demoSettingAllowsInstructorToCompleteFutureLesson | Service respects false and debits once; setting restored in finally |
| SchedulingWebIntegrationTest | requestCapAllowsReplacementButRefusesExtraRequest | Slot-based request capped; replacement allowed at exact cap |
| SchedulingWebIntegrationTest | overCapAssignmentAndRescheduleAreRefused | Legacy over-cap data cannot be assigned or replaced; old request unchanged |

The integration tests use real temporary repositories; no production demo data is used by the tests. Total rose from 663 to 672. Relevant test files are under src/test/java/com/drivingschool/.

### Files changed in Batch 1

1. src/main/java/com/drivingschool/booking/model/Booking.java — guarded attendance transitions, retaining no-argument entry points with safe defaults.
2. src/main/java/com/drivingschool/booking/service/BookingService.java — configured gate passed with school Clock; commitment count under existing shared lock.
3. src/main/resources/application.properties — attendance setting, default true, run-only environment override.
4. src/test/java/com/drivingschool/booking/model/BookingTest.java — explicit times and model boundary tests.
5. src/test/java/com/drivingschool/booking/service/BookingServiceCrudTest.java — request commitment tests.
6. src/test/java/com/drivingschool/booking/SchedulingWebIntegrationTest.java — attendance and slot-based cap tests.
7. README.md — setting, safe demo command and changed booking behavior.
8. docs/SMOKE_TEST.md — this evidence and test inventory.
9. docs/VIVA_QA.md — explanations and current verification result.

No Batch 2/3 changes. The pre-existing home/index.jsp modification and other pre-existing documentation were left untouched. No route or form-field rename, payment-confirmation change, queue algorithm change, bubble-sort change or unused-contract edit. Existing member headers and labels were retained. Remaining business review findings are not declared fixed by this batch.
