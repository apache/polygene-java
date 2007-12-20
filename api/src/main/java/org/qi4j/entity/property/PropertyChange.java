/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.entity.property;

import org.qi4j.composite.Composite;
import org.qi4j.entity.EntitySession;

/**
 * TODO
 */
public class PropertyChange<T>
{
    private Property<T> property;
    private T newValue;
    private Composite composite;
    private EntitySession entitySession;

    public void set( Property<T> property, T newValue, Composite composite, EntitySession entitySession )
    {
        this.property = property;
        this.newValue = newValue;
        this.composite = composite;
        this.entitySession = entitySession;
    }

    public Property<T> getProperty()
    {
        return property;
    }

    public T getNewValue()
    {
        return newValue;
    }

    public Composite getComposite()
    {
        return composite;
    }

    public EntitySession getEntitySession()
    {
        return entitySession;
    }
}
