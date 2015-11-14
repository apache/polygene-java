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

package org.apache.zest.library.restlet.identity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkConcern;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;

import static org.apache.zest.functional.Iterables.filter;
import static org.apache.zest.functional.Iterables.first;

@Mixins( IdentityManager.Mixin.class )
@Concerns( { UnitOfWorkConcern.class } )
public interface IdentityManager
{
    String SEPARATOR = "~";
    String IDENTITY_SIGNATURE = "[0-9][0-9]*~.*";

    boolean isIdentity( String candidate );

    String generate( Class type, @Optional String canonicalName );

    String extractName( String identity );

    Class extractType( String identity );

    @UnitOfWorkPropagation
    String findPrefix( Class type );

    class Mixin
        implements IdentityManager
    {
        @Service
        private UuidIdentityGeneratorService uuidService;

        @This
        private Configuration<IdentityMappingConfiguration> config;

        private ConcurrentHashMap<String, Class> actualClasses = new ConcurrentHashMap<>();

        @Override
        public boolean isIdentity( String candidate )
        {
            return candidate.matches( IDENTITY_SIGNATURE );
        }

        @Override
        public String generate( Class type, String canonicalName )
        {
            if( canonicalName == null )
            {
                canonicalName = uuidService.generate( type );
            }
            if( isIdentity( canonicalName ) )
            {
                // This is already an ID, and we simply return it.
                return canonicalName;
            }
            String prefix = findPrefix( type );
            return prefix + SEPARATOR + canonicalName;
        }

        @Override
        public String findPrefix( Class type )
        {
            Map<String, String> mapping = config.get().mapping().get();
            String prefix = mapping.get( type.getName() );
            if( prefix == null )
            {
                config.refresh();
                mapping = config.get().mapping().get();
                prefix = Integer.toString( mapping.size() + 1 );
                mapping.put( type.getName(), prefix );
                config.get().mapping().set( mapping );
                config.save();
            }
            actualClasses.put( type.getName(), type );
            return prefix;
        }

        @Override
        public String extractName( String identity )
        {
            if( !isIdentity( identity ) )
            {
                return identity;
            }
            int pos = identity.indexOf( SEPARATOR );
            if( pos < 1 )
            {
                throw new InvalidIdentityFormatException( identity );
            }
            return identity.substring( pos + 1 );
        }

        @Override
        public Class extractType( String identity )
        {
            if( !isIdentity( identity ) )
            {
                throw new IllegalArgumentException( "Given argument '" + identity + "' is not an Identity" );
            }
            int pos = identity.indexOf( SEPARATOR );
            if( pos < 1 )
            {
                throw new InvalidIdentityFormatException( identity );
            }
            String prefix = identity.substring( 0, pos );
            Map.Entry<String, String> found =
                first(
                    filter(
                        new FindClassSpecification( prefix ),
                        config.get().mapping().get().entrySet()
                    )
                );
            return found == null ? null : actualClasses.get( found.getKey() );
        }

        private static class FindClassSpecification
            implements Predicate<Map.Entry<Class, String>>
        {
            private String prefix;

            private FindClassSpecification( String prefix )
            {
                this.prefix = prefix;
            }

            @Override
            public boolean test( Map.Entry<Class, String> item )
            {
                return item.getValue().equals( prefix );
            }
        }
    }
}
