package org.qi4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.qi4j.annotation.scope.Name;

/**
 * Annotation to denote that a method returns or sets a property for the object
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.METHOD } )
@Documented
public @interface Property
{
    @Name String value() default ""; // Name of the property. If not set then name will be JavaBean name of getter method
}
