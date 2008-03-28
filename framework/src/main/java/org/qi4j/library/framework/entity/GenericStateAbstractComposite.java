package org.qi4j.library.framework.entity;

import org.qi4j.composite.Mixins;

/**
 * Declares state management mixins
 */
@Mixins( { PropertyMixin.class, AssociationMixin.class } )
public interface GenericStateAbstractComposite
{
}
