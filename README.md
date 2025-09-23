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

TSV dic/en/sex.dic now at 
https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/structure/codelist/ESTAT/SEX/?compress=true&format=TSV&formatVersion=2.0

TSV data/tag00038.tsv.gz now at
https://ec.europa.eu/eurostat/api/dissemination/sdmx/2.1/data/tag00038/?format=TSV&compressed=true


