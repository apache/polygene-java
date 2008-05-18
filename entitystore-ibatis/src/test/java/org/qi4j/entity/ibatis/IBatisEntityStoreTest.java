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
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerInfo;
import org.qi4j.entity.ibatis.internal.IBatisEntityState;
import org.qi4j.property.Property;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyModel;
import org.qi4j.spi.service.provider.DefaultServiceInstanceProvider;
import org.qi4j.spi.structure.CompositeDescriptor;
import org.qi4j.structure.Visibility;

/**
 * {@code IBatisEntityStoreTest} tests {@code IBatisEntityStore}.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
@Ignore
public final class IBatisEntityStoreTest extends AbstractTestCase
{
    private IBatisEntityStore entityStore;

    public IBatisEntityStoreTest()
        throws Exception
    {
    }

    private static final String SQL_MAP_CONFIG_XML = "SqlMapConfig.xml";


    public final void testActivate()
        throws SQLException
    {

        // Make sure there's default data in database
        derbyDatabaseHandler.checkDataInitialization();
    }

    /**
     * Construct a new entity store and activates it.
     *
     * @return A new entity store.
     * @since 0.1.0
     */
    private IBatisEntityStore newAndActivateEntityStore()
    {
        return moduleInstance.getStructureContext().getServiceLocator().findService( IBatisEntityStore.class ).get();
//        ServiceDescriptor descriptor = newValidServiceDescriptor();
//        IBatisEntityStore entityStore = new IBatisEntityStore( descriptor );
//        try
//        {
//            entityStore.activate();
//        }
//        catch( Exception e )
//        {
//            e.printStackTrace();
//            fail( "Activation with valid configuration must succeed." );
//        }
//
//        return entityStore;
    }

    /**
     * Construct a new valid service descriptor.
     *
     * @return a new valid service descriptor.
     * @since 0.1.0
     */
    private ServiceDescriptor newValidServiceDescriptor()
    {
        final Map<Class, Serializable> infos = new HashMap<Class, Serializable>();

        final IBatisEntityStoreServiceInfo ibatisEntityStoreServiceInfo = createServiceInfo();

        infos.put( IBatisEntityStoreServiceInfo.class, ibatisEntityStoreServiceInfo );
        infos.put( DBInitializerInfo.class, newDbInitializerInfo() );

        return new ServiceDescriptor( IBatisEntityStore.class, DefaultServiceInstanceProvider.class, "ibatis", Visibility.module, true, infos );
    }

    private IBatisEntityStoreServiceInfo createServiceInfo()
    {

        return new IBatisEntityStoreServiceInfo( getSqlMapConfigUrl() );
    }

    private String getSqlMapConfigUrl()
    {
        final URL sqlMapConfigURL = getClass().getResource( SQL_MAP_CONFIG_XML );
        return sqlMapConfigURL.toString();
    }

    /**
     * Tests {@link org.qi4j.spi.entity.EntityStore#newEntityState(org.qi4j.spi.structure.CompositeDescriptor, org.qi4j.spi.entity.QualifiedIdentity)}
     *
     * @throws SQLException Thrown if initialization fails.
     */
    @Test
    public final void testNewEntityState()
        throws SQLException
    {
        final CompositeDescriptor personCompositeDescriptor = getCompositeDescriptor( PersonComposite.class );

        final IBatisEntityState state = (IBatisEntityState) entityStore.newEntityState( personCompositeDescriptor, new QualifiedIdentity( "1" ) );
        assertNotNull( state );

        checkStateProperties( getCompositeBinding( PersonComposite.class ), state );

    }

    private static void checkStateProperties( final CompositeBinding personBinding, final IBatisEntityState state )
    {
        for( final PropertyBinding propertyBinding : personBinding.getPropertyBindings() )
        {
            final PropertyModel propertyModel = propertyBinding.getPropertyResolution().getPropertyModel();

            final Property property = (Property) state.getProperty( propertyModel.getQualifiedName() );

            assertNotNull( "Property [" + propertyModel.getName() + "] is not found.", property );
        }
    }

    @Test( expected = EntityStoreException.class )
    public void testgetStateForNonExistentIdentity()
    {
        final QualifiedIdentity identity = new QualifiedIdentity( "1123123", PersonComposite.class.getName() );
        final EntityState state = this.entityStore.getEntityState( getCompositeDescriptor( PersonComposite.class ), identity );
        assertNull( state );
    }

    @Test public void testgetStateForValidIdentity()
    {
        final QualifiedIdentity johnSmithIdentity = new QualifiedIdentity( "1", PersonComposite.class.getName() );

        final EntityState state = entityStore.getEntityState( getCompositeDescriptor( PersonComposite.class ), johnSmithIdentity );
        assertNotNull( state );

        final Property identityProperty = (Property) state.getProperty( "identity" );
        assertNotNull( identityProperty );
        assertEquals( "1", identityProperty.get() );

        final Property firstNameProperty = (Property) state.getProperty( "firstName" );
        assertNotNull( "firstName", firstNameProperty );
        assertEquals( "firstName", "John", firstNameProperty.get() );

        final Property lastNameProperty = (Property) state.getProperty( "lastName" );
        assertNotNull( "lastName", lastNameProperty );
        assertEquals( "lastName", "Smith", lastNameProperty.get() );
    }

    public final void assemble( final ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( PersonComposite.class );
        module.addProperty().withAccessor( IBatisConfiguration.class ).sqlMapConfigURL().set( getSqlMapConfigUrl() );
        // module.addProperty().withAccessor( IBatisConfiguration.class ).configProperties().set(  );
        module.addServices( IBatisEntityStoreService.class ).instantiateOnStartup();
/* todo
        final ModuleAssembly config = module.getLayerAssembly().newModuleAssembly();
        config.setName( "config" );
        config.addComposites( IBatisConfiguration.class ).visibleIn( Visibility.layer );
        config.addServices( MemoryEntityStoreService.class );
        config.addProperty().withAccessor( IBatisConfiguration.class ).sqlMapConfigURL().set( getSqlMapConfigUrl() );
*/
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        entityStore = moduleInstance.getStructureContext().getServiceLocator().findService( IBatisEntityStore.class ).get();
    }
}