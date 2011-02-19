package org.qi4j.samples.cargo.app1.system;

import org.qi4j.api.constraint.Constraint;

/**
 *
 */
public class UnLocodeConstraint
        implements Constraint<UnLocode, String> {

    public boolean isValid( UnLocode code, String value) {
        return value.equals(value.toUpperCase()) && value.length() == 5;
    }
}