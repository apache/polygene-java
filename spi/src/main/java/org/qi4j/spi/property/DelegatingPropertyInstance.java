/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.property;

import org.qi4j.property.Property;
import org.qi4j.property.PropertyVetoException;
import org.qi4j.property.ReadableProperty;

/**
 * TODO
 */
public class DelegatingPropertyInstance<T>
    implements ReadableProperty<T>
{
    Property<T> delegate;

    public DelegatingPropertyInstance( Property<T> delegate )
    {
        this.delegate = delegate;
    }


    public T get()
    {
        return delegate.get();
    }

    public void set( T newValue ) throws PropertyVetoException
    {
        delegate.set( newValue );
    }

    public <T> T getPropertyInfo( Class<T> infoType )
    {
        return delegate.getPropertyInfo( infoType );
    }

    public String getName()
    {
        return delegate.getName();
    }

    public String getQualifiedName()
    {
        return delegate.getQualifiedName();
    }
}
