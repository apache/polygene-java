/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.tools.shell;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FileUtils
{
    public static void createDir( String directoryName )
    {
        File dir = new File( directoryName ).getAbsoluteFile();
        if( !dir.mkdirs() )
        {
            System.err.println( "Unable to create directory " + dir );
            System.exit( 1 );
        }
    }

    public static Map<String, String> readPropertiesResource( String resourceName )
    {
        ClassLoader cl = FileUtils.class.getClassLoader();
        InputStream in = cl.getResourceAsStream( resourceName );
        try
        {
            Properties properties = readProperties( in );
            Map<String, String> result = new HashMap<String, String>();
            for( Map.Entry prop : properties.entrySet() )
            {
                result.put( prop.getKey().toString(), prop.getValue().toString() );
            }
            return result;
        }
        catch( IOException e )
        {
            System.err.println( "Unable to read resource " + resourceName );
            System.exit( 2 );
            return null;
        }
    }

    private static Properties readProperties( InputStream in )
        throws IOException
    {
        Properties p = new Properties();
        p.load( in );
        return p;
    }
}
