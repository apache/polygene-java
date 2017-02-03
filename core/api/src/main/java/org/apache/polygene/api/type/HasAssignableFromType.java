package org.apache.polygene.api.type;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class HasAssignableFromType<T extends HasTypes> extends HasTypesPredicate<T>
{
    public HasAssignableFromType( Type type )
    {
        super( Collections.singletonList( type ) );
    }

    public HasAssignableFromType( T hasTypes )
    {
        super( hasTypes.types().collect( toList() ) );
    }

    @Override
    protected Predicate<Type> matchPredicate( Type candidate )
    {
        // TODO; what to do if there is ParameterizedType here??
        // Now set to ClassCastException and see if anything surfaces
        //if( candidate instanceof Class )
        {
            Class<?> clazz = (Class<?>) candidate;
            return input -> !input.equals( candidate ) && clazz.isAssignableFrom( (Class<?>) input );
        }
        //return input -> input.equals( candidate );
    }
}
