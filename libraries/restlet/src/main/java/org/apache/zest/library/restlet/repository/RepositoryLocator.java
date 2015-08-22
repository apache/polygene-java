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

package org.apache.zest.library.restlet.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceReference;

@Mixins( RepositoryLocator.Mixin.class )
public interface RepositoryLocator
{
    <T extends Identity> CrudRepository<T> find( Class<T> entityType );

    class Mixin
        implements RepositoryLocator
    {

        private Map<Class, CrudRepository> repositories = new HashMap<>();

        public Mixin( @Service Iterable<ServiceReference<CrudRepository>> repositories )
        {
            StreamSupport.stream( repositories.spliterator(), true ).forEach( ref -> {
                Class type = ref.metaInfo( EntityTypeDescriptor.class ).entityType();
                this.repositories.put( type, ref.get() );
            } );
        }

        @Override
        public <T extends Identity> CrudRepository<T> find( Class<T> entityType )
        {
            //noinspection unchecked
            return repositories.get( entityType );
        }
    }
}
