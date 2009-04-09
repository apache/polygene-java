package org.qi4j.library.constraints.annotation;

import org.qi4j.api.constraint.ConstraintDeclaration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@NotEmpty
@Contains( "@" )
public @interface Email
{
}
