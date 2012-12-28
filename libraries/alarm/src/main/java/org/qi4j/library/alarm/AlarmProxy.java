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

import java.util.List;
import java.util.Locale;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.concern.UnitOfWorkConcern;
import org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation;

@Concerns( UnitOfWorkConcern.class )
@Mixins( AlarmProxy.Mixin.class )
public interface AlarmProxy extends AlarmPoint, TransientComposite
{
    @Mixins( FactoryMixin.class )
    public interface Factory extends ServiceComposite
    {
        @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.REQUIRED)
        AlarmProxy create( String identity, String systemName, String categoryName, AlarmClass alarmClass );
    }

    abstract class FactoryMixin
        implements Factory
    {
        @Structure
        private Module module;

        @Service
        private AlarmPointFactory factory;

        @Override
        public AlarmProxy create( String identity, String systemName, String categoryName, AlarmClass alarmClass )
        {
            UnitOfWork unitOfWork = module.currentUnitOfWork();
            AlarmPoint alarmPoint;
            try
            {
                alarmPoint = unitOfWork.get( AlarmPoint.class, identity );
            }
            catch( NoSuchEntityException e )
            {
                alarmPoint = factory.create( identity, systemName, categoryName, alarmClass );
            }
            TransientBuilder<AlarmProxy> builder = module.newTransientBuilder( AlarmProxy.class );
            builder.prototype().category().set( alarmPoint.category().get() );
            builder.prototype().alarmClass().set( alarmClass );
            builder.use( identity );
            return builder.newInstance();
        }
    }

    abstract class Mixin
        implements AlarmPoint
    {

        @Structure
        private Module module;

        @Uses
        String identity;

        @Override
        @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.REQUIRED )
        public void trigger( String trigger )
            throws IllegalArgumentException
        {
            AlarmPoint alarmPoint = findAlarmPoint();
            alarmPoint.trigger( trigger );
        }

        @Override
        @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.REQUIRED )
        public void activate()
        {
            AlarmPoint alarmPoint = findAlarmPoint();
            alarmPoint.activate();
        }

        @Override
        @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.REQUIRED )
        public void deactivate()
        {
            AlarmPoint alarmPoint = findAlarmPoint();
            alarmPoint.deactivate();
        }

        @Override
        @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.REQUIRED )
        public void acknowledge()
        {
            AlarmPoint alarmPoint = findAlarmPoint();
            alarmPoint.acknowledge();
        }

        @Override
        @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.REQUIRED )
        public boolean currentCondition()
        {
            AlarmPoint alarmPoint = findAlarmPoint();
            return alarmPoint.currentCondition();
        }

        @Override
        @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.REQUIRED )
        public void updateCondition( boolean condition )
        {
            AlarmPoint alarmPoint = findAlarmPoint();
            alarmPoint.updateCondition( condition );
        }

        @Override
        @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.REQUIRED )
        public AlarmStatus currentStatus()
        {
            AlarmPoint alarmPoint = findAlarmPoint();
            return alarmPoint.currentStatus();
        }

        @Override
        @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.REQUIRED )
        public AlarmHistory history()
        {
            AlarmPoint alarmPoint = findAlarmPoint();
            return alarmPoint.history();
        }

        @Override
        @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.REQUIRED )
        public List<String> attributeNames()
        {
            AlarmPoint alarmPoint = findAlarmPoint();
            return alarmPoint.attributeNames();
        }

        @Override
        @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.REQUIRED )
        public String attribute( String name )
        {
            AlarmPoint alarmPoint = findAlarmPoint();
            return alarmPoint.attribute( name );
        }

        @Override
        @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.REQUIRED )
        public void setAttribute( String name, @Optional String value )
        {
            AlarmPoint alarmPoint = findAlarmPoint();
            alarmPoint.setAttribute( name, value );
        }

        @Override
        @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.REQUIRED )
        public String name()
        {
            AlarmPoint alarmPoint = findAlarmPoint();
            return alarmPoint.name();
        }

        @Override
        @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.REQUIRED )
        public String descriptionInDefaultLocale()
        {
            AlarmPoint alarmPoint = findAlarmPoint();
            return alarmPoint.descriptionInDefaultLocale();
        }

        @Override
        @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.REQUIRED )
        public String description( Locale locale )
        {
            AlarmPoint alarmPoint = findAlarmPoint();
            return alarmPoint.description( locale );
        }

        private AlarmPoint findAlarmPoint()
        {
            return module.currentUnitOfWork().get( AlarmPoint.class, identity );
        }
    }
}
