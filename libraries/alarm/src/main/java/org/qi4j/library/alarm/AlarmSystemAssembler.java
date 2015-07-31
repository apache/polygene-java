/*
 * Copyright (c) 2011, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.alarm;

import org.qi4j.bootstrap.Assemblers;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ServiceDeclaration;

public class AlarmSystemAssembler
    extends Assemblers.VisibilityIdentity<AlarmSystemAssembler>
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        ServiceDeclaration alarmSystem = module.services( AlarmSystemService.class ).visibleIn( visibility() );
        if( hasIdentity() )
        {
            alarmSystem.identifiedBy( identity() );
        }
        module.services( SimpleAlarmModelService.class ).setMetaInfo( new AlarmModelDescriptor( "Simple", false ) );
        module.services( StandardAlarmModelService.class ).setMetaInfo( new AlarmModelDescriptor( "Standard", true ) );
        module.services( ExtendedAlarmModelService.class ).setMetaInfo( new AlarmModelDescriptor( "Extended", false ) );
        module.services( AlarmPointFactory.class );
        module.entities( AlarmPointEntity.class );

        module.values( AlarmEvent.class );
        module.values( AlarmStatus.class );
        module.values( AlarmCategory.class );
        module.values( SimpleAlarmCategory.class );

        module.transients( AlarmProxy.class );
        module.services( AlarmProxy.Factory.class );
        module.forMixin( AlarmPoint.class ).declareDefaults().alarmClass().set( AlarmClass.B );
    }
}
