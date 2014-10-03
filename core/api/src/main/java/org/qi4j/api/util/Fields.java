package org.qi4j.api.util;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.qi4j.functional.Iterables;

import static org.qi4j.functional.Iterables.iterable;

/**
 * Useful methods for handling Fields.
 */
public final class Fields
{
    public static final BiFunction<Class<?>, String, Field> FIELD_NAMED = new BiFunction<Class<?>, String, Field>()
    {
        @Override
        public Field apply( Class<?> aClass, String name )
        {
            return Iterables.first( Iterables.filter( Classes.memberNamed( name ), FIELDS_OF.apply( aClass ) ) );
        }
    };

    public static final Function<Type, Iterable<Field>> FIELDS_OF = Classes.forClassHierarchy( new Function<Class<?>, Iterable<Field>>()
    {
        @Override
        public Iterable<Field> apply( Class<?> type )
        {
            return iterable( type.getDeclaredFields() );
        }
    } );
}
