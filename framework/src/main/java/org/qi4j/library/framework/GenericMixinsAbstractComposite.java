package org.qi4j.library.framework;

import org.qi4j.api.annotation.Mixins;
import org.qi4j.library.framework.properties.PropertiesMixin;
import org.qi4j.library.framework.scripting.JavaScriptMixin;

/**
 * Adds all the various generic mixins that can be used to implement
 * Composite methods.
 */
@Mixins( { PropertiesMixin.class, JavaScriptMixin.class, FinderMixin.class, RMIMixin.class, NoopMixin.class } )
public interface GenericMixinsAbstractComposite
{
}
