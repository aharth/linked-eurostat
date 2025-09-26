# Linked Eurostat

The European Commission provides detailed statistics on the EU and candidate countries as part of Eurostat (http://ec.europa.eu/eurostat).
The raw Eurostat data is provided as Linked Data (http://en.wikipedia.org/wiki/Linked_Data) as part of the linked-eurostat project.
The wrapper is online at http://estatwrap.ontologycentral.com/.

A data dump from 2009 can be found at http://ontologycentral.com/2009/01/eurostat/.

## Build

To build the project, do:

```
$ mvn clean package war:war -Dmaven.test.skip=true -Dcheckstyle.skip=true
```

## Command-line tool

```
java -jar target/linked-eurostat-1.0.0-SNAPSHOT-jar-with-dependencies.jar 
```

## Web application

Deploy the following web application to Apache Tomcat (or other servlet container).

```
target/linked-eurostat-1.0.0-SNAPSHOT.war
```

## URI Patterns

The application provides consistent URI patterns across all endpoints:

### Parameter Definitions
- `{id}` - Dataset identifier (e.g., `tag00038`, `tec00114`)
- `{dim}` - Dimension name (e.g., `geo`, `time`, `freq`)
- `{clid}` - Codelist identifier (e.g., `CL_GEO`, `CL_TIME`, `CL_FREQ`)
- `{value}` - Code or concept value (e.g., `EU27`, `AT`, `2020`, `A` for annual frequency)

### URI Structure

#### Endpoints
- `/cl/{dim}` - Code Lists (SKOS Concept Schemes and Concepts)
- `/cs/{id}` - Concept Schemes
- `/ds/{id}` - Data Structure Definitions
- `/df/{id}` - Data Flows
- `/dc/{id}` - Data Constraints
- `/da/{id}` - Data Observations

#### Document URIs vs Fragment Identifiers

**Document URIs** (without fragments) refer to entire RDF documents and are used in `rdfs:seeAlso` links:
```turtle
rdfs:seeAlso <../cs/{id}> ;     # Link to concept scheme document
rdfs:seeAlso <../ds/{id}> ;     # Link to data structure document
rdfs:seeAlso <../df/{id}> ;     # Link to dataflow document
rdfs:seeAlso <../dc/{id}> ;     # Link to data constraint document
```

**Fragment Identifiers** (with `#`) refer to specific resources within documents and are used in property values:

- **Main Resources:**
  - `/cs/{id}#cs` - The concept scheme resource
  - `/ds/{id}#ds` - The data structure definition resource
  - `/df/{id}#df` - The dataflow resource
  - `/dc/{id}#constraint` - The data constraint resource
  - `/cl/{dim}#cl-{clid}` - The codelist scheme resource

- **Sub-Resources:**
  - `/cl/{dim}#code-{value}` - Individual code resources (e.g., `#code-EU27`)
  - `/cs/{id}#concept-{value}` - Individual concepts (e.g., `#concept-2020`)
  - `/ds/{id}#component-{id}` - Data structure components (e.g., `#component-FREQ`)
  - `/ds/{id}#dim-{id}` - Dimension properties (e.g., `#dim-GEO`)
  - `/ds/{id}#attr-{id}` - Attribute properties (e.g., `#attr-OBS_STATUS`)
  - `/ds/{id}#measure-{id}` - Measure properties (e.g., `#measure-OBS_VALUE`)

#### Special Notes

- **Data Observations (`/da/{id}`):** Outputs Turtle format instead of RDF/XML for better relative URI support
- **Cross-references:** Use relative paths like `../cl/{dim}#code-{value}` or `../ds/{id}#ds`

### Design Principles
- **Relative paths:** All cross-references use `../` relative paths
- **Case preservation:** Fragment identifiers preserve original case
- **Semantic prefixing:** Clear prefixes identify resource types
- **Consistency:** Uniform patterns across all endpoints