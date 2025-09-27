<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
   xmlns="http://www.w3.org/1999/xhtml"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   version="1.0">

  <xsl:output method="xml"
	      encoding="utf-8"
	      doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
	      doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" />

  <xsl:template match="codelists">
    <html>
      <head>
	<title>Inventory of Codelists</title>
	<style type="text/css" media="screen">
	  .title { font-size: 120% }

	  table {
	    margin-top: 1em;
	  }
	</style>
      </head>
      <body>
	<div>
	  <a href="/">Home</a>
	</div>

	<h1>Inventory of Codelists</h1>

	<p>
	  Total: <xsl:value-of select="count(codelist)"/> codelists
	</p>

	<p>
	  Generated from
	  <a href="https://ec.europa.eu/eurostat/api/dissemination/files/inventory?type=codelist">Eurostat codelists inventory</a>
	  at <xsl:value-of select="current-dateTime()"/>.
	</p>

	<p>
	  This catalog lists all available SKOS concept schemes (codelists) in the Eurostat system.
	  Each codelist contains standardized codes and labels for statistical dimensions.
	</p>

	<table>
	  <thead>
	    <tr>
	      <th>Code</th>
	      <th>Label</th>
	      <th>Version</th>
	      <th>Links</th>
	    </tr>
	  </thead>
	  <tbody>
	    <xsl:apply-templates select="codelist">
	      <xsl:sort select="code"/>
	    </xsl:apply-templates>
	  </tbody>
	</table>

      </body>
    </html>
  </xsl:template>

  <xsl:template match="codelist">
    <tr>
      <td>
	<a>
	  <xsl:attribute name="href">./cl/<xsl:value-of select="translate(code, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/></xsl:attribute>
	  <xsl:value-of select="code"/>
	</a>
      </td>
      <td><xsl:value-of select="label"/></td>
      <td><xsl:value-of select="version"/></td>
      <td>
	<xsl:if test="latest_sdmx_url">
	  <a>
	    <xsl:attribute name="href"><xsl:value-of select="latest_sdmx_url"/></xsl:attribute>
	    SDMX
	  </a>
	</xsl:if>
	<xsl:if test="latest_tsv_url and latest_sdmx_url"> | </xsl:if>
	<xsl:if test="latest_tsv_url">
	  <a>
	    <xsl:attribute name="href"><xsl:value-of select="latest_tsv_url"/></xsl:attribute>
	    TSV
	  </a>
	</xsl:if>
	<xsl:if test="mapping_file and (latest_sdmx_url or latest_tsv_url)"> | </xsl:if>
	<xsl:if test="mapping_file">
	  <a>
	    <xsl:attribute name="href"><xsl:value-of select="mapping_file"/></xsl:attribute>
	    Mapping
	  </a>
	</xsl:if>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>