/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.zest.library.restlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.usecase.Usecase;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.library.restlet.metainfo.UserIdentity;
import org.apache.zest.library.restlet.repository.RepositoryLocator;
import org.apache.zest.library.restlet.resource.NotPresentException;
import org.apache.zest.library.restlet.resource.ResourceFactory;
import org.apache.zest.library.restlet.resource.ServerResource;
import org.apache.zest.library.restlet.serialization.ZestConverter;
import org.apache.zest.spi.ZestSPI;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.routing.Router;
import org.restlet.security.User;

public class ZestEntityRestlet<T extends Identity> extends Restlet
{
    @Structure
    private ValueBuilderFactory vbf;

    @Structure
    private UnitOfWorkFactory uowf;

    @Uses
    private ResourceFactory resourceFactory;

    @Uses
    private Router router;

    @Uses
    @Optional
    private Class<T> identityType;

    @Structure
    private ZestSPI spi;

    @Uses
    private ZestConverter converter;

    @Service
    private RepositoryLocator locator;

    @Override
    public void handle( Request request, Response response )
    {
        try
        {
            super.handle( request, response );
            Method method = request.getMethod();
            if( method.equals( Method.GET ) )
            {
                get( request, response );
            }
            if( method.equals( Method.DELETE ) )
            {
                delete( request, response );
            }
            if( method.equals( Method.POST ) )
            {
                post( request, response );
            }
            if( method.equals( Method.PUT ) )
            {
                put( request, response );
            }
        }
        catch( RuntimeException e )
        {
            e.printStackTrace();
            throw e;
        }
    }

    private void get( Request request, Response response )
    {
        execute( request, response, resource -> {
                     try
                     {
                         T result = resource.get();
                         if( result != null )
                         {
                             if( result instanceof EntityComposite )
                             {
                                 result = locator.find( identityType ).toValue( result );
                             }
                             Representation representation = converter.toRepresentation( result, new Variant(), null );
                             response.setEntity( representation );
                             response.setEntity( representation );
                             response.setStatus( Status.SUCCESS_OK );
                         }
                         else
                         {
                             response.setStatus( Status.CLIENT_ERROR_NOT_FOUND );
                         }
                     }
                     catch( NoSuchEntityException e )
                     {
                         response.setStatus( Status.CLIENT_ERROR_NOT_FOUND, e, "Entity not found." );
                     }
                 }
        );
    }

    private void put( Request request, Response response )
    {
        execute( request, response, resource -> {

            T value = convertToObject( identityType, request );
            resource.put( value );
            response.setStatus( Status.SUCCESS_OK );
        } );
    }

    private void delete( Request request, Response response )
    {
        execute( request, response, resource -> {
            resource.delete();
            response.setStatus( Status.SUCCESS_OK );
        } );
    }

    private void post( final Request request, final Response response )
    {
        execute( request, response, resource -> {
            RestForm form = createRestForm( request );
            RestLink link = resource.post( form );
            response.setLocationRef( link.path().get() );
            response.setStatus( Status.REDIRECTION_SEE_OTHER );
        } );
    }

    private RestForm createRestForm( final Request request )
    {
        //noinspection MismatchedQueryAndUpdateOfCollection
        Form form = new Form( request.getEntity() );
        ValueBuilder<RestForm> builder = vbf.newValueBuilderWithState(
            RestForm.class,
            descriptor -> {
                if( descriptor.qualifiedName().name().equals( "fields" ) )
                {
                    List<FormField> result = new ArrayList<>();
                    for( Parameter param : form )
                    {
                        String name = param.getName();
                        String value = param.getValue();
                        ValueBuilder<FormField> fieldBuilder = vbf.newValueBuilder( FormField.class );
                        FormField prototype = fieldBuilder.prototype();
                        prototype.name().set( name );
                        prototype.value().set( value );
                        prototype.type().set( FormField.TEXT );
                        result.add( fieldBuilder.newInstance() );
                    }
                    return result;
                }
                return null;
            },
            descriptor -> null,
            descriptor -> null,
            descriptor -> null
        );
        return builder.newInstance();
    }

    private void execute( Request request, Response response, Consumer<ServerResource<T>> closure )
    {
        UnitOfWork uow = null;
        try
        {
            uow = createUnitOfWork( request );
            ServerResource<T> resource = createResource( request, response, getContext() );
            closure.accept( resource );
            uow.complete();
        }
        catch( UnsupportedOperationException e )
        {
            e.printStackTrace();
            response.setStatus( Status.CLIENT_ERROR_BAD_REQUEST, e, e.getMessage() );
        }
        catch( ConversionException e )
        {
            e.printStackTrace();
            response.setStatus( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage() );
        }
        catch( NotPresentException e )
        {
            e.printStackTrace();
            response.setStatus( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            response.setStatus( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage() );
        }
        finally
        {
            if( uow != null && uow.isOpen() )
            {
                uow.discard();
            }
        }
    }

    private ServerResource<T> createResource( Request request, Response response, Context context )
    {
        @SuppressWarnings( "unchecked" )
        ServerResource<T> serverResource = resourceFactory.create( identityType, request, response, context );
        return serverResource;
    }

    private UnitOfWork createUnitOfWork( Request request )
    {
        User user = request.getClientInfo().getUser();
        UserIdentity userIdentity = new UserIdentity( user.getIdentifier(),
                                                      user.getName(),
                                                      user.getEmail(),
                                                      user.getFirstName(),
                                                      user.getLastName()
        );
        Usecase usecase = UsecaseBuilder
            .buildUsecase( request.getResourceRef().getIdentifier( true ) )
            .withMetaInfo( userIdentity )
            .newUsecase();
        return uowf.newUnitOfWork( usecase );
    }

    private <K> K convertToObject( Class<K> type, Request request )
    {
        try
        {
            return converter.toObject( request.getEntity(), type, null );
        }
        catch( IOException e )
        {
            throw new ConversionException( request.getEntityAsText() );
        }
    }
}
