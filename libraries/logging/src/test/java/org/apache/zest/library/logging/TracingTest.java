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

package org.apache.zest.library.logging;

import org.junit.Test;
import org.apache.zest.api.concern.ConcernOf;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.query.Query;
import org.apache.zest.api.query.QueryBuilder;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.apache.zest.library.logging.trace.Trace;
import org.apache.zest.library.logging.trace.TraceAllConcern;
import org.apache.zest.library.logging.trace.TraceConcern;
import org.apache.zest.library.logging.trace.assemblies.TracingAssembler;
import org.apache.zest.library.logging.trace.records.CompositeTraceRecordEntity;
import org.apache.zest.library.logging.trace.records.EntityTraceRecordEntity;
import org.apache.zest.library.logging.trace.records.ServiceTraceRecordEntity;
import org.apache.zest.library.logging.trace.records.TraceRecord;
import org.apache.zest.test.AbstractPolygeneTest;

import java.util.Iterator;
import org.apache.zest.test.EntityTestAssembler;

import static org.junit.Assert.*;
import static org.apache.zest.api.query.QueryExpressions.orderBy;
import static org.apache.zest.api.query.QueryExpressions.templateFor;

public class TracingTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( SomeService.class ).instantiateOnStartup();
        module.services( SomeService2.class ).instantiateOnStartup();
        new TracingAssembler().assemble( module );
        new EntityTestAssembler().assemble( module );
        new RdfMemoryStoreAssembler().assemble( module );
        module.entities( CompositeTraceRecordEntity.class );
        module.entities( EntityTraceRecordEntity.class );
        module.entities( ServiceTraceRecordEntity.class );
    }

    @Test
    public void whenTraceOnMixinTypeMethodExpectOneEntryInEntityStore()
        throws Exception
    {
        SomeService sc = serviceFinder.findService( SomeService.class ).get();
        assertEquals( 123, sc.doSomethingImportant() );
        assertEquals( 456, sc.doSomethingLessImportant() );
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        QueryBuilder<TraceRecord> builder = queryBuilderFactory.newQueryBuilder( TraceRecord.class );
        Query<TraceRecord> query = uow.newQuery( builder );
        // IS sorting needed??
//        TraceRecord template = templateFor( TraceRecord.class );
//        query.orderBy( orderBy( template.methodName() ) );
        Iterator<TraceRecord> result = query.iterator();
        assertTrue( result.hasNext() );
        TraceRecord rec1 = result.next();
        assertEquals( "doSomethingImportant", rec1.methodName().get() );
        assertFalse( result.hasNext() );
        uow.complete();
    }

    @Test
    public void whenTraceAllOnCompositeTypeExpectTwoEntryInEntityStore()
        throws Exception
    {
        SomeService2 sc = serviceFinder.findService( SomeService2.class ).get();
        assertEquals( 123, sc.doSomethingImportant() );
        assertEquals( 456, sc.doSomethingLessImportant() );
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        QueryBuilder<TraceRecord> builder = queryBuilderFactory.newQueryBuilder( TraceRecord.class );
        Query<TraceRecord> query = uow.newQuery( builder );
        // IS sorting needed??
//        TraceRecord template = templateFor( TraceRecord.class );
//        query.orderBy( orderBy( template.methodName() ) );
        Iterator<TraceRecord> result = query.iterator();
        assertTrue( result.hasNext() );
        TraceRecord rec1 = result.next();
        assertEquals( "doSomethingImportant", rec1.methodName().get() );
        assertTrue( result.hasNext() );
        TraceRecord rec2 = result.next();
        assertEquals( "doSomethingLessImportant", rec2.methodName().get() );
        assertFalse( result.hasNext() );
        uow.complete();
    }

    @Test
    public void whenTraceOnMixinImplExpectTwoEntryInEntityStore()
        throws Exception
    {
        SomeService sc = serviceFinder.findService( SomeService.class ).get();
        assertEquals( 123, sc.doSomethingImportant() );
        assertEquals( 789, sc.doSomethingModeratelyImportant() );
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            QueryBuilder<TraceRecord> builder = queryBuilderFactory.newQueryBuilder( TraceRecord.class );
            Query<TraceRecord> query = uow.newQuery( builder );
            // IS sorting needed??
            TraceRecord template = templateFor( TraceRecord.class );
            query.orderBy( orderBy( template.methodName() ) );
            Iterator<TraceRecord> result = query.iterator();
            assertTrue( result.hasNext() );
            TraceRecord rec1 = result.next();
            assertEquals( "doSomethingImportant", rec1.methodName().get() );
            assertTrue( result.hasNext() );
            TraceRecord rec2 = result.next();
            assertEquals( "doSomethingModeratelyImportant", rec2.methodName().get() );
            assertFalse( result.hasNext() );
            uow.complete();
        }
        catch( Exception e )
        {
            uow.discard();
            throw e;
        }
        catch( Error e )
        {
            uow.discard();
            throw e;
        }
    }

    @Test
    public void whenTraceOnConcernExpectOneEntryInEntityStore()
        throws Exception
    {
        // It is not possible to put Annotation on Concern Methods, so it should only record one.

        SomeService sc = serviceFinder.findService( SomeService.class ).get();
        assertEquals( 123, sc.doSomethingImportant() );
        assertEquals( 753, sc.doSomethingInsanelyImportant() );
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        QueryBuilder<TraceRecord> builder = queryBuilderFactory.newQueryBuilder( TraceRecord.class );
        Query<TraceRecord> query = uow.newQuery( builder );
        // IS sorting needed??
//        TraceRecord template = templateFor( TraceRecord.class );
//        query.orderBy( orderBy( template.methodName() ) );
        Iterator<TraceRecord> result = query.iterator();
        assertTrue( result.hasNext() );
        TraceRecord rec1 = result.next();
        assertEquals( "doSomethingImportant", rec1.methodName().get() );
        assertFalse( result.hasNext() );
        uow.complete();
    }

    @Mixins( SomeMixin.class )
    @Concerns( { SomeConcern.class, TraceConcern.class } )
    public interface SomeService
        extends Some, ServiceComposite
    {
    }

    @Mixins( SomeMixin.class )
    @Concerns( { SomeConcern.class, TraceAllConcern.class } )
    public interface SomeService2
        extends Some, ServiceComposite
    {
    }

    public interface Some
    {
        @Trace
        int doSomethingImportant();

        int doSomethingLessImportant();

        int doSomethingModeratelyImportant();

        int doSomethingInsanelyImportant();
    }

    public static class SomeConcern
        extends ConcernOf<Some>
        implements Some
    {
        public int doSomethingImportant()
        {
            System.out.println( "-- doSomethingImportant()" );
            return next.doSomethingImportant();
        }

        public int doSomethingLessImportant()
        {
            System.out.println( "-- doSomethingLessImportant()" );
            return next.doSomethingLessImportant();
        }

        public int doSomethingModeratelyImportant()
        {
            System.out.println( "-- doSomethingLessImportant()" );
            return next.doSomethingModeratelyImportant();
        }

        @Trace
        public int doSomethingInsanelyImportant()
        {
            System.out.println( "-- doSomethingInsanelyImportant()" );
            return next.doSomethingInsanelyImportant();
        }
    }

    public static class SomeMixin
        implements Some
    {

        public int doSomethingImportant()
        {
            System.out.println( "---- doSomethingImportant()" );
            return 123;
        }

        public int doSomethingLessImportant()
        {
            System.out.println( "---- doSomethingLessImportant()" );
            return 456;
        }

        @Trace
        public int doSomethingModeratelyImportant()
        {
            System.out.println( "---- doSomethingModeratelyImportant()" );
            return 789;
        }

        public int doSomethingInsanelyImportant()
        {
            System.out.println( "---- doSomethingInsanelyImportant()" );
            return 753;
        }
    }
}
