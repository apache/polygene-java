package org.qi4j.entity.ibatis;

import org.qi4j.spi.composite.CompositeBinding;

/**
 * TODO
 *
 * @author edward.yakop@gmail.com
 */
interface IBatisEntityStateDao
{
    /**
     * TODO
     */
    boolean deleteComposite( String aCompositeIdentity, CompositeBinding aCompositeBinding );
}
