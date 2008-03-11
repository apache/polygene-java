/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007-2008, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2007, Alin Dreghiciu. All Rights Reserved. 
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
package org.qi4j.entity;

/**
 * Qi4j exception to be thrown in case that an entity composite
 * was not found during a lookup call.
 */
public class EntityCompositeNotFoundException extends EntitySessionException
{
    private String identity;
    private Class compositeType;

    public EntityCompositeNotFoundException( String message, String identity, Class compositeType )
    {
        super( message );
        this.identity = identity;
        this.compositeType = compositeType;
    }

    public String getIdentity()
    {
        return identity;
    }

    public Class getCompositeType()
    {
        return compositeType;
    }

    @Override public String getMessage()
    {
        return super.getMessage() + "(" + getIdentity() + " of type " + compositeType.getName() + ")";
    }
}
