<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" version="1.0" encoding="UTF-8"/>

  <xsl:param name="snippetId">&lt;undefined&gt;</xsl:param>
  <xsl:param name="outputId">&lt;undefined&gt;</xsl:param>

  <xsl:template match="java">
    <xsl:apply-templates select="snippet[@id=$snippetId]"/>
    <xsl:if test="not(snippet[@id=$snippetId])">
      <div class="codesnippet-error">
        No snippet with snippet id <tt><xsl:value-of select="$snippetId"/></tt> found.
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template match="snippet">
    <xsl:text>
</xsl:text>
    <div class="codesnippet" id="{$outputId}">
      <code><pre><xsl:copy-of select="*|text()"/></pre></code>
    </div>
  </xsl:template>

  <xsl:template match="*|@*|comment()">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
