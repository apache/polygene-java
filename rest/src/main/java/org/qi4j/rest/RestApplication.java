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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.*;
import org.qi4j.rest.entity.AllEntitiesResource;
import org.qi4j.rest.entity.EntitiesResource;
import org.qi4j.rest.entity.EntityResource;
import org.qi4j.rest.query.IndexResource;
import org.qi4j.rest.query.SPARQLResource;
import org.qi4j.rest.type.EntityTypeResource;
import org.qi4j.rest.type.EntityTypesResource;
import org.restlet.*;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;

public class RestApplication extends Application
{
    public static final MediaType APPLICATION_SPARQL_JSON = new MediaType( "application/sparql-results+json", "SPARQL JSON" );

    @Structure ObjectBuilderFactory factory;
    @Structure UnitOfWorkFactory unitOfWorkFactory;

    public RestApplication( @Uses Context parentContext )
    {
        super( parentContext );

        getMetadataService().addExtension( "srj", APPLICATION_SPARQL_JSON );
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

        router.attach( "/service", Qi4jServiceResource.class );

        router.attach( "/entitytype", createFinder( EntityTypesResource.class ) );
        router.attach( "/entitytype/{type}", createFinder( EntityTypeResource.class ) );

        router.attach( "/entity", createFinder( AllEntitiesResource.class ) );
        router.attach( "/entity/{type}", createFinder( EntitiesResource.class ) );
        router.attach( "/entity/{type}/{identity}", createFinder( EntityResource.class ) );

        router.attach( "/query", createFinder( SPARQLResource.class ) );
        router.attach( "/query/index", createFinder( IndexResource.class ) );

        // Add filters
        return new ExtensionMediaTypeFilter( getContext(), router );
    }

    private Finder createFinder( Class<? extends Resource> resource )
    {
        ObjectBuilder<Finder> builder = factory.newObjectBuilder( Finder.class );
        builder.use( getContext().createChildContext() );
        builder.use( resource );
        return builder.newInstance();
    }
}
