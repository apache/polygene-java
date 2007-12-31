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

package org.qi4j.runtime.entity.property;

import org.qi4j.entity.property.PropertyVetoException;
import org.qi4j.entity.property.ReadableProperty;
import org.qi4j.entity.property.WritableProperty;

/**
 * TODO
 */
public class PropertyInvocation<T>
    implements ReadableProperty<T>, WritableProperty<T>
{
    private ReadableProperty<T> read;
    private WritableProperty<T> write;
    private PropertyInstanceValue<T> value;

    public PropertyInvocation( ReadableProperty<T> read, WritableProperty<T> write, PropertyInstanceValue<T> value )
    {
        this.read = read;
        this.write = write;
        this.value = value;
    }

    public T get()
    {
        return read.get();
    }

    public void set( T newValue ) throws PropertyVetoException
    {
        write.set( newValue );
    }

    public void setPropertyInstance( PropertyInstance<T> instance )
    {
        value.setInstance( instance );
    }
}
