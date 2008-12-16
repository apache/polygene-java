/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.library.jini.javaspaces;

import org.qi4j.api.service.Activatable;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.library.http.HttpService;
import org.qi4j.library.http.Interface;
import org.qi4j.api.util.StreamUtils;
import com.sun.jini.start.NonActivatableServiceDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.security.Policy;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.ArrayList;
import net.jini.config.Configuration;
import net.jini.config.EmptyConfiguration;

public class OutriggerMixin
    implements Activatable
{
    @Service( optional = true ) HttpService httpService;
    @This org.qi4j.api.service.Configuration<OutriggerConfiguration> my;

    private NonActivatableServiceDescriptor.Created lookupCreated;
    private File fileToCleanup;

    public void activate()
        throws Exception
    {
        // Prepare a temporary directory.
        File qi4j = new File( "qi4jtemp" );
        qi4j.mkdir();
        fileToCleanup = qi4j.getAbsoluteFile();
        File jini = new File( qi4j, "jini" );
        jini.mkdirs();
        File temp = File.createTempFile( "outrigger", "", jini );
        temp.delete();
        temp.mkdirs();
        // Start Outrigger
        Configuration serviceDescConfig = EmptyConfiguration.INSTANCE;
        String httpURLtext = my.configuration().outriggerDlJarLocation().get();
        if( httpURLtext == null )
        {
            // If Http Service not running in this JVM, we need the Download URL from configuration.
            if( httpService != null )
            {
                httpURLtext = constructDownloadURL();
            }
        }
        String outriggerJarLocation = createOutriggerJars( temp );
        File outriggerJarFile = new File( outriggerJarLocation );
        String securityPolicyLocation = createSecurityPolicy( temp, outriggerJarFile );
        String outriggerConfig = createOutriggerConfig( temp );
        System.out.println( "         Http: " + httpURLtext );
        System.out.println( "     Security: " + Policy.getPolicy() );
        System.out.println( " Jar Location: " + outriggerJarLocation );
        System.out.println( "Configuration: " + outriggerConfig );
        NonActivatableServiceDescriptor serviceDescriptor = new NonActivatableServiceDescriptor(
            httpURLtext,
            securityPolicyLocation,
            outriggerJarLocation,
            "com.sun.jini.outrigger.TransientOutriggerImpl",
            new String[]{ outriggerConfig } );
        lookupCreated = (NonActivatableServiceDescriptor.Created) serviceDescriptor.create( serviceDescConfig );

    }

    public void passivate() throws Exception
    {
        // How to shutdown properly???
        removeFile( fileToCleanup );
    }

    private String createOutriggerConfig( File dir )
        throws IOException
    {
        StringBuffer configuration = new StringBuffer();
        if( my.configuration().useJrmp().get() )
        {
            System.out.println( "Using JRMP..." );
            configuration.append( "import net.jini.jrmp.JrmpExporter;\n" +
                          "import net.jini.core.discovery.LookupLocator;\n" +
                          "\n" +
                          "com.sun.jini.outrigger\n" +
                          "{\n" +
                          "    serverExporter = new JrmpExporter();\n" );
        }
        else
        {
            System.out.println( "Using JERI..." );
            configuration.append( "import net.jini.jeri.BasicILFactory;\n" +
                            "import net.jini.jeri.BasicJeriExporter;\n" +
                            "import net.jini.jeri.tcp.TcpServerEndpoint;\n" +
                            "import net.jini.core.discovery.LookupLocator;\n" +
                            "com.sun.jini.outrigger\n" +
                            "{" +
                            "    private invocationLayerFactory = new BasicILFactory();\n" +
                            "    serverExporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0),\n" +
                            "                                           invocationLayerFactory,\n" +
                            "                                           false,\n" +
                            "                                           true);\n" );
        }
        configuration.append( "    initialLookupGroups = new String[] {" );
        String groupData = my.configuration().groups().get();
        String[] groups;
        if( groupData != null )
        {
            groups = convert( groupData );
        }
        else
        {
            groups = new String[] { "qi4j" };
        }
        boolean first = true;
        for( String group : groups )
        {
            if( !first )
            {
                configuration.append( ", " );
            }
            first = false;
            configuration.append( "\"" );
            configuration.append( group );
            configuration.append( "\"" );
        }

        configuration.append( "};\n}" );

        File outriggerConfigFile = new File( dir, "outrigger.conf" );
        if( !outriggerConfigFile.exists() )
        {
            InputStream outriggerConfig = new ByteArrayInputStream( configuration.toString().getBytes() );
            copyStreamToFile( outriggerConfig, outriggerConfigFile );
        }
        return outriggerConfigFile.getAbsolutePath();
    }

    private String[] convert( String data )
    {
        if( data == null )
        {
            return new String[0];
        }
        ArrayList<String> result = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer( data, ",", false );
        while( st.hasMoreTokens() )
        {
            String token = st.nextToken();
            result.add( token );
        }
        String[] retVal = new String[result.size() ];
        return result.toArray( retVal );
    }

    private String createOutriggerJars( File temp )
        throws IOException
    {
        File outriggerJar = new File( temp, "outrigger-2.1.1.jar" );
        if( !outriggerJar.exists() )
        {
            InputStream outrigger = getClass().getResourceAsStream( "outrigger-2.1.1.jar" );
            copyStreamToFile( outrigger, outriggerJar );
        }
        return outriggerJar.getAbsolutePath();
    }

    private void removeFile( File fileToCleanup )
    {
        if( fileToCleanup == null )
        {
            return;
        }

        File[] files = fileToCleanup.listFiles();
        if( files == null )
        {
            return;
        }
        for( File file : files )
        {
            removeFile( file );
        }
        fileToCleanup.delete();
    }

    private String constructDownloadURL()
        throws UnknownHostException
    {
        Interface[] interfaces = httpService.interfacesServed();
        String host = interfaces[ 0 ].hostName();
        int port = interfaces[ 0 ].port();
        String protocol = interfaces[ 0 ].protocol().toString();
        return protocol + "://" + host + ":" + port + "/jini/outrigger-dl.jar";
    }

    private String createSecurityPolicy( File tempDir, File outriggerJarFile )
        throws IOException
    {
        String securityPolicy = my.configuration().securityPolicy().get();
        if( securityPolicy == null || "".equals( securityPolicy ) )
        {
            securityPolicy = "grant {  permission java.security.AllPermission;  };";
        }
        File securityPolicyFile = new File( tempDir, "outrigger-security.policy" );
        copyStringToFile( securityPolicy, securityPolicyFile );
        return securityPolicyFile.getAbsolutePath();
    }

    private void copyStringToFile( String securityPolicy, File securityPolicyFile )
        throws IOException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream( securityPolicy.getBytes() );
        copyStreamToFile( bais, securityPolicyFile );
    }

    private void copyStreamToFile( InputStream inputStream, File destinationFile )
        throws IOException
    {
        destinationFile = destinationFile.getAbsoluteFile();
        File parentFile = destinationFile.getParentFile();
        if( !parentFile.exists() )
        {
            parentFile.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream( destinationFile );
        StreamUtils.copyStream( inputStream, fos, true );
    }
}
