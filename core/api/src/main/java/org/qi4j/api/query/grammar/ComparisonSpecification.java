package org.qi4j.api.query.grammar;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;

/**
 * Base comparison Specification.
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

    public PropertyFunction<T> property()
    {
        return property;
    }

    @Override
    public final boolean satisfiedBy( Composite item )
    {
        try
        {
            Property<T> prop = property.map( item );

            if( prop == null )
            {
                return false;
            }

            T propValue = prop.get();
            if( propValue == null )
            {
                return false;
            }

            return compare( propValue );
        }
        catch( IllegalArgumentException e )
        {
            return false;
        }
    }

    protected abstract boolean compare( T value );

    public T value()
    {
        return value;
    }
}
