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
package org.qi4j.http.server;

import java.net.Socket;
import java.util.Iterator;
import java.util.HashMap;
import org.qi4j.http.HttpConnection;
import org.qi4j.composite.CompositeBuilderFactory;

class Worker
    implements Runnable
{
    private CompositeBuilderFactory cbf;
    private Queue queue;
    private final HashMap<Socket,HttpConnection> connections;

    public Worker( Queue queue )
    {
        this.queue = queue;
        connections = new HashMap<Socket, HttpConnection>();
    }

    public void run()
    {
        try
        {
            while( true )
            {
                Socket socket = queue.next();
                HttpConnection conn;
                synchronized( connections )
                {
                    conn = connections.get( socket );
                    if( conn == null )
                    {
                        conn = newConnection();
                        connections.put( socket, conn );
                    }
                }
                conn.process( socket );
            }
        }
        catch( InterruptedException e )
        {
            // Expected when to shutdown.
        }
    }

    private HttpConnection newConnection()
    {
        return cbf.newComposite( HttpConnection.class );
    }
}
