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

package org.apache.zest.library.alarm;

import java.util.List;
import java.util.Locale;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.composite.TransientBuilder;
import org.apache.zest.api.composite.TransientBuilderFactory;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.concern.Concerns;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkConcern;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation;

@Concerns( UnitOfWorkConcern.class )
@Mixins( AlarmProxy.Mixin.class )
public interface AlarmProxy extends AlarmPoint, TransientComposite
{
    @Mixins( FactoryMixin.class )
    interface Factory extends ServiceComposite
    {
        @UnitOfWorkPropagation( UnitOfWorkPropagation.Propagation.REQUIRED )
        AlarmProxy create( String identity, String systemName, String categoryName, AlarmClass alarmClass );
    }

    abstract class FactoryMixin
        implements Factory
    {
        @Structure
        private TransientBuilderFactory tbf;

        @Structure
        private UnitOfWorkFactory uowf;

        @Service
        private AlarmPointFactory factory;

        @Override
        public AlarmProxy create( String identity, String systemName, String categoryName, AlarmClass alarmClass )
        {
            UnitOfWork unitOfWork = uowf.currentUnitOfWork();
            AlarmPoint alarmPoint;
            try
            {
                alarmPoint = unitOfWork.get( AlarmPoint.class, identity );
            }
            catch( NoSuchEntityException e )
            {
                alarmPoint = factory.create( identity, systemName, categoryName, alarmClass );
            }
            TransientBuilder<AlarmProxy> builder = tbf.newTransientBuilder( AlarmProxy.class );
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
        private UnitOfWorkFactory uowf;

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
            return uowf.currentUnitOfWork().get( AlarmPoint.class, identity );
        }
    }
}
