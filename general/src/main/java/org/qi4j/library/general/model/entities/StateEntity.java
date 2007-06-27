package org.qi4j.library.general.model.entities;

import org.qi4j.api.persistence.Identity;
import org.qi4j.library.general.model.State;

/**
 * This interface represents entity of a StateEntity whose values are stored in
 * {@link org.qi4j.library.general.model.State} and is identifiable using
 * {@link org.qi4j.api.persistence.Identity}.
 */
public interface StateEntity extends State, Identity
{
}
