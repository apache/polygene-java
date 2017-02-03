package org.apache.polygene.api.type;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class HasAssignableToType<T extends HasTypes> extends HasTypesPredicate<T>
{
    public HasAssignableToType( Type type )
    {
        super( Collections.singletonList( type ) );
    }

    public HasAssignableToType( T hasTypes )
    {
        super( hasTypes.types().collect( toList() ) );
    }

    @Override
    protected Predicate<Type> matchPredicate( Type candidate )
    {
        // TODO; what to do if there is ParameterizedType here??
        // Now set to ClassCastException and see if anything surfaces
        Class<?> clazz = (Class<?>) candidate;
        return input -> !input.equals( candidate ) && ( (Class<?>) input ).isAssignableFrom( clazz );
    }
}
