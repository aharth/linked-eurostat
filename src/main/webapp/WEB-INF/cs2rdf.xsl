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

  <xsl:output method='xml' encoding='utf-8' indent='yes'/>

  <xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

  <xsl:template match='m:Structure'>
    <rdf:RDF>
      <rdf:Description rdf:about="">
        <rdfs:comment>No guarantee of correctness! USE AT YOUR OWN RISK!</rdfs:comment>
        <dcterms:publisher>Eurostat (http://epp.eurostat.ec.europa.eu/) via Linked Eurostat (http://estatwrap.ontologycentral.com/)</dcterms:publisher>
        <foaf:topic rdf:resource="#cs"/>
        <prov:wasGeneratedBy rdf:resource="#transformation"/>
      </rdf:Description>

      <!-- PROV: Transformation activity -->
      <prov:Activity rdf:about="#transformation">
        <rdfs:label>SDMX to RDF Concept Scheme Transformation</rdfs:label>
        <prov:used>
          <xsl:attribute name="rdf:resource">https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/structure/conceptscheme/ESTAT/<xsl:value-of select="//s:ConceptScheme/@id"/></xsl:attribute>
        </prov:used>
        <prov:wasAssociatedWith rdf:resource="#estatwrap"/>
        <dcterms:date><xsl:value-of select="current-dateTime()"/></dcterms:date>
      </prov:Activity>

      <!-- PROV: Agent (estatwrap service) -->
      <prov:SoftwareAgent rdf:about="#estatwrap">
        <rdfs:label>Linked Eurostat (estatwrap)</rdfs:label>
        <foaf:homepage rdf:resource="http://estatwrap.ontologycentral.com/"/>
        <dcterms:description>Service for converting Eurostat SDMX data to RDF</dcterms:description>
      </prov:SoftwareAgent>

      <xsl:apply-templates select="m:Structures/s:ConceptSchemes"/>
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
    <dcterms:date><xsl:value-of select="."/></dcterms:date>
  </xsl:template>

  <xsl:template match='s:ConceptSchemes'>
    <xsl:apply-templates select="s:ConceptScheme"/>
  </xsl:template>

  <xsl:template match='s:ConceptScheme'>
    <skos:ConceptScheme>
      <xsl:attribute name="rdf:about">#cs-<xsl:value-of select="@id"/></xsl:attribute>
      <skos:notation><xsl:value-of select="@id"/></skos:notation>
      <dcterms:identifier><xsl:value-of select="@id"/></dcterms:identifier>
      <xsl:if test="@version">
        <dcterms:hasVersion><xsl:value-of select="@version"/></dcterms:hasVersion>
      </xsl:if>
      <xsl:if test="@agencyID">
        <dcterms:creator><xsl:value-of select="@agencyID"/></dcterms:creator>
      </xsl:if>

      <!-- Apply templates for scheme name -->
      <xsl:apply-templates select="c:Name" mode="scheme-label"/>

      <!-- PROV: Derived from original SDMX source -->
      <prov:wasDerivedFrom>
        <xsl:attribute name="rdf:resource">https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/structure/conceptscheme/ESTAT/<xsl:value-of select="@id"/></xsl:attribute>
      </prov:wasDerivedFrom>
      <prov:wasGeneratedBy rdf:resource="#transformation"/>

      <!-- Create hasTopConcept relations for each concept -->
      <xsl:for-each select="s:Concept">
        <skos:hasTopConcept>
          <xsl:attribute name="rdf:resource">#concept-<xsl:value-of select="@id"/></xsl:attribute>
        </skos:hasTopConcept>
      </xsl:for-each>
    </skos:ConceptScheme>

    <!-- Generate individual concept resources -->
    <xsl:apply-templates select="s:Concept"/>
  </xsl:template>

  <xsl:template match='c:Name' mode='scheme-label'>
    <rdfs:label>
      <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute>
      <xsl:value-of select="."/>
    </rdfs:label>
  </xsl:template>

  <xsl:template match='s:Concept'>
    <skos:Concept>
      <xsl:attribute name="rdf:about">#concept-<xsl:value-of select="@id"/></xsl:attribute>
      <skos:notation><xsl:value-of select="@id"/></skos:notation>
      <dcterms:identifier><xsl:value-of select="@id"/></dcterms:identifier>

      <!-- Link back to the concept scheme -->
      <skos:inScheme>
        <xsl:attribute name="rdf:resource">#cs-<xsl:value-of select="../@id"/></xsl:attribute>
      </skos:inScheme>

      <!-- Generate labels in multiple languages -->
      <xsl:apply-templates select="c:Name" mode="concept-label"/>

      <!-- Add information about core representation if present -->
      <xsl:if test="s:CoreRepresentation/s:Enumeration">
        <skos:note>
          <xsl:text>Core representation: </xsl:text>
          <xsl:value-of select="s:CoreRepresentation/s:Enumeration"/>
        </skos:note>
      </xsl:if>

      <xsl:if test="s:CoreRepresentation/s:TextFormat">
        <skos:note>
          <xsl:text>Text format: </xsl:text>
          <xsl:value-of select="s:CoreRepresentation/s:TextFormat/@textType"/>
        </skos:note>
      </xsl:if>

    </skos:Concept>
  </xsl:template>

  <xsl:template match='c:Name' mode='concept-label'>
    <skos:prefLabel>
      <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute>
      <xsl:value-of select="."/>
    </skos:prefLabel>
  </xsl:template>


  <!-- Ignore other elements -->
  <xsl:template match="*"/>
</xsl:stylesheet>
