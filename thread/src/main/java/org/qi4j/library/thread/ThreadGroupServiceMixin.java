/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
package org.qi4j.library.thread;

import java.util.HashMap;
import org.qi4j.injection.scope.This;
import org.qi4j.service.Configuration;
import org.qi4j.property.Property;

public class ThreadGroupServiceMixin
    implements ThreadGroupService
{
    private ThreadGroup rootGroup;
    private HashMap<String, ThreadGroup> groups;

    public ThreadGroupServiceMixin( @This Configuration<ThreadGroupConfiguration> config )
    {
        ThreadGroupConfiguration configuration = config.configuration();
        Property<String> rootName = configuration.rootGroupName();
        String name = rootName.get();
        groups = new HashMap<String, ThreadGroup>();
        rootGroup = new ThreadGroup( name );
    }

    public ThreadGroup getThreadGroup( String name )
    {
        synchronized( this )
        {
            ThreadGroup tg = groups.get( name );
            if( tg == null )
            {
                tg = new ThreadGroup( rootGroup, name );
                groups.put( name, tg );
            }
            return tg;
        }
    }
}
