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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template name="user.head.content">
<xsl:text disable-output-escaping="yes">
<![CDATA[

<!-- favicon -->

<link rel="shortcut icon" href="http://polygene.apache.org/favicon.ico" type="image/vnd.microsoft.icon" />
<link rel="icon" href="http://polygene.apache.org/favicon.ico" type="image/x-icon" />

<!-- style -->

<link href="css/shCore.css" rel="stylesheet" type="text/css" />
<link href="css/shCoreEclipse.css" rel="stylesheet" type="text/css" />
<link href="css/shThemeEclipse.css" rel="stylesheet" type="text/css" />
<link href="css/polygene.css" rel="stylesheet" type="text/css" />

<!-- Syntax Highlighter -->

<script type="text/javascript" src="js/shCore.js"></script>
<script type="text/javascript" src="js/shBrushJava.js"></script>
<script type="text/javascript" src="js/shBrushScala.js"></script>
<script type="text/javascript" src="js/shBrushJScript.js"></script>
<script type="text/javascript" src="js/shBrushBash.js"></script>
<script type="text/javascript" src="js/shBrushPlain.js"></script>
<script type="text/javascript" src="js/shBrushXml.js"></script>
<script type="text/javascript" src="js/shBrushGroovy.js"></script>
<script type="text/javascript" src="js/shBrushPython.js"></script>
<script type="text/javascript" src="js/shBrushRuby.js"></script>
<script type="text/javascript" src="js/shBrushCSharp.js"></script>

<script type="text/javascript">
  SyntaxHighlighter.defaults['tab-size'] = 4;
  SyntaxHighlighter.defaults['gutter'] = false;
  SyntaxHighlighter.defaults['toolbar'] = false;
  SyntaxHighlighter.all()
</script>

<!-- JQuery -->

<script type="text/javascript" src="js/jquery-1.6.4.min.js"></script>

<!-- Image Scaler -->

<script type="text/javascript" src="js/imagescaler.js"></script>

<!-- Table Styler -->

<script type="text/javascript" src="js/tablestyler.js"></script>

]]>
</xsl:text>
  </xsl:template>

</xsl:stylesheet>

