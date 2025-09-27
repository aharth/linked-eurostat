<?xml version='1.0' encoding='utf-8'?>

<xsl:stylesheet
   xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
   xmlns:dcterms="http://purl.org/dc/terms/"
   xmlns:ical="http://www.w3.org/2002/12/cal/ical#"
   xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
   xmlns:gaap="http://edgarwrap.ontologycentral.com/vocab/us-gaap#"
   xmlns:sdmx-measure="http://purl.org/linked-data/sdmx/2009/measure#"
   xmlns:qb="http://purl.org/linked-data/cube#"
   xmlns:skos="http://www.w3.org/2004/02/skos/core#"
   xmlns:foaf="http://xmlns.com/foaf/0.1/"
   xmlns:dcat="http://www.w3.org/ns/dcat#"
   xmlns:adms="http://www.w3.org/ns/adms#"
   xmlns:owl="http://www.w3.org/2002/07/owl#"
   xmlns:prov="http://www.w3.org/ns/prov#"
   xmlns:m="http://www.sdmx.org/resources/sdmxml/schemas/v3_0/message"
   xmlns:s="http://www.sdmx.org/resources/sdmxml/schemas/v3_0/structure"
   xmlns:c="http://www.sdmx.org/resources/sdmxml/schemas/v3_0/common"
   xmlns:sdmx="http://www.sdmx.org/resources/sdmxml/schemas/v3_0/message"
   xmlns:common="http://www.sdmx.org/resources/sdmxml/schemas/v3_0/common"
   xmlns:structure="http://www.sdmx.org/resources/sdmxml/schemas/v3_0/structure"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="http://www.sdmx.org/resources/sdmxml/schemas/v3_0/message SDMXMessage.xsd"
   version='1.0'>

  <xsl:output method='xml' encoding='utf-8'/>

  <xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

  <xsl:template match='m:Structure | sdmx:Structure'>
    <rdf:RDF>
      <rdf:Description rdf:about="">
	<rdfs:comment>No guarantee of correctness! USE AT YOUR OWN RISK!</rdfs:comment>
	<dcterms:publisher>Eurostat (http://epp.eurostat.ec.europa.eu/) via Linked Eurostat (http://estatwrap.ontologycentral.com/)</dcterms:publisher>
	<foaf:topic rdf:resource="#df"/>
	<prov:wasGeneratedBy rdf:resource="#transformation"/>
      </rdf:Description>

      <!-- PROV: Transformation activity -->
      <prov:Activity rdf:about="#transformation">
        <rdfs:label>SDMX to RDF Dataflow Transformation</rdfs:label>
        <prov:used>
          <xsl:attribute name="rdf:resource">https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/structure/dataflow/ESTAT/<xsl:value-of select="//s:Dataflow/@id"/></xsl:attribute>
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

      <!-- Note: qb:DataStructureDefinition removed - belongs in /ds/{id}, not /df/{id} -->

      <!-- Process dataflows -->
      <xsl:apply-templates select="m:Structures/s:Dataflows"/>

      <!-- Process other structures -->
      <xsl:apply-templates/>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match='sdmx:Header | m:Header'>
    <rdf:Description rdf:about="#header">
      <xsl:apply-templates/>
    </rdf:Description>
  </xsl:template>

  <xsl:template match='sdmx:ID | m:ID'>
    <dcterms:identifier><xsl:value-of select="."/></dcterms:identifier>
  </xsl:template>

  <xsl:template match='sdmx:Prepared | m:Prepared'>
    <dcterms:date rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="."/></dcterms:date>
  </xsl:template>

  <xsl:template match='sdmx:CodeLists | s:Codelists'>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match='structure:CodeList | s:Codelist'>
<!--    <xsl:if test="@id != 'CL_GEO'"> -->
      <skos:ConceptScheme>
	<xsl:attribute name="rdf:about">#cl-<xsl:value-of select="@id"/></xsl:attribute>
	<skos:notation><xsl:value-of select="@urn"/></skos:notation>
	<dcterms:identifier><xsl:value-of select="@id"/></dcterms:identifier>
	<xsl:apply-templates/>
      </skos:ConceptScheme>
<!--    </xsl:if> -->
  </xsl:template>

  <xsl:template match='structure:Name|structure:Description|c:Name|c:Description'>
    <rdfs:label>
      <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute>
      <xsl:value-of select="."/>
    </rdfs:label>
  </xsl:template>

  <xsl:template match='structure:Code | s:Code'>
    <skos:hasTopConcept>
      <skos:Concept>
	<!--
	    <xsl:choose>
	    <xsl:when test="../@id='CL_geo'">
	    <xsl:attribute name="rdf:about">/cl/geo<xsl:value-of select="@value"/></xsl:attribute>
	    </xsl:when>
	    <xsl:otherwise>
	-->
	<xsl:attribute name="rdf:about">../cl/<xsl:value-of select="substring(../@id, 4)"/>#code-<xsl:value-of select="@value"/></xsl:attribute>
	<skos:inScheme><xsl:attribute name="rdf:resource">#cl-<xsl:value-of select="../@id"/></xsl:attribute></skos:inScheme>

	<xsl:apply-templates/>
      </skos:Concept>
    </skos:hasTopConcept>
  </xsl:template>

<!--
<http://example.org/EuroStat/CodeList/geo>	a skos:ConceptScheme;
		rdfs:label "Geopolitical entity (declaring)"@en;
		skos:notation "CL_geo";
		skos:hasTopConcept <http://example.org/EuroStat/CodeList/geo#EU27>;
		skos:hasTopConcept <http://example.org/EuroStat/CodeList/geo#EU25>;
		skos:hasTopConcept <http://example.org/EuroStat/CodeList/geo#EU15>;
<http://example.org/EuroStat/CodeList/geo#EU27>	a skos:Concept;
		skos:prefLabel "European Union (27 countries)"@en;
		skos:inScheme <http://example.org/EuroStat/CodeList/geo>;
		skos:notation "EU27"
-->

  <!-- Template for Dataflows -->
  <xsl:template match='s:Dataflows'>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match='s:Dataflow'>
    <dcat:Dataset>
      <xsl:attribute name="rdf:about">#df</xsl:attribute>

      <!-- Basic identifiers -->
      <dcterms:identifier><xsl:value-of select="@id"/></dcterms:identifier>
      <adms:identifier>
        <adms:Identifier>
          <skos:notation><xsl:value-of select="@urn"/></skos:notation>
          <adms:schemeAgency><xsl:value-of select="@agencyID"/></adms:schemeAgency>
        </adms:Identifier>
      </adms:identifier>
      <owl:versionInfo><xsl:value-of select="@version"/></owl:versionInfo>

      <!-- Process names and descriptions -->
      <xsl:apply-templates select="c:Name"/>
      <xsl:apply-templates select="c:Description"/>

      <!-- PROV: Derived from original SDMX source -->
      <prov:wasDerivedFrom>
        <xsl:attribute name="rdf:resource">https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/structure/dataflow/ESTAT/<xsl:value-of select="@id"/></xsl:attribute>
      </prov:wasDerivedFrom>
      <prov:wasGeneratedBy rdf:resource="#transformation"/>

      <!-- Note: qb:structure removed - dataflows are catalog metadata, not data cubes -->

      <!-- Process annotations -->
      <xsl:apply-templates select="c:Annotations"/>
    </dcat:Dataset>
  </xsl:template>

  <!-- Template for Names in dataflows -->
  <xsl:template match='s:Dataflow/c:Name'>
    <dcterms:title>
      <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute>
      <xsl:value-of select="."/>
    </dcterms:title>
  </xsl:template>

  <!-- Template for Descriptions in dataflows -->
  <xsl:template match='s:Dataflow/c:Description'>
    <dcterms:description>
      <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute>
      <xsl:value-of select="."/>
    </dcterms:description>
  </xsl:template>

  <!-- Template for Annotations -->
  <xsl:template match='c:Annotations'>
    <xsl:apply-templates select="c:Annotation"/>
  </xsl:template>

  <xsl:template match='c:Annotation'>
    <xsl:choose>
      <!-- DOI -->
      <xsl:when test="c:AnnotationType = 'DISSEMINATION_DOI_XML'">
        <xsl:value-of select="c:AnnotationTitle" disable-output-escaping="yes"/>
      </xsl:when>

      <!-- Creation date -->
      <xsl:when test="c:AnnotationType = 'CREATED'">
        <dcterms:created rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="c:AnnotationTitle"/></dcterms:created>
      </xsl:when>

      <!-- Data timestamps -->
      <xsl:when test="c:AnnotationType = 'DISSEMINATION_TIMESTAMP_DATA'">
        <dcterms:modified rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="c:AnnotationTitle"/></dcterms:modified>
      </xsl:when>

      <xsl:when test="c:AnnotationType = 'UPDATE_DATA'">
        <dcat:updateDate rdf:datatype="http://www.w3.org/2001/XMLSchema#dateTime"><xsl:value-of select="c:AnnotationTitle"/></dcat:updateDate>
      </xsl:when>

      <!-- Time coverage -->
      <xsl:when test="c:AnnotationType = 'OBS_PERIOD_OVERALL_OLDEST'">
        <dcat:temporalCoverage>
          <dcterms:PeriodOfTime>
            <dcat:startDate rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear"><xsl:value-of select="c:AnnotationTitle"/></dcat:startDate>
          </dcterms:PeriodOfTime>
        </dcat:temporalCoverage>
      </xsl:when>

      <xsl:when test="c:AnnotationType = 'OBS_PERIOD_OVERALL_LATEST'">
        <dcat:temporalCoverage>
          <dcterms:PeriodOfTime>
            <dcat:endDate rdf:datatype="http://www.w3.org/2001/XMLSchema#gYear"><xsl:value-of select="c:AnnotationTitle"/></dcat:endDate>
          </dcterms:PeriodOfTime>
        </dcat:temporalCoverage>
      </xsl:when>

      <!-- Observation count -->
      <xsl:when test="c:AnnotationType = 'OBS_COUNT'">
        <dcterms:extent><xsl:value-of select="c:AnnotationTitle"/> observations</dcterms:extent>
      </xsl:when>

      <!-- Source dataset -->
      <xsl:when test="c:AnnotationType = 'DISSEMINATION_SOURCE_DATASET'">
        <xsl:call-template name="split-source-datasets">
          <xsl:with-param name="datasets" select="c:AnnotationTitle"/>
        </xsl:call-template>
      </xsl:when>

      <!-- Metadata links -->
      <xsl:when test="c:AnnotationType = 'ESMS_HTML' and c:AnnotationURL">
        <dcat:landingPage>
          <xsl:attribute name="rdf:resource"><xsl:value-of select="c:AnnotationURL"/></xsl:attribute>
        </dcat:landingPage>
      </xsl:when>

      <xsl:when test="c:AnnotationType = 'ESMS_SDMX' and c:AnnotationURL">
        <dcat:distribution>
          <dcat:Distribution>
            <dcterms:format>application/zip</dcterms:format>
            <dcat:downloadURL>
              <xsl:attribute name="rdf:resource"><xsl:value-of select="c:AnnotationURL"/></xsl:attribute>
            </dcat:downloadURL>
          </dcat:Distribution>
        </dcat:distribution>
      </xsl:when>

      <!-- Source institutions -->
      <xsl:when test="c:AnnotationType = 'SOURCE_INSTITUTIONS'">
        <xsl:for-each select="c:AnnotationText">
          <dcterms:publisher>
            <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute>
            <xsl:value-of select="."/>
          </dcterms:publisher>
        </xsl:for-each>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- Template to split comma-separated source datasets -->
  <xsl:template name="split-source-datasets">
    <xsl:param name="datasets"/>
    <xsl:choose>
      <xsl:when test="contains($datasets, ',')">
        <!-- Process first dataset -->
        <xsl:variable name="first" select="normalize-space(substring-before($datasets, ','))"/>
        <dcterms:source>
          <xsl:attribute name="rdf:resource">../df/<xsl:value-of select="translate($first, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>#df</xsl:attribute>
        </dcterms:source>
        <!-- Recursively process remaining datasets -->
        <xsl:call-template name="split-source-datasets">
          <xsl:with-param name="datasets" select="substring-after($datasets, ',')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <!-- Process single/last dataset -->
        <xsl:variable name="dataset" select="normalize-space($datasets)"/>
        <xsl:if test="$dataset != ''">
          <dcterms:source>
            <xsl:attribute name="rdf:resource">../df/<xsl:value-of select="translate($dataset, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>#df</xsl:attribute>
          </dcterms:source>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*"/>
</xsl:stylesheet>
