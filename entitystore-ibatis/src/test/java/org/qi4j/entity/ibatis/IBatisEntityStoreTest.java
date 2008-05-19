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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.junit.Ignore;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerConfiguration;
import org.qi4j.entity.ibatis.internal.IBatisEntityState;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.property.Property;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyModel;
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
    private IBatisEntityStoreService entityStore;

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
        throws SQLException, UnitOfWorkCompletionException
    {
        final UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        final CompositeDescriptor personCompositeDescriptor = getCompositeDescriptor( PersonComposite.class );

        final IBatisEntityState state = (IBatisEntityState) entityStore.newEntityState( personCompositeDescriptor, new QualifiedIdentity( "1", PersonComposite.class.getName() ) );
        assertNotNull( state );

        checkStateProperties( getCompositeBinding( PersonComposite.class ), state );
        uow.complete();
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
        module.addServices( IBatisEntityStoreService.class );

        final ModuleAssembly config = module.getLayerAssembly().newModuleAssembly();
        config.setName( "config" );
        config.addComposites( IBatisConfiguration.class).visibleIn( Visibility.layer );
        config.addComposites( DBInitializerConfiguration.class ).visibleIn( Visibility.layer );
        config.addServices( MemoryEntityStoreService.class );
        config.addProperty().withAccessor( IBatisConfiguration.class ).sqlMapConfigURL().set( getSqlMapConfigUrl() );
        derbyDatabaseHandler.initDbInitializerInfo( config );
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        entityStore = getEntityStore();
    }

    private IBatisEntityStoreService getEntityStore() throws Exception
    {
        assertNotNull( moduleInstance );
        return moduleInstance.getStructureContext().getServiceLocator().findService( IBatisEntityStoreService.class ).get();
    }
}