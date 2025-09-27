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
│   ├── toc/xml                                   # Table of contents (dataset catalog)
│   └── rss/en/statistics-update.rss              # RSS feed of dataset updates
└── files/
    └── inventory?type=codelist                   # Codelist inventory
```

## URI Structure Documents

**Document URIs** (without fragments) refer to entire RDF documents.

### Endpoints

- `/cl/{dim}` - Code Lists (SKOS Concept Schemes and Concepts)
  - → `/sdmx/3.0/structure/codelist/ESTAT/{dim}`
- `/cs/{id}` - Concept Schemes
  - → `/sdmx/3.0/structure/conceptscheme/ESTAT/{id}`
- `/ds/{id}` - Data Structure Definitions
  - → `/sdmx/3.0/structure/datastructure/ESTAT/{id}`
- `/df/{id}` - Data Flows
  - → `/sdmx/3.0/structure/dataflow/ESTAT/{id}`
- `/dc/{id}` - Data Constraints
  - → `/sdmx/3.0/structure/dataconstraint/ESTAT/{id}`
- `/da/{id}` - Data Observations
  - → `/sdmx/3.0/data/dataflow/ESTAT/{id}/1.0?format=tsv&compress=false`
- `/toc.html` / `/toc.rdf` - Table of Contents (Datasets Catalog)
  - → `/catalogue/toc/xml`
- `/codelists.html` / `/codelists.rdf` - Codelists Inventory (SKOS Concept Schemes Catalog)
  - → `/files/inventory?type=codelist`
- `/feed.rdf` - RSS/RDF Feed of Recent Dataset Update Events
  - → `/catalogue/rss/en/statistics-update.rss`

## URI Structure Fragments

**Fragment Identifiers** (with `#`) refer to specific resources within documents and are used in property values.

### Additional Parameter Definitions

- `{clid}` - Codelist identifier (e.g., `CL_GEO`, `CL_TIME`, `CL_FREQ`) - component ID with "CL_" prefix
- `{compid}` - Component identifier (e.g., `FREQ`, `GEO`, `OBS_STATUS`, `OBS_VALUE`) - raw SDMX component ID
- `{value}` - Code or concept value (e.g., `EU27`, `AT`, `2020`, `A` for annual frequency)

**Parameter Relationships:** For a geographic dimension:
- Component ID: `GEO` → used in `#component-GEO`, `#dim-GEO`
- Codelist ID: `CL_GEO` → used in `#cl-CL_GEO`, codelist references
- Dimension name: `geo` → used in `/cl/geo` URL path

### Main Resources
- `/cs/{id}#cs` - The concept scheme resource
- `/ds/{id}#ds` - The data structure definition resource
- `/df/{id}#df` - The dataflow resource
- `/dc/{id}#constraint` - The data constraint resource
- `/cl/{dim}#cl-{clid}` - The codelist scheme resource
- `/da/{id}#ds` - The data set resource
- `/feed.rdf#update-{id}-{timestamp}` - Dataset update event activities

### Sub-Resources
- `/cl/{dim}#code-{value}` - Individual code resources (e.g., `#code-EU27`)
- `/cs/{id}#concept-{value}` - Individual concepts (e.g., `#concept-2020`)
- `/ds/{id}#component-{compid}` - Data structure components (e.g., `#component-FREQ`)
- `/ds/{id}#dim-{compid}` - Dimension properties (e.g., `#dim-GEO`)
- `/ds/{id}#attr-{compid}` - Attribute properties (e.g., `#attr-OBS_STATUS`)
- `/ds/{id}#measure-{compid}` - Measure properties (e.g., `#measure-OBS_VALUE`)

## Semantic Model

The application uses well-known vocabularies to describe the resources, including the W3C Data Cube vocabulary (qb:), SKOS vocabulary (skos:), and DCAT vocabulary (dcat:).

### Document URI Classes

Document URIs (without fragments) are not assigned explicit RDF classes. They are described with properties like `rdfs:comment`, `dcterms:publisher`, and `foaf:topic` pointing to the main fragment resource.

### Fragment URI Classes

#### Main Resources
- `/cs/{id}#cs` → `skos:ConceptScheme`
- `/ds/{id}#ds` → `qb:DataStructureDefinition`
- `/df/{id}#df` → `dcat:Dataset`
- `/dc/{id}#constraint` → `qb:DataConstraint`
- `/cl/{dim}#cl-{clid}` → `skos:ConceptScheme`
- `/da/{id}#ds` → `qb:DataSet`
- `/feed.rdf#update-{id}-{timestamp}` → `prov:Activity`

#### Sub-Resources
- `/cl/{dim}#code-{value}` → `skos:Concept`
- `/cs/{id}#concept-{value}` → `skos:Concept`
- `/ds/{id}#component-{compid}` → `qb:ComponentSpecification`
- `/ds/{id}#dim-{compid}` → `qb:DimensionProperty`
- `/ds/{id}#attr-{compid}` → `qb:AttributeProperty`
- `/ds/{id}#measure-{compid}` → `qb:MeasureProperty`


## Design Principles

- **Relative paths:** All cross-references use `../` relative paths
- **Case preservation:** Fragment identifiers preserve original case
- **Semantic prefixing:** Clear prefixes identify resource types
- **Consistency:** Uniform patterns across all endpoints

