/*
 * Copyright 2007-2008 Niclas Hedhman.
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

import org.qi4j.api.query.*;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.named.NamedEntityFinder;

/**
 * Default implementation of {@link QueryBuilderFactory}
 *
 * @author Alin Dreghiciu
 * @since March 25, 2008
 */
public final class QueryBuilderFactoryImpl
    implements QueryBuilderFactory
{

    /**
     * Parent unit of work.
     */
    private final UnitOfWork unitOfWork;
    private ServiceFinder finder;
    private ClassLoader classLoader;

    public static void initialize()
    {
        QueryExpressions.setProvider( new QueryExpressionsProviderImpl() );
    }

    /**
     * Constructor.
     *
     * @param unitOfWork parent unit of work; cannot be null
     * @param finder     The ServiceFinder of the Module this QueryBuilderFactory belongs to.
     */
    public QueryBuilderFactoryImpl( final UnitOfWork unitOfWork, ClassLoader classLoader, ServiceFinder finder )
    {
        NullArgumentException.validateNotNull( "Unit of work instance", unitOfWork );
        NullArgumentException.validateNotNull( "ServiceFinder", finder );
        this.finder = finder;
        this.unitOfWork = unitOfWork;
        this.classLoader = classLoader;
    }

    /**
     * @see QueryBuilderFactory#newQueryBuilder(Class)
     */
    public <T> QueryBuilder<T> newQueryBuilder( final Class<T> resultType )
    {
        NotQueryableException.throwIfNotQueryable( resultType );

        final ServiceReference<EntityFinder> serviceReference = finder.findService( EntityFinder.class );
        if( serviceReference == null )
        {
            return new QueryBuilderImpl<T>( unitOfWork, null, classLoader, resultType );
        }
        return new QueryBuilderImpl<T>( unitOfWork, serviceReference.get(), classLoader, resultType );
    }

    public <T> Query<T> newNamedQuery( Class<T> resultType, String name )
    {
        final ServiceReference<NamedEntityFinder> serviceReference = finder.findService( NamedEntityFinder.class );
        if( serviceReference == null )
        {
            throw new MissingIndexingSystemException();
        }
        return new NamedQueryImpl<T>( serviceReference.get(), unitOfWork, classLoader, name, resultType );
    }
}