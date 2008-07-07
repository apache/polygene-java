/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.logging.tracing;

import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.ConcernOf;
import org.qi4j.composite.Concerns;
import org.qi4j.composite.Mixins;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.trace.Trace;
import org.qi4j.trace.TraceAllConcern;
import org.qi4j.trace.records.CompositeTraceRecordEntity;
import org.qi4j.trace.records.EntityTraceRecordEntity;
import org.qi4j.trace.records.ServiceTraceRecordEntity;
import org.qi4j.trace.assemblies.StandardTraceServiceComposite;
import org.qi4j.trace.service.TraceServiceConfiguration;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;

public class TracingTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( SomeComposite.class );
        module.addServices( StandardTraceServiceComposite.class );
        module.addServices( MemoryEntityStoreService.class );
        module.addServices( UuidIdentityGeneratorService.class );
        module.addEntities( CompositeTraceRecordEntity.class );
        module.addEntities( EntityTraceRecordEntity.class );
        module.addEntities( ServiceTraceRecordEntity.class );
    }

    @Test
    public void testTrace()
        throws Exception
    {
        SomeComposite sc = compositeBuilderFactory.newCompositeBuilder( SomeComposite.class ).newInstance();
        sc.doSomethingImportant();
        sc.doSomethingLessImportant();
    }

    @Mixins( SomeMixin.class )
    @Concerns( { SomeConcern.class, TraceAllConcern.class } )
    public interface SomeComposite extends Some, Composite
    {
    }

    public interface Some
    {
        @Trace int doSomethingImportant();

        int doSomethingLessImportant();
    }

    public static class SomeConcern extends ConcernOf<Some>
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
    }
}
