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
package org.qi4j.http.sideeffects;

import org.qi4j.http.types.IoMonitor;
import org.qi4j.composite.SideEffectOf;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.net.SocketException;

public class SystemErrIoMonitorSideEffect extends SideEffectOf<IoMonitor>
    implements IoMonitor
{
    public void ioException( String refLocation, IOException e )
    {
        System.err.println( "IOException at " + refLocation );
        e.printStackTrace( System.err );
    }

    public void closedChannelException( String refLocation, ClosedChannelException e )
    {
        System.err.println( "ClosedChannelException at " + refLocation );
        e.printStackTrace( System.err );
    }

    public void socketException( String refLocation, SocketException e )
    {
        System.err.println( "SocketException at " + refLocation );
        e.printStackTrace( System.err );
    }
}
