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

package org.apache.zest.library.alarm;

import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.value.ValueBuilder;

@Mixins(AlarmPointFactory.Mixin.class)
public interface AlarmPointFactory extends ServiceComposite
{
    AlarmPoint create(String identity, String systemName, String categoryName, AlarmClass alarmClass );
    
    abstract class Mixin
        implements AlarmPointFactory {

        @Structure
        private Module module;
        
        @Override
        public AlarmPoint create( String identity, String systemName, String categoryName, AlarmClass alarmClass )
        {
            UnitOfWork uow = module.currentUnitOfWork();
            EntityBuilder<AlarmPoint> builder = uow.newEntityBuilder( AlarmPoint.class, identity );
            builder.instance().category().set( createCategory(categoryName) );
            builder.instance().alarmClass().set( alarmClass );

            AlarmPoint.AlarmState prototype = builder.instanceFor( AlarmPoint.AlarmState.class );
            AlarmStatus normal = createNormalAlarmStatus();
            prototype.systemName().set( systemName );
            prototype.currentStatus().set( normal );

            return builder.newInstance();
        }

        private AlarmStatus createNormalAlarmStatus()
        {
            ValueBuilder<AlarmStatus> builder = module.newValueBuilder( AlarmStatus.class );
            builder.prototypeFor(AlarmStatus.State.class).name().set( AlarmPoint.STATUS_NORMAL );
            return builder.newInstance();
        }

        private AlarmCategory createCategory( String categoryName )
        {
            ValueBuilder<AlarmCategory> builder = module.newValueBuilder( AlarmCategory.class );
            builder.prototype().name().set( categoryName );
            return builder.newInstance();
        }
    }
}
