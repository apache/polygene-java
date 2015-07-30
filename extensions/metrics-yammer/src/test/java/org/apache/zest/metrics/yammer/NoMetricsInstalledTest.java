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

package org.apache.zest.metrics.yammer;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.reporting.ConsoleReporter;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

public class NoMetricsInstalledTest extends AbstractZestTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( Person.class );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void givenMetricsEnabledZestWhenManyEntityChangesExpectCounterToBeOneOrZeroAndChangeRateHigh()
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
