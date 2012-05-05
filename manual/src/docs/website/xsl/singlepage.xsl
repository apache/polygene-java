<!--
  ~ Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~
  ~     You may obtain a copy of the License at
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<!--
  Generates single XHTML document from DocBook XML source using DocBook XSL
  stylesheets.

  NOTE: The URL reference to the current DocBook XSL stylesheets is
  rewritten to point to the copy on the local disk drive by the XML catalog
  rewrite directives so it doesn't need to go out to the Internet for the
  stylesheets. This means you don't need to edit the <xsl:import> elements on
  a machine by machine basis.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:import href="http://docbook.sourceforge.net/release/xsl/current/xhtml/docbook.xsl"/>
  <xsl:import href="common.xsl"/>

  <xsl:import href="html-params.xsl"/>

  <xsl:import href="head-offline.xsl"/>

  <xsl:import href="footer.xsl"/>

  <xsl:import href="syntaxhighlight.xsl"/>

  <!-- Add links back to top -->

  <xsl:template name="section.titlepage.before.recto">
    <xsl:variable name="top-anchor">
      <xsl:call-template name="object.id">
        <xsl:with-param name="object" select="/*[1]"/>
      </xsl:call-template>
    </xsl:variable>

    <p class="returntotop">
      <a href="#{$top-anchor}">
        <xsl:text>Return to top</xsl:text>
      </a>
    </p>
  </xsl:template>

</xsl:stylesheet>

