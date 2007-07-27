/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.api.persistence.impl;

import org.qi4j.api.persistence.Identity;
import org.qi4j.api.persistence.Persistent;
import org.qi4j.api.annotation.Dependency;
import org.qi4j.api.annotation.Uses;
import java.net.URL;


/**
 * This mixin contains the identity of an object.
 */
public final class IdentityImpl
    implements Identity
{
    private String identity;
    @Uses(optional=true) private Persistent persistence;

    public IdentityImpl( String identity )
    {
        this.identity = identity;
    }

    /** Returns the client view of the identity.
     *
     * It is unique within the owning repository, but potentially not unique globally and between
     * types.
     *
     * @return The Identity of 'this' composite.
     */
    public String getIdentity()
    {
        return identity;
    }

}
