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

package org.apache.zest.library.restlet.crud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.library.restlet.FormField;
import org.apache.zest.library.restlet.RestForm;
import org.apache.zest.library.restlet.RestLink;
import org.apache.zest.library.restlet.identity.IdentityManager;
import org.apache.zest.library.restlet.repository.CrudRepository;
import org.apache.zest.library.restlet.repository.RepositoryLocator;
import org.apache.zest.library.restlet.resource.ResourceBuilder;
import org.apache.zest.library.restlet.resource.ServerResource;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;

@Mixins( EntityListResource.Mixin.class )
public interface EntityListResource<T extends Identity> extends ServerResource<EntityList>
{
    abstract class Mixin<T extends Identity>
        implements EntityListResource<T>
    {
        @This
        private Parameters<T> parameters;

        @Structure
        private ValueBuilderFactory vbf;

        @Service
        private ResourceBuilder resourceBuilder;

        @Service
        private RepositoryLocator locator;

        @Service
        private IdentityManager identityManager;

        @Override
        public EntityList get()
        {
            Property<Request> request = parameters.request();
            Reference base = request.get().getResourceRef();
            String name = "list[" + parameters.entityType().get().getSimpleName() + "]";
            String identity = identityManager.generate( EntityListResource.class, name );
            ValueBuilder<EntityList> builder = vbf.newValueBuilder( EntityList.class );
            List<EntityRef> result = getEntityRefs( base );
            EntityList prototype = builder.prototype();
            prototype.identity().set( identity );
            prototype.entities().set( Collections.unmodifiableList( result ) );
            prototype.commands().set( Collections.singletonList( resourceBuilder.createCommand( base ) ) );
            return builder.newInstance();
        }

        @Override
        public RestLink post( RestForm form )
        {
            FormField nameField = form.field( "name" );
            String name = null;
            if( nameField != null )
            {
                name = nameField.value().get();
            }
            Reference base = parameters.request().get().getResourceRef();

            //noinspection unchecked
            Class<T> entityType = parameters.entityType().get();

            identityManager.generate( entityType, name );
            locator.find( entityType ).create( name );
            return resourceBuilder.createRestLink( name, base, Method.GET );
        }

        @SuppressWarnings( "unchecked" )
        private List<EntityRef> getEntityRefs( Reference base )
        {
            ArrayList result = new ArrayList<>();
            Property<Class<T>> property = parameters.entityType();
            Class<T> entityType = property.get();
            CrudRepository<T> repository = locator.find( entityType );
            Iterable<T> all = repository.findAll();
            Stream<T> stream = StreamSupport.stream( all.spliterator(), false );
            stream
                .map( entity -> entity.identity().get() )
                .map( identity -> resourceBuilder.createEntityRef( identity, base ) )
                .forEach( result::add );
            return result;
        }
    }
}
