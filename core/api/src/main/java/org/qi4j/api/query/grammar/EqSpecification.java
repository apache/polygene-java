package org.qi4j.api.query.grammar;

/**
 * Equals Specification.
 */
public class EqSpecification<T>
    extends ComparisonSpecification<T>
{
    public EqSpecification( PropertyFunction<T> property, T value )
    {
        super( property, value );
    }

    @Override
    protected boolean compare( T value )
    {
        return value.equals( this.value );
    }

    @Override
    public String toString()
    {
        return property.toString() + "=" + value.toString();
    }
}
