package org.qi4j.manual.recipes.createConstraint;

import org.qi4j.api.constraint.ConstraintDeclaration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// START SNIPPET: annotation
@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.METHOD } )
public @interface PhoneNumber
{
}
// END SNIPPET: annotation
