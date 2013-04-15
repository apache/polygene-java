/*
 * Copyright 2009 Niclas Hedhman.
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

package org.qi4j.sample.rental.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.qi4j.api.Qi4j;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.Energy4Java;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import static org.qi4j.functional.Iterables.first;

public class QuikitServlet
    extends HttpServlet
{
    private Application application;
    private ServiceFinder finder;
    private UrlService urlService;
    private DocumentBuilderFactory documentFactory;
    private TreeMap<String, Page> mountPoints;

    @Override
    public void init( ServletConfig config )
        throws ServletException
    {
        try
        {
            mountPoints = new TreeMap<String, Page>();
            documentFactory = DocumentBuilderFactory.newInstance();
            documentFactory.setNamespaceAware( true );
            ClassLoader cl = getClass().getClassLoader();
            SchemaFactory schemaFactory = SchemaFactory.newInstance( "http://www.w3.org/2001/XMLSchema" );
            Source[] schemaSources = new Source[ 2 ];
            schemaSources[ 0 ] = new StreamSource( cl.getResourceAsStream( "xhtml1-strict.xsd" ) );
            schemaSources[ 1 ] = new StreamSource( cl.getResourceAsStream( "xml.xsd" ) );
            Schema schema = schemaFactory.newSchema( schemaSources );
            documentFactory.setSchema( schema );

            ApplicationAssembler assembler = createApplicationAssembler( config );

            Energy4Java qi4j = new Energy4Java();
            application = qi4j.newApplication( assembler );
            application.activate();
            Module module = application.findModule( "WebLayer", "PagesModule" );
            finder = module;

            if( application.mode() == Application.Mode.development )
            {
                DataInitializer initializer = module.newTransient( DataInitializer.class );
                initializer.initialize();
            }
            Iterable<ServiceReference<Page>> iterable = finder.findServices( Page.class );
            for( ServiceReference<Page> page : iterable )
            {
                PageMetaInfo pageMetaInfo = page.metaInfo( PageMetaInfo.class );
                String mountPoint = pageMetaInfo.mountPoint();
                mountPoints.put( mountPoint, page.get() );
            }
        }
        catch( Exception e )
        {
            throw new ServletException( "Can not initialize Qi4j.", e );
        }
    }

    private ApplicationAssembler createApplicationAssembler( ServletConfig config )
        throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
    {
        String assemblerClassname = config.getInitParameter( "qi4j-assembler" );
        ClassLoader loader = getClass().getClassLoader();
        Class<?> assemblerClass = loader.loadClass( assemblerClassname );
        ApplicationAssembler assembler;
        Constructor cons = assemblerClass.getConstructor( Application.Mode.class );
        if( cons == null )
        {
            assembler = (ApplicationAssembler) assemblerClass.newInstance();
        }
        else
        {
            Application.Mode mode;
            String modeSetting = config.getInitParameter( "qi4j-application-mode" );
            if( modeSetting == null )
            {
                mode = Application.Mode.development;
            }
            else
            {
                mode = Application.Mode.valueOf( modeSetting );
            }
            assembler = (ApplicationAssembler) cons.newInstance( mode );
        }
        return assembler;
    }

    @Override
    public void destroy()
    {
        try
        {
            application.passivate();
        }
        catch( Exception e )
        {
            throw new RuntimeException( "Problem to passivate Qi4j", e );
        }
    }

    @Override
    protected void doGet( HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse )
        throws ServletException, IOException
    {
        String path = httpServletRequest.getPathInfo();
        if( urlService == null )
        {
            urlService = finder.findService( UrlService.class ).get();
            String uri = httpServletRequest.getRequestURI();
            String basePath = uri.substring( 0, uri.length() - path.length() );
            urlService.registerBaseUri( basePath );
        }
        PrintWriter output = httpServletResponse.getWriter();
        while( path.startsWith( "//" ) )
        {
            path = path.substring( 1 );
        }
        for( Map.Entry<String, Page> entry : mountPoints.entrySet() )
        {
            if( path.startsWith( entry.getKey() ) )
            {
                Page page = entry.getValue();
                String subPath = path.substring( entry.getKey().length() );
                try
                {
                    renderPage( page, subPath, output, httpServletRequest );
                }
                catch( Exception e )
                {
                    throw new ServletException( e );
                }
                break;
            }
        }
        output.flush();
    }

    private void renderPage( Page page, String path, PrintWriter output, HttpServletRequest httpRequest )
        throws ParserConfigurationException, SAXException, IOException, RenderException, TransformerException
    {
        Class<? extends Composite> pageClass = (Class<Composite>) first( Qi4j.FUNCTION_DESCRIPTOR_FOR
                                                                             .map( page ).types() );

        String pageName = pageClass.getSimpleName() + ".html";
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        documentBuilder.setEntityResolver( createEntityResolver( documentBuilder ) );
        InputStream pageResource = pageClass.getResourceAsStream( pageName );
        Document dom = documentBuilder.parse( pageResource );
        try
        {
            Context context = new Context( dom, page, path, httpRequest );
            page.render( context );
        }
        catch( Throwable e )
        {
            error( dom, e );
        }
        DOMSource source = new DOMSource( dom );
        StreamResult result = new StreamResult( output );
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform( source, result );
        output.flush();
    }

    private EntityResolver createEntityResolver( DocumentBuilder builder )
        throws SAXException
    {
        QuikitResolver quickitResolver = new QuikitResolver( builder );
        SchemaFactory schemaFactory = SchemaFactory.newInstance( "http://www.w3.org/2001/XMLSchema" );
        schemaFactory.setResourceResolver( quickitResolver );
        return quickitResolver;
    }

    private void error( Document dom, Throwable exception )
    {
        Element root = dom.getDocumentElement();
        dom.removeChild( root );
        Element preElement = (Element) dom.appendChild( dom.createElementNS( Page.XHTML, "html" ) )
            .appendChild( dom.createElementNS( Page.XHTML, "body" ) )
            .appendChild( dom.createElementNS( Page.XHTML, "code" ) )
            .appendChild( dom.createElementNS( Page.XHTML, "pre" ) );

        StringWriter stringWriter = new StringWriter( 2000 );
        PrintWriter writer = new PrintWriter( stringWriter );
        exception.printStackTrace( writer );
        writer.close();
        String content = stringWriter.getBuffer().toString();
        preElement.setTextContent( content );
    }

    static class Context
        implements QuikitContext
    {
        private final Page page;
        private String methodName;
        private Element element;
        private Element parentElement;
        private final Document dom;
        private final String path;
        private final HttpServletRequest request;
        private byte[] data = null;

        public Context( Document dom, Page page, String path, HttpServletRequest httpServletRequest )
        {
            this.dom = dom;
            this.page = page;
            this.path = path;
            this.request = httpServletRequest;
        }

        public Page page()
        {
            return page;
        }

        public String methodName()
        {
            return methodName;
        }

        public Document dom()
        {
            return dom;
        }

        public Element element()
        {
            return element;
        }

        public Element parentElement()
        {
            return parentElement;
        }

        public String path()
        {
            return path;
        }

        public String queryString()
        {
            return request.getQueryString();
        }

        public String getHeader( String headerKey )
        {
            return request.getHeader( headerKey );
        }

        public byte[] data()
            throws RenderException
        {
            if( data == null )
            {
                try
                {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    InputStream in = request.getInputStream();
                    int i;
                    while( ( i = in.read() ) != -1 )
                    {
                        baos.write( i );
                    }
                    data = baos.toByteArray();
                }
                catch( IOException e )
                {
                    throw new RenderException( "I/O problems.", e );
                }
            }
            return data;
        }

        public void setDynamic( String method, Element element, Element parent )
        {
            this.methodName = method;
            this.element = element;
            this.parentElement = parent;
        }
    }
}
