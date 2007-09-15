package org.qi4j.runtime.resolution;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.qi4j.api.model.DependencyKey;
import org.qi4j.api.persistence.EntitySession;
import org.qi4j.api.query.Query;
import org.qi4j.spi.dependency.DependencyInjectionContext;
import org.qi4j.spi.dependency.DependencyResolution;
import org.qi4j.spi.dependency.DependencyResolver;

/**
 * TODO
 */
public class EntityDependencyResolver
    implements DependencyResolver
{
    EntitySession session;

    public EntityDependencyResolver( EntitySession session )
    {
        this.session = session;
    }

    public DependencyResolution resolveDependency( DependencyKey key )
    {
        return new EntityDependencyResolution( key );
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
            // Is it a Query?
            if( key.getRawType().equals( Query.class ) || key.getRawType().equals( Iterable.class ) )
            {
                Type type = key.getGenericType();
                if( type instanceof ParameterizedType )
                {
                    // Get the result type
                    ParameterizedType paramType = (ParameterizedType) type;
                    Type resultType = paramType.getActualTypeArguments()[ 0 ];
                    if( resultType instanceof Class )
                    {
                        Query query = session.getQueryFactory().newQuery( (Class) resultType );

                        if( key.getRawType().equals( Query.class ) )
                        {
                            return query;
                        }
                        else
                        {
                            return query.prepare();
                        }
                    }
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