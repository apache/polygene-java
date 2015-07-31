/*
 * Copyright 2007-2009 Niclas Hedhman.
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime.query;

import org.qi4j.api.query.NotQueryableException;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.NoSuchServiceException;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.spi.query.EntityFinder;

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