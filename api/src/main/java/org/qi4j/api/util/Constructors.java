package org.qi4j.api.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import static org.qi4j.api.util.Iterables.iterable;

/**
 * TODO
 */
public final class Constructors
{
    public static final Function<Type, Iterable<Constructor<?>>> CONSTRUCTORS_OF = Classes.forClassHierarchy( new Function<Class<?>, Iterable<Constructor<?>>>()
                    {
                        @Override
                        public Iterable<Constructor<?>> map( Class<?> type )
                        {
                            return iterable( type.getDeclaredConstructors() );
                        }
                    } );

}
