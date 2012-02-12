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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.grammar.EqSpecification;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;

import static org.qi4j.api.query.QueryExpressions.and;
import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.ge;
import static org.qi4j.api.query.QueryExpressions.isNull;
import static org.qi4j.api.query.QueryExpressions.lt;
import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

@Mixins( ScheduleRepository.Mixin.class )
public interface ScheduleRepository
    extends ServiceComposite
{

    Query<ScheduleEntity> findNotDurable( String schedulerIdentity );

    Query<ScheduleEntity> findRunning( String schedulerIdentity );

    Query<ScheduleEntity> findRunnables( String schedulerIdentity, Long from, Long to );

    Query<ScheduleEntity> findNotDurableWithoutNextRun( String schedulerIdentity );

    abstract class Mixin
        implements ScheduleRepository
    {

        @Structure
        private Module module;

        public Query<ScheduleEntity> findNotDurable( String schedulerIdentity )
        {
            QueryBuilder<ScheduleEntity> builder = module.newQueryBuilder( ScheduleEntity.class );
            ScheduleEntity template = templateFor( ScheduleEntity.class );
            builder = builder.where( and( eqSchedulerIdentity( schedulerIdentity, template ),
                                          eq( template.durable(), false ) ) );
            return module.currentUnitOfWork().newQuery( builder );
        }

        public Query<ScheduleEntity> findRunning( String schedulerIdentity )
        {
            QueryBuilder<ScheduleEntity> builder = module.newQueryBuilder( ScheduleEntity.class );
            ScheduleEntity template = templateFor( ScheduleEntity.class );
            builder = builder.where( and( eqSchedulerIdentity( schedulerIdentity, template ),
                                          eq( template.running(), true ) ) );
            return module.currentUnitOfWork().newQuery( builder );
        }

        public Query<ScheduleEntity> findRunnables( String schedulerIdentity, Long from, Long to )
        {
            QueryBuilder<ScheduleEntity> builder = module.newQueryBuilder( ScheduleEntity.class );
            ScheduleEntity template = templateFor( ScheduleEntity.class );
            builder = builder.where( and( eqSchedulerIdentity( schedulerIdentity, template ),
                                          eq( template.running(), false ),
                                          ge( template.nextRun(), from ),
                                          lt( template.nextRun(), to ) ) );
            return module.currentUnitOfWork().newQuery( builder ).orderBy( orderBy( template.nextRun() ) );
        }

        public Query<ScheduleEntity> findNotDurableWithoutNextRun( String schedulerIdentity )
        {
            QueryBuilder<ScheduleEntity> builder = module.newQueryBuilder( ScheduleEntity.class );
            ScheduleEntity template = templateFor( ScheduleEntity.class );
            builder = builder.where( and( eqSchedulerIdentity( schedulerIdentity, template ),
                                          eq( template.durable(), false ),
                                          isNull( template.nextRun() ) ) );
            return module.currentUnitOfWork().newQuery( builder );
        }

        private EqSpecification<String> eqSchedulerIdentity( String schedulerIdentity, Schedule template )
        {
            return eq( template.schedulerIdentity(), schedulerIdentity );
        }
    }
}
