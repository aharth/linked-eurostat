<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
   xmlns="http://www.w3.org/1999/xhtml"
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
   xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
   xmlns:skos="http://www.w3.org/2004/02/skos/core#"
   xmlns:dcat="http://www.w3.org/ns/dcat#"
   xmlns:foaf="http://xmlns.com/foaf/0.1/"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:dcterms="http://purl.org/dc/terms/"
   xmlns:prov="http://www.w3.org/ns/prov#"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   version="1.0">

  <xsl:output method="xml"
	      encoding="utf-8"/>

  <xsl:template match="codelists">
    <rdf:RDF>
      <!-- Document description -->
      <rdf:Description rdf:about="">
        <rdfs:comment>No guarantee of correctness! USE AT YOUR OWN RISK!</rdfs:comment>
        <dcterms:publisher>Eurostat (http://epp.eurostat.ec.europa.eu/) via Linked Eurostat (http://estatwrap.ontologycentral.com/)</dcterms:publisher>
        <dcterms:title>Eurostat Codelists Catalog</dcterms:title>
        <dcterms:description>Comprehensive catalog of all SKOS concept schemes (codelists) available in the Eurostat system</dcterms:description>
        <rdfs:seeAlso rdf:resource="https://ec.europa.eu/eurostat/api/dissemination/files/inventory?type=codelist"/>
        <prov:wasGeneratedBy rdf:resource="#transformation"/>
        <foaf:topic rdf:resource="#catalog"/>
      </rdf:Description>

      <!-- Main catalog resource -->
      <dcat:Catalog rdf:about="#catalog">
        <dcterms:title>Eurostat Codelists Catalog</dcterms:title>
        <dcterms:description>Catalog of SKOS concept schemes used by Eurostat for statistical data classification</dcterms:description>
        <dcterms:publisher rdf:resource="http://epp.eurostat.ec.europa.eu/"/>
        <dcterms:issued rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="current-dateTime()"/></dcterms:issued>
        <dcat:dataset rdf:resource="#codelists-dataset"/>
        <xsl:apply-templates select="codelist" mode="catalog-entry"/>
      </dcat:Catalog>

      <!-- Dataset about the codelists collection -->
      <dcat:Dataset rdf:about="#codelists-dataset">
        <dcterms:title>Eurostat Codelists Collection</dcterms:title>
        <dcterms:description>Collection of all SKOS concept schemes available in Eurostat</dcterms:description>
        <dcterms:publisher rdf:resource="http://epp.eurostat.ec.europa.eu/"/>
        <dcat:keyword>SKOS</dcat:keyword>
        <dcat:keyword>concept scheme</dcat:keyword>
        <dcat:keyword>codelist</dcat:keyword>
        <dcat:keyword>classification</dcat:keyword>
      </dcat:Dataset>

      <!-- PROV: Transformation activity -->
      <prov:Activity rdf:about="#transformation">
        <rdfs:label>Eurostat Codelists Inventory to RDF Transformation</rdfs:label>
        <prov:used rdf:resource="https://ec.europa.eu/eurostat/api/dissemination/files/inventory?type=codelist"/>
        <prov:wasAssociatedWith rdf:resource="#estatwrap"/>
        <dcterms:date rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="current-dateTime()"/></dcterms:date>
      </prov:Activity>

      <!-- PROV: Agent (estatwrap service) -->
      <prov:SoftwareAgent rdf:about="#estatwrap">
        <rdfs:label>Linked Eurostat (estatwrap)</rdfs:label>
        <foaf:homepage rdf:resource="http://estatwrap.ontologycentral.com/"/>
        <dcterms:description>Service for converting Eurostat data to RDF</dcterms:description>
      </prov:SoftwareAgent>

      <!-- Individual codelist descriptions -->
      <xsl:apply-templates select="codelist"/>
    </rdf:RDF>
  </xsl:template>

  <!-- Catalog entry reference (just dcat:dataset property) -->
  <xsl:template match="codelist" mode="catalog-entry">
    <dcat:dataset>
      <xsl:attribute name="rdf:resource">./cl/<xsl:value-of select="translate(code, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/></xsl:attribute>
    </dcat:dataset>
  </xsl:template>

  <!-- Full codelist description -->
  <xsl:template match="codelist">
    <skos:ConceptScheme>
      <xsl:attribute name="rdf:about">./cl/<xsl:value-of select="translate(code, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/></xsl:attribute>

      <!-- Core metadata -->
      <dcterms:identifier><xsl:value-of select="code"/></dcterms:identifier>
      <dcterms:title><xsl:value-of select="label"/></dcterms:title>
      <rdfs:label><xsl:value-of select="label"/></rdfs:label>

      <!-- Version information -->
      <xsl:if test="version">
        <dcterms:hasVersion><xsl:value-of select="version"/></dcterms:hasVersion>
      </xsl:if>

      <!-- Publisher -->
      <dcterms:publisher rdf:resource="http://epp.eurostat.ec.europa.eu/"/>
      <dcterms:source><xsl:value-of select="source"/></dcterms:source>

      <!-- Classification -->
      <rdf:type rdf:resource="http://www.w3.org/ns/dcat#Dataset"/>
      <dcat:keyword>SKOS</dcat:keyword>
      <dcat:keyword>concept scheme</dcat:keyword>
      <dcat:keyword>codelist</dcat:keyword>

      <!-- Access URLs -->
      <xsl:if test="latest_sdmx_url">
        <dcat:accessURL>
          <xsl:attribute name="rdf:resource"><xsl:value-of select="latest_sdmx_url"/></xsl:attribute>
        </dcat:accessURL>
      </xsl:if>
      <xsl:if test="latest_tsv_url">
        <dcat:downloadURL>
          <xsl:attribute name="rdf:resource"><xsl:value-of select="latest_tsv_url"/></xsl:attribute>
        </dcat:downloadURL>
      </xsl:if>

      <!-- Distributions -->
      <xsl:if test="latest_sdmx_url">
        <dcat:distribution>
          <dcat:Distribution>
            <dcat:accessURL>
              <xsl:attribute name="rdf:resource"><xsl:value-of select="latest_sdmx_url"/></xsl:attribute>
            </dcat:accessURL>
            <dcterms:format>application/xml</dcterms:format>
            <dcat:mediaType>application/xml</dcat:mediaType>
            <dcterms:title>SDMX-ML Structure</dcterms:title>
          </dcat:Distribution>
        </dcat:distribution>
      </xsl:if>

      <xsl:if test="latest_tsv_url">
        <dcat:distribution>
          <dcat:Distribution>
            <dcat:downloadURL>
              <xsl:attribute name="rdf:resource"><xsl:value-of select="latest_tsv_url"/></xsl:attribute>
            </dcat:downloadURL>
            <dcterms:format>text/tab-separated-values</dcterms:format>
            <dcat:mediaType>text/tab-separated-values</dcat:mediaType>
            <dcterms:title>Tab-separated Values</dcterms:title>
          </dcat:Distribution>
        </dcat:distribution>
      </xsl:if>

      <!-- RDF representation link -->
      <foaf:page>
        <xsl:attribute name="rdf:resource">./cl/<xsl:value-of select="translate(code, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/></xsl:attribute>
      </foaf:page>

      <!-- Mapping file if available -->
      <xsl:if test="mapping_file">
        <dcterms:conformsTo>
          <xsl:attribute name="rdf:resource"><xsl:value-of select="mapping_file"/></xsl:attribute>
        </dcterms:conformsTo>
      </xsl:if>

    </skos:ConceptScheme>
  </xsl:template>

</xsl:stylesheet>