/*
 * Copyright (c) 2012, Paul Merlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.test.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class FreePortFinder
{

    public static int findFreePortOnLoopback()
            throws IOException
    {
        return findFreePortOnIface( InetAddress.getLocalHost() );
    }

    public static int findFreePortOnLoopback( int prefered )
            throws IOException
    {
        return findFreePortOnIface( InetAddress.getLocalHost(), prefered );
    }

    public static int findFreePortOnIface( InetAddress address )
            throws IOException
    {
        return findFreePortOnIface( address, -1 );
    }

    public static int findFreePortOnIface( InetAddress address, int prefered )
            throws IOException
    {
        ServerSocket server;
        if ( prefered > 0 ) {
            server = new ServerSocket( prefered, 1, address );
        } else {
            server = new ServerSocket( 0, 1, address );
        }
        int port = server.getLocalPort();
        server.close();
        return port;
    }

}
