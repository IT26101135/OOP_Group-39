# Batches 4 and 5 review - 10 November 2026

Both batches are ready for review. No Git write commands were used. Member headers,
step comments, labelled examples, routes, form names, bubble sort, FIFO queue and
security rules were retained. All browser work used isolated fictional seed data.

## Business report

- Full CSV and six individual section downloads share the same business tables.
- UTF-8 BOM, quoted comma-separated cells, CRLF rows and spreadsheet formula
  neutralisation are covered by tests. Money uses two decimal places; dates use
  `dd MMM yyyy`; ratings use one decimal place. Names replace internal IDs.
- The header includes the school, generation date and time, signed-in administrator
  and reporting period. Filenames include the generation date.
- Sections cover enrollments by package, revenue by month, bookings by status,
  bookings by instructor, instructor ratings and vehicle usage. Money sections
  end with a Total row. Empty sections retain headings and an explanatory row.
- Revenue includes paid enrollments only. Monthly revenue uses the enrollment
  month because this system does not store a separate payment-confirmation date.
  Reports read each store once, but do not provide an atomic multi-store snapshot.
- The printable report uses these same tables and header, with white paper styling.

Evidence: [downloaded CSV](screenshots/batch45/school-report.csv),
[printed report PDF](screenshots/batch45/school-report-print.pdf).

## Cards and home photographs

The chosen background is soft grey, with white cards, 16px corners, 24px padding,
dark slate text and soft shadows. The dark navigation and amber primary button
remain. Shared rules in `app.css` cover account dashboards, lists, forms, details,
tables, statuses, dialogs and toasts. Reduced-motion preferences are respected.

The six photographs below the home hero now span the viewport with overlaid text,
using the existing photographs. The hero itself was unchanged: its before and after
screenshots have identical SHA-256 hashes. Public package marketing keeps its style.

[Open the screenshot gallery](screenshots/batch45/index.html) for 30 matched pairs
at **1536 x 864**, the deep-slate alternative, dialog, toast and print evidence.
Soft grey is recommended because the cards and dark navigation remain easy to
distinguish without making the whole account page dark.

The 24 page routes include visitor home/packages/login/register; student profile,
enrollments, bookings, booking details, progress, payment instructions, profile edit
and password; instructor dashboard and availability; and administrator dashboard,
vehicles, vehicle edit, packages, students, instructors, enrollments, bookings,
reviews and reports. Six additional matched pairs show each lower home photograph.

Most before images were captured from the previous built application. For the six
lower photo comparisons, the original stylesheet was applied to the unchanged home
markup and images were awaited until decoded. The stylesheet was read with
read-only `git show`; no checkout or restore was performed.

## Validation

- `npm.cmd run build:css`: passed. The existing Browserslist database-age warning
  remains; it did not prevent the build.
- `.\mvnw.cmd clean verify`: **BUILD SUCCESS; 653 tests, zero failures, errors or
  skipped tests**. The final run took 3 minutes 20 seconds.
- All 24 browser routes returned HTTP 200 at 1536 x 864 with no horizontal overflow.
- All 18 final browser checks passed: six full-width home photos, a white confirmation
  dialog, full CSV BOM/filename/names/paid total, six section downloads, white print
  styling with navigation hidden, and protection of anonymous CSV access.
- A rendered text scan on the 20 authenticated pages found no measured solid-background
  text contrast below 4.5:1. Success and error toasts were also captured following a
  harmless learning-goal update and a rejected duplicate enrollment on isolated data.
- `git diff --check`: passed.

Changed tests and reasons:

| Test file | Change and reason |
| --- | --- |
| `AdminReportServiceTest.java` | Updated the old CSV expectations for the business layout; added three tests for named sections/header/totals/BOM, empty sections, individual downloads and formula neutralisation/quoting. Existing legacy summary tests remain. |
| `ReviewWebIntegrationTest.java` | Updated the expected filename to include the generation date. Existing access-control assertions remain. |

## Viva and individual diagrams

The [current speaking guide](viva-2026/README.md) contains **112 answers**:
20 project questions, 12 for each of six members, 15 what-if questions and five
group questions. Each answer has 2-4 short sentences, followed by code evidence
and browser paths where useful. 170 file/method references were checked against
the repository. Conventional OOP terminology and the course's fifth-topic
terminology are distinguished explicitly.

- [Speaking guide PDF - 17 pages](viva-2026/speaking-guide.pdf)
- [Cheat sheets PDF - six pages, one per member](viva-2026/cheat-sheets.pdf)
- [Class diagrams PDF - six pages, one per member](viva-2026/class-diagrams.pdf)
- Individual guides and SVG/Mermaid diagrams: [IT26101132](viva-2026/IT26101132.md),
  [IT26101133](viva-2026/IT26101133.md), [IT26101134](viva-2026/IT26101134.md),
  [IT26101135](viva-2026/IT26101135.md), [IT26101136](viva-2026/IT26101136.md),
  [IT26101137](viva-2026/IT26101137.md).

The reproducible source generator is `scripts/build_viva.py`. Earlier viva/map
documents link to this current pack; their historical material is retained.

## Limits and honest answers

**UNVERIFIED / NEEDS CHECK:** desktop Excel was not opened; CSV bytes, layout and
download behavior were tested. PDFs were generated and their page counts checked,
but physical paper output was not tested. Mobile layouts, every hover/focus state,
screen-reader behavior and photograph-background contrast were not exhaustively
audited; the text scan is not a complete WCAG certification. All listed pages were
opened, but every mutation/error branch was not exercised through the browser.

The guide marks unconfirmed Git/team-history claims and the lecturer's preferred
fifth OOP topic as **NEEDS CHECK**. It explains the actual limits: manual payment,
no sent email or receipt upload, local reset codes, file storage without database
transactions, and locking limited to this JVM. It does not invent individual
contributions, reviewers or a hardest bug.

The final application run used a newly created empty folder under the Windows
temporary directory (`drivespark-batch45-4d3577061b2546558a743c29984110a1`). Existing
`data-demo` was not reused. Run-local demo variables are cleared by the runner's
`finally` block when stopped. Use a fresh empty data folder for your own preview.

Final shutdown confirmed: the application stopped, the runner reported that its
run-only demo variables were cleared, and port 18080 had no listening process.
