/*
 * Copyright 2014 Paul Merlin.
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
package org.qi4j.test.util;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * A set of methods useful for stating assumptions about the conditions in which a test is meaningful.
 */
public class Assume
    extends org.junit.Assume
{

    /**
     * If called on a IBM JDK, the test will halt and be ignored.
     */
    public static void assumeNoIbmJdk()
    {
        assumeFalse( System.getProperty( "java.vendor" ).contains( "IBM" ) );
    }

    /**
     * If called on a headless runtime, the test will halt and be ignored.
     */
    public static void assumeDisplayPresent()
    {
        assumeFalse( GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance() );
        String display = System.getenv( "DISPLAY" );
        assumeThat( display, is( notNullValue() ) );
        assumeTrue( display.length() > 0 );
    }

    /**
     * If called on a networkless runtime, the test will halt and be ignored.
     */
    public static void assumeNetworking()
    {
        try
        {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            assumeThat( ifaces, is( notNullValue() ) );
            assumeTrue( ifaces.hasMoreElements() );
        }
        catch( SocketException ex )
        {
            assumeFalse( true );
        }
    }

    /**
     * If called on a runtime with no access to qi4j.org on port 80, the test will halt and be ignored.
     */
    public static void assumeConnectivity()
    {
        assumeConnectivity( "qi4j.org", 80 );
    }

    /**
     * If called on a runtime with no access to given host and port, the test will halt and be ignored.
     *
     * @param host Host
     * @param port Port
     */
    public static void assumeConnectivity( String host, int port )
    {
        try( Socket socket = new Socket( host, port ) )
        {
            // Connected
        }
        catch( IOException ex )
        {
            assumeFalse( true );
        }
    }

    /**
     * If called on a runtime without the given System Property set, the test will halt and be ignored.
     * @param key the name of the system property
     * @return The System Propery value if not null
     */
    public static String assumeSystemPropertyNotNull( String key )
    {
        String property = System.getProperty( key );
        assumeNotNull( property );
        return property;
    }
}
