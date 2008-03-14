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

import java.io.Serializable;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerInfo;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.entity.StoreException;
import org.qi4j.spi.service.provider.DefaultServiceInstanceProvider;
import org.qi4j.spi.structure.ServiceDescriptor;
import static org.qi4j.spi.structure.Visibility.module;

/**
 * {@code IBatisEntityStoreTest} tests {@code IBatisEntityStore}.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
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

    /**
     * Construct a new entity store and activates it.
     *
     * @return A new entity store.
     * @since 0.1.0
     */
    private IBatisEntityStore newAndActivateEntityStore()
    {
        ServiceDescriptor descriptor = newValidServiceDescriptor();
        IBatisEntityStore entityStore = new IBatisEntityStore( descriptor );
        try
        {
            entityStore.activate();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Activation with valid configuration must succeed." );
        }

        return entityStore;
    }

    /**
     * Test {@link IBatisEntityStore#exists(String,CompositeBinding)}.
     *
     * @throws SQLException Thrown if failed.
     */
    public final void testExists()
        throws SQLException
    {
        // Initialize the derby and entity store
        initializeDerby();
        IBatisEntityStore entityStore = newAndActivateEntityStore();

        // Intialize test arguments
        PersonComposite composite = moduleInstance.getStructureContext().getCompositeBuilderFactory().newComposite( PersonComposite.class );
        CompositeBinding personBinding = runtime.getCompositeBinding( composite );

        // **********************
        // Test invalid arguments
        // **********************
        String failMsg = "Invoke with invalid arguments. Must throw an [IllegalArgumentException]";
        try
        {
            entityStore.exists( null, null );
            fail( failMsg );
        }
        catch( IllegalArgumentException e )
        {
            // Expected
        }
        catch( StoreException e )
        {
            fail( failMsg );
        }

        try
        {
            entityStore.exists( null, personBinding );
            fail( failMsg );
        }
        catch( IllegalArgumentException e )
        {
            // Expected
        }
        catch( StoreException e )
        {
            fail( failMsg );
        }

        // *************************
        // Test with valid arguments
        // *************************
        try
        {
            boolean isExist = entityStore.exists( "1", personBinding );
            assertTrue( isExist );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Check must exists must throw any exception." );
        }

        try
        {
            boolean isExist = entityStore.exists( "3", personBinding );
            assertFalse( isExist );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Check must exists must throw any exception." );
        }
    }

    /**
     * Construct a new valid service descriptor.
     *
     * @return a new valid service descriptor.
     * @since 0.1.0
     */
    private ServiceDescriptor newValidServiceDescriptor()
    {
        HashMap<Class, Serializable> infos = new HashMap<Class, Serializable>();

        Class<? extends IBatisEntityStoreTest> aClass = getClass();
        URL sqlMapConfigURL = aClass.getResource( SQL_MAP_CONFIG_XML );
        String sqlMapConfigURLAsString = sqlMapConfigURL.toString();

        IBatisEntityStoreServiceInfo batisEntityStoreServiceInfo =
            new IBatisEntityStoreServiceInfo( sqlMapConfigURLAsString );
        infos.put( IBatisEntityStoreServiceInfo.class, batisEntityStoreServiceInfo );
        infos.put( DBInitializerInfo.class, newDbInitializerInfo() );

        return new ServiceDescriptor( IBatisEntityStore.class, DefaultServiceInstanceProvider.class, "ibatis", module, true, infos );
    }

    @Override
    public final void assemble( ModuleAssembly aModule )
        throws AssemblyException
    {
        String testName = getName();
        if( "testExists".equals( testName ) )
        {
            aModule.addComposites( PersonComposite.class );
        }
    }

    protected final boolean isDerbyServerShouldBeStarted()
    {
        String testName = getName();
        return "testActivate".equals( testName ) || "testExists".equals( testName );
    }
}