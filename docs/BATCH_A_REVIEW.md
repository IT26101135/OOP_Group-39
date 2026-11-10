# Batch A review - 10 November 2026

**Completed: functional Batch A only. Stopped for review before B and C.**
This implements audit F02-F06, the safe-startup helper for F01, and the renewal
next-step text needed by the changed behavior. F07-F10 and the remaining visual
findings stay in later batches. No Git write commands were used.

## Resulting behavior

- **Attendance:** with `app.attendance.require-lesson-ended=true` (the default),
  both Completed and No-show wait until the lesson ends. The exact end is allowed.
  An explicit false setting permits early attendance only for an isolated preview.
  No-show still does not spend credit; completion still spends one.
- **Commitment cap:** the existing Pending-plus-Confirmed limit was retained.
  Both request paths, rescheduling and assignment already checked it under the
  shared lock. Regression checks cover extra requests, replacement at the limit,
  assignment counting the same request once, and legacy over-cap refusal.
- **Renewal:** an expired or used-up course can be replaced when no Pending or
  Confirmed lesson remains. Only one Awaiting payment request is allowed. The
  request does not grant credit or change the old receipt. Payment confirmation
  reloads eligibility under the existing shared lock before switching the active
  package. Old receipts, captured amounts and lesson history remain.
- **Validity:** request, reschedule, assignment, acceptance and reassignment now
  check the lesson date against the receipt's expiry. The expiry day is included.
  Legacy paid receipts without an expiry remain supported. A legacy FIFO request
  outside validity is rejected with its reason, preserving history.
- **Schedule protection:** deletion uses the same Confirmed-lesson protection as
  working-hours editing. Leave creation and editing also reject assigned Pending
  lessons, with an administrator next step. Unassigned requests remain distinct.
  Schedule writes use the same class monitor as account/booking writes.
- **Startup:** `scripts/run-demo.ps1` always spawns a fresh PowerShell process,
  creates a new empty temporary data folder and runs the built WAR. It refuses an
  existing DriveSpark process, a busy port or a stale/missing WAR. It clears four
  run-only variables in `finally`. The normal IDE run still reads existing `./data`
  with seeding off; it deliberately does not replace your saved records.

Run after stopping the app and building with Java 17 or later:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-demo.ps1
```

Optional `-JavaHome 'C:\path\to\jdk-17'`, `-Port 18081` and
`-AllowEarlyAttendance` are documented in README. The helper was actually exercised
for both attendance settings, and a second simultaneous launch was refused.

## Build evidence

The app was stopped before `npm.cmd run build:css` and `.\mvnw.cmd clean verify`.
CSS succeeded; the existing Browserslist age warning remains. No CSS source rules
were changed in this batch. The actual full Maven result was:

```text
[INFO] Tests run: 673, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  03:34 min
[INFO] Finished at: 2026-11-10T11:12:54+05:30
```

[Saved summary](screenshots/batchA/verify-summary.txt).
Full log: `%TEMP%\drivespark-batchA-verify.log`. Surefire XML totals independently
agree. Total increased from 653 to 673 through twenty new tests. Two existing test
methods changed expectations; no future-completion test was weakened or globally
switched to the demo setting.

The earlier focused run had 107 tests with one error in a new presentation fixture:
the helper expected an enrollment status, but the fixture supplied `UNPAID`. That
fixture was corrected to `AWAITING_PAYMENT`. The full clean run above includes the
correction and the first-payment compatibility test added during review.

## Every test changed, and why

Paths are under `src/test/java/com/drivingschool/`.

| File | Existing method changed | Reason |
| --- | --- | --- |
| `booking/model/BookingTest.java` | `noShowStartsAtLessonStartAndDemoCanBypassTime` renamed to `noShowWaitsUntilLessonEndAndDemoCanBypassTime` | The requested policy now rejects start and one second before end, preserves Confirmed, permits exact end, and retains explicit false bypass. |
| `learningpackage/service/EnrollmentPresentationServiceTest.java` | `expiredReceiptHasNoBookLinkAndDoesNotPromiseRenewal` renamed to `expiredReceiptOffersAnotherPackageWithoutBooking` | Eligible expired receipts now link to another package; they still cannot book lessons from the expired receipt. |

| File | New method | Why this check matters |
| --- | --- | --- |
| `instructor/service/InstructorScheduleServiceTest.java` | `protectsConfirmedLessonsDuringHoursDeletion` | Refused deletion retains both the hours and Confirmed lesson. |
| same | `blocksAddingAndMovingLeaveOverAssignedPendingLesson` | Both leave writes reject the conflict and retain the original leave and Pending booking. |
| same | `unassignedRequestsDoNotPreventLeave` | Requested instructor preference is not an actual assignment; preserves FIFO scheduling behavior. |
| `learningpackage/service/EnrollmentPaymentServiceTest.java` | `expiredRenewalKeepsOldReceiptAndLessonHistory` | Renewal payment changes only the active course/balance; old amount, validity, receipt and completed lesson survive. |
| same | `firstPaymentStillHandlesAnOlderUnpaidPendingRequest` | The renewal-only restriction does not change first-payment handling for older unpaid requests. |
| same | `usedUpStudentCanRenewAndOldPaymentCannotBeConfirmedAgain` | A used-up learner gets a new receipt and balance, without reopening an old paid receipt. |
| same | `pendingLessonPreventsRenewal` | Expired course plus open Pending booking cannot create a renewal request. |
| same | `confirmedLessonPreventsRenewal` | The same safeguard covers Confirmed promises. |
| same | `paymentRechecksOpenLessonsCreatedAfterRenewalRequest` | Eligibility changing after the request cannot silently replace the old account during payment. |
| same | `usablePackageIncludingItsExpiryDayCannotBeReplaced` | A package with lessons left remains current through the expiry day. |
| same | `paymentRechecksUsablePackageRestoredAfterRenewalRequest` | A restored usable balance cannot be overwritten by confirming an earlier renewal request. |
| same | `concurrentRenewalRequestsCreateOnlyOneAwaitingReceipt` | Concurrent submissions produce one request and a clear duplicate explanation. |
| `learningpackage/service/EnrollmentPresentationServiceTest.java` | `renewalSummaryDirectsOpenLessonsAndExistingPaymentRequestToTheirNextStep` | Summary links match the actual reason renewal is waiting. |
| `booking/SchedulingWebIntegrationTest.java` | `noShowDuringLessonIsRefusedWithoutChangingBalance` | Mid-lesson no-show is rejected without altering attendance or credit. |
| same | `endedLessonCanBeMarkedNoShowWithoutSpendingCredit` | Ended no-show remains possible without introducing a credit charge. |
| same | `demoSettingAlsoAllowsEarlyNoShow` | Only the explicit false setting bypasses time; restored in `finally`. |
| same | `bothRequestPathsRefuseDatesBeyondExpiry` | Neither request overload can save an out-of-validity appointment. |
| same | `expiryDayIsIncludedAndLegacyNullValidityStillWorks` | Covers inclusive last day and existing legacy receipt support. |
| same | `reschedulingBeyondExpiryKeepsOriginalRequest` | Failed replacement does not cancel the original or save another booking. |
| same | `legacyRequestBeyondExpiryCannotBeAssignedOrAccepted` | Assignment retires an invalid legacy queue item; instructor acceptance cannot bypass validity. |

All existing payment, role, CSRF, ownership, bubble-sort and FIFO tests remained
enabled. Five test files were edited in this batch.

## Browser and isolated-data evidence

[Screenshot gallery](screenshots/batchA/index.html) - twenty screenshots at
**1536 x 864**, including renewed dashboards/history and blocked-action messages.
[Full per-step smoke checklist](SMOKE_TEST.md) includes all 33 steps for both runs.

| Run | Result | Meaning |
| --- | --- | --- |
| Default attendance true | 30 PASS, 0 FAIL, 3 UNVERIFIED | Future completion correctly refused. Steps 22-24 cannot complete that new-student chain under the real gate. |
| Explicit isolated override | 33 PASS | Step 21 intentionally expects early completion; steps 22-24 verify debit 10 to 9, one completed lesson/1.0 hours, and a new pending review. |
| Normal-run additional checks | 22 / 22 passed | Renewal confirmation/history, expiry refusal and inclusive boundary, cap replacement/assignment, hours deletion/leave protection, affected pages. |
| Override early no-show | 1 / 1 passed | Confirmed B004 becomes No-show; Student4's 18 credits remain unchanged. |

The default run approved seeded R013 for steps 25-26, producing average 4.33. The
override run instead created and approved new R017 from B025, producing 4.67.
Neither substitution nor override is described as actual delivered teaching.
Unit/integration tests separately verify the real end-time boundaries.

Normal data directory:
`%TEMP%\drivespark-preview-e85a910d158a4d3bbb2b625f0f0389ee`.
Override data directory:
`%TEMP%\drivespark-preview-8bdeff7f5cb64cefbe5cdcc93a071801`.
Both were fresh empty directories. The two owned Java processes were stopped after
checking. Both launcher logs confirm that run-only variables were cleared.

## Files and documentation

Application source changes:

- `booking/model/Booking.java`: end-time no-show boundary.
- `booking/service/PaidEnrollmentGuard.java`: reusable lesson-date validity check.
- `booking/service/BookingService.java`: call that check at each new promise.
- `instructor/service/InstructorScheduleService.java`: deletion/leave safeguards
  and shared locking.
- `learningpackage/service/EnrollmentService.java`: shared renewal eligibility at
  request and confirmation.
- `learningpackage/service/EnrollmentPresentationService.java`: accurate renewal
  messages and next-step links.

Added launcher `scripts/run-demo.ps1`; updated README, SMOKE_TEST and VIVA_QA. The
source-checked viva generator and outputs were refreshed for the new behavior and
673-test result: 112 answers, 171 code references, seventeen-page speaking PDF,
six-page diagram PDF and six one-page cheat sheets. Diagrams remain selected class
views; module ownership is unchanged.

Member headers, existing step comments and CRUD/OOP/SOLID labels were retained.
No route or form field was renamed. Bubble sort, FIFO queue implementation,
security settings and seed identities were not changed.

## Limits and next batch

**UNVERIFIED:** physical lesson delivery and real wall-clock waiting through its
end in the browser; every corrupt-file or partial-write recovery scenario; every
legacy permutation; all desktop/mobile sizes and assistive-technology states;
desktop Excel and physical printing. Actual end boundaries are verified in tests,
not by pretending the demonstration lesson occurred.

File writes still have no rollback across files or cross-process locking. Existing
payment rules still prevent use of an already-expired receipt; this batch does not
automatically revive old Confirmed lessons or cancel them. They must be resolved
before renewal. Working-hours deletion mirrors the existing Confirmed-lesson rule;
assigned Pending protection added here applies specifically to leave.

Normal IDE startup deliberately continues to load your existing data. Use the
preview helper for six instructors/seven packages. The historical `data-demo`
folder was not overwritten. Login labels/alignment and the other visual findings
remain queued for C; invalid filter and contact/dead-end findings remain in B.
