# Driving School System Test Checklist

Owner: IT26101132 - [Name]. Browser results apply only to the listed pages, states and widths.

Automated tests run with `.\mvnw.cmd clean verify` on Windows. Manual browser scenarios are marked Not run until a member records actual behavior. Use a separate empty `APP_DATA_DIRECTORY` for seeding.

| ID | Module | Scenario | Steps | Expected result | Actual result | Pass/fail |
|---|---|---|---|---|---|---|
| A01 | Auth | Valid/invalid sign-up | Submit valid and malformed email, phone, NIC and password values | Valid user is BCrypt-hashed and Pending; invalid input is rejected | Automated validation tests; browser pass not recorded | Manual not run |
| A02 | Auth | Duplicate email | Submit same address twice | Second registration is rejected | Automated suite | Pass |
| A03 | Auth | Pending/suspended login | Login with seeded students 8/10 | Clear waiting/suspended message, no authenticated session | Manual not run | Manual not run |
| A04 | Auth | Role redirects and CSRF | Login each role; submit logout without/with token | Correct role route; write without CSRF is blocked | Automated auth tests | Pass |
| A05 | Student | Profile/password/admin status | Edit profile, change password, approve/suspend | Own data changes; status gates login | Automated profile/admin tests; full manual flow not recorded | Partial |
| B01 | Packages | CRUD and validation | Add/edit/deactivate package; try nonpositive price/lessons | Valid package persists; invalid values reject | Existing package tests | Pass |
| B02 | Enrollment | Active package limit | Enroll and attempt second active enrollment | Lesson balance set and second active enrollment rejected | Manual browser flow not recorded | Not run |
| B03 | Public UI | Visitor pages | Browse Home/About/Packages/Instructors/FAQ/Contact/Reviews | Public pages show active packages/approved reviews | Manual browser flow not recorded | Not run |
| C01 | Instructor | Bubble sort | Test name/experience/rating both directions, empty, one item and ties | Adjacent swaps produce stable expected output | JUnit `InstructorSorterTest` | Pass |
| C02 | Instructor | Schedule and leave | Add valid/overlapping availability and leave | Invalid overlaps rejected; leave blocks slots | Manual browser flow not recorded | Not run |
| C03 | Instructor | Accept/attendance/progress | Accept/decline, complete/no-show, enter skill notes | Status and lesson balance/progress update | Conflict/cutoff unit tests; manual flow not recorded | Partial |
| D01 | Booking | Slot generation | Select instructor/date with availability and leave | Only future, unoccupied one-hour slots appear; empty state otherwise | `SlotAvailabilityService` compile; browser scenario not run | Not run |
| D02 | Booking | Server-side conflict checks | Attempt past, leave, nonworking, instructor/student/vehicle overlap, inactive package, wrong vehicle type/status | Request rejected with specific reason; rejected reason visible to student | JUnit conflict checker tests | Pass |
| D03 | Booking | FIFO and cancellation | Submit several requests, cancel one, reschedule one, process next | Oldest remaining pending request processes; reschedule joins queue tail | JUnit queue tests; browser scenario not run | Partial |
| D04 | Booking | 24-hour boundary | Cancel at 23:59 and exactly 24 hours; reschedule | Inside cutoff rejected; exactly 24 hours allowed; replacement is new Pending | JUnit cutoff tests; reschedule UI not manually run | Partial |
| D05 | Admin schedule | Filters/reassign/force-cancel | Filter date/instructor/vehicle/status, reassign and force-cancel | Conflict rechecked; reason stored; confirmation shown | Manual browser flow not recorded | Not run |
| E01 | Vehicle | CRUD/status/date warnings | Create/read/update/delete temporary vehicles; check renewal boundaries and invalid dates | Valid edits persist; duplicates and invalid dates reject; missing dates remain unknown | VehicleMaintenanceTest (5) and existing vehicle tests passed; fleet/create/edit viewed at 375/768/1280; V006 near-expiry badge visible | Automated pass; browser destructive actions UNVERIFIED |
| E02 | Sample seed | Repeat seed | Start twice with demo seed enabled and the same temporary data path | 1 admin, 5 instructors, 10 students, 6 packages, 6 vehicles, 7 enrollments, 15 bookings, 10 reviews; no duplicates | DemoDataSeedRunnerTest passed in Phase 4 clean verify; demo app restarted using temporary data | Pass |
| F01 | Reviews | CRUD, eligibility and uniqueness | Review own completed lesson; edit/delete; try another student, incomplete lesson, invalid stars, duplicate and missing CSRF | Valid own feedback persists; invalid or unauthorized writes reject with friendly errors | ReviewWebIntegrationTest (8) and ReviewVisibilityTest (2) passed; public/student/instructor views and expanded star editor viewed at 375/768/1280 | Automated pass; browser write submissions UNVERIFIED |
| F02 | Reviews | Moderation/rating | Approve, hide and delete temporary reviews; browse status filters | Only Approved reviews public; instructor average recalculated | Existing moderation/rating tests and new route tests passed; all/pending/approved/hidden filters viewed at 375/768/1280 | Automated pass; browser moderation writes UNVERIFIED |
| F03 | Reports | Counts, CSV and print | Compare tables and SVG bars with saved records; export CSV; open print layout | Seven sections; exact counts and revenue; quoted UTF-8 CSV; white print background | AdminReportServiceTest (6) passed; browser CSV matches all 31 displayed rows with PAID 125000.00; reports viewed at 375/768/1280; print CSS and PDF generated | Pass for listed checks; physical printing UNVERIFIED |

## Protected URL role matrix

Automated integration tests cover representative anonymous, student and admin requests. This full matrix still needs a recorded manual browser pass. Expected: visitors redirect to `/login?required`; logged-in roles other than the allowed role receive 403.

| Protected URL(s) | Visitor | Student | Instructor | Admin | Manual status |
|---|---|---|---|---|---|
| `/student/profile`, `/student/profile/edit`, `/student/progress`, `/student/password`, `/student/account`, `/enrollments`, `/bookings` | Block | Allow | Block | Block | Not run for every URL |
| `/instructor/dashboard`, `/instructor/availability` | Block | Block | Allow | Block | Not run for every URL |
| `/admin/dashboard`, `/admin/students`, `/admin/instructors`, `/admin/packages`, `/admin/vehicles`, `/admin/bookings`, `/admin/reviews`, `/admin/reports`, `/admin/reports/export.csv` | Block | Block | Block | Allow | Not run for every URL |
| `/reviews` | Block | Own + approved | Own instructor reviews | All/moderation | Not run for every role |
| Public home/about/packages/instructors/FAQ/contact/reviews | Allow | Allow | Allow | Allow | Not run |

Security notes: passwords are BCrypt hashed; server-side validation and role checks are used; JSP content uses `<c:out>` for escaped dynamic output; POST requests are CSRF checked. Reset delivery is simulated and local file storage does not provide database transactions.

## Automated verification record

2026-11-07: mvnw.cmd clean verify — PASS; 504 tests, 0 failures, 0 errors, 0 skipped. This verifies automated tests only; manual browser rows above remain unrun.

## Phase 4 verification — 2026-11-08

Executed with Java 17 using `.\mvnw.cmd clean verify`:

```text
Tests run: 565, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 56.128 s
```

Added 21 tests in four new files; no existing tests were changed:

- `vehicle/service/VehicleMaintenanceTest.java`: 5 tests for real file CRUD, date boundaries, expired/unknown dates and rejected edits.
- `review/service/ReviewVisibilityTest.java`: 2 tests for public approved-only feedback and matching instructor list/detail visibility.
- `review/service/AdminReportServiceTest.java`: 6 tests for exact counts, calendar order, saved payment amounts, approved ratings, chart widths and CSV quoting/formula prefixes.
- `review/ReviewWebIntegrationTest.java`: 8 tests for review CRUD, completion/ownership/duplicate/validation checks, moderation, admin-only reports and CSRF.

Browser evidence:

- Chrome: fleet, vehicle create/edit, public reviews, student/instructor reviews, expanded student editor, all four moderation filters and reports at 375/768/1280 pixels.
- No horizontal overflow, duplicate element IDs, JavaScript exceptions or HTTP errors detected in these checked states. Expanded review stars display correctly and clicking three stars selects rating 3 in that form.
- Report SVG containers animate with `ds-progress`; reduced motion disables that animation.
- With JavaScript disabled, all 31 report table rows remain visible at each width; CSV stays an ordinary link and browser-menu printing is explained.
- Report CSV returns HTTP 200 and exactly matches every displayed table row. Its paid total is 125000.00.
- Print media uses white paper, dark headings and hidden navigation. A browser PDF was generated in the temporary directory; physical printing was not attempted.

Tests use temporary files or mocks. Browser checks use the temporary `drivespark-phase4-check` demo directory, not project `data/`. No browser delete, hide, cancel, suspend or queue-processing actions were submitted. Remaining module rows and the full protected-role matrix keep their earlier unverified status until Phase 6.

## Phase 5 verification — 2026-11-08

Owner: IT26101132 - [Name] (checklist maintenance). The integration documentation remains owned by IT26101135.

Final commands: `npm run build:css`, `.\mvnw.cmd -q -DskipTests compile`, `.\mvnw.cmd clean verify`, then restart the application against the existing temporary demo directory. The final Maven output was:

```text
Tests run: 575, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 01:00 min
```

New tests written earlier in Phase 5 and included in this full run:

- IT26101135: `StudentAccountCrudTest` — 6 tests for real-file account CRUD, enrollment/booking/review deletion guards, invalid-profile preservation and duplicate-registration exceptions.
- IT26101136: `LearningPackageLifecycleTest` — 4 tests for full package CRUD and saved metadata, enrollment history, legacy lesson references, and missing/malformed deletion targets.

The initial new student test used record-style accessors; it was corrected to the existing `StudentProfile` getters. No existing test was changed. Earlier targeted runs passed 6 and 4 tests respectively; the resumed work did not repeat those targeted runs.

### Evidence for all six members

| Member | CRUD evidence | Custom exception evidence |
|---|---|---|
| IT26101135 | `StudentAccountCrudTest.supportsStudentAccountCrudWithRealFiles` | The same class checks ValidationException history guards and DuplicateRecordException registration. |
| IT26101133 | `InstructorAccountServiceTest.supportsProfileAndAccountCrud`; `InstructorScheduleServiceTest` | Missing email and overlap/ownership validation tests. |
| IT26101134 | `BookingServiceCrudTest.supportsBookingCrudWithoutChangingLessonBalance`; `SchedulingWebIntegrationTest` | Malformed filters, missing records and restricted deletion tests. |
| IT26101132 | `VehicleMaintenanceTest.datesSurviveCreateReadUpdateAndDelete`; existing vehicle service/web tests | Invalid service dates, missing vehicles and duplicate plate tests. |
| IT26101136 | `LearningPackageLifecycleTest.supportsFullPackageCrudAndSavedMetadata`; `EnrollmentServiceTest` | Missing/malformed targets, protected receipts and duplicate active enrollment tests. |
| IT26101137 | `ReviewWebIntegrationTest.supportsReviewCrudThroughRealRoutes`; existing moderation tests | Invalid ratings, duplicate completed-lesson reviews and ownership checks. |

### Accessibility and documentation evidence

- The earlier Phase 5 browser audit visited 32 page/role combinations at 375px. Form controls had labels; titles, descriptions, language and main landmarks were present. The confirmed error-page skip-link gap was fixed.
- After rebuild/restart, Chrome screenshots were viewed at 375/768/1280 for the dedicated 403, 404 and 500 pages, the friendly application error page, public review stars and the expanded student review editor.
- The error-page skip link receives keyboard focus and is visible. Star radio inputs show a visible focus ring; inactive stars use RGB 148,163,184 (`#94A3B8`).
- Main text contrast against page/card tokens: 17.19:1 / 16.19:1. Secondary text contrast: 7.34:1 / 6.92:1. These measurements apply to the stated tokens, not a complete WCAG certification of every rendered state.
- No missing form labels, duplicate IDs, horizontal overflow or JavaScript exceptions were detected in the changed states checked at all three widths.
- Expected error responses were deliberately requested: unknown page 404, missing-CSRF login 403, direct error handler 500 and missing package 404. The rejected login reached no authentication or data-writing action. No destructive browser action was submitted.
- Local links in all three documentation files resolve. CRUD_MAP has 99 verified method rows; OOP_SOLID_MAP has 60 examples; VIVA_GUIDE has six member sections with 30 questions and six five-interaction demonstrations.
- README includes local build/run instructions, sample logins, component contracts, verification limits and the included unmodified Tailwind MIT notice.

The original `data/`, the newly noticed untracked `data-demo/` folder and the pre-existing `package-lock.json` change were left untouched. The application runs against temporary demo data. Full role/access/menu/static-file verification, unvisited page/state visual checks and the Git branch plan remain Phase 6 work. Member names remain `[Name]` until supplied by the team.

## Premium UI Phase E verification — 2026-11-08

Evidence owner: **IT26101135 - [Name]**. This section records the new restyle's final phase, separately from the earlier numbered phases.

### Build and automated tests

Java 17; commands actually run: `npm run build:css`, `node --check src/main/resources/static/js/site.js`, `.\mvnw.cmd -q -DskipTests compile`, `.\mvnw.cmd clean verify`, and read-only `git diff --check`.

```text
Tests run: 575, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 57.501 s
```

No Java source or JUnit test was changed in the A–E restyle. The existing full suite remains intact. The Browserslist/caniuse-lite age warning and Git line-ending notices are nonblocking; package manifests and lockfiles were not changed in this restyle. The app was stopped for the clean verification and restarted against isolated temporary demo data, not project `data/` or user `data-demo/`.

### Browser and accessibility checks

Headless Chrome was controlled through its local DevTools connection. axe-core **4.14.0** was downloaded to the system temporary directory for testing only; no accessibility library was added to the application or its package manifests.

The final automated scan covered these **32 page/role combinations at 375, 768 and 1280px (96 checks)**, with zero automated WCAG-tagged violations:

| Role | Pages checked at all three widths |
|---|---|
| Visitor | Home, packages, a package detail, instructors, approved reviews, About, FAQ, Contact, Login, Sign up, password reset, setup check |
| Student | Dashboard/profile, profile editor, password, progress, enrolments, bookings, personal reviews |
| Instructor | Dashboard, working hours/leave, personal reviews |
| Admin | Dashboard, students, instructors, packages, vehicles, vehicle creation, vehicle editing, bookings, review moderation, reports |

Each checked page had a title, description, English language declaration, one main landmark, a skip link and image alt attributes. No horizontal overflow, foreign stylesheet/script/image URLs, unexpected HTTP errors or JavaScript exceptions were detected in those checks. Representative screenshots were inspected; this is not a pixel-by-pixel human review of every state.

The home page's original decorative `01` contrast failed the initial scan (2.53:1). Its colour was changed to `#A3A3A3`. The review carousel container was then given a named region; a focused recheck passed at all three widths with the earlier ARIA manual-review warning resolved.

Additional checked states: fullscreen mobile navigation, instructor profile/feedback tab, expanded lesson notes, student account settings, the confirmation dialog (opened directly without a destructive action), and the expanded student review editor. These produced no automated violations. Keyboard tests confirmed visible skip-link and input outlines, main-content focus, fragment links to tabs, the assigned-lessons sidebar link, star-input focus styling, and Escape dismissal of the navigation and native dialog. No destructive action was submitted.

Dedicated 404 and 500 pages and the friendly 400/403 error states were also viewed/scanned at all three widths. Their main regions were made focusable; skip-link focus then passed with JavaScript explicitly disabled. Expected error responses came from an unknown page (404), malformed package ID (400), direct error dispatch (500), and a student's attempt to open admin dashboard (403). These were deliberate GET requests, not unexpected application failures.

### Colour measurements

| Foreground / background | Contrast |
|---|---|
| `#FFFFFF` / `#050505` | 20.38:1 |
| `#A3A3A3` / `#050505` | 8.08:1 |
| `#A3A3A3` / `#0A0A0A` | 7.85:1 |
| `#000000` / `#F59E0B` | 9.78:1 |
| `#FFFFFF` / `#0A0A0A` | 19.80:1 |

These are calculated colour-pair measurements. axe marked contrast over gradients for manual review; representative gradient states were visually inspected, but complete measured contrast over every image/animation position is **UNVERIFIED**. A full screen-reader audit and all browser/platform combinations are **UNVERIFIED**. Zero automated violations is not a WCAG certification.

### Print, export and local assets

- Home, student profile and admin reports were checked under print media. The computed body was white, headings were `#171717`, navigation was hidden, and no visible white-on-white heading/paragraph/table text was detected. Reports used neutral `#404040` bars. Browser PDFs and print-preview screenshots were generated in the system temporary directory.
- Existing CSV export returned **200**, `text/csv`, with `report,label,value` headers and saved report rows. It was read/exported only; no report data was changed.
- The local Inter font loaded with HTTPS requests blocked. Fonts and illustrations do not depend on a CDN. The eight optional image filenames are documented without generating requests to absent files; no third-party photo is shipped.
- The image guide records the original fallback credit for each section and the credit format required for future downloaded photos. README credits include Inter's bundled OFL notice, Tailwind's MIT notice and the inspiration-only boundary.
- README local links were checked. Code changes retained member headers and explanatory comments; all CRUD/OOP/SOLID Java labels, routes, form field names, bubble sort and FIFO implementations were preserved.

The original numbered Phase 6 complete role/security audit and Git branch plan are separate work; this A–E restyle did not execute or replace that plan. No Git write commands or folder changes were performed.
