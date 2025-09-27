# URI Templates and Patterns

This document defines the URI structure and patterns used by the Linked Eurostat application.

## Parameter Definitions

- `{id}` - Dataset identifier (e.g., `tag00038`, `tec00114`)
- `{dim}` - Dimension name (e.g., `geo`, `time`, `freq`) - lowercase version of component ID

## Upstream Eurostat API Structure

The application maps to Eurostat's API which has three main branches:

```
https://ec.europa.eu/eurostat/api/dissemination/
├── sdmx/3.0/
│   ├── data/dataflow/ESTAT/{id}/1.0              # Actual statistical data
│   └── structure/
│       ├── dataflow/ESTAT/{id}                   # Dataflow metadata
│       ├── datastructure/ESTAT/{id}              # Data structure definitions
│       ├── codelist/ESTAT/{dim}                  # Code lists
│       ├── conceptscheme/ESTAT/{id}              # Concept schemes
│       └── dataconstraint/ESTAT/{id}             # Data constraints
├── catalogue/
│   └── toc/xml                                   # Table of contents (dataset catalog)
└── files/
    └── inventory?type=codelist                   # Codelist inventory
```

## URI Structure

### Endpoints

- `/cl/{dim}` - Code Lists (SKOS Concept Schemes and Concepts)
  - → `https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/structure/codelist/ESTAT/{dim}`
- `/cs/{id}` - Concept Schemes
  - → `https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/structure/conceptscheme/ESTAT/{id}`
- `/ds/{id}` - Data Structure Definitions
  - → `https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/structure/datastructure/ESTAT/{id}`
- `/df/{id}` - Data Flows
  - → `https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/structure/dataflow/ESTAT/{id}`
- `/dc/{id}` - Data Constraints
  - → `https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/structure/dataconstraint/ESTAT/{id}`
- `/da/{id}` - Data Observations
  - → `https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/data/dataflow/ESTAT/{id}/1.0?format=tsv&compress=false`
- ToC servlet - Table of Contents
  - → `https://ec.europa.eu/eurostat/api/dissemination/catalogue/toc/xml`

### Document URIs vs Fragment Identifiers

**Document URIs** (without fragments) refer to entire RDF documents and are used in `rdfs:seeAlso` links:
```turtle
rdfs:seeAlso <../cs/{id}> ;     # Link to concept scheme document
rdfs:seeAlso <../ds/{id}> ;     # Link to data structure document
rdfs:seeAlso <../df/{id}> ;     # Link to dataflow document
rdfs:seeAlso <../dc/{id}> ;     # Link to data constraint document
```

**Fragment Identifiers** (with `#`) refer to specific resources within documents and are used in property values:

### Additional Parameter Definitions

- `{clid}` - Codelist identifier (e.g., `CL_GEO`, `CL_TIME`, `CL_FREQ`) - component ID with "CL_" prefix
- `{compid}` - Component identifier (e.g., `FREQ`, `GEO`, `OBS_STATUS`, `OBS_VALUE`) - raw SDMX component ID
- `{value}` - Code or concept value (e.g., `EU27`, `AT`, `2020`, `A` for annual frequency)

**Parameter Relationships:** For a geographic dimension:
- Component ID: `GEO` → used in `#component-GEO`, `#dim-GEO`
- Codelist ID: `CL_GEO` → used in `#cl-CL_GEO`, codelist references
- Dimension name: `geo` → used in `/cl/geo` URL path

#### Main Resources
- `/cs/{id}#cs` - The concept scheme resource
- `/ds/{id}#ds` - The data structure definition resource
- `/df/{id}#df` - The dataflow resource
- `/dc/{id}#constraint` - The data constraint resource
- `/cl/{dim}#cl-{clid}` - The codelist scheme resource

#### Sub-Resources
- `/cl/{dim}#code-{value}` - Individual code resources (e.g., `#code-EU27`)
- `/cs/{id}#concept-{value}` - Individual concepts (e.g., `#concept-2020`)
- `/ds/{id}#component-{compid}` - Data structure components (e.g., `#component-FREQ`)
- `/ds/{id}#dim-{compid}` - Dimension properties (e.g., `#dim-GEO`)
- `/ds/{id}#attr-{compid}` - Attribute properties (e.g., `#attr-OBS_STATUS`)
- `/ds/{id}#measure-{compid}` - Measure properties (e.g., `#measure-OBS_VALUE`)

## Special Notes

- **Data Observations (`/da/{id}`):** Outputs Turtle format instead of RDF/XML for better relative URI support
- **Cross-references:** Use relative paths like `../cl/{dim}#code-{value}` or `../ds/{id}#ds`

## Design Principles

- **Relative paths:** All cross-references use `../` relative paths
- **Case preservation:** Fragment identifiers preserve original case
- **Semantic prefixing:** Clear prefixes identify resource types
- **Consistency:** Uniform patterns across all endpoints

## Semantic Model

### Resource Types and Relationships

- **Dataflows (`/df/{id}#df`)**: `dcat:Dataset` - Catalog metadata describing publication configurations
  - Use `dcterms:source` to reference source datasets
  - Do NOT use `qb:structure` (they are metadata, not data cubes)

- **Data Observations (`/da/{id}#dataset`)**: `qb:DataSet` - Actual statistical data cubes
  - Use `qb:structure` to reference data structure definitions
  - Contains the actual observations and measurements

- **Data Structure Definitions (`/ds/{id}#ds`)**: `qb:DataStructureDefinition` - Cube schemas
  - Define dimensions, measures, and attributes
  - Referenced by data cubes via `qb:structure`

- **Code Lists (`/cl/{dim}#cl-{clid}`)**: `skos:ConceptScheme` - Controlled vocabularies
  - Referenced by dimension properties in DSDs

