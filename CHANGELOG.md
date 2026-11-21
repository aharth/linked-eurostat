# Changelog

## [2026-11-21] Valid timestamps, truncation stated, relative self-links, plain-text errors

- **Timestamps:** `dcterms:date` was `2026-11-21T18:44:27+0100`, which is not an
  `xsd:dateTime` (Jena warned on every `/da/` document). `Main.ISO8601` now
  writes `+01:00` (`XXX`, was `Z`).
- **Truncation is stated in the provenance record.** When `Da.MAX_ROWS` or
  `MAX_COLS` cuts a dataset, `<#transformation>` gets an `rdfs:comment` saying
  what is missing and which filter gets it. Before, a cut showed only in the
  log. The column cap now keeps the **newest** 80 periods instead of the oldest
  80: periods ascend left to right, so long series used to lose their recent
  data. Status flags follow the kept columns.
- **No absolute self-links in RDF.** `/da/` and the stylesheets for `/cl/`,
  `/cs/`, `/ds/`, `/df/`, `/dc/`, `/toc.rdf`, `/codelists.rdf` and `/feed.rdf`
  pointed at `http://estatwrap.ontologycentral.com/…`, and `/da/` at the static
  `table_of_contents.rdf`. All are now relative (`../toc.rdf`, `../`, `./`, `""`),
  so they follow whatever host serves the document. Exceptions: the
  feed's `rss:link` stays absolute (`https`) for feed readers, and so do the old
  appspot redirects and the SPARQL `BASE` on HTML pages. The publisher text
  names `https://ec.europa.eu/eurostat` instead of the old portal URL and no
  longer names this host.
- **Errors are one `text/plain` line**, e.g. `400 Bad Request: value of geo does
  not match …`, instead of Tomcat's HTML page. They are sent with
  `Cache-Control: no-store`; several servlets set a day's `max-age` before
  fetching, so errors used to be cacheable that long. CORS headers stay. The
  rate limiter's 429 goes the same way and says the limit.
- Tests: `DaTurtleTest` (valid dates, the host, the cut, the newest periods,
  status flags not shifted), `ServletUtilTest`.

## [2026-11-21] `/status` and `/status.json`: what the wrapper has seen of Eurostat

Suggested by linked-regionalstatistik, same shape. There, data calls timed
out for hours while the catalogue answered in under a second, and nothing
showed it.

- `UpstreamHealth` records every upstream call per kind, taken from the URL:
  `data` (SDMX data), `structure` (SDMX structure), `catalogue` (TOC, RSS,
  codelist inventory) and `self` (SPARQL fetching its `FROM` graphs). Each kind
  has its state (`ok`, `failing`, `unknown`), last success, last failure, the
  error and failed URL, and failures in a row.
- Recorded in one place, `HttpClientUtil.respond(conn)`, which replaces
  `getResponseCode()` at all nine servlets' call sites and inside
  `checkResponseCode`. A failure is an exception, a 5xx or a 429. A 400 or 404
  means Eurostat answered. A failure while streaming the body is not recorded,
  since it cannot be told apart from the client hanging up.
- `/status` never calls Eurostat, so it is safe to poll, and is not rate-limited.
  200 when data is servable, 503 when the last `data` call failed. JSON
  (`.json` or no preference) or `text/plain` if ranked higher, else 406;
  `Cache-Control: no-store`.
- It reports the table of contents too: number of datasets, when it was loaded,
  or why it failed. Before, a failed TOC fetch at startup left an empty map until
  the next restart, visible only in the log. That fetch now also goes through
  `HttpClientUtil`, so it has timeouts and the User-Agent.
- Tests, offline: `UpstreamHealthTest`, `StatusServletTest`.

## [2026-11-21] Dimension filters on `/da/{id}`, passed through to Eurostat

Asked for by linked-transit: from a region or city code to its figures without
crawling. `/da/{id}?geo=DE254` (NUTS), `/da/urb_cpop1?cities=DE014C` (Urban
Audit; Nürnberg is `DE014C`, not `DE003C`, which is München) and any other
dimension of the TSV header become Eurostat's `c[dim]=…`. A comma lists
alternatives; `TIME_PERIOD` also takes `ge:`/`gt:`/`le:`/`lt:` and `+` for a
range. Eurostat does the filtering; the wrapper checks the syntax (`DataFilter`)
and answers 400 on a malformed name or value.

- Not offered, by decision: prefix matching (`DE2*`) and filters on the value.
  Eurostat rejects both (HTTP 400, `sw:` and `c[OBS_VALUE]` tested), and doing
  them in the wrapper would drop upstream rows. A client gets a prefix's codes
  from `/dc/{id}` and sends them as a list: two requests, not one per region.
- No LAU lookup: Eurostat's dissemination API has no dataset keyed by LAU code.
- No links to linked-gisco: shared ids stay strings (checklist). Each code
  already carries its string as `dcterms:identifier` in `/cl/{dim}`.
- The dataset carries a `hydra:search` template, `{id}{?dim1,…,TIME_PERIOD}`,
  one optional `hydra:IriTemplateMapping` per dimension with `hydra:property`,
  `sh:pattern` and the first observation's value as `skos:example`.
- Observations name `<{id}#ds>` instead of `<#ds>`, so a filtered view's
  observations still belong to the one dataset `/da/{id}#ds`.
- `prov:used` is the actual upstream URL, filters included.
- An upstream error passes on Eurostat's `faultstring` (e.g. "values for
  dimension are not allowed: GEO=XX999") instead of only the reason phrase.
- Tests, offline: `DataFilterTest` (syntax, encoding, rejected prefixes, value
  filters and injection), `DaTurtleTest` (a recorded filtered answer parsed
  against a filtered base: observations in `/da/{id}#ds`, every template
  example matches its own pattern, no host), `DaServletTest` (fault text).

## [2026-10-03] Bypass keys from settings.xml; Jena 6.2.0; .gitignore repaired

- The `api-keys` context-param in `web.xml` is now `${api.keys}`, filtered in
  at package time (`filteringDeploymentDescriptors`) from `~/.m2/settings.xml`;
  `pom.xml` declares it empty. No key is in the repository, as across the
  family since the shared token was rotated on 2026-08-26.
- Apache Jena 6.1.0 → 6.2.0 (`jena-arq`), the family's current release.
- `.gitignore`: the family's standard rules had been appended without a
  newline, merging the old `~` line into `~env.sh`, so `env.sh` was NOT
  ignored. Repaired, and the generated `table_of_contents.{html,rdf}` (out
  of git since 4cec05a) and a local `apache-jena-*/` distribution are now
  ignored too.
- `CLAUDE.md` added.

## [2026-10-02] CORS exposes Link, ETag, Last-Modified, Location

`CorsFilter` now sends `Access-Control-Expose-Headers: Link, ETag,
Last-Modified, Location`. Without it a browser script fetching cross-origin
could read none of them: CORS hides every header outside a short safelist.

## [2026-08-24] Loopback exempt from the rate limiter

A smoke suite is longer than one 50-request window, so a local run tripped the
limiter halfway and reported throttling as breakage. `isLoopbackRequest` now
exempts requests that genuinely originate on this machine.

Deliberately NOT by adding 127.0.0.1 to the IP exemption set: that set is tested
against the client IP, which prefers `X-Forwarded-For` — a header anybody may
send — so it would have let any request on the internet opt out of rate limiting
with one line of its own headers. The test is the connection's own peer address
being loopback AND no forwarding header being present at all. Behind the reverse
proxy every request carries one, so public traffic can never be exempted.

All notable changes to the Eurostat Linked Data Wrapper project will be documented in this file.

## [2026-07-23] Fix a literal-string User-Agent; drop the GitHub URI

- **`FeedServlet` and `CodelistsServlet` sent the literal string
  `"com.ontologycentral.estatwrap.BuildInfo.getUserAgent()"` as their
  User-Agent** — the call was quoted, so Eurostat's access log recorded a Java
  expression instead of an identifiable client. Both now call the method.
  `HttpClientUtil` was always correct.
- **`project.user.agent` and the `BuildInfo` fallback now advertise
  `https://ontologycentral.com/bot#s`** instead of the GitHub repository, so
  the URI resolves to a page with a contact address (`bot@ontologycentral.com`).

## [2026-06-30]
- Fix stale integration tests to use current SDMX 3.0 / catalogue endpoints (`CreateToCTest`, `ToCTest`, `TestEncoding`)

## [2026-06-15] CORS

- Add a permissive `CorsFilter` (allows `Accept, Content-Type, Authorization, DPoP`) so a Solid SPA can fetch resources cross-origin; an `OPTIONS` preflight is answered `204`.

## [2025-10-22]
- Refactor HTTP client code to eliminate duplication between CLI and webapp

## [2025-09-27]
- Add inventory of codelists (available at /codelists.html and /codelists.rdf)
- Add RSS/RDF feed of recent dataset updates (available at /feed.rdf)

## [2025-09-26]
- Update URI patterns: simplify fragment IDs, add rdfs:seeAlso links, and fix README.md inconsistencies
- Add SPARQL query interface endpoint (/sparql) with HTML form and Jena ARQ backend

## [2025-09-24]
- Fully support SDMX 3.0 XML API, removed legacy `/data/{id}` servlet

## [2025-09-23]
- Move towards the SDMX 3.0 XML API

## [2025-09-21]
- Major refactoring, current version of JDK and libraries

## [2018-10-30]
- Eurostat moved to https

## [2018-01-20]
- Enable caching on HTTP requests to Eurostat

## [2017-08-03]
- Removed external link to geo codelist

## [2017-06-22]
- Unicode fix, more links to dictionary files

## [2017-01-27]
- Minor fixes, project mavenised

## [2017-01-02]
- Reduced truncation

## [2016-07-14]
- Moved to updated SPARQL processing endpoint

## [2013-09-21]
- Adapted modeling closer towards [RDF Data Cube specification](http://www.w3.org/TR/vocab-data-cube/)

## [2012-09-22]
- Updated TOC, changed links to statistics, links to eurostat.linked-statistics.org (forked from here) dictionary files

## [2011-10-20]
- Remove wrongly specified geo skos concepts

## [2011-09-02]
- Restriction to 8 columns and 8192 rows

## [2011-08-25]
- Extended restriction to 8 columns (which will likely be the time dimension in most cases) and 16384 rows, and added a warning if file is truncated

## [2011-08-14]
- Corrected typo: qs:dataSet instead of qs:dataset

## [2011-07-24]
- Corrected property URIs in DSD to lowercase, added [feed with last change date](feed.rdf)

## [2011-07-21]
- Added sameAs links to [NUTS (from the EnAKTing project)](http://nuts.psi.enakting.org/) in [dic/geo](./dic/geo)

## [2011-05-26]
- Added sameAs links to [NUTS (with geometries)](http://nuts.geovocab.org/) in [dic/geo](./dic/geo)

## [2011-02-12]
- Time dimension now also with dcterms:date; minor bugfixes; added measures/dimensions list

## [2011-02-10]
- Fixed changed Eurostat URI for table of contents

## [2011-01-21]
- Added RDF Data Cube vocab data structure definitions (under /dsd/{id}), added de and fr labels to dictionary files

## [2010-12-16]
- Data adheres to the RDF Data Cube vocab; visualisations work on the new data

## [2010-09-25]
- Added [NACE](dic/nace_r2) mappings to European Union data

## [2010-09-24]
- Added restriction to 16 columns and 1024 rows due to GAE resource limitations

## [2010-09-23]
- Added ToC and [geographical](dic/geo) mappings to European Union data and UN FAO

## [2010-09-16]
- Added dataset description to [CKAN](http://www.ckan.net/package/linked-eurostat)

## [2010-08-24]
- Now dictionary files are correctly converted

## [2010-07-21]
- Updated wrapper to Eurostat's new download scheme
