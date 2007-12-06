<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:qi4j="http://www.qi4j.org/rdf/1.0#"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">

  <xsl:template match="rdf:RDF/qi4j:application">
    <html>
      <head>
        <title>
          <xsl:value-of select="rdfs:label"/>
        </title>
        <link rel="meta" type="application/rdf+xml" href="application.xml"/>
        <link rel="stylesheet" type="text/css" title="Complete"
              href="complete.css"/>
        <link rel="stylesheet" type="text/css" title="Structure"
              href="structure.css"/>
      </head>
      <body>
        <div class="toc">
          <ol>
            <li>Application
              <a href="#{@rdf:ID}">
                <xsl:value-of select="rdfs:label"/>
              </a>
            </li>
            <ol>
              <xsl:for-each select="qi4j:layers/qi4j:layer">
                <li>Layer
                  <a href="#{@rdf:ID}">
                    <xsl:value-of select="rdfs:label"/>
                  </a>
                </li>
                <xsl:for-each select="qi4j:modules/qi4j:module">
                  <ol>
                    <li>Module
                      <a href="#{@rdf:ID}">
                        <xsl:value-of select="rdfs:label"/>
                      </a>
                    </li>

                    <xsl:if test="qi4j:publiccomposites/qi4j:composite">
                      <ol>
                        <li>Public composites</li>
                        <xsl:for-each select="qi4j:publiccomposites/qi4j:composite">
                          <ol>
                            <li>Composite
                              <xsl:value-of select="rdfs:label"/>
                            </li>
                          </ol>
                        </xsl:for-each>
                      </ol>
                    </xsl:if>

                    <xsl:if test="qi4j:privatecomposites/qi4j:composite">
                      <ol>
                        <li>Private composites</li>
                        <xsl:for-each select="qi4j:privatecomposites/qi4j:composite">
                          <ol>
                            <li>Composite
                              <a href="#{@rdf:ID}">
                                <xsl:value-of select="rdfs:label"/>
                              </a>
                            </li>
                          </ol>
                        </xsl:for-each>
                      </ol>
                    </xsl:if>

                  </ol>
                </xsl:for-each>
              </xsl:for-each>
            </ol>
          </ol>
        </div>

        <div class="application">
          <a name="{@rdf:ID}"/>
          <h1>
            Application
            <xsl:value-of select="rdfs:label"/>
          </h1>
          <xsl:for-each select="qi4j:layers/qi4j:layer">
            <div class="layer">
              <a name="{@rdf:ID}"/>
              <h2>
                Layer
                <xsl:value-of select="rdfs:label"/>
              </h2>

              <xsl:for-each select="qi4j:modules/qi4j:module">
                <div class="module">
                  <a name="{@rdf:ID}"/>
                  <h3>
                    Module
                    <xsl:value-of select="rdfs:label"/>
                  </h3>

                  <xsl:if test="qi4j:publiccomposites/qi4j:composite">
                    <div class="public composites">
                      <h4>Public composites</h4>
                      <xsl:for-each select="qi4j:publiccomposites/qi4j:composite">
                        <h5>
                          Composite
                          <xsl:value-of select="rdfs:label"/>
                        </h5>
                      </xsl:for-each>
                    </div>
                  </xsl:if>

                  <xsl:if test="qi4j:privatecomposites/qi4j:composite">
                    <div class="private composites">
                      <h4>Private composites</h4>
                      <xsl:for-each select="qi4j:privatecomposites/qi4j:composite">
                        <xsl:apply-templates select="."/>
                      </xsl:for-each>
                    </div>
                  </xsl:if>
                </div>
              </xsl:for-each>
            </div>
          </xsl:for-each>
        </div>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="qi4j:composite">
    <div class="composite">
      <a name="{@rdf:ID}"/>
      <h5>
        Composite
        <xsl:value-of select="rdfs:label"/>
      </h5>

      <div class="methods">

        <xsl:for-each select="qi4j:methods/qi4j:method">
          <div class="method">
            <h6>
              <xsl:value-of select="rdfs:label"/>
            </h6>
            <xsl:if test="qi4j:constraints/qi4j:constraint">
              <div class="constraints">
                <h7>Constraints</h7>
                <xsl:for-each select="qi4j:constraints/qi4j:constraint">
                  <div class="constraint">
                    <xsl:value-of select="rdfs:label"/>
                  </div>
                </xsl:for-each>
              </div>
            </xsl:if>

            <xsl:if test="qi4j:concerns/qi4j:concern">
              <div class="concerns">
                <h7>Concerns</h7>
                <xsl:for-each select="qi4j:concerns/qi4j:concern">
                  <div class="concern">
                    <xsl:value-of select="rdfs:label"/>
                  </div>
                </xsl:for-each>
              </div>
            </xsl:if>

            <xsl:if test="qi4j:sideeffects/qi4j:sideeffect">
              <div class="sideeffects">
                <h7>Side-effects</h7>
                <xsl:for-each select="qi4j:sideeffects/qi4j:sideeffect">
                  <div class="sideeffect">
                    <xsl:value-of select="rdfs:label"/>
                  </div>
                </xsl:for-each>
              </div>
            </xsl:if>
          </div>
        </xsl:for-each>
      </div>
    </div>
  </xsl:template>
</xsl:stylesheet>