package org.qi4j.api.query.grammar;

import org.qi4j.api.composite.Composite;
import org.qi4j.functional.Iterables;

import java.util.Collection;

/**
* TODO
*/
public class ContainsAllSpecification<T>
        extends ExpressionSpecification
{
    private PropertyFunction<? extends Collection<T>> collectionProperty;
    private Iterable<T> valueCollection;

    public ContainsAllSpecification( PropertyFunction<? extends Collection<T>> collectionProperty, Iterable<T> valueCollection )
    {
        this.collectionProperty = collectionProperty;
        this.valueCollection = valueCollection;
    }

    public PropertyFunction<? extends Collection<T>> getCollectionProperty()
    {
        return collectionProperty;
    }

    public Iterable<T> getValueCollection()
    {
        return valueCollection;
    }

    @Override
    public boolean satisfiedBy( Composite item )
    {
        Collection<T> collection = collectionProperty.map( item ).get();

        if (collection == null)
            return false;

        for( T value : valueCollection )
        {
            if (!collection.contains( value ))
                return false;
        }

        return true;
    }

    @Override
    public String toString()
    {
        return collectionProperty + " contains "+ Iterables.toList( valueCollection );
    }
}
