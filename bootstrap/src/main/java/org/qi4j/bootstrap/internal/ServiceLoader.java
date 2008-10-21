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
import java.util.Iterator;
import java.util.LinkedList;
import org.qi4j.bootstrap.ApplicationFactory;

public class ServiceLoader
{
    private static LinkedList<ClassLoader> loaders;

    static
    {
        loaders = new LinkedList<ClassLoader>();
        addClassloader( ServiceLoader.class.getClassLoader() );
    }

    public static void addClassloader( ClassLoader loader )
    {
        Enumeration cfg;
        try
        {
            cfg = loader.getResources( "META-INF/services/" + ApplicationFactory.class.getName() );
            if( cfg.hasMoreElements() )
            {
                loaders.add( loader );
            }
        }
        catch( IOException e )
        {
            // ignore, nothing we can do.
        }
    }

    public static void removeClassloader( ClassLoader loader )
    {
        loaders.remove( loader );
    }

    public Iterator<? extends ApplicationFactory> providers()
        throws IOException
    {
        LinkedList<ApplicationFactory> result = new LinkedList<ApplicationFactory>();
        for( ClassLoader loader : loaders )
        {
            Enumeration cfg = loader.getResources( "META-INF/services/" + ApplicationFactory.class.getName() );
            while( cfg.hasMoreElements() )
            {
                URL rc = (URL) cfg.nextElement();
                processResource( loader, result, rc );
            }
        }
        return result.iterator();
    }

    private void processResource(
        ClassLoader classLoader, LinkedList<ApplicationFactory> result,
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

    private void processProvider( LinkedList<ApplicationFactory> result,
                                  ClassLoader classLoader, String providerClassName )
        throws IOException
    {
        Class<? extends ApplicationFactory> provider = loadProvider( classLoader, providerClassName );
        if( provider == null )
        {
            return;
        }
        ApplicationFactory instance = instantiateProvider( provider );
        if( instance == null )
        {
            return;
        }
        if( !result.contains( provider ) )
        {
            result.add( instance );
        }

    }

    private Class<? extends ApplicationFactory> loadProvider( ClassLoader ldr, String line )
    {
        try
        {
            return (Class<? extends ApplicationFactory>) ldr.loadClass( line );
        }
        catch( ClassCastException ex )
        {
            System.err.println( "Class " + line + " was not of " + ApplicationFactory.class.getName() + " subtype." );
        }
        catch( ClassNotFoundException ex )
        {
            System.err.println( "Class " + line + " was not found. Skipping." );
        }
        return null;
    }

    private ApplicationFactory instantiateProvider( Class<? extends ApplicationFactory> provider )
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
