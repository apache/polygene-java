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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.scope.Service;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.This;
import org.qi4j.entity.ConcurrentEntityModificationException;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.http.HttpProcessor;
import org.qi4j.http.HttpService;
import org.qi4j.http.HttpServiceConfiguration;
import org.qi4j.http.types.IoMonitor;
import org.qi4j.http.types.UnitOfWorkMonitor;
import org.qi4j.service.Activatable;
import org.qi4j.structure.Module;

public class HttpServiceMixin
    implements HttpService, Activatable
{
    @Structure CompositeBuilderFactory cbf;
    @Structure UnitOfWorkFactory uowf;
    @Structure Module module;
    @Service private Iterable<HttpProcessor> processors;
    @This( optional = true ) IoMonitor monitor;
    @This( optional = true ) UnitOfWorkMonitor uowMonitor;

    private ServerSocketChannel serverSocketChannel;
    Selector selector;
    final Queue queue;
    private final ThreadGroup threadGroup;
    private final List<Thread> threads;

    public HttpServiceMixin()
    {
        queue = new Queue();
        threadGroup = new ThreadGroup( "org.qi4j.http.HttpServiceMixin" );
        threads = new ArrayList<Thread>();
    }

    public void activate()
        throws Exception
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        HttpServiceConfiguration config = uow.find( module.name().get(), HttpServiceConfiguration.class );
        initialize( config );
        setupThreads( config );
        try
        {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking( false );
            ServerSocket socket = serverSocketChannel.socket();
            socket.setReuseAddress( true );
            String hostName = config.hostName().get();
            int port = config.hostPort().get();
            SocketAddress socketAddress = new InetSocketAddress( hostName, port );
            socket.bind( socketAddress );
            selector = Selector.open();
            serverSocketChannel.register( selector, SelectionKey.OP_ACCEPT );
            uow.complete();
        }
        catch( SocketException e )
        {
            if( monitor != null )
            {
                monitor.socketException( HttpServiceMixin.class.getName(), e );
            }
            close();
            uow.discard();
            throw e;
        }
        catch( ClosedChannelException e )
        {
            if( monitor != null )
            {
                monitor.closedChannelException( HttpServiceMixin.class.getName(), e );
            }
            close();
            uow.discard();
            throw e;
        }
        catch( IOException e )
        {
            if( monitor != null )
            {
                monitor.ioException( HttpServiceMixin.class.getName(), e );
            }
            close();
            uow.discard();
            throw e;
        }
        catch( ConcurrentEntityModificationException e )
        {
            uowMonitor.concurrentEntityModificationException( HttpServiceMixin.class.getName(), e );
            close();
            uow.discard();
            throw e;
        }
        catch( UnitOfWorkCompletionException e )
        {
            uowMonitor.completionException( HttpServiceMixin.class.getName(), e );
            close();
            uow.discard();
            throw e;
        }
        finally
        {

        }
    }

    private void initialize( HttpServiceConfiguration config )
    {
        if( config.hostName().get() == null )
        {
            config.hostName().set( "localhost" );
        }
        if( config.hostPort().get() == null )
        {
            config.hostPort().set( 2828 );
        }
    }

    private void setupThreads( HttpServiceConfiguration config )
    {
        int workers = config.numberOfWorkerThreads().get();
        for( int i = 0; i < workers; i++ )
        {
            Worker worker = new Worker(queue);
            Thread t = new Thread( threadGroup, worker, "HttpConnectionWorker-" + i );
            t.start();
            threads.add( t );
        }
        Acceptor acceptor = new Acceptor( this );
        Thread t = new Thread( threadGroup, acceptor, "Http Acceptor" );
        t.start();
        threads.add( t );
    }

    private void close()
        throws IOException
    {
        try
        {
            if( serverSocketChannel != null )
            {
                serverSocketChannel.close();
            }
            if( selector != null )
            {
                selector.close();
            }
        }
        finally
        {
            serverSocketChannel = null;
            selector = null;
        }
    }

    public void passivate() throws Exception
    {
        close();
    }

}
