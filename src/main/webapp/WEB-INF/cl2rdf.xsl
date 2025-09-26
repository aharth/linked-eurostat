<?xml version='1.0' encoding='utf-8'?>

<xsl:stylesheet
   xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
   xmlns:dcterms="http://purl.org/dc/terms/"
   xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
   xmlns:skos="http://www.w3.org/2004/02/skos/core#"
   xmlns:foaf="http://xmlns.com/foaf/0.1/"
   xmlns:prov="http://www.w3.org/ns/prov#"
   xmlns:m="http://www.sdmx.org/resources/sdmxml/schemas/v3_0/message"
   xmlns:s="http://www.sdmx.org/resources/sdmxml/schemas/v3_0/structure"
   xmlns:c="http://www.sdmx.org/resources/sdmxml/schemas/v3_0/common"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   version='1.0'>

  <xsl:output method='xml' encoding='utf-8'/>

  <xsl:template match='m:Structure'>
    <rdf:RDF>
      <rdf:Description rdf:about="">
        <rdfs:comment>No guarantee of correctness! USE AT YOUR OWN RISK!</rdfs:comment>
        <dcterms:publisher>Eurostat (http://epp.eurostat.ec.europa.eu/) via Linked Eurostat (http://estatwrap.ontologycentral.com/)</dcterms:publisher>
        <foaf:topic rdf:resource="#cl"/>
        <prov:wasGeneratedBy rdf:resource="#transformation"/>
      </rdf:Description>

      <!-- PROV: Transformation activity -->
      <prov:Activity rdf:about="#transformation">
        <rdfs:label>SDMX to RDF Codelist Transformation</rdfs:label>
        <prov:used>
          <xsl:attribute name="rdf:resource">https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/structure/codelist/ESTAT/<xsl:value-of select="//s:Codelist/@id"/></xsl:attribute>
        </prov:used>
        <prov:wasAssociatedWith rdf:resource="#estatwrap"/>
        <dcterms:date rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="current-dateTime()"/></dcterms:date>
      </prov:Activity>

      <!-- PROV: Agent (estatwrap service) -->
      <prov:SoftwareAgent rdf:about="#estatwrap">
        <rdfs:label>Linked Eurostat (estatwrap)</rdfs:label>
        <foaf:homepage rdf:resource="http://estatwrap.ontologycentral.com/"/>
        <dcterms:description>Service for converting Eurostat SDMX data to RDF</dcterms:description>
      </prov:SoftwareAgent>

      <!-- Process codelists -->
      <xsl:apply-templates select="m:Structures/s:Codelists"/>

      <!-- Process other elements -->
      <xsl:apply-templates/>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match='m:Header'>
    <rdf:Description rdf:about="#header">
      <xsl:apply-templates/>
    </rdf:Description>
  </xsl:template>

  <xsl:template match='m:ID'>
    <dcterms:identifier><xsl:value-of select="."/></dcterms:identifier>
  </xsl:template>

  <xsl:template match='m:Prepared'>
    <dcterms:date rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="."/></dcterms:date>
  </xsl:template>

  <!-- Template for Codelists -->
  <xsl:template match='s:Codelists'>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match='s:Codelist'>
    <skos:ConceptScheme>
      <xsl:attribute name="rdf:about">#cl-<xsl:value-of select="@id"/></xsl:attribute>

      <!-- Basic identifiers -->
      <dcterms:identifier><xsl:value-of select="@id"/></dcterms:identifier>
      <skos:notation><xsl:value-of select="@urn"/></skos:notation>
      <dcterms:creator><xsl:value-of select="@agencyID"/></dcterms:creator>
      <dcterms:hasVersion><xsl:value-of select="@version"/></dcterms:hasVersion>

      <!-- Process names and descriptions -->
      <xsl:apply-templates select="c:Name"/>
      <xsl:apply-templates select="c:Description"/>

      <!-- PROV: Derived from original SDMX source -->
      <prov:wasDerivedFrom>
        <xsl:attribute name="rdf:resource">https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/structure/codelist/ESTAT/<xsl:value-of select="@id"/></xsl:attribute>
      </prov:wasDerivedFrom>
      <prov:wasGeneratedBy rdf:resource="#transformation"/>

      <!-- Process codes -->
      <xsl:apply-templates select="s:Code"/>
    </skos:ConceptScheme>
  </xsl:template>

  <!-- Template for Names -->
  <xsl:template match='s:Codelist/c:Name'>
    <skos:prefLabel>
      <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute>
      <xsl:value-of select="."/>
    </skos:prefLabel>
  </xsl:template>

  <!-- Template for Descriptions -->
  <xsl:template match='s:Codelist/c:Description'>
    <skos:definition>
      <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute>
      <xsl:value-of select="."/>
    </skos:definition>
  </xsl:template>

  <!-- Template for Codes -->
  <xsl:template match='s:Code'>
    <skos:hasTopConcept>
      <skos:Concept>
        <xsl:attribute name="rdf:about">#code-<xsl:value-of select="@id"/></xsl:attribute>

        <skos:notation><xsl:value-of select="@urn"/></skos:notation>
        <dcterms:identifier><xsl:value-of select="@id"/></dcterms:identifier>
        <skos:inScheme>
          <xsl:attribute name="rdf:resource">#cl-<xsl:value-of select="../@id"/></xsl:attribute>
        </skos:inScheme>

        <!-- Process code names -->
        <xsl:apply-templates select="c:Name" mode="code"/>
        <xsl:apply-templates select="c:Description" mode="code"/>

        <!-- Handle parent relationships -->
        <xsl:if test="@parent">
          <skos:broader>
            <xsl:attribute name="rdf:resource">#code-<xsl:value-of select="@parent"/></xsl:attribute>
          </skos:broader>
        </xsl:if>
      </skos:Concept>
    </skos:hasTopConcept>
  </xsl:template>

  <!-- Template for Code Names -->
  <xsl:template match='s:Code/c:Name' mode="code">
    <skos:prefLabel>
      <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute>
      <xsl:value-of select="."/>
    </skos:prefLabel>
  </xsl:template>

  <!-- Template for Code Descriptions -->
  <xsl:template match='s:Code/c:Description' mode="code">
    <skos:definition>
      <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute>
      <xsl:value-of select="."/>
    </skos:definition>
  </xsl:template>

  <xsl:template match="*"/>
</xsl:stylesheet>