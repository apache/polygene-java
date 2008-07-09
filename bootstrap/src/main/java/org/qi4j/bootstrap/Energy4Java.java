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

package org.qi4j.bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.qi4j.structure.Application;

public class Energy4Java
{
    private static final Logger LOG = Logger.getAnonymousLogger();

    private ApplicationFactory factory;

    public Energy4Java()
    {
        this( findQi4jRuntime() );
    }

    public Energy4Java( ApplicationFactory factory )
    {
        this.factory = factory;
    }

    public Application newApplication( Assembler assembler )
        throws AssemblyException
    {
        return factory.newApplication( assembler );
    }

    public Application newApplication( Assembler[][][] assemblers )
        throws AssemblyException
    {
        return factory.newApplication( assemblers );
    }

    public Application newApplication( ApplicationAssembly applicationAssembly )
        throws AssemblyException
    {
        return factory.newApplication( applicationAssembly );
    }

    private static ApplicationFactory findQi4jRuntime()
        throws BootstrapException
    {
        Level oldLevel = LOG.getLevel();
        LOG.setLevel( Level.FINEST );
        try
        {
            ServiceLoader loader = new ServiceLoader();
            ClassLoader classloader = Energy4Java.class.getClassLoader();
            Iterator<? extends ApplicationFactory> providers = loader.providers( ApplicationFactory.class, classloader );
            if( providers.hasNext() )
            {
                return providers.next();
            }
        }
        catch( IOException e )
        {
            throw new BootstrapException( "Unable to load a ApplicationFactory provider.", e );
        }
        finally
        {
            LOG.setLevel( oldLevel );
        }
        throw new BootstrapException( "No Application Factory providers found." );
    }

    private static class ServiceLoader
    {

        public Iterator<? extends ApplicationFactory> providers( Class clazz, ClassLoader ldr )
            throws IOException
        {
            LOG.fine( "searching providers for " + clazz );
            HashMap<Class<? extends ApplicationFactory>, ApplicationFactory> result =
                new HashMap<Class<? extends ApplicationFactory>, ApplicationFactory>();

            Enumeration cfg = ldr.getResources( "META-INF/services/" + clazz.getName() );
            while( cfg.hasMoreElements() )
            {
                URL rc = (URL) cfg.nextElement();
                processResource( ldr, result, rc );
            }
            return result.values().iterator();
        }

        private void processResource(
            ClassLoader classLoader, HashMap<Class<? extends ApplicationFactory>, ApplicationFactory> result,
            URL rc )
            throws IOException
        {
            InputStream in = null;
            BufferedReader rd = null;
            try
            {
                in = rc.openStream();
                rd = new BufferedReader( new InputStreamReader( in, "UTF-8" ) );
                LOG.fine( "found provider file " + rc );

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

        private void processProvider( HashMap<Class<? extends ApplicationFactory>, ApplicationFactory> result,
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
            if( result.containsKey( provider ) )
            {
                LOG.severe( "provider exists already: " + provider.getClass() );
            }
            else
            {
                LOG.fine( "found provider: " + provider.getClass() );
                result.put( provider, instance );
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
                LOG.warning( "Class " + line + " was not of " + ApplicationFactory.class.getName() + " subtype." );
            }
            catch( ClassNotFoundException ex )
            {
                LOG.warning( "Class " + line + " was not found. Skipping." );
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
                LOG.warning( "Class " + provider.getName() + " is an interface or abstract class." );
            }
            catch( IllegalAccessException ex )
            {
                LOG.warning( "Class " + provider.getName() + " is not accessible. Make sure it is public and have a public no-args constructor." );
            }
            return null;
        }

    }
}
