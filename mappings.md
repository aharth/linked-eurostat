# Dictionary Mappings Documentation

This document summarizes the external URI mappings that were implemented in the Dictionary*.java classes before they were removed. These mappings linked Eurostat codes to external ontologies and vocabularies.

## Dictionary Types and Usage

The dictionary system handled different types of codelists with specialized mappings:

- **geo**: Geographical identifiers (NUTS regions) → `DictionaryGeo`
- **cities**: Urban Audit geographical identifiers → `DictionaryCities`
- **metroreg**: Metropolitan region identifiers → `DictionaryCities` (same structure)
- **unit**: Units of measurement → `DictionaryUnits`
- **nace_r2**: NACE economic activity codes → `DictionaryNace`
- **default**: Other codelists → `Dictionary` (base class)

## DictionaryGeo Mappings

### DBpedia Country Mappings
The following country codes were mapped to DBpedia resources:

```
AR → http://dbpedia.org/resource/Argentina
AT → http://dbpedia.org/resource/Austria
BE → http://dbpedia.org/resource/Belgium
BO → http://dbpedia.org/resource/Bolivia
BA → http://dbpedia.org/resource/Bosnia_and_Herzegovina
BR → http://dbpedia.org/resource/Brazil
BG → http://dbpedia.org/resource/Bulgaria
CN → http://dbpedia.org/resource/China
CO → http://dbpedia.org/resource/Colombia
HR → http://dbpedia.org/resource/Croatia
CZ → http://dbpedia.org/resource/Czech_Republic
DK → http://dbpedia.org/resource/Denmark
EE → http://dbpedia.org/resource/Estonia
FI → http://dbpedia.org/resource/Finland
FR → http://dbpedia.org/resource/France
DE → http://dbpedia.org/resource/Germany
GR → http://dbpedia.org/resource/Greece
EL → http://dbpedia.org/resource/Greece
HU → http://dbpedia.org/resource/Hungary
IT → http://dbpedia.org/resource/Italy
JP → http://dbpedia.org/resource/Japan
LV → http://dbpedia.org/resource/Latvia
LI → http://dbpedia.org/resource/Liechtenstein
LT → http://dbpedia.org/resource/Lithuania
LU → http://dbpedia.org/resource/Luxembourg
MT → http://dbpedia.org/resource/Malta
MX → http://dbpedia.org/resource/Mexico
NL → http://dbpedia.org/resource/Netherlands
PE → http://dbpedia.org/resource/Peru
PL → http://dbpedia.org/resource/Poland
PT → http://dbpedia.org/resource/Portugal
RO → http://dbpedia.org/resource/Romania
SK → http://dbpedia.org/resource/Slovakia
SI → http://dbpedia.org/resource/Slovenia
ES → http://dbpedia.org/resource/Spain
SE → http://dbpedia.org/resource/Sweden
CH → http://dbpedia.org/resource/Switzerland
US → http://dbpedia.org/resource/United_states
UY → http://dbpedia.org/resource/Uruguay
```

### EEA Countries Mappings
European countries were mapped to EEA resources:
```
Pattern: {code} → http://rdfdata.eionet.europa.eu/eea/countries/{code}
```
Applied to: AD, AL, AM, AT, AZ, BA, BE, BG, BY, CH, CY, CZ, DE, DK, EE, EFTA4, EIONET, ES, EU15, EU25, EU27, EU6, FI, FR, GB, GE, GR, HR, HU, IE, IS, IT, KZ, LI, LT, LU, LV, MC, MD, ME, MK, MT, NL, NO, PL, PT, RO, RS, RU, SE, SI, SK, SM, TR, UA, XK

### FAO Geopolitical Ontology Mappings
Selected countries mapped to FAO geopolitical ontology:
```
AR → http://aims.fao.org/geopolitical.owl#Argentina
AT → http://aims.fao.org/geopolitical.owl#Austria
BE → http://aims.fao.org/geopolitical.owl#Belgium
BO → http://aims.fao.org/geopolitical.owl#Bolivia
BA → http://aims.fao.org/geopolitical.owl#Bosnia_and_Herzegovina
BR → http://aims.fao.org/geopolitical.owl#Brazil
BG → http://aims.fao.org/geopolitical.owl#Bulgaria
CN → http://aims.fao.org/geopolitical.owl#China
CO → http://aims.fao.org/geopolitical.owl#Colombia
HR → http://aims.fao.org/geopolitical.owl#Croatia
CZ → http://aims.fao.org/geopolitical.owl#Czech_Republic_the
DK → http://aims.fao.org/geopolitical.owl#Denmark
EE → http://aims.fao.org/geopolitical.owl#Estonia
FI → http://aims.fao.org/geopolitical.owl#Finland
FR → http://aims.fao.org/geopolitical.owl#France
DE → http://aims.fao.org/geopolitical.owl#Germany
GR → http://aims.fao.org/geopolitical.owl#Greece
HU → http://aims.fao.org/geopolitical.owl#Hungary
IT → http://aims.fao.org/geopolitical.owl#Italy
JP → http://aims.fao.org/geopolitical.owl#Japan
LV → http://aims.fao.org/geopolitical.owl#Latvia
LI → http://aims.fao.org/geopolitical.owl#Liechtenstein
LT → http://aims.fao.org/geopolitical.owl#Lithuania
LU → http://aims.fao.org/geopolitical.owl#Luxembourg
MT → http://aims.fao.org/geopolitical.owl#Malta
MX → http://aims.fao.org/geopolitical.owl#Mexico
NL → http://aims.fao.org/geopolitical.owl#Netherlands_the
PE → http://aims.fao.org/geopolitical.owl#Peru
PL → http://aims.fao.org/geopolitical.owl#Poland
PT → http://aims.fao.org/geopolitical.owl#Portugal
RO → http://aims.fao.org/geopolitical.owl#Romania
SK → http://aims.fao.org/geopolitical.owl#Slovakia
SI → http://aims.fao.org/geopolitical.owl#Slovenia
ES → http://aims.fao.org/geopolitical.owl#Spain
SE → http://aims.fao.org/geopolitical.owl#Sweden
CH → http://aims.fao.org/geopolitical.owl#Switzerland
US → http://aims.fao.org/geopolitical.owl#United_States_of_America
UY → http://aims.fao.org/geopolitical.owl#Uruguay
```

### NUTS Region Mappings
For NUTS regions, multiple external vocabularies were linked:
```
{nuts_code} → http://rdfdata.eionet.europa.eu/ramon/nuts/{nuts_code}
{nuts_code} → http://nuts.geovocab.org/id/{nuts_code}
```

## DictionaryCities Mappings

Cities were linked to their parent countries:
```
Pattern: {city_code} → http://rdfdata.eionet.europa.eu/eea/countries/{first_2_chars_of_city_code}
```
This created `ramon:partOf` relationships between cities and their countries.

## DictionaryNace Mappings

NACE economic activity codes were mapped to:
```
{nace_code} → http://rdfdata.eionet.europa.eu/eurostatdic/nace_r2/{nace_code}
```

For single-character NACE codes:
```
{nace_code} → http://ec.europa.eu/eurostat/ramon/rdfdata/nace_r2/{nace_code}
```

For 3-character codes where substring(1) is numeric:
```
{nace_code} → http://ec.europa.eu/eurostat/ramon/rdfdata/nace_r2/{substring_from_position_1}
```

## DictionaryUnits Mappings

Units of measurement were mapped to DBpedia when available:
```
1000 → Thousand
MIO → Million
T → Tonne
KG → Kilogram
GR → Gram
LT → Litre
OZ → Ounce
MN → Minute
HOUR → Hour
DAY → Day
MONTH → Month
YEAR → Year
ECU → European_Currency_Unit
EUR → Euro
```

Pattern: `{unit_code} → http://dbpedia.org/resource/{unit_name}` (when mapping exists)

## RDF Properties Used

- `owl:sameAs`: Used for equivalence links to external vocabularies
- `rdfs:label`: Used for human-readable labels
- `skos:notation`: Used for the code notation
- `ramon:partOf`: Used for hierarchical relationships (cities to countries)
- `skos:Concept`: RDF type for most dictionary entries
- `ramon:NUTSRegion`: RDF type specifically for geographical regions

## Implementation Notes

1. All dictionaries extended a base `Dictionary` class that provided basic SKOS structure
2. The `DictionaryPage.convert()` method determined which specialized dictionary class to use based on the ID
3. Metropolitan regions (`metroreg`) used the same structure as cities
4. Not all codes had external mappings - only those explicitly defined in the mapping tables
5. Some mappings were commented out in the original code (e.g., alternative RAMON country mappings)

## Migration Impact

With the removal of these Dictionary classes, the external URI mappings are no longer automatically generated. The functionality has been replaced by the `/cl/` endpoint which uses the SDMX 3.0 API directly from Eurostat, but may not include all the same external vocabulary links that were previously generated by these custom Dictionary classes.