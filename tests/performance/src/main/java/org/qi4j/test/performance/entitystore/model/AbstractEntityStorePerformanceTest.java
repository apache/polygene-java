package org.qi4j.test.performance.entitystore.model;

import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Performance Test Suite for Entity Stores.
 */
public abstract class AbstractEntityStorePerformanceTest
{
    private Application application;
    private UnitOfWorkFactory unitOfWorkFactory;
    private String storeName;
    private Assembler infrastructure;

    private int ITERATIONS = 20000;

    protected AbstractEntityStorePerformanceTest( String storeName, Assembler infrastructure )
    {
        this.storeName = storeName;
        this.infrastructure = infrastructure;
    }

    @Before
    public void warmup()
        throws Exception
    {
        Assembler assembler = new Assembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.entities( SimpleProduct.class );
            }
        };
        createQi4jRuntime( assembler );

        for( int i = 0; i < 10000; i++ )
        {
            UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
            SimpleProduct product = uow.newEntity( SimpleProduct.class );
            String id = product.identity().get();
            uow.discard();
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
                public void assemble( ModuleAssembly module )
                    throws AssemblyException
                {
                    module.entities( SimpleProduct.class );
                }
            };
            createQi4jRuntime( assembler );

            profile( new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Report report = new Report( storeName );
                        report.start( "createEntityWithSingleProperty" );
                        for( int i = 0; i < ITERATIONS; i++ )
                        {
                            UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
                            SimpleProduct product = uow.newEntity( SimpleProduct.class );
                            String id = product.identity().get();
                            uow.complete();
                            if( i % 1000 == 0 )
                            {
                                Logger.getLogger( getClass().getName() ).info( "Iteration:" + i );
                            }
                        }
                        report.stop( ITERATIONS );
                        writeReport( report );
                    }
                    catch( UnitOfWorkCompletionException e )
                    {
                        e.printStackTrace();
                    }
                    catch( IOException e )
                    {
                        e.printStackTrace();
                    }
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
                public void assemble( ModuleAssembly module )
                    throws AssemblyException
                {
                    module.entities( SimpleProduct.class );
                }
            };
            createQi4jRuntime( assembler );
            profile( new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Report report = new Report( storeName );
                        report.start( "createEntityInBulkWithSingleProperty" );
                        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
                        for( int i = 0; i < ITERATIONS; i++ )
                        {
                            SimpleProduct product = uow.newEntity( SimpleProduct.class );
                            String id = product.identity().get();

                            if( i % 1000 == 0 )
                            {
                                uow.complete();
                                uow = unitOfWorkFactory.newUnitOfWork();
                            }
                        }
                        uow.complete();
                        report.stop( ITERATIONS );
                        writeReport( report );
                    }
                    catch( UnitOfWorkCompletionException e )
                    {
                        e.printStackTrace();
                    }
                    catch( IOException e )
                    {
                        e.printStackTrace();
                    }
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
                public void assemble( ModuleAssembly module )
                    throws AssemblyException
                {
                    module.entities( ComplexProduct.class );
                }
            };
            createQi4jRuntime( assembler );
            profile( new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Report report = new Report( storeName );
                        report.start( "createEntityWithComplexType" );
                        for( int i = 0; i < ITERATIONS; i++ )
                        {
                            UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
                            ComplexProduct product = uow.newEntity( ComplexProduct.class );
                            String id = product.identity().get();
                            uow.complete();
                        }
                        report.stop( ITERATIONS );
                        writeReport( report );
                    }
                    catch( UnitOfWorkCompletionException e )
                    {
                        e.printStackTrace();
                    }
                    catch( IOException e )
                    {
                        e.printStackTrace();
                    }
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
                public void assemble( ModuleAssembly module )
                    throws AssemblyException
                {
                    module.entities( ComplexProduct.class );
                }
            };
            createQi4jRuntime( assembler );
            profile( new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Report report = new Report( storeName );
                        report.start( "createEntityInBulkWithComplexType" );
                        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
                        for( int i = 0; i < ITERATIONS; i++ )
                        {
                            ComplexProduct product = uow.newEntity( ComplexProduct.class );
                            String id = product.identity().get();

                            if( i % 1000 == 0 )
                            {
                                uow.complete();
                                uow = unitOfWorkFactory.newUnitOfWork();
                            }
                        }
                        uow.complete();
                        report.stop( ITERATIONS );
                        writeReport( report );
                    }
                    catch( UnitOfWorkCompletionException e )
                    {
                        e.printStackTrace();
                    }
                    catch( IOException e )
                    {
                        e.printStackTrace();
                    }
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
                public void assemble( ModuleAssembly module )
                    throws AssemblyException
                {
                    module.entities( ComplexProduct.class );
                }
            };
            createQi4jRuntime( assembler );
            {
                UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
                for( int i = 0; i < ITERATIONS; i++ )
                {
                    ComplexProduct product = uow.newEntity( ComplexProduct.class, "product" + i );
                    product.name().set( "Product " + i );

                    if( i % 1000 == 0 )
                    {
                        uow.complete();
                        uow = unitOfWorkFactory.newUnitOfWork();
                    }
                }
                uow.complete();
            }

            profile( new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Report report = new Report( storeName );
                        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
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
                                uow = unitOfWorkFactory.newUnitOfWork();
                            }
                        }
                        uow.complete();
                        report.stop( ITERATIONS );
                        writeReport( report );
                    }
                    catch( UnitOfWorkCompletionException e )
                    {
                        e.printStackTrace();
                    }
                    catch( IOException e )
                    {
                        e.printStackTrace();
                    }
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

    private void profile( Runnable runnable )
    {
        runnable.run();
    }

    private void writeReport( Report report )
        throws IOException
    {
        File dir = new File( "target/perf-result/" );
        dir.mkdirs();
        String name = dir.getAbsolutePath() + "/result-" + report.name() + ".xml";
        FileWriter writer = new FileWriter( name, true );
        BufferedWriter out = new BufferedWriter( writer );
        report.writeTo( out );
        out.flush();
        out.close();
        System.out.println( "Report written to " + name );
    }

    private void createQi4jRuntime( Assembler testSetup )
        throws Exception
    {
        Energy4Java qi4j = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                {
                    { infrastructure, testSetup }
                }
            };
        application = qi4j.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        application.activate();

        Module moduleInstance = application.findModule( "Layer 1", "Module 1" );
        unitOfWorkFactory = moduleInstance;
    }

    protected void cleanUp()
        throws Exception
    {
        if( unitOfWorkFactory != null && unitOfWorkFactory.isUnitOfWorkActive())
        {
            UnitOfWork current;
            while( unitOfWorkFactory.isUnitOfWorkActive() && ( current = unitOfWorkFactory.currentUnitOfWork() ) != null )
            {
                if( current.isOpen() )
                {
                    current.discard();
                }
                else
                {
                    throw new InternalError( "I have seen a case where a UoW is on the stack, but not opened." );
                }
            }
            new Exception( "UnitOfWork not properly cleaned up" ).printStackTrace();
        }

        if( application != null )
        {
            application.passivate();
        }
    }
}