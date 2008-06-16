/* Copyright 2008 Neo Technology, http://neotechnology.com.
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
package org.qi4j.entity.neo4j.test;

import java.util.Collection;
import org.junit.Before;
import org.neo4j.api.core.Transaction;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.neo4j.Configuration;
import org.qi4j.entity.neo4j.NeoTransactionService;
import org.qi4j.test.AbstractQi4jTest;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public abstract class TestBase extends AbstractQi4jTest
{

    // Test framework stuff...

    private NeoTransactionService txFactory;
    private final boolean useTxService;
    private final Assembler serviceAssembler;
    private final Class<? extends EntityComposite>[] composites;
    private final boolean useIdGenerator;

    protected TestBase( Configuration config, boolean useTxService, boolean useIdGenerator, Class<? extends EntityComposite>... composites )
    {
        assert ( config == Configuration.DIRECT || config == Configuration.INDIRECT ) : "Unsuported Configuration";
        if( config == Configuration.INDIRECT && !useTxService )
        {
            throw new IllegalArgumentException( "The Indirect Neo Entity Store needs a transaction manager." );
        }
        this.useTxService = useTxService;
        this.useIdGenerator = useIdGenerator;
        this.serviceAssembler = config;
        this.composites = composites;
    }

    protected final void perform( TestExecutor executor ) throws Exception
    {
        if( !useTxService )
        {
            Transaction tx = txFactory.beginTx();
            try
            {
                setup( executor );
                tx.success();
            }
            finally
            {
                tx.finish();
            }
            tx = txFactory.beginTx();
            try
            {
                verify( executor );
                tx.success();
            }
            finally
            {
                tx.finish();
            }
        }
        else
        {
            setup( executor );
            verify( executor );
        }
    }

    private void setup( TestExecutor executor ) throws Exception
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            TestExecutor.setup( uow, executor );
        }
        finally
        {
            try
            {
                uow.complete();
            }
            catch( Exception e )
            {
                System.err.println( "uow.complete(); failed in makeCase( ... )" );
                e.printStackTrace();
            }
        }
    }

    private void verify( TestExecutor executor ) throws Exception
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            TestExecutor.verify( uow, executor );
        }
        finally
        {
            try
            {
                uow.complete();
            }
            catch( Exception e )
            {
                System.err.println( "uow.complete(); failed in makeCase( ... )" );
                e.printStackTrace();
            }
        }
    }

    protected final <T extends Collection> Case<T> perform( CollectionBuilder<T>... builders ) throws UnitOfWorkCompletionException
    {
        Case test;
        if( !useTxService )
        {
            Transaction tx = txFactory.beginTx();
            try
            {
                test = makeCase( builders );
                tx.success();
            }
            finally
            {
                tx.finish();
            }
            tx = txFactory.beginTx();
            try
            {
                executeVerification( test );
            }
            finally
            {
                tx.finish();
            }
        }
        else
        {
            test = makeCase( builders );
            executeVerification( test );
        }
        return test;
    }

    protected final <T extends Collection> void verify( Case<T> test, String name, int... expected ) throws UnitOfWorkCompletionException
    {
        if( !useTxService )
        {
            Transaction tx = txFactory.beginTx();
            try
            {
                executeVerification( test, name, expected );
                tx.success();
            }
            finally
            {
                tx.finish();
            }
        }
        else
        {
            executeVerification( test, name, expected );
        }
    }

    private <T extends Collection> void executeVerification( Case<T> test, String name, int[] expected ) throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            CollectionBuilder.verify( uow, test, name, expected );
        }
        finally
        {
            try
            {
                uow.complete();
            }
            catch( Exception e )
            {
                System.err.println( "uow.complete(); failed in explicit executeVerification( ... )" );
                e.printStackTrace();
            }
        }
    }

    private <T extends Collection> void executeVerification( Case<T> test ) throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            CollectionBuilder.verify( uow, test );
        }
        finally
        {
            try
            {
                uow.complete();
            }
            catch( Exception e )
            {
                System.err.println( "uow.complete(); failed in implicit executeVerification( ... )" );
                e.printStackTrace();
            }
        }
    }

    private <T extends Collection> Case makeCase( CollectionBuilder<T>[] builders ) throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            return CollectionBuilder.produce( uow, builders );
        }
        finally
        {
            try
            {
                uow.complete();
            }
            catch( Exception e )
            {
                System.err.println( "uow.complete(); failed in makeCase( ... )" );
                e.printStackTrace();
            }
        }
    }

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        serviceAssembler.assemble( module );
        if( useIdGenerator )
        {
            Configuration.IDENTITY_GENERATOR.assemble( module );
        }
        module.addEntities( ElementOwnerComposite.class, ContainedElementComposite.class );
        if( composites.length != 0 )
        {
            module.addEntities( composites );
        }
    }

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        if( !useTxService )
        {
            txFactory = serviceLocator.findService( NeoTransactionService.class ).get();
        }
    }
}
