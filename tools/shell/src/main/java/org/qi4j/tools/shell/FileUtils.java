package org.qi4j.tools.shell;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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

    public static void createDir( File dir )
    {
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

    public static void writeFile( File file, String data )
        throws IOException
    {
        try (BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file ), "UTF-8" ) ))
        {
            writer.write( data );
        }
    }

    public static Map<String, String> readPropertiesFile( File file )
    {
        try (InputStream in = new BufferedInputStream( new FileInputStream( file ) ))
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
            System.err.println( "Unable to read file " + file.getAbsolutePath() );
            System.exit( 2 );
            return null;
        }
    }

    public static String readFile( File file )
        throws IOException
    {
        try (BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( file ), "UTF-8" ) ))
        {
            StringBuilder builder = new StringBuilder();
            String line = reader.readLine();
            while( line != null )
            {
                builder.append( line );
                builder.append( '\n' );
                line = reader.readLine();
            }
            return builder.toString();
        }
    }
}
