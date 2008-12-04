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

import org.qi4j.composite.NullArgumentException;
import org.qi4j.query.MissingIndexingSystemException;
import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilder;
import org.qi4j.query.QueryBuilderFactory;
import org.qi4j.query.QueryExpressions;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.service.ServiceFinder;
import org.qi4j.service.ServiceReference;
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
    private final UnitOfWorkInstance unitOfWorkInstance;
    private ServiceFinder finder;

    public static void initialize()
    {
        QueryExpressions.setProvider( new QueryExpressionsProviderImpl() );
    }

    /**
     * Constructor.
     *
     * @param unitOfWorkInstance parent unit of work; cannot be null
     * @param finder             The ServiceFinder of the Module this QueryBuilderFactory belongs to.
     */
    public QueryBuilderFactoryImpl( final UnitOfWorkInstance unitOfWorkInstance, ServiceFinder finder )
    {
        NullArgumentException.validateNotNull( "Unit of work instance", unitOfWorkInstance );
        NullArgumentException.validateNotNull( "ServiceFinder", finder );
        this.finder = finder;
        this.unitOfWorkInstance = unitOfWorkInstance;
    }

    /**
     * @see QueryBuilderFactory#newQueryBuilder(Class)
     */
    public <T> QueryBuilder<T> newQueryBuilder( final Class<T> resultType )
    {
        ModuleInstance module = unitOfWorkInstance.module();
        ServiceFinder serviceLocator = module.serviceFinder();
        final ServiceReference<EntityFinder> serviceReference = serviceLocator.findService( EntityFinder.class );
        if( serviceReference == null )
        {
            return new QueryBuilderImpl<T>( unitOfWorkInstance, null, resultType );
        }
        try
        {
            return new QueryBuilderImpl<T>( unitOfWorkInstance, serviceReference.get(), resultType );
        }
        finally
        {
            serviceReference.releaseService();
        }
    }

    public <T> Query<T> newNamedQuery( String name, Class<T> resultType )
    {
        ModuleInstance module = unitOfWorkInstance.module();
        ServiceFinder serviceLocator = module.serviceFinder();
        final ServiceReference<NamedEntityFinder> serviceReference = serviceLocator.findService( NamedEntityFinder.class );
        if( serviceReference == null )
        {
            throw new MissingIndexingSystemException();
        }
        try
        {
            return new NamedQueryImpl<T>( serviceReference.get(), unitOfWorkInstance, name, resultType );
        }
        finally
        {
            serviceReference.releaseService();
        }
    }
}