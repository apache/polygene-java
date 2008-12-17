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
package org.qi4j.library.jini.lookup;

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
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.library.http.HttpService;
import org.qi4j.library.http.Interface;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.util.StreamUtils;

public class ReggieMixin
    implements Activatable
{
    @Service( optional = true ) HttpService httpService;
    @This org.qi4j.api.configuration.Configuration<ReggieConfiguration> my;

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
        File temp = File.createTempFile( "reggie", "", jini );
        temp.delete();
        boolean success = temp.mkdirs();
        // Start Reggie
        Configuration serviceDescConfig = EmptyConfiguration.INSTANCE;
        String httpURL = my.configuration().reggieDlJarLocation().get();
        if( httpURL == null )
        {
            // If Http Service not running in this JVM, we need the Download URL from configuration.
            if( httpService != null )
            {
                httpURL = constructDownloadURL();
            }
        }
        String reggieJarLocation = createReggieJars( temp );
        File reggieJarFile = new File( reggieJarLocation );
        String securityPolicyLocation = createSecurityPolicy( temp, reggieJarFile );
        String reggieConfig = createReggieConfig( temp );
        System.out.println( "Policy: " + Policy.getPolicy() );
        NonActivatableServiceDescriptor serviceDescriptor = new NonActivatableServiceDescriptor(
            httpURL,
            securityPolicyLocation,
            reggieJarLocation,
            "com.sun.jini.reggie.TransientRegistrarImpl",
            new String[]{ reggieConfig } );
        lookupCreated = (NonActivatableServiceDescriptor.Created) serviceDescriptor.create( serviceDescConfig );
    }

    public void passivate() throws Exception
    {
        System.out.println( "Destroying Reggie." );
        Object admin = ( (Administrable) lookupCreated.proxy ).getAdmin();
        ( (DestroyAdmin) admin ).destroy();
        removeFile( fileToCleanup );
    }

    private String createReggieConfig( File dir )
        throws IOException
    {
        StringBuffer configuration = new StringBuffer();
        if( my.configuration().useJrmp().get() )
        {
            System.out.println( "Using JRMP..." );
            configuration.append( "import net.jini.jrmp.JrmpExporter;\n" +
                                  "\n" +
                                  "com.sun.jini.reggie {\n" +
                                  "\n" +
                                  "    serverExporter = new JrmpExporter();\n" );
        }
        else
        {
            System.out.println( "Using JERI..." );
            configuration.append( "import net.jini.jeri.BasicILFactory;\n" +
                                  "import net.jini.jeri.BasicJeriExporter;\n" +
                                  "import net.jini.jeri.tcp.TcpServerEndpoint;\n" +
                                  "\n" +
                                  "com.sun.jini.reggie\n" +
                                  "{\n" +
                                  "    private invocationLayerFactory = new BasicILFactory();\n" +
                                  "    serverExporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0),\n" +
                                  "                                           invocationLayerFactory,\n" +
                                  "                                           false,\n" +
                                  "                                           true);\n" );
        }
        configuration.append( "    initialMemberGroups = new String[] {" );
        String groupData = my.configuration().groups().get();
        String[] groups;
        if( groupData != null )
        {
            groups = convert( groupData );
        }
        else
        {
            groups = new String[]{ "qi4j" };
        }
        boolean first = true;
        for( String group : groups )
        {
            if( !first )
            {
                configuration.append( ", " );
            }
            first = false;
            configuration.append( " \"" );
            configuration.append( group );
            configuration.append( "\" " );
        }

        configuration.append( "};\n}" );
        File reggieConfigFile = new File( dir, "reggie.conf" );
        if( !reggieConfigFile.exists() )
        {
            InputStream reggieConfig = new ByteArrayInputStream( configuration.toString().getBytes() );
            copyStreamToFile( reggieConfig, reggieConfigFile );
        }
        return reggieConfigFile.getAbsolutePath();
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


    private String createReggieJars( File temp )
        throws IOException
    {
        File reggieJar = new File( temp, "reggie-2.1.1.jar" );
        if( !reggieJar.exists() )
        {
            InputStream reggie = getClass().getResourceAsStream( "reggie-2.1.1.jar" );
            copyStreamToFile( reggie, reggieJar );
        }
        return reggieJar.getAbsolutePath();
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
        return protocol + "://" + host + ":" + port + "/jini/reggie-dl.jar";
    }

    private String createSecurityPolicy( File tempDir, File reggieJarFile )
        throws IOException
    {
        String securityPolicy = my.configuration().securityPolicy().get();
        if( securityPolicy == null || "".equals( securityPolicy ) )
        {
            securityPolicy = "grant {  permission java.security.AllPermission;  };";
        }
        File securityPolicyFile = new File( tempDir, "reggie-security.policy" );
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
