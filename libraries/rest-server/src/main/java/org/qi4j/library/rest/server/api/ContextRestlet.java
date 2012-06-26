/**
 *
 * Copyright 2009-2011 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.rest.server.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.cache.CacheOptions;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.library.rest.server.restlet.ResponseWriterDelegator;
import org.qi4j.library.rest.server.spi.CommandResult;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Uniform;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * JAVADOC
 */
public abstract class ContextRestlet
    extends Restlet
{
    @Structure
    protected Module module;

    @Service
    private CommandResult commandResult;

    @Service
    private ResponseWriterDelegator responseWriter;

    private Map<Class, Uniform> subResources = Collections.synchronizedMap( new HashMap<Class, Uniform>() );

    @Override
    public void handle( Request request, Response response )
    {
        super.handle( request, response );

        MDC.put( "url", request.getResourceRef().toString() );

        try
        {
            int tries = 0;

            // TODO Make this number configurable
            while( tries < 10 )
            {
                tries++;

                // Root of the call
                Reference ref = request.getResourceRef();
                List<String> segments = ref.getScheme()
                                            .equals( "riap" ) ? ref.getRelativeRef( new Reference( "riap://application/" ) )
                    .getSegments() : ref.getRelativeRef().getSegments();

                // Handle conversion of verbs into standard interactions
                if( segments.get( segments.size() - 1 ).equals( "" ) )
                {
                    if( request.getMethod().equals( Method.DELETE ) )
                    {
                        // Translate DELETE into command "delete"
                        segments.set( segments.size() - 1, "delete" );
                    }
                    else if( request.getMethod().equals( Method.PUT ) )
                    {
                        // Translate PUT into command "update"
                        segments.set( segments.size() - 1, "update" );
                    }
                }

                request.getAttributes().put( "segments", segments );
                request.getAttributes().put( "template", new StringBuilder( "/rest/" ) );

                Usecase usecase = UsecaseBuilder.buildUsecase( getUsecaseName( request ) )
                    .withMetaInfo( request.getMethod().isSafe() ? CacheOptions.ALWAYS : CacheOptions.NEVER )
                    .newUsecase();
                UnitOfWork uow = module.newUnitOfWork( usecase );

                ObjectSelection.newSelection();

                try
                {
                    // Start handling the build-up for the context
                    Uniform resource = createRoot( request, response );
                    resource.handle( request, response );

                    if( response.getEntity() != null )
                    {
                        if( response.getEntity().getModificationDate() == null )
                        {

                            ResourceValidity validity = (ResourceValidity) Request.getCurrent()
                                .getAttributes()
                                .get( ContextResource.RESOURCE_VALIDITY );
                            if( validity != null )
                            {
                                validity.updateResponse( response );
                            }
                        }

                        // Check if characterset is set
                        if( response.getEntity().getCharacterSet() == null )
                        {
                            response.getEntity().setCharacterSet( CharacterSet.UTF_8 );
                        }

                        // Check if language is set
                        if( response.getEntity().getLanguages().isEmpty() )
                        {
                            response.getEntity().getLanguages().add( Language.ENGLISH );
                        }

                        uow.discard();
                    }
                    else
                    {
                        // Check if last modified and tag is set
                        ResourceValidity validity = null;

                        try
                        {
                            validity = ObjectSelection.type( ResourceValidity.class );
                        }
                        catch( IllegalArgumentException e )
                        {
                            // Ignore
                        }

                        uow.complete();

                        Object result = commandResult.getResult();
                        if( result != null )
                        {
                            if( result instanceof Representation )
                            {
                                response.setEntity( (Representation) result );
                            }
                            else
                            {
                                if( !responseWriter.writeResponse( result, response ) )
                                {
                                    throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Could not write result of type " + result
                                        .getClass()
                                        .getName() );
                                }
                            }

                            if( response.getEntity() != null )
                            {
                                // Check if characterset is set
                                if( response.getEntity().getCharacterSet() == null )
                                {
                                    response.getEntity().setCharacterSet( CharacterSet.UTF_8 );
                                }

                                // Check if language is set
                                if( response.getEntity().getLanguages().isEmpty() )
                                {
                                    response.getEntity().getLanguages().add( Language.ENGLISH );
                                }

                                // Check if last modified and tag should be set
                                if( validity != null )
                                {
                                    UnitOfWork lastModifiedUoW = module.newUnitOfWork();

                                    try
                                    {
                                        validity.updateEntity( lastModifiedUoW );

                                        validity.updateResponse( response );
                                    }
                                    finally
                                    {
                                        lastModifiedUoW.discard();
                                    }
                                }
                            }
                        }
                        return;
                    }
                    return;
                }
                catch( ConcurrentEntityModificationException ex )
                {
                    uow.discard();

                    // Try again
                    ObjectSelection.newSelection();
                }
                catch( Throwable e )
                {
                    uow.discard();
                    handleException( response, e );
                    return;
                }
            } // Try again
        }
        finally
        {
            MDC.clear();
        }
    }

    protected abstract Uniform createRoot( Request request, Response response );

    // Callbacks used from resources
    public void subResource( Class<? extends ContextResource> subResourceClass )
    {
        Uniform subResource = subResources.get( subResourceClass );

        if( subResource == null )
        {
            // Instantiate and store subresource instance
            subResource = module.newObject( subResourceClass, this );
            subResources.put( subResourceClass, subResource );
        }

        subResource.handle( Request.getCurrent(), Response.getCurrent() );
    }

    private String getUsecaseName( Request request )
    {
        if( request.getMethod().equals( org.restlet.data.Method.DELETE ) )
        {
            return "delete";
        }
        else
        {
            return request.getResourceRef().getLastSegment();
        }
    }

    private void handleException( Response response, Throwable ex )
    {
        try
        {
            throw ex;
        }
        catch( ResourceException e )
        {
            // IAE (or subclasses) are considered client faults
            LoggerFactory.getLogger( getClass() ).debug( "ResourceException thrown during processing", e );
            response.setEntity( new StringRepresentation( e.getMessage() ) );
            response.setStatus( e.getStatus() );
        }
        catch( IllegalArgumentException e )
        {
            // IAE (or subclasses) are considered client faults
            LoggerFactory.getLogger( getClass() ).debug( "IllegalArgumentsException thrown during processing", e );
            response.setEntity( new StringRepresentation( e.getMessage() ) );
            response.setStatus( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY );
        }
        catch( RuntimeException e )
        {
            // RuntimeExceptions are considered server faults
            LoggerFactory.getLogger( getClass() ).warn( "Exception thrown during processing", e );
            response.setEntity( new StringRepresentation( e.getMessage() ) );
            response.setStatus( Status.SERVER_ERROR_INTERNAL );
        }
        catch( Exception e )
        {
            // Checked exceptions are considered client faults
            LoggerFactory.getLogger( getClass() ).debug( "Checked exception thrown during processing", e );
            response.setEntity( new StringRepresentation( e.getMessage() ) );
            response.setStatus( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY );
        }
        catch( Throwable e )
        {
            // Anything else are considered server faults
            LoggerFactory.getLogger( getClass() ).error( "Exception thrown during processing", e );
            response.setEntity( new StringRepresentation( e.getMessage() ) );
            response.setStatus( Status.SERVER_ERROR_INTERNAL );
        }
    }
}
