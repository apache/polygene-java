package org.qi4j.api.query.grammar;

import java.util.function.Predicate;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;

/**
 * Property null Specification.
 */
public class PropertyNullSpecification<T>
    implements Predicate<Composite>
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
    public boolean test( Composite item )
    {
        Property<T> prop = property.apply( item );

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
