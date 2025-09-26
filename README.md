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

For detailed URI structure and templates, see [uri-templates.md](uri-templates.md).