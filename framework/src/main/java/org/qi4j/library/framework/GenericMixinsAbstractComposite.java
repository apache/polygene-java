package org.qi4j.library.framework;

import org.qi4j.composite.Mixins;
import org.qi4j.library.framework.entity.AssociationMixin;
import org.qi4j.library.framework.entity.PropertyMixin;
import org.qi4j.library.framework.properties.PropertiesMixin;
import org.qi4j.library.framework.scripting.JavaScriptMixin;

/**
 * Adds all the various generic mixins that can be used to implement
 * Composite methods.
 */
@Mixins( { PropertyMixin.class, AssociationMixin.class, PropertiesMixin.class, JavaScriptMixin.class, FinderMixin.class, RMIMixin.class, NoopMixin.class } )
public interface GenericMixinsAbstractComposite
{
}
