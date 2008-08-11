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
package org.qi4j.rest;

import org.qi4j.entity.ConcurrentEntityModificationException;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.UnitOfWorkException;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.Uses;
import org.qi4j.object.ObjectBuilder;
import org.qi4j.object.ObjectBuilderFactory;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Finder;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;

public class RestApplication extends Application
{
    @Structure ObjectBuilderFactory factory;
    @Structure UnitOfWorkFactory unitOfWorkFactory;

    public RestApplication( @Uses Context parentContext )
    {
        super( parentContext );
    }

    @Override
    public void handle( Request request, Response response )
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            super.handle( request, response );
            uow.complete();
        }
        catch( UnitOfWorkException e )
        {
            uow.discard();
            response.setStatus( Status.CLIENT_ERROR_NOT_ACCEPTABLE );
            response.setEntity( new ExceptionRepresentation( e ) );
            // More info to send...
        }
        catch( ConcurrentEntityModificationException e )
        {
            uow.discard();
            response.setStatus( Status.CLIENT_ERROR_LOCKED );
            response.setEntity( new ExceptionRepresentation( e ) );
            // Info to try again...
        }
        catch( UnitOfWorkCompletionException e )
        {
            uow.discard();
            response.setStatus( Status.CLIENT_ERROR_NOT_ACCEPTABLE );
            response.setEntity( new ExceptionRepresentation( e ) );
        }
    }

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public synchronized Restlet createRoot()
    {
        Router router = new Router( getContext() );
        router.attach( "/entity", createFinder( EntityTypesResource.class ) );
        router.attach( "/entity/{type}", createFinder( EntityTypeResource.class ) );
        router.attach( "/entity/{type}/{identity}", createFinder( EntityResource.class ) );
        return router;
    }

    private Finder createFinder( Class<? extends Resource> resource )
    {
        ObjectBuilder<Finder> builder = factory.newObjectBuilder( Finder.class );
        builder.use( getContext() );
        builder.use( resource );
        return builder.newInstance();
    }
}
