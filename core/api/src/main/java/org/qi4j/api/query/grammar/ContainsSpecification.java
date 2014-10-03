package org.qi4j.api.query.grammar;

import java.util.Collection;
import java.util.function.Predicate;
import org.qi4j.api.composite.Composite;

/**
 * Contains Specification.
 */
public class ContainsSpecification<T>
    implements Predicate<Composite>
{
    private PropertyFunction<? extends Collection<T>> collectionProperty;
    private T value;

    public ContainsSpecification( PropertyFunction<? extends Collection<T>> collectionProperty, T value )
    {
        this.collectionProperty = collectionProperty;
        this.value = value;
    }

    public PropertyFunction<? extends Collection<T>> collectionProperty()
    {
        return collectionProperty;
    }

    public T value()
    {
        return value;
    }

    @Override
    public boolean test( Composite item )
    {
        Collection<T> collection = collectionProperty.apply( item ).get();

        if( collection == null )
        {
            return false;
        }

        return collection.contains( value );
    }

    @Override
    public String toString()
    {
        return collectionProperty + " contains " + value;
    }
}
