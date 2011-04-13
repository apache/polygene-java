package org.qi4j.library.alarm;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;

/**
 * Alarm Names must contain characters. The default is 5 characters but can be overridden. It must also
 * not only contain white spaces.
 */
@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Constraints( AlarmNameConstraint.class )
public @interface AlarmName
{
    int length() default 5;
}
