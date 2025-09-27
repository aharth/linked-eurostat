# Changelog

All notable changes to the Eurostat Linked Data Wrapper project will be documented in this file.

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
