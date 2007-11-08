package org.qi4j.runtime.resolution;

import java.util.HashMap;
import java.util.Map;
import org.qi4j.dependency.DependencyInjectionContext;
import org.qi4j.dependency.DependencyResolution;
import org.qi4j.dependency.DependencyResolver;
import org.qi4j.entity.EntitySession;
import org.qi4j.model.DependencyKey;
import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilder;
import org.qi4j.query.QueryBuilderFactory;

/**
 * TODO
 */
public class EntityDependencyResolver
    implements DependencyResolver
{
    Map<String, QueryBuilderFactory> queries = new HashMap<String, QueryBuilderFactory>();

    EntitySession session;

    public EntityDependencyResolver( EntitySession session )
    {
        this.session = session;
    }

    public DependencyResolution resolveDependency( DependencyKey key )
    {
        return new EntityDependencyResolution( key );
    }

    public void addQueryFactory( String name, QueryBuilderFactory queryBuilderFactory )
    {
        queries.put( name, queryBuilderFactory );
    }

    private class EntityDependencyResolution implements DependencyResolution
    {
        DependencyKey key;

        private EntityDependencyResolution( DependencyKey key )
        {
            this.key = key;
        }

        public Object getDependencyInjection( DependencyInjectionContext context )
        {
            // Is it a Query, QueryBuilderFactory or Iterable?
            if( key.getRawType().equals( Query.class ) ||
                key.getRawType().equals( QueryBuilderFactory.class ) ||
                key.getRawType().equals( Iterable.class ) )
            {
                // Check for registered named queries
                String name = key.getName();
                if( name != null )
                {
                    QueryBuilderFactory builderFactory = queries.get( name );
                    if( builderFactory != null )
                    {
                        if( key.getRawType().equals( QueryBuilderFactory.class ) )
                        {
                            return builderFactory;
                        }
                        else if( key.getRawType().equals( Iterable.class ) )
                        {
                            QueryBuilder queryBuilder = builderFactory.newQueryBuilder( key.getDependencyType() );
                            Query query = queryBuilder.newQuery();
                            return query.iterator();
                        }
                        else
                        {
                            QueryBuilder queryBuilder = builderFactory.newQueryBuilder( key.getDependencyType() );
                            return queryBuilder.newQuery();
                        }
                    }
                }

                QueryBuilderFactory builderFactory = session.getQueryFactory();

                if( key.getRawType().equals( QueryBuilderFactory.class ) )
                {
                    return builderFactory;
                }

                QueryBuilder queryBuilder = builderFactory.newQueryBuilder( key.getDependencyType() );
                Query query = queryBuilder.newQuery();
                if( key.getRawType().equals( Query.class ) )
                {
                    return query;
                }
                else
                {
                    return query.iterator();
                }
            }
            else if( key.getName() != null )
            {
                Class dependencyType = key.getRawType();
                return session.getReference( key.getName(), dependencyType );
            }

            return null;
        }
    }
}