package org.qi4j.api.query.grammar;

/**
 * Greater than Specification.
 */
public class GtSpecification<T>
    extends ComparisonSpecification<T>
{
    public GtSpecification( PropertyFunction<T> property, T value )
    {
        super( property, value );
    }

    @Override
    protected boolean compare( T value )
    {
        return ( (Comparable) value ).compareTo( this.value ) > 0;
    }

    @Override
    public String toString()
    {
        return property.toString() + ">" + value.toString();
    }
}
