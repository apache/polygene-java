package org.qi4j.runtime.injection.provider;

import java.util.HashMap;
import java.util.Map;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilder;
import org.qi4j.query.QueryBuilderFactory;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;

/**
 * TODO
 */
public final class EntityInjectionProviderFactory
    implements InjectionProviderFactory
{
    Map<String, QueryBuilderFactory> queries = new HashMap<String, QueryBuilderFactory>();

    UnitOfWork unitOfWork;

    public EntityInjectionProviderFactory( UnitOfWork unitOfWork )
    {
        this.unitOfWork = unitOfWork;
    }

    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel ) throws InvalidInjectionException
    {
        return new EntityDependencyResolution( dependencyModel );
    }

    public void addQueryFactory( String name, QueryBuilderFactory queryBuilderFactory )
    {
        queries.put( name, queryBuilderFactory );
    }

    private class EntityDependencyResolution implements InjectionProvider
    {
        DependencyModel dependencyModel;

        private EntityDependencyResolution( DependencyModel key )
        {
            this.dependencyModel = key;
        }

        public Object provideInjection( InjectionContext context ) throws InjectionProviderException
        {
            Class rawType = dependencyModel.rawInjectionType();
            // Is it a Query, QueryBuilderFactory or Iterable?
            if( rawType.equals( Query.class ) ||
                rawType.equals( QueryBuilderFactory.class ) ||
                rawType.equals( Iterable.class ) )
            {
/*
                // Check for registered named queries
                String name = dependencyModel.getName();
                if( name != null )
                {
                    QueryBuilderFactory builderFactory = queries.get( name );
                    if( builderFactory != null )
                    {
                        if( dependencyModel.getRawType().equals( QueryBuilderFactory.class ) )
                        {
                            return builderFactory;
                        }
                        else if( dependencyModel.getRawType().equals( Iterable.class ) )
                        {
                            QueryBuilder queryBuilder = builderFactory.newQueryBuilder( dependencyModel.getDependencyType() );
                            Query queryobsolete = queryBuilder.newQuery();
                            return queryobsolete.iterator();
                        }
                        else
                        {
                            QueryBuilder queryBuilder = builderFactory.newQueryBuilder( dependencyModel.getDependencyType() );
                            return queryBuilder.newQuery();
                        }
                    }
                }
*/

                QueryBuilderFactory builderFactory = unitOfWork.queryBuilderFactory();

                if( rawType.equals( QueryBuilderFactory.class ) )
                {
                    return builderFactory;
                }

                QueryBuilder queryBuilder = builderFactory.newQueryBuilder( dependencyModel.injectionClass() );
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
            else if( dependencyModel.getName() != null )
            {
                Class dependencyType = dependencyModel.getRawType();
                return unitOfWork.getReference( dependencyModel.getName(), dependencyType );
            }
*/

            return null;
        }
    }
}