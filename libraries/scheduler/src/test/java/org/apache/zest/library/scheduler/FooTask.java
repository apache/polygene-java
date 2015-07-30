/*
 * Copyright (c) 2010-2014, Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.library.scheduler;

import org.apache.zest.api.common.Optional;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixins( FooTask.Mixin.class )
public interface FooTask
    extends Task, Identity
{
    Property<String> input();

    @Optional
    Property<String> output();

    public static abstract class Mixin
        implements Runnable
    {

        private static final Logger LOGGER = LoggerFactory.getLogger( FooTask.class );

        @This
        private FooTask me;

        @Override
        public void run()
        {
            LOGGER.info( "FooTask.run({})", me.input().get() );
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
