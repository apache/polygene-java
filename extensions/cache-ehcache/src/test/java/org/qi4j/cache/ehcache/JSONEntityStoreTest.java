package org.qi4j.cache.ehcache;

import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationService;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class JSONEntityStoreTest
{

    private static SingletonAssembler assembler;

    @BeforeClass
    public static void setup()
            throws Exception
    {
        assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                    throws AssemblyException
            {
                module.entities(
                        Account.class
                );

                ModuleAssembly cacheCfgModule = module.layer().application().layer("configLayer").module( "configModule" );

                cacheCfgModule.services( MemoryEntityStoreService.class )
                        .instantiateOnStartup()
                        .visibleIn(Visibility.module);

                cacheCfgModule.services( UuidIdentityGeneratorService.class ).visibleIn( Visibility.module );
                cacheCfgModule.services( OrgJsonValueSerializationService.class ).taggedWith( ValueSerialization.Formats.JSON );
                cacheCfgModule.entities( EhCacheConfiguration.class).visibleIn(Visibility.application);

                module.layer().uses( cacheCfgModule.layer() );

                module.services( EhCachePoolService.class)
                        .visibleIn(Visibility.module)
                        .identifiedBy("ehcache");


                new EntityTestAssembler()
                        .visibleIn( Visibility.module)
                        .assemble( module);

            }
        };

    }

    @Test
    public void cached_NEW_State()
            throws Exception
    {

        UnitOfWork uow1 = assembler.module().newUnitOfWork();
        EntityBuilder<Account> b = uow1.newEntityBuilder(Account.class);

        b.instance().name().set("account1");
        b.instance().balance().set( BigDecimal.ZERO );

        String accountId = b.newInstance().identity().get();

        uow1.complete();

        UnitOfWork uow2 = assembler.module().newUnitOfWork();
        Account account2 = uow2.get(Account.class, accountId);
        account2.balance().set( BigDecimal.ONE);
        uow2.complete();

    }

    @Test
    public void globalStateClone()
            throws Exception
    {

        UnitOfWork uow1 = assembler.module().newUnitOfWork();
        EntityBuilder<Account> b = uow1.newEntityBuilder(Account.class);

        b.instance().name().set("account1");
        b.instance().balance().set( BigDecimal.ZERO );

        String accountId = b.newInstance().identity().get();

        uow1.complete();

        UnitOfWork uow2 = assembler.module().newUnitOfWork();
        Account account2 = uow2.get( Account.class, accountId);
        account2.balance().set( BigDecimal.ONE);
        uow2.complete();

        UnitOfWork uow3 = assembler.module().newUnitOfWork();
        Account account3 = uow3.get( Account.class, accountId);
        account3.balance().set( BigDecimal.TEN);
        uow3.discard();

        UnitOfWork uow4 = assembler.module().newUnitOfWork();
        Account account4 = uow4.get( Account.class, accountId);

        assertEquals( BigDecimal.ONE, account4.balance().get());

        uow4.discard();
    }

        public interface Account
            extends EntityComposite
    {

        Property<String> name();

        Property<BigDecimal> balance();
    }

}
