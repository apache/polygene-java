package org.apache.polygene.api.type;

import java.lang.reflect.Type;
import java.util.function.Predicate;

public class HasEqualOrAssignableToType<T extends HasTypes> implements Predicate<T>
{
    private final Predicate<T> composedPredicate;

    public HasEqualOrAssignableToType( Type type )
    {
        composedPredicate = new HasEqualType<T>( type ).or( new HasAssignableToType<>( type ) );
    }

    public HasEqualOrAssignableToType( T hasTypes )
    {
        composedPredicate = new HasEqualType<>( hasTypes ).or( new HasAssignableToType<>( hasTypes ) );
    }

    @Override
    public boolean test( T hasTypes )
    {
        return composedPredicate.test( hasTypes );
    }
}
