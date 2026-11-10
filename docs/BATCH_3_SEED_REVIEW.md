# Batch 3: revised seed and snapshot

Owner: **IT26101132 — Vehicles and Sample Data**. Completed 10 November 2026
(Asia/Colombo). Batch 4 has not started. Home images remain for Batch 5.

## Current records

The loader and checked-in `sample-data` now describe the same fictional school
records. The snapshot was generated from a fresh loader run on 10 November 2026,
including its real repository ID counters. The old snapshot's different counts
and relationships have been replaced, not silently retained alongside the new
records.

| Instructor | Experience | Approved reviews | Calculated average |
| --- | ---: | ---: | ---: |
| Shahithiyan Sivarajah | 8 years | 2 | 4.5 |
| Pragash Tharmalingam | 3 years | 2 | 4.0 |
| Piravarshini Kanagarajah | 12 years | 2 | 4.5 |
| Ruwini Jayasekara | 5 years | 2 | 3.5 |
| Pasindu Wickramasinghe | 15 years | 2 | 5.0 |
| Sayumi Perera | 2 years | 2 | 3.0 |

The experience values deliberately differ from file order. Descending bubble
sort produces Pasindu, Piravarshini, Shahithiyan, Ruwini, Pragash, Sayumi.
Each profile has a short two-sentence bio and appropriate teaching types.
No instructor licence numbers, qualifications or accreditation claims are seeded.
Ratings are recalculated from approved reviews; pending/hidden feedback is excluded.

| Group | Count and scenarios |
| --- | --- |
| Accounts | 19: Anjali Perera as admin, 6 instructors, 12 students |
| Packages | 7: manual beginner/complete, automatic beginner/complete, motorcycle beginner, five-hour refresher, driving-test preparation |
| Vehicles | 6: Suzuki Alto, Toyota Vitz, Suzuki Celerio, Nissan March, Bajaj Discover 125, Honda Dio |
| Enrollments | 9: 7 Paid, 1 Awaiting payment, 1 Cancelled |
| Bookings | 24: 3 Pending, 2 Confirmed, 16 Completed, 1 Cancelled, 1 No-show, 1 Rejected |
| Reviews | 16: 12 Approved, 2 Pending, 2 Hidden; distinct comments with mixed and critical feedback |
| Weekly availability | 42 blocks across all six instructors |
| Lesson notes | 3, linked to completed lessons |
| Days off | 1, with no conflicting active assignment |
| Total records | 133 across 12 record stores and 12 counter files |

Captured Paid revenue is **LKR 190,500**. Package prices, hours and validity are
listed in [the snapshot guide](../sample-data/README.md). Assigned instructors and
vehicles match their course types. Completed lessons occur after enrollment and
before expiry; balances account for completed lessons. No-show/cancelled lessons
do not consume credits. V003 is in maintenance and has no assignments. V006
insurance expires ten days after a fresh seed, with service due in seven days.

The unassigned FIFO order is B001 then B002. B003 is assigned to Sayumi and awaits
instructor approval. E006 is expired and E007 is used up; E008 and E009 belong to
Dharshan (Student 8). The two new students have no package, covering empty states.

## Login compatibility and privacy

Existing admin, instructor1–5 and student1–10 email logins and all passwords are
unchanged. Every fixture account uses `DriveSpark123!`, stored as a BCrypt hash.
New logins:

- `instructor6@drivingschool.test` — Sayumi Perera.
- `chamodi.abeysinghe@learners.example.test` — Chamodi Abeysinghe.
- `nivethan.yogarajah@learners.example.test` — Nivethan Yogarajah.

All emails use reserved `.test` domains. Phone numbers use a deliberately
non-dialable +94/zero-prefix pattern; NICs use an impossible zero-prefix civil
identity pattern. They pass this app's syntax validation, not real allocation or
civil-identity checks. Student addresses and registrations are invented. No real
person's NIC or phone number was copied. School contact settings remain empty.

**Existing data-demo folders retain old names and records. Use a fresh empty data
folder for this seed.** The loader is repeat-safe, not an old-data migration. It
does not overwrite existing names, account decisions, bookings, receipts, reviews
or notes, and does not refill spent credits. Approved-review ratings are derived
again when it runs. The snapshot has fixed dates; use a fresh loader run for
current relative dates or describe the stored snapshot dates honestly.

## Changes and tests

- `DemoDataSeedRunner.java`: six varied instructors, twelve named students, seven
  courses, realistic vehicles, compatible lessons, linked reviews, progress notes,
  availability and leave. Dates use the existing Asia/Colombo application clock.
- `sample-data/*.txt` and matching counters: regenerated from the actual loader.
  The empty legacy instructor-availability store remains for compatibility.
- `README.md`, `sample-data/README.md`, `docs/SMOKE_TEST.md` and `docs/VIVA_QA.md`:
  current logins, counts, scenarios, privacy limits and fresh-folder instructions.
  Older smoke/viva execution records remain explicitly historical.

Only **DemoDataSeedRunnerTest.java** changed in this batch. Its original test now
deliberately expects **6 instructors instead of 5**, 19 accounts instead of 16,
7 packages instead of 6, 24 bookings instead of 15, 16 reviews instead of 10,
12 approved reviews, 16 completed lessons and the new revenue total. It checks
the six names and sort order, approved-only ratings, course/vehicle compatibility,
dates, privacy patterns, balances and repeat seeding. After cancelling an unpaid
request and exhausting a balance, another run must preserve those decisions.

The added `checkedInSnapshotDeserializesWithMatchingNamesAndCounters` test reads a
temporary copy through real repositories, validates all record counts and review
links, checks login passwords, and verifies the next IDs S013, I007, P008 and B025.
There are now two tests in this class. Existing headers and step comments remain.
Bubble sort, FIFO ordering, routes, form names and security rules were not changed.

## Verification evidence

The focused loader/idempotency test passed before snapshot generation. A
preliminary package build with tests skipped was used only to generate that
snapshot in an isolated app. That app was then stopped and its run-only variables
cleared before the required final build.

`npm.cmd run build:css` passed, with the existing outdated Browserslist database
warning. No dependency updates were made. `git diff --check` reported no whitespace
errors; Git emitted its configured LF-to-CRLF notices for the JSON Lines files.

With Java 17, final `.\mvnw.cmd clean verify` finished at 02:59:30:

```text
Tests run: 650, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 03:18 min
```

A separate structural script passed **365 checks** across all 133 snapshot
records: JSON parsing, ID uniqueness/formats, exact high-water counters, absence
of old project text in data, fictional email/phone/NIC patterns, review links,
approved averages and non-overlapping active bookings.

The verified WAR then ran on port 18080 in another fresh PowerShell process with
only run-local demo settings: `DRIVESPARK_DEMO_SEED_ENABLED=true`,
`DRIVESPARK_ADMIN_SEED_ENABLED=false`, and `APP_DATA_DIRECTORY` pointing to this
new empty directory:

```text
C:/Users/USER/AppData/Local/Temp/drivespark-copy-batch3-74a13e7820ba4c689ed046438d1be1d4
```

Chrome headless through the installed Playwright package used a 1536 x 864
viewport and reduced motion. All **90 checks passed**, covering:

- Visitor home/about/FAQ/contact/packages/instructors/reviews/auth pages, seven
  public packages, twelve approved public reviews and the six-instructor sort order.
- All nineteen login outcomes: seventeen accepted, pending Isuri and suspended
  Janani refused. Their names and `.test` login addresses came from the snapshot.
- Admin dashboard, students, instructors, packages, bookings/details, payments,
  reviews, reports and vehicles, including revenue and maintenance/insurance warnings.
- Instructor 1 and Instructor 6 dashboards, working hours and reviews, including
  Sayumi's assigned motorcycle lesson with Tharushi.
- Student profile, progress, package, lesson/detail, payment and review pages;
  expired, used-up, unpaid/cancelled and no-package scenarios.
- Agreement between the final fresh runtime records and checked-in snapshot,
  ignoring only independently salted BCrypt hashes. JUnit separately verified
  every snapshot password against the unchanged fixture password.
- Unchanged runtime file hashes before/after browsing. No data-changing forms
  or destructive actions were submitted in this browser run.

The initial browser script compared refused-login paths without accounting for
the servlet's `;jsessionid` suffix. Normalizing that suffix made both blocked-login
checks pass; no authentication or security-rule change was made.

The app and test browser are stopped, and the app runner printed that its
run-only demo variables were cleared in `finally`. No live data or Git state was
modified. Temporary evidence:

```text
%TEMP%/drivespark-batch3-seed-test.log
%TEMP%/drivespark-batch3-verify.log
%TEMP%/drivespark-batch3-structure.json
%TEMP%/drivespark-copy-batch3-browser.json
%TEMP%/drivespark-copy-batch3-app.log
%TEMP%/drivespark-copy-batch3-run.ps1
%TEMP%/drivespark-copy-batch3-browser.cjs
```

Full transactional journeys with this exact dataset, a separate seed-disabled
snapshot browser run, mobile layouts and visual screenshot comparisons remain
**UNVERIFIED**. The snapshot does have real-repository deserialization coverage.
Home image changes and white-card screenshots remain for Batch 5. The CSV/print
redesign remains for Batch 4.
