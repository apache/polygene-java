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

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.Set;
import java.util.Iterator;
import java.net.Socket;

class Acceptor
    implements Runnable
{
    private HttpServiceMixin httpServiceMixin;

    public Acceptor( HttpServiceMixin httpServiceMixin )
    {
        this.httpServiceMixin = httpServiceMixin;
    }

    public void run()
    {
        try
        {
            acceptorLoop();
        }
        catch( IOException e )
        {
            if( httpServiceMixin.monitor != null )
            {
                httpServiceMixin.monitor.ioException( Acceptor.class.getName(), e );
            }
        }
    }

    private void acceptorLoop()
        throws IOException
    {
        while( ( httpServiceMixin.selector.select() ) > 0 )
        {
            Set<SelectionKey> readyKeys = httpServiceMixin.selector.selectedKeys();
            Iterator i = readyKeys.iterator();

            while( i.hasNext() )
            {
                SelectionKey sk = (SelectionKey) i.next();
                i.remove();
                // The key indexes into the selector so you
                // can retrieve the socket that's ready for I/O
                ServerSocketChannel nextReady = (ServerSocketChannel) sk.channel();

                Socket s = nextReady.accept().socket();
                httpServiceMixin.queue.post( s );
            }
        }
    }
}
