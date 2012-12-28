package org.qi4j.api.query.grammar;

/**
 * Not equals Specification.
 */
public class NeSpecification<T>
    extends ComparisonSpecification<T>
{
    public NeSpecification( PropertyFunction<T> property, T value )
    {
        super( property, value );
    }

    @Override
    protected boolean compare( T value )
    {
        return !value.equals( this.value );
    }

    @Override
    public String toString()
    {
        return property.toString() + "!=" + value.toString();
    }
}
