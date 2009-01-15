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
package org.qi4j.library.jini.transaction;

import com.sun.jini.admin.DestroyAdmin;
import com.sun.jini.start.NonActivatableServiceDescriptor;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.security.Policy;
import java.util.ArrayList;
import java.util.StringTokenizer;
import net.jini.admin.Administrable;
import net.jini.config.Configuration;
import net.jini.config.EmptyConfiguration;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.util.Streams;
import org.qi4j.library.http.HttpService;
import org.qi4j.library.http.Interface;

public class MahaloMixin
    implements Activatable
{
    @Optional @Service HttpService httpService;
    @This org.qi4j.api.configuration.Configuration<MahaloConfiguration> my;

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
        File temp = File.createTempFile( "mahalo", "", jini );
        temp.delete();
        temp.mkdirs();
        Configuration serviceDescConfig = EmptyConfiguration.INSTANCE;
        String httpURLtext = my.configuration().mahaloDlJarLocation().get();
        if( httpURLtext == null )
        {
            // If Http Service not running in this JVM, we need the Download URL from configuration.
            if( httpService != null )
            {
                httpURLtext = constructDownloadURL();
            }
        }
        String mahaloJarLocation = createMahaloJars( temp );
        File mahaloJarFile = new File( mahaloJarLocation );
        String securityPolicyLocation = createSecurityPolicy( temp, mahaloJarFile );
        String mahaloConfig = createMahaloConfig( temp );
        System.out.println( "Policy: " + Policy.getPolicy() );
        NonActivatableServiceDescriptor serviceDescriptor = new NonActivatableServiceDescriptor(
            httpURLtext,
            securityPolicyLocation,
            mahaloJarLocation,
            "com.sun.jini.mahalo.TransientMahaloImpl",
            new String[]{ mahaloConfig } );
        lookupCreated = (NonActivatableServiceDescriptor.Created) serviceDescriptor.create( serviceDescConfig );

    }

    public void passivate()
        throws Exception
    {
        System.out.println( "Destroying Mahalo." );
        Object admin = ( (Administrable) lookupCreated.proxy ).getAdmin();
        ( (DestroyAdmin) admin ).destroy();
        removeFile( fileToCleanup );
    }

    private String createMahaloConfig( File dir )
        throws IOException
    {
        StringBuffer configuration = new StringBuffer();
        if( my.configuration().useJrmp().get() )
        {
            System.out.println( "Using JRMP..." );
            configuration.append( "import net.jini.jrmp.JrmpExporter;\n" +
                                  "\n" +
                                  "com.sun.jini.mahalo\n" +
                                  "{\n" +
                                  "    serverExporter = new JrmpExporter();\n" );
        }
        else
        {
            System.out.println( "Using JERI..." );
            configuration.append( "import net.jini.jeri.BasicILFactory;\n" +
                                  "import net.jini.jeri.BasicJeriExporter;\n" +
                                  "import net.jini.jeri.tcp.TcpServerEndpoint;\n" +
                                  "\n" +
                                  "com.sun.jini.mahalo\n" +
                                  "{\n" +
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
        File configFile = new File( dir, "mahalo.conf" );
        if( !configFile.exists() )
        {
            InputStream config = new ByteArrayInputStream( configuration.toString().getBytes() );
            copyStreamToFile( config, configFile );
        }
        return configFile.getAbsolutePath();
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
        String[] retVal = new String[result.size()];
        return result.toArray( retVal );
    }


    private String createMahaloJars( File temp )
        throws IOException
    {
        File mahaloJar = new File( temp, "mahalo-2.1.1.jar" );
        if( !mahaloJar.exists() )
        {
            InputStream mahalo = getClass().getResourceAsStream( "mahalo-2.1.1.jar" );
            copyStreamToFile( mahalo, mahaloJar );
        }
        return mahaloJar.getAbsolutePath();
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
        return protocol + "://" + host + ":" + port + "/jini/mahalo-dl.jar";
    }

    private String createSecurityPolicy( File tempDir, File mahaloJarFile )
        throws IOException
    {
        String securityPolicy = my.configuration().securityPolicy().get();
        if( securityPolicy == null || "".equals( securityPolicy ) )
        {
            securityPolicy = "grant {  permission java.security.AllPermission;  };";
        }
        File securityPolicyFile = new File( tempDir, "mahalo-security.policy" );
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
        Streams.copyStream( inputStream, fos, true );
    }
}
