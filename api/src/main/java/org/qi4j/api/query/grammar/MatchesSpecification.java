package org.qi4j.api.query.grammar;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;

/**
 * TODO
 */
public class MatchesSpecification
        extends ExpressionSpecification
{
    private PropertyFunction<String> property;
    private String regexp;

    public MatchesSpecification( PropertyFunction<String> property, String regexp )
    {
        this.property = property;
        this.regexp = regexp;
    }

    public PropertyFunction<String> getProperty()
    {
        return property;
    }

    public String getRegexp()
    {
        return regexp;
    }

    @Override
    public boolean satisfiedBy( Composite item )
    {
        Property<String> prop = property.map( item );

        if (prop == null)
            return false;

        String val = prop.get();

        if (val == null)
            return false;

        return val.matches( regexp );
    }


    @Override
    public String toString()
    {
        return new StringBuilder()
            .append( "( " )
            .append( property )
            .append( " matches " )
            .append( "\"" )
            .append( regexp )
            .append( "\"" )
            .append( " )" )
            .toString();
    }
}
