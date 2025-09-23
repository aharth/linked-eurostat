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
   xmlns:void="http://rdfs.org/ns/void#"
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
      </rdf:Description>

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
    <dcterms:date><xsl:value-of select="."/></dcterms:date>
  </xsl:template>

  <!-- Template for DataConstraints -->
  <xsl:template match='s:DataConstraints'>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match='s:DataConstraint'>
    <void:Dataset>
      <xsl:attribute name="rdf:about">#constraint-<xsl:value-of select="@id"/></xsl:attribute>

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

      <!-- Process constraint attachment -->
      <xsl:apply-templates select="s:ConstraintAttachment"/>

      <!-- Process cube regions -->
      <xsl:apply-templates select="s:CubeRegion"/>

      <!-- Count available values for statistics -->
      <xsl:variable name="totalValues" select="count(s:CubeRegion/s:KeyValue/s:Value)"/>
      <xsl:if test="$totalValues > 0">
        <void:distinctSubjects rdf:datatype="http://www.w3.org/2001/XMLSchema#integer"><xsl:value-of select="$totalValues"/></void:distinctSubjects>
      </xsl:if>
    </void:Dataset>
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
      <dcterms:source>
        <xsl:attribute name="rdf:resource"><xsl:value-of select="s:Dataflow"/></xsl:attribute>
      </dcterms:source>
    </xsl:if>
    <xsl:if test="s:DataStructure">
      <qb:structure>
        <xsl:attribute name="rdf:resource"><xsl:value-of select="s:DataStructure"/></xsl:attribute>
      </qb:structure>
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
        <skos:notation><xsl:value-of select="@id"/></skos:notation>

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