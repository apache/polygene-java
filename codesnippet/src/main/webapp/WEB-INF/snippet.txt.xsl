<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="text" version="1.0" encoding="UTF-8"/>

  <xsl:param name="snippetId">&lt;undefined&gt;</xsl:param>
  <xsl:param name="outputId">&lt;undefined&gt;</xsl:param>

  <xsl:template match="java">
    <xsl:apply-templates select="snippet[@id=$snippetId]"/>
    <xsl:if test="not(snippet[@id=$snippetId])">
      <xsl:text>No snippet with snippet id '</xsl:text><xsl:value-of select="$snippetId"/><xsl:text>' found.</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="snippet">
    <xsl:text><xsl:copy-of select="*|text()"/></xsl:text>
  </xsl:template>

  <xsl:template match="*|@*|comment()">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
