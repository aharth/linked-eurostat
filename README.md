# Linked Eurostat

The European Commission provides detailed statistics on the EU and candidate countries as part of Eurostat (http://ec.europa.eu/eurostat).

The linked-eurostat wrapper provides the raw Eurostat data as Linked Data (http://en.wikipedia.org/wiki/Linked_Data).
The wrapper is online at http://estatwrap.ontologycentral.com/.

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