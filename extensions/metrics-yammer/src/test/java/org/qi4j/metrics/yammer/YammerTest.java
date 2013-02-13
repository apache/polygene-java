/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.metrics.yammer;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.reporting.ConsoleReporter;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

public class YammerTest extends AbstractQi4jTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( Person.class );
        new EntityTestAssembler().assemble( module );
        new YammerMetricsAssembler().assemble( module );
    }

    @Test
    public void givenMetricsEnabledQi4jWhenManyEntityChangesExpectCounterToBeOneOrZeroAndChangeRateHigh()
        throws UnitOfWorkCompletionException
    {
        ConsoleReporter reporter = new ConsoleReporter( Metrics.defaultRegistry(), System.out, MetricPredicate.ALL );
        reporter.start( 100, TimeUnit.MILLISECONDS );
        for( int i=0; i < 20000; i++ )
        {
            createEntity(i);
        }
        for( int i=0; i < 20000; i++ )
        {
            readEntity( i );
        }
    }

    private void readEntity( int id )
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            Person p = uow.get( Person.class, "" + id );
        }
        finally
        {
            if( uow.isOpen())
                uow.discard();
        }
    }

    private void createEntity( int id )
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            uow.newEntity( Person.class, "" + id );
            uow.complete();
        }
        finally
        {
            if( uow.isOpen())
                uow.discard();
        }
    }

    public interface Person extends EntityComposite
    {
        @Optional
        Property<String> name();

    }
}
