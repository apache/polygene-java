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
package org.apache.zest.bootstrap;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import org.apache.zest.functional.Iterables;

import static org.apache.zest.functional.Iterables.filter;
import static org.apache.zest.functional.Iterables.flatten;
import static org.apache.zest.functional.Iterables.flattenIterables;
import static org.apache.zest.functional.Iterables.iterable;
import static org.apache.zest.functional.Iterables.map;

/**
 * Scan classpath for classes that matches given criteria. Useful for automated assemblies with lots of similar classes.
 */
public class ClassScanner
{
    /**
     * Get all classes from the same package of the given class, and recursively in all subpackages.
     * <p>
     * This only works if the seed class is loaded from a file: URL. Jar files are possible as well. Abstract classes
     * are not included in the results. For further filtering use e.g. Iterables.filter.
     * </p>
     * @param seedClass starting point for classpath scanning
     *
     * @return iterable of all concrete classes in the same package as the seedclass, and also all classes in subpackages.
     */
    public static Iterable<Class<?>> findClasses( final Class<?> seedClass )
    {
        CodeSource codeSource = seedClass.getProtectionDomain().getCodeSource();
        if( codeSource == null )
        {
            return Iterables.empty();
        }

        URL location = codeSource.getLocation();

        if( !location.getProtocol().equals( "file" ) )
        {
            throw new IllegalArgumentException( "Can only enumerate classes from file system locations. URL is:" + location );
        }

        final File file;
        try
        {
            file = new File( location.toURI().getPath() );
        }
        catch( URISyntaxException e )
        {
            throw new IllegalArgumentException( "The file location of codebase is invalid. Can not convert to URI. URL is:" + location );
        }

        if( file.getName().endsWith( ".jar" ) )
        {
            try
            {
                final String packageName = seedClass.getPackage().getName().replace( '.', '/' );

                JarFile jarFile = new JarFile( file );
                Iterable<JarEntry> entries = Iterables.iterable( jarFile.entries() );
                try
                {
                    return Iterables.toList( filter( new ValidClass(),
                                                     map( new Function<JarEntry, Class<?>>()
                                                     {
                                                         @Override
                                                         public Class apply( JarEntry jarEntry )
                                                         {
                                                             String name = jarEntry.getName();
                                                             name = name.substring( 0, name.length() - 6 );
                                                             name = name.replace( '/', '.' );
                                                             try
                                                             {
                                                                 return seedClass.getClassLoader().loadClass( name );
                                                             }
                                                             catch( ClassNotFoundException e )
                                                             {
                                                                 return null;
                                                             }
                                                         }
                                                     }
                                                         , filter( new Predicate<JarEntry>()
                                                     {
                                                         @Override
                                                         public boolean test( JarEntry jarEntry )
                                                         {
                                                             return jarEntry.getName()
                                                                        .startsWith( packageName ) && jarEntry.getName()
                                                                 .endsWith( ".class" );
                                                         }
                                                     }, entries ) ) ) );
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
            Iterable<File> files = findFiles( path, new Predicate<File>()
            {
                @Override
                public boolean test( File file )
                {
                    return file.getName().endsWith( ".class" );
                }
            } );

            return filter( new ValidClass(),
                           map( new Function<File, Class<?>>()
                           {
                               @Override
                               public Class<?> apply( File f )
                               {
                                   String fileName = f.getAbsolutePath().substring( file.toString().length() + 1 );
                                   fileName = fileName.replace( File.separatorChar, '.' )
                                       .substring( 0, fileName.length() - 6 );
                                   try
                                   {
                                       return seedClass.getClassLoader().loadClass( fileName );
                                   }
                                   catch( ClassNotFoundException e )
                                   {
                                       return null;
                                   }
                               }
                           }, files ) );
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

        return new Predicate<Class<?>>()
        {
            @Override
            public boolean test( Class<?> aClass )
            {
                return pattern.matcher( aClass.getName() ).matches();
            }
        };
    }

    private static Iterable<File> findFiles( File directory, final Predicate<File> filter )
    {
        return flatten( filter( filter, iterable( directory.listFiles() ) ),
                        flattenIterables( map( new Function<File, Iterable<File>>()
                        {
                            @Override
                            public Iterable<File> apply( File file )
                            {
                                return findFiles( file, filter );
                            }
                        }, filter( new Predicate<File>()
                        {
                            @Override
                            public boolean test( File file )
                            {
                                return file.isDirectory();
                            }
                        }, iterable( directory.listFiles() ) ) ) ) );
    }

    private static class ValidClass
        implements Predicate<Class<?>>
    {
        @Override
        public boolean test( Class<?> item )
        {
            return ( item.isInterface() || !Modifier.isAbstract( item.getModifiers() ) ) && ( !item.isEnum() && !item.isAnonymousClass() );
        }
    }
}
