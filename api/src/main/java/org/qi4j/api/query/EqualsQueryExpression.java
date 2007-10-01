package org.qi4j.api.query;

/**
 * TODO
 */
public class EqualsQueryExpression
{
    PropertyExpression property;
    Object value;

    public EqualsQueryExpression( PropertyExpression property, Object value )
    {
        this.property = property;
        this.value = value;
    }

    public PropertyExpression getProperty()
    {
        return property;
    }

    public Object getValue()
    {
        return value;
    }
}
