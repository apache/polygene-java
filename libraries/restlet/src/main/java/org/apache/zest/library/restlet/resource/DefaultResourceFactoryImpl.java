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

import java.util.Map;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.api.property.PropertyDescriptor;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.spi.ZestSPI;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Router;

public class DefaultResourceFactoryImpl<K extends Identity, T extends ServerResource<K>>
    implements ResourceFactory<K, T>
{
    @Uses
    private Class<T> resourceType;

    @Uses
    private Context context;

    @Uses
    private Router router;

    @Uses
    private Request request;

    @Structure
    private ValueBuilderFactory vbf;

    @Structure
    private ZestSPI spi;

    @Override
    public T create( Class<T> entityType, Request request, Response response, Context context )
    {
        final Map<String, Object> attributes = request.getAttributes();
        String id = (String) attributes.get( "id" );

        ValueBuilder<T> builder = vbf.newValueBuilderWithState(
            resourceType,
            descriptor -> findValue( attributes, descriptor ),
            descriptor -> null,
            descriptor -> null,
            descriptor -> null
        );
        //noinspection unchecked
        ServerResource.Parameters<T> params = builder.prototypeFor( ServerResource.Parameters.class );
        params.id().set( id );
        params.entityType().set( entityType );
        params.context().set( this.context );
        params.request().set( request );
        params.router().set( router );
        params.response().set( response );
        return builder.newInstance();
    }

    private Object findValue( Map<String, Object> attributes, PropertyDescriptor descriptor )
    {
        String name = descriptor.qualifiedName().name();
        if( name.equals( "identity" ) )
        {
            Object id = attributes.get( "id" );
            if( id == null )
            {
                throw new IllegalArgumentException( resourceType.getName() + " implements Identity and must have an {id} attribute in the path templatee." );
            }
            return id;
        }
        return attributes.get( name );
    }
}
