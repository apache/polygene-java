/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entity.ibatis;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerInfo;
import org.qi4j.spi.service.ServiceInstanceProvider;
import org.qi4j.spi.structure.ServiceDescriptor;
import static org.qi4j.spi.structure.Visibility.module;

/**
 * {@code IBatisEntityStoreTest} tests {@code IBatisEntityStore}.
 *
 * @author edward.yakop@gmail.com
 */
public final class IBatisEntityStoreTest extends AbstractTestCase
{
    private static final String SQL_MAP_CONFIG_XML = "SqlMapConfig.xml";

    /**
     * Test constructor.
     *
     * @since 0.1.0
     */
    public final void testConstructor()
    {
        try
        {
            new IBatisEntityStore( null );
            fail( "Service descriptor is [null]. Must throw [IllegalArgumentException]." );
        }
        catch( IllegalArgumentException e )
        {
            // Expected
        }
        catch( Exception e )
        {
            fail( "Service descriptor is [null]. Must throw [IllegalArgumentException]." );
        }

        ServiceDescriptor descriptor = newValidServiceDescriptor();
        try
        {
            new IBatisEntityStore( descriptor );
        }
        catch( Exception e )
        {
            fail( "Constructing with valid argument must not throw any exception." );
        }
    }

    /**
     * Test activate.
     *
     * @throws SQLException Thrown if checking initialization failed.
     * @since 0.1.0
     */
    public final void testActivate()
        throws SQLException
    {
        initializeDerby();
        newAndActivateEntityStore();

        // Make sure there's default data in database
        checkDataInitialization();
    }

    private IBatisEntityStore newAndActivateEntityStore()
    {
        // Test activation with valid descriptor
        ServiceDescriptor descriptor = newValidServiceDescriptor();
        IBatisEntityStore iBatisEntityStore = new IBatisEntityStore( descriptor );
        try
        {
            iBatisEntityStore.activate();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Activation with valid configuration must succeed." );
        }

        return iBatisEntityStore;
    }

    /**
     * Test {@link IBatisEntityStore#exists(String, org.qi4j.spi.composite.CompositeBinding)}.
     *
     * @throws SQLException Thrown if failed.
     */
    public final void testExists()
        throws SQLException
    {
        // Initialize the derby and entity store
        initializeDerby();
        IBatisEntityStore iBatisEntityStore = newAndActivateEntityStore();

        try
        {
            boolean isExist = iBatisEntityStore.exists( "1", null );
            assertTrue( isExist );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Check must exists must throw any exception." );
        }

        try
        {
            boolean isExist = iBatisEntityStore.exists( "3", null );
            assertFalse( isExist );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Check must exists must throw any exception." );
        }
    }

    private ServiceDescriptor newValidServiceDescriptor()
    {
        HashMap<Class, Object> infos = new HashMap<Class, Object>();

        Class<? extends IBatisEntityStoreTest> aClass = getClass();
        URL sqlMapConfigURL = aClass.getResource( SQL_MAP_CONFIG_XML );
        String sqlMapConfigURLAsString = sqlMapConfigURL.toString();

        IBatisEntityStoreServiceInfo batisEntityStoreServiceInfo =
            new IBatisEntityStoreServiceInfo( sqlMapConfigURLAsString );
        infos.put( DBInitializerInfo.class, newDbInitializerInfo() );
        infos.put( IBatisEntityStoreServiceInfo.class, batisEntityStoreServiceInfo );

        return new ServiceDescriptor( IBatisEntityStoreTest.class, ServiceInstanceProvider.class, module, infos );
    }

    protected final boolean isDerbyServerShouldBeStarted()
    {
        String testName = getName();
        return "testActivate".equals( testName ) || "testExists".equals( testName );
    }
}
