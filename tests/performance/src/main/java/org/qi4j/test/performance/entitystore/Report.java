/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.test.performance.entitystore;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class Report
{
    private final HashMap<String, Long> results;
    private String current;
    private long start;
    private final String name;

    public Report( String name )
    {
        results = new HashMap<>();
        this.name = name;
    }

    public void start( String type )
    {
        current = type;
        start = System.nanoTime();
    }

    public void stop( int iterations )
    {
        long end = System.nanoTime();
        long duration = end - start;
        results.put( current, ( 1000000000L * iterations ) / duration );
    }

    public long intermediate( int iterations )
    {
        long end = System.nanoTime();
        long duration = end - start;
        return ( 1000000000L * iterations ) / duration;
    }

    public long duration( String type )
    {
        return results.get( type );
    }

    public String name()
    {
        return name;
    }

    public String toXML()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append( "  <test unit=\"per sec\">\n" );
        for( Map.Entry entry : results.entrySet() )
        {
            buffer.append( "    <result id=\"" );
            buffer.append( entry.getKey() );
            buffer.append( "\">" );
            buffer.append( entry.getValue() );
            buffer.append( "</result>\n" );
        }
        buffer.append( "  </test>\n" );
        return buffer.toString();
    }

    public void writeTo( Writer writer )
        throws IOException
    {
        BufferedWriter out;
        if( writer instanceof BufferedWriter )
        {
            out = (BufferedWriter) writer;
        }
        else
        {
            out = new BufferedWriter( writer );
        }
        out.write( toXML() );
        out.flush();
    }
}
