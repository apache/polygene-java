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
package org.apache.polygene.bootstrap;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Scan classpath for classes that matches given criteria. Useful for automated assemblies with lots of similar classes.
 */
public class ClassScanner
{
    private static final ValidClass VALID_CLASS_PREDICATE = new ValidClass();

    /**
     * Get all classes from the same package of the given class, and recursively in all subpackages.
     * <p>
     * This only works if the seed class is loaded from a file: URL. Jar files are possible as well. Abstract classes
     * are not included in the results. For further filtering use e.g. Stream.filter.
     * </p>
     * @param seedClass starting point for classpath scanning
     *
     * @return Stream of all concrete classes in the same package as the seedclass, and also all classes in subpackages.
     */
    public static Stream<? extends Class<?>> findClasses( final Class<?> seedClass )
    {
        CodeSource codeSource = seedClass.getProtectionDomain().getCodeSource();
        if( codeSource == null )
        {
            return Stream.of();
        }

        URL location = codeSource.getLocation();

        if( !location.getProtocol().equals( "file" ) )
        {
            throw new IllegalArgumentException(
                "Can only enumerate classes from file system locations. URL is:" + location );
        }

        final File file;
        try
        {
            file = new File( location.toURI().getPath() );
        }
        catch( URISyntaxException e )
        {
            throw new IllegalArgumentException(
                "The file location of codebase is invalid. Can not convert to URI. URL is:" + location );
        }

        if( file.getName().endsWith( ".jar" ) )
        {
            try
            {
                final String packageName = seedClass.getPackage().getName().replace( '.', '/' );

                JarFile jarFile = new JarFile( file );
                List<JarEntry> entries = Collections.list( jarFile.entries() );
                try
                {
                    return entries.stream()
                                  .filter( jarEntry -> jarEntry.getName().startsWith( packageName )
                                                       && jarEntry.getName().endsWith( ".class" ) )
                                  .map( jarEntry ->
                                        {
                                            String name = jarEntry.getName();
                                            name = name.substring( 0, name.length() - 6 );
                                            name = name.replace( '/', '.' );
                                            try
                                            {
                                                return seedClass.getClassLoader().loadClass( name );
                                            }
                                            catch( ClassNotFoundException | NoClassDefFoundError e )
                                            {
                                                return null;
                                            }
                                        } )
                                  .filter( VALID_CLASS_PREDICATE );
                }
                finally
                {
                    jarFile.close();
                }
            }
            catch( IOException e )
            {
                throw new IllegalArgumentException( "Could not open jar file " + file, e );
            }
        }
        else
        {
            final File path = new File( file, seedClass.getPackage().getName().replace( '.', File.separatorChar ) );
            Stream<File> classFiles = findFiles( path, candidate -> candidate.getName().endsWith( ".class" ) );
            return classFiles
                .map( classFile ->
                      {
                          String fileName = classFile.getAbsolutePath().substring( file.toString().length() + 1 );
                          fileName = fileName.replace( File.separatorChar, '.' ).substring( 0, fileName.length() - 6 );
                          try
                          {
                              return seedClass.getClassLoader().loadClass( fileName );
                          }
                          catch( ClassNotFoundException e )
                          {
                              return null;
                          }
                      } )
                .filter( VALID_CLASS_PREDICATE );
        }
    }

    /**
     * Useful specification for filtering classes based on a regular expression matching the class names.
     * <p>
     * Example: matches(".*Model") -&gt; match only class names that end with Model
     * </p>
     *
     * @param regex The regular expression to be matched.
     *
     * @return regex class name specification
     */
    public static Predicate<Class<?>> matches( String regex )
    {
        final Pattern pattern = Pattern.compile( regex );
        return aClass -> pattern.matcher( aClass.getName() ).matches();
    }

    private static Stream<File> findFiles( File directory, final Predicate<File> filter )
    {
        File[] listedFiles = directory.listFiles();
        if( listedFiles == null )
        {
            return Stream.of();
        }
        return Stream.concat( Stream.of( listedFiles ).filter( filter ),
                              Stream.of( listedFiles )
                                    .filter( File::isDirectory )
                                    .map( dir -> findFiles( dir, filter ) )
                                    .flatMap( Function.identity() ) );
    }

    private static class ValidClass
        implements Predicate<Class<?>>
    {
        @Override
        public boolean test( Class<?> item )
        {
            return item != null && ( item.isInterface() || !Modifier.isAbstract( item.getModifiers() ) )
                   && ( !item.isEnum() && !item.isAnonymousClass() );
        }
    }
}
