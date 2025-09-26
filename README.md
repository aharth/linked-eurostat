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

### Endpoint Structure
- `/cl/{dim}` - Code Lists (SKOS Concept Schemes and Concepts)
- `/cs/{id}` - Concept Schemes
- `/ds/{id}` - Data Structure Definitions
- `/df/{id}` - Data Flows
- `/dc/{id}` - Data Constraints
- `/da/{id}` - Data Observations

### Fragment Identifier Patterns

#### `/cl` (Code Lists)
- **Schemes:** `#cl-{clid}` (e.g., `#cl-CL_GEO`)
- **Codes:** `#code-{value}` (e.g., `#code-EU27`)

#### `/cs` (Concept Schemes)
- **Schemes:** `#cs` (e.g., `/cs/TAG00038#cs`)
- **Concepts:** `#concept-{value}` (e.g., `#concept-2020`)

#### `/ds` (Data Structures)
- **Structures:** `#ds` (e.g., `/ds/TAG00038#ds`)
- **Components:** `#component-{id}` (e.g., `#component-FREQ`)
- **Dimensions:** `#dim-{id}` (e.g., `#dim-GEO`)
- **Attributes:** `#attr-{id}` (e.g., `#attr-OBS_STATUS`)
- **Measures:** `#measure-{id}` (e.g., `#measure-OBS_VALUE`)

#### `/df` (Data Flows)
- **Data Flows:** `#df` (e.g., `/df/TAG00038#df`)
- **Code Lists:** `#cl-{clid}` (e.g., `#cl-CL_GEO`) (referenced within dataflows)

#### `/dc` (Data Constraints)
- **Constraints:** `#constraint` (e.g., `/dc/TAG00038#constraint`)
- **Dimensions:** `#dim-{id}` (e.g., `#dim-GEO`)

#### `/da` (Data Observations)
- **Output Format:** Turtle (text/turtle) - optimized for relative URI handling
- **Cross-references:** Uses relative paths to other endpoints
  - Code lists: `../cl/{dimension}#code-{value}`
  - Concept schemes: `../cs/{dimension}#concept-{value}`
  - Data structures: `../ds/{id}#ds`
- **Note:** The `/da` endpoint outputs Turtle format instead of RDF/XML because Turtle natively supports relative URIs in namespace declarations, which resolves compatibility issues with RDF/XML tools that struggle with relative namespace URIs in properties.

### URI Usage Patterns

#### Document URIs vs Fragment Identifiers

**Document URIs** (without fragments) refer to entire RDF documents:
- `/cs/{id}` - Complete concept scheme document
- `/ds/{id}` - Complete data structure definition document
- `/df/{id}` - Complete dataflow document
- `/dc/{id}` - Complete data constraint document
- `/da/{id}` - Complete data observations document
- `/cl/{dim}` - Complete codelist document

**Fragment Identifiers** (with `#`) refer to specific resources within documents:
- `/cs/{id}#cs` - The concept scheme resource itself
- `/ds/{id}#ds` - The data structure definition resource
- `/df/{id}#df` - The dataflow resource
- `/dc/{id}#constraint` - The data constraint resource
- `/cl/{dim}#cl-{clid}` - The codelist scheme resource (uses codelist ID)
- `/cl/{dim}#code-{value}` - Individual code resources

#### Cross-Reference Usage

**In `rdfs:seeAlso` links:** Use document URIs to reference entire related documents
```turtle
rdfs:seeAlso <../cs/{id}> ;     # Link to concept scheme document
rdfs:seeAlso <../ds/{id}> ;     # Link to data structure document
rdfs:seeAlso <../df/{id}> ;     # Link to dataflow document
rdfs:seeAlso <../dc/{id}> ;     # Link to data constraint document
```

**In property values:** Use fragment identifiers to reference specific resources
```turtle
qb:structure <../ds/{id}#ds> ; # Reference the specific data structure definition
```

### Design Principles
- **Relative paths:** All cross-references use `../` relative paths
- **Case preservation:** Fragment identifiers preserve original case
- **Semantic prefixing:** Clear prefixes identify resource types
- **Consistency:** Uniform patterns across all endpoints