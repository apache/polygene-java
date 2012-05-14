/*
 * Copyright (c) 2010 Niclas Hedhman <niclas@hedhman.org>.
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

package org.qi4j.entitystore.voldemort;

import org.junit.Ignore;
import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.voldemort.assembly.VoldemortAssembler;
import org.qi4j.test.entity.AbstractEntityStoreTest;
import voldemort.server.VoldemortConfig;
import voldemort.server.VoldemortServer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

@Ignore( "This version of Voldemort is still using Jetty 6. If we can not upgrade, we should drop support for this ES.")
public class VoldemortTest extends AbstractEntityStoreTest
{
    private List<VoldemortServer> servers = new ArrayList<VoldemortServer>();

    @Override
    public void setUp()
        throws Exception
    {
        Thread.sleep(200);
        File voldemortHome1 = setupVoldemortHome();
        startServer( voldemortHome1, "node0.properties" );
        File voldemortHome2 = setupVoldemortHome();
        startServer( voldemortHome2, "node1.properties" );
        super.setUp();
    }

    @Override
    public void tearDown()
        throws Exception
    {
        super.tearDown();
        for( VoldemortServer server : servers )
        {
            server.stop();
        }
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        new VoldemortAssembler( Visibility.layer ).assemble( module );
    }

    private File setupVoldemortHome()
        throws IOException
    {
        File tmp = File.createTempFile( "qi4j", "" );
        File dir = tmp.getParentFile();
        File voldemortHome = new File( dir, "qi4j-voldemort" + new Random().nextInt( 10000 ) );
        boolean created = voldemortHome.mkdir();
        File configDir = new File( voldemortHome, "config" );
        created = configDir.mkdir();
        copyFile( "cluster.xml", configDir );
        copyFile( "stores.xml", configDir );
        return voldemortHome;
    }

    private void startServer( File voldemortHome, String serverProps )
        throws IOException
    {
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream( serverProps );
        props.load( in );
        props.setProperty( "voldemort.home", voldemortHome.getCanonicalPath() );
        VoldemortConfig config = new VoldemortConfig( props );
        config.setEnableJmx( false );
        VoldemortServer server = new VoldemortServer( config );
        server.start();
        this.servers.add( server );
    }

    private void copyFile( String source, File voldemortHome )
        throws IOException
    {
        InputStream in = getClass().getResourceAsStream( source );
        FileOutputStream out = new FileOutputStream( new File( voldemortHome, source ) );
        BufferedOutputStream stream = new BufferedOutputStream( out );
        int data = in.read();
        while( data != -1 )
        {
            stream.write( data );
            data = in.read();
        }
        in.close();
        stream.close();
    }

}
