/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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

package org.qi4j.bootstrap.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;

/**
 * Implementation of ServiceLoader mechanism in Java.
 */
public final class ServiceLoader
{
    private static LinkedList<ClassLoader> loaders;

    static
    {
        loaders = new LinkedList<ClassLoader>();
        addClassloader( ServiceLoader.class.getClassLoader() );
    }

    public static void addClassloader( ClassLoader loader )
    {
        loaders.add( loader );
    }

    public static void removeClassloader( ClassLoader loader )
    {
        loaders.remove( loader );
    }

    public <T> Iterable<T> providers(Class<T> providerClass)
        throws IOException
    {
        LinkedList<T> result = new LinkedList<T>();
        for( ClassLoader loader : loaders )
        {
            Enumeration cfg = loader.getResources( "META-INF/services/" + providerClass.getName() );
            while( cfg.hasMoreElements() )
            {
                URL rc = (URL) cfg.nextElement();
                processResource( loader, result, rc );
            }
        }
        return result;
    }

    private <T> void processResource(
        ClassLoader classLoader, LinkedList<T> result,
        URL rc )
        throws IOException
    {
        InputStream in = null;
        BufferedReader rd = null;
        try
        {
            in = rc.openStream();
            rd = new BufferedReader( new InputStreamReader( in, "UTF-8" ) );

            String line = rd.readLine();
            while( null != line )
            {
                String providerClassName = trimLine( line );
                processProvider( result, classLoader, providerClassName );
                line = rd.readLine();
            }
        }
        finally
        {
            if( rd != null )
            {
                rd.close();
            }
            if( in != null )
            {
                in.close();
            }
        }
    }

    private String trimLine( String line )
    {
        int hashPos = line.indexOf( '#' );
        if( hashPos >= 0 )
        {
            line = line.substring( 0, hashPos );
        }
        line = line.trim();
        return line;
    }

    private <T> void processProvider( LinkedList<T> result,
                                  ClassLoader classLoader, String providerClassName )
        throws IOException
    {
        Class<T> provider = loadProvider( classLoader, providerClassName );
        if( provider == null )
        {
            return;
        }
        T instance = instantiateProvider( provider );
        if( instance == null )
        {
            return;
        }
        if( !result.contains( instance ) )
        {
            result.add( instance );
        }

    }

    private <T> Class<T> loadProvider( ClassLoader ldr, String line )
    {
        try
        {
            return (Class<T>) ldr.loadClass( line );
        }
        catch( ClassCastException ex )
        {
            System.err.println( "Class " + line + " was not of " + ApplicationAssemblyFactory.class.getName() + " subtype." );
        }
        catch( ClassNotFoundException ex )
        {
            System.err.println( "Class " + line + " was not found. Skipping." );
        }
        return null;
    }

    private <T> T instantiateProvider( Class<T> provider )
        throws IOException
    {
        try
        {
            return provider.newInstance();
        }
        catch( InstantiationException ex )
        {
            System.err.println( "Class " + provider.getName() + " is an interface or abstract class." );
        }
        catch( IllegalAccessException ex )
        {
            System.err.println( "Class " + provider.getName() + " is not accessible. Make sure it is public and have a public no-args constructor." );
        }
        return null;
    }

}
