package org.qi4j.entity.ibatis.test;

import org.qi4j.service.Configuration;
import org.qi4j.entity.ibatis.IBatisConfiguration;

/**
 * @autor Michael Hunger
 * @since 18.05.2008
 */
class TestIBatisEntityStoreServiceInfoConfiguration implements Configuration<IBatisConfiguration>
{
    private IBatisConfiguration config;

    TestIBatisEntityStoreServiceInfoConfiguration( final IBatisConfiguration config )
    {
        this.config = config;
    }

    public IBatisConfiguration configuration()
    {
        return config;
    }

    public void refresh()
    {
    }
}
