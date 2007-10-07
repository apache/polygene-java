package org.qi4j.library.general.model;

import org.qi4j.api.Composite;

/**
 * TODO
 */
public interface Aggregated
{
    Composite getAggregate();

    Composite getRootAggregate();
}
