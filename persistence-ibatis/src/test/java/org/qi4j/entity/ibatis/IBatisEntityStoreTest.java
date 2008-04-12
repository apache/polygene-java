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
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import static junit.framework.Assert.*;
import org.jmock.Mockery;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.entity.Identity;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerInfo;
import org.qi4j.entity.ibatis.internal.IBatisEntityState;
import org.qi4j.property.Property;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.structure.ModuleContext;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.composite.CompositeMethodBinding;
import org.qi4j.spi.composite.CompositeMethodModel;
import org.qi4j.spi.composite.CompositeMethodResolution;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.service.provider.DefaultServiceInstanceProvider;
import static org.qi4j.structure.Visibility.*;

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
    @Test
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

    /**
     * Tests {@link org.qi4j.spi.entity.EntityStore#newEntityState(org.qi4j.spi.structure.CompositeDescriptor,org.qi4j.spi.serialization.EntityId)}
     *
     * @throws SQLException Thrown if initialization fails.
     */
    // @Test
    public final void testNewEntityState()
        throws SQLException
    {
        // Initialize the derby and entity store
        initializeDerby();
        IBatisEntityStore entityStore = newAndActivateEntityStore();

        // Intialize test arguments
        ModuleContext moduleContext = moduleInstance.getModuleContext();
        Map<Class<? extends Composite>, CompositeContext> compositeContexts = moduleContext.getCompositeContexts();
        CompositeContext personCompositeContext = compositeContexts.get( PersonComposite.class );
        CompositeBinding personBinding = personCompositeContext.getCompositeBinding();

        Mockery mockery = new Mockery();
        UnitOfWork unitOfWork = mockery.mock( UnitOfWork.class );
        HashMap<Method, Object> initialValues = new HashMap<Method, Object>();
        try
        {
            IBatisEntityState state = (IBatisEntityState) entityStore.newEntityState( "1", personBinding );
            assertNotNull( state );

            checkStateProperties( personBinding, state );
        }
        catch( EntityStoreException e )
        {
            e.printStackTrace();
            fail( "Creating entity state must not fail." );
        }

    }

    private static void checkStateProperties( CompositeBinding personBinding, IBatisEntityState state )
    {
        Collection<CompositeMethodBinding> methodBindings = personBinding.getCompositeMethodBindings();
        for( CompositeMethodBinding methodBinding : methodBindings )
        {
            PropertyBinding propertyBinding = methodBinding.getPropertyBinding();
            if( propertyBinding == null )
            {
                continue;
            }

            CompositeMethodResolution methodResolution = methodBinding.getCompositeMethodResolution();
            CompositeMethodModel methodModel = methodResolution.getCompositeMethodModel();
            Method propertyMethod = methodModel.getMethod();
            Class<?> propertyReturnType = propertyMethod.getReturnType();
            if( Property.class.isAssignableFrom( propertyReturnType ) )
            {
                Property property = state.getProperty( propertyMethod );

                if( property == null )
                {
                    String propertyName = propertyBinding.getPropertyResolution().getPropertyModel().getName();
                    fail( "Property [" + propertyName + "] is not found." );
                }
            }
        }
    }

    /**
     * Tests {@link IBatisEntityStore#getEntityState(org.qi4j.entity.UnitOfWork , String, CompositeBinding)}
     *
     * @throws SQLException Thrown if initialization fails.
     */
    // @Test
    public final void testGetEntityState()
        throws SQLException
    {
        // Initialize the derby and entity store
        initializeDerby();
        IBatisEntityStore entityStore = newAndActivateEntityStore();

        // Intialize test arguments
        ModuleContext moduleContext = moduleInstance.getModuleContext();
        Map<Class<? extends Composite>, CompositeContext> compositeContexts = moduleContext.getCompositeContexts();
        CompositeContext personCompositeContext = compositeContexts.get( PersonComposite.class );
        CompositeBinding personBinding = personCompositeContext.getCompositeBinding();

        Mockery mockery = new Mockery();
        UnitOfWork unitOfWork = mockery.mock( UnitOfWork.class );

        // ============================
        // Test get with valid identity
        // ============================
        try
        {
            IBatisEntityState state = entityStore.getEntityState( unitOfWork, "1", personBinding );
            assertNotNull( state );

            // --------
            // Identity
            // --------
            Property identityProperty = state.getProperty( Identity.class.getMethod( "identity" ) );
            assertNotNull( identityProperty );
            assertEquals( "1", identityProperty.get() );

            // ----------
            // First Name
            // ----------
            Property firstNameProperty = state.getProperty( HasFirstName.class.getMethod( "firstName" ) );
            assertNotNull( firstNameProperty );
            assertEquals( "John", firstNameProperty.get() );

            // ---------
            // Last Name
            // ---------
            Property lastNameProperty = state.getProperty( HasLastName.class.getMethod( "lastName" ) );
            assertNotNull( lastNameProperty );
            assertEquals( "Smith", lastNameProperty.get() );
        }
        catch( EntityStoreException e )
        {
            e.printStackTrace();
            fail( "Creating entity state must not fail." );
        }
        catch( NoSuchMethodException e )
        {
            e.printStackTrace();
            fail();
        }

        // ===================================
        // Test get with non-existant identity
        // ===================================
        try
        {
            IBatisEntityState state = entityStore.getEntityState( unitOfWork, "1123123", personBinding );
            assertNull( state );
        }
        catch( EntityStoreException e )
        {
            e.printStackTrace();
            fail( "Creating entity state must not fail." );
        }
    }

    public final void assemble( ModuleAssembly aModule )
        throws AssemblyException
    {
        aModule.addComposites( PersonComposite.class );
    }

}