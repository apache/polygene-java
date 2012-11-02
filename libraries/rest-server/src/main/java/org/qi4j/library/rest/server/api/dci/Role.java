/**
 *
 * Copyright 2009-2011 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.rest.server.api.dci;

/**
 * Base class for methodful roles
 */
public class Role<T>
    implements Comparable<Role<T>>
{
    // Self reference to the bound Data object
    protected T self;

    public Role()
    {
    }

    public Role( T self )
    {
        this.self = self;
    }

    public void bind( T newSelf )
    {
        self = newSelf;
    }

    public T self()
    {
        return self;
    }

    @Override
    public boolean equals( Object obj )
    {
        return obj != null && obj instanceof Role && self.equals( ( (Role) obj ).self );
    }

    @Override
    public int compareTo( Role<T> role )
    {
        return ( (Comparable<T>) self ).compareTo( role.self );
    }
}
