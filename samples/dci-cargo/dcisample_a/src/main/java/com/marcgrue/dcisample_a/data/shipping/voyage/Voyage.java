package com.marcgrue.dcisample_a.data.shipping.voyage;

import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;

/**
 * A voyage is a ship, train, flight etc carrying a cargo from one location
 * to another. A {@link Schedule} describes the route it takes.
 *
 * A cargo can be loaded onto part of, or the whole voyage.
 *
 * All properties are mandatory and immutable.
 */
public interface Voyage
{
    Property<VoyageNumber> voyageNumber();

    Property<Schedule> schedule();
}
