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
import org.jmock.Mockery;
import org.junit.Ignore;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.ibatis.AbstractTestCase;
import org.qi4j.entity.ibatis.HasFirstName;
import org.qi4j.entity.ibatis.HasLastName;
import org.qi4j.entity.ibatis.PersonComposite;
import static org.qi4j.property.ComputedPropertyInstance.getQualifiedName;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.structure.ModuleContext;
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
    // @Test
    public void testGetProperty()
    {
        // =======================
        // Test with default value
        // =======================
        HashMap<String, Object> initialValues1 = new HashMap<String, Object>();
        EntityState personEntityState1 = newPersonEntityState( initialValues1 );

        // ----------
        // First name
        // ----------
        try
        {
            Method firstNamePropertyAccessor = HasFirstName.class.getMethod( "firstName" );

            String firstNameProperty = (String) personEntityState1.getProperty( getQualifiedName( firstNamePropertyAccessor ) );
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
            Method lastNamePropertyAccessor = HasLastName.class.getMethod( "lastName" );

            String lastNameProperty = (String) personEntityState1.getProperty( getQualifiedName( lastNamePropertyAccessor ) );
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
        HashMap<String, Object> initialValues2 = new HashMap<String, Object>();
        String expectedFirstNameValue = "Jane";
        initialValues2.put( "firstName", expectedFirstNameValue );
        EntityState personEntityState2 = newPersonEntityState( initialValues2 );

        // ----------
        // First name
        // ----------
        try
        {
            Method firstNamePropertyAccessor = HasFirstName.class.getMethod( "firstName" );

            String firstNameProperty = (String) personEntityState2.getProperty( getQualifiedName( firstNamePropertyAccessor ) );
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
            Method lastNamePropertyAccessor = HasLastName.class.getMethod( "lastName" );

            String lastNameProperty = (String) personEntityState2.getProperty( getQualifiedName( lastNamePropertyAccessor ) );
            assertNotNull( lastNameProperty );

            assertEquals( DEFAULT_LAST_NAME, lastNameProperty );

        }
        catch( NoSuchMethodException e )
        {
            fail( "HasFirstName must have [firstName] method." );
        }

        // Test get first name on 
    }

    private IBatisEntityState newPersonEntityState( HashMap<String, Object> initialValues )
    {
        ModuleContext moduleContext = moduleInstance.getModuleContext();
        Map<Class<? extends Composite>, CompositeContext> compositeContexts = moduleContext.getCompositeContexts();
        CompositeContext personCompositeContext = compositeContexts.get( PersonComposite.class );
        assertNotNull( personCompositeContext );

        CompositeBinding personCompositeBinding = personCompositeContext.getCompositeBinding();
        assertNotNull( personCompositeBinding );

        Mockery mockery = new Mockery();
        UnitOfWork unitOfWork = mockery.mock( UnitOfWork.class );
        return null;
        // new IBatisEntityState( new QualifiedIdentity( "1", PersonComposite.class.getName() ), personCompositeBinding, initialValues, EntityStatus.NEW, statusNew, unitOfWork, dao );
    }

    /**
     * Returns all person composite property bindings.
     *
     * @return All person composite property bindings.
     * @since 0.1.0
     */
    private Map<String, PropertyBinding> getPersonCompositePropertyBindings()
    {
        CompositeBuilderFactory builderFactory = moduleInstance.getStructureContext().getCompositeBuilderFactory();
        PersonComposite composite = builderFactory.newComposite( PersonComposite.class );
        CompositeBinding personBinding = runtime.getCompositeBinding( composite );
        Iterable<PropertyBinding> propertyBindings = personBinding.getPropertyBindings();
        Map<String, PropertyBinding> properties = new HashMap<String, PropertyBinding>();
        for( PropertyBinding aBinding : propertyBindings )
        {
            String propertyName = aBinding.getPropertyResolution().getPropertyModel().getName();
            properties.put( propertyName, aBinding );
        }
        assertFalse( "Properties must not be empty.", properties.isEmpty() );
        return properties;
    }

    public final void assemble( ModuleAssembly aModule ) throws AssemblyException
    {
        aModule.addComposites( PersonComposite.class );

        // Has Name
        HasFirstName hasFirstName = aModule.addProperty().withAccessor( HasFirstName.class );
        hasFirstName.firstName().set( DEFAULT_FIRST_NAME );

        HasLastName hasLastName = aModule.addProperty().withAccessor( HasLastName.class );
        hasLastName.lastName().set( DEFAULT_LAST_NAME );
    }
}
