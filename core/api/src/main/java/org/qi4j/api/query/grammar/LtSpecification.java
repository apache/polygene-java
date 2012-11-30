package org.qi4j.api.query.grammar;

/**
 * Lesser than Specification.
 */
public class LtSpecification<T>
    extends ComparisonSpecification<T>
{
    public LtSpecification( PropertyFunction<T> property, T value )
    {
        super( property, value );
    }

    @Override
    protected boolean compare( T value )
    {
        return ( (Comparable) value ).compareTo( this.value ) < 0;
    }

    @Override
    public String toString()
    {
        return property.toString() + "<" + value.toString();
    }
}
