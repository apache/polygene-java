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
package org.apache.polygene.runtime.query;

import org.apache.polygene.api.query.NotQueryableException;
import org.apache.polygene.api.query.QueryBuilder;
import org.apache.polygene.api.query.QueryBuilderFactory;
import org.apache.polygene.api.service.NoSuchServiceException;
import org.apache.polygene.api.service.ServiceFinder;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.util.NullArgumentException;
import org.apache.polygene.spi.query.EntityFinder;

/**
 * Default implementation of {@link QueryBuilderFactory}
 */
public final class QueryBuilderFactoryImpl
    implements QueryBuilderFactory
{
    private ServiceFinder finder;

    /**
     * Constructor.
     *
     * @param finder The ServiceFinder of the Module this QueryBuilderFactory belongs to.
     */
    public QueryBuilderFactoryImpl( ServiceFinder finder )
    {
        NullArgumentException.validateNotNull( "ServiceFinder", finder );
        this.finder = finder;
    }

    /**
     * @see QueryBuilderFactory#newQueryBuilder(Class)
     */
    @Override
    public <T> QueryBuilder<T> newQueryBuilder( final Class<T> resultType )
    {
        NotQueryableException.throwIfNotQueryable( resultType );

        final ServiceReference<EntityFinder> serviceReference;
        try
        {
            serviceReference = finder.findService( EntityFinder.class );
            return new QueryBuilderImpl<T>( serviceReference.get(), resultType, null );
        }
        catch( NoSuchServiceException e )
        {
            return new QueryBuilderImpl<T>( null, resultType, null );
        }
    }
}