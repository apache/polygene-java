package org.qi4j.samples.cargo.app1.system;

import org.qi4j.api.constraint.Constraint;

import java.util.Date;

/**
 *
 */
public class FutureDateConstraint
        implements Constraint<FutureDate, Date> {

    public boolean isValid(FutureDate date, Date value) {
        return value.getTime() > System.currentTimeMillis();
    }
}