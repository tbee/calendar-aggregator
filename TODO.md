# TODO

Where Shall We Dance? (this project, formerly dancemoments) and the dance partner search (the
shallwedance.org project) are two applications for two questions — *where* and *who* — that point at each
other. This file records what is left to do on the *where* side and on the seam between the two.

Decisions taken (2026-11-08):

- **English is the language of the site.** There is no i18n setup and none is added now: `lang="en"`, English
  copy, English date formatting. Event and location names stay in the language their source wrote them.
  (Consequence: a Dutch event on an English page. The venue and event subjects are the strongest local signal;
  per-language URL variants are only worth it if the Dutch long tail turns out to matter more than the
  international reach.)
- **Language belongs to the user, not to the location** (a Dutch dancer visits a German location), so a
  `CalendarLocation` gets no language or country metadata.
- **`showHidden` stays** an undocumented filter parameter that may be used without login. It is covered by the
  noindex rule below, so it cannot create indexable variants. It does reveal sources marked hidden to whoever
  guesses the parameter; if that ever matters, put a random token in it.
- **The two applications stay separate** for now. The iframe on `shallwedance.org/where` stays until phase 3.

## Phase 1 — make the public pages indexable (done, 2026-11-08)

- [x] **Fixed the document.** `month.html`/`list.html` passed their own `<head>` and `<body>` into
      `template.html`, which inserted them inside its own, so the served HTML had two of each. They now pass
      named fragments (`pageHead`/`pageBody`) that the layout inserts as content.
- [x] **A real head** in `template.html`: `charset`, `viewport`, a title per view, meta description, canonical,
      robots, Open Graph, `rel="alternate"` for the ical feed, and `WebSite` JSON-LD.
- [x] **`PageMeta`** computes title/description/canonical/robots in one pure place. The bare calendar is the
      page; the list view and **every query string variant** are `noindex, follow` and point their canonical at
      `/`, so filtered variants cannot compete in the index.
- [x] **Internal links are clean**: the `?lat=&lon=&distance=` that every link used to carry is gone, so the
      bare pages are reachable by following links.
- [x] **`/robots.txt`**, generated per deployment: the reader host allows everything and names its sitemap, the
      writer host disallows everything.
- [x] **`/sitemap.xml`** with the calendar (the only indexable page so far).
- [x] **`X-Robots-Tag: noindex, nofollow`** on `where-admin.shallwedance.org`, which serves the same public
      pages as the reader.
- [x] Tests: `PageMetaTest`, `PublicPageHtmlTest` (one head, one body, the head content, clean links),
      `RobotsAndSitemapTest`, `AdminHostNoIndexFilterTest`. App suite: 46 green.

Release notes:

- No URL changed; the ical feed and the iframe are untouched.
- After deploying, submit `https://where.shallwedance.org/sitemap.xml` in Search Console. A *Domain* property on
  `shallwedance.org` already covers the subdomain; the submission is what matters.
- Check after deploy: `curl -s https://where.shallwedance.org/ | head -40`, `/robots.txt`, `/sitemap.xml`, and
  `curl -sI https://where-admin.shallwedance.org/ | grep -i x-robots-tag`.

## Phase 2 — the pages that can actually rank

- [ ] `/locatie/{slug}` per `CalendarLocation` (its `name` is already unique): upcoming dates, address, map
      link, labels, links to the sources, and a link to the partner search. These are the pages for
      "dansavond Delft" and "Tanzabend Köln".
- [ ] `/evenement/{date}-{locatie}-{onderwerp}` per occurrence: `Event` JSON-LD (name, startDate/endDate,
      `Place` with the address, url, `offers` when the subject carries a price), linking to the venue and the
      calendar.
- [ ] Link them from the list view and the calendar tooltips, and let `?locationId=` redirect to the venue page.
- [ ] Extend the sitemap with the venue pages and the upcoming events. The app already deletes past events, so
      the sitemap stays to upcoming ones.
- [ ] `<lastmod>` is impossible today: entities have only `id` and a version, no timestamps. Either add
      created/updated columns or leave `lastmod` out (it is optional).
- [ ] Revisit the phase 1 choice: is `/list` the one that should be noindexed? If its text-rich table is the
      better search result, invert it (canonical `/` → `/list`).

## Phase 3 — the seam with shallwedance.org

- [ ] Replace the iframe on the main site's `/where` with a real page: the next few events (needs a small
      `GET /api/v1/event?limit=n` here — `CalendarResource` and `OpenApiConfig` already exist), each linking to
      its page here, plus "bekijk de volledige agenda".
- [ ] Then remove `/where` from `SeoRules.NOINDEX_PREFIXES` in the main project, give it a title/description and
      add it to the main sitemap.
- [ ] Cross-link both ways with descriptive anchors (here → "zoek een danspartner", there → venue/event pages).
      One canonical home per event: details here, never duplicated there.

## Phase 4 — optional: one domain

- [ ] Only if authority consolidation becomes the bottleneck: a Traefik path route on `shallwedance.org`, e.g.
      `/evenementen` → `dancemoments-reader-svc`, with `BASEURL=https://shallwedance.org/evenementen`, the
      hardcoded absolute paths fixed (`/logo`, `/bulma-checkbox-1.2.1.css`, htmx requests), a 301 from the
      subdomain and updated canonical/sitemap.
- Keep `/ical` stable: it is in people's calendar subscriptions.

## Nits found on the way (not done)

- [ ] `WebMvcConfig` is not annotated `@Configuration`, so it is dead code: its `LocaleChangeInterceptor` and
      `messageSource` are not registered. Either annotate it or delete it (the application class already
      provides both beans).
- [ ] The head loads htmx, idiomorph, Bulma and a FontAwesome kit from three CDNs. For Core Web Vitals,
      self-host or defer what the first paint does not need.
- [ ] No favicon; only `/logo`, a 200px PNG.
- [ ] The weekday header in `month.html` is hardcoded English. Correct while English is the default, but it is
      where i18n would start.
