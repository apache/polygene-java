package org.qi4j.api.query.grammar;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;

/**
* TODO
*/
public abstract class ComparisonSpecification<T>
    extends ExpressionSpecification
{
    protected final PropertyFunction<T> property;
    protected final T value;

    public ComparisonSpecification( PropertyFunction<T> property, T value )
    {
        this.property = property;
        this.value = value;
    }

    public PropertyFunction<T> getProperty()
    {
        return property;
    }

    @Override
    public final boolean satisfiedBy( Composite item )
    {
        try
        {
            Property<T> prop = property.map( item );

            if (prop == null)
                return false;

            T value = prop.get();
            if (value == null)
                return false;

            return compare(value);
        } catch( IllegalArgumentException e )
        {
            return false;
        }
    }

    protected abstract boolean compare(T value);

    public T getValue()
    {
        return value;
    }
}
