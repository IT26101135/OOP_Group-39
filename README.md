# DriveSpark Academy


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
- Authentication uses BCrypt hashes, server-side account statuses, sessions, role guards, CSRF checks, and simulated password reset tokens. New student accounts wait for admin approval.
- Students can edit profiles, enroll in one active package, view lesson balance/progress, request an available one-hour instructor slot, see FIFO position, cancel/reschedule under the 24-hour rule, and review a completed lesson once.
- Instructors can manage weekly availability and leave, accept/decline assigned requests, record attendance, lesson notes and skill scores, and view their reviews.
- Admins can manage students, instructors, packages and vehicles; process the FIFO request queue; reassign and force-cancel bookings; moderate reviews; and view report tables/CSV.
- Booking creation and processing share `BookingConflictChecker`; it checks enrollment, future time, instructor schedule/leave and conflicts, student overlap, vehicle status/type and vehicle overlap.
- Records are persisted as JSON Lines files through `FileRepository<T>` under `app.data-directory` (default `./data`).

Known limits: there is no payment gateway, email reset delivery, relational database, durable transaction layer, pagination, activity log, or report chart. The demo loader expects a clean data directory on its first run. The protected URL matrix has automated coverage for representative routes, but a complete manual browser audit across every listed URL and role remains to be recorded. See `REPORT.md` and `docs/TEST_CHECKLIST.md`.

### Demo seed and sample logins

The repeat-safe fixture runner starts only when `DRIVESPARK_DEMO_SEED_ENABLED=true`. Use a fresh data folder for a clean demo:

```powershell
$env:DRIVESPARK_DEMO_SEED_ENABLED = "true"
$env:DRIVESPARK_ADMIN_SEED_ENABLED = "false"
$env:APP_DATA_DIRECTORY = ".\data-demo"
.\mvnw.cmd spring-boot:run
```

All seeded users use `DriveSpark123!`:

- Admin: `admin@drivingschool.test`
- Instructors: `instructor1@drivingschool.test` through `instructor5@drivingschool.test`
- Students: `student1@drivingschool.test` through `student10@drivingschool.test` (students 1?7 Active, 8?9 Pending, 10 Suspended)

The fixtures contain 6 packages, 6 vehicles, 7 paid enrollments, 15 bookings and 10 reviews. The seed idempotency test runs the loader twice and checks stable record counts. Turn `DRIVESPARK_DEMO_SEED_ENABLED` off after the demo.

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
- Create `feature/ITxxxxxxxx-topic` branches from `develop` and open pull requests into `develop`.
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
| Page | `#0B1120` | Deep navy-charcoal background |
| Surface / raised | `#111827` / `#1F2937` | Cards and controls |
| Primary / hover | `#6366F1` / `#818CF8` | Indigo brand accents and focus |
| Accent / hover | `#F59E0B` / `#FBBF24` | Warm highlights |
| Main / secondary text | `#F1F5F9` / `#94A3B8` | Body copy and labels |
| Muted token | `#64748B` | Decorative-only token; too low contrast for normal text on the page background |
| Feedback | `#22C55E`, `#F59E0B`, `#EF4444`, `#38BDF8` | Success, warning, danger and info |

The page background, main text and secondary text have contrast ratios above WCAG AA for normal text. `#64748B` is retained as a design token but should only be used for non-text decoration; use `text-slate-400` for readable muted copy. Gradient action buttons use lighter indigo/sky fills with dark text so their labels stay legible. Font stacks prefer Inter and Poppins/Plus Jakarta Sans when locally available, then system UI. No remote fonts are loaded, so font rendering works offline.

### Shared pieces and assets

`common/navbar.jspf` owns the responsive role-aware navigation, `common/dashboard-sidebar.jspf` supplies the dashboard sidebar/mobile drawer, and `common/footer.jspf` owns the footer and shared toast/confirmation dialog. Reusable view fragments are in `common/components/`; shared component styles (`ds-button`, cards, inputs, tables, badges, alerts and page containers) are in `app.css`. `images/logo.svg` is the header/footer brand mark, `images/logo-icon.svg` is a compact brand icon, `images/favicon.svg` is the tab icon, `images/hero-night-road.svg` is the local home illustration, and `images/error-road.svg` illustrates error pages. Add real photos using the paths and accessibility guidance in `src/main/resources/static/images/README.md`.

### Reusable JSP fragment examples

Fragments are static JSP includes. Set their expected variables immediately before including them:

```jsp
<c:set var="status" value="${booking.status}" />
<%@ include file="/WEB-INF/views/common/components/status-badge.jspf" %>

<c:set var="ratingFieldName" value="rating" />
<c:set var="ratingLabel" value="Instructor rating" />
<c:set var="ratingValue" value="${param.rating}" />
<%@ include file="/WEB-INF/views/common/components/star-input.jspf" %>

<c:set var="emptyTitle" value="No bookings yet" />
<c:set var="emptyMessage" value="Your upcoming lessons will appear here." />
<%@ include file="/WEB-INF/views/common/components/empty-state.jspf" %>
```

`confirm-dialog.jspf` is included by the shared footer; mark the existing POST form with `data-confirm="Delete this record?"` to request confirmation. The native dialog supports keyboard dismissal and then submits that same form. `toasts.jspf` renders the existing `success`, `message`, `formError`, and `error` model attributes. `review-tabs.jspf` supplies admin moderation filter links. `star-display.jspf` uses `ratingValue` (including decimal averages); `skeleton.jspf`, `search-bar.jspf`, `pagination.jspf`, and `breadcrumbs.jspf` provide shared structures for pages that support those interactions.

### Credits

- Tailwind CSS CLI, configuration and utility framework: [Tailwind CSS v3 documentation](https://v3.tailwindcss.com/docs/installation).
- No Tailwind UI, Flowbite, HyperUI, Preline, daisyUI, paid templates or copied template blocks were used. The JSP layouts, component styles and SVG illustrations in this project were written for DriveSpark Academy.

### Current UI coverage notes

The shared dashboard sidebar/drawer is applied to student, instructor and admin dashboards. Browser error dispatches use branded 403, 404, and 500 JSPs; JSON error responses remain Spring Boot's JSON format. The reusable fragment directory includes accessible star input/display, modal, toast, tabs, skeleton, empty-state, search, pagination, badge, and breadcrumb structures. Most pages use the shared styles, but page-specific visual review and data-dependent information still vary. Social destinations intentionally use `#` placeholders until official profile URLs are supplied.
