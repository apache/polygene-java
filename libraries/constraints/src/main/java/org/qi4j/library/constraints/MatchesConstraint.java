package org.qi4j.library.constraints;

import java.util.regex.Pattern;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.library.constraints.annotation.Matches;

/**
 * Implement @Matches constraint.
 */
public class MatchesConstraint
    implements Constraint<Matches, String>
{

    private static final long serialVersionUID = 1L;

    @Override
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
