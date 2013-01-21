package org.qi4j.api.query.grammar;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;

/**
 * Property null Specification.
 */
public class PropertyNullSpecification<T>
    extends ExpressionSpecification
{
    private PropertyFunction<T> property;

    public PropertyNullSpecification( PropertyFunction<T> property )
    {
        this.property = property;
    }

    public PropertyFunction<T> property()
    {
        return property;
    }

    @Override
    public boolean satisfiedBy( Composite item )
    {
        Property<T> prop = property.map( item );

        if( prop == null )
        {
            return true;
        }

        return prop.get() == null;
    }

    @Override
    public String toString()
    {
        return property.toString() + "is null";
    }
}
