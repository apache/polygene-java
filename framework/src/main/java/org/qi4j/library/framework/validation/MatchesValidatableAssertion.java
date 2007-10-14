package org.qi4j.library.framework.validation;

import java.util.regex.Pattern;
import org.qi4j.library.framework.validation.annotation.Matches;

/**
 * TODO
 */
public class MatchesValidatableAssertion
    extends AbstractAnnotationValidatableAssertion<Matches, String>
{
    protected boolean isValid( Matches annotation, String argument )
    {
        Pattern pattern = Pattern.compile( annotation.value() );
        return pattern.matcher( argument ).matches();
    }
}
