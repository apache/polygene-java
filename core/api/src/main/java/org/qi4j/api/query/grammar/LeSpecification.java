package org.qi4j.api.query.grammar;

/**
 * Less or equals Specification.
 */
public class LeSpecification<T>
    extends ComparisonSpecification<T>
{
    public LeSpecification( PropertyFunction<T> property, T value )
    {
        super( property, value );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    protected boolean compare( T value )
    {
        return ( (Comparable) value ).compareTo( this.value ) <= 0;
    }

    @Override
    public String toString()
    {
        return property.toString() + "<=" + value.toString();
    }
}
