# Batch 1: public copy review

Completed 10 November 2026 (Asia/Colombo). This batch changes public page copy,
sign-in and registration copy, password recovery copy, shared public navigation
and footer text, and error wording. Home photo structure and styling remain for
Batch 5. Seed records remain for Batch 3, so their old names, descriptions and
review comments can still appear on public pages until that batch.

## Changes

- Replaced slogans and project explanations with short lesson, package and booking information.
- Removed unconfirmed location/hours and unsupported inspection/dual-control claims.
- Added empty `app.school.email`, `phone`, `address` and `opening-hours` settings.
  `SchoolPresentationAdvice` and `school-contact.jspf` share and escape those values;
  empty lines are hidden. The payment-contact setting is outside this batch and
  will be reconciled with these settings in Batch 2.
- Added shared public formatting for LKR prices, review dates and lesson-type labels.
- Kept the existing reset-code flow and security checks; the page does not claim
  that reset email was sent.
- Retained routes, form field names, member headers, step comments, CRUD/OOP/SOLID
  labels, bubble sort and FIFO implementation. No Git writes were performed.
- Honest fixture limitations remain documented in README.md and sample-data/README.md.

## Verification

No app was listening on ports 8080 or 18080 at the initial process check.
`npm.cmd run build:css` passed. The `npm.ps1` wrapper was blocked by local
PowerShell execution policy; using the installed `.cmd` wrapper succeeded.
Browserslist reported its existing outdated caniuse-lite database warning.
No dependency updates were made.

The first Maven run failed with 16 assertions, catching an accidental redirect
punctuation change and old error-message expectations. The redirect was corrected;
route and security assertions were retained. The final Java 17 run of
`.\mvnw.cmd clean verify` completed at 02:08:17:

```text
Tests run: 646, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 03:16 min
```

Every changed test and its reason:

| Test file | Reason |
| --- | --- |
| common/exception/AuthorizationErrorHandlerTest.java | Expect the revised 403 message and recovery advice. |
| common/exception/GlobalExceptionHandlerTest.java | Expect the friendly storage-error message; still assert HTTP 500 and the error view. |
| student/StudentProfileWebIntegrationTest.java | Update its shared storage-error message assertion; retain the failed-write and data-preservation checks. |

The verified WAR ran on port 18080 in a fresh PowerShell process with
`DRIVESPARK_DEMO_SEED_ENABLED=true`, `DRIVESPARK_ADMIN_SEED_ENABLED=false` and
`APP_DATA_DIRECTORY` pointing to the new empty directory:

```text
C:/Users/USER/AppData/Local/Temp/drivespark-copy-batch1-362f419c2da64ef4ab447d78b1e5796a
```

Chrome headless, driven through the installed Playwright package, used a 1536 x
864 viewport and reduced motion. All 54 browser checks passed. For each of visitor,
student, instructor and admin, the checks opened:

```text
/
/about
/faq
/contact
/packages
/packages/P001
/instructors
/public/reviews
/login
/register
/forgot-password
/setup-check
/missing-page
```

Checks asserted expected HTTP status, absence of targeted old static copy,
hidden unconfigured contact lines and formatted public prices. Existing signed-in
redirects from login/registration were followed. Two additional checks covered a
CSRF-protected invalid contact submission and a student's HTTP 403 recovery page.
No destructive actions were clicked and no real contact messages were submitted.

Browser 500 recovery, empty-data variants, populated contact settings and mobile
layouts are **UNVERIFIED**. Automated error-handler tests cover the shared 500
message. Browser checks verified rendered text and responses; visual screenshot
comparison is **UNVERIFIED** and remains part of Batch 5.

Temporary execution evidence:

```text
%TEMP%/drivespark-batch1-verify-final.log
%TEMP%/drivespark-copy-batch1-browser.json
%TEMP%/drivespark-copy-batch1-app.log
%TEMP%/drivespark-copy-batch1-run.ps1
%TEMP%/drivespark-copy-batch1-browser.cjs
```

The isolated app was stopped and its run-only demo variables cleared in `finally`.
Batch 2 has not started.
