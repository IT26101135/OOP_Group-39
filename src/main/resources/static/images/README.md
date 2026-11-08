<!-- Documentation owner: IT26101136 - [Name], Shared UI -->
# Local image guide

DriveSpark Academy currently uses original CSS landscapes, gradients and local SVG illustrations. No photo is needed to render a complete page. No Rivian logo, photo, video, text, icon, font file or code is included. No media is hot-linked. Inter is a local, openly licensed font; its source and license are documented in the root README.

The folder on disk is `src/main/resources/static/images/`. Its browser URL is `/images/`, with the application's context path added by `c:url` in JSP. Do not create another `static` or image subfolder.

## Reserved photo filenames and credits

These are optional replacement filenames, **not missing dependencies**. None of the photos below is shipped or requested by the current pages. Keep the original fallback unless an appropriately licensed photo has actually been downloaded into this existing folder.

| Filename | Home section / target | Current fallback and credit |
|---|---|---|
| `hero.jpg` | 1. Welcome hero; `.ds-home-hero .ds-cinematic-background` | Original mountain horizon and road built in `app.css`, IT26101136. No external photo used. |
| `promo.jpg` | 2. Monthly package; `.ds-home-promo .ds-cinematic-background` | Original contour gradients, IT26101136. No external photo used. |
| `safety-bg.jpg` | 5. Safety; `.ds-home-safety .ds-cinematic-background` | Original neutral gradient and circular artwork, IT26101136. No external photo used. |
| `safety-fg.png` | 5. Optional decorative foreground, currently not rendered | Original CSS circles are the present foreground, IT26101136. No external image used. |
| `location.jpg` | 6. Location; `.ds-location-art` | Original abstract street grid, IT26101136. It is not a real map. |
| `cta.jpg` | 7. Get behind the wheel; `.ds-home-cta .ds-cinematic-background` | Original CSS road scenery, IT26101136. No external photo used. |
| `flexible-bg.jpg` | 8. Flexible schedule; `.ds-home-flexible .ds-cinematic-background` | Original diagonal-line gradients, IT26101136. No external photo used. |
| `mission.jpg` | 11. Mission; `.ds-home-mission` | Original subtle neutral gradient, IT26101136. No external photo used. |

Existing SVG assets are project-authored: `favicon.svg`, `logo.svg`, `logo-icon.svg`, `hero-night-road.svg`, and `error-road.svg`. The current header/footer use an HTML text wordmark, and Home/About use CSS scenery. Legacy SVG illustrations remain available locally; the favicon and error illustration are still used.

## Adding a photo later

1. Download a free image from its original Unsplash or Pexels page after checking the current license. Do not use Rivian media, an unlicensed search-result image, or a remote image URL.
2. Save it under the matching filename above. Optimise its size before adding it. Keep the existing CSS gradients and overlay as the fallback.
3. For a cinematic background, add a local image layer to the relevant selector in `app.css` only **after the file exists**. For example:

   ```css
   .ds-home-hero .ds-cinematic-background {
     background-image: linear-gradient(0deg, #000D, #0003),
                       url('../images/hero.jpg'),
                       linear-gradient(150deg, #262626, #050505);
   }
   ```

4. Decorative backgrounds already have `aria-hidden="true"`. For a meaningful inline photo, add a short, accurate `alt`, width and height; lazy-load below-the-fold images. A purely decorative foreground should use `alt=""` and `aria-hidden="true"`. Keep essential information in HTML text.
5. Record its credit here and in the root README before distributing it: `filename — photographer — original photo page URL — source/license URL — download date — edits made`. Leave no invented credits.
6. Run `npm run build:css`, restart the app, and check 375/768/1280px, text contrast, keyboard use, reduced motion and offline loading. Do not add CSS references to filenames that are still absent.

Instructor portraits are optional local paths starting with a single `/`; other paths fall back to initials. Keep portrait files in an existing suitable asset folder and give every portrait a source credit. Do not introduce a remote portrait URL.

## Placeholder details

Colombo, the street address, opening hours, vehicle safety descriptions and sample numbers are project demonstration content. Get directions opens Contact while a real address is unconfirmed. Confirm real business details before replacing these notes.
