package org.apache.polygene.api.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.function.Predicate;

import static org.apache.polygene.api.util.Classes.interfacesOf;

public abstract class HasTypesPredicate<T extends HasTypes> implements Predicate<T>
{
    protected final List<Type> matchTypes;

    protected HasTypesPredicate( List<Type> types )
    {
        matchTypes = types;
    }

    @Override
    public final boolean test( T hasTypes )
    {
        for( Type matchType : matchTypes )
        {
            if( matchType instanceof Class )
            {
                if( hasTypes.types().anyMatch( matchPredicate( matchType ) ) )
                {
                    return true;
                }
            }
            else
            {
                if( matchType instanceof ParameterizedType )
                {
                    // Foo<Bar> check
                    // First check Foo
                    ParameterizedType parameterizedType = (ParameterizedType) matchType;
                    Type rawType = parameterizedType.getRawType();

                    if( hasTypes.types().anyMatch( matchPredicate( rawType ) ) )
                    {
                        // Then check Bar
                        if( interfacesOf( hasTypes.types() ).anyMatch( intf -> intf.equals( matchType ) ) )
                        {
                            return true;
                        }
                    }
                }
                else if( matchType instanceof WildcardType )
                {
                    return true;
                }
            }
        }
        return false;
    }

    protected abstract Predicate<Type> matchPredicate( Type candidate );
}
