package org.qi4j.bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

public interface ServiceLoader
{
    <T> T findFirstService( Class<T> neededType )
        throws IOException;

    <T> Iterable<T> findAllServices( Class<T> neededType )
        throws IOException;

    /**
     * The default implementation of ServiceLoader mechanism uses the Service Mechanism from Java2, which
     * looks for text files inside the META-INF/services/ directory in all visible jars on the classpath,
     * i.e. {@code classloader.getResources( "/META-INF/services/" + type.getName() ) }, where type is the
     * Class of requested type. The text file is expected to comprise of a single line, with the fully qualified
     * class name of the implementation, which should reside in the same jar file (but not required).
     */
    public final class StandaloneApplicationServiceLoader
        implements ServiceLoader
    {

        public <T> Iterable<T> findAllServices( Class<T> neededType )
            throws IOException
        {
            LinkedList<T> result = new LinkedList<T>();

            ClassLoader loader = getClass().getClassLoader();
            Enumeration cfg = loader.getResources( "META-INF/services/" + neededType.getName() );
            while( cfg.hasMoreElements() )
            {
                URL rc = (URL) cfg.nextElement();
                processResource( loader, result, rc, neededType );
            }
            return result;
        }

        public <T> T findFirstService( Class<T> neededType )
            throws IOException
        {
            final Iterator<T> allProviders = findAllServices( neededType ).iterator();
            if( allProviders.hasNext() )
            {
                return allProviders.next();
            }
            System.err.println( "No provider found for " + neededType + "." );
            return null;
        }

        private <T> void processResource(
            ClassLoader classLoader, LinkedList<T> result,
            URL rc, Class<T> neededType
        )
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
                    if( line.length() > 0 )
                    {
                        processProvider( result, classLoader, providerClassName, neededType );
                    }
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
                                          ClassLoader classLoader,
                                          String providerClassName,
                                          Class<T> neededType
        )
        {
            Class<T> provider = loadProvider( classLoader, providerClassName, neededType );
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

        private <T> Class<T> loadProvider( ClassLoader ldr, String providerClassName, Class<T> neededType )
        {
            try
            {
                final Class<?> providerClass = ldr.loadClass( providerClassName );
                return (Class<T>) providerClass.asSubclass( neededType );
            }
            catch( ClassCastException ex )
            {
                System.err
                    .println( "Class " + providerClassName + " was not of " + neededType.getName() + " subtype." );
            }
            catch( ClassNotFoundException ex )
            {
                System.err.println( "Class " + providerClassName + " was not found. Skipping." );
            }
            return null;
        }

        private <T> T instantiateProvider( Class<T> provider )
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
                System.err
                    .println(
                        "Class " + provider.getName() + " is not accessible. Make sure it is public and have a public no-args constructor." );
            }
            return null;
        }
    }
}
