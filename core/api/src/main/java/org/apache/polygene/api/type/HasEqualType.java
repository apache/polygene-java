package org.apache.polygene.api.type;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class HasEqualType<T extends HasTypes> extends HasTypesPredicate<T>
{
    public HasEqualType( Type type )
    {
        super( Collections.singletonList( type ) );
    }

    public HasEqualType( T hasTypes )
    {
        super( hasTypes.types().collect( toList() ) );
    }

    @Override
    protected Predicate<Type> matchPredicate( Type candidate )
    {
        return candidate::equals;
    }
}
