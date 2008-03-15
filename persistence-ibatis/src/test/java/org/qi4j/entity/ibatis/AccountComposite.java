package org.qi4j.entity.ibatis;

import org.qi4j.composite.Mixins;
import org.qi4j.entity.EntityComposite;
import org.qi4j.library.framework.entity.AssociationMixin;
import org.qi4j.library.framework.entity.PropertyMixin;

/**
 * @author edward.yakop@gmail.com
 */
@Mixins( { PropertyMixin.class, AssociationMixin.class } )
public interface AccountComposite extends EntityComposite, Account
{
}
