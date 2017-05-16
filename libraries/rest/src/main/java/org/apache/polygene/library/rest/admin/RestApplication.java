/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.library.rest.admin;

import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.object.ObjectFactory;
import org.apache.polygene.api.unitofwork.ConcurrentEntityModificationException;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.api.unitofwork.UnitOfWorkException;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

public class RestApplication
    extends Application
{
    public static final MediaType APPLICATION_SPARQL_JSON = new MediaType( "application/sparql-results+json", "SPARQL JSON" );

    @Structure
    private ObjectFactory objectFactory;

    @Structure
    private UnitOfWorkFactory uowf;

    public RestApplication( @Uses Context parentContext )
    {
        super( parentContext );

        getMetadataService().addExtension( "srj", APPLICATION_SPARQL_JSON );

        getTunnelService().setExtensionsTunnel( true );
    }

    @Override
    public void handle( Request request, Response response )
    {
        UnitOfWork uow = uowf.newUnitOfWork();
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
    public synchronized Restlet createInboundRoot()
    {
        Router router = new Router( getContext() );

        router.attach( "/entity", newFinder( EntitiesResource.class ) );
        router.attach( "/entity/{reference}", newFinder( EntityResource.class ) );

        router.attach( "/query", newFinder( SPARQLResource.class ) );
        router.attach( "/query/sparqlhtml", newFinder( SPARQLResource.class ) );
        router.attach( "/query/index", newFinder( IndexResource.class ) );

        return router;
    }

    private Finder newFinder( Class<? extends ServerResource> resource )
    {
        Finder finder = objectFactory.newObject( Finder.class );
        finder.setTargetClass( resource );
        return finder;
    }
}
