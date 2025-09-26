<?xml version='1.0' encoding='utf-8'?>

<xsl:stylesheet
   xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
   xmlns:dcterms="http://purl.org/dc/terms/"
   xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
   xmlns:qb="http://purl.org/linked-data/cube#"
   xmlns:skos="http://www.w3.org/2004/02/skos/core#"
   xmlns:foaf="http://xmlns.com/foaf/0.1/"
   xmlns:prov="http://www.w3.org/ns/prov#"
   xmlns:m="http://www.sdmx.org/resources/sdmxml/schemas/v3_0/message"
   xmlns:s="http://www.sdmx.org/resources/sdmxml/schemas/v3_0/structure"
   xmlns:c="http://www.sdmx.org/resources/sdmxml/schemas/v3_0/common"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   version='1.0'>

  <xsl:output method='xml' encoding='utf-8'/>

  <xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

  <!-- Template to convert URN to relative /cl URI -->
  <!-- Input: urn:sdmx:org.sdmx.infomodel.codelist.Codelist=ESTAT:FREQ(3.7) -->
  <!-- Output: ../cl/freq#cl-FREQ -->
  <xsl:template name="urn-to-cl-uri">
    <xsl:param name="urn"/>
    <xsl:if test="contains($urn, '=ESTAT:')">
      <xsl:variable name="after-estat" select="substring-after($urn, '=ESTAT:')"/>
      <xsl:variable name="codelist-id">
        <xsl:choose>
          <xsl:when test="contains($after-estat, '(')">
            <xsl:value-of select="substring-before($after-estat, '(')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$after-estat"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:value-of select="concat('../cl/', translate($codelist-id, $uppercase, $lowercase), '#cl-', $codelist-id)"/>
    </xsl:if>
  </xsl:template>



  <!-- Template to convert URN to relative /cs URI -->
  <!-- Input: urn:sdmx:org.sdmx.infomodel.conceptscheme.Concept=ESTAT:TAG00038(19.0).freq -->
  <!-- Output: ../cs/tag00038#concept-freq -->
  <xsl:template name="urn-to-cs-uri">
    <xsl:param name="urn"/>
    <xsl:if test="contains($urn, '=ESTAT:')">
      <xsl:variable name="after-estat" select="substring-after($urn, '=ESTAT:')"/>
      <xsl:variable name="scheme-and-concept">
        <xsl:choose>
          <xsl:when test="contains($after-estat, '(')">
            <xsl:variable name="after-version" select="substring-after($after-estat, ')')"/>
            <xsl:variable name="scheme-id" select="substring-before($after-estat, '(')"/>
            <xsl:variable name="concept-id" select="substring-after($after-version, '.')"/>
            <xsl:value-of select="concat('../cs/', translate($scheme-id, $uppercase, $lowercase), '#concept-', $concept-id)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat('../cs/', translate($after-estat, $uppercase, $lowercase))"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:value-of select="$scheme-and-concept"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match='m:Structure'>
    <rdf:RDF>
      <rdf:Description rdf:about="">
        <rdfs:comment>No guarantee of correctness! USE AT YOUR OWN RISK!</rdfs:comment>
        <dcterms:publisher>Eurostat (http://epp.eurostat.ec.europa.eu/) via Linked Eurostat (http://estatwrap.ontologycentral.com/)</dcterms:publisher>
        <foaf:topic rdf:resource="#ds"/>
        <prov:wasGeneratedBy rdf:resource="#transformation"/>
      </rdf:Description>

      <!-- PROV: Transformation activity -->
      <prov:Activity rdf:about="#transformation">
        <rdfs:label>SDMX to RDF Data Structure Transformation</rdfs:label>
        <prov:used>
          <xsl:attribute name="rdf:resource">https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/structure/datastructure/ESTAT/<xsl:value-of select="//s:DataStructure/@id"/></xsl:attribute>
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

      <!-- Process data structures -->
      <xsl:apply-templates select="m:Structures/s:DataStructures"/>

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

  <!-- Template for DataStructures -->
  <xsl:template match='s:DataStructures'>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match='s:DataStructure'>
    <qb:DataStructureDefinition>
      <xsl:attribute name="rdf:about">#ds</xsl:attribute>

      <!-- Basic identifiers -->
      <dcterms:identifier><xsl:value-of select="@id"/></dcterms:identifier>
      <skos:notation><xsl:value-of select="@urn"/></skos:notation>
      <dcterms:creator><xsl:value-of select="@agencyID"/></dcterms:creator>
      <dcterms:hasVersion><xsl:value-of select="@version"/></dcterms:hasVersion>

      <!-- Process names -->
      <xsl:apply-templates select="c:Name"/>

      <!-- PROV: Derived from original SDMX source -->
      <prov:wasDerivedFrom>
        <xsl:attribute name="rdf:resource">https://ec.europa.eu/eurostat/api/dissemination/sdmx/3.0/structure/datastructure/ESTAT/<xsl:value-of select="@id"/></xsl:attribute>
      </prov:wasDerivedFrom>
      <prov:wasGeneratedBy rdf:resource="#transformation"/>

      <!-- Process dimensions -->
      <xsl:apply-templates select="s:DataStructureComponents/s:DimensionList"/>

      <!-- Process attributes -->
      <xsl:apply-templates select="s:DataStructureComponents/s:AttributeList"/>

      <!-- Process measures -->
      <xsl:apply-templates select="s:DataStructureComponents/s:MeasureList"/>
    </qb:DataStructureDefinition>
  </xsl:template>

  <!-- Template for Names -->
  <xsl:template match='s:DataStructure/c:Name'>
    <rdfs:label>
      <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute>
      <xsl:value-of select="."/>
    </rdfs:label>
  </xsl:template>

  <!-- Template for Dimensions -->
  <xsl:template match='s:DimensionList'>
    <xsl:apply-templates select="s:Dimension | s:TimeDimension"/>
  </xsl:template>

  <xsl:template match='s:Dimension | s:TimeDimension'>
    <qb:component>
      <qb:ComponentSpecification>
        <xsl:attribute name="rdf:about">#component-<xsl:value-of select="@id"/></xsl:attribute>
        <qb:dimension>
          <qb:CodedProperty>
            <xsl:attribute name="rdf:about">#dim-<xsl:value-of select="@id"/></xsl:attribute>
            <dcterms:identifier><xsl:value-of select="@id"/></dcterms:identifier>
            <xsl:if test="@position">
              <qb:order rdf:datatype="http://www.w3.org/2001/XMLSchema#integer"><xsl:value-of select="@position"/></qb:order>
            </xsl:if>
            <!-- Add rdfs:range based on dimension type -->
            <xsl:choose>
              <xsl:when test="s:LocalRepresentation/s:Enumeration">
                <!-- Dimensions with code lists have range skos:Concept -->
                <rdfs:range rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
                <qb:codeList>
                  <xsl:attribute name="rdf:resource">
                    <xsl:call-template name="urn-to-cl-uri">
                      <xsl:with-param name="urn" select="s:LocalRepresentation/s:Enumeration"/>
                    </xsl:call-template>
                  </xsl:attribute>
                </qb:codeList>
              </xsl:when>
              <xsl:when test="local-name() = 'TimeDimension' or @id = 'TIME_PERIOD'">
                <!-- Time dimensions have appropriate time range -->
                <rdfs:range rdf:resource="http://www.w3.org/2001/XMLSchema#gYear"/>
              </xsl:when>
              <xsl:otherwise>
                <!-- Default for other dimensions -->
                <rdfs:range rdf:resource="http://www.w3.org/2001/XMLSchema#string"/>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="s:ConceptIdentity">
              <qb:concept>
                <xsl:attribute name="rdf:resource">
                  <xsl:call-template name="urn-to-cs-uri">
                    <xsl:with-param name="urn" select="s:ConceptIdentity"/>
                  </xsl:call-template>
                </xsl:attribute>
              </qb:concept>
            </xsl:if>
          </qb:CodedProperty>
        </qb:dimension>
      </qb:ComponentSpecification>
    </qb:component>
  </xsl:template>

  <!-- Template for Attributes -->
  <xsl:template match='s:AttributeList'>
    <xsl:apply-templates select="s:Attribute"/>
  </xsl:template>

  <xsl:template match='s:Attribute'>
    <qb:component>
      <qb:ComponentSpecification>
        <xsl:attribute name="rdf:about">#component-<xsl:value-of select="@id"/></xsl:attribute>
        <qb:attribute>
          <qb:AttributeProperty>
            <xsl:attribute name="rdf:about">#attr-<xsl:value-of select="@id"/></xsl:attribute>
            <dcterms:identifier><xsl:value-of select="@id"/></dcterms:identifier>
            <xsl:if test="s:LocalRepresentation/s:Enumeration">
              <qb:codeList>
                <xsl:attribute name="rdf:resource">
                  <xsl:call-template name="urn-to-cl-uri">
                    <xsl:with-param name="urn" select="s:LocalRepresentation/s:Enumeration"/>
                  </xsl:call-template>
                </xsl:attribute>
              </qb:codeList>
            </xsl:if>
            <xsl:if test="s:ConceptIdentity">
              <qb:concept>
                <xsl:attribute name="rdf:resource">
                  <xsl:call-template name="urn-to-cs-uri">
                    <xsl:with-param name="urn" select="s:ConceptIdentity"/>
                  </xsl:call-template>
                </xsl:attribute>
              </qb:concept>
            </xsl:if>
          </qb:AttributeProperty>
        </qb:attribute>
      </qb:ComponentSpecification>
    </qb:component>
  </xsl:template>

  <!-- Template for Measures -->
  <xsl:template match='s:MeasureList'>
    <xsl:apply-templates select="s:Measure"/>
  </xsl:template>

  <xsl:template match='s:Measure'>
    <qb:component>
      <qb:ComponentSpecification>
        <xsl:attribute name="rdf:about">#component-<xsl:value-of select="@id"/></xsl:attribute>
        <qb:measure>
          <qb:MeasureProperty>
            <xsl:attribute name="rdf:about">#measure-<xsl:value-of select="@id"/></xsl:attribute>
            <dcterms:identifier><xsl:value-of select="@id"/></dcterms:identifier>
            <xsl:if test="s:LocalRepresentation/s:TextFormat/@textType">
              <rdfs:range>
                <xsl:choose>
                  <xsl:when test="s:LocalRepresentation/s:TextFormat/@textType = 'Double'">
                    <xsl:attribute name="rdf:resource">http://www.w3.org/2001/XMLSchema#double</xsl:attribute>
                  </xsl:when>
                  <xsl:when test="s:LocalRepresentation/s:TextFormat/@textType = 'Integer'">
                    <xsl:attribute name="rdf:resource">http://www.w3.org/2001/XMLSchema#integer</xsl:attribute>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:attribute name="rdf:resource">http://www.w3.org/2001/XMLSchema#string</xsl:attribute>
                  </xsl:otherwise>
                </xsl:choose>
              </rdfs:range>
            </xsl:if>
            <xsl:if test="s:ConceptIdentity">
              <qb:concept>
                <xsl:attribute name="rdf:resource">
                  <xsl:call-template name="urn-to-cs-uri">
                    <xsl:with-param name="urn" select="s:ConceptIdentity"/>
                  </xsl:call-template>
                </xsl:attribute>
              </qb:concept>
            </xsl:if>
          </qb:MeasureProperty>
        </qb:measure>
      </qb:ComponentSpecification>
    </qb:component>
  </xsl:template>

  <xsl:template match="*"/>
</xsl:stylesheet>