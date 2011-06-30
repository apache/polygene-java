/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.scheduler.schedule;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.grammar.EqSpecification;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.library.scheduler.SchedulerService;

import static org.qi4j.api.query.QueryExpressions.*;

@Mixins( ScheduleRepository.Mixin.class )
public interface ScheduleRepository
        extends ServiceComposite
{

    Query<ScheduleEntity> findNotDurable();

    Query<ScheduleEntity> findRunning();

    Query<ScheduleEntity> findRunnables( Long from, Long to );

    Query<ScheduleEntity> findNotDurableWithoutNextRun();

    abstract class Mixin
            implements ScheduleRepository
    {

        @Structure
        private UnitOfWorkFactory uowf;
        @Structure
        private QueryBuilderFactory qbf;
        @Service
        private SchedulerService scheduler;

        public Query<ScheduleEntity> findNotDurable()
        {
            QueryBuilder<ScheduleEntity> builder = qbf.newQueryBuilder( ScheduleEntity.class );
            ScheduleEntity template = templateFor( ScheduleEntity.class );
            builder = builder.where( and( eqSchedulerIdentity( template ),
                                          eq( template.durable(), false ) ) );
            return uowf.currentUnitOfWork().newQuery( builder );
        }

        public Query<ScheduleEntity> findRunning()
        {
            QueryBuilder<ScheduleEntity> builder = qbf.newQueryBuilder( ScheduleEntity.class );
            ScheduleEntity template = templateFor( ScheduleEntity.class );
            builder = builder.where( and( eqSchedulerIdentity( template ),
                                          eq( template.running(), true ) ) );
            return uowf.currentUnitOfWork().newQuery( builder );
        }

        public Query<ScheduleEntity> findRunnables( Long from, Long to )
        {
            QueryBuilder<ScheduleEntity> builder = qbf.newQueryBuilder( ScheduleEntity.class );
            ScheduleEntity template = templateFor( ScheduleEntity.class );
            builder = builder.where( and( eqSchedulerIdentity( template ),
                                          eq( template.running(), false ),
                                          ge( template.nextRun(), from ),
                                          lt( template.nextRun(), to ) ) );
            return uowf.currentUnitOfWork().newQuery( builder ).orderBy( orderBy( template.nextRun() ) );
        }

        public Query<ScheduleEntity> findNotDurableWithoutNextRun()
        {
            QueryBuilder<ScheduleEntity> builder = qbf.newQueryBuilder( ScheduleEntity.class );
            ScheduleEntity template = templateFor( ScheduleEntity.class );
            builder = builder.where( and( eqSchedulerIdentity( template ),
                                          eq( template.durable(), false ),
                                          isNull( template.nextRun() ) ) );
            return uowf.currentUnitOfWork().newQuery( builder );
        }

        private EqSpecification<String> eqSchedulerIdentity( ScheduleEntity template )
        {
            return eq( template.schedulerIdentity(), scheduler.identity().get() );
        }

    }

}
