<?xml version='1.0' encoding='utf-8'?>

<xsl:stylesheet
   xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
   xmlns:dcterms="http://purl.org/dc/terms/"
   xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
   xmlns:qb="http://purl.org/linked-data/cube#"
   xmlns:skos="http://www.w3.org/2004/02/skos/core#"
   xmlns:foaf="http://xmlns.com/foaf/0.1/"
   xmlns:dcat="http://www.w3.org/ns/dcat#"
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
        <foaf:topic rdf:resource="#dc"/>
        <prov:wasGeneratedBy rdf:resource="#transformation"/>
      </rdf:Description>

      <!-- PROV: Transformation activity -->
      <prov:Activity rdf:about="#transformation">
        <rdfs:label>SDMX to RDF Data Constraint Transformation</rdfs:label>
        <prov:used>
          <xsl:attribute name="rdf:resource">https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/structure/dataconstraint/ESTAT/<xsl:value-of select="//s:DataConstraint/@id"/></xsl:attribute>
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

      <!-- Process data constraints -->
      <xsl:apply-templates select="m:Structures/s:DataConstraints"/>

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

  <!-- Template for DataConstraints -->
  <xsl:template match='s:DataConstraints'>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match='s:DataConstraint'>
    <qb:DataConstraint>
      <xsl:attribute name="rdf:about">#constraint</xsl:attribute>

      <!-- Basic identifiers -->
      <dcterms:identifier><xsl:value-of select="@id"/></dcterms:identifier>
      <skos:notation><xsl:value-of select="@urn"/></skos:notation>
      <dcterms:creator><xsl:value-of select="@agencyID"/></dcterms:creator>
      <dcterms:hasVersion><xsl:value-of select="@version"/></dcterms:hasVersion>
      <xsl:if test="@role">
        <dcterms:type><xsl:value-of select="@role"/></dcterms:type>
      </xsl:if>

      <!-- Process names -->
      <xsl:apply-templates select="c:Name"/>

      <!-- PROV: Derived from original SDMX source -->
      <prov:wasDerivedFrom>
        <xsl:attribute name="rdf:resource">https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/structure/dataconstraint/ESTAT/<xsl:value-of select="@id"/></xsl:attribute>
      </prov:wasDerivedFrom>
      <prov:wasGeneratedBy rdf:resource="#transformation"/>

      <!-- Process constraint attachment -->
      <xsl:apply-templates select="s:ConstraintAttachment"/>

      <!-- Process cube regions -->
      <xsl:apply-templates select="s:CubeRegion"/>

      <!-- Count available values for statistics -->
      <xsl:variable name="totalValues" select="count(s:CubeRegion/s:KeyValue/s:Value)"/>
      <xsl:if test="$totalValues > 0">
        <dcterms:extent><xsl:value-of select="$totalValues"/> constraint values</dcterms:extent>
      </xsl:if>
    </qb:DataConstraint>
  </xsl:template>

  <!-- Template for Names -->
  <xsl:template match='s:DataConstraint/c:Name'>
    <rdfs:label>
      <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute>
      <xsl:value-of select="."/>
    </rdfs:label>
  </xsl:template>

  <!-- Template for Constraint Attachment -->
  <xsl:template match='s:ConstraintAttachment'>
    <xsl:if test="s:Dataflow">
      <!-- Extract dataset ID from URN like urn:sdmx:org.sdmx.infomodel.dataflow.Dataflow=ESTAT:TAG00038(1.0) -->
      <xsl:variable name="dataflowId">
        <xsl:choose>
          <xsl:when test="contains(s:Dataflow, '=ESTAT:')">
            <xsl:variable name="after-estat" select="substring-after(s:Dataflow, '=ESTAT:')"/>
            <xsl:choose>
              <xsl:when test="contains($after-estat, '(')">
                <xsl:value-of select="translate(substring-before($after-estat, '('), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="translate($after-estat, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="s:Dataflow"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <dcterms:source rdf:resource="../df/{$dataflowId}#df"/>
    </xsl:if>
    <xsl:if test="s:DataStructure">
      <!-- Extract dataset ID from URN like urn:sdmx:org.sdmx.infomodel.datastructure.DataStructure=ESTAT:TAG00038(27.0) -->
      <xsl:variable name="dsId">
        <xsl:choose>
          <xsl:when test="contains(s:DataStructure, '=ESTAT:')">
            <xsl:variable name="after-estat" select="substring-after(s:DataStructure, '=ESTAT:')"/>
            <xsl:choose>
              <xsl:when test="contains($after-estat, '(')">
                <xsl:value-of select="translate(substring-before($after-estat, '('), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="translate($after-estat, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="s:DataStructure"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <qb:structure rdf:resource="../ds/{$dsId}#ds"/>
    </xsl:if>
  </xsl:template>

  <!-- Template for Cube Regions -->
  <xsl:template match='s:CubeRegion'>
    <dcat:theme>
      <skos:ConceptScheme>
        <xsl:attribute name="rdf:about">#available-values</xsl:attribute>
        <rdfs:label>Available dimension values</rdfs:label>
        <xsl:apply-templates select="s:KeyValue"/>
      </skos:ConceptScheme>
    </dcat:theme>
  </xsl:template>

  <!-- Template for Key Values -->
  <xsl:template match='s:KeyValue'>
    <skos:hasTopConcept>
      <skos:Concept>
        <xsl:attribute name="rdf:about">#dim-<xsl:value-of select="@id"/></xsl:attribute>
        <skos:prefLabel><xsl:value-of select="@id"/></skos:prefLabel>
        <dcterms:identifier><xsl:value-of select="@id"/></dcterms:identifier>

        <!-- Count values for this dimension -->
        <xsl:variable name="valueCount" select="count(s:Value)"/>
        <skos:note>Available values: <xsl:value-of select="$valueCount"/></skos:note>

        <!-- List some example values -->
        <xsl:for-each select="s:Value[position() &lt;= 5]">
          <skos:example><xsl:value-of select="."/></skos:example>
        </xsl:for-each>

        <!-- Add note if there are more values -->
        <xsl:if test="$valueCount > 5">
          <skos:note>... and <xsl:value-of select="$valueCount - 5"/> more values</skos:note>
        </xsl:if>

        <!-- Special handling for time periods -->
        <xsl:if test="@id = 'TIME_PERIOD'">
          <dcat:temporalCoverage>
            <dcterms:PeriodOfTime>
              <dcat:startDate rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear"><xsl:value-of select="s:Value[1]"/></dcat:startDate>
              <dcat:endDate rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear"><xsl:value-of select="s:Value[last()]"/></dcat:endDate>
            </dcterms:PeriodOfTime>
          </dcat:temporalCoverage>
        </xsl:if>
      </skos:Concept>
    </skos:hasTopConcept>
  </xsl:template>

  <xsl:template match="*"/>
</xsl:stylesheet>