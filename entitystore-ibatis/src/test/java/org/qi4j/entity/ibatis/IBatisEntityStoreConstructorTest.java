package org.qi4j.entity.ibatis;

import org.junit.Test;
import org.qi4j.composite.NullArgumentException;

/**
 * @autor Michael Hunger
 * @since 18.05.2008
 */
public class IBatisEntityStoreConstructorTest
{
    @Test( expected = NullArgumentException.class )
    public final void testConstructorCallWithNull()
    {
        new IBatisEntityStore( null, null );
    }

    @Test public void testValidConstructorCall()
    {
        new IBatisEntityStore( new TestIBatisEntityStoreServiceInfoConfiguration(null), null );
    }

}
