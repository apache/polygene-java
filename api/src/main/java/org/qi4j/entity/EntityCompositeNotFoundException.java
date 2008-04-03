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
public class EntityCompositeNotFoundException extends UnitOfWorkException
{
    private String identity;
    private Class compositeType;

    public EntityCompositeNotFoundException( String identity, Class compositeType )
    {
        this.identity = identity;
        this.compositeType = compositeType;
    }

    public String identity()
    {
        return identity;
    }

    public Class compositeType()
    {
        return compositeType;
    }

    @Override public String getMessage()
    {
        return "Could not find entity (" + identity() + " of type " + compositeType.getName() + ")";
    }
}
