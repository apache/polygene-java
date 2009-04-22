package org.qi4j.library.constraints;

import java.util.regex.Pattern;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.library.constraints.annotation.Matches;

/**
 * JAVADOC
 */
public class MatchesConstraint
    implements Constraint<Matches, String>
{
    public boolean isValid( Matches annotation, String argument )
    {
        if( argument != null )
        {
            Pattern pattern = Pattern.compile( annotation.value() );
            return pattern.matcher( argument ).matches();
        }

        return false;
    }
}
