package org.qi4j.library.constraints.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.qi4j.api.constraint.ConstraintDeclaration;

@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@NotEmpty
@Contains( "@" )
public @interface Email
{
}
