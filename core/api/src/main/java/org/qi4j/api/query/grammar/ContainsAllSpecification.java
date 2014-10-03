package org.qi4j.api.query.grammar;

import java.util.Collection;
import java.util.function.Predicate;
import org.qi4j.api.composite.Composite;
import org.qi4j.functional.Iterables;

/**
 * Contains All Specification.
 */
public class ContainsAllSpecification<T>
    implements Predicate<Composite>
{
    private PropertyFunction<? extends Collection<T>> collectionProperty;
    private Iterable<T> valueCollection;

    public ContainsAllSpecification( PropertyFunction<? extends Collection<T>> collectionProperty,
                                     Iterable<T> valueCollection
    )
    {
        this.collectionProperty = collectionProperty;
        this.valueCollection = valueCollection;
    }

    public PropertyFunction<? extends Collection<T>> collectionProperty()
    {
        return collectionProperty;
    }

    public Iterable<T> containedValues()
    {
        return valueCollection;
    }

    @Override
    public boolean test( Composite item )
    {
        Collection<T> collection = collectionProperty.apply( item ).get();

        if( collection == null )
        {
            return false;
        }

        for( T value : valueCollection )
        {
            if( !collection.contains( value ) )
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString()
    {
        return collectionProperty + " contains " + Iterables.toList( valueCollection );
    }
}
