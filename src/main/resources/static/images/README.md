<!-- Documentation owner: IT26101136 - [Name], Shared UI -->
# Local photo guide

Checked 2026-11-09: all ten supplied WebP photographs exist directly in `src/main/resources/static/images/`. The proposed photos subfolder is absent. Files are used in their existing locations; none was moved, generated, downloaded or resized. Warning: mission.webp is 303,206 bytes, over 250 KB. No supplied photo exceeds 2000px width.

Optional filenames are each basename below plus `.webp`, `.jpg`, `.jpeg` or `.png` (checked in that order). Browser paths are `/images/<filename>` for the supplied files; `/images/photos/<filename>` is also supported, prefixed using `c:url`. Recommended delivery size: 1600px wide and under 200 KB. Flag any supplied file over 250 KB or wider than 2000px before use; never resize originals automatically.

## Inventory and credits

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

Every named photograph has a user-supplied credit, so no CREDIT NEEDED entries apply to this inventory. Any additional photo must be marked CREDIT NEEDED until its photographer, source and licence are provided.

## Placement and fallback

- hero-road: home hero and account-form side panels; eager loading.
- promo-lesson: home package spotlight and instructors header.
- safety-bg, location, cta-drive, flexible-bg and mission: matching home sections; mission also supplies the about landscape.
- vehicle-manual, vehicle-automatic and vehicle-motorcycle: package panels, package header and mega-menu categories. Refresher uses the manual illustration/photo.

LocalPhotoAdvice checks the fixed local filenames before rendering. The shared local-photo.jspf supplies escaped descriptions, width/height, loading priority and a dark overlay; CSS uses object-fit cover. The existing gradients/SVGs remain underneath. Missing files generate no image elements or requests, including with JavaScript disabled. If a file disappears after rendering, site.js removes the failed element and its overlay. Images are not hot-linked.

Fallback CSS landscapes, thumbnail gradients and existing SVG illustrations are original project assets. Actual photo placement and contrast are checked during the Phase 4 browser pass. The supplied dimensions above are measured from the original files. A street/building photograph is illustrative and does not identify the academy's placeholder address. People and vehicles pictured are illustrative rather than academy records.
