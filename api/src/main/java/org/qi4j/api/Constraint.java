package org.qi4j.api;

import java.lang.annotation.Annotation;

/**
 * All constraints must implement this interface, which is used for each
 * parameter validation.
 */
public interface Constraint<A extends Annotation, P>
{
    boolean isValid( A annotation, P parameter )
        throws NullPointerException;
}
