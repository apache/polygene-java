package org.qi4j.entitystore.qrm;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.qrm.entity.Account;
import org.qi4j.entitystore.qrm.entity.AccountComposite;
import org.qi4j.entitystore.qrm.internal.QrmMapperService;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.test.AbstractQi4jTest;

/**
 * User: alex
 */
public class CRUDTests
    extends AbstractQi4jTest
{

    private DerbyDatabaseHandler dbHandler = null;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        dbHandler = new DerbyDatabaseHandler();

        final Properties props = new Properties();

//        props.put("connection.driver_class","com.mysql.jdbc.Driver");
//        props.put("connection.url","jdbc:mysql://localhost/sqlmaps");
//        props.put("connection.username","admin");
//        props.put("connection.password","secret");

        props.put( "connection.driver_class", "org.apache.derby.jdbc.ClientDriver" );
        props.put( "connection.url", "jdbc:derby://localhost/testdb;create=true" );
        props.put( "connection.username", "sa" );
        props.put( "connection.password", "derbypass" );
        props.put( "connection.pool_size", "1" );

        props.put( "dialect", "org.hibernate.dialect.DerbyDialect" );
        props.put( "current_session_context_class", "thread" );
        props.put( "cache.provider_class", "org.hibernate.cache.NoCacheProvider" );
        props.put( "show_sql", "true" );
        props.put( "hbm2ddl.auto", "create-drop" );

        QrmEntityStoreDescriptor desc = new QrmEntityStoreDescriptor()
        {
            public List<Class> types()
            {
                return Arrays.asList( new Class[]{ Account.class } );
            }

            public Properties props()
            {
                return props;
            }
        };

        module.services( QrmMapperService.class );
        module.services( QrmEntityStoreService.class )
            .setMetaInfo( desc )
            .visibleIn( Visibility.application )
            .instantiateOnStartup();
        module.entities( AccountComposite.class );
    }

    @Test
    public void retrieve()
        throws Exception
    {
        dbHandler.executeUpdate( "insert into account (account_id, version, name, balance) " +
                                 "values ('org.qi4j.entitystore.qrm.entity.Account:23', 0, 'Test account 23', 3909.54)" );

        System.err
            .println( "====================================== retrieve test ======================================" );

        UnitOfWork uow = null;

        try
        {
            uow = moduleInstance.unitOfWorkFactory().newUnitOfWork();

            Account acc = uow.get( Account.class, "org.qi4j.entitystore.qrm.entity.Account:23" );

            Assert.assertEquals( "Test account 23", acc.name().get() );
            Assert.assertEquals( new BigDecimal( "3909.54" ), acc.balance().get() );
        }
        catch( EntityNotFoundException enfe )
        {
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void create()
        throws Exception
    {
        dbHandler.executeUpdate( "delete from account" );

        System.err
            .println( "====================================== create test ======================================" );

        UnitOfWork uow = null;

        Account account = null;

        String newId = Account.class.getName() + ":" + UUID.randomUUID();

        try
        {
            uow = moduleInstance.unitOfWorkFactory().newUnitOfWork();

            EntityBuilder<Account> eBuilder = uow.newEntityBuilder( Account.class, newId );

            Account instance = eBuilder.instance();

            instance.balance().set( new BigDecimal( "323423.87" ) );

            instance.name().set( "Big account." );

            account = eBuilder.newInstance();

            uow.complete();
        }
        catch( EntityNotFoundException enfe )
        {
            uow.discard();
        }

        try
        {
            uow = moduleInstance.unitOfWorkFactory().newUnitOfWork();

            Account acc_after = uow.get( Account.class, newId );

            Assert.assertNotSame( account, acc_after );

            Assert.assertEquals( "Big account.", acc_after.name().get() );

            Assert.assertEquals( new BigDecimal( "323423.87" ), acc_after.balance().get() );
        }
        catch( EntityNotFoundException enfe )
        {
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void delete()
        throws Exception
    {
        dbHandler.executeUpdate( "delete from account" );

        System.err
            .println( "====================================== delete test ======================================" );

        UnitOfWork uow = null;

        Account account = null;

        String newId = Account.class.getName() + ":" + UUID.randomUUID();

        {
            uow = moduleInstance.unitOfWorkFactory().newUnitOfWork();

            EntityBuilder<Account> eBuilder = uow.newEntityBuilder( Account.class, newId );

            Account instance = eBuilder.instance();

            instance.balance().set( new BigDecimal( "323423.87" ) );

            instance.name().set( "Big account." );

            account = eBuilder.newInstance();

            uow.complete();
        }

        // make sure we can find it.
        {
            uow = moduleInstance.unitOfWorkFactory().newUnitOfWork();

            Account acc_after = uow.get( Account.class, newId );

            Assert.assertNotSame( account, acc_after );

            Assert.assertEquals( "Big account.", acc_after.name().get() );

            Assert.assertEquals( new BigDecimal( "323423.87" ), acc_after.balance().get() );
            uow.discard();
        }

        {
            uow = moduleInstance.unitOfWorkFactory().newUnitOfWork();

            Account acc_after = uow.get( Account.class, newId );

            uow.remove( acc_after );

            uow.complete();
        }

        // make sure we can not find it.
        try
        {
            uow = moduleInstance.unitOfWorkFactory().newUnitOfWork();

            Account acc_after = uow.get( Account.class, newId );

            if( acc_after != null )
            {
                Assert.fail( "should nnot have found anything" );
            }
        }
        catch( NoSuchEntityException nsee )
        {
            System.err.println( "NoSuchEntityException is thrown. Correct behavior." );
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void update()
        throws Exception
    {
        dbHandler.executeUpdate( "delete from account" );

        System.err
            .println( "====================================== update test ======================================" );

        UnitOfWork uow = null;

        Account account = null;

        String newId = Account.class.getName() + ":" + UUID.randomUUID();

        try
        {
            uow = moduleInstance.unitOfWorkFactory().newUnitOfWork();

            EntityBuilder<Account> eBuilder = uow.newEntityBuilder( Account.class, newId );

            Account instance = eBuilder.instance();

            instance.balance().set( new BigDecimal( "323423.87" ) );

            instance.name().set( "Big account." );

            account = eBuilder.newInstance();

            uow.complete();
        }
        catch( Throwable enfe )
        {
            uow.discard();
        }

        // find and update it.
        Account acc_after = null;
        {
            uow = moduleInstance.unitOfWorkFactory().newUnitOfWork();

            acc_after = uow.get( Account.class, newId );

            Assert.assertNotSame( account, acc_after );

            Assert.assertEquals( "Big account.", acc_after.name().get() );

            Assert.assertEquals( new BigDecimal( "323423.87" ), acc_after.balance().get() );

            acc_after.balance().set( new BigDecimal( "23.45" ) );

            acc_after.name().set( "Small Account" );

            uow.complete();
        }

        // make sure we can not find it and changes are persisted.
        Account updatedAcc = null;
        try
        {
            uow = moduleInstance.unitOfWorkFactory().newUnitOfWork();

            updatedAcc = uow.get( Account.class, newId );

            Assert.assertNotSame( acc_after, updatedAcc );

            Assert.assertEquals( "Small Account", updatedAcc.name().get() );

            Assert.assertEquals( new BigDecimal( "23.45" ), updatedAcc.balance().get() );
        }
        catch( NoSuchEntityException nsee )
        {
            System.err.println( "NoSuchEntityException is thrown. Correct behavior." );
        }
        finally
        {
            uow.discard();
        }
    }
}
