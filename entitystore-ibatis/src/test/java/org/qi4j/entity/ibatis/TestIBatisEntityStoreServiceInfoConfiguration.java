package org.qi4j.entity.ibatis;

import org.qi4j.service.Configuration;

/**
 * @autor Michael Hunger
 * @since 18.05.2008
 */
class TestIBatisEntityStoreServiceInfoConfiguration implements Configuration<IBatisEntityStoreServiceInfo>
{
    private final IBatisEntityStoreServiceInfo config;

    public TestIBatisEntityStoreServiceInfoConfiguration( final IBatisEntityStoreServiceInfo config )
    {
        this.config = config;
    }

    public IBatisEntityStoreServiceInfo configuration()
    {
        return config;
    }

    public void refresh()
    {
    }
}
