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
