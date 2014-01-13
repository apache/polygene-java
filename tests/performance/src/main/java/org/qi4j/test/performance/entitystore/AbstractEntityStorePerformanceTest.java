/*
 * Copyright (c) 2008-2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008-2013, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2012-2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.test.performance.entitystore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Callable;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.ApplicationAssemblerAdapter;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.ModuleAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;

/**
 * Performance Test Suite for Entity Stores.
 */
public abstract class AbstractEntityStorePerformanceTest
{
    private final String storeName;
    private final Assembler infrastructure;
    private final Logger logger;
    private Application application;
    protected Module module;

    private final int ITERATIONS = 20000;

    protected AbstractEntityStorePerformanceTest( String storeName, Assembler infrastructure )
    {
        this.storeName = storeName;
        this.infrastructure = infrastructure;
        this.logger = LoggerFactory.getLogger( getClass().getPackage().getName() + "." + storeName );
    }

    @Before
    public void warmup()
        throws Exception
    {
        try
        {
            Assembler assembler = new Assembler()
            {
                @Override
                public void assemble( ModuleAssembly module )
                    throws AssemblyException
                {
                    module.entities( SimpleProduct.class );
                }
            };
            createQi4jRuntime( assembler );

            for( int i = 0; i < 10000; i++ )
            {
                try( UnitOfWork uow = module.newUnitOfWork( newUsecase( "Warmup " + i ) ) )
                {
                    SimpleProduct product = uow.newEntity( SimpleProduct.class );
                    String id = product.identity().get();
                }
            }
        }
        catch( Exception ex )
        {
            logger.error( "Unable to warmup: {}", ex.getMessage(), ex );
            throw ex;
        }
        finally
        {
            cleanUp();
        }
    }

    @Test
    public void whenCreateEntityWithSinglePropertyThenRecordIterationsPerSecond()
        throws Exception
    {
        try
        {
            Assembler assembler = new Assembler()
            {
                @Override
                public void assemble( ModuleAssembly module )
                    throws AssemblyException
                {
                    module.entities( SimpleProduct.class );
                }
            };
            createQi4jRuntime( assembler );

            profile( new Callable<Void>()
            {
                @Override
                public Void call()
                    throws Exception
                {
                    Report report = new Report( storeName );
                    report.start( "createEntityWithSingleProperty" );
                    for( int i = 0; i < ITERATIONS; i++ )
                    {
                        try( UnitOfWork uow = module.newUnitOfWork( newUsecase( "createEntityWithSingleProperty " + i ) ) )
                        {
                            SimpleProduct product = uow.newEntity( SimpleProduct.class );
                            String id = product.identity().get();
                            uow.complete();
                        }
                        if( i % 1000 == 0 )
                        {
                            logger.info( "Iteration {}", i );
                        }
                    }
                    report.stop( ITERATIONS );
                    writeReport( report );
                    return null;
                }
            } );
        }
        finally
        {
            cleanUp();
        }
    }

    @Test
    public void whenCreateEntityWithSinglePropertyInBatchThenRecordIterationsPerSecond()
        throws Exception
    {
        try
        {
            Assembler assembler = new Assembler()
            {
                @Override
                public void assemble( ModuleAssembly module )
                    throws AssemblyException
                {
                    module.entities( SimpleProduct.class );
                }
            };
            createQi4jRuntime( assembler );
            profile( new Callable<Void>()
            {
                @Override
                public Void call()
                    throws Exception
                {
                    Report report = new Report( storeName );
                    report.start( "createEntityInBulkWithSingleProperty" );
                    int bulk = 0;
                    UnitOfWork uow = module.newUnitOfWork( newUsecase( "createEntityInBulkWithSingleProperty " + bulk ) );
                    for( int i = 0; i < ITERATIONS; i++ )
                    {
                        SimpleProduct product = uow.newEntity( SimpleProduct.class );
                        String id = product.identity().get();
                        if( i % 1000 == 0 )
                        {
                            uow.complete();
                            bulk++;
                            uow = module.newUnitOfWork( newUsecase( "createEntityInBulkWithSingleProperty " + bulk ) );
                        }
                    }
                    uow.complete();
                    report.stop( ITERATIONS );
                    writeReport( report );
                    return null;
                }
            } );
        }
        finally
        {
            cleanUp();
        }
    }

    @Test
    public void whenCreateEntityWithComplexTypeThenRecordIterationsPerSecond()
        throws Exception
    {
        try
        {
            Assembler assembler = new Assembler()
            {
                @Override
                public void assemble( ModuleAssembly module )
                    throws AssemblyException
                {
                    module.entities( ComplexProduct.class );
                }
            };
            createQi4jRuntime( assembler );
            profile( new Callable<Void>()
            {
                @Override
                public Void call()
                    throws Exception
                {
                    Report report = new Report( storeName );
                    report.start( "createEntityWithComplexType" );
                    for( int i = 0; i < ITERATIONS; i++ )
                    {
                        try( UnitOfWork uow = module.newUnitOfWork( newUsecase( "createEntityWithComplexType " + i ) ) )
                        {
                            ComplexProduct product = uow.newEntity( ComplexProduct.class );
                            String id = product.identity().get();
                            uow.complete();
                        }
                    }
                    report.stop( ITERATIONS );
                    writeReport( report );
                    return null;
                }
            } );
        }
        finally
        {
            cleanUp();
        }
    }

    @Test
    public void whenCreateEntityWithComplexTypeInBatchThenRecordIterationsPerSecond()
        throws Exception
    {
        try
        {
            Assembler assembler = new Assembler()
            {
                @Override
                public void assemble( ModuleAssembly module )
                    throws AssemblyException
                {
                    module.entities( ComplexProduct.class );
                }
            };
            createQi4jRuntime( assembler );
            profile( new Callable<Void>()
            {
                @Override
                public Void call()
                    throws Exception
                {
                    Report report = new Report( storeName );
                    report.start( "createEntityInBulkWithComplexType" );
                    int bulk = 0;
                    UnitOfWork uow = module.newUnitOfWork( newUsecase( "createEntityInBulkWithComplexType " + bulk ) );
                    for( int i = 0; i < ITERATIONS; i++ )
                    {
                        ComplexProduct product = uow.newEntity( ComplexProduct.class );
                        String id = product.identity().get();
                        if( i % 1000 == 0 )
                        {
                            uow.complete();
                            bulk++;
                            uow = module.newUnitOfWork( newUsecase( "createEntityInBulkWithComplexType " + bulk ) );
                        }
                    }
                    uow.complete();
                    report.stop( ITERATIONS );
                    writeReport( report );
                    return null;
                }
            } );
        }
        finally
        {
            cleanUp();
        }
    }

    @Test
    public void whenReadEntityWithComplexTypeThenRecordIterationsPerSecond()
        throws Exception
    {
        try
        {
            Assembler assembler = new Assembler()
            {
                @Override
                public void assemble( ModuleAssembly module )
                    throws AssemblyException
                {
                    module.entities( ComplexProduct.class );
                }
            };
            createQi4jRuntime( assembler );
            {
                int bulk = 0;
                UnitOfWork uow = module.newUnitOfWork( newUsecase( "readEntityWithComplexType PREPARE " + bulk ) );
                for( int i = 0; i < ITERATIONS; i++ )
                {
                    ComplexProduct product = uow.newEntity( ComplexProduct.class, "product" + i );
                    product.name().set( "Product " + i );

                    if( i % 1000 == 0 )
                    {
                        uow.complete();
                        bulk++;
                        uow = module.newUnitOfWork( newUsecase( "readEntityWithComplexType PREPARE " + bulk ) );
                    }
                }
                uow.complete();
            }

            profile( new Callable<Void>()
            {
                @Override
                public Void call()
                    throws Exception
                {
                    Report report = new Report( storeName );
                    int bulk = 0;
                    UnitOfWork uow = module.newUnitOfWork( newUsecase( "readEntityWithComplexType " + bulk ) );
                    Random rnd = new Random();
                    report.start( "readEntityWithComplexType" );
                    String id = rnd.nextInt( ITERATIONS ) + "";
                    for( int i = 0; i < ITERATIONS; i++ )
                    {
                        ComplexProduct product = uow.get( ComplexProduct.class, "product" + id );

                        String name = product.name().get();

                        if( i % 100 == 0 )
                        {
                            uow.discard();
                            bulk++;
                            uow = module.newUnitOfWork( newUsecase( "readEntityWithComplexType " + bulk ) );
                        }
                    }
                    uow.complete();
                    report.stop( ITERATIONS );
                    writeReport( report );
                    return null;
                }
            } );
        }
        finally
        {
            cleanUp();
        }
    }

    // If you want to profile this test, then tell profiler to only check
    // below this method call
    private void profile( Callable<Void> runnable )
        throws Exception
    {
        runnable.call();
    }

    private void writeReport( Report report )
        throws IOException
    {
        File dir = new File( "build/reports/perf/" );
        dir.mkdirs();
        String name = dir.getAbsolutePath() + "/result-" + report.name() + ".xml";
        FileWriter writer = new FileWriter( name, true );
        try( BufferedWriter out = new BufferedWriter( writer ) )
        {
            report.writeTo( out );
            out.flush();
        }
        System.out.println( "Report written to " + name );
    }

    private void createQi4jRuntime( Assembler testSetup )
        throws Exception
    {
        Energy4Java qi4j = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
        {
            {
                {
                    infrastructure, testSetup
                }
            }
        };
        application = qi4j.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        application.activate();

        Module moduleInstance = application.findModule( "Layer 1", "Module 1" );
        module = moduleInstance;
    }

    protected void cleanUp()
        throws Exception
    {
        try
        {
            if( module != null && module.isUnitOfWorkActive() )
            {
                UnitOfWork current;
                while( module.isUnitOfWorkActive() && ( current = module.currentUnitOfWork() ) != null )
                {
                    if( current.isOpen() )
                    {
                        current.discard();
                    }
                    else
                    {
                        throw new InternalError( "I have seen a case where a UoW is on the stack, but not opened: "
                                                 + current.usecase().name() );
                    }
                }
                new Exception( "UnitOfWork not properly cleaned up" ).printStackTrace();
            }
        }
        finally
        {
            if( application != null )
            {
                application.passivate();
            }
        }
    }
}
