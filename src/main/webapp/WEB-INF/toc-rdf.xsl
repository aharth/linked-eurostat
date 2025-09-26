<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
   xmlns="http://www.w3.org/1999/xhtml"
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
   xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
   xmlns:cop="http://ontologycentral.com/copernicus/ns#"
   xmlns:foaf="http://xmlns.com/foaf/0.1/"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:dcterms="http://purl.org/dc/terms/"
   xmlns:prov="http://www.w3.org/ns/prov#"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:nt="urn:eu.europa.ec.eurostat.navtree"
   version="1.0">

  <xsl:output method="xml"
	      encoding="utf-8"/>

  <!-- Template to convert DD.MM.YYYY to YYYY-MM-DD -->
  <xsl:template name="convert-date">
    <xsl:param name="date"/>
    <xsl:choose>
      <xsl:when test="contains($date, '.')">
        <xsl:variable name="day" select="substring-before($date, '.')"/>
        <xsl:variable name="rest" select="substring-after($date, '.')"/>
        <xsl:variable name="month" select="substring-before($rest, '.')"/>
        <xsl:variable name="year" select="substring-after($rest, '.')"/>
        <xsl:value-of select="concat($year, '-', format-number($month, '00'), '-', format-number($day, '00'))"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$date"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template></xsl:parameter>
</invoke>

  <xsl:template match="nt:tree">
    <rdf:RDF>
      <rdf:Description rdf:about="">
        <rdfs:comment>No guarantee of correctness! USE AT YOUR OWN RISK!</rdfs:comment>
        <dcterms:publisher>Eurostat (http://epp.eurostat.ec.europa.eu/) via Linked Eurostat (http://estatwrap.ontologycentral.com/)</dcterms:publisher>
        <dcterms:title>Eurostat Table of Contents</dcterms:title>
        <prov:wasGeneratedBy rdf:resource="#transformation"/>
      </rdf:Description>

      <!-- PROV: Transformation activity -->
      <prov:Activity rdf:about="#transformation">
        <rdfs:label>Eurostat TOC XML to RDF Transformation</rdfs:label>
        <prov:used rdf:resource="https://ec.europa.eu/eurostat/api/dissemination/catalogue/toc/xml"/>
        <prov:wasAssociatedWith rdf:resource="#estatwrap"/>
        <dcterms:date><xsl:value-of select="current-dateTime()"/></dcterms:date>
      </prov:Activity>

      <!-- PROV: Agent (estatwrap service) -->
      <prov:SoftwareAgent rdf:about="#estatwrap">
        <rdfs:label>Linked Eurostat (estatwrap)</rdfs:label>
        <foaf:homepage rdf:resource="http://estatwrap.ontologycentral.com/"/>
        <dcterms:description>Service for converting Eurostat data to RDF</dcterms:description>
      </prov:SoftwareAgent>
      <xsl:apply-templates/>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="nt:branch">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="nt:children">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="nt:leaf">
    <rdf:Description>
      <xsl:attribute name="rdf:about">./id/<xsl:value-of select="nt:code"/></xsl:attribute>

      <!-- Multi-language titles -->
      <xsl:for-each select="nt:title">
        <dcterms:title>
          <xsl:attribute name="xml:lang"><xsl:value-of select="@language"/></xsl:attribute>
          <xsl:value-of select="."/>
        </dcterms:title>
      </xsl:for-each>

      <!-- Legacy dc:title for compatibility -->
      <dc:title><xsl:value-of select="nt:title[@language='en']"/></dc:title>

      <!-- Dataset identifier -->
      <dcterms:identifier><xsl:value-of select="nt:code"/></dcterms:identifier>

      <!-- Update dates -->
      <dcterms:modified>
        <xsl:call-template name="convert-date">
          <xsl:with-param name="date" select="nt:lastUpdate"/>
        </xsl:call-template>
      </dcterms:modified>
      <xsl:if test="nt:lastModified and nt:lastModified != nt:lastUpdate">
        <dcterms:date>
          <xsl:call-template name="convert-date">
            <xsl:with-param name="date" select="nt:lastModified"/>
          </xsl:call-template>
        </dcterms:date>
      </xsl:if>

      <!-- Legacy dc:date for compatibility -->
      <dc:date>
        <xsl:call-template name="convert-date">
          <xsl:with-param name="date" select="nt:lastUpdate"/>
        </xsl:call-template>
      </dc:date>

      <!-- Temporal coverage -->
      <xsl:if test="nt:dataStart">
        <dcterms:coverage>
          <dcterms:PeriodOfTime>
            <dcterms:start><xsl:value-of select="nt:dataStart"/></dcterms:start>
            <xsl:if test="nt:dataEnd">
              <dcterms:end><xsl:value-of select="nt:dataEnd"/></dcterms:end>
            </xsl:if>
          </dcterms:PeriodOfTime>
        </dcterms:coverage>
      </xsl:if>

      <!-- Data volume -->
      <xsl:if test="nt:values">
        <dcterms:extent><xsl:value-of select="nt:values"/> data points</dcterms:extent>
      </xsl:if>

      <!-- Source -->
      <xsl:if test="nt:source[@language='en']">
        <dcterms:source><xsl:value-of select="nt:source[@language='en']"/></dcterms:source>
      </xsl:if>

      <!-- Metadata links -->
      <xsl:for-each select="nt:metadata">
        <dcterms:conformsTo>
          <xsl:attribute name="rdf:resource"><xsl:value-of select="."/></xsl:attribute>
        </dcterms:conformsTo>
      </xsl:for-each>

      <!-- Download links -->
      <xsl:for-each select="nt:downloadLink">
        <foaf:page>
          <xsl:attribute name="rdf:resource"><xsl:value-of select="."/></xsl:attribute>
        </foaf:page>
      </xsl:for-each>

    </rdf:Description>
  </xsl:template>

  <xsl:template match="*"/>
</xsl:stylesheet>
