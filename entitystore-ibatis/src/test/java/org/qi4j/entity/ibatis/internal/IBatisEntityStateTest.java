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
package org.qi4j.entity.ibatis.internal;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.entity.ibatis.entity.HasFirstName;
import org.qi4j.entity.ibatis.entity.HasLastName;
import org.qi4j.entity.ibatis.entity.PersonComposite;
import org.qi4j.entity.ibatis.test.AbstractTestCase;
import static org.qi4j.property.ComputedPropertyInstance.getQualifiedName;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.property.PropertyBinding;

/**
 * @author edward.yakop@gmail.com
 */
@Ignore
public final class IBatisEntityStateTest extends AbstractTestCase
{
    private static final String DEFAULT_FIRST_NAME = "Edward";
    private static final String DEFAULT_LAST_NAME = "Yakop";

    /**
     * Test constructor of entity state.
     */
    @SuppressWarnings( "unchecked" )
    @Test
    public void testGetProperty()
    {
        // =======================
        // Test with default value
        // =======================
        final Map<String, Object> initialValues1 = new HashMap<String, Object>();
        final EntityState personEntityState1 = newPersonEntityState( initialValues1 );

        // ----------
        // First name
        // ----------
        try
        {
            final Method firstNamePropertyAccessor = HasFirstName.class.getMethod( "firstName" );

            final String firstNameProperty = (String) personEntityState1.getProperty( getQualifiedName( firstNamePropertyAccessor ) );
            assertNotNull( firstNameProperty );

            assertEquals( DEFAULT_FIRST_NAME, firstNameProperty );

        }
        catch( NoSuchMethodException e )
        {
            fail( "HasFirstName must have [firstName] method." );
        }

        // ---------
        // Last name
        // ---------
        try
        {
            final Method lastNamePropertyAccessor = HasLastName.class.getMethod( "lastName" );

            final String lastNameProperty = (String) personEntityState1.getProperty( getQualifiedName( lastNamePropertyAccessor ) );
            assertNotNull( lastNameProperty );

            assertEquals( DEFAULT_LAST_NAME, lastNameProperty );

        }
        catch( NoSuchMethodException e )
        {
            fail( "HasFirstName must have [firstName] method." );
        }

        // ==================================
        // Test with initialzed default value
        // ==================================
        final HashMap<String, Object> initialValues2 = new HashMap<String, Object>();
        final String expectedFirstNameValue = "Jane";
        initialValues2.put( "firstName", expectedFirstNameValue );
        final EntityState personEntityState2 = newPersonEntityState( initialValues2 );

        // ----------
        // First name
        // ----------
        try
        {
            final Method firstNamePropertyAccessor = HasFirstName.class.getMethod( "firstName" );

            final String firstNameProperty = (String) personEntityState2.getProperty( getQualifiedName( firstNamePropertyAccessor ) );
            assertNotNull( firstNameProperty );

            assertEquals( expectedFirstNameValue, firstNameProperty );
        }
        catch( NoSuchMethodException e )
        {
            fail( "HasFirstName must have [firstName] method." );
        }

        // ---------
        // Last name
        // ---------
        try
        {
            final Method lastNamePropertyAccessor = HasLastName.class.getMethod( "lastName" );

            final String lastNameProperty = (String) personEntityState2.getProperty( getQualifiedName( lastNamePropertyAccessor ) );
            assertNotNull( lastNameProperty );

            assertEquals( DEFAULT_LAST_NAME, lastNameProperty );

        }
        catch( NoSuchMethodException e )
        {
            fail( "HasFirstName must have [firstName] method." );
        }

        // Test get first name on 
    }


    /**
     * Returns all person composite property bindings.
     *
     * @return All person composite property bindings.
     * @since 0.1.0
     */
    private Map<String, PropertyBinding> getPersonCompositePropertyBindings()
    {
        final CompositeBuilderFactory builderFactory = moduleInstance.structureContext().getCompositeBuilderFactory();
        final PersonComposite composite = builderFactory.newComposite( PersonComposite.class );
        final CompositeBinding personBinding = runtime.getCompositeBinding( composite );
        final Iterable<PropertyBinding> propertyBindings = personBinding.getPropertyBindings();
        final Map<String, PropertyBinding> properties = new HashMap<String, PropertyBinding>();
        for( final PropertyBinding aBinding : propertyBindings )
        {
            final String propertyName = aBinding.getPropertyResolution().getPropertyModel().getName();
            properties.put( propertyName, aBinding );
        }
        assertFalse( "Properties must not be empty.", properties.isEmpty() );
        return properties;
    }

    public final void assemble( final ModuleAssembly aModule ) throws AssemblyException
    {
        aModule.addComposites( PersonComposite.class );

        // Has Name
        final HasFirstName hasFirstName = aModule.addProperty().withAccessor( HasFirstName.class );
        hasFirstName.firstName().set( DEFAULT_FIRST_NAME );

        final HasLastName hasLastName = aModule.addProperty().withAccessor( HasLastName.class );
        hasLastName.lastName().set( DEFAULT_LAST_NAME );
    }
}
