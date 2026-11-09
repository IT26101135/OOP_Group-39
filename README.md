# DriveSpark Academy

Documentation owner: **IT26101135 - [Name]**. Member names remain placeholders until supplied by the team.


Driving school registration and scheduling system for SE1020 Object Oriented Programming.

## Technology

- Java 17 and Spring Boot 3
- Spring MVC, JSP and JSTL
- Maven Wrapper; WAR packaging
- JSON Lines file storage (`.txt`, one JSON object per line); no database

## Run the application

From the project root on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Then open <http://localhost:8080/>. The `/setup-check` page checks the application setup. The application stores runtime data in `./data`, relative to the directory from which it is started.

To create the packaged WAR:

```powershell
.\mvnw.cmd clean package
```

The project can also be built and its automated checks run with `.\mvnw.cmd clean verify`.

## How the code is organized

Java code is under `src/main/java/com/drivingschool`. Most features have their own package, such as `student`, `vehicle`, `review`, and `auth`.

A typical web request follows this path:

1. A **controller** receives the browser request and selects a page or response.
2. A **service** applies the feature's rules.
3. A **repository** reads or writes stored records.
4. A **model** represents the data being handled.

For example, to understand vehicle editing, start at `vehicle/controller/VehicleController.java`, follow the service it calls, then inspect `vehicle/repository/VehicleRepository.java` and `vehicle/model/Vehicle.java`. JSP views are in `src/main/webapp/WEB-INF/views`; static files are in `src/main/resources/static`.

Spring annotations connect some classes automatically. `@SpringBootApplication` starts the app and scans its packages. `@Controller` and `@Service` mark web and business classes. `@Bean` creates a shared object for dependency injection. For example, `DrivesparkAcademyApplication` provides a `Clock` set to the Colombo time zone, and services can receive that clock through their constructors.

## Current implementation

- Visitor pages: Home, About, Packages, Instructors (manual bubble sort), FAQ, Contact, and approved Reviews.
- Authentication uses BCrypt hashes, server-side account statuses, sessions, role guards, CSRF checks, and simulated password reset tokens. New student accounts are Active and can log in immediately. Accounts explicitly marked Pending or Suspended remain blocked.
- Students can edit profiles, request enrollment, view lesson balance/progress, request an available one-hour instructor slot with an active paid package, see FIFO position, cancel/reschedule under the 24-hour rule, and review a completed lesson once.
- Instructors can manage weekly availability and leave, accept/decline assigned requests, record attendance, lesson notes and skill scores, and view their reviews.
- Admins can manage students, instructors, packages and vehicles; process the FIFO request queue; reassign and force-cancel bookings; moderate reviews; and view report tables, animated SVG charts, CSV exports and a white print layout.
- Booking creation and processing share `BookingConflictChecker`; it checks enrollment, future time, instructor schedule/leave and conflicts, student overlap, vehicle status/type and vehicle overlap.
- Records are persisted as JSON Lines files through `FileRepository<T>` under `app.data-directory` (default `./data`).

Known limits: there is no payment gateway, email reset delivery, relational database, durable transaction layer, pagination, or activity log. The demo loader expects a clean data directory on its first run. The protected URL matrix has automated coverage for representative routes, but a complete manual browser audit across every listed URL and role remains to be recorded. See `REPORT.md` and `docs/TEST_CHECKLIST.md`.

### Demo seed and sample logins

The repeat-safe fixture runner starts only when `DRIVESPARK_DEMO_SEED_ENABLED=true`. Use a fresh data folder for a clean demo:

```powershell
$env:DRIVESPARK_DEMO_SEED_ENABLED = "true"
$env:DRIVESPARK_ADMIN_SEED_ENABLED = "false"
$env:APP_DATA_DIRECTORY = Join-Path $env:TEMP "drivespark-viva-demo"
.\mvnw.cmd spring-boot:run
```

All seeded users use `DriveSpark123!`:

- Admin: `admin@drivingschool.test`
- Instructors: `instructor1@drivingschool.test` through `instructor5@drivingschool.test`
- Students: `student1@drivingschool.test` through `student10@drivingschool.test` (students 1–7 Active, 8–9 Pending, 10 Suspended)

The fixtures contain 6 packages, 6 vehicles, 9 enrollments (7 Paid, 1 Awaiting payment, 1 Cancelled), 15 bookings and 10 reviews. The seed idempotency test repeats the loader, checks stable record counts and ensures handled receipts and exhausted balances stay unchanged. Turn `DRIVESPARK_DEMO_SEED_ENABLED` off after the demo. The first seed expects a fresh isolated data directory; do not seed over real coursework records. Names such as `DemoMake` and placeholder contact/social links are demonstration content, not real businesses or destinations.

### Module folders and owners

| Source folder | Owner | Main responsibility |
|---|---|---|
| `auth`, `student`, `common`, `admin` | IT26101135 | Authentication, student CRUD, shared foundation and integration |
| `instructor` | IT26101133 | Instructor profiles, availability, lesson notes, bubble sort |
| `booking` | IT26101134 | Slot generation, conflict checking, FIFO scheduling |
| `vehicle`, `sample` | IT26101132 | Vehicle CRUD/warnings and repeat-safe sample data |
| `learningpackage` | IT26101136 | Packages, enrollment and package pages |
| `review`, `report` | IT26101137 | Reviews, moderation and admin reports |

## Tests

Automated tests are under `src/test/java`. To run the full verification suite on Windows:

```powershell
.\mvnw.cmd clean verify
```

## Team workflow

- `main` is for stable releases; `develop` is the integration branch.
- The project lead performs all Git operations. The member branch/PR plan is produced after Phase 6; this documentation does not run Git commands.
- Coordinate changes to shared APIs and storage rules in `CONTRACTS.md` with the team before merging.
- Do not push directly to `main` after the initial setup. Use your own Git identity.

## Evaluation preparation: ownership, CRUD, and OOP

Each member should be ready to explain one complete request through the
controller, service, model, and repository, including validation and any
reason a delete is refused. The evaluation expects questions from anywhere in
the project, so every member should also be able to trace another feature and
explain the shared `User`, `FileRepository`, authorization, and CSRF code. CRUD
coverage in this project is:

| Member | CRUD contribution | Useful code to explain |
|---|---|---|
| IT26101135 | Register/read/update a student profile; delete an account only when it has no lesson or review history | `StudentAccountController`, `StudentAccountService`, `StudentProfileService`, `AuthService`, `User`, `Student` |
| IT26101133 | Create/list/update/delete instructor records and login identities; sort instructors by experience | `InstructorAccountService`, `InstructorService`, `InstructorSorter`, `Instructor`, `InstructorAccount` |
| IT26101134 | Create/read lesson requests, update them by assignment/completion/cancellation, and delete a cancelled request; process pending requests FIFO | `BookingService`, `BookingRequestQueue`, `Booking`, `BookingStatus`, `LessonIntervalUtils` |
| IT26101132 | Create/list/update/delete vehicles; deletion is blocked when lesson history refers to a vehicle | `VehicleController`, `VehicleService`, `VehicleStore`, `VehicleRepository`, `Vehicle` |
| IT26101136 | Create/list/update/delete packages; deletion is blocked when lesson history refers to a package | `AdminManagementController`, `LearningPackageController`, `LearningPackageService`, `LearningPackage` |
| IT26101137 | Create/read/update/delete reviews and approve/hide them | `ReviewController`, `AdminReviewController`, `ReviewService`, `ReviewStore`, `Review` |

Delete operations preserve referenced history: a student cannot delete an
account with lesson/review records, an instructor or package cannot be deleted
when lessons refer to it, a vehicle cannot be deleted when assigned in lesson
history, and a lesson request must be cancelled before permanent deletion.
Use deactivation/cancellation when preserving those records is appropriate.

OOP concepts visible across these examples:

- **Encapsulation:** model fields are private; constructors and methods validate changes (for example, `Vehicle` and `LearningPackage`).
- **Inheritance and polymorphism:** `Student`, `Admin`, and `InstructorAccount` extend `User`; `UserRepository` reads different account subtypes through the common `User` type.
- **Abstraction:** `Identifiable`, `StudentLookup`, `VehicleStore`, and `ReviewStore` expose contracts; `FileRepository<T>` shares file persistence behind an abstract base class.
- **SOLID examples:** MVC separates web handling, business rules, and storage responsibilities (SRP); the store interfaces let vehicle/review services depend on contracts (DIP) and keep those contracts focused (ISP). `User` subclasses share the same role/dashboard contract (LSP). These are examples of applying SOLID, not a claim that every class perfectly follows every principle; explain trade-offs and concrete code during evaluation.

## Design guide

### Tailwind CSS build

The site uses Tailwind CSS **3.4.17** via the local npm CLI (Option C, because Node is installed). The generated `src/main/resources/static/css/tailwind.css` is committed, so the normal Maven app run works offline and does not need Node or a CDN. When JSP classes or design tokens change, rebuild the bundle:

```powershell
npm install
npm run build:css
```

Then run the app as usual with `.\mvnw.cmd spring-boot:run`. Tailwind configuration lives in `tailwind.config.js`; shared tokens and `@apply` components live in `src/main/resources/static/css/app.css`. Use the `ds-*` component classes in JSPs for shared layout and buttons/cards, and put browser interaction in `src/main/resources/static/js/site.js`.

### Palette and typography

| Token | Value | Use |
|---|---|---|
| Page | `#050505` | Near-black page background |
| Surface / raised | `#0A0A0A` / `#171717` | Quiet surfaces and controls |
| Primary / hover | `#F59E0B` / `#FBBF24` | Primary actions and small warning highlights |
| Main text | `#FFFFFF` | Headings and important values |
| Body / secondary text | `#D4D4D4` / `#A3A3A3` | Paragraphs, labels and supporting information |
| Divider / field border | `#262626` / `#525252` | Thin separators and form outlines |

The design uses one local variable font: **Inter**, served as `DriveSpark Inter` from `static/css/inter-latin-variable.woff2`. Its unmodified SIL Open Font License is beside it in `INTER_LICENSE.txt`. The font was copied from the npm package `@fontsource-variable/inter` version **5.3.0**; there is no runtime font CDN request and no additional npm dependency to install for it. The framework build remains Tailwind CSS 3.4.17.

Headings use a light-to-medium weight and responsive sizes up to 88px. Shared focus outlines are white with an offset; amber stays restrained. Reduced motion disables the hero zoom, parallax, smooth scrolling and reveal animation. Essential content and native links/forms remain usable without JavaScript. Contrast measurements and automated accessibility results describe the checked states, not a WCAG certification.

### Shared pieces and assets

`common/navbar.jspf` owns the role-aware navigation, `common/dashboard-sidebar.jspf` supplies the workspace rail/native mobile menu, and `common/footer.jspf` supplies the black footer and shared toast/confirmation dialog. Reusable fragments live in `common/components/`; shared styles live in `app.css`. Rebuild `tailwind.css` after changing CSS or JSP classes.

The current header and footer use our HTML text wordmark. Home and About use original CSS road scenery and gradients; `images/favicon.svg` is our own monochrome car icon. The remaining local `logo.svg` is used by password reset, and `error-road.svg` illustrates error pages. No Rivian logo, media, font file, icon or code is included.

The [image guide](src/main/resources/static/images/README.md) lists the ten supplied local WebP photographs, measured sizes and exact user-provided credits. Missing optional files retain original fallbacks without an image request.

The home spotlight displays the first active package from existing data; it implies no discount. Its review carousel reads only the approved public review page, copies text safely, and has manual Previous/Next controls plus a normal-link fallback. Location and opening hours are clearly marked placeholders.

### Reusable JSP fragment examples

Fragments are static JSP includes. Set their expected variables immediately before including them:

```jsp
<c:set var="status" value="${booking.status}" />
<%@ include file="/WEB-INF/views/common/components/status-badge.jspf" %>

<c:set var="ratingFieldName" value="rating" />
<c:set var="ratingFieldId" value="review-${review.id}-rating" />
<c:set var="ratingLabel" value="Instructor rating" />
<c:set var="ratingValue" value="${param.rating}" />
<%@ include file="/WEB-INF/views/common/components/star-input.jspf" %>

<c:set var="emptyTitle" value="No bookings yet" />
<c:set var="emptyMessage" value="Your upcoming lessons will appear here." />
<%@ include file="/WEB-INF/views/common/components/empty-state.jspf" %>
```

`confirm-dialog.jspf` is included by the shared footer; mark the existing POST form with `data-confirm="Delete this record?"` to request confirmation. The native dialog supports keyboard dismissal and then submits that same form. `toasts.jspf` renders the existing `success`, `message`, `formError`, and `error` model attributes. `review-tabs.jspf` supplies admin moderation filter links. `star-display.jspf` uses `ratingValue` (including decimal averages); `skeleton.jspf`, `search-bar.jspf`, `pagination.jspf`, and `breadcrumbs.jspf` provide shared structures for pages that support those interactions.

### Credits and licenses

| Source | Used here | License / attribution |
|---|---|---|
| [Tailwind CSS 3.4.17](https://github.com/tailwindlabs/tailwindcss/tree/v3.4.17) and [v3 documentation](https://v3.tailwindcss.com/docs/installation) | Local CLI, utilities and responsive/focus patterns adapted into project components | MIT; copyright Tailwind Labs, Inc. See the [upstream license](https://github.com/tailwindlabs/tailwindcss/blob/v3.4.17/LICENSE) and included [license notice](docs/TAILWIND_LICENSE.txt). |
| [Inter](https://github.com/rsms/inter), distributed through [Fontsource](https://fontsource.org/fonts/inter) (`@fontsource-variable/inter` 5.3.0) | Local `inter-latin-variable.woff2`, copied from the package's `inter-latin-wght-normal.woff2` | Copyright 2016 The Inter Project Authors. SIL Open Font License 1.1; [included unmodified license](src/main/resources/static/css/INTER_LICENSE.txt). |
| DriveSpark JSP layouts, `ds-*` CSS, vanilla JS, CSS landscapes and local SVG assets | Coursework-specific components, text and original artwork | File headers identify the team owner. No third-party template or photo is claimed as project-authored. |
| [Rivian](https://rivian.com/) | Visual inspiration for restrained typography, open spacing and cinematic section proportions | Inspiration only. No Rivian assets, text, font files, icons, media or code were copied or bundled. |

No paid Tailwind UI code, CDN scripts or hot-linked media are bundled. The supplied local photographs and their Pexels/Unsplash credits are listed in the local photography Credits section below; additional photographs require attribution before use. No Flowbite, HyperUI, Preline, daisyUI or Meraki UI package was installed. Dependencies already declared in `pom.xml` retain their upstream licenses.

### Current UI coverage notes

The twelve-section home page, public catalog/detail/instructor/review/information pages, authentication forms and role workspaces use the premium black/white/amber theme. Role menus and existing URLs are retained. The workspace CSS covers related student, instructor and admin forms and lists; Java services, the manual bubble sort and linked-node FIFO were not changed during this restyle.

Tabs support arrow keys, Home/End and direct fragment links. Skip links move focus to main content; native mobile disclosures remain usable without scripts. Public review cards retain `.ds-review-card`, a plain-text `blockquote`, metadata `p` and `.ds-star-display` so the home reader can reuse approved content. Print styles use white paper, dark text and neutral chart bars; interactive controls are omitted from paper output.

The footer contains no pretend social links. Confirm the real address and hours before replacing placeholder copy. See [the test checklist](docs/TEST_CHECKLIST.md) for the final browser checks and remaining manual limits.

## Viva and ownership documents

- [CRUD map](docs/CRUD_MAP.md): 99 verified class/method rows connecting eight entities to repositories, services, URLs and JSPs.
- [OOP/SOLID map](docs/OOP_SOLID_MAP.md): ten concrete examples per member, with the limits of each example explained.
- [Viva guide](docs/VIVA_GUIDE.md): exact owned Java/JSP/CSS/JS files, five-sentence stories, CRUD chains, OOP/SOLID references, five questions and five demo interactions per member.
- [Test checklist](docs/TEST_CHECKLIST.md): actual dated automated results and browser verification limits.

Shared files have one owner: Java foundation, startup and integration documentation belong to 135; `tailwind.config.js` and npm build wiring belong to 135; `app.css`, generated `tailwind.css`, `site.js`, navigation/layout and general shared components belong to 136. Review stars and moderation filters belong to 137. The viva guide lists the exact shared-file assignments; module use does not transfer file ownership.

## Shared component maintenance

| Component | Source | How to use it |
|---|---|---|
| Cards, buttons, labels, fields and focus rings | `static/css/app.css` → `static/css/tailwind.css` | Use existing `ds-*` classes and Tailwind utilities, then rebuild CSS. Do not edit the generated bundle by hand. |
| Navigation and dashboard menu | `common/navbar.jspf`, `common/dashboard-sidebar.jspf` | Existing role data comes from NavigationInterceptor; native links/details remain usable without JS. |
| Two-panel tabs | `common/components/tabs.jspf` | Set `tabsLabel`, `firstTabTitle`, `secondTabTitle`, `firstPanelId`, `secondPanelId`; give panels unique IDs and keep both visible in fallback HTML. JS adds keyboard tab behaviour. |
| Star input | `common/components/star-input.jspf` | Set `ratingFieldName`, a unique `ratingFieldId` per form, `ratingLabel` and `ratingValue`; the existing submitted field name remains unchanged. Native radio inputs work without JS. |
| Fractional star display | `common/components/star-display.jspf` | Set decimal `ratingValue`; clipped fill and an accessible label show the stored average. |
| Toasts and confirmation | `common/components/toasts.jspf`, `confirm-dialog.jspf` | Shared footer includes these once. Success messages auto-hide; errors stay until dismissed. `data-confirm` protects an existing form without changing its route. |
| Optional motion, loading and print | `static/js/site.js` | One local file handles progressive enhancement. Animation respects reduced motion; form validation and named inputs retain their existing behaviour. |
| Report charts, exact tables and print | `admin/reports.jsp`, `static/css/report.css` | Bars use saved counts, tables keep exact values, and print CSS changes the report to white paper. Export is a normal admin-only CSV link. |

The checked-in CSS and local assets let the app run without Node after dependencies have been downloaded. A first Maven/npm installation still needs its dependencies available locally or a network connection. `target/`, `node_modules/`, runtime demo folders and credentials should not be included in member PRs. Include generated `src/main/resources/static/css/tailwind.css` so running the app does not require a CSS build.

## Manual enrollment payments

New student accounts can log in immediately. Enrolling saves one **Awaiting payment** request per student, the current package price and a reference such as `ENR-E001`. It does not activate a package, grant lessons or start the validity countdown. The payment instructions and My enrollments pages show the reference and captured amount.

Pay by bank transfer or at the school office, then send the receipt and reference using your own email service. Set `ADMIN_EMAIL` before starting the app to change the contact address; `app.payment.admin-email` in `application.properties` defaults to `admin@drivespark.test`. This is a placeholder address. Contact the school for its real bank details. **The system does not send email and does not upload receipts.** Administrators use Awaiting payment to confirm a receipt or cancel an unpaid request. Both actions use the shared confirmation dialog; cancellation has an optional reason. Confirmation starts validity on that day and grants the package lesson count.

Enrollment `status` is `AWAITING_PAYMENT`, `PAID` or `CANCELLED`. The existing `paymentStatus` getter maps Paid to `PAID`, and both other states to `UNPAID`. References are derived from the permanent enrollment ID. Old saved receipts without the new state or reference still load as Paid; an explicitly old `UNPAID` receipt loads as Awaiting payment. Missing payment fields also default to Paid for old-format compatibility. Existing paid balances and expiry dates remain intact; no data files need deleting.

New route: `GET /enrollments/{id}/payment` (signed-in owning student only). Existing POST routes and form field names remain unchanged. Awaiting payment, Cancelled, missing or unrelated receipts cannot supply a balance through the account repository. Old-format paid receipts remain valid; no data files need deleting. Every request, reschedule, assignment, reassignment, FIFO-processing, instructor acceptance and completion path checks the saved Paid receipt again. Unpaid or Cancelled legacy queue heads are rejected with a clear reason. Cancellation and rejection remain available for cleanup.

## Earlier Phase 5 verification

2026-11-08, Java 17, `.\mvnw.cmd clean verify`:

```text
Tests run: 575, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 01:00 min
```

`npm run build:css`, compile, documentation links and `git diff --check` also passed. New Phase 5 tests add student account CRUD/history protection (6) and package lifecycle/deletion protection (4). Existing tests were not weakened or changed. Accessibility evidence and its limits are in [the checklist](docs/TEST_CHECKLIST.md); this is not the complete Phase 6 role audit.

## Premium UI maintenance and verification

Shared presentation owner: **IT26101136** (`app.css`, generated `tailwind.css`, `site.js`, layout fragments, local font/license and image guide). Root README and test evidence owner: **IT26101135**. Report stylesheet owner: **IT26101137**. Existing module ownership remains unchanged.

For CSS/template changes, run `npm run build:css`, then restart the app. For the full Java suite, use `.\mvnw.cmd clean verify`. Include generated `tailwind.css`, the local font and its license in the eventual member PR; exclude `target/` and `node_modules/`. No Git write commands are performed by this workflow.

Phase E verification is recorded in [docs/TEST_CHECKLIST.md](docs/TEST_CHECKLIST.md). Accessibility tooling is a temporary development check and is not loaded by the application. Member names remain `[Name]` until the group supplies them.

### Final restyle verification (2026-11-08)

```text
Tests run: 575, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 57.501 s
```

The final browser audit checked 32 page/role combinations at 375/768/1280px, plus expanded controls and error pages. It found no automated WCAG-tagged violations in the checked final states. Print previews/PDFs and CSV export were checked. Screen-reader testing and full measured contrast over every gradient state remain UNVERIFIED; see the checklist for the exact scope. No tests were changed.

### Manual-payment demonstration

1. Register and log in as a new Active student in temporary demo data.
2. Choose a package and Enroll. Show its Awaiting payment receipt, amount and reference.
3. Open Book / My bookings. Booking is blocked with a link to My enrollments; forged forms are checked on the server too.
4. In a separate admin session, open **Awaiting payment** (`/admin/enrollments`), check the receipt externally and confirm **Mark as Paid**. Double clicks or competing decisions get an already-handled message. Inactive courses and Suspended/Pending students cannot be confirmed.
5. Return to the student session, choose an available instructor hour and send a booking request.

Admin payment POST routes are `/admin/enrollments/mark-paid` and `/admin/enrollments/cancel-request`. Both accept `enrollmentId`; cancellation also accepts optional `reason` (300 characters maximum). Cancelled receipts remain in history with their explanation and cannot be deleted through the student page. The admin dashboard shows the waiting count; reports and CSV Revenue contain Paid amounts only.

Student8's fresh demo data includes an unpaid request and a cancelled historical request for testing the red badge. Student9 remains the explicit Pending-account example. The loader preserves existing accounts and decisions; it does not change old saved accounts into new demo statuses.

Payment and booking writes share one lock in the running Java application. This prevents two admins handling the same receipt and prevents a booking write racing a payment decision. File storage still has no durable multi-file transaction or coordination between several application instances; run one instance per data directory.

### Manual payment Phase 2 verification (2026-11-08)

```text
Tests run: 608, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 01:11 min
```

CSS build, compilation and documentation links passed. The complete temporary-data browser hand-off was checked at 375/768/1280px, including a blocked forged unpaid booking, admin confirmation, successful Paid booking, cancellation, red Cancelled labels, waiting-count changes and the no-payments empty state. The app was restarted and handled decisions remained unchanged. Reports and CSV contain Paid revenue only. See [TEST_CHECKLIST.md](docs/TEST_CHECKLIST.md) for test changes, evidence and limits.

## Photo Credits

hero-road: Photo by Marcelo Mora on Pexels (Pexels License) - https://www.pexels.com/photo/scenic-mountain-road-at-sunset-32540808/

safety-bg: Photo by Erik Mclean on Pexels (Pexels License) - https://www.pexels.com/photo/photo-of-a-car-s-dashboard-9661322/

location: Photo by Erik Mclean on Pexels (Pexels License) - https://www.pexels.com/photo/cars-parked-near-building-on-street-in-daylight-7442030/

cta-drive: Photo by Mustafa Ödemiş on Pexels (Pexels License) - https://www.pexels.com/photo/rainy-road-trip-view-through-windshield-34730119/

flexible-bg: Photo by Anna Hinckel on Pexels (Pexels License) - https://www.pexels.com/photo/a-time-lapse-photography-of-a-highway-at-night-4599217/

mission: Photo by Mitchell Henderson on Pexels (Pexels License) - https://www.pexels.com/photo/road-in-mountains-15993667/

vehicle-manual: Photo by FBO Media on Pexels (Pexels License) - https://www.pexels.com/photo/sleek-blue-sport-hatchback-in-minimalist-setting-35549144/

vehicle-automatic: Photo by Oli Liao on Pexels (Pexels License) - https://www.pexels.com/photo/sleek-black-sedan-parked-in-urban-setting-35628774/

vehicle-motorcycle: Photo by Raj Pillay on Pexels (Pexels License) - https://www.pexels.com/photo/triumph-street-triple-765-r-27704082/

promo-lesson: Photo by Bas Peperzak on Unsplash (Unsplash License) -
 https://unsplash.com/photos/driving-test-instructor-and-student-tyhpK_QelPo

## Credits — local photography

No Rivian assets are used; the design is only inspired by premium automotive websites.

Photos are used under the Pexels and Unsplash licences. Brands and people shown do not endorse this project.

The following credits are supplied for the intended photographs. As checked on 2026-11-09, all ten supplied WebP files exist directly in static/images (the photos subfolder is absent). They are used in place, with original gradients/SVGs retained underneath. mission.webp exceeds 250 KB and is left unchanged. No photos were generated or resized. File status, delivery sizes, conditional placement and missing-file behavior are documented in [the local image guide](src/main/resources/static/images/README.md).

| Photo basename | File status | Bytes | Dimensions | Supplied credit |
|---|---|---|---|---|
| hero-road.webp | PRESENT | 122852 | 1920 × 1280 | Photo by Marcelo Mora on Pexels (Pexels License) - https://www.pexels.com/photo/scenic-mountain-road-at-sunset-32540808/ |
| promo-lesson.webp | PRESENT | 131188 | 1600 × 1066 | Photo by Bas Peperzak on Unsplash (Unsplash License) - https://unsplash.com/photos/driving-test-instructor-and-student-tyhpK_QelPo |
| safety-bg.webp | PRESENT | 55860 | 1600 × 1066 | Photo by Erik Mclean on Pexels (Pexels License) - https://www.pexels.com/photo/photo-of-a-car-s-dashboard-9661322/ |
| location.webp | PRESENT | 84334 | 1600 × 1066 | Photo by Erik Mclean on Pexels (Pexels License) - https://www.pexels.com/photo/cars-parked-near-building-on-street-in-daylight-7442030/ |
| cta-drive.webp | PRESENT | 245918 | 1600 × 1066 | Photo by Mustafa Ödemiş on Pexels (Pexels License) - https://www.pexels.com/photo/rainy-road-trip-view-through-windshield-34730119/ |
| flexible-bg.webp | PRESENT | 110698 | 1600 × 1066 | Photo by Anna Hinckel on Pexels (Pexels License) - https://www.pexels.com/photo/a-time-lapse-photography-of-a-highway-at-night-4599217/ |
| mission.webp | PRESENT | 303206 | 1600 × 1067 | Photo by Mitchell Henderson on Pexels (Pexels License) - https://www.pexels.com/photo/road-in-mountains-15993667/ |
| vehicle-manual.webp | PRESENT | 29378 | 1200 × 800 | Photo by FBO Media on Pexels (Pexels License) - https://www.pexels.com/photo/sleek-blue-sport-hatchback-in-minimalist-setting-35549144/ |
| vehicle-automatic.webp | PRESENT | 29674 | 1200 × 675 | Photo by Oli Liao on Pexels (Pexels License) - https://www.pexels.com/photo/sleek-black-sedan-parked-in-urban-setting-35628774/ |
| vehicle-motorcycle.webp | PRESENT | 42800 | 1200 × 800 | Photo by Raj Pillay on Pexels (Pexels License) - https://www.pexels.com/photo/triumph-street-triple-765-r-27704082/ |
