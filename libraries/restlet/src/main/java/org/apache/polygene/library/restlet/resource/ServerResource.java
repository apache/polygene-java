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

package org.apache.polygene.library.restlet.resource;

import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identifiable;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.library.restlet.RestForm;
import org.apache.polygene.library.restlet.RestLink;
import org.apache.polygene.library.restlet.identity.IdentityManager;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Router;

@Mixins( { ServerResource.NotPresent.class, ServerResource.IdentityMixin.class } )
public interface ServerResource<T extends HasIdentity> extends Identifiable
{
    T get();

    void put( T value );

    void delete();

    RestLink post( RestForm form );

    interface Parameters<T>
    {
        @Optional
        Property<String> id();

        Property<Class<T>> entityType();

        Property<Request> request();

        Property<Response> response();

        Property<Context> context();

        Property<Router> router();
    }

    abstract class IdentityMixin<T extends HasIdentity>
        implements ServerResource<T>
    {
        @This
        private Parameters<T> parameters;

        @Service
        private IdentityManager identityManager;

        @Override
        public Identity identity()
        {
            return identityManager.generate( parameters.entityType().get(), parameters.id().get() );
        }
    }

    abstract class NotPresent
        implements ServerResource
    {
        @Override
        public HasIdentity get()
        {
            throw new NotPresentException();
        }

        @Override
        public void put( HasIdentity value )
        {
            throw new NotPresentException();
        }

        @Override
        public void delete()
        {
            throw new NotPresentException();
        }

        @Override
        public RestLink post( RestForm form )
        {
            throw new NotPresentException();
        }
    }
}
