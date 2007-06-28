package org.qi4j.library.general.model.composites;

import org.qi4j.api.Composite;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.library.general.model.State;
import org.qi4j.library.framework.properties.PropertiesMixin;

/**
 * This interface represents ValueObject of State
 */
@ImplementedBy( { PropertiesMixin.class } )
public interface StateComposite extends State, Composite
{
}
