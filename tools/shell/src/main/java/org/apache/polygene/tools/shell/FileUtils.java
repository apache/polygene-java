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
package org.apache.polygene.tools.shell;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FileUtils
{
    public static File createDir( String directoryName )
    {
        try
        {
            File dir = new File( directoryName ).getAbsoluteFile();
            Files.createDirectories( dir.toPath() );
            return dir;
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    public static void removeDir( File dir ) throws IOException
    {
        Files.walkFileTree( dir.toPath(), new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile( final Path file, final BasicFileAttributes attrs ) throws IOException
            {
                Files.delete( file );
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory( final Path dir, final IOException exc ) throws IOException
            {
                Files.delete( dir );
                return FileVisitResult.CONTINUE;
            }
        } );
    }

    public static Map<String, String> readTemplateProperties( String templateName )
    {
        File propertiesFile = new File( polygeneHome(), "etc/templates/" + templateName + "/template.properties" );
        try( InputStream in = new BufferedInputStream( new FileInputStream( propertiesFile ) ) )
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
            String message = "Unable to read template \'" + templateName + "\'.";
            System.err.println( message );
            throw new RuntimeException( message );
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
        System.out.println( "Creating " + dest.getAbsolutePath() );
        if( dest.exists() )
        {
            Files.delete( dest.toPath() );
        }
        Files.copy( srcFile.toPath(), dest.toPath() );
    }

    public static File polygeneHome()
    {
        String home = System.getProperty( "polygene.home" );
        return new File( home ).getAbsoluteFile();
    }

    public static PrintWriter createJavaClassPrintWriter( Map<String, String> properties,
                                                          String module,
                                                          String packagename,
                                                          String classname
    )
        throws IOException
    {
        File projectDir = new File( properties.get( "project.dir" ) );
        File packageDir = new File( projectDir, module + "/src/main/java/" + packagename );
        if( !packageDir.exists() )
        {
            if( !packageDir.mkdirs() )
            {
                System.err.println( "Unable to create directory: " + packageDir.getAbsolutePath() );
            }
        }
        final File destination = new File( packageDir, classname + ".java" );
        return new PrintWriter( new FileWriter( destination ) )
        {
            @Override
            public void close()
            {
                super.close();
                System.out.println( "Creating " + destination );
            }
        };
    }
}
