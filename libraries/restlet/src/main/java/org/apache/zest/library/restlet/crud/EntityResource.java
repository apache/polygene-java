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

package org.apache.zest.library.restlet.crud;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import org.apache.zest.api.common.Optional;
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
import org.apache.zest.library.restlet.repository.RepositoryLocator;
import org.apache.zest.library.restlet.resource.ResourceBuilder;
import org.apache.zest.library.restlet.resource.ServerResource;
import org.apache.zest.spi.ZestSPI;
import org.restlet.data.Reference;

@Mixins( EntityResource.Mixin.class )
public interface EntityResource<T extends Identity> extends ServerResource<T>
{
    interface EntityParam
    {
        @Optional
        Property<String> invoke();
    }

    abstract class Mixin<T extends Identity>
        implements EntityResource<T>
    {

        @Structure
        private ZestSPI spi;

        @Structure
        private ValueBuilderFactory vbf;

        @This
        private Identity me;

        @This
        private Parameters<T> parameters;

        @This
        private EntityParam entityParam;

        @Service
        private RepositoryLocator locator;

        @Service
        private ResourceBuilder resourceBuilder;

        @Override
        public T get()
        {
            Class entityType = parameters.entityType().get();
            //noinspection unchecked
            return (T) locator.find( entityType ).get( identity() );
        }

        @Override
        public void put( T value )
        {
            Class<T> entityType = parameters.entityType().get();
            locator.find( entityType ).update( value );
        }

        @Override
        public void delete()
        {
            Class entityType = parameters.entityType().get();
            String nameOfEntity = parameters.id().get();
            locator.find( entityType ).delete( nameOfEntity );
        }

        @Override
        public RestLink post( RestForm form )
        {
            Class<T> type = parameters.entityType().get();
            String methodName = entityParam.invoke().get();
            try
            {
                Method method = findMethod( type, methodName );
                if( method == null ) // no arg method doesn't exist, look for single arg method
                {
                    throw new IllegalArgumentException( "Method '" + methodName + "' is not present on " + type.getName() );
                }
                if( method.getParameterCount() == 1 )
                {
                    Class entityType = parameters.entityType().get();
                    //noinspection unchecked
                    T entity = (T) locator.find( entityType ).get( identity() );

                    Class argType = method.getParameterTypes()[ 0 ];
                    Object parameters = createParametersComposite( form, argType );
                    method.invoke( entity, parameters );
                }
                else
                {
                    method.invoke( me );
                }
            }
            catch( Exception e )
            {
                String message = e.getMessage();
                while( e instanceof InvocationTargetException )
                {
                    e = (Exception) ( (InvocationTargetException) e ).getTargetException();
                    message = e.getMessage();
                }
                throw new RuntimeException( message, e );
            }
            Reference base = parameters.request().get().getResourceRef();
            return resourceBuilder.createRestLink( "", base, org.restlet.data.Method.GET );
        }

        private Object createParametersComposite( RestForm form, Class argType )
        {
            ValueBuilder<?> vb = vbf.newValueBuilderWithState(
                argType,
                descriptor -> {
                    FormField field = form.field( descriptor.qualifiedName().name() );
                    if( field == null )
                    {
                        return null;
                    }
                    Class<?> propertyType = descriptor.valueType().mainType();
                    Property<String> value = field.value();
                    if( value == null )
                    {
                        return null;
                    }
                    return convertPropertyValue( value.get(), propertyType );
                },
                descriptor -> null,
                descriptor -> null,
                descriptor -> null
            );
            return vb.newInstance();
        }

        private Method findMethod( Class<T> type, String methodName )
        {
            Method[] methods = type.getMethods();
            Method method = null;
            for( Method m : methods )
            {
                if( m.getName().equals( methodName ) )
                {
                    method = m;
                    break;
                }
            }
            return method;
        }

        private Object convertPropertyValue( String input, Class<?> propertyType )
        {
            if( propertyType.equals( String.class ) )
            {
                return input;
            }
            if( Integer.class.isAssignableFrom( propertyType ) )
            {
                return Integer.parseInt( input );
            }
            if( Boolean.class.isAssignableFrom( propertyType ) )
            {
                return Boolean.valueOf( input );
            }
            if( Double.class.isAssignableFrom( propertyType ) )
            {
                return Double.parseDouble( input );
            }
            if( Long.class.isAssignableFrom( propertyType ) )
            {
                return Long.parseLong( input );
            }
            if( Byte.class.isAssignableFrom( propertyType ) )
            {
                return Byte.parseByte( input );
            }
            if( Short.class.isAssignableFrom( propertyType ) )
            {
                return Short.parseShort( input );
            }
            if( Float.class.isAssignableFrom( propertyType ) )
            {
                return Float.parseFloat( input );
            }
            if( Character.class.isAssignableFrom( propertyType ) )
            {
                return input.charAt( 0 );
            }
            if( TemporalAccessor.class.isAssignableFrom( propertyType ) )
            {
                return DateTimeFormatter.ISO_DATE_TIME.parse( input );
            }
            return null;
        }
    }
}
