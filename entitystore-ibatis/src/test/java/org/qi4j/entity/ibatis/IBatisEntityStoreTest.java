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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerConfiguration;
import org.qi4j.entity.ibatis.entity.PersonComposite;
import org.qi4j.entity.ibatis.test.AbstractTestCase;
import org.qi4j.entity.ibatis.DerbyDatabaseHandler;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.property.Property;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;
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
public final class IBatisEntityStoreTest extends AbstractTestCase
{
    private static final String SCHEMA_FILE = "testDbSchema.sql";
    private static final String DATA_FILE = "testDbData.sql";
    private static final String SQL_MAP_CONFIG_XML = "SqlMapConfig.xml";

    private static final String NEW_TEST_ID = "111";
    private static final String JOHN_SMITH_ID = "1";

    private IBatisEntityStoreService entityStore;

    @Test public void isThereDataInTheDatabaseAfterInitialization()
        throws Exception
    {
        entityStore.toString();
        derbyDatabaseHandler.checkDataInitialization();
    }

    @Test public final void createNewEntityStateWithoutPersisting()
        throws SQLException, UnitOfWorkCompletionException
    {
        final UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        final CompositeDescriptor personCompositeDescriptor = getCompositeDescriptor( PersonComposite.class );

        final EntityState state = entityStore.newEntityState( personCompositeDescriptor, id( NEW_TEST_ID ) );
        assertNotNull( state );
        checkEntityStateProperties( getCompositeBinding( PersonComposite.class ), state, false );
        uow.complete();
    }

    @Test public final void newEntityStateIsPersistedToDatabase()
        throws SQLException, UnitOfWorkCompletionException
    {
        final Map<String, String> data = createTestData( "Edward", "Yakop" );

        final UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

        final CompositeBuilder<PersonComposite> builder = uow.newEntityBuilder( PersonComposite.class );
        final PersonComposite person = builder.newInstance();
        final String newId = person.identity().get();

        person.firstName().set( data.get( "FIRST_NAME" ) );
        person.lastName().set( data.get( "LAST_NAME" ) );

        uow.complete();
        assertPersonEqualsInDatabase( newId, data );
    }

    @Test( expected = EntityStoreException.class )
    public void loadOfNonExistingEntityFails()
    {
        loadEntity( "1123123" );
    }

    @Test public void loadExistingEntity()
    {
        final EntityState state = loadEntity( JOHN_SMITH_ID );
        assertPersonEntityStateEquals( JOHN_SMITH_ID, "John", "Smith", state );
    }

    @Test public void findExistingPersonComposite() throws UnitOfWorkCompletionException
    {
        final UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        final PersonComposite person = uow.find( JOHN_SMITH_ID, PersonComposite.class );
        assertPersonEquals( JOHN_SMITH_ID, "John", "Smith", person );
        uow.complete();
    }

    public final void assemble( final ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( PersonComposite.class );
        module.addServices( UuidIdentityGeneratorService.class );
        module.addServices( IBatisEntityStoreService.class );

        final ModuleAssembly config = module.getLayerAssembly().newModuleAssembly();
        config.setName( "config" );
        config.addComposites( IBatisConfiguration.class ).visibleIn( Visibility.layer );
        config.addComposites( DBInitializerConfiguration.class ).visibleIn( Visibility.layer );
        config.addServices( MemoryEntityStoreService.class );
        config.addProperty().withAccessor( IBatisConfiguration.class ).sqlMapConfigURL().set( getSqlMapConfigUrl() );
        derbyDatabaseHandler.initDbInitializerInfo( config, SCHEMA_FILE, DATA_FILE );
    }

    private EntityState loadEntity( final String id )
    {
        final QualifiedIdentity qualifiedIdentity = id( id );
        return this.entityStore.getEntityState( getCompositeDescriptor( PersonComposite.class ), qualifiedIdentity );
    }

    private Map<String, String> createTestData( final String firstName, final String lastName )
    {
        final Map<String, String> data = new HashMap<String, String>();
        data.put( "FIRST_NAME", firstName );
        data.put( "LAST_NAME", lastName );
        return data;
    }

    private String getSqlMapConfigUrl()
    {
        final URL sqlMapConfigURL = getClass().getResource( SQL_MAP_CONFIG_XML );
        return sqlMapConfigURL.toString();
    }

    private void assertPersonEqualsInDatabase( final String identity, final Map<String, ?> values )
    {
        final int count = derbyDatabaseHandler.executeStatement( "select * from person where id = '" + identity + "'", new DerbyDatabaseHandler.ResultSetCallback()
        {
            public void row( final ResultSet rs ) throws SQLException
            {
                assertEquals( "id", identity, rs.getString( "id" ) );
                assertContainsValues( rs, values );
            }
        } );
        assertEquals( "Person with Id " + identity, 1, count );
    }

    private void assertContainsValues( final ResultSet rs, final Map<String, ?> values )
        throws SQLException
    {
        if( values == null )
        {
            return;
        }

        for( final Map.Entry<String, ?> entry : values.entrySet() )
        {
            final String name = entry.getKey();
            assertEquals( name, entry.getValue(), rs.getString( name ) );
        }
    }

    private static void checkEntityStateProperties( final CompositeBinding compositeBinding, final EntityState state, final boolean checkAll )
    {
        assertNotNull( "identity", state.getIdentity() );
        assertNotNull( "identity", state.getIdentity().getIdentity() );
        if( !checkAll )
        {
            return;
        }

        for( final PropertyBinding propertyBinding : compositeBinding.getPropertyBindings() )
        {
            final PropertyModel propertyModel = propertyBinding.getPropertyResolution().getPropertyModel();

            final String propertyName = propertyModel.getName();
            if( "identity".equals( propertyName ) )
            {
                continue;
            }
            final Property property = (Property) state.getProperty( propertyModel.getQualifiedName() );

            assertNotNull( "Property [" + propertyName + ": " + propertyModel.getType() + "] is not found.", property );
        }
    }

    private void assertPersonEntityStateEquals( final String id, final String firstName, final String lastName, final EntityState state )
    {
        assertNotNull( state );
        final QualifiedIdentity qualifiedIdentity = state.getIdentity();

        assertNotNull( "identity", qualifiedIdentity );
        assertEquals( "identity", id, qualifiedIdentity.getIdentity() );

        assertEquals( "identity", id, state.getProperty( "identity" ) );
        assertEquals( "firstName", firstName, state.getProperty( "firstName" ) );
        assertEquals( "lastName", lastName, state.getProperty( "lastName" ) );
    }

    private void assertPersonEquals( final String id, final String firstName, final String lastName, final PersonComposite person )
    {
        assertEquals( "identity", id, person.identity().get() );
        assertEquals( "firstName", firstName, person.firstName().get() );
        assertEquals( "lastName", lastName, person.lastName().get() );
    }

    private QualifiedIdentity id( final String id )
    {
        return new QualifiedIdentity( id, PersonComposite.class.getName() );
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