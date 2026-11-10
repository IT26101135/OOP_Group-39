# Batch B review - 10 November 2026

**Batch B is complete and stopped for review before Batch C.** This covers audit
F07-F10, the profile-input/empty-summary issues V05/V08, and related recovery and
repeated-submit paths. No Git write commands were used. Existing routes, form
field names, member headers, step comments and CRUD/OOP/SOLID labels were retained.

## Changes and resulting behavior

- **Schedule filters:** status is checked against the existing enum before
  rendering. Lowercase/spaced valid choices are normalized. Invalid status and
  reversed date ranges return a clear 400 response. JSP compares strings, avoiding
  the enum-coercion 500. Empty results have a working Clear schedule filters link.
- **Contact submissions:** the confirmation truthfully says the message was saved.
  Saved enquiries are readable newest first in a read-only **Website enquiries**
  section on the existing admin dashboard. Controller and service both enforce
  administrator access. Enquiry text is escaped and the dashboard remains no-store.
  No route, email sender, reply action, read/unread state or deletion feature was
  added. Staff follow up outside the app using the sender's address.
- **Validation:** invalid contact submissions retain their typed fields and form
  token. Missing/mistyped parameters and unsupported methods use the shared HTML
  error view. HTTP statuses remain correct. Missing records link to known lists;
  form/conflict recovery links are offered only when appropriate to the current
  role. Internal exception details are not shown.
- **Repeated actions:** messages explain what happened and how to recover rather
  than exposing `CONFIRMED`/`PENDING` as mechanical instructions. The completion
  end time now uses a readable date/time. All transition, payment and time rules
  remain intact.
- **Duplicate submits:** the browser marks an accepted submit immediately, after
  confirmation handling, and prevents another accepted submit while it is pending.
  Named button values are still collected before delayed loading/disable behavior.
  Identical normalized contact retries within one minute are coalesced by the
  server's synchronized contact service. This is not a claim of universal
  idempotency or cross-process duplicate protection.
- **Dialogs:** every opening resets the previous decision. Every close clears its
  pending form. Escape cannot reuse an old confirmation; only the current explicit
  confirmation resubmits the form. Browser checks sent no deletion POST.
- **Student next steps:** a learner with no package gets a compact summary and a
  direct package link. Unknown details are not expanded into meaningless statistics.
  Unpaid requests distinguish course allowance from usable credit and link to
  payment instructions. Fully committed accounts link to current requests and do
  not offer another booking form; server cap checks remain authoritative.
- **Other empty states:** public and account lists gained relevant package,
  contact, form, schedule or clear-search links. The review list heading no longer
  implies that all published feedback belongs to the current student.
- **Profile lesson types:** known stored names display as friendly labels and map
  back to their existing canonical values on save. Custom lesson-type text is
  preserved. Field names, account identity, credit and password behavior stay intact.

The contact reader completes the existing save-only flow without adding an inbox
route or promising automatic delivery. School contact values still come solely
from the existing configuration and remain hidden when empty.

## Required build and test result

The app was stopped before the builds. `npm.cmd run build:css` passed, with the
existing Browserslist age warning. CSS source rules were not changed; generated
CSS includes utilities used by the updated markup. Actual final Maven summary:

```text
[INFO] Tests run: 691, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  03:43 min
[INFO] Finished at: 2026-11-10T11:54:58+05:30
```

[Saved build summary](screenshots/batchB/verify-summary.txt).
Full log: `%TEMP%\drivespark-batchB-verify.log`. Surefire XML independently totals
691 tests, zero failures/errors/skips. The WAR's JavaScript and CSS were compared
byte-for-byte with source before starting it. `node --check` and `git diff --check`
also passed.

The focused run initially had one fixture-isolation failure: a new enquiry test
left an admin behind for an existing first-admin bootstrap test. The new fixture
now removes only its own temporary administrator in `finally`; production bootstrap
rules were not altered. An earlier full build passed, then full CSS/clean verification
was repeated after the dialog JavaScript correction so the packaged resources
included the final code. The summary above is that final run.

## Every changed test and its reason

Paths below are under `src/test/java/com/drivingschool/`. There are **18 new tests**,
taking 673 to 691, and three existing test methods with changed assertions/setup.
Ten test files were added or edited.

| File | Existing method changed | Reason |
| --- | --- | --- |
| `booking/model/BookingTest.java` | `refusesFutureCompletionWithoutChangingState` | Expected end-time text now uses `1 Nov 2026, 11:00 am`; the same future-time refusal and unchanged Confirmed state remain asserted. |
| `common/exception/AuthorizationErrorHandlerTest.java` | `otherResponseStatusErrorsKeepTheirOriginalStatus` | Adds the shared HTML-view assertion while retaining HTTP 404. |
| `common/exception/ErrorStatusMappingTest.java` | `badRequestUsesFriendlyInputPage` | Tests return-to-admin-form as an administrator; a visitor must no longer receive that private recovery link. |

| File | New method | Purpose |
| --- | --- | --- |
| `admin/AdminDashboardWebIntegrationTest.java` | `visitorEnquiryReachesOnlyTheProtectedDashboardAndRetriesDoNotDuplicate` | Real CSRF-protected contact posts reach the admin model once; public model omits enquiries and missing CSRF remains forbidden. The new temporary admin is cleaned up after this test. |
| same | `invalidEnquiryKeepsTypedFieldsOnContactForm` | A 400 keeps email/message and supplies a usable token and error. |
| `booking/SchedulingWebIntegrationTest.java` | `invalidScheduleFiltersReturn400AndKeepRecordsIntact` | Invalid status/range returns validation rather than a crash; booking state is unchanged. |
| same | `validStatusIsNormalizedAndFullCommitmentsHideNewRequestForm` | Valid mixed input keeps selection; cap-full page does not offer another request. |
| `common/exception/ErrorPageSupportTest.java` (new) | `recoveryDoesNotOfferAnotherRolesForm` | A student error does not link to an admin form. |
| same | `missingVehicleLinksToExistingListRatherThanMissingEditForm` | Avoids a repeated 404 recovery loop. |
| same | `conflictReturnsInstructorToTheirWorkingHoursForm` | Duplicate/conflict recovery uses the instructor's own form. |
| `common/exception/GlobalExceptionHandlerTest.java` | `missingFieldUsesHelpfulHtmlViewEvenWithJsonAccept` | Missing form input returns a safe HTML view with 400. |
| same | `invalidNumberUsesHelpfulHtmlView` | Type binding failure is readable rather than raw technical data. |
| same | `wrongMethodKeeps405WithRecoveryView` | Wrong HTTP method preserves 405 and offers recovery. |
| `common/service/ContactServiceTest.java` (new) | `immediateRetriesSaveOnlyOneNormalizedMessage` | A quick normalized retry creates one record. |
| same | `sameTextAfterRetryWindowCanBeSentAgain` | Coalescing does not permanently suppress a later legitimate enquiry. |
| same | `invalidSubmissionDoesNotSaveARecord` | Invalid input cannot leave a saved message. |
| same | `administratorReadsNewestFirstWithoutChangingRecords` | Read order and explicit administrator check work without another save. |
| same | `nonAdministratorCannotReadEnquiries` | Service access control is enforced independently of the controller. |
| `learningpackage/service/EnrollmentPresentationServiceTest.java` | `newStudentGetsCompactPackageNextStep` | No unusable details or Book action; package next step is provided. |
| same | `awaitingPaymentShowsAllowanceWithoutUsableCredits` | Course allowance remains visible without granting bookable credit. |
| `student/service/StudentProfileServiceTest.java` | `friendlyLessonTypesKeepCanonicalStorageAndCustomChoices` | Friendly choices save correctly; custom text is not rewritten into another value. |

Existing attendance, renewal, payment, ownership, role, CSRF, FIFO and bubble-sort
tests stayed enabled. No successful future-completion test was bypassed.

## Browser evidence

[Nineteen screenshots at 1536 x 864](screenshots/batchB/index.html).
[31 additional checks, all passed](screenshots/batchB/batchB-checks.json).
[Full per-step checklist](SMOKE_TEST.md) and
[raw 33-step results](screenshots/batchB/smoke-results.json).

The app was started through the preview helper in a fresh shell and a new empty
folder: `%TEMP%\drivespark-preview-2e2e1c33150d4c45a45777ae372eecb1`.
The attendance setting was explicitly true. Only fictional data was changed.
Visitor, student, instructor and admin sessions were used.

Normal smoke: **30 PASS, 0 FAIL, 3 UNVERIFIED**. Steps 22-24 cannot demonstrate
successful completion/debit/new-review creation for a future lesson because the
real attendance gate refuses it. Moderation/rating steps 25-26 use existing pending
R013, not a pretend completion. No override run is claimed for Batch B.

The additional checks cover actual browser contact double-submit (one POST), server
retry coalescing, contact correction, escaped enquiry text, access denial and role
recovery, invalid/empty filters, missing records, no-package/unpaid/cap-full states,
profile round-trips without credit changes, readable repeat completion, safe HTML
for missing fields, stale dialog decision reset, Escape with zero deletion POSTs,
and the required-reason dialog.

The owned preview process was stopped and its `finally` block confirmed run-only
variables were cleared. Existing `data` and `data-demo` were not used or replaced.
No real destructive action was submitted.

## Documentation and remaining work

README, SMOKE_TEST and VIVA_QA describe the new behavior. The source-checked speaking
pack/PDFs were refreshed: 112 answers, 172 references, seventeen speaking-guide pages,
six diagram pages and six one-page cheat sheets. Actual email sending, receipt
upload, database transactions, durable audit logging and pagination are still absent.

**UNVERIFIED:** real lesson delivery or waiting through its end in the browser;
every zero-package/zero-instructor empty-school branch and every malformed-input
variant; assistive-technology/hover/focus coverage; desktop Excel and physical
printing. The three-size desktop/mobile checks belong to Batch C. Source-checked
empty-state links are not all claimed as pixel-tested data permutations.

This batch deliberately leaves the audited login label contrast/alignment,
dashboard spacing, duplicate active sidebar items, unavailable attendance-button
appearance and rating-precision styling for **Batch C**. The server remains the
authority for those attendance and payment rules. File storage still lacks
multi-file transactions and cross-process locking.
