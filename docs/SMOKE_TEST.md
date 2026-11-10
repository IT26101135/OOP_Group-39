# Smoke test: executed checklist

## Batch C current retest - 10 November 2026

This is the latest run. The app was stopped before the CSS build and clean Maven verification.
All runs used `scripts/run-demo.ps1`, a fresh shell, run-local variables cleared on shutdown,
and a fresh empty data folder. Existing `data`, `data-demo` and sample files were not used for browser mutations.

```text
[INFO] Tests run: 694, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  03:46 min
[INFO] Finished at: 2026-11-10T13:11:13+05:30
```

`npm.cmd run build:css` passed (existing Browserslist age warning only).
Three new tests cover the attendance display boundary, preview override and non-confirmed statuses.
No existing Java test changed; see the [test inventory and review](BATCH_C_REVIEW.md).

**Normal gate: 30 PASS, 0 FAIL, 3 UNVERIFIED.** Steps 22-24 require the future lesson to end.
Step 21 verifies that trying to complete it early is refused and does not debit a lesson.
Steps 25-26 moderate seeded R013 rather than pretending that step 24 created a review.
Its stored average is checked separately from the new one-decimal display.

| Step | Current check | Result |
| ---: | --- | --- |
| 1 | Visitor / | PASS |
| 2 | Visitor /packages | PASS |
| 3 | Visitor /instructors | PASS |
| 4 | Visitor /public/reviews | PASS |
| 5 | Register a new student | PASS |
| 6 | New student login | PASS |
| 7 | Request a package | PASS |
| 8 | Payment instructions | PASS |
| 9 | Unpaid booking refused | PASS |
| 10 | Administrator dashboard | PASS |
| 11 | Confirm payment | PASS |
| 12 | Student sees Paid | PASS |
| 13 | Find available times | PASS |
| 14 | Request future lesson | PASS |
| 15 | Read numeric FIFO position | PASS |
| 16 | Maintenance vehicle rejected | PASS |
| 17 | Assign FIFO heads then new request | PASS |
| 18 | Student sees waiting for instructor | PASS |
| 19 | Instructor sees assigned request | PASS |
| 20 | Instructor accepts | PASS |
| 21 | Future completion must be refused | PASS |
| 22 | Credit debit after real completion | UNVERIFIED |
| 23 | Progress following completed new lesson | UNVERIFIED |
| 24 | New review after completed lesson | UNVERIFIED |
| 25 | Approve existing pending review | PASS |
| 26 | Public instructor rating updates | PASS |
| 27 | Report page | PASS |
| 28 | Business CSV download | PASS |
| 29 | Student denied administrator dashboard | PASS |
| 30 | Wrong password | PASS |
| 31 | Suspended account | PASS |
| 32 | Cancel within 24 hours | PASS |
| 33 | Administrator setup checks | PASS |

**Visual/interaction evidence:** 136 page/role viewport checks (34 cases at 1536x864,
1920x1080, 1280x720 and 390x844), all 200 with no broken images, document overflow
or out-of-bounds controls. All **44 normal interaction checks PASS**. The 104 scoped
solid-background text readings reported no text below 4.5:1. This is not a full accessibility certification.

**Separate early-attendance override: 5/5 PASS.** On a new seed only, admin controls
were enabled; instructor 4 marked B004 No-show without a debit, and instructor 5
completed B005, reducing S002 from 18 to 17 credits. These do not count as a positive
completion under the default time gate. No real/live data was touched.

Normal folder: `C:\Users\USER\AppData\Local\Temp\drivespark-preview-e59c0e23d8654ce9b6459d2ac5315559`.
Override folder: `C:\Users\USER\AppData\Local\Temp\drivespark-preview-a669e7e9f91f487199a685ff6afec5a1`.
Both previews were stopped and both launcher logs confirm run-only variables were cleared.

Evidence: [before/after gallery](screenshots/batchC/index.html),
[page results](screenshots/batchC/after-pages.json),
[33-step results](screenshots/batchC/smoke-results.json),
[functional probes](screenshots/batchC/functional-probes.json),
[normal interactions](screenshots/batchC/normal-checks.json),
[override interactions](screenshots/batchC/override-checks.json),
[contrast readings](screenshots/batchC/after-contrast.json).
Physical phones, other browser engines and assistive technology remain **UNVERIFIED**.

## Batch B current retest - 10 November 2026

This is a historical run; see the Batch C retest above for current evidence. Batch A payment, attendance, renewal and cap protections
remain enabled. The preview was stopped before CSS and Maven builds.

```text
[INFO] Tests run: 691, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  03:43 min
[INFO] Finished at: 2026-11-10T11:54:58+05:30
```

`npm.cmd run build:css` passed (existing Browserslist age warning only). The final
WAR's JavaScript and CSS were compared with source. The app then ran through
`scripts/run-demo.ps1` with Java 17, a fresh shell and a fresh empty folder:
`%TEMP%\drivespark-preview-2e2e1c33150d4c45a45777ae372eecb1`.
The attendance setting was true; no early-attendance override was used.

**33-step smoke: 30 PASS, 0 FAIL, 3 UNVERIFIED.** The normal gate correctly refuses
future completion, so the new student's positive completion/debit/review chain
cannot run without waiting. Seeded pending R013 was used for moderation/rating
steps 25-26, explicitly separate from the blocked new review.

| Step | Current check | Result |
| ---: | --- | --- |
| 1 | Visitor / | PASS |
| 2 | Visitor /packages | PASS |
| 3 | Visitor /instructors | PASS |
| 4 | Visitor /public/reviews | PASS |
| 5 | Register a new student | PASS |
| 6 | New student login | PASS |
| 7 | Request a package | PASS |
| 8 | Payment instructions | PASS |
| 9 | Unpaid booking refused | PASS |
| 10 | Administrator dashboard | PASS |
| 11 | Confirm payment | PASS |
| 12 | Student sees Paid | PASS |
| 13 | Find available times | PASS |
| 14 | Request future lesson | PASS |
| 15 | Read numeric FIFO position | PASS |
| 16 | Maintenance vehicle rejected | PASS |
| 17 | Assign FIFO heads then new request | PASS |
| 18 | Student sees waiting for instructor | PASS |
| 19 | Instructor sees assigned request | PASS |
| 20 | Instructor accepts | PASS |
| 21 | Future completion must be refused | PASS |
| 22 | Credit debit after real completion | UNVERIFIED |
| 23 | Progress following completed new lesson | UNVERIFIED |
| 24 | New review after completed lesson | UNVERIFIED |
| 25 | Approve existing pending review | PASS |
| 26 | Public instructor rating updates | PASS |
| 27 | Report page | PASS |
| 28 | Business CSV download | PASS |
| 29 | Student denied administrator dashboard | PASS |
| 30 | Wrong password | PASS |
| 31 | Suspended account | PASS |
| 32 | Cancel within 24 hours | PASS |
| 33 | Administrator setup checks | PASS |

### Batch B additional browser checks

**31 / 31 passed**, using visitor, student and instructor sessions plus the
administrator. All nineteen evidence screenshots use 1536 x 864.

- Invalid status and reversed date range return 400 with a schedule recovery link;
  valid lowercase/spaced status selects Pending. No booking data is changed.
- An empty filter has Clear schedule filters. A missing vehicle links to the
  existing vehicle list, avoiding a missing-record loop.
- Contact validation keeps typed fields and CSRF protection. Invalid native input
  leaves the submit button usable. Two immediate browser submits send one POST.
- The server also coalesces an identical quick contact retry into one saved enquiry.
  No-token contact POST still returns 403.
- The saved enquiry appears in Admin dashboard > Website enquiries. Literal HTML
  in its text is escaped, not executed. Visitor, student and instructor sessions
  cannot read those enquiries. Public contact pages never list stored messages.
- The confirmation says the message was saved, with a package next-step link; it
  does not promise a sent email. Staff follow up outside the website.
- New learners see a compact package summary and Browse packages. Unpaid learners
  see the course allowance and payment instructions. Fully committed learners
  see current requests instead of a new booking form.
- Friendly profile lesson types round-trip to canonical storage without changing
  lesson credit; the test restored Student1 to Manual car afterwards.
- Student and instructor 403 links lead to their own dashboards. Missing fields
  with JSON Accept still render the safe HTML recovery page.
- Repeating completed attendance gives a plain next step rather than raw enum text.
- A dialog opened after an old confirmation clears its prior decision; Escape
  sends no account-deletion POST. The reason dialog waits for a reason and closes
  with Escape. No destructive confirmation was submitted.

[Full review and every changed test](BATCH_B_REVIEW.md).
[Evidence gallery](screenshots/batchB/index.html),
[raw checklist](screenshots/batchB/smoke-results.json),
[31 extra checks](screenshots/batchB/batchB-checks.json).

The owned preview was stopped; its `finally` block reported that run-only variables
were cleared. Existing live `data` and `data-demo` folders were not used or replaced.

**UNVERIFIED:** positive new-lesson steps 22-24 under the real time gate; every
zero-package/zero-instructor empty-school branch and every malformed input variant;
all assistive-technology/hover/focus states; three-size desktop/mobile visuals;
physical printing and desktop Excel. Batch C visual fixes have not started.

---


## Batch A current retest - 10 November 2026

This is the latest functional evidence. It supersedes older attendance and renewal
expectations below. No Batch B or C fixes are included.

The preview was stopped before `npm.cmd run build:css` and `.\mvnw.cmd clean verify`.
CSS built successfully; the existing Browserslist age warning remains. The actual
Maven result was **673 tests, zero failures, errors or skips; BUILD SUCCESS**,
3 minutes 34 seconds, finished 11:12:54 Asia/Colombo.

The new `scripts/run-demo.ps1` launcher was exercised twice with Java 17. Each run
spawned a fresh shell and a new empty temporary folder. The normal run explicitly
used the attendance rule true; the second used `-AllowEarlyAttendance`, false for
that run only. A second simultaneous launcher was correctly refused. Both previews
were stopped after checking, and both printed that run-only variables were cleared.

- Normal data: `%TEMP%\drivespark-preview-e85a910d158a4d3bbb2b625f0f0389ee`.
- Override data: `%TEMP%\drivespark-preview-8bdeff7f5cb64cefbe5cdcc93a071801`.
- Normal checklist: **30 PASS, 0 FAIL, 3 UNVERIFIED**. Correct refusal of future
  completion prevents the new student's steps 22-24 from reaching completion.
- Explicit override checklist: **33 PASS** with step 21 expecting the permitted
  demonstration completion, plus one passing early no-show check. This is a wiring
  and downstream-flow check; it is not proof that a real lesson took place.
- **22 additional normal-run checks passed**, including renewal payment/history,
  expiry-day boundaries, replacement/assignment at the commitment cap, and schedule
  deletion/leave protections.

Fixture IDs in both runs: new student S013, request E010 for P001 (LKR 25,000, ten
lessons), B025 for 24 Nov 2026 at 2:00 pm. The normal run approves existing pending
R013 for steps 25-26 (average 4.5 to 4.33). The override run creates and approves
new R017 on B025 (average 4.5 to 4.67). The replacement is stated rather than
pretending the default run created a completed lesson. Phones/emails are fictional.

| Step | Check | Default rule | Explicit override |
| ---: | --- | --- | --- |
| 1 | Visitor / | PASS | PASS |
| 2 | Visitor /packages | PASS | PASS |
| 3 | Visitor /instructors | PASS | PASS |
| 4 | Visitor /public/reviews | PASS | PASS |
| 5 | Register a new student | PASS | PASS |
| 6 | New student login | PASS | PASS |
| 7 | Request a package | PASS | PASS |
| 8 | Payment instructions | PASS | PASS |
| 9 | Unpaid booking refused | PASS | PASS |
| 10 | Administrator dashboard | PASS | PASS |
| 11 | Confirm payment | PASS | PASS |
| 12 | Student sees Paid | PASS | PASS |
| 13 | Find available times | PASS | PASS |
| 14 | Request future lesson | PASS | PASS |
| 15 | Read numeric FIFO position | PASS | PASS |
| 16 | Maintenance vehicle rejected | PASS | PASS |
| 17 | Assign FIFO heads then new request | PASS | PASS |
| 18 | Student sees waiting for instructor | PASS | PASS |
| 19 | Instructor sees assigned request | PASS | PASS |
| 20 | Instructor accepts | PASS | PASS |
| 21 | Future completion must be refused | PASS | PASS |
| 22 | Credit debit after real completion | UNVERIFIED | PASS |
| 23 | Progress following completed new lesson | UNVERIFIED | PASS |
| 24 | New review after completed lesson | UNVERIFIED | PASS |
| 25 | Approve existing pending review | PASS | PASS |
| 26 | Public instructor rating updates | PASS | PASS |
| 27 | Report page | PASS | PASS |
| 28 | Business CSV download | PASS | PASS |
| 29 | Student denied administrator dashboard | PASS | PASS |
| 30 | Wrong password | PASS | PASS |
| 31 | Suspended account | PASS | PASS |
| 32 | Cancel within 24 hours | PASS | PASS |
| 33 | Administrator setup checks | PASS | PASS |

Default step 21 requires refusal; override step 21 deliberately permits completion.
Steps 22-24 remain **UNVERIFIED under the normal browser flow**; the separate
override verifies debit 10 to 9, progress 1 / 1.0 hours, and a new pending review.
JUnit additionally checks the real end-time boundaries without waiting on a clock.

### Additional Batch A results

| Scenario | Result | Evidence |
| --- | --- | --- |
| Expired Student6 renews | PASS | New E011 awaits payment, then activates with ten credits; E006 is unchanged and retained. |
| Used-up Student7 renews | PASS | New E012 activates; old E007 remains Paid; old lesson history stays. |
| One awaiting request | PASS | Student8's duplicate creates no receipt. Concurrent renewal requests are also covered by JUnit. |
| Renewal with open lessons | PASS | Student1 gets a clear resolution message; no new receipt. Expired-plus-open Pending/Confirmed cases and confirmation rechecks pass in JUnit. |
| Later-than-expiry new lesson | PASS | E001 ends 25 Dec; 28 Dec request returns 400 and saves nothing. |
| Later-than-expiry reschedule | PASS | HTTP 400, original B001 byte-equivalent as a parsed record. |
| Last valid day | PASS | 25 Dec at 2:00 pm accepted. Legacy null validity remains covered by JUnit. |
| Commitments capped | PASS | Ten open requests for ten credits; extra requests refused. |
| Replace at the cap | PASS | B025 cancelled only after its replacement is saved; still ten open requests. |
| Assign at the cap | PASS | Existing FIFO head assigned once; still ten commitments. |
| Delete hours over Confirmed lesson | PASS | H028 removal returns 400; H028 and B004 Confirmed remain. |
| Leave over assigned Pending lesson | PASS | Sayumi's B003 date returns 400 with an administrator next step; no leave saved. |
| Default admin/instructor future completion | PASS | Both return 400; Confirmed state and ten-credit balance retained. |
| Default future no-show | PASS | HTTP 400, unchanged Confirmed state; during-lesson and exact-end boundaries verified by JUnit. |
| Explicit early no-show override | PASS | B004 becomes No-show; Student4's 18 credits stay unchanged. |
| Double payment confirmation | PASS | Repeated E010 confirmation grants no additional credits. |

[Batch A review and every changed test](BATCH_A_REVIEW.md).
[Evidence gallery](screenshots/batchA/index.html).
[Normal raw checklist](screenshots/batchA/smoke-results.json),
[additional checks](screenshots/batchA/batchA-checks.json),
[override checklist](screenshots/batchA/override/smoke-results.json).

**UNVERIFIED:** actual wall-clock lesson delivery/end in the browser, every legacy
corruption/multi-file failure, every filter/form permutation, desktop Excel and
physical printing. No B/C visual repair is claimed; login labels/alignment remain
known issues. The requested desktop-size matrix belongs to Batch C.

---


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
