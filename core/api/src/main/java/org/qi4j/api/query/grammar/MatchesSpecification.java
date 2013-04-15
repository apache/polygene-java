package org.qi4j.api.query.grammar;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;

/**
 * Regular expression match Specification.
 */
public class MatchesSpecification
    extends ExpressionSpecification
{
    private PropertyFunction<String> property;
    private Object value;

    public MatchesSpecification( PropertyFunction<String> property, String regexp )
    {
        this.property = property;
        this.value = regexp;
    }

    public MatchesSpecification( PropertyFunction<String> property, Variable variable )
    {
        this.property = property;
        this.value = variable;
    }

    public PropertyFunction<String> property()
    {
        return property;
    }

    public Object value()
    {
        return value;
    }

    public String regexp()
    {
        return ( String ) value;
    }

    @Override
    public boolean satisfiedBy( Composite item )
    {
        Property<String> prop = property.map( item );

        if( prop == null )
        {
            return false;
        }

        String val = prop.get();

        if( val == null )
        {
            return false;
        }

        return val.matches( ( String ) value );
    }

    @Override
    public String toString()
    {
        return new StringBuilder()
            .append( "( " )
            .append( property )
            .append( " matches " )
            .append( "\"" )
            .append( value )
            .append( "\"" )
            .append( " )" )
            .toString();
    }
}
