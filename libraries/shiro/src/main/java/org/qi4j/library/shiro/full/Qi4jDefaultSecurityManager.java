/*
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.shiro.full;

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.Realm;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.functional.Function;

import static org.qi4j.functional.Iterables.map;
import static org.qi4j.functional.Iterables.toList;

public class Qi4jDefaultSecurityManager
        extends DefaultSecurityManager
{

    public Qi4jDefaultSecurityManager( @Service Iterable<ServiceReference<Realm>> realmsRef )
    {
        super( toList( map( new Function<ServiceReference<Realm>, Realm>()
        {

            public Realm map( ServiceReference<Realm> from )
            {
                return from.get();
            }

        }, realmsRef ) ) );
    }

}
