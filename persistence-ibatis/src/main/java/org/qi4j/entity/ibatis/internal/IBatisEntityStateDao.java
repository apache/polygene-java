package org.qi4j.entity.ibatis.internal;

import org.qi4j.spi.composite.CompositeBinding;

/**
 * TODO
 *
 * @author edward.yakop@gmail.com
 */
public interface IBatisEntityStateDao
{
    /**
     * TODO
     */
    boolean deleteComposite( String aCompositeIdentity, CompositeBinding aCompositeBinding );
}
