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
package org.qi4j.tools.codesnippet.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import junit.framework.JUnit4TestAdapter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import static org.junit.Assert.*;

/**
 * Very, very basic test for the {@link IndentStripFilter}. Could be more elaborate (XMLUnit???).
 */
public class IndentStripFilterTest
{
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter( IndentStripFilterTest.class );
    }

    private static SAXTransformerFactory transformerFactory;

    @BeforeClass
    public static void setUp()
        throws TransformerFactoryConfigurationError,
               ServletException
    {
        transformerFactory = createTransformerFactory();
    }

    @Test
    public void testStripIndent()
        throws SAXException,
               ParserConfigurationException,
               IOException,
               TransformerException
    {
        XMLReader input = createReader();
        IndentStripFilter filter = new IndentStripFilter( "sample", input );
        InputSource inputSource = getInputSource( "IndentStripFilterTest.testStripIndent.in.xml" );
        Transformer tf = transformerFactory.newTransformer( getSource( "IndentStripFilterTest.identity.xsl" ) );
        StringWriter actual = new StringWriter();

        Result outputTarget = new StreamResult( actual );

        tf.transform( new SAXSource( filter, inputSource ), outputTarget );

        String expected = getResourceAsString( "IndentStripFilterTest.testStripIndent.out.xml" );

        assertEquals( trimLineFeeds( expected ), trimLineFeeds( actual.toString() ) );
    }

    private String trimLineFeeds( String data )
    {
        StringBuffer buf = new StringBuffer();
        for( int i = 0; i < data.length(); i++ )
        {
            char ch = data.charAt( i );
            if( ch != 10 && ch != 13 )
            {
                buf.append( ch );
            }
        }
        return buf.toString();
    }

    @Test
    public void testDontStripIndent()
        throws SAXException,
               ParserConfigurationException,
               IOException,
               TransformerException
    {
        XMLReader input = createReader();
        IndentStripFilter filter = new IndentStripFilter( "anyStringButNotSample", input );
        InputSource inputSource = getInputSource( "IndentStripFilterTest.testStripIndent.in.xml" );
        Transformer tf = transformerFactory.newTransformer( getSource( "IndentStripFilterTest.identity.xsl" ) );
        StringWriter actual = new StringWriter();

        Result outputTarget = new StreamResult( actual );

        tf.transform( new SAXSource( filter, inputSource ), outputTarget );

        // -- no stripping should have been done, so we use the input for comparison
        String expected = getResourceAsString( "IndentStripFilterTest.testStripIndent.in.xml" );

        assertEquals( trimLineFeeds( expected ), trimLineFeeds( actual.toString() ) );
    }

    private String getResourceAsString( String resource )
        throws IOException
    {
        Reader in = new BufferedReader( new InputStreamReader( getResourceAsStream( resource ), "UTF-8" ) );
        try
        {
            StringWriter result = new StringWriter();

            for( int cc = in.read(); -1 != cc; cc = in.read() )
            {
                result.write( cc );
            }

            return result.toString();
        }
        finally
        {
            in.close();
        }
    }

    /**
     * Test filter manually with small chunks of character data, because this cannot (easily) be achieved with a parser
     * on small documents.
     *
     * @throws SAXException
     */
    @Test
    public void testMultipleCharacterEvents()
        throws SAXException
    {
        final String I_0 = "";
        final String I_1 = " ";
        final String I_2 = "  ";
        final String I_3 = "   ";

        final String[][] testValues =
            {
                {
                    "Hello world! \n\nMy name is Foo, Foo Bar.", "Hello world! \n\nMy name is Foo, Foo Bar."
                },
                {
                    "\tA sample with one tab indent", "A sample with one tab indent"
                },
                {
                    I_1 + "A sample with\n" + I_2 + "multiple lines and increasing indent\n" + I_3 + "on each line",
                    I_0 + "A sample with\n" + I_1 + "multiple lines and increasing indent\n" + I_2 + "on each line",
                }
            };

        for( int ii = 0; ii < testValues.length; ii++ )
        {
            String input = testValues[ ii ][ 0 ];
            String expected = testValues[ ii ][ 1 ];

            final String actual = filterStringInput( input );

            assertEquals( expected, actual );
        }
    }

    private String filterStringInput( final String input )
        throws SAXException
    {
        final StringBuffer actual = new StringBuffer();

        IndentStripFilter filter = new IndentStripFilter( "root" );

        ContentHandler handler = new DefaultHandler()
        {
            @Override
            public void characters( char[] ch, int start, int length )
                throws SAXException
            {
                actual.append( ch, start, length );
            }
        };

        filter.setContentHandler( handler );
        char[] cbuf = input.toCharArray();
        filter.startDocument();
        filter.startElement( "", "root", "", new AttributesImpl() );
        for( int ii = 0; ii < cbuf.length; ++ii )
        {
            filter.characters( cbuf, ii, 1 );
        }
        filter.endElement( "", "root", "" );
        filter.endDocument();

        return actual.toString();
    }

    private XMLReader createReader()
        throws SAXException,
               ParserConfigurationException
    {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware( true );
        SAXParser sp = spf.newSAXParser();
        XMLReader rd = sp.getXMLReader();

        return rd;
    }

    private InputSource getInputSource( String resource )
    {
        return new InputSource( getResourceAsStream( resource ) );
    }

    private Source getSource( String resource )
    {
        return new StreamSource( getResourceAsStream( resource ) );
    }

    private InputStream getResourceAsStream( String resource )
    {
        return IndentStripFilter.class.getResourceAsStream( resource );
    }

    private static SAXTransformerFactory createTransformerFactory()
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
}
