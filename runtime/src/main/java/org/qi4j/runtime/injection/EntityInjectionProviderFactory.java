package org.qi4j.runtime.injection;

import java.util.HashMap;
import java.util.Map;
import org.qi4j.entity.EntitySession;
import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilder;
import org.qi4j.query.QueryBuilderFactory;
import org.qi4j.spi.injection.InjectionContext;
import org.qi4j.spi.injection.InjectionProvider;
import org.qi4j.spi.injection.InjectionProviderFactory;
import org.qi4j.spi.injection.InjectionResolution;
import org.qi4j.spi.injection.InvalidInjectionException;

/**
 * TODO
 */
public class EntityInjectionProviderFactory
    implements InjectionProviderFactory
{
    Map<String, QueryBuilderFactory> queries = new HashMap<String, QueryBuilderFactory>();

    EntitySession session;

    public EntityInjectionProviderFactory( EntitySession session )
    {
        this.session = session;
    }

    public InjectionProvider newInjectionProvider( InjectionResolution resolution ) throws InvalidInjectionException
    {
        return new EntityDependencyResolution( resolution );
    }

    public void addQueryFactory( String name, QueryBuilderFactory queryBuilderFactory )
    {
        queries.put( name, queryBuilderFactory );
    }

    private class EntityDependencyResolution implements InjectionProvider
    {
        InjectionResolution resolution;

        private EntityDependencyResolution( InjectionResolution key )
        {
            this.resolution = key;
        }


        public Object provideInjection( InjectionContext context )
        {
            Class rawType = resolution.getInjectionModel().getRawInjectionType();
            // Is it a Query, QueryBuilderFactory or Iterable?
            if( rawType.equals( Query.class ) ||
                rawType.equals( QueryBuilderFactory.class ) ||
                rawType.equals( Iterable.class ) )
            {
/*
                // Check for registered named queries
                String name = resolution.getName();
                if( name != null )
                {
                    QueryBuilderFactory builderFactory = queries.get( name );
                    if( builderFactory != null )
                    {
                        if( resolution.getRawType().equals( QueryBuilderFactory.class ) )
                        {
                            return builderFactory;
                        }
                        else if( resolution.getRawType().equals( Iterable.class ) )
                        {
                            QueryBuilder queryBuilder = builderFactory.newQueryBuilder( resolution.getDependencyType() );
                            Query query = queryBuilder.newQuery();
                            return query.iterator();
                        }
                        else
                        {
                            QueryBuilder queryBuilder = builderFactory.newQueryBuilder( resolution.getDependencyType() );
                            return queryBuilder.newQuery();
                        }
                    }
                }
*/

                QueryBuilderFactory builderFactory = session.getQueryFactory();

                if( rawType.equals( QueryBuilderFactory.class ) )
                {
                    return builderFactory;
                }

                QueryBuilder queryBuilder = builderFactory.newQueryBuilder( resolution.getInjectionModel().getInjectionClass() );
                Query query = queryBuilder.newQuery();
                if( rawType.equals( Query.class ) )
                {
                    return query;
                }
                else
                {
                    return query.iterator();
                }
            }
/*
            else if( resolution.getName() != null )
            {
                Class dependencyType = resolution.getRawType();
                return session.getReference( resolution.getName(), dependencyType );
            }
*/

            return null;
        }
    }
}