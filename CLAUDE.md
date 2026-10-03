# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Linked Eurostat is a Java web application that provides Eurostat data as Linked Data/RDF. It wraps European Commission statistical data from the Eurostat portal and converts it to RDF format. The application serves data through various HTTP endpoints and provides both human-readable pages and machine-readable RDF output.

## Build Commands

Build the project:
```bash
mvn clean package war:war -DskipTests
```

Create executable JAR with dependencies:
```bash
mvn clean package -DskipTests
```

Run tests:
```bash
mvn test
```

Run a single test class:
```bash
mvn test -Dtest=TestClassName
```

Compile without running tests:
```bash
mvn compile
```

## Architecture

### Main Components

1. **Command-Line Interface** (`Main.java`): Downloads and converts Eurostat files without truncation. Supports both data files (`.tsv.gz`) and dictionary files (`.dic`).

2. **Web Application**: Java servlets handling different types of requests:
   - `IdentifierServlet` (`/id/*`): Handles identifier resolution
   - `PageServlet` (`/page/*`): Serves human-readable pages
   - `DataServlet` (`/data/*`): Serves RDF data
   - `DictionaryServlet` (`/dic/*`): Serves dictionary data
   - `DsdServlet` (`/dsd/*`): Serves data structure definitions
   - `FeedServlet` (`/feed.rdf`): Serves RSS/RDF feeds
   - `TimelineServlet` (`/vis/timeline`): Visualization endpoint

3. **Conversion Layer** (`com.ontologycentral.estatwrap.convert`): Core conversion logic for transforming Eurostat data to RDF format.

### Key Dependencies

- **Saxon-HE**: XSLT processor for XML transformations
- **JUnit**: Testing framework
- **Commons CLI**: Command-line parsing
- **jakarta.servlet-api 6.0.0**: Jakarta EE web application framework (Tomcat 11+ compatible)
- **javax.cache**: Caching API (JSR-107)

### Data Flow

1. Raw Eurostat data is fetched from `https://ec.europa.eu/eurostat/estat-navtree-portlet-prod/BulkDownloadListing`
2. TSV/GZ files are decompressed and parsed
3. Data is converted to RDF using XML streaming
4. Output is served via web endpoints or saved to files

## Development Notes

- **Java Version**: Java 17 (minimum supported for Jakarta EE and Tomcat 11)
- **Servlet API**: Migrated from javax.servlet to jakarta.servlet for Tomcat 11 compatibility
- Uses Maven Surefire plugin v3.5.4 for testing with `useSystemClassLoader=false`
- Main class: `com.ontologycentral.estatwrap.Main`
- Web app configured in `src/main/webapp/WEB-INF/web.xml` (Jakarta EE 6.0 format)
- Static files served from `src/main/webapp/` including table of contents HTML/RDF
- Supports both CLI mode and web application deployment
- Tests require network connectivity to Eurostat servers, use `-DskipTests` for offline builds