package org.qi4j.library.constraints.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.qi4j.api.constraint.ConstraintDeclaration;

/**
 * Marks a property as being a string, non null, valid email.
 */
@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@NotEmpty
@Contains( "@" )
public @interface Email
{
}
