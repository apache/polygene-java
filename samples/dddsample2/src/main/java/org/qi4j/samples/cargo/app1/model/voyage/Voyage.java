package org.qi4j.samples.cargo.app1.model.voyage;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;

/**
 *
 */
public interface Voyage extends EntityComposite
{

    Property<VoyageNumber> voyageNumber();

    Property<Schedule> schedule();
}