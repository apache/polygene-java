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

package org.apache.polygene.library.restlet.crud;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueBuilderFactory;
import org.apache.polygene.library.restlet.FormField;
import org.apache.polygene.library.restlet.RestForm;
import org.apache.polygene.library.restlet.RestLink;
import org.apache.polygene.library.restlet.repository.RepositoryLocator;
import org.apache.polygene.library.restlet.resource.ResourceBuilder;
import org.apache.polygene.library.restlet.resource.ServerResource;
import org.apache.polygene.spi.PolygeneSPI;
import org.restlet.data.Reference;

@Mixins( EntityResource.Mixin.class )
public interface EntityResource<T extends HasIdentity> extends ServerResource<T>
{
    interface EntityParam
    {
        @Optional
        Property<String> invoke();
    }

    abstract class Mixin<T extends HasIdentity>
        implements EntityResource<T>
    {

        @Structure
        private PolygeneSPI spi;

        @Structure
        private ValueBuilderFactory vbf;

        @This
        private HasIdentity me;

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
            String idOfEntity = parameters.id().get();
            locator.find( entityType ).delete( new StringIdentity( idOfEntity ) );
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
                Class entityType = parameters.entityType().get();
                //noinspection unchecked
                T entity = (T) locator.find( entityType ).get( identity() );
                if( method.getParameterCount() == 1 )
                {
                    Class argType = method.getParameterTypes()[ 0 ];
                    Object parameters = createParametersComposite( form, argType );
                    method.invoke( entity, parameters );
                }
                else
                {
                    method.invoke( entity );
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
            return resourceBuilder.createRestLink( new StringIdentity( "" ), base, org.restlet.data.Method.GET );
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
                    Class<?> propertyType = descriptor.valueType().primaryType();
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
