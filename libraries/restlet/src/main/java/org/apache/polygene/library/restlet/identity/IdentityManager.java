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

package org.apache.polygene.library.restlet.identity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.concern.Concerns;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.IdentityGenerator;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.unitofwork.concern.UnitOfWorkConcern;
import org.apache.polygene.api.unitofwork.concern.UnitOfWorkPropagation;

@Mixins( IdentityManager.Mixin.class )
@Concerns( { UnitOfWorkConcern.class } )
public interface IdentityManager
{
    String SEPARATOR = "~";
    String IDENTITY_SIGNATURE = "[0-9][0-9]*~.*";

    boolean isIdentity( String candidate );

    Identity generate( Class type, @Optional String canonicalName );

    String extractName( Identity identity );

    Class extractType( Identity identity );

    @UnitOfWorkPropagation
    String findPrefix( Class type );

    class Mixin
        implements IdentityManager
    {
        @Service
        private IdentityGenerator uuidService;

        @This
        private Configuration<IdentityMappingConfiguration> config;

        private ConcurrentHashMap<String, Class> actualClasses = new ConcurrentHashMap<>();

        @Override
        public boolean isIdentity( String candidate )
        {
            return candidate.matches( IDENTITY_SIGNATURE );
        }

        @Override
        public Identity generate( Class type, String canonicalName )
        {
            if( canonicalName == null )
            {
                canonicalName = uuidService.generate( type ).toString();
            }
            if( isIdentity( canonicalName ) )
            {
                // This is already an ID, and we simply return it.
                return new StringIdentity( canonicalName );
            }
            String prefix = findPrefix( type );
            return new StringIdentity( prefix + SEPARATOR + canonicalName );
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
        public String extractName( Identity identity )
        {
            String idString = identity.toString();
            int pos = idString.indexOf( SEPARATOR );
            if( pos < 1 )
            {
                throw new InvalidIdentityFormatException( idString );
            }
            return idString.substring( pos + 1 );
        }

        @Override
        public Class extractType( Identity identity )
        {
            String idString = identity.toString();
            if( !isIdentity( idString ) )
            {
                throw new IllegalArgumentException( "Given argument '" + idString + "' is not an Identity" );
            }
            int pos = idString.indexOf( SEPARATOR );
            if( pos < 1 )
            {
                throw new InvalidIdentityFormatException( idString );
            }
            String prefix = idString.substring( 0, pos );
            Map.Entry<String, String> found = config.get().mapping().get().entrySet().stream()
                                                    .filter( new FindClassSpecification( prefix ) )
                                                    .findFirst().orElse( null );
            return found == null ? null : actualClasses.get( found.getKey() );
        }

        private static class FindClassSpecification
            implements Predicate<Map.Entry<String, String>>
        {
            private String prefix;

            private FindClassSpecification( String prefix )
            {
                this.prefix = prefix;
            }

            @Override
            public boolean test( Map.Entry<String, String> item )
            {
                return item.getValue().equals( prefix );
            }
        }
    }
}
