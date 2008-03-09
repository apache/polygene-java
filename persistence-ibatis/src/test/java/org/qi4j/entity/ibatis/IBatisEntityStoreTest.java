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
import java.util.Map;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.PropertyDeclaration;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerInfo;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.entity.StoreException;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.service.ServiceInstanceProvider;
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

    private static final String DEFAULT_FIRST_NAME = "Edward";
    private static final String DEFAULT_LAST_NAME = "Yakop";

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
     * Test {@link IBatisEntityStore#exists(String, org.qi4j.spi.composite.CompositeBinding)}.
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
        PersonComposite composite = moduleInstance.getCompositeBuilderFactory().newComposite( PersonComposite.class );
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
        HashMap<Class, Object> infos = new HashMap<Class, Object>();

        Class<? extends IBatisEntityStoreTest> aClass = getClass();
        URL sqlMapConfigURL = aClass.getResource( SQL_MAP_CONFIG_XML );
        String sqlMapConfigURLAsString = sqlMapConfigURL.toString();

        IBatisEntityStoreServiceInfo batisEntityStoreServiceInfo =
            new IBatisEntityStoreServiceInfo( sqlMapConfigURLAsString );
        infos.put( IBatisEntityStoreServiceInfo.class, batisEntityStoreServiceInfo );
        infos.put( DBInitializerInfo.class, newDbInitializerInfo() );

        return new ServiceDescriptor( IBatisEntityStoreTest.class, ServiceInstanceProvider.class, module, infos );
    }

    /**
     * Tests {@link IBatisEntityStore#computePropertyValue(PropertyBinding, Map, boolean)}.
     *
     * @since 0.1.0
     */
    public final void testComputePropertyValue()
    {
        ServiceDescriptor descriptor = newValidServiceDescriptor();
        IBatisEntityStore entityStore = new IBatisEntityStore( descriptor );

        // Set up test arguments
        PersonComposite composite = moduleInstance.getCompositeBuilderFactory().newComposite( PersonComposite.class );
        CompositeBinding personBinding = runtime.getCompositeBinding( composite );
        Iterable<PropertyBinding> propertyBindings = personBinding.getPropertyBindings();
        PropertyBinding binding = null;
        String propertyName = "firstName";
        for( PropertyBinding aBinding : propertyBindings )
        {
            String tempPropertyName = aBinding.getName();
            if( propertyName.equals( tempPropertyName ) )
            {
                binding = aBinding;
                break;
            }
        }
        Map<String, Object> propertyValues = new HashMap<String, Object>();

        // ****************************
        // Test to return default value
        // ****************************
        Object testValue1 = entityStore.computePropertyValue( binding, propertyValues, true );
        assertEquals( DEFAULT_FIRST_NAME, testValue1 );

        // Test to return null, because empty property values and use default value to false
        Object testValue2 = entityStore.computePropertyValue( binding, propertyValues, false );
        assertNull( testValue2 );

        // *****************************
        // Test to return assigned value
        // *****************************
        String expectedValue3 = "value3";
        propertyValues.put( propertyName, expectedValue3 );
        Object testValue3 = entityStore.computePropertyValue( binding, propertyValues, true );
        assertEquals( expectedValue3, testValue3 );

        // ***********************
        // Test with debug mode on
        // ***********************
        HashMap<Class, Object> infos = new HashMap<Class, Object>();
        Class<? extends IBatisEntityStoreTest> aClass = getClass();
        URL sqlMapConfigURL = aClass.getResource( SQL_MAP_CONFIG_XML );
        String sqlMapConfigURLAsString = sqlMapConfigURL.toString();
        IBatisEntityStoreServiceInfo entityStoreServiceInfo =
            new IBatisEntityStoreServiceInfo( sqlMapConfigURLAsString );
        entityStoreServiceInfo.setIsDebugMode( true );
        infos.put( IBatisEntityStoreServiceInfo.class, entityStoreServiceInfo );
        ServiceDescriptor descriptor2 = new ServiceDescriptor(
            IBatisEntityStoreTest.class, ServiceInstanceProvider.class, module, infos );
        IBatisEntityStore entityStore2 = new IBatisEntityStore( descriptor2 );
        propertyValues.put( propertyName, 2 );

        String failMsg = "Must [fail]. Mismatch between expected (String) and actual (Integer) property value type.";
        try
        {
            entityStore2.computePropertyValue( binding, propertyValues, false );
            fail( failMsg );
        }
        catch( IllegalStateException e )
        {
            // Expected
        }
        catch( Exception e )
        {
            fail( failMsg );
        }
    }

    @Override
    public final void configure( ModuleAssembly aModule )
    {
        String testName = getName();
        if( "testComputePropertyValue".equals( testName ) ||
            "testExists".equals( testName ) )
        {
            aModule.addComposites( PersonComposite.class );

            if( "testComputePropertyValue".equals( testName ) )
            {
                PropertyDeclaration hasFirstNameDeclaration = aModule.addProperty();
                HasFirstName firstName = hasFirstNameDeclaration.withAccessor( HasFirstName.class );
                firstName.firstName().set( DEFAULT_FIRST_NAME );

                PropertyDeclaration hasLastNameDeclaration = aModule.addProperty();
                HasLastName lastName = hasLastNameDeclaration.withAccessor( HasLastName.class );
                lastName.lastName().set( DEFAULT_LAST_NAME );
            }
        }
    }

    protected final boolean isDerbyServerShouldBeStarted()
    {
        String testName = getName();
        return "testActivate".equals( testName ) || "testExists".equals( testName );
    }
}