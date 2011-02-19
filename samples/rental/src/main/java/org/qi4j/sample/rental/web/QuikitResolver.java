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

import com.sun.org.apache.xerces.internal.impl.xs.XSImplementationImpl;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class QuikitResolver
    implements LSResourceResolver, EntityResolver
{
    private Properties local;

    public QuikitResolver()
    {
        local = new Properties();
        try
        {
            InputStream stream = getClass().getClassLoader().getResourceAsStream( "resolve.properties" );
            BufferedInputStream in = new BufferedInputStream( stream );
            local.load( in );
        }
        catch( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }

    public InputSource resolveEntity( String publicId, String systemId )
        throws SAXException, IOException
    {
        String resourceName = local.getProperty( publicId );
        if( resourceName == null )
        {
            System.out.println( "Not locally available: " + publicId + "  " + systemId );
            return null;
        }
        InputStream in = getClass().getClassLoader().getResourceAsStream( resourceName );
        return new InputSource( in );
    }

    public LSInput resolveResource( String type, String namespaceURI, String publicId, String systemId, String baseURI )
    {
        String resourceName = local.getProperty( systemId );
        if( resourceName == null )
        {
            System.out.println( "type: " + type );
            System.out.println( "namespaceURI: " + namespaceURI );
            System.out.println( "publicId: " + publicId );
            System.out.println( "systemId: " + systemId );
            System.out.println( "baseURI: " + baseURI );
            return null;
        }

        InputStream resource = getClass().getClassLoader().getResourceAsStream( resourceName );
        LSInput input = ( (DOMImplementationLS) XSImplementationImpl.getDOMImplementation() ).createLSInput();
        input.setBaseURI( baseURI );
        input.setByteStream( resource );
        input.setPublicId( publicId );
        input.setSystemId( systemId );
        return input;
    }
}
