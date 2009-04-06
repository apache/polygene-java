/*
 * Copyright 2009 Georg Ragaller. All rights Reserved.
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;
import static org.junit.Assert.*;

public class JavaXmlCommentsReaderTest
{
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter( JavaXmlCommentsReaderTest.class );
    }

    @Test
    public void testSourceWithoutXmlComments() throws IOException
    {
        String actual = readFully(newJavaXmlCommentsReader( "SourceWithoutXmlComments.java.txt" ));
        String expected = readFully(newInputStreamReader( "SourceWithoutXmlComments.java.out.txt"));
        
        assertEquals( expected, actual );
    }

    @Test
    public void testSourceWithXmlComments() throws IOException
    {
        String actual = readFully(newJavaXmlCommentsReader( "SourceWithXmlComments.java.txt" ));
        String expected = readFully(newInputStreamReader( "SourceWithXmlComments.java.out.txt"));
        
        assertEquals( expected, actual );
    }
    
    @Test
    public void testCDATAEscape() throws IOException
    {
        String actual = readFully(newJavaXmlCommentsReader( "CDATAEscape.java.txt" ));
        String expected = readFully(newInputStreamReader( "CDATAEscape.java.out.txt"));
        
        assertEquals( expected, actual );
    }
    @Test
    public void main() throws UnsupportedEncodingException, IOException
    {
        String actual = readFully(newJavaXmlCommentsReader( "CDATAEscape.java.txt" ));
        
        FileWriter out = new FileWriter("CDATAEscape.java.out.txt");
        out.append( actual );
        out.close();
    }
    
    private InputStreamReader newInputStreamReader( String resource )
    {
        return new InputStreamReader(getResourceAsStream( resource ));
    }

    private String readFully( Reader rd ) throws IOException
    {
        BufferedReader buf = new BufferedReader(rd);
        StringWriter result = new StringWriter();

        for( int cc = buf.read(); -1 != cc; cc = buf.read() )
        {
            result.write( cc );
        }

        return result.toString();
     }

    private JavaXmlCommentsReader newJavaXmlCommentsReader( String resource )
        throws UnsupportedEncodingException
    {
        return new JavaXmlCommentsReader(new InputStreamReader(getResourceAsStream( resource ),"UTF-8"));
    }
    
    private InputStream getResourceAsStream( String resource )
    {
        InputStream result = JavaXmlCommentsReaderTest.class.getResourceAsStream( resource );
        
        if (null == result)
        {
            throw new IllegalArgumentException("missing resource: " + resource);
        }
        return result;
    }

}
