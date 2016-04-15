<?xml version="1.0" encoding="iso-8859-1"?>

<!--
  ~  Licensed to the Apache Software Foundation (ASF) under one
  ~  or more contributor license agreements.  See the NOTICE file
  ~  distributed with this work for additional information
  ~  regarding copyright ownership.  The ASF licenses this file
  ~  to you under the Apache License, Version 2.0 (the
  ~  "License"); you may not use this file except in compliance
  ~  with the License.  You may obtain a copy of the License at
  ~
  ~       http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  ~
  ~
  -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:res="http://www.w3.org/2005/sparql-results#"
                exclude-result-prefixes="res xsl">

  <!--
  <xsl:output
    method="html"
    media-type="text/html"
    doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
    indent="yes"
    encoding="UTF-8"/>
  -->

  <xsl:output
    method="xml"
    indent="yes"
    encoding="UTF-8"
    doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
    omit-xml-declaration="no"/>

  <xsl:template name="header">

    <div>
      <h2>Header</h2>
      <xsl:for-each select="res:head/res:link">
        <p>Link to
          <xsl:value-of select="@href"/>
        </p>
      </xsl:for-each>
    </div>
  </xsl:template>

  <xsl:template name="boolean-result">

    <div>
      <h2>Boolean Result</h2>
      <p>Value
        <xsl:value-of select="res:boolean"/>
      </p>
    </div>
  </xsl:template>


  <xsl:template name="vb-result">
    <table border="1">
      <xsl:text>
      </xsl:text>
      <tr>
        <xsl:for-each select="res:head/res:variable">
          <th>
            <xsl:value-of select="@name"/>
          </th>
        </xsl:for-each>

      </tr>
      <xsl:text>
      </xsl:text>
      <xsl:for-each select="res:results/res:result">
        <tr>
            <xsl:apply-templates select="."/>
        </tr>
      </xsl:for-each>
    </table>

  </xsl:template>

  <xsl:template match="res:result">
    <xsl:variable name="current" select="."/>
    <xsl:for-each select="//res:head/res:variable">
      <xsl:variable name="name" select="@name"/>
      <td>
        <xsl:choose>

          <xsl:when test="$current/res:binding[@name=$name]">
            <!-- apply template for the correct value type (bnode, uri,
           literal) -->

              <xsl:choose>
                  <xsl:when test="$name = 'identity'">
                    <a href="entity/{$current/res:binding[@name=$name]}"><xsl:apply-templates select="$current/res:binding[@name=$name]"/></a>
                  </xsl:when>
                  <xsl:otherwise>
                      <xsl:apply-templates select="$current/res:binding[@name=$name]"/>
                  </xsl:otherwise>
              </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <!-- no binding available for this variable in this solution -->
            [unbound]
          </xsl:otherwise>
        </xsl:choose>

      </td>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="res:bnode">
    <xsl:text>nodeID</xsl:text>
    <xsl:value-of select="text()"/>
  </xsl:template>

  <xsl:template match="res:uri">
    <xsl:variable name="uri" select="text()"/>
    <xsl:text>URI </xsl:text>
    <xsl:value-of select="$uri"/>
  </xsl:template>

  <xsl:template match="res:literal">
    <xsl:choose>
      <xsl:when test="@datatype">

        <!-- datatyped literal value -->
        <xsl:value-of select="text()"/>
        (datatype
        <xsl:value-of select="@datatype"/>
        )
      </xsl:when>
      <xsl:when test="@xml:lang">
        <!-- lang-string -->
        <xsl:value-of select="text()"/>
        @
        <xsl:value-of select="@xml:lang"/>
      </xsl:when>

      <xsl:when test="string-length(text()) != 0">
        <!-- present and not empty -->
        <xsl:value-of select="text()"/>
      </xsl:when>
      <xsl:when test="string-length(text()) = 0">
        <!-- present and empty -->
        [empty literal]
      </xsl:when>
    </xsl:choose>

  </xsl:template>

  <xsl:template match="res:sparql">
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
      <head>
        <title>SPARQL Query Results</title>
      </head>
      <body>

        <xsl:if test="res:head/res:link">
          <xsl:call-template name="header"/>
        </xsl:if>

        <xsl:choose>
          <xsl:when test="res:boolean">
            <xsl:call-template name="boolean-result"/>

          </xsl:when>

          <xsl:when test="res:results">
            <xsl:call-template name="vb-result"/>
          </xsl:when>

        </xsl:choose>


      </body>
    </html>

  </xsl:template>
</xsl:stylesheet>
