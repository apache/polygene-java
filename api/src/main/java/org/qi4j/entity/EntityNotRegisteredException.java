/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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

import org.qi4j.composite.InvalidApplicationException;
import org.qi4j.structure.Module;

public class EntityNotRegisteredException extends InvalidApplicationException
{
    private static final long serialVersionUID = 7185723686654157891L;

    private Class<? extends EntityComposite> entityType;
    private Module module;

    public EntityNotRegisteredException( Class<? extends EntityComposite> entityType, Module module )
    {
        super( "Trying to find unregistered composite of type [" + entityType.getName() + "] in module [" +
                        module.name().get() + "]." );
        this.entityType = entityType;
        this.module = module;
    }

    public Class<? extends EntityComposite> compositeType()
    {
        return entityType;
    }

    public Module module()
    {
        return this.module;
    }
}