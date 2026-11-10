# Batch 2: account pages, messages and payment instructions

Completed 10 November 2026 (Asia/Colombo). Batch 3 has not started.

## Result

- Student, instructor and admin pages use short headings, practical empty-state
  messages and readable statuses. Visible references resolve to names, vehicle
  registrations, packages and lesson dates. Internal route identifiers, HTML IDs,
  form values and payment references are retained.
- Shared display formatting uses LKR thousand separators, dates such as
  `16 Nov 2026` and times such as `9:00 am`. Native date/time and numeric form
  values keep the formats required for submission.
- Schedule filters show names and registrations while keeping the original
  `instructorId` and `vehicleId` field names.
- Confirmation dialogs show the requested action on the confirmation button.
  Cancellation, removal and payment messages explain the action in plain words.
- Payment instructions use `app.school.email`, the same source as the public
  contact details. It defaults to empty. The email instruction is hidden when
  empty, and a contact-form link remains. No email delivery is claimed.
  README.md explains that the old `ADMIN_EMAIL` / `app.payment.admin-email`
  configuration is no longer used. Login emails and passwords are unchanged.
- Report-page labels resolve names and registrations, month labels are readable,
  and displayed revenue uses LKR formatting. CSV layout and print-report
  restructuring remain for Batch 4.
- `AccountDisplayAdvice` resolves read-only display labels lazily and caches each
  catalog within the request. Missing references use readable fallback labels
  rather than internal IDs. It does not change records or authorization rules.

Affected view groups: `student/*.jsp`, `booking/*.jsp*`, `instructor/*.jsp`,
`admin/*.jsp`, `vehicle/*.jsp`, `review/list.jsp`, shared navigation/sidebar,
confirmation/status/review-filter/enrollment-summary components and `site.js`.
Supporting changes cover the shared presentation advice, payment contact setting,
and message literals in booking, enrollment, instructor, review, student and
vehicle controllers/services/models. Only the CSS output was regenerated;
`app.css` was not changed.

Home image structure and styling were not changed in Batch 2. The white-card
work and home image work remain for Batch 5. Old seeded names, descriptions,
qualifications and review comments remain for Batch 3. Use a fresh empty data
folder for that seed; existing folders retain their old records.

Member headers, step comments, CRUD/OOP/SOLID labels, existing routes and form
field names were retained. Bubble sort, FIFO ordering, validation conditions,
payment gates, attendance restrictions and access rules were not changed.
Only read-only Git commands were used.

## Final verification

The isolated app was stopped before the final build. `npm.cmd run build:css`
passed, with the existing outdated Browserslist database warning. No dependencies
were updated. `node --check src/main/resources/static/js/site.js` and
`git diff --check` passed.

With Java 17, the final `.\mvnw.cmd clean verify` finished at 02:39:28:

```text
Tests run: 649, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 03:09 min
```

Earlier verification found old message assertions, including an expectation
constructed by concatenating a package ID. Those expectations were updated while
retaining the same behavior checks. A browser run then found raw course-type
labels in the admin package dropdowns. The visible labels were corrected, followed
by another CSS build, full verification and a fresh-data browser run. The browser
harness also changed to click the visible time-slot label rather than the styled
radio input beneath it; no app change was needed for that harness timeout.

Every test file changed in this batch:

| Test | Reason |
| --- | --- |
| `booking/SchedulingWebIntegrationTest.java` | Expect the readable missing-package fallback without an ID and the revised paid-package message. Existing HTTP, ownership and payment restrictions remain asserted. |
| `booking/service/BookingConflictCheckerTest.java` | Expect the revised paid-package message when an enrollment cannot supply lessons. |
| `learningpackage/EnrollmentWebIntegrationTest.java` | Expect an empty default payment-contact email instead of an invented fallback address. |
| `learningpackage/service/EnrollmentPaymentServiceTest.java` | Match the revised suspended-account and unavailable-package messages; still assert rejection and unchanged lesson balances. |
| `learningpackage/service/EnrollmentPresentationServiceTest.java` | Expect the readable past-package explanation and missing-package fallback without an ID. |
| `common/controller/AccountDisplayAdviceTest.java` (new, 3 tests) | Check lazy per-request name resolution, missing-reference fallbacks, and LKR/date/time/status formatting. |

The final WAR ran on port 18080 in a fresh PowerShell process. Only that run set
`DRIVESPARK_DEMO_SEED_ENABLED=true`, `DRIVESPARK_ADMIN_SEED_ENABLED=false` and
`APP_DATA_DIRECTORY`. The last setting pointed to the new empty directory:

```text
C:/Users/USER/AppData/Local/Temp/drivespark-copy-batch2-caa329d20ef645cb951cfbb8682b6296
```

Chrome headless through the installed Playwright package used a 1536 x 864
viewport and reduced motion. All **57 checks passed**:

- Visitors were redirected to login for the protected routes checked below.
- Student: profile, profile edit, password, progress, enrollments, bookings,
  booking details, paid payment instructions and reviews.
- Instructor: dashboard, working hours/leave and reviews.
- Admin: dashboard, students, packages, instructors, awaiting payments, bookings,
  reviews, reports, vehicles, vehicle creation/editing, booking details and reviews.
- Student 8: awaiting-payment and cancelled payment instructions. With no school
  email configured, neither an invented contact address nor an email-receipt line
  was displayed.
- A new lesson request in the isolated data folder displayed the revised success
  message. No production data was used.
- Account deletion, working-hour removal, payment confirmation, package deletion
  and admin lesson cancellation dialogs were opened and cancelled. The harness
  recorded zero POST requests while inspecting these dialogs.

Page checks asserted successful responses and absence of standalone internal IDs
and targeted technical status/algorithm labels in visible main-page text.

Temporary evidence:

```text
%TEMP%/drivespark-batch2-verify.log
%TEMP%/drivespark-copy-batch2-browser.json
%TEMP%/drivespark-copy-batch2-app.log
%TEMP%/drivespark-copy-batch2-run.ps1
%TEMP%/drivespark-copy-batch2-browser.cjs
```

The isolated app and test browser are stopped. The app runner cleared its run-only
demo variables in `finally`. No real destructive actions were performed.

## Limits

The following are **UNVERIFIED in the browser**: a populated school-email setting,
every other success/error flash path, every missing-record or empty-data page
variant, and mobile layouts. Missing-reference and formatting behavior has JUnit
coverage. Visual screenshot comparison and white-card contrast measurements are
**UNVERIFIED** and remain part of Batch 5. CSV restructuring remains for Batch 4.
