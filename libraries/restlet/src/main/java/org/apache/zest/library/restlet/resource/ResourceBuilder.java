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

package org.apache.zest.library.restlet.resource;

import java.io.IOException;
import java.util.Collections;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.object.ObjectFactory;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.library.restlet.Command;
import org.apache.zest.library.restlet.FormField;
import org.apache.zest.library.restlet.RestForm;
import org.apache.zest.library.restlet.RestLink;
import org.apache.zest.library.restlet.crud.EntityRef;
import org.apache.zest.library.restlet.identity.IdentityManager;
import org.apache.zest.library.restlet.serialization.ZestConverter;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.routing.Route;
import org.restlet.routing.Router;

@Mixins( ResourceBuilder.Mixin.class )
public interface ResourceBuilder
{
    EntityRef createEntityRef( String name, Reference base );

    EntityRef createEntityRef( String name, RestLink get, RestLink put, RestLink delete );

    RestLink createRestLink( String name, Reference base, Method method );

    RestLink createRestLink( String name, Reference base, Method method, String description );

    Command createCommand( Reference base );

    RestForm createNameForm( Reference base );

    FormField createFormField( String name, String type );

    <T extends Identity> Representation toRepresentation( Class<T> type, T composite );

    <T extends Identity> T toObject( Class<T> type, Representation representation )
        throws IOException;

    Route findRoute( String name, Router router );

    class Mixin
        implements ResourceBuilder
    {
        @Service
        private IdentityManager identityManager;

        private final ZestConverter converter;

        @Structure
        private ValueBuilderFactory vbf;

        public Mixin( @Structure ObjectFactory objectFactory )
        {
            converter = new ZestConverter( objectFactory );
        }

        public EntityRef createEntityRef( String identity, Reference base )
        {
            String name = identityManager.extractName( identity );
            RestLink get = createRestLink( name, base, Method.GET );
            RestLink put = createRestLink( name, base, Method.PUT );
            RestLink delete = createRestLink( name, base, Method.DELETE );
            return createEntityRef( name, get, put, delete );
        }

        public EntityRef createEntityRef( String identity, RestLink get, RestLink put, RestLink delete )
        {
            ValueBuilder<EntityRef> refBuilder = vbf.newValueBuilder( EntityRef.class );
            EntityRef refPrototype = refBuilder.prototype();
            refPrototype.name().set( identityManager.extractName( identity ) );
            refPrototype.get().set( get );
            refPrototype.put().set( put );
            refPrototype.delete().set( delete );
            return refBuilder.newInstance();
        }

        public RestLink createRestLink( String name, Reference base, Method method )
        {
            name = identityManager.extractName( name );

            ValueBuilder<RestLink> builder = vbf.newValueBuilder( RestLink.class );
            RestLink prototype = builder.prototype();
            String path = base.toUri().resolve( name ).getPath();
            prototype.path().set( path.endsWith( "/" ) ? path : path + "/" );
            prototype.method().set( method.getName() );
            return builder.newInstance();
        }

        @Override
        public RestLink createRestLink( String name, Reference base, Method method, String description )
        {
            ValueBuilder<RestLink> builder = vbf.newValueBuilder( RestLink.class );
            RestLink prototype = builder.prototype();
            prototype.path().set( base.toUri().resolve( name ).getPath() + "/" );
            prototype.method().set( method.getName() );
            prototype.description().set( description );
            return builder.newInstance();
        }

        public Command createCommand( Reference base )
        {
            RestForm form = createNameForm( base );
            ValueBuilder<Command> builder = vbf.newValueBuilder( Command.class );
            builder.prototype().name().set( "create" );
            builder.prototype().form().set( form );
            return builder.newInstance();
        }

        public RestForm createNameForm( Reference base )
        {
            ValueBuilder<RestForm> builder = vbf.newValueBuilder( RestForm.class );
            builder.prototype().link().set( createRestLink( "form", base, Method.POST ) );
            builder.prototype().fields().set( Collections.singletonList( createFormField( "name", FormField.TEXT ) ) );
            return builder.newInstance();
        }

        public FormField createFormField( String name, String type )
        {
            ValueBuilder<FormField> builder = vbf.newValueBuilder( FormField.class );
            builder.prototype().name().set( name );
            builder.prototype().type().set( type );
            return builder.newInstance();
        }

        @Override
        public <T extends Identity> Representation toRepresentation( Class<T> type, T composite )
        {
            return converter.toRepresentation( composite, new Variant(), null );
        }

        @Override
        public <T extends Identity> T toObject( Class<T> type, Representation representation )
            throws IOException
        {
            return converter.toObject( representation, type, null );
        }

        @Override
        public Route findRoute( String name, Router router )
        {
            return router.getRoutes().stream().filter( route -> name.equals( route.getName() ) ).findFirst().get();
        }
    }
}
