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
package org.apache.zest.library.scheduler;

import org.apache.zest.api.common.Optional;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.zest.api.unitofwork.concern.UnitOfWorkPropagation.Propagation.REQUIRES_NEW;

@Mixins( FooTask.Mixin.class )
public interface FooTask
    extends Task, Identity
{
    Property<String> input();

    @Optional
    Property<String> output();

    @UseDefaults
    Property<Integer> runCounter();

    abstract class Mixin
        implements Task
    {
        private static final Logger LOGGER = LoggerFactory.getLogger( FooTask.class );

        @This
        private FooTask me;

        @Override
        public void run()
        {
            LOGGER.info( "FooTask.run({})", me.input().get() );
            synchronized( this )
            {
                me.runCounter().set( me.runCounter().get() + 1 );
                LOGGER.info( "Identity: " + me.identity().get() );
                LOGGER.info( " Counter: " + me.runCounter().get() );
            }
            if( me.input().get().equals( Constants.BAZAR ) )
            {
                if( me.output().get() == null )
                {
                    me.output().set( Constants.BAR );
                }
                else
                {
                    me.output().set( me.output().get() + Constants.BAR );
                }
            }
        }
    }
}
