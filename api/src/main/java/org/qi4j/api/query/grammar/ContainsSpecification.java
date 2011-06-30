package org.qi4j.api.query.grammar;

import org.qi4j.api.composite.Composite;

import java.util.Collection;

/**
 * TODO
 */
public class ContainsSpecification<T>
        extends ExpressionSpecification
{
    private PropertyFunction<? extends Collection<T>> collectionProperty;
    private T value;

    public ContainsSpecification( PropertyFunction<? extends Collection<T>> collectionProperty, T value )
    {
        this.collectionProperty = collectionProperty;
        this.value = value;
    }

    public PropertyFunction<? extends Collection<T>> getCollectionProperty()
    {
        return collectionProperty;
    }

    public T getValue()
    {
        return value;
    }

    @Override
    public boolean satisfiedBy( Composite item )
    {
        Collection<T> collection = collectionProperty.map( item ).get();

        if (collection == null)
            return false;

        return collection.contains( value );
    }

    @Override
    public String toString()
    {
        return collectionProperty + " contains " + value;
    }
}
