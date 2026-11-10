# Batch C visual review - 10 November 2026

Batch C covers the remaining visual findings from the [current-state audit](STEP_0_CURRENT_AUDIT.md). It stops here for review. See the [before/after gallery](screenshots/batchC/index.html), which contains 136 matched viewport pairs.

## Changes

| Audit | Result | Main files |
| --- | --- | --- |
| V01/V02 | Dark, readable authentication labels; padded, aligned form/photo cards; aligned password toggles; compact recovery form fits the 1280x720 desktop action area. Phone forms stack and hide the decorative photo. | `static/css/app.css`, generated `tailwind.css`, `auth/login.jsp`, `auth/register.jsp` |
| V03 | Student overview cards share a top edge. Admin statistics have space below the payment-attention card. Phone statistics wrap without pushing controls off screen. | `static/css/app.css` |
| V04 | One current sidebar/drawer page is highlighted. In-page anchors remain usable without becoming additional current pages. | `static/js/site.js`, `static/css/app.css` |
| V05/V08 | Friendly profile input was checked in the matrix, and the no-package view was checked after registration. Unpaid summaries from Batch B were retained; a new unpaid-summary interaction was not asserted in Batch C. | Existing Batch B presentation files, unchanged here |
| V06 | Future confirmed lessons show their attendance availability time and disabled completion/no-show controls. The display uses the same injected clock and attendance setting as the service. The explicit isolated override enables them. | `AttendancePresentationAdvice.java`, shared `attendance-time.jspf`, instructor dashboard and admin booking card |
| V07 | Public and instructor averages display one decimal. A stored 4.33 displays as 4.3; calculations and review storage are unchanged. | `SchoolPresentationAdvice.java`, public instructors and instructor dashboard JSPs |

The chosen account/form background is soft slate grey: it separates the white cards clearly while retaining dark navigation.

The shared CSS also gives disabled buttons readable colours, appropriate keyboard focus on white cards and dark navigation, visible focus for star/slot radios, and Windows forced-colour focus. Reduced-motion hover checks use the existing preference. The public cinematic home/package sections retain their existing design.

Existing routes, form field names, member headers, step comments and CRUD/OOP/SOLID labels were retained. No Git write commands were used. Booking, payment, security, queue and sort rules were not changed.

## Required build

The app was stopped before both builds. `npm.cmd run build:css` passed (existing Browserslist age warning only). The actual final `.\mvnw.cmd clean verify` summary is:

```text
[INFO] Tests run: 694, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  03:46 min
[INFO] Finished at: 2026-11-10T13:11:13+05:30
```

[Saved summary](screenshots/batchC/verify-summary.txt). Full log: `%TEMP%\drivespark-batchC-verify.log`. The packaged app's CSS and JavaScript match source byte for byte. JavaScript syntax and read-only `git diff --check` passed; Git emitted existing sample-data line-ending warnings only. Earlier successful runs were superseded after a browser focus check found an input selector overriding the visible outline; the final rule was rechecked in Chrome and the full build repeated.

## Test changes

Only one test file was added in this batch: `src/test/java/com/drivingschool/booking/AttendancePresentationAdviceTest.java`.

| Test method | Reason |
| --- | --- |
| `attendanceButtonsWaitUntilExactEnd` | Checks the one-second-before/exact-end boundary for the new display helper. |
| `explicitPreviewOverrideEnablesFutureConfirmedLesson` | Checks that display controls follow the existing configuration switch. |
| `overrideDoesNotOfferAttendanceForClosedOrPendingLessons` | Prevents offering attendance for non-confirmed records even with the override. |

No existing Java test was edited. The browser smoke assertion for average ratings was deliberately changed to one decimal; it still separately checks the stored average.

## Execution results

- Normal 33-step smoke: **30 PASS, 0 FAIL, 3 UNVERIFIED** (22-24, explained below).
- Normal browser interactions: **44/44 PASS**. Includes native registration/login, password toggles, keyboard/forced-colour focus, reduced motion, phone menus, one active navigation item, aligned cards, rating precision and future-attendance controls. A direct early completion request still returns 400 without changing the booking.
- Fresh early-attendance override: **5/5 PASS**. Future admin/instructor controls are enabled. B004 No-show keeps S004 at 18 credits; B005 completion reduces S002 from 18 to 17. These were native button actions on isolated seeded data only.
- Final normal folder: `C:\Users\USER\AppData\Local\Temp\drivespark-preview-e59c0e23d8654ce9b6459d2ac5315559`.
- Separate override folder: `C:\Users\USER\AppData\Local\Temp\drivespark-preview-a669e7e9f91f487199a685ff6afec5a1`.

Both app runs are stopped. Both launcher logs confirm run-only variables were cleared. Existing local data folders and seed files were untouched. Full logs remain in `%TEMP%\drivespark-batchC-app.log` and `%TEMP%\drivespark-batchC-override-app.log`.

## Evidence and limits

The matrix covers 34 visitor/student/instructor/admin page cases at **1536x864, 1920x1080, 1280x720 and 390x844**. All final captures returned 200, with no broken images, document horizontal overflow or out-of-bounds controls reported. Long pages and wide tables retain normal scrolling. Extra captures cover attendance controls, authentication alerts, keyboard focus and phone menus.

The solid-background text scan covers 26 authentication/account page cases at all four sizes: **104 readings, no text below 4.5:1 reported**. This is a scoped browser measurement, not a complete WCAG certification; photographic backgrounds, every possible state, other browsers, physical phone hardware and assistive technologies remain **UNVERIFIED**.

The unpaid overview summary at every viewport is **UNVERIFIED** in Batch C; its earlier Batch B evidence remains available.

The normal 33-step smoke keeps early attendance disabled. Steps 22-24 (new future lesson's debit, completed progress and new review) remain **UNVERIFIED** until the lesson ends. Seeded review moderation is explicitly separate from that chain. The isolated override run is separate evidence and does not establish those outcomes under the default time gate.

Browser evidence: [page matrix](screenshots/batchC/after-pages.json), [contrast](screenshots/batchC/after-contrast.json), [normal interaction checks](screenshots/batchC/normal-checks.json), [override interaction checks](screenshots/batchC/override-checks.json), [33-step smoke](screenshots/batchC/smoke-results.json), [functional probes](screenshots/batchC/functional-probes.json).

The [speaking pack](viva-2026/README.md), six diagrams and six printable cheat sheets were regenerated with 694 tests and 173 checked source references. Team-history answers and other evidence-dependent statements retain their existing NEEDS CHECK markers.
