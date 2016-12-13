/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.test.performance.entitystore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Callable;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.service.ServiceFinder;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.bootstrap.ApplicationAssemblerAdapter;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.Energy4Java;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.polygene.api.usecase.UsecaseBuilder.newUsecase;

/**
 * Performance Test Suite for Entity Stores.
 */
public abstract class AbstractEntityStorePerformanceTest
{
    private final String storeName;
    private final Assembler infrastructure;
    private final Logger logger;
    private Application application;
    protected UnitOfWorkFactory uowf;
    protected ServiceFinder serviceFinder;

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
            Assembler assembler = module -> module.entities( SimpleProduct.class );
            createPolygeneRuntime( assembler );

            for( int i = 0; i < 10000; i++ )
            {
                try (UnitOfWork uow = uowf.newUnitOfWork( newUsecase( "Warmup " + i ) ))
                {
                    SimpleProduct product = uow.newEntity( SimpleProduct.class );
                    product.identity().get();
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
            Assembler assembler = module -> module.entities( SimpleProduct.class );
            createPolygeneRuntime( assembler );

            profile( () -> {
                Report report = new Report( storeName );
                report.start( "createEntityWithSingleProperty" );
                for( int i = 0; i < ITERATIONS; i++ )
                {
                    try (UnitOfWork uow = uowf.newUnitOfWork( newUsecase( "createEntityWithSingleProperty " + i ) ))
                    {
                        SimpleProduct product = uow.newEntity( SimpleProduct.class );
                        product.identity().get();
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
            Assembler assembler = module -> module.entities( SimpleProduct.class );
            createPolygeneRuntime( assembler );
            profile( () -> {
                Report report = new Report( storeName );
                report.start( "createEntityInBulkWithSingleProperty" );
                int bulk = 0;
                UnitOfWork uow = uowf.newUnitOfWork( newUsecase( "createEntityInBulkWithSingleProperty " + bulk ) );
                for( int i = 0; i < ITERATIONS; i++ )
                {
                    SimpleProduct product = uow.newEntity( SimpleProduct.class );
                    product.identity().get();
                    if( i % 1000 == 0 )
                    {
                        uow.complete();
                        bulk++;
                        uow = uowf.newUnitOfWork( newUsecase( "createEntityInBulkWithSingleProperty " + bulk ) );
                    }
                }
                uow.complete();
                report.stop( ITERATIONS );
                writeReport( report );
                return null;
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
            Assembler assembler = module -> module.entities( ComplexProduct.class );
            createPolygeneRuntime( assembler );
            profile( () -> {
                Report report = new Report( storeName );
                report.start( "createEntityWithComplexType" );
                for( int i = 0; i < ITERATIONS; i++ )
                {
                    try (UnitOfWork uow = uowf.newUnitOfWork( newUsecase( "createEntityWithComplexType " + i ) ))
                    {
                        ComplexProduct product = uow.newEntity( ComplexProduct.class );
                        product.identity().get();
                        uow.complete();
                    }
                }
                report.stop( ITERATIONS );
                writeReport( report );
                return null;
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
            Assembler assembler = module -> module.entities( ComplexProduct.class );
            createPolygeneRuntime( assembler );
            profile( () -> {
                Report report = new Report( storeName );
                report.start( "createEntityInBulkWithComplexType" );
                int bulk = 0;
                UnitOfWork uow = uowf.newUnitOfWork( newUsecase( "createEntityInBulkWithComplexType " + bulk ) );
                for( int i = 0; i < ITERATIONS; i++ )
                {
                    ComplexProduct product = uow.newEntity( ComplexProduct.class );
                    product.identity().get();
                    if( i % 1000 == 0 )
                    {
                        uow.complete();
                        bulk++;
                        uow = uowf.newUnitOfWork( newUsecase( "createEntityInBulkWithComplexType " + bulk ) );
                    }
                }
                uow.complete();
                report.stop( ITERATIONS );
                writeReport( report );
                return null;
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
            Assembler assembler = module -> module.entities( ComplexProduct.class );
            createPolygeneRuntime( assembler );
            {
                int bulk = 0;
                UnitOfWork uow = uowf.newUnitOfWork( newUsecase( "readEntityWithComplexType PREPARE " + bulk ) );
                for( int i = 0; i < ITERATIONS; i++ )
                {
                    ComplexProduct product = uow.newEntity( ComplexProduct.class, new StringIdentity( "product" + i ) );
                    product.name().set( "Product " + i );

                    if( i % 1000 == 0 )
                    {
                        uow.complete();
                        bulk++;
                        uow = uowf.newUnitOfWork( newUsecase( "readEntityWithComplexType PREPARE " + bulk ) );
                    }
                }
                uow.complete();
            }

            profile( () -> {
                Report report = new Report( storeName );
                int bulk = 0;
                UnitOfWork uow = uowf.newUnitOfWork( newUsecase( "readEntityWithComplexType " + bulk ) );
                Random rnd = new Random();
                report.start( "readEntityWithComplexType" );
                String id = rnd.nextInt( ITERATIONS ) + "";
                for( int i = 0; i < ITERATIONS; i++ )
                {
                    ComplexProduct product = uow.get( ComplexProduct.class, new StringIdentity( "product" + id ) );
                    product.name().get();
                    if( i % 100 == 0 )
                    {
                        uow.discard();
                        bulk++;
                        uow = uowf.newUnitOfWork( newUsecase( "readEntityWithComplexType " + bulk ) );
                    }
                }
                uow.complete();
                report.stop( ITERATIONS );
                writeReport( report );
                return null;
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
        if( !dir.mkdirs() )
        {
            System.out.println( "Couldn't create Performance result directory." );
        }
        String name = dir.getAbsolutePath() + "/result-" + report.name() + ".xml";
        FileWriter writer = new FileWriter( name, true );
        try (BufferedWriter out = new BufferedWriter( writer ))
        {
            report.writeTo( out );
            out.flush();
        }
        System.out.println( "Report written to " + name );
    }

    private void createPolygeneRuntime( Assembler testSetup )
        throws Exception
    {
        Energy4Java polygene = new Energy4Java();
        Assembler[][][] assemblers = new Assembler[][][]
            {
                {
                    {
                        infrastructure, testSetup
                    }
                }
            };
        application = polygene.newApplication( new ApplicationAssemblerAdapter( assemblers )
        {
        } );
        application.activate();

        Module moduleInstance = application.findModule( "Layer 1", "Module 1" );
        uowf = moduleInstance.unitOfWorkFactory();
        serviceFinder = moduleInstance;
    }

    protected void cleanUp()
        throws Exception
    {
        try
        {
            if( uowf != null && uowf.isUnitOfWorkActive() )
            {
                UnitOfWork current;
                while( uowf.isUnitOfWorkActive() && ( current = uowf.currentUnitOfWork() ) != null )
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
