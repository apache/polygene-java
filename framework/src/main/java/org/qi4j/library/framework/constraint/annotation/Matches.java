package org.qi4j.library.framework.constraint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.qi4j.annotation.ConstraintDeclaration;

@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.PARAMETER, ElementType.ANNOTATION_TYPE } )
public @interface Matches
{
    String value();
}
