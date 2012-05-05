<!--
  Generates chunked XHTML documents from DocBook XML source using DocBook XSL
  stylesheets.

  NOTE: The URL reference to the current DocBook XSL stylesheets is
  rewritten to point to the copy on the local disk drive by the XML catalog
  rewrite directives so it doesn't need to go out to the Internet for the
  stylesheets. This means you don't need to edit the <xsl:import> elements on
  a machine by machine basis.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exsl="http://exslt.org/common" version="1.0">
  <xsl:import href="../docbook-xsl/xhtml/chunkfast.xsl"/>

  <xsl:import href="http://docbook.sourceforge.net/release/xsl/current/xhtml/chunk.xsl"/>

  <xsl:import href="common.xsl"/>

  <!--<xsl:import href="html-params.xsl"/>-->

  <!--<xsl:import href="head.xsl"/>-->

  <xsl:import href="syntaxhighlight.xsl"/>

  <!--<xsl:import href="disqus-footer.xsl"/>-->



    <!-- chunking options -->
    <xsl:param name="chunk.section.depth" select="2"/>
    <xsl:param name="use.id.as.filename" select="1"/>

    <!-- css options -->
    <xsl:param name="html.stylesheet" select="'css/style.css'"/>
    <xsl:param name="css.decoration" select="'0'"/>

    <xsl:param name="toc.max.depth" select="1"/>
    <xsl:param name="generate.toc" select="''"/>

    <xsl:template name="footer.navigation">
      <div class="footer"> (c) 2012 The Qi4j Community</div>
    </xsl:template>

    <xsl:template name="header.navigation">
      <div class="logo"><a href="index.html"><img src="images/logo.png" /></a></div>
      <div class="top-nav">
        <xsl:variable name="tocTree">
          <xsl:call-template name="make.toc">
            <xsl:with-param name="toc-context" select="/article"/>
            <xsl:with-param name="toc.title.p" select="false()"/>
            <xsl:with-param name="nodes" select="/article/section"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:apply-templates select="exsl:node-set($tocTree)" mode="filter.links">
          <xsl:with-param name="filter">
            <xsl:call-template name="href.target">
              <xsl:with-param name="object" select="ancestor-or-self::section[parent::article]"/>
              <xsl:with-param name="context" select="ancestor-or-self::section[parent::article]"/>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:apply-templates>
      </div>

      <div class="sub-nav">
        <xsl:variable name="subTocTree">
          <xsl:call-template name="make.toc">
            <xsl:with-param name="toc.title.p" select="false()"/>
            <xsl:with-param name="toc-context"
                            select="descendant-or-self::section[parent::section][1]"/>
            <xsl:with-param name="nodes"
                            select="ancestor-or-self::section[parent::article]/section"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:apply-templates select="exsl:node-set($subTocTree)" mode="filter.links">
          <xsl:with-param name="filter">
            <xsl:call-template name="href.target">
              <xsl:with-param name="object" select="."/>
              <xsl:with-param name="context" select="."/>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:apply-templates>
      </div>
    </xsl:template>

    <xsl:template match="@*|node()" mode="filter.links">
      <xsl:param name="filter" select="NO-FILTER"/>
      <xsl:choose>
        <xsl:when test="(string-length($filter) > 0 and contains(@href,$filter))
                        or (string-length($filter) = 0 and contains(@href,'index.html'))">
          <span>
            <xsl:apply-templates select="@*|node()" mode="filter.links">
              <xsl:with-param name="filter" select="$filter"/>
            </xsl:apply-templates>
          </span>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy>
            <xsl:apply-templates select="@*|node()" mode="filter.links">
              <xsl:with-param name="filter" select="$filter"/>
            </xsl:apply-templates>
          </xsl:copy>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>



</xsl:stylesheet>

