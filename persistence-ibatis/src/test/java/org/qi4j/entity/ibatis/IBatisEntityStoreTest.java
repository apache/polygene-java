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
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerInfo;
import org.qi4j.entity.ibatis.internal.property.MutablePropertyInstance;
import org.qi4j.property.Property;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.entity.StoreException;
import org.qi4j.spi.property.ImmutablePropertyInstance;
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
     * Test {@link IBatisEntityStore#exists(String, CompositeBinding)}.
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
        Map<String, PropertyBinding> personPropertyBindings = getPersonCompositePropertyBindings();
        String firstNamePropertyName = "firstName";
        PropertyBinding binding = personPropertyBindings.get( firstNamePropertyName );
        assertNotNull( "Property [firstName] must exists.", binding );

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
        propertyValues.put( firstNamePropertyName, expectedValue3 );
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
        propertyValues.put( firstNamePropertyName, 2 );

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

    /**
     * Returns all person composite property bindings.
     *
     * @return All person composite property bindings.
     * @since 0.1.0
     */
    private Map<String, PropertyBinding> getPersonCompositePropertyBindings()
    {
        CompositeBuilderFactory builderFactory = moduleInstance.getCompositeBuilderFactory();
        PersonComposite composite = builderFactory.newComposite( PersonComposite.class );
        CompositeBinding personBinding = runtime.getCompositeBinding( composite );
        Iterable<PropertyBinding> propertyBindings = personBinding.getPropertyBindings();
        Map<String, PropertyBinding> properties = new HashMap<String, PropertyBinding>();
        for( PropertyBinding aBinding : propertyBindings )
        {
            String propertyName = aBinding.getName();
            properties.put( propertyName, aBinding );
        }
        assertFalse( "Properties must not be empty.", properties.isEmpty() );
        return properties;
    }

    /**
     * Tests new property instance.
     *
     * @since 0.1.0
     */
    public final void testNewPropertyInstance()
    {
        ServiceDescriptor descriptor = newValidServiceDescriptor();
        IBatisEntityStore entityStore = new IBatisEntityStore( descriptor );

        // Set up test arguments
        Map<String, PropertyBinding> properties = getPersonCompositePropertyBindings();

        // ***********************
        // Test immutable property
        // ***********************
        PropertyBinding identityBinding = properties.get( "identity" );
        assertNotNull( "Property binding [identity] must exists.", identityBinding );
        String expectedValue1 = "anIdentityValue";
        Property<Object> identityProperty = entityStore.newPropertyInstance( identityBinding, expectedValue1 );
        assertNotNull( identityProperty );
        assertTrue( ImmutablePropertyInstance.class.equals( identityProperty.getClass() ) );
        assertEquals( expectedValue1, identityProperty.get() );

        // *********************
        // Test mutable property
        // *********************
        PropertyBinding firstNameBinding = properties.get( "firstName" );
        assertNotNull( "Property binding [firstName] must exists.", firstNameBinding );
        String expectedValue2 = "Edward";
        Property<Object> firstNameProperty = entityStore.newPropertyInstance( firstNameBinding, expectedValue2 );
        assertNotNull( firstNameProperty );
        assertTrue( MutablePropertyInstance.class.equals( firstNameProperty.getClass() ) );
        assertEquals( expectedValue2, firstNameProperty.get() );
    }

    /**
     * Tests {@link IBatisEntityStore#transformToProperties(CompositeBinding, Map, boolean)}.
     *
     * @since 0.1.0
     */
    public final void testTransformToProperties()
    {
        ServiceDescriptor descriptor = newValidServiceDescriptor();
        IBatisEntityStore entityStore = new IBatisEntityStore( descriptor );

        CompositeBuilderFactory builderFactory = moduleInstance.getCompositeBuilderFactory();
        PersonComposite composite = builderFactory.newComposite( PersonComposite.class );
        CompositeBinding personBinding = runtime.getCompositeBinding( composite );

        String[] propertyQualifiedNames = new String[]{
            "org.qi4j.entity.Identity:identity",
            "org.qi4j.entity.ibatis.HasFirstName:firstName",
            "org.qi4j.entity.ibatis.HasLastName:lastName",
        };

        // *******************************************************
        // Test with empty property values
        // *******************************************************
        HashMap<String, Object> propertyValues = new HashMap<String, Object>();

        // ----------------------------------
        // Test with default value turned off
        // ----------------------------------
        Map<String, Property> properties1 = entityStore.transformToProperties( personBinding, propertyValues, false );

        String[] expectedValues1 = new String[]{
            null, null, null
        };
        testPropertiesMap( properties1, propertyQualifiedNames, expectedValues1 );

        // ---------------------------------
        // Test with default value turned on
        // ---------------------------------
        Map<String, Property> properties2 = entityStore.transformToProperties( personBinding, propertyValues, true );
        String[] expectedValues2 = new String[]{
            null, DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME
        };
        testPropertiesMap( properties2, propertyQualifiedNames, expectedValues2 );

        // *********************************
        // Test with initial property values
        // *********************************

        // --------------------------------------------------------------
        // Test with partial property values and default value turned off
        // --------------------------------------------------------------
        String identityExpectedValue3 = "1";
        String firstNameExpected3 = "Niclas";
        propertyValues.put( "identity", identityExpectedValue3 );
        propertyValues.put( "firstName", firstNameExpected3 );

        Map<String, Property> properties3 = entityStore.transformToProperties( personBinding, propertyValues, false );
        String[] expectedValues3 = new String[]{
            identityExpectedValue3, firstNameExpected3, null
        };
        testPropertiesMap( properties3, propertyQualifiedNames, expectedValues3 );

        // -------------------------------------------------------------
        // Test with partial property values and default value turned on
        // -------------------------------------------------------------
        Map<String, Property> properties4 = entityStore.transformToProperties( personBinding, propertyValues, true );
        String[] expectedValues4 = new String[]{
            identityExpectedValue3, firstNameExpected3, DEFAULT_LAST_NAME
        };
        testPropertiesMap( properties4, propertyQualifiedNames, expectedValues4 );

        // -----------------------------------------------------------
        // Test with full property values and default value turned off
        // -----------------------------------------------------------
        String lastNameExpected5 = "Hedhman";
        propertyValues.put( "lastName", lastNameExpected5 );
        Map<String, Property> properties5 = entityStore.transformToProperties( personBinding, propertyValues, false );
        String[] expectedValues5 = new String[]{
            identityExpectedValue3, firstNameExpected3, lastNameExpected5
        };
        testPropertiesMap( properties5, propertyQualifiedNames, expectedValues5 );

        // -----------------------------------------------------------
        // Test with full property values and default value turned on
        // -----------------------------------------------------------
        Map<String, Property> properties6 = entityStore.transformToProperties( personBinding, propertyValues, false );
        testPropertiesMap( properties6, propertyQualifiedNames, expectedValues5 );
    }

    /**
     * Helper method to test properties map.
     *
     * @param aPropertiesMap         The properties map. This argument must not be {@code null}.
     * @param propertyQualifiedNames The expected property qualified names. This argument must not be {@code null}.
     * @param expectedValues         The expected values. This argument must not be {@code null}.
     * @since 0.1.0
     */
    private static void testPropertiesMap(
        Map<String, Property> aPropertiesMap, String[] propertyQualifiedNames, String[] expectedValues )
    {
        int numberProperties = propertyQualifiedNames.length;

        // **********************************
        // Test number of expected properties
        // **********************************
        int numberOfActualProperties = aPropertiesMap.size();
        assertEquals( "Number of properties must be [" + numberProperties + "]",
                      numberProperties, numberOfActualProperties );

        // *************************
        // Test each property values
        // *************************
        for( int i = 0; i < propertyQualifiedNames.length; i++ )
        {
            String propertyQualifiedName = propertyQualifiedNames[ i ];
            Property property = aPropertiesMap.get( propertyQualifiedName );
            assertNotNull( property );
            Object expectedValue = expectedValues[ i ];

            Object actualValue = property.get();
            assertEquals( "Property [" + propertyQualifiedName + "] must have [" + expectedValue + "] value.",
                          expectedValue, actualValue );
        }
    }

    @Override
    public final void configure( ModuleAssembly aModule )
    {
        String testName = getName();
        if( "testComputePropertyValue".equals( testName ) ||
            "testExists".equals( testName ) ||
            "testNewPropertyInstance".equals( testName ) ||
            "testTransformToProperties".equals( testName ) )
        {
            aModule.addComposites( PersonComposite.class );

            if( "testComputePropertyValue".equals( testName ) ||
                "testTransformToProperties".equals( testName ) )
            {
                PropertyDeclaration hasFirstNameDeclaration = aModule.addProperty();
                Property<String> firstName = hasFirstNameDeclaration.withAccessor( PersonComposite.class ).firstName();
                firstName.set( DEFAULT_FIRST_NAME );

                PropertyDeclaration hasLastNameDeclaration = aModule.addProperty();
                Property<String> lastName = hasLastNameDeclaration.withAccessor( PersonComposite.class ).lastName();
                lastName.set( DEFAULT_LAST_NAME );
            }
        }
    }

    protected final boolean isDerbyServerShouldBeStarted()
    {
        String testName = getName();
        return "testActivate".equals( testName ) || "testExists".equals( testName );
    }
}