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

### Endpoint Structure
- `/cl/*` - Code Lists (SKOS Concept Schemes and Concepts)
- `/cs/*` - Concept Schemes
- `/ds/*` - Data Structure Definitions
- `/df/*` - Data Flows
- `/dc/*` - Data Constraints
- `/da/*` - Data Observations

### Fragment Identifier Patterns

#### `/cl` (Code Lists)
- **Schemes:** `#cl-{id}` (e.g., `#cl-CL_GEO`)
- **Codes:** `#code-{value}` (e.g., `#code-EU27`)

#### `/cs` (Concept Schemes)
- **Schemes:** `#cs-{id}` (e.g., `#cs-TIME_PERIOD`)
- **Concepts:** `#concept-{value}` (e.g., `#concept-2020`)

#### `/ds` (Data Structures)
- **Structures:** `#ds-{id}` (e.g., `#ds-TAG00038`)
- **Components:** `#component-{id}` (e.g., `#component-FREQ`)
- **Dimensions:** `#dim-{id}` (e.g., `#dim-GEO`)
- **Attributes:** `#attr-{id}` (e.g., `#attr-OBS_STATUS`)
- **Measures:** `#measure-{id}` (e.g., `#measure-OBS_VALUE`)

#### `/df` (Data Flows)
- **Code Lists:** `#cl-{id}` (e.g., `#cl-CL_GEO`)
- **Data Flows:** `#df-{id}` (e.g., `#df-TAG00038`)

#### `/dc` (Data Constraints)
- **Constraints:** `#constraint-{id}` (e.g., `#constraint-TAG00038`)
- **Dimensions:** `#dim-{id}` (e.g., `#dim-GEO`)

#### `/da` (Data Observations)
- **Output Format:** Turtle (text/turtle) - optimized for relative URI handling
- **Cross-references:** Uses relative paths to other endpoints
  - Code lists: `../cl/{dimension}#code-{value}`
  - Concept schemes: `../cs/{dimension}#concept-{value}`
  - Data structures: `../ds/{id}#dsd`
- **Note:** The `/da` endpoint outputs Turtle format instead of RDF/XML because Turtle natively supports relative URIs in namespace declarations, which resolves compatibility issues with RDF/XML tools that struggle with relative namespace URIs in properties.

### Design Principles
- **Relative paths:** All cross-references use `../` relative paths
- **Case preservation:** Fragment identifiers preserve original case
- **Semantic prefixing:** Clear prefixes identify resource types
- **Consistency:** Uniform patterns across all endpoints