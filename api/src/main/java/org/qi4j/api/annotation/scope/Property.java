package org.qi4j.api.annotation.scope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to denote that a method returns a computed property for the object
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.METHOD } )
@Documented
@DependencyScope
public @interface Property
{
    @Name String value() default ""; // Name of the property. If not set then name will be name of getter method
}
