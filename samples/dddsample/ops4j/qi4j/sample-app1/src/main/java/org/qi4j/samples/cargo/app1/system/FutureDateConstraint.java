package org.qi4j.samples.cargo.app1.system;

import java.util.Date;
import org.qi4j.api.constraint.Constraint;

/**
 *
 */
public class FutureDateConstraint
        implements Constraint<FutureDate, Date> {

    public boolean isValid(FutureDate date, Date value) {
        return value.getTime() > System.currentTimeMillis();
    }
}