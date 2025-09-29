# External RDF/Linked Data Classifications

This document lists external RDF/Linked Data classifications that could be mapped to enhance the semantic value of Eurostat data.

## High Priority - Directly Available as RDF

### NUTS (Nomenclature of Territorial Units for Statistics)
- **URI**: `http://publications.europa.eu/resource/authority/nuts`
- **Format**: SKOS concept schemes with hierarchical levels (0-3)
- **Use Case**: Geographic dimensions in Eurostat data
- **Status**: Available as RDF/SKOS

### NACE (Statistical Classification of Economic Activities)
- **Source**: EU Vocabularies
- **Format**: RDF/SKOS
- **Use Case**: Business/industry statistics
- **Status**: Available as linked open data

### CPA (Statistical Classification of Products by Activity)
- **Source**: EU Vocabularies
- **Format**: RDF/SKOS
- **Use Case**: Production statistics (linked to NACE)
- **Status**: Available as linked open data

## Medium Priority - Available through EU Systems

### Agricultural Products (AGRIPROD)
- **Source**: EU Vocabularies
- **Format**: RDF
- **Use Case**: Agricultural statistics
- **Status**: Available as RDF

### ECOICOP (European Classification of Individual Consumption)
- **Source**: EU Vocabularies
- **Format**: Linked data
- **Use Case**: Household expenditure data
- **Status**: Available as linked data

### European Socio-economic Groups (ESeG)
- **Source**: EU Vocabularies
- **Format**: RDF
- **Use Case**: Demographic/social statistics
- **Status**: Available as RDF

### CN (Combined Nomenclature)
- **Source**: EU Vocabularies
- **Format**: RDF/SKOS
- **Use Case**: Trade statistics
- **Status**: Available as linked open data

### GEONOM (Geonomenclature)
- **Source**: EU Vocabularies
- **Format**: RDF/SKOS
- **Use Case**: Geographic classifications
- **Status**: Available as linked open data

### Waste Classifications (EWC-Stat, LoW)
- **Source**: EU Vocabularies
- **Format**: RDF/SKOS
- **Use Case**: Environmental statistics
- **Status**: Available as linked open data

## Lower Priority - Requires Investigation

### ISCO (International Standard Classification of Occupations)
- **Source**: Potentially available through Euro SDMX registry
- **Use Case**: Employment statistics
- **Status**: Needs investigation

### ISCED (International Standard Classification of Education)
- **Source**: Potentially available through Euro SDMX registry
- **Use Case**: Education statistics
- **Status**: Needs investigation

### COFOG (Classification of Functions of Government)
- **Source**: Potentially available through Euro SDMX registry
- **Use Case**: Public finance data
- **Status**: Needs investigation

## Access Points

### Primary Sources
- **ShowVoc Platform**: `https://showvoc.op.europa.eu/`
- **EU Vocabularies**: `https://op.europa.eu/en/web/eu-vocabularies/eurostat`
- **Euro SDMX Registry**: `https://webgate.ec.europa.eu/sdmxregistry/`

### Technical Details
- **Modeling**: Classifications use XKOS ontology (extension of SKOS)
- **Formats**: RDF/XML, Turtle, N-Triples, JSON-LD
- **Standards**: SKOS concept schemes with hierarchical relationships

## Implementation Notes

1. **Start with NUTS** - Most comprehensive and directly available
2. **Add NACE** - Critical for economic data mapping
3. **Investigate ISCO/ISCED/COFOG** through Euro SDMX registry for additional coverage
4. **Consider versioning** - Classifications may have multiple versions/years available

## FOAF Linking Patterns with External Sources

The system implements bidirectional FOAF linking to connect internal RDF resources with external sources:

### Internal Linking Pattern
- **Document URI** (`/da/id`) ↔ **Fragment URI** (`/da/id#ds`) via `foaf:topic`/`foaf:page`
- Creates bidirectional navigation between documents and their main resources

### External Source Linking

#### 1. Original Data Sources (foaf:homepage)
**Implementation**: `Da.java` conversion
```turtle
<#ds> foaf:homepage <https://ec.europa.eu/eurostat/web/products-datasets/-/{id}> .
```
- Links from internal dataset fragments to original Eurostat dataset pages
- Uses `foaf:homepage` for primary external page reference

#### 2. Download Links (foaf:page)
**Implementation**: `toc-rdf.xsl` transformation
```xml
<foaf:page rdf:resource="{downloadLink}"/>
```
- Links from table of contents entries to external Eurostat download URLs
- Enables direct access to TSV/CSV data files

#### 3. Internal Resource References (foaf:page)
**Implementation**: `codelists-rdf.xsl` transformation
```xml
<foaf:page rdf:resource="./cl/{codelist-id}"/>
```
- Links from catalog entries to corresponding internal codelist resources
- Creates navigation within the linked data space

### FOAF Property Usage

| Property | Purpose | Cardinality | Usage |
|----------|---------|-------------|--------|
| `foaf:topic` | Document → Main Resource | 1:1 | Internal navigation |
| `foaf:page` | Resource → Document | 1:1 | Internal bidirectional |
| `foaf:page` | Resource → External Pages | 1:n | External downloads |
| `foaf:homepage` | Resource → Primary Page | 1:1 | Original source |

### Implementation Files
- **Bidirectional linking**: All `*2rdf.xsl` files (ds, cs, cl, df, dc)
- **External homepage links**: `Da.java` conversion class
- **Download page links**: `toc-rdf.xsl`, `codelists-rdf.xsl`
- **Service homepage**: All transformation files link to `http://estatwrap.ontologycentral.com/`

This creates a comprehensive web of references connecting internal RDF resources to their external Eurostat sources and related pages.