/*
 * Copyright 2009 Georg Ragaller. All rights reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.tools.codesnippet.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.qi4j.tools.codesnippet.parser.IndentStripFilter;
import org.qi4j.tools.codesnippet.parser.JavaXmlCommentsReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

/**
 * <p>
 * A servlet to provide codesnippets from annotated Java sources as xhtml or xhtml fragments.
 * </p>
 * <ul>
 * <li>Supported servlet <a href="#init-params">init parameters</a>.
 * <li>Supported <a href="#request-params">request parameters</a>.
 * <li><a href="#url-mapping">URL mapping</a>.
 * </ul>
 * <h4><a name="init-params">Init parameters</a></h3>
 * <p>
 * The servlet can be confiured with the following init parameters:
 * <dl>
 * <dt><tt>{@value #INIT_PARAM_SOURCE_BASE}</tt></dt>
 * <dd>An URL, which is used as the base for mapping the servlet request's path info to the URL of the requested java
 * source containing the code-snippet.<br/> It defaults to <tt>{@value #DEFAULT_SOURCE_BASE}</tt>.
 * <p>
 * <b style="color:red">Note:</b><br/> The URL must denote a directory, so a trailing slash '/' is required
 * </p>
 * </dd>
 * <dt><tt>{@value #INIT_PARAM_SOURCE_ENCODING}</tt></dt>
 * <dd>The encoding of the java source files.<br/> It defaults to <tt>{@value #DEFAULT_SOURCE_ENCODING}</tt>.</dd>
 * <dt><tt>{@value #INIT_PARAM_OUTPUT_ENCODING}</tt></dt>
 * <dd>The encoding of the result of the snippet-request.<br/> It defaults to <tt>{@value #DEFAULT_OUTPUT_ENCODING}</tt>
 * .</dd>
 * </dl>
 * </p> <h4><a name="request-params">Request parameters</a></h4>
 * <p>
 * The following request parameters are supported:
 * <dl>
 * <dt><tt>{@value #REQ_PARAM_SNIPPET_ID}</tt></dt>
 * <dd>The id of the snippet to retrieve. The snippet id must be unique in a source file.<br/> This parameter is
 * <em>required</em>.</dd>
 * <dt><tt>{@value #REQ_PARAM_SOURCE_ENCODING}</tt></dt>
 * <dd>The encoding of the source file the snippet is retrieved from.<br/> This parameter can be used to override the
 * {@value #INIT_PARAM_SOURCE_ENCODING} init parameter.<br/> This parameter is <em>optional</em>.</dd>
 * <dt><tt>{@value #REQ_PARAM_OUTPUT_ID}</tt></dt>
 * <dd>The id which is used for the id attribute in the outermost block element of the output, if the outpt type supports it.
 * This parameter is <em>optional</em> and defaults to the value of the {@value #REQ_PARAM_SNIPPET_ID} parameter.</dd>
 * </dl>
 * </p>
 * <h4><a name="url-mapping">URL mapping</a></h4>
 * <p>
 * The URL mapping is as follows:
 * <ol>
 * <li>The value of the {@link HttpServletRequest#getPathInfo()} is retrieved.</li>
 * <li>The suffix of this value is checked against the supported output types (see {@link CodesnippetResponseType}) and
 * removed.</li>
 * <li>The remaining part is used to create an URL, relative to the source base URL (see <a href="#init-params">init
 * parameters</a>). This URL is then used to retrieve and parse the java code.</li>
 * </ol>
 * <h5>Example:</h5>
 * <p>
 * Let's assume the servlet runs in the <samp>/qi4-codesnippet</samp> servlet context on <samp>localhost</samp>. With
 * the source base set to the (fictitious) URL <tt>http://snippets.localdomain/src/main/java/</tt> and a request URL of
 * <samp>http://localhost:8080/qi4j-codesnippet/org/qi4j/samples/SnippetSample.java.xhtml?snippet=main</samp>, the java
 * source at <samp>http://snippets.localdomain/src/main/java/org/qi4j/samples/SnippetSample.java</samp> will be
 * retrieved, the snippet <samp>main</samp> extracted and rendered as a {@link CodesnippetResponseType#xhtml xhtml}
 * document.
 * </p>
 * </p>
 */
public final class CodesnippetServlet
    extends HttpServlet
{
    public static final String REQ_PARAM_SOURCE_ENCODING = "encoding";
    public static final String REQ_PARAM_SNIPPET_ID = "snippet";
    public static final String REQ_PARAM_OUTPUT_ID = "id";
    public static final String INIT_PARAM_SOURCE_BASE = "codesnippet.sourceBase";
    public static final String DEFAULT_SOURCE_BASE = "https://scm.ops4j.org/repos/ops4j/projects/qi4j/";
    public static final String INIT_PARAM_SOURCE_ENCODING = "codesnippet.sourceEncoding";
    public static final String DEFAULT_SOURCE_ENCODING = "UTF-8";
    public static final String INIT_PARAM_OUTPUT_ENCODING = "codesnippet.outputEncoding";
    public static final String DEFAULT_OUTPUT_ENCODING = "UTF-8";

    private URL sourceBase;
    private Map<CodesnippetResponseType, Templates> allTemplates;
    private String sourceEncoding;
    private String outputEncoding;

    @Override
    public void init()
        throws ServletException
    {
        try
        {
            sourceBase = new URL( getInitParameter( INIT_PARAM_SOURCE_BASE, DEFAULT_SOURCE_BASE ) );
            sourceEncoding = getInitParameter( INIT_PARAM_SOURCE_ENCODING, DEFAULT_SOURCE_ENCODING );
            outputEncoding = getInitParameter( INIT_PARAM_OUTPUT_ENCODING, DEFAULT_OUTPUT_ENCODING );
        }
        catch( MalformedURLException ex )
        {
            throw new ServletException( "error while initializing " + CodesnippetServlet.class.getName(), ex );
        }

        allTemplates = createTemplates();
    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
        throws ServletException,
               IOException
    {
        String snippetId = req.getParameter( REQ_PARAM_SNIPPET_ID );

        if( null == snippetId )
        {
            resp.sendError( HttpServletResponse.SC_BAD_REQUEST, "missing parameter 'snippet'" );
            return;
        }

        String outputId = req.getParameter( REQ_PARAM_OUTPUT_ID );

        if( null == outputId )
        {
            assert ( null != snippetId );
            outputId = snippetId;
        }

        String pathInfo = req.getPathInfo();

        if( null == pathInfo )
        {
            resp.sendError( HttpServletResponse.SC_BAD_REQUEST, "missing path info" );
            return;
        }

        String suffix = getSuffix( pathInfo );

        if( !CodesnippetResponseType.isValidName( suffix ) )
        {
            resp.sendError( HttpServletResponse.SC_BAD_REQUEST, "invalid suffix: ." + suffix );
            return;
        }

        String encoding = req.getParameter( REQ_PARAM_SOURCE_ENCODING );

        URL source = new URL( sourceBase, "." + removeSuffix( pathInfo, suffix ) );

        try
        {
            writeSnippet( source, snippetId, outputId, encoding, resp, CodesnippetResponseType.valueOf( suffix ) );
        }
        catch( ServletException ex )
        {
            throw new ServletException( getRootCause( ex ) );
        }
    }

    private String removeSuffix( String pathInfo, String suffix )
    {
        assert ( null != pathInfo );
        assert ( null != suffix );

        return pathInfo.substring( 0, pathInfo.length() - suffix.length() - 1 );
    }

    private String getSuffix( String pathInfo )
    {
        assert ( null != pathInfo );

        int idx = pathInfo.lastIndexOf( '.' );

        if( -1 == idx )
        {
            return null;
        }
        else
        {
            return pathInfo.substring( idx + 1 );
        }
    }

    /**
     * Create pre-compiled instance of the stylesheets. Since a {@link Templates} instance is thread-safe by definition,
     * we can store them for later usage.
     *
     * @throws ServletException
     */
    private Map<CodesnippetResponseType, Templates> createTemplates()
        throws ServletException
    {
        Map<CodesnippetResponseType, Templates> result = new HashMap<CodesnippetResponseType, Templates>();

        SAXTransformerFactory stf = createTransformerFactory();

        try
        {
            for( CodesnippetResponseType outputType : CodesnippetResponseType.values() )
            {
                Templates templates = createTemplates( stf, outputType.name() );
                result.put( outputType, templates );
            }
            return result;
        }
        catch( TransformerConfigurationException ex )
        {
            throw new ServletException( "error while creating XSLT templates", ex );
        }
    }

    /**
     * @param stf        factory to use for creating the {@link Templates} instance
     * @param outputType output type used to lookup the XSL stylesheet
     *
     * @return a new templates instance based on a XSL stylesheet
     *
     * @throws TransformerConfigurationException
     *                          when parsing to construct the Templates object fails
     * @throws ServletException if the XSL stylesheet cannot be located
     */
    private Templates createTemplates( SAXTransformerFactory stf, String outputType )
        throws TransformerConfigurationException,
               ServletException
    {
        String resource = "/WEB-INF/snippet." + outputType + ".xsl";
        InputStream is = getServletContext().getResourceAsStream( resource );

        if( null == is )
        {
            throw new ServletException( "packaging error, missing xsl: " + resource );
        }

        Source ss = new StreamSource( is );
        Templates templates = stf.newTemplates( ss );
        return templates;
    }

    private SAXTransformerFactory createTransformerFactory()
        throws TransformerFactoryConfigurationError,
               ServletException
    {
        TransformerFactory tf;
        tf = TransformerFactory.newInstance();
        if( !tf.getFeature( SAXTransformerFactory.FEATURE ) )
        {
            throw new ServletException( SAXTransformerFactory.FEATURE + " not supported by the JAXP implementation" );
        }
        if( !tf.getFeature( SAXTransformerFactory.FEATURE_XMLFILTER ) )
        {
            throw new ServletException( SAXTransformerFactory.FEATURE_XMLFILTER
                                        + " not supported by the JAXP implementation" );
        }
        return (SAXTransformerFactory) tf;
    }

    /**
     * Reads the given java source, uses a transformer pipeline to extract and format the specified snippet and writes
     * the result to the given print writer
     *
     * @param encoding   the encoding of the java source file, if <tt>null</tt>, then the globally configured encoding is
     *                   assumed
     * @param javaSource the url denoting the java source file to read
     * @param snippetId  the id of the codesnippet to extract
     * @param outputId   see {@link #REQ_PARAM_OUTPUT_ID}
     * @param output     the print writer, the transformed result is written into
     * @param templates  the templates instance used to create the transformer
     */
    private void transform( String encoding,
                            URL javaSource,
                            String snippetId,
                            String outputId,
                            PrintWriter output,
                            Templates templates
    )
        throws TransformerFactoryConfigurationError,
               ServletException,
               ParserConfigurationException,
               SAXException,
               IOException,
               TransformerException
    {
        SAXTransformerFactory stf = createTransformerFactory();
        SAXParserFactory spf = SAXParserFactory.newInstance();

        spf.setNamespaceAware( true );
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();

        XMLFilter stripIndent = new IndentStripFilter( REQ_PARAM_SNIPPET_ID );
        XMLFilter snippet = stf.newXMLFilter( templates );

        stripIndent.setParent( reader );
        snippet.setParent( stripIndent );

        SAXSource xmlSource = new SAXSource( stripIndent, new InputSource( new JavaXmlCommentsReader(
            new InputStreamReader( javaSource.openStream(), ( null == encoding ) ? sourceEncoding : encoding ) ) ) );
        Result outputTarget = new StreamResult( output );

        Transformer transformer = templates.newTransformer();
        transformer.setParameter( "snippetId", snippetId );
        transformer.setParameter( "outputId", outputId );
        transformer.transform( xmlSource, outputTarget );
    }

    private String getInitParameter( String name, String defaultValue )
    {
        String value = getInitParameter( name );
        return ( null == value ? defaultValue : value );
    }

    private void writeSnippet( URL source, String snippetId, String outputId, String encoding,
                               HttpServletResponse resp, CodesnippetResponseType outputType
    )
        throws ServletException
    {
        try
        {
            resp.setCharacterEncoding( outputEncoding );
            resp.setContentType( outputType.getContentType() );
            transform( encoding, source, snippetId, outputId, resp.getWriter(), allTemplates.get( outputType ) );
        }
        catch( TransformerFactoryConfigurationError ex )
        {
            throw new ServletException( ex );
        }
        catch( ParserConfigurationException ex )
        {
            throw new ServletException( ex );
        }
        catch( SAXException ex )
        {
            throw new ServletException( ex );
        }
        catch( IOException ex )
        {
            throw new ServletException( ex );
        }
        catch( TransformerException ex )
        {
            throw new ServletException( ex );
        }
    }

    /**
     * Remove unwanted intermediary {@link Throwable}s.
     */
    private Throwable getRootCause( Throwable ex )
    {
        Throwable rootCause = ex.getCause();

        for( Throwable cause = rootCause; null != cause; cause = cause.getCause() )
        {
            rootCause = cause;
        }
        return rootCause;
    }
}
