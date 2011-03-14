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
package org.qi4j.entitystore.qrm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.entitystore.qrm.entity.AccountComposite;
import org.qi4j.entitystore.qrm.entity.Person;
import org.qi4j.entitystore.qrm.entity.PersonComposite;
import org.qi4j.entitystore.qrm.test.AbstractTestCase;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;

import static org.junit.Assert.*;

/**
 * {@code IBatisEntityStoreTest} tests {@code IBatisEntityStore}.
 */
@Ignore( "Needs attention" )
public final class IBatisEntityStoreTest
    extends AbstractTestCase
{

    private static final String NEW_TEST_ID = "111";

    private QrmSqlEntityStoreService entityStore;
    private static final String JOHNS_ACCOUNT = "Johns Account";

    @Test
    public void isThereDataInTheDatabaseAfterInitialization()
        throws Exception
    {
//        entityStore.iterator();
        derbyDatabaseHandler.checkDataInitialization();
    }

    @Test
    public final void newEntityStateIsPersistedToDatabase()
        throws SQLException, UnitOfWorkCompletionException
    {
        final Map<String, String> data = createTestData( "Edward", "Yakop" );

        final UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

        final EntityBuilder<PersonComposite> builder = uow.newEntityBuilder( PersonComposite.class );
        PersonComposite person = builder.instance();
        person.firstName().set( data.get( "FIRST_NAME" ) );
        person.lastName().set( data.get( "LAST_NAME" ) );
        person = builder.newInstance();
        final String newId = person.identity().get();

        uow.complete();
        assertPersonEqualsInDatabase( newId, data );
    }

    @Test
    public final void existingEntityIsDeletedFromPersistentStore()
        throws SQLException, UnitOfWorkCompletionException
    {
        final UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        final PersonComposite john = uow.get( PersonComposite.class, TestConfig.JOHN_SMITH_ID );
        uow.remove( john );
        uow.complete();
        derbyDatabaseHandler.executeStatement( "select count(*) CNT from person where ID= '" + TestConfig.JOHN_SMITH_ID + "'", new DerbyDatabaseHandler.ResultSetCallback()
        {
            public void row( final ResultSet rs )
                throws SQLException
            {
                assertEquals( 0, rs.getInt( "CNT" ) );
            }
        } );
    }

    @Test
    public final void completeThrowsNPE()
        throws UnitOfWorkCompletionException
    {
        final UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        uow.get( PersonComposite.class, TestConfig.JOHN_SMITH_ID );
        uow.complete();
    }

    @Test
    public final void existingEntityIsUpdatedInPersistentStore()
        throws SQLException, UnitOfWorkCompletionException
    {
        final UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        final PersonComposite john = uow.get( PersonComposite.class, TestConfig.JOHN_SMITH_ID );
        john.lastName().set( "Doe" );
        uow.complete();
        derbyDatabaseHandler.executeStatement( "select LAST_NAME from person where ID= '" + TestConfig.JOHN_SMITH_ID + "'", new DerbyDatabaseHandler.ResultSetCallback()
        {
            public void row( final ResultSet rs )
                throws SQLException
            {
                assertEquals( "Doe", rs.getString( "LAST_NAME" ) );
            }
        } );
    }

    @Test
    public final void associationIsPersistedToDatabase()
        throws SQLException, UnitOfWorkCompletionException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        final PersonComposite john = uow.get( PersonComposite.class, TestConfig.JOHN_SMITH_ID );
        assertNotNull( "john", john );
        final AccountComposite johnsAccount = uow.newEntity( AccountComposite.class );
        final String accountId = johnsAccount.identity().get();
        johnsAccount.name().set( JOHNS_ACCOUNT );
        johnsAccount.primaryContactPerson().set( john );
        uow.complete();

        uow = unitOfWorkFactory.newUnitOfWork();
        final AccountComposite account = uow.get( AccountComposite.class, accountId );
        assertEquals( "account name", JOHNS_ACCOUNT, account.name().get() );
        final Person contactPerson = account.primaryContactPerson().get();
        assertEquals( "john is contact", john, contactPerson );
        uow.discard();
    }

    @Test( expected = EntityStoreException.class )
    public void loadOfNonExistingEntityFails()
    {
        loadEntity( "1123123" );
    }

    @Test
    public void loadExistingEntity()
    {
        final EntityState state = loadEntity( TestConfig.JOHN_SMITH_ID );
        assertPersonEntityStateEquals( TestConfig.JOHN_SMITH_ID, "John", "Smith", state );
    }

    @Test
    public void loadExistingEntityWithAccounts()
    {
        final EntityState state = loadEntity( TestConfig.JANE_SMITH_ID );
        assertPersonEntityStateEquals( TestConfig.JANE_SMITH_ID, "Jane", "Smith", state );
//        assertEquals( "ACCOUNTS", state.manyAssociationNames().iterator().next() );
        assertEquals( "no association accounts", null, state.getAssociation( QualifiedName.fromQN( "ACCOUNTS" ) ) );
        final ManyAssociationState manyAssociation = state.getManyAssociation( QualifiedName.fromQN( "ACCOUNTS" ) );
        assertEquals( "many association accounts", 2, manyAssociation.count() );
//        assertTrue( "account reference 1", manyAssociation.contains( createId( "1", Account.class ) ) );
//        assertTrue( "account reference 2", manyAssociation.contains( createId( "2", Account.class ) ) );
    }

    private QualifiedIdentity createId( String id, Class<?> compositeClass )
    {
        return new QualifiedIdentity( id, compositeClass );
    }

    @Test
    public void findExistingPersonComposite()
        throws UnitOfWorkCompletionException
    {
        final UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        final PersonComposite person = uow.get( PersonComposite.class, TestConfig.JOHN_SMITH_ID );
        assertPersonEquals( TestConfig.JOHN_SMITH_ID, "John", "Smith", person );
        uow.complete();
    }

    public final void assemble( final ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( PersonComposite.class, AccountComposite.class );
        module.services( UuidIdentityGeneratorService.class );
        module.services( QrmSqlEntityStoreService.class );

        final ModuleAssembly config = module.layer().module( "config" );
        config.entities( QrmSqlConfiguration.class ).visibleIn( Visibility.layer );
        config.services( MemoryEntityStoreService.class );
        config.forMixin( QrmSqlConfiguration.class )
            .declareDefaults()
            .sqlMapConfigURL()
            .set( derbyDatabaseHandler.getUrlString( TestConfig.SQL_MAP_CONFIG_XML ) );
        derbyDatabaseHandler.initDbInitializerInfo( config, TestConfig.SCHEMA_FILE, TestConfig.DATA_FILE );
    }

    private EntityState loadEntity( final String id )
    {
/*
        final QualifiedIdentity qualifiedIdentity = id( id );
        return this.entityStore.getEntityState( qualifiedIdentity );
*/
        return null;
    }

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        entityStore = getEntityStore();
/*
        entityStore.registerEntityType( spi.getEntityDescriptor( PersonComposite.class, moduleInstance ).entityType() );
        entityStore.registerEntityType( spi.getEntityDescriptor( AccountComposite.class, moduleInstance ).entityType() );
*/
    }

    private QrmSqlEntityStoreService getEntityStore()
        throws Exception
    {
        assertNotNull( moduleInstance );
        return moduleInstance.serviceFinder()
            .<QrmSqlEntityStoreService>findService( QrmSqlEntityStoreService.class )
            .get();
    }
}