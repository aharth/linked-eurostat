<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
   xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:dcterms="http://purl.org/dc/terms/"
   xmlns:prov="http://www.w3.org/ns/prov#"
   xmlns:dcat="http://www.w3.org/ns/dcat#"
   xmlns:foaf="http://xmlns.com/foaf/0.1/"
   xmlns:rss="http://purl.org/rss/1.0/"
   version="1.0">

  <xsl:output method="xml" encoding="utf-8"/>

  <xsl:template match="/rss">
    <rdf:RDF>
      <!-- Document description -->
      <rdf:Description rdf:about="">
        <rdfs:comment>No guarantee of correctness! USE AT YOUR OWN RISK!</rdfs:comment>
        <dcterms:publisher>Eurostat (http://epp.eurostat.ec.europa.eu/) via Linked Eurostat (http://estatwrap.ontologycentral.com/)</dcterms:publisher>
        <dcterms:title>Linked Eurostat - Recent Dataset Updates</dcterms:title>
        <dcterms:description>RSS/RDF feed of recent Eurostat dataset update events</dcterms:description>
        <prov:wasDerivedFrom rdf:resource="https://ec.europa.eu/eurostat/api/dissemination/catalogue/rss/en/statistics-update.rss"/>
        <prov:wasGeneratedBy rdf:resource="#transformation"/>
      </rdf:Description>

      <!-- PROV: Transformation activity -->
      <prov:Activity rdf:about="#transformation">
        <rdfs:label>Eurostat RSS to RDF Transformation</rdfs:label>
        <prov:used rdf:resource="https://ec.europa.eu/eurostat/api/dissemination/catalogue/rss/en/statistics-update.rss"/>
        <prov:wasAssociatedWith rdf:resource="#estatwrap"/>
        <dcterms:date rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="current-dateTime()"/></dcterms:date>
      </prov:Activity>

      <!-- PROV: Agent (estatwrap service) -->
      <prov:SoftwareAgent rdf:about="#estatwrap">
        <rdfs:label>Linked Eurostat (estatwrap)</rdfs:label>
        <rdfs:seeAlso rdf:resource="http://estatwrap.ontologycentral.com/"/>
        <dcterms:description>Service for converting Eurostat data to RDF</dcterms:description>
      </prov:SoftwareAgent>

      <!-- RSS 1.0 Channel -->
      <rss:channel rdf:about="http://estatwrap.ontologycentral.com/feed.rdf">
        <rss:title>Linked Eurostat Updates</rss:title>
        <rss:link>http://estatwrap.ontologycentral.com/</rss:link>
        <rss:description>RSS feed of recent Eurostat dataset update events</rss:description>
        <dc:language>en</dc:language>
        <dc:creator>Linked Eurostat</dc:creator>
        <dc:publisher>Eurostat</dc:publisher>

        <!-- RSS 1.0 items sequence -->
        <rss:items>
          <rdf:Seq>
            <xsl:for-each select="channel/item">
              <xsl:variable name="datasetId">
                <xsl:choose>
                  <xsl:when test="contains(title, ' - ')">
                    <xsl:value-of select="translate(normalize-space(substring-before(title, ' - ')), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="translate(title, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:variable name="updateDate">
                <xsl:call-template name="format-date">
                  <xsl:with-param name="date" select="pubDate"/>
                </xsl:call-template>
              </xsl:variable>
              <rdf:li>
                <xsl:attribute name="rdf:resource">
                  <xsl:value-of select="concat('#update-', $datasetId, '-', translate($updateDate, ':T-', ''))"/>
                </xsl:attribute>
              </rdf:li>
            </xsl:for-each>
          </rdf:Seq>
        </rss:items>
      </rss:channel>

      <!-- Also keep as DCAT Catalog for semantic web compatibility -->
      <dcat:Catalog rdf:about="http://estatwrap.ontologycentral.com/feed.rdf">
        <dcterms:title>Linked Eurostat Updates</dcterms:title>
        <dcterms:description>RSS feed of recent Eurostat dataset update events</dcterms:description>
        <dcterms:publisher rdf:resource="http://epp.eurostat.ec.europa.eu/"/>
        <dcat:keyword>Eurostat</dcat:keyword>
        <dcat:keyword>statistics</dcat:keyword>
        <dcat:keyword>linked data</dcat:keyword>
        <dcat:keyword>recent updates</dcat:keyword>

        <!-- Count of recent updates -->
        <dcterms:extent><xsl:value-of select="count(channel/item)"/> recent dataset updates</dcterms:extent>
      </dcat:Catalog>

      <!-- Individual update events -->
      <xsl:apply-templates select="channel/item"/>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="item">
    <!-- Create an update event URI based on dataset ID and date -->
    <xsl:variable name="datasetId">
      <xsl:choose>
        <xsl:when test="contains(title, ' - ')">
          <xsl:value-of select="translate(normalize-space(substring-before(title, ' - ')), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="translate(title, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="updateDate">
      <xsl:call-template name="format-date">
        <xsl:with-param name="date" select="pubDate"/>
      </xsl:call-template>
    </xsl:variable>

    <!-- RSS 1.0 Item -->
    <rss:item>
      <xsl:attribute name="rdf:about">
        <xsl:value-of select="concat('#update-', $datasetId, '-', translate($updateDate, ':T-', ''))"/>
      </xsl:attribute>

      <rss:title><xsl:value-of select="description"/></rss:title>
      <rss:link>
        <xsl:value-of select="concat('http://estatwrap.ontologycentral.com/df/', $datasetId)"/>
      </rss:link>
      <rss:description>
        <xsl:choose>
          <xsl:when test="category = 'UPDATED_DATASET_DATA'">
            <xsl:text>Dataset </xsl:text><xsl:value-of select="$datasetId"/><xsl:text> data was updated</xsl:text>
          </xsl:when>
          <xsl:when test="category = 'UPDATED_DATASET_STRUCTURE_DATA'">
            <xsl:text>Dataset </xsl:text><xsl:value-of select="$datasetId"/><xsl:text> structure and data were updated</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>Dataset </xsl:text><xsl:value-of select="$datasetId"/><xsl:text> was updated</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </rss:description>
      <dcterms:identifier><xsl:value-of select="$datasetId"/></dcterms:identifier>
      <dc:date><xsl:value-of select="$updateDate"/></dc:date>
      <dc:creator>Eurostat</dc:creator>
    </rss:item>

    <!-- Also keep as PROV Activity for semantic web compatibility -->
    <prov:Activity>
      <xsl:attribute name="rdf:about">
        <xsl:value-of select="concat('#update-', $datasetId, '-', translate($updateDate, ':T-', ''))"/>
      </xsl:attribute>

      <rdfs:label><xsl:value-of select="description"/> (updated <xsl:value-of select="$updateDate"/>)</rdfs:label>
      <dcterms:date rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="$updateDate"/></dcterms:date>

      <!-- Update category -->
      <xsl:if test="category">
        <dcat:keyword><xsl:value-of select="category"/></dcat:keyword>
        <xsl:choose>
          <xsl:when test="category = 'UPDATED_DATASET_DATA'">
            <rdfs:comment>Dataset data was updated</rdfs:comment>
          </xsl:when>
          <xsl:when test="category = 'UPDATED_DATASET_STRUCTURE_DATA'">
            <rdfs:comment>Dataset structure and data were updated</rdfs:comment>
          </xsl:when>
          <xsl:otherwise>
            <rdfs:comment>Dataset was updated</rdfs:comment>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>

      <!-- Reference to the dataset that was updated -->
      <rdfs:seeAlso>
        <xsl:attribute name="rdf:resource">
          <xsl:value-of select="concat('./df/', $datasetId)"/>
        </xsl:attribute>
      </rdfs:seeAlso>
      <dcterms:identifier><xsl:value-of select="$datasetId"/></dcterms:identifier>

      <!-- Links -->
      <dcterms:publisher rdf:resource="http://epp.eurostat.ec.europa.eu/"/>
    </prov:Activity>
  </xsl:template>

  <!-- Template to extract dataset URI for Linked Eurostat -->
  <xsl:template name="extract-dataset-uri">
    <xsl:param name="title"/>
    <xsl:param name="link"/>

    <xsl:variable name="datasetId">
      <xsl:choose>
        <xsl:when test="contains($title, ' - ')">
          <xsl:value-of select="translate(normalize-space(substring-before($title, ' - ')), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
        </xsl:when>
        <xsl:when test="contains($link, '/page/')">
          <xsl:value-of select="translate(substring-after($link, '/page/'), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="translate($title, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:value-of select="concat('http://estatwrap.ontologycentral.com/id/', $datasetId)"/>
  </xsl:template>

  <!-- Template to format RSS date to ISO datetime -->
  <xsl:template name="format-date">
    <xsl:param name="date"/>
    <!-- Input format: "2025-09-27 11:00:00.0" -->
    <!-- Output format: "2025-09-27T11:00:00" -->
    <xsl:choose>
      <xsl:when test="contains($date, ' ')">
        <xsl:variable name="datePart" select="substring-before($date, ' ')"/>
        <xsl:variable name="timePart" select="substring-before(substring-after($date, ' '), '.')"/>
        <xsl:value-of select="concat($datePart, 'T', $timePart)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$date"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>