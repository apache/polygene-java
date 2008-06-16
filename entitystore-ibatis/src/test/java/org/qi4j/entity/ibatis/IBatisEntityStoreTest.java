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
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.EntityBuilder;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerConfigurationComposite;
import org.qi4j.entity.ibatis.entity.AccountComposite;
import org.qi4j.entity.ibatis.entity.PersonComposite;
import org.qi4j.entity.ibatis.test.AbstractTestCase;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;
import org.qi4j.structure.Visibility;

/**
 * {@code IBatisEntityStoreTest} tests {@code IBatisEntityStore}.
 */
public final class IBatisEntityStoreTest extends AbstractTestCase
{
    private static final String SCHEMA_FILE = "testDbSchema.sql";
    private static final String DATA_FILE = "testDbData.sql";
    private static final String SQL_MAP_CONFIG_XML = "SqlMapConfig.xml";

    private static final String NEW_TEST_ID = "111";
    private static final String JOHN_SMITH_ID = "1";

    private IBatisEntityStoreService entityStore;
    private static final String JOHNS_ACCOUNT = "Johns Account";

    @Test public void isThereDataInTheDatabaseAfterInitialization()
        throws Exception
    {
        entityStore.toString();
        derbyDatabaseHandler.checkDataInitialization();
    }

    @Test public final void createNewEntityStateWithoutPersisting()
        throws UnitOfWorkCompletionException
    {
        final UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            final CompositeDescriptor personCompositeDescriptor = getCompositeDescriptor( PersonComposite.class );

            final EntityState state = entityStore.newEntityState( personCompositeDescriptor, id( NEW_TEST_ID ) );
            assertNotNull( state );
            checkEntityStateProperties( getCompositeDescriptor( PersonComposite.class ), state, false );
            uow.complete();
        }
        catch( RuntimeException e )
        {
            uow.discard();
        }
    }

    @Test public final void newEntityStateIsPersistedToDatabase()
        throws SQLException, UnitOfWorkCompletionException
    {
        final Map<String, String> data = createTestData( "Edward", "Yakop" );

        final UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

        final EntityBuilder<PersonComposite> builder = uow.newEntityBuilder( PersonComposite.class );
        final PersonComposite person = builder.newInstance();
        final String newId = person.identity().get();

        person.firstName().set( data.get( "FIRST_NAME" ) );
        person.lastName().set( data.get( "LAST_NAME" ) );

        uow.complete();
        assertPersonEqualsInDatabase( newId, data );
    }

    @Test public final void existingEntityIsDeletedFromPersistentStore()
        throws SQLException, UnitOfWorkCompletionException
    {
        final UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        final PersonComposite john = uow.find( JOHN_SMITH_ID, PersonComposite.class );
        uow.remove( john );
        uow.complete();
        derbyDatabaseHandler.executeStatement( "select count(*) CNT from person where ID= '" + JOHN_SMITH_ID + "'", new DerbyDatabaseHandler.ResultSetCallback()
        {
            public void row( final ResultSet rs ) throws SQLException
            {
                assertEquals( 0, rs.getInt( "CNT" ) );
            }
        } );
    }

    @Test @Ignore public final void existingEntityIsUpdatedInPersistentStore()
        throws SQLException, UnitOfWorkCompletionException
    {
        final UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            final PersonComposite john = uow.find( JOHN_SMITH_ID, PersonComposite.class );
            john.lastName().set( "Doe" );
        }
        finally
        {
            uow.complete();
        }
        derbyDatabaseHandler.executeStatement( "select LAST_NAME from person where ID= '" + JOHN_SMITH_ID + "'", new DerbyDatabaseHandler.ResultSetCallback()
        {
            public void row( final ResultSet rs ) throws SQLException
            {
                assertEquals( "Doe", rs.getString( "LAST_NAME" ) );
            }
        } );
    }

    @Test @Ignore public final void associationIsPersistedToDatabase()
        throws SQLException, UnitOfWorkCompletionException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        final PersonComposite john;
        final String accountId;
        try
        {
            john = uow.find( JOHN_SMITH_ID, PersonComposite.class );
            assertNotNull( "john", john );
            final AccountComposite johnsAccount = uow.newEntity( AccountComposite.class );
            accountId = johnsAccount.identity().get();
            johnsAccount.name().set( JOHNS_ACCOUNT );
            johnsAccount.primaryContactPerson().set( john );
        }
        finally
        {
            uow.complete();
        }

        uow = unitOfWorkFactory.newUnitOfWork();
        final AccountComposite account = uow.find( accountId, AccountComposite.class );
        assertEquals( "account name", JOHNS_ACCOUNT, account.name().get() );
        final PersonComposite contactPerson = account.primaryContactPerson().get();
        assertEquals( "john is contact", john, contactPerson );
        uow.discard();
    }

    @Test( expected = EntityStoreException.class )
    public void loadOfNonExistingEntityFails()
    {
        loadEntity( "1123123" );
    }

    @Test @Ignore public void loadExistingEntity()
    {
        final EntityState state = loadEntity( JOHN_SMITH_ID );
        assertPersonEntityStateEquals( JOHN_SMITH_ID, "John", "Smith", state );
    }

    @Test @Ignore public void findExistingPersonComposite() throws UnitOfWorkCompletionException
    {
        final UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            final PersonComposite person = uow.find( JOHN_SMITH_ID, PersonComposite.class );
            assertPersonEquals( JOHN_SMITH_ID, "John", "Smith", person );
            uow.complete();
        }
        catch( UnitOfWorkCompletionException e )
        {
            uow.discard();
            throw e;
        }
        catch( RuntimeException e )
        {
            uow.discard();
            throw e;
        }
    }

    public final void assemble( final ModuleAssembly module )
        throws AssemblyException
    {
        module.addEntities( PersonComposite.class );
        module.addEntities( AccountComposite.class );
        module.addServices( UuidIdentityGeneratorService.class ).visibleIn( Visibility.layer );
        module.addServices( IBatisEntityStoreService.class );

        final ModuleAssembly config = module.getLayerAssembly().newModuleAssembly();
        config.setName( "config" );
        config.addEntities( IBatisConfigurationComposite.class ).visibleIn( Visibility.layer );
        config.addEntities( DBInitializerConfigurationComposite.class ).visibleIn( Visibility.layer );
        config.addServices( MemoryEntityStoreService.class );
        config.addProperty().withAccessor( IBatisConfigurationComposite.class ).sqlMapConfigURL().set( getSqlMapConfigUrl() );
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

    private static void checkEntityStateProperties( final CompositeDescriptor compositeBinding, final EntityState state, final boolean checkAll )
    {
        assertNotNull( "identity", state.getIdentity() );
        assertNotNull( "identity", state.getIdentity().identity() );
        if( !checkAll )
        {
            return;
        }

/*
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
*/
    }

    private void assertPersonEntityStateEquals( final String id, final String firstName, final String lastName, final EntityState state )
    {
        assertNotNull( state );
        final QualifiedIdentity qualifiedIdentity = state.getIdentity();

        assertNotNull( "identity", qualifiedIdentity );
        assertEquals( "identity", id, qualifiedIdentity.identity() );

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
        return moduleInstance.serviceFinder().findService( IBatisEntityStoreService.class ).get();
    }
}