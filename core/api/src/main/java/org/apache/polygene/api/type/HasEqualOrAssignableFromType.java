package org.apache.polygene.api.type;

import java.lang.reflect.Type;
import java.util.function.Predicate;

public class HasEqualOrAssignableFromType<T extends HasTypes> implements Predicate<T>
{
    private final Predicate<T> composedPredicate;

    public HasEqualOrAssignableFromType( Type type )
    {
        composedPredicate = new HasEqualType<T>( type ).or( new HasAssignableFromType<>( type ) );
    }

    public HasEqualOrAssignableFromType( T hasTypes )
    {
        composedPredicate = new HasEqualType<>( hasTypes ).or( new HasAssignableFromType<>( hasTypes ) );
    }

    @Override
    public boolean test( T hasTypes )
    {
        return composedPredicate.test( hasTypes );
    }
}
