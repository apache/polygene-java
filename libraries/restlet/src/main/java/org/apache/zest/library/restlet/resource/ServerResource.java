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

import org.apache.zest.api.common.Optional;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.library.restlet.RestForm;
import org.apache.zest.library.restlet.RestLink;
import org.apache.zest.library.restlet.identity.IdentityManager;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Router;

@Mixins( { ServerResource.NotPresent.class, ServerResource.IdentityMixin.class } )
public interface ServerResource<T extends Identity>
{
    String identity();

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

    abstract class IdentityMixin<T extends Identity>
        implements ServerResource<T>
    {
        @This
        private Parameters<T> parameters;

        @Service
        private IdentityManager identityManager;

        @Override
        public String identity()
        {
            return identityManager.generate( parameters.entityType().get(), parameters.id().get() );
        }
    }

    abstract class NotPresent
        implements ServerResource
    {
        @Override
        public Identity get()
        {
            throw new NotPresentException();
        }

        @Override
        public void put( Identity value )
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
