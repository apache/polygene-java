<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template name="user.head.content">
  <xsl:text disable-output-escaping="yes">
<![CDATA[

<!-- favicon -->

<link rel="shortcut icon" href="http://qi4j.org/favicon.ico" type="image/vnd.microsoft.icon" />
<link rel="icon" href="http://qi4j.org/favicon.ico" type="image/x-icon" />

<!-- style -->

<link href="css/shCore.css" rel="stylesheet" type="text/css" />
<link href="css/shCoreEclipse.css" rel="stylesheet" type="text/css" />
<link href="css/shThemeEclipse.css" rel="stylesheet" type="text/css" />
<link href="css/qi4j.css" rel="stylesheet" type="text/css" />

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

<!-- Version Switcher -->

<script type="text/javascript" src="js/versionswitcher.js"></script>

<!-- Qi4j WebSite Progressive Enhancement -->

<link href="css/progressive-enhancement.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="js/jquery.scrollTo-1.4.2.js"></script>
<script type="text/javascript" src="js/progressive-enhancement.js"></script>

<!-- Analytics -->
 <script type="text/javascript">
  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-3118496-1']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();
 </script>
]]>
  </xsl:text>
  </xsl:template>

</xsl:stylesheet>

