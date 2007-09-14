package org.qi4j.runtime;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import org.qi4j.api.DependencyInjectionContext;
import org.qi4j.api.DependencyKey;
import org.qi4j.api.DependencyResolution;
import org.qi4j.api.DependencyResolver;
import org.qi4j.api.persistence.EntitySession;
import org.qi4j.api.persistence.Query;

/**
 * TODO
 */
public final class EntityDependencyResolver
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

        public Iterable getDependencyInjection( DependencyInjectionContext context )
        {
            // Is it a Query?
            if( key.getRawClass().equals( Query.class ) || key.getRawClass().equals( Iterable.class ) )
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

                        if( key.getRawClass().equals( Query.class ) )
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
                Class dependencyType = key.getRawClass();
                session.getReference( key.getName(), dependencyType );
            }

            return Collections.EMPTY_LIST;
        }
    }
}