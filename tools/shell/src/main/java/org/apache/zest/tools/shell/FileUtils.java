/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.tools.shell;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FileUtils
{
    public static File createDir( String directoryName )
    {
        File dir = new File( directoryName ).getAbsoluteFile();
        if( !dir.mkdirs() )
        {
            System.err.println( "Unable to create directory " + dir );
//            System.exit( 1 );   during testing, I am tired of deleting directory over and over again.
        }
        return dir;
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
        catch( Exception e )
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

    public static void copyFile( File srcFile, File dest )
        throws IOException
    {
        byte[] buffer = new byte[100000];
        try(BufferedInputStream in = new BufferedInputStream( new FileInputStream(srcFile) )){
            try(BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( dest ) )){
                int bytes;
                while( (bytes = in.read( buffer )) != -1 ){
                    out.write( buffer, 0, bytes );
                }
                out.flush();
            }
        }
    }
}
