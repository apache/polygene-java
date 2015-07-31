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

package org.qi4j.library.alarm;

import org.junit.Test;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class AlarmProxyTest extends AbstractQi4jTest
{
    // START SNIPPET: documentation
    @Service
    private AlarmProxy.Factory factory;

    private AlarmProxy myAlarmPoint;
    // END SNIPPET: documentation

    // START SNIPPET: documentation
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new AlarmSystemAssembler().assemble( module );
        // END SNIPPET: documentation
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void givenAlarmPointWhenActivateExpectActivationEvent()
        throws Exception
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
// START SNIPPET: documentation
            myAlarmPoint = factory.create( "This Alarm Identity", "ProActiveCRM", "Sales", AlarmClass.B );
            myAlarmPoint.history().maxSize().set( 20 );
// END SNIPPET: documentation

// START SNIPPET: documentation
            myAlarmPoint.activate();
// END SNIPPET: documentation
            uow.complete();
            assertThat( myAlarmPoint.history().activateCounter(), equalTo( 1 ) );
            AlarmEvent event = myAlarmPoint.history().firstEvent();
            assertThat( event, notNullValue() );
            assertThat( event.alarmIdentity().get(), equalTo( "This Alarm Identity" ) );
            assertThat( event.newStatus().get().name( null ), equalTo( "Activated" ) );
            assertThat( event.oldStatus().get().name( null ), equalTo( "Normal" ) );
        }
        catch( Exception e )
        {
            uow.discard();
            throw e;
        }
    }
}
